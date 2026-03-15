package server.core;

import java.io.*;
import java.net.*;
import java.util.List;

import model.User;
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
            // Khởi tạo luồng gửi/nhận đối tượng (Object)
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                // Đọc lệnh từ Client gửi lên (Dạng String hoặc Object tùy bạn thiết kế)
                Object request = in.readObject();
                
                if (request instanceof String) {
                    String cmd = (String) request;
                    if (cmd.startsWith("LOGIN")) {
                        handleLogin(cmd);
                    } else if(cmd.startsWith("MSG")){
                        
                        broadcastMessage(cmd);
                    }else if(cmd.startsWith("GET_HISTORY")){
                        int friendID = Integer.parseInt(cmd.split("\\|")[1]);
                        UserDAO dao = new UserDAO();
                        List<model.Message> history = dao.getMessageHistory(this.currentUser.getUserID(), friendID);
                        out.writeObject(history);
                        out.flush();
                    }
                } 
            }
        } catch (Exception e) {
            System.out.println("Mot client da ngat ket noi.");
            server.clientHandlers.remove(this);
        } 
    }

    private void handleLogin(String cmd) throws IOException {
        // Giao thức: LOGIN|username|password
        String[] parts = cmd.split("\\|");
        String user = parts[1];
        String pass = parts[2];

        UserDAO dao = new UserDAO();
        User result = dao.checkLogin(user, pass);

        if (result != null) {
            this.currentUser = result;
            out.writeObject("LOGIN_SUCCESS");
            out.writeObject(result); // Gửi đối tượng User về cho Client lưu trữ
            List<User> friends = dao.getFriendList(result.getUserID());
            out.writeObject(friends); // Gửi danh sách bạn bè về cho Client
            out.flush();
        } else {
            out.writeObject("LOGIN_FAILED");
            out.flush();
        }
    }

    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void broadcastMessage(String msg) {
        String[] parts = msg.split("\\|");
        String content = parts[2];
    // Duyệt qua tất cả những người đang kết nối vào Server
        for (ClientHandler handler : ChatServer.clientHandlers) {
        try {
            // Gửi tin nhắn cho tất cả mọi người TRỪ bản thân mình gửi
            if (handler != this) { 
                handler.out.writeObject("RECEIVE_MSG|" + this.currentUser.getFullName() + "|" + content);
                handler.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            }
        }
    }
}