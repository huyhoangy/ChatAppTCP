package server.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import model.Message;
import model.User;
import server.dao.UserDAO;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final ChatServer server;
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
                if (!(request instanceof String)) {
                    continue;
                }

                String cmd = (String) request;
                System.out.println("Server nhan: " + cmd);

                if (cmd.startsWith("LOGIN")) {
                    handleLogin(cmd);
                } else if (cmd.startsWith("REGISTER")) {
                    handleRegister(cmd);
                } else if (cmd.startsWith("SEARCH")) {
                    handleSearch(cmd);
                } else if (cmd.startsWith("ADD_FRIEND")) {
                    handleAddFriend(cmd);
                } else if (cmd.startsWith("GET_HISTORY")) {
                    handleGetHistory(cmd);
                } else if (cmd.startsWith("MSG")) {
                    handlePrivateMessage(cmd);
                } else if (cmd.startsWith("SEND_GROUP_MSG")) {
                    handleGroupMessage(cmd);
                } else if (cmd.startsWith("CREATE_GROUP")) {
                    handleCreateGroup(cmd);
                } else if (cmd.startsWith("GET_GROUP_HISTORY")) {
                    handleGetGroupHistory(cmd);
                } else if (cmd.startsWith("RENAME_GROUP")) {
                    handleRenameGroup(cmd);
                } else if (cmd.startsWith("DISBAND_GROUP")) {
                    handleDisbandGroup(cmd);
                } else if (cmd.startsWith("GET_ADDABLE_MEMBERS")) {
                    handleGetAddableMembers(cmd);
                } else if (cmd.startsWith("GET_GROUP_MEMBERS")) {
                    handleGetGroupMembers(cmd);
                } else if (cmd.startsWith("ADD_GROUP_MEMBER")) {
                    handleAddGroupMember(cmd);
                } else if (cmd.startsWith("REMOVE_GROUP_MEMBER")) {
                    handleRemoveGroupMember(cmd);
                }
            }
        } catch (Exception e) {
            System.out.println("Ngat ket noi voi: " + (currentUser != null ? currentUser.getFullName() : "Unknown"));
        } finally {
            server.clientHandlers.remove(this);
            closeEverything();
        }
    }

    private void handleLogin(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 3);
        if (parts.length < 3) {
            out.writeObject("LOGIN_FAILED");
            out.flush();
            return;
        }

        UserDAO dao = new UserDAO();
        User result = dao.checkLogin(parts[1], parts[2]);
        if (result != null) {
            currentUser = result;
            out.writeObject("LOGIN_SUCCESS");
            out.writeObject(result);
            out.writeObject(dao.getFriendList(result.getUserID()));
            out.writeObject(dao.getUserGroups(result.getUserID()));
        } else {
            out.writeObject("LOGIN_FAILED");
        }
        out.flush();
    }

    private void handleRegister(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 4);
        if (parts.length < 4) {
            out.writeObject("REG_FAILED");
            out.flush();
            return;
        }

        UserDAO dao = new UserDAO();
        if (dao.register(parts[1], parts[2], parts[3])) {
            out.writeObject("REG_SUCCESS");
        } else {
            out.writeObject("REG_FAILED");
        }
        out.flush();
    }

    private void handleSearch(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 2);
        if (parts.length < 2) {
            out.writeObject(new ArrayList<>());
            out.flush();
            return;
        }

        out.writeObject(new UserDAO().searchUsers(currentUser.getUserID(), parts[1]));
        out.flush();
    }

    private void handleAddFriend(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 2);
        if (parts.length < 2) {
            out.writeObject("ADD_FRIEND_FAILED");
            out.flush();
            return;
        }

        int targetID = Integer.parseInt(parts[1]);
        if (new UserDAO().addFriend(currentUser.getUserID(), targetID)) {
            out.writeObject("ADD_FRIEND_SUCCESS");
        } else {
            out.writeObject("ADD_FRIEND_FAILED");
        }
        out.flush();
    }

    private void handleGetHistory(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 2);
        if (parts.length < 2) {
            out.writeObject(new ArrayList<Message>());
            out.flush();
            return;
        }

        int friendID = Integer.parseInt(parts[1]);
        out.writeObject(new UserDAO().getMessageHistory(currentUser.getUserID(), friendID));
        out.flush();
    }

    private void handlePrivateMessage(String cmd) {
        String[] parts = cmd.split("\\|", 3);
        if (parts.length < 3) {
            return;
        }

        int receiverID = Integer.parseInt(parts[1]);
        String content = parts[2];
        new UserDAO().saveMessage(currentUser.getUserID(), receiverID, content);
        broadcastPrivate(receiverID, content);
    }

    private void broadcastPrivate(int receiverID, String content) {
        for (ClientHandler h : server.clientHandlers) {
            if (h.currentUser != null && (h.currentUser.getUserID() == receiverID || h == this)) {
                try {
                    h.out.writeObject("RECEIVE_MSG|" + currentUser.getFullName() + "|" + content);
                    h.out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleCreateGroup(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 3);
        if (parts.length < 3) {
            out.writeObject("GROUP_ACTION_FAILED|CREATE");
            out.flush();
            return;
        }

        String groupName = parts[1];
        String[] memberIds = parts[2].split(",");
        UserDAO dao = new UserDAO();
        int groupID = dao.createGroup(groupName, currentUser.getUserID());

        if (groupID <= 0) {
            out.writeObject("GROUP_ACTION_FAILED|CREATE");
            out.flush();
            return;
        }

        for (String idStr : memberIds) {
            if (!idStr.trim().isEmpty()) {
                dao.addGroupMembers(groupID, Integer.parseInt(idStr.trim()));
            }
        }

        out.writeObject("CREATE_GROUP_SUCCESS|" + groupID + "|" + groupName);
        out.flush();
    }

    private void handleGetGroupHistory(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 2);
        if (parts.length < 2) {
            out.writeObject(new ArrayList<Message>());
            out.flush();
            return;
        }

        int groupID = Integer.parseInt(parts[1]);
        List<Message> history = new UserDAO().getGroupMessageHistory(groupID);
        out.writeObject(history != null ? history : new ArrayList<Message>());
        out.flush();
    }

    private void handleGroupMessage(String cmd) {
        String[] parts = cmd.split("\\|", 3);
        if (parts.length < 3) {
            return;
        }

        int groupID = Integer.parseInt(parts[1]);
        String content = parts[2];

        new UserDAO().saveGroupMessage(currentUser.getUserID(), groupID, content);
        broadcastToGroup(groupID, content);
    }

    private void broadcastToGroup(int groupID, String content) {
        List<Integer> memberIDs = new UserDAO().getGroupMembers(groupID);
        for (ClientHandler h : server.clientHandlers) {
            if (h.currentUser != null && memberIDs.contains(h.currentUser.getUserID())) {
                try {
                    h.out.writeObject("RECEIVE_GROUP_MSG|" + groupID + "|" + currentUser.getFullName() + "|" + content);
                    h.out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleRenameGroup(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 3);
        if (parts.length < 3) {
            out.writeObject("GROUP_ACTION_FAILED|RENAME");
            out.flush();
            return;
        }

        int groupID = Integer.parseInt(parts[1]);
        String newName = parts[2].trim();
        if (newName.isEmpty()) {
            out.writeObject("GROUP_ACTION_FAILED|RENAME");
            out.flush();
            return;
        }

        UserDAO dao = new UserDAO();
        if (dao.renameGroup(groupID, newName)) {
            List<Integer> memberIDs = dao.getGroupMembers(groupID);
            for (ClientHandler h : server.clientHandlers) {
                if (h.currentUser != null && memberIDs.contains(h.currentUser.getUserID())) {
                    h.out.writeObject("NOTIFY_RENAME|" + groupID + "|" + newName);
                    h.out.flush();
                }
            }
        } else {
            out.writeObject("GROUP_ACTION_FAILED|RENAME");
            out.flush();
        }
    }

    private void handleDisbandGroup(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 2);
        if (parts.length < 2) {
            out.writeObject("GROUP_ACTION_FAILED|DISBAND");
            out.flush();
            return;
        }

        int groupID = Integer.parseInt(parts[1]);
        UserDAO dao = new UserDAO();
        List<Integer> memberIDs = dao.getGroupMembers(groupID);

        if (dao.disbandGroup(groupID, currentUser.getUserID())) {
            for (ClientHandler h : server.clientHandlers) {
                if (h.currentUser != null && memberIDs.contains(h.currentUser.getUserID())) {
                    h.out.writeObject("GROUP_DISBANDED|" + groupID);
                    h.out.flush();
                }
            }
        } else {
            out.writeObject("GROUP_ACTION_FAILED|DISBAND");
            out.flush();
        }
    }

    private void handleGetAddableMembers(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 2);
        if (parts.length < 2) {
            out.writeObject(new ArrayList<User>());
            out.flush();
            return;
        }
        int groupID = Integer.parseInt(parts[1]);
        UserDAO dao = new UserDAO();
        out.writeObject(dao.getAddableFriendsForGroup(currentUser.getUserID(), groupID));
        out.flush();
    }

    private void handleGetGroupMembers(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 2);
        if (parts.length < 2) {
            out.writeObject(new ArrayList<User>());
            out.flush();
            return;
        }
        int groupID = Integer.parseInt(parts[1]);
        UserDAO dao = new UserDAO();
        out.writeObject(dao.getGroupMemberUsers(groupID));
        out.flush();
    }

    private void handleAddGroupMember(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 3);
        if (parts.length < 3) {
            out.writeObject("GROUP_ACTION_FAILED|ADD_MEMBER");
            out.flush();
            return;
        }
        int groupID = Integer.parseInt(parts[1]);
        int userID = Integer.parseInt(parts[2]);
        UserDAO dao = new UserDAO();
        if (!dao.isGroupAdmin(groupID, currentUser.getUserID())) {
            out.writeObject("GROUP_ACTION_FAILED|ADD_MEMBER");
            out.flush();
            return;
        }
        if (dao.addMemberToGroup(groupID, userID)) {
            out.writeObject("GROUP_MEMBER_ADDED|" + groupID + "|" + userID);
        } else {
            out.writeObject("GROUP_ACTION_FAILED|ADD_MEMBER");
        }
        out.flush();
    }

    private void handleRemoveGroupMember(String cmd) throws IOException {
        String[] parts = cmd.split("\\|", 3);
        if (parts.length < 3) {
            out.writeObject("GROUP_ACTION_FAILED|REMOVE_MEMBER");
            out.flush();
            return;
        }
        int groupID = Integer.parseInt(parts[1]);
        int userID = Integer.parseInt(parts[2]);
        UserDAO dao = new UserDAO();
        if (!dao.isGroupAdmin(groupID, currentUser.getUserID())) {
            out.writeObject("GROUP_ACTION_FAILED|REMOVE_MEMBER");
            out.flush();
            return;
        }
        if (dao.removeMemberFromGroup(groupID, userID)) {
            out.writeObject("GROUP_MEMBER_REMOVED|" + groupID + "|" + userID);
        } else {
            out.writeObject("GROUP_ACTION_FAILED|REMOVE_MEMBER");
        }
        out.flush();
    }

    private void closeEverything() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
