package client.controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import client.service.ClientService;
import client.ui.LoginGUI;
import client.ui.MainChatGUI;
import model.User;

public class LoginController {
    private LoginGUI view;
    private ClientService service;

    public LoginController(LoginGUI view, ClientService service) {
        this.view = view;
        this.service = service;
    }

    public void init() {
        view.getBtnLogin().addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String user = view.getTxtUsername().getText();
        String pass = new String(view.getTxtPassword().getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        new Thread(() -> {
            try {
                service.connect();
                User loggedInUser = service.login(user, pass);

                if (loggedInUser != null) {
                    SwingUtilities.invokeLater(() -> {
                        MainChatGUI mainChat = new MainChatGUI(loggedInUser, this.service);
                        
                        ChatController chatController = new ChatController(mainChat, this.service, loggedInUser);
                        chatController.init();
                        
                        mainChat.setVisible(true);
                        view.dispose();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(view, "Đăng nhập thất bại!");
                        service.close();
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(view, "Lỗi kết nối Server!");
                });
                e.printStackTrace();
            }
        }).start();
    }
}