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
                        String[] parts = cmd.split("\\|");
                        int receiverID = Integer.parseInt(parts[1]);
                        String content = parts[2];
                        UserDAO dao = new UserDAO();
                        dao.saveMessage(this.currentUser.getUserID(), receiverID, content);
                        broadcastMessage(receiverID,content);
                    }else if(cmd.startsWith("GET_HISTORY")){
                        int friendID = Integer.parseInt(cmd.split("\\|")[1]);
                        UserDAO dao = new UserDAO();
                        List<model.Message> history = dao.getMessageHistory(this.currentUser.getUserID(), friendID);
                        out.writeObject(history);
                        out.flush();
                    }else if( cmd.startsWith("REGISTER")){
                        String[] parts = cmd.split("\\|");
                        String user = parts[1];
                        String pass = parts[2];
                        String name = parts[3];
                        UserDAO dao = new UserDAO();
                        if(dao.register(user, pass, name)){
                            out.writeObject("REG_SUCCESS");
                        }else{
                            out.writeObject("REG_FAILED");
                        }
                        out.flush();
                    }
                } 
            }
        } catch (Exception e) {
            System.out.println("Người dùng " + (currentUser != null ? currentUser.getFullName() : "") + " đã ngắt kết nối.");
            
        }finally {
            server.clientHandlers.remove(this);
            closeEverything();

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
    // Trong server.core.ClientHandler
    private void broadcastMessage(int receiverID, String content) {
    // Duyệt qua tất cả những người đang Online
        for (ClientHandler handler : server.clientHandlers) {
        // Gửi cho người nhận (máy B) 
        // VÀ gửi cho chính người gửi (máy A) để xác nhận
        if (handler.currentUser.getUserID() == receiverID || handler == this) {
            try {
                // Giao thức: RECEIVE_MSG | Tên người gửi | Nội dung
                handler.out.writeObject("RECEIVE_MSG|" + this.currentUser.getFullName() + "|" + content);
                handler.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                }
            }
        }
    }
}