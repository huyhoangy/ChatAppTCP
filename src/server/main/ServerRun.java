package server.main;

import server.core.ChatServer;

public class ServerRun {
    public static void main(String[] args) {
        ChatServer server = new ChatServer(1234);
        server.start();
    }
}
