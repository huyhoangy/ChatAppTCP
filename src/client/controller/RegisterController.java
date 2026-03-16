package client.controller;

import javax.swing.JOptionPane;

import client.service.ClientService;
import client.ui.LoginGUI;
import client.ui.RegisterGUI;

public class RegisterController {
    private RegisterGUI view;
    private ClientService service;  
    public RegisterController(RegisterGUI view, ClientService service) {
        this.view = view;
        this.service = service;
    }
    public void init(){
        view.getBtnRegister().addActionListener(e -> handleRegister());
        view.getLblBackToLogin().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt ){
                LoginGUI loginView = new LoginGUI();
                new LoginController(loginView, service).init();
                loginView.setVisible(true);
                view.dispose();

            }
        });        
    }
    private void handleRegister(){
        String user=view.getTxtUsername().getText().trim();
        String pass=new String(view.getTxtPassword().getPassword());
        String name=view.getTxtFullName().getText().trim();
        String confirm = new String(view.getTxtConfirmPassword().getPassword());
        if(user.isEmpty() || pass.isEmpty() || name.isEmpty()){
            javax.swing.JOptionPane.showMessageDialog(view, "Vui lòng điền đầy đủ thông tin!");
            return;
        }
        if(!pass.equals(confirm)){
            javax.swing.JOptionPane.showMessageDialog(view, "Mật khẩu xác nhận không khớp!");
            return;
        }
        try{
            service.connect();
            service.getOut().writeObject("REGISTER|"+ user + "|"+pass +"|"+ name);
            service.getOut().flush();
            String response = (String) service.getIn().readObject();
            if ("REG_SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(view, "Đăng ký thành công! Hãy đăng nhập.");
                view.getLblBackToLogin().dispatchEvent(new java.awt.event.MouseEvent(view.getLblBackToLogin(), 
                        java.awt.event.MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 1, false));
            } else {
                JOptionPane.showMessageDialog(view, "Đăng ký thất bại. Tên đăng nhập có thể đã tồn tại.");
            }
        }
        catch(Exception e){
            javax.swing.JOptionPane.showMessageDialog(view, "Đăng ký thất bại: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
