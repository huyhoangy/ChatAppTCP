package client.ui;

import client.ui.component.RoundedPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterGUI extends JFrame {
    private JTextField txtUsername, txtFullName;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnRegister;
    private JLabel lblBackToLogin;

    public RegisterGUI() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng Ký Tài Khoản");
        setSize(900, 600); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // --- Panel chính nền tím ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(106, 43, 226));
        
        // --- Form trắng bo góc (Dùng GridBagLayout ở đây để căn chỉnh các field) ---
        RoundedPanel formPanel = new RoundedPanel(30, Color.WHITE);
        formPanel.setPreferredSize(new Dimension(420, 520)); 
        formPanel.setLayout(new GridBagLayout()); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.insets = new Insets(10, 40, 10, 40); 

        // Tiêu đề ĐĂNG KÝ ---
        JLabel lblTitle = new JLabel("ĐĂNG KÝ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 50, 50));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0; 
        gbc.gridy = 0; 
        gbc.gridwidth = 2; 
        gbc.weighty = 0.3; 
        formPanel.add(lblTitle, gbc);

        gbc.weighty = 0.0;
        gbc.gridwidth = 1; 

        addFormField(formPanel, "Tên đăng nhập:", txtUsername = new JTextField(), gbc, 1);
        addFormField(formPanel, "Họ và tên:", txtFullName = new JTextField(), gbc, 2);
        addFormField(formPanel, "Mật khẩu:", txtPassword = new JPasswordField(), gbc, 3);
        addFormField(formPanel, "Xác nhận mật khẩu:", txtConfirmPassword = new JPasswordField(), gbc, 4);

        //  Nút Đăng ký màu tím xịn ---
        btnRegister = new JButton("Đăng Ký");
        btnRegister.setBackground(new Color(106, 43, 226));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnRegister.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0)); 
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setFocusPainted(false);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 40, 10, 40); 
        formPanel.add(btnRegister, gbc);

        //  Nút quay lại Đăng nhập ---
        lblBackToLogin = new JLabel("Đã có tài khoản? Đăng nhập");
        lblBackToLogin.setForeground(new Color(106, 43, 226));
        lblBackToLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblBackToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBackToLogin.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 40, 20, 40); 
        formPanel.add(lblBackToLogin, gbc);

        mainPanel.add(formPanel);
        add(mainPanel);
    }

   
    private void addFormField(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc, int row) {
        // Cấu hình label (bên trái)
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(70, 70, 70));
        
        gbc.gridx = 0; 
        gbc.gridy = row; 
        gbc.weightx = 0.3; 
        gbc.insets = new Insets(10, 40, 5, 10);
        panel.add(label, gbc);

        // Cấu hình field (bên phải)
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210), 1), 
            BorderFactory.createEmptyBorder(8, 10, 8, 10) 
        ));
        
        gbc.gridx = 1; 
        gbc.gridy = row; 
        gbc.weightx = 0.7; 
        gbc.insets = new Insets(10, 0, 5, 40); 
        panel.add(field, gbc);
    }

    public JTextField getTxtUsername() { return txtUsername; }
    public JTextField getTxtFullName() { return txtFullName; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JPasswordField getTxtConfirmPassword() { return txtConfirmPassword; }
    public JButton getBtnRegister() { return btnRegister; }
    public JLabel getLblBackToLogin() { return lblBackToLogin; }
}