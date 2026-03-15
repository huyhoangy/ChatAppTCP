package client.controller;

import java.io.ObjectInputStream;
import java.util.List;

import client.service.ClientService;
import client.ui.MainChatGUI;
import model.User;

public class ChatController {
    private MainChatGUI view;
    private ClientService service;
    private User currentUser;
    public ChatController(MainChatGUI view, ClientService service, User user) {
        this.view = view;
        this.service = service;
        this.currentUser = user;
    }
    public void init(){
        view.getBtnSend().addActionListener(e -> sendMessage());
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = service.getIn().readObject(); 
                    if (obj instanceof List) {
                    List<model.Message> history = (List<model.Message>) obj;
                    view.getPnlChatContent().removeAll();
                    for (model.Message m : history) {
                        boolean isMe = (m.getSenderID() == currentUser.getUserID());
                        view.addMessage(m.getContent(), isMe);
                    }
                    view.getPnlChatContent().revalidate();
                    view.getPnlChatContent().repaint();
                } 
                    else if (obj instanceof String) {
                        String msg = (String) obj;
                        if (msg.startsWith("RECEIVE_MSG")) {
                        String[] parts = msg.split("\\|");
                        String senderName = parts[1];
                        String content = parts[2];
                        boolean isMe = senderName.equals(currentUser.getFullName());
                        if (!isMe) {
                        view.addMessage(content, false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi nhận dữ liệu: " + e.getMessage());
            }
        }).start();
    }
    private void sendMessage(){
        String content = view.getTxtMessage().getText().trim();
        if(!content.isEmpty()){
            try {
                service.getOut().writeObject("MSG|ALL|" + content);
                service.getOut().flush();
                view.addMessage(content, true);
                view.getTxtMessage().setText("");
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } 
    }  
}
