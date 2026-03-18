package server.core;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import model.User;
import model.Message;
import server.dao.UserDAO;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ChatServer server;
    private User currentUser;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object request = in.readObject();
                if (request instanceof String) {
                    String cmd = (String) request;
                    System.out.println("Server nhan: " + cmd);

                    if (cmd.startsWith("LOGIN")) handleLogin(cmd);
                    else if (cmd.startsWith("REGISTER")) handleRegister(cmd);
                    else if (cmd.startsWith("SEARCH")) handleSearch(cmd);
                    else if (cmd.startsWith("ADD_FRIEND")) handleAddFriend(cmd);
                    else if (cmd.startsWith("GET_HISTORY")) handleGetHistory(cmd);
                    else if (cmd.startsWith("MSG")) handlePrivateMessage(cmd);
                    else if (cmd.startsWith("SEND_GROUP_MSG")) handleGroupMessage(cmd);
                    else if (cmd.startsWith("CREATE_GROUP")) handleCreateGroup(cmd);
                    // QUAN TRỌNG: Sửa lỗi treo luồng Client
                    else if (cmd.startsWith("GET_GROUP_HISTORY")) handleGetGroupHistory(cmd);
                }
            }
        } catch (Exception e) {
            System.out.println("Ngat ket noi voi: " + (currentUser != null ? currentUser.getFullName() : "Unknown"));
        } finally {
            server.clientHandlers.remove(this);
            closeEverything();
        }
    }

    private void handleGetGroupHistory(String cmd) throws IOException {
        int groupID = Integer.parseInt(cmd.split("\\|")[1]);
        List<Message> history = new UserDAO().getGroupMessageHistory(groupID);
        // Luôn gửi trả một List (dù rỗng) để Client không bị Block
        out.writeObject(history != null ? history : new ArrayList<Message>());
        out.flush();
    }

    private void handleGroupMessage(String cmd) {
        String[] parts = cmd.split("\\|");
        int groupID = Integer.parseInt(parts[1]);
        String content = parts[2];
        new UserDAO().saveGroupMessage(this.currentUser.getUserID(), groupID, content);
        broadcastToGroup(groupID, content);
    }

    private void broadcastToGroup(int groupID, String content) {
        List<Integer> memberIDs = new UserDAO().getGroupMembers(groupID);
        for (ClientHandler h : server.clientHandlers) {
            if (h.currentUser != null && memberIDs.contains(h.currentUser.getUserID())) {
                try {
                    h.out.writeObject("RECEIVE_GROUP_MSG|" + groupID + "|" + this.currentUser.getFullName() + "|" + content);
                    h.out.flush();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    // Các hàm handleLogin, handleRegister... giữ nguyên như bản cũ của bạn
    private void handleLogin(String cmd) throws IOException {
        String[] parts = cmd.split("\\|");
        UserDAO dao = new UserDAO();
        User result = dao.checkLogin(parts[1], parts[2]);
        if (result != null) {
            this.currentUser = result;
            out.writeObject("LOGIN_SUCCESS");
            out.writeObject(result);
            out.writeObject(dao.getFriendList(result.getUserID()));
            out.writeObject(dao.getUserGroups(result.getUserID())); 
            out.flush();
        } else {
            out.writeObject("LOGIN_FAILED");
            out.flush();
        }
    }

    private void handlePrivateMessage(String cmd) {
        String[] parts = cmd.split("\\|");
        int receiverID = Integer.parseInt(parts[1]);
        String content = parts[2];
        new UserDAO().saveMessage(this.currentUser.getUserID(), receiverID, content);
        broadcastPrivate(receiverID, content);
    }

    private void broadcastPrivate(int receiverID, String content) {
        for (ClientHandler h : server.clientHandlers) {
            if (h.currentUser != null && (h.currentUser.getUserID() == receiverID || h == this)) {
                try {
                    h.out.writeObject("RECEIVE_MSG|" + this.currentUser.getFullName() + "|" + content);
                    h.out.flush();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
    
    private void handleSearch(String cmd) throws IOException {
        String keyword = cmd.split("\\|")[1];
        out.writeObject(new UserDAO().searchUsers(this.currentUser.getUserID(), keyword));
        out.flush();
    }

    private void handleAddFriend(String cmd) throws IOException {
        int targetID = Integer.parseInt(cmd.split("\\|")[1]);
        if (new UserDAO().addFriend(this.currentUser.getUserID(), targetID)) {
            out.writeObject("ADD_FRIEND_SUCCESS");
        } else {
            out.writeObject("ADD_FRIEND_FAILED");
        }
        out.flush();
    }

    private void handleGetHistory(String cmd) throws IOException {
        int friendID = Integer.parseInt(cmd.split("\\|")[1]);
        out.writeObject(new UserDAO().getMessageHistory(this.currentUser.getUserID(), friendID));
        out.flush();
    }

    private void handleCreateGroup(String cmd) throws IOException {
        String[] parts = cmd.split("\\|");
        String groupName = parts[1];
        String[] memberIds = parts[2].split(",");
        UserDAO dao = new UserDAO();
        int groupID = dao.createGroup(groupName, this.currentUser.getUserID());
        if (groupID > 0) {
            for (String idStr : memberIds) {
                dao.addGroupMembers(groupID, Integer.parseInt(idStr));
            }
            out.writeObject("CREATE_GROUP_SUCCESS|" + groupID + "|" + groupName);
            out.flush();
        }
    }
        private void handleRegister(String cmd) throws IOException {
    // Giao thức từ Client gửi lên: REGISTER|username|password|fullName
    String[] parts = cmd.split("\\|");
    if (parts.length < 4) {
        out.writeObject("REG_FAILED");
        out.flush();
        return;
    }

    String user = parts[1];
    String pass = parts[2];
    String name = parts[3];

    UserDAO dao = new UserDAO();
    // Gọi hàm register trong UserDAO (bạn đã viết hàm này rồi)
    if (dao.register(user, pass, name)) {
        out.writeObject("REG_SUCCESS");
    } else {
        out.writeObject("REG_FAILED");
    }
    out.flush();
}

    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}