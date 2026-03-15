package client.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.User;

public class ClientService {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String host = "localhost";
    private int port = 1234;
    public void connect() throws IOException{
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connected to server at " + host + ":" + port);
    }
    public User login(String username,String password){
        try {
            out.writeObject("LOGIN|" + username + "|" + password);
            out.flush();
            Object response = in.readObject();
            if(response instanceof String && response.equals("LOGIN_SUCCESS")){
                User loggedInUser = (User) in.readObject();
                return loggedInUser;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ObjectInputStream getIn() {
        return in;
    }
    public ObjectOutputStream getOut() {
        return out;
    }
}
