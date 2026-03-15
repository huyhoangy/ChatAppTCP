package client.ui;

import java.awt.*;
import javax.swing.*;
import client.ui.component.RoundedPanel;
import model.User;
import java.util.List;

public class MainChatGUI extends JFrame {
    private User currentUser;
    private JPanel pnlFriendsList;
    private JPanel pnlChatContent;
    private JTextField txtMessage;
    private JButton btnSend;
    private JLabel lblTargetName; 
    private client.service.ClientService service;

    public MainChatGUI(User user,client.service.ClientService service) {
        this.currentUser = user;
        this.service = service;
        initComponents();
    }

    private void initComponents(){
        setTitle("Message - " + currentUser.getFullName());
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Cột trái: Danh sách bạn bè ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 700));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
        
        JLabel lblTitle = new JLabel("Đoạn chat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        leftPanel.add(lblTitle, BorderLayout.NORTH);

        pnlFriendsList = new JPanel();
        pnlFriendsList.setLayout(new BoxLayout(pnlFriendsList, BoxLayout.Y_AXIS));
        pnlFriendsList.setBackground(Color.WHITE);
        JScrollPane scrollFriends = new JScrollPane(pnlFriendsList);
        scrollFriends.setBorder(null);
        leftPanel.add(scrollFriends, BorderLayout.CENTER);

        // --- Vùng chat chính ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setPreferredSize(new Dimension(0, 70));
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        lblTargetName = new JLabel(" Chọn một người để bắt đầu chat");
        lblTargetName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTargetName.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        chatHeader.add(lblTargetName, BorderLayout.CENTER);
        rightPanel.add(chatHeader, BorderLayout.NORTH);

        pnlChatContent = new JPanel();
        pnlChatContent.setLayout(new BoxLayout(pnlChatContent, BoxLayout.Y_AXIS));
        pnlChatContent.setBackground(new Color(245, 245, 245));
        JScrollPane scrollChat = new JScrollPane(pnlChatContent);
        scrollChat.setBorder(null);
        rightPanel.add(scrollChat, BorderLayout.CENTER);

        // --- Footer nhập tin nhắn ---
        JPanel chatFooter = new JPanel(new BorderLayout(10, 0));
        chatFooter.setPreferredSize(new Dimension(0, 60));
        chatFooter.setBackground(Color.WHITE);
        chatFooter.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        txtMessage = new JTextField();
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatFooter.add(txtMessage, BorderLayout.CENTER);

        btnSend = new JButton("Gửi");
        btnSend.setBackground(new Color(106, 43, 226));
        btnSend.setForeground(Color.WHITE);
        chatFooter.add(btnSend, BorderLayout.EAST);

        rightPanel.add(chatFooter, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    public void addMessage(String text, boolean isMe) {
        JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Thêm để tránh bong bóng chat quá cao

        RoundedPanel bubble = new RoundedPanel(15, isMe ? new Color(106, 43, 226) : new Color(230, 230, 230));
        JLabel lblText = new JLabel("<html><p style=\"width: 200px\">" + text + "</p></html>");
        lblText.setForeground(isMe ? Color.WHITE : Color.BLACK);
        lblText.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        bubble.add(lblText);
        row.add(bubble);
        pnlChatContent.add(row);
        
        pnlChatContent.revalidate(); 
        pnlChatContent.repaint();
        
        // Tự động cuộn xuống cuối
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane)pnlChatContent.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void addFriendItem(User friend) {
        JPanel item = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        item.setBackground(java.awt.Color.WHITE);
        item.setMaximumSize(new java.awt.Dimension(300, 60));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblName = new JLabel(friend.getFullName());
        lblName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
        
        item.add(new JLabel("●")); 
        item.add(lblName);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblTargetName.setText(" Đang chat với: " + friend.getFullName());
                pnlChatContent.removeAll();
                pnlChatContent.revalidate();
                pnlChatContent.repaint();
                try {
                    service.getOut().writeObject("GET_HISTORY|"+ friend.getUserID());
                    service.getOut().flush();
                    
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                
            }
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(Color.WHITE);
            }
        });

        pnlFriendsList.add(item); 
        pnlFriendsList.revalidate();
        pnlFriendsList.repaint();
    }

    public JButton getBtnSend() { return btnSend; }
    public JTextField getTxtMessage() { return txtMessage; }
    public JPanel getPnlChatContent() { return pnlChatContent; }
}