package client.ui;

import client.ui.component.RoundedPanel;
import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginGUI() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng Nhập");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel chính có màu Gradient tím (Bạn có thể dùng code vẽ Gradient ở bước trước)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(106, 43, 226));

        // Form đăng nhập trắng, bo góc
        RoundedPanel form = new RoundedPanel(30, Color.WHITE);
        form.setPreferredSize(new Dimension(350, 450));
        form.setLayout(null);

        JLabel title = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        title.setBounds(0, 40, 350, 30);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        form.add(title);

        // Ô nhập Username
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setBounds(40, 100, 270, 20);
        form.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(40, 125, 270, 35);
        form.add(txtUsername);

        // Ô nhập Password
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setBounds(40, 180, 270, 20);
        form.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(40, 205, 270, 35);
        form.add(txtPassword);

        // Nút Đăng nhập màu tím
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setBounds(40, 300, 270, 45);
        btnLogin.setBackground(new Color(106, 43, 226));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        form.add(btnLogin);

        mainPanel.add(form);
        add(mainPanel);
    }

    // Getters
    public JTextField getTxtUsername() { return txtUsername; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JButton getBtnLogin() { return btnLogin; }
}