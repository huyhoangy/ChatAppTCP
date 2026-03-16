package client.controller;

import java.io.ObjectInputStream;
import java.util.List;

import javax.swing.SwingUtilities;

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
                    // Trong Thread của ChatController.java
                    if (obj instanceof List) {
                        List<model.Message> history = (List<model.Message>) obj;   
                       // Đảm bảo chạy trên Event Dispatch Thread của Swing để tránh lỗi giao diện
                        SwingUtilities.invokeLater(() -> {
                        view.getPnlChatContent().removeAll(); // Chỉ xóa khi đã có dữ liệu trong tay
                        for (model.Message m : history) {
                        boolean isMe = (m.getSenderID() == currentUser.getUserID());
                        view.addMessage(m.getContent(), isMe);
                    }
                    view.getPnlChatContent().revalidate();
                    view.getPnlChatContent().repaint();
                    });
                } 
                    else if (obj instanceof String) {
                        String msg = (String) obj;
                        if (msg.startsWith("RECEIVE_MSG")) {
                        String[] parts = msg.split("\\|");
                        String senderName = parts[1];
                        String content = parts[2];
                        String chattingWith = view.getLblTargetName().getText();
                        if (senderName.equals(currentUser.getFullName()) || chattingWith.contains(senderName)) {
                        boolean isMe = senderName.equals(currentUser.getFullName());
                        view.addMessage(content, isMe);
                    } else {
                        System.out.println("Tin nhắn mới từ " + senderName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Loi nhan du lieu: " + e.getMessage());
            }
        }).start();
    }
    private void sendMessage(){
        String content = view.getTxtMessage().getText().trim();
        int targetID = view.getSelectedFriendID();
        if(!content.isEmpty()&&   targetID != -1){
            try {
                service.getOut().writeObject("MSG|" + targetID + "|" + content);
                service.getOut().flush();
                view.getTxtMessage().setText("");
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } 
    }  
}
