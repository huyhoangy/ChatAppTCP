package server.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private int port;
    public static List<ClientHandler> clientHandlers = new ArrayList<>();
    public ChatServer(int port){
        this.port = port;
    }
    public void start(){
        try (
            ServerSocket serverSocket = new ServerSocket(port)
        )
        {
         System.out.println("Chat Server is running on port " + port);
         while(true){
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress());
            ClientHandler handler = new ClientHandler(socket, this);
            clientHandlers.add(handler);
            Thread thread = new Thread(handler);
            thread.start();
         }   
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Error in ChatServer: " + e.getMessage());
        }
    }
    
}
