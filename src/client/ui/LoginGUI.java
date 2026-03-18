package client.ui;

import client.controller.LoginController;
import client.controller.RegisterController;
import client.ui.component.RoundedPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginGUI extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblRegister;

    public LoginGUI() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng Nhập Hệ Thống");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        //  Panel chính nền tím (GridBagLayout để căn giữa form trắng)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(106, 43, 226));

        //  Form đăng nhập trắng, bo góc (Absolute Layout để dùng setBounds)
        RoundedPanel form = new RoundedPanel(30, Color.WHITE);
        form.setPreferredSize(new Dimension(350, 480));
        form.setLayout(null);

        // --- Các thành phần trong Form ---
        JLabel title = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        title.setBounds(0, 40, 350, 35);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        form.add(title);

        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setBounds(40, 100, 270, 20);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(40, 125, 270, 35);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(txtUsername);

        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setBounds(40, 180, 270, 20);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(40, 205, 270, 35);
        form.add(txtPassword);

        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setBounds(40, 300, 270, 45);
        btnLogin.setBackground(new Color(106, 43, 226));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        form.add(btnLogin);

        //  Dòng chữ chuyển sang Đăng ký
        lblRegister = new JLabel("Chưa có tài khoản? Đăng ký ngay!");
        lblRegister.setBounds(40, 360, 270, 25);
        lblRegister.setForeground(new Color(106, 43, 226));
        lblRegister.setHorizontalAlignment(SwingConstants.CENTER);
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        
        // Hiệu ứng Hover cho link Đăng ký
        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lblRegister.setText("<html><u>Chưa có tài khoản? Đăng ký ngay!</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblRegister.setText("Chưa có tài khoản? Đăng ký ngay!");
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                RegisterGUI regView = new RegisterGUI();
                client.service.ClientService service = new client.service.ClientService();
                new RegisterController(regView, service).init();
                regView.setVisible(true);
                dispose(); 
            }
        });
        form.add(lblRegister);

        mainPanel.add(form);
        add(mainPanel);
    }

    public JTextField getTxtUsername() { return txtUsername; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JButton getBtnLogin() { return btnLogin; }
}