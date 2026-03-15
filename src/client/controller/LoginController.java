package client.controller;

import java.util.List;

import javax.swing.JOptionPane;

import client.service.ClientService;
import client.ui.LoginGUI;
import client.ui.MainChatGUI;
import model.User;

public class LoginController {
    private LoginGUI view ;
    private ClientService service;
    public LoginController(LoginGUI view, ClientService service) {
        this.view = view;
        this.service = service;
    }
    public void init(){
        view.getBtnLogin().addActionListener(e -> handleLogin());
    }
    private void handleLogin(){
        try {
            String user= view.getTxtUsername().getText();
            String pass = new String(view.getTxtPassword().getPassword());
            if(user.isEmpty() || pass.isEmpty()){
                JOptionPane.showMessageDialog(view, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            service.connect();
            User loggedInUser = service.login(user, pass);
            if(loggedInUser != null){
                List<User> friends = (List<User>) service.getIn().readObject();
                MainChatGUI mainChat = new MainChatGUI(loggedInUser, this.service);
                JOptionPane.showMessageDialog(view, "Đăng nhập thành công! Chào " + loggedInUser.getFullName());
                ChatController chatController = new ChatController(mainChat, this.service, loggedInUser);
                chatController.init();
                for (User f : friends) {
                    mainChat.addFriendItem(f);
                }
                mainChat.setVisible(true); 
                view.dispose(); 
            } else {
                JOptionPane.showMessageDialog(view, "Đăng nhập thất bại. Kiểm tra lại tài khoản/mật khẩu.");
                service.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
            JOptionPane.showMessageDialog(view, "Không kết nối được tới Server. Hãy chạy ServerRun trước!");

        }
    }
}
