package client.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

import client.controller.LoginController;
import client.ui.component.RoundedPanel;
import model.User;
import java.util.List;
import javax.swing.border.EmptyBorder;

public class MainChatGUI extends JFrame {
    private User currentUser;
    private JPanel pnlFriendsList, pnlChatContent;
    private JTextField txtMessage, txtSearch;
    private JButton btnSend;
    private JLabel lblTargetName;
    private client.service.ClientService service;
    private int selectedID = 0; // 0 = chưa chọn, ID dương là User, ID âm là Group (VD: -5 là Group ID 5)
    private java.util.List<User> allFriends = new java.util.ArrayList<>();

    public MainChatGUI(User user, client.service.ClientService service) {
        this.currentUser = user;
        this.service = service;
        initComponents();
    }

    private void initComponents() {
        setTitle("Message - " + currentUser.getFullName());
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- CỘT TRÁI (DANH SÁCH CHAT) ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // Header trái: Tiêu đề + Nút Tạo nhóm
        JPanel leftHeader = new JPanel(new BorderLayout(5, 10));
        leftHeader.setBackground(Color.WHITE);
        leftHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Đoạn chat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        // Nút Tạo nhóm chat
        JButton btnCreateGroup = new JButton("+ Nhóm");
        btnCreateGroup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCreateGroup.setFocusPainted(false);
        btnCreateGroup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreateGroup.addActionListener(e -> handleCreateGroup());
        
        titlePanel.add(lblTitle, BorderLayout.WEST);
        titlePanel.add(btnCreateGroup, BorderLayout.EAST);
        
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        leftHeader.add(titlePanel, BorderLayout.NORTH);
        leftHeader.add(txtSearch, BorderLayout.SOUTH);

        pnlFriendsList = new JPanel();
        pnlFriendsList.setLayout(new BoxLayout(pnlFriendsList, BoxLayout.Y_AXIS));
        pnlFriendsList.setBackground(Color.WHITE);
        JScrollPane scrollFriends = new JScrollPane(pnlFriendsList);
        scrollFriends.setBorder(null);

        // Footer trái: Đăng xuất
        JPanel leftFooter = new JPanel(new BorderLayout());
        leftFooter.setPreferredSize(new Dimension(0, 50));
        leftFooter.setBackground(Color.WHITE);
        leftFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 1, new Color(230, 230, 230)));
        JButton btnLogout = new JButton("🚪 Đăng xuất");
        btnLogout.setForeground(new Color(231, 76, 60));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());
        leftFooter.add(btnLogout);

        leftPanel.add(leftHeader, BorderLayout.NORTH);
        leftPanel.add(scrollFriends, BorderLayout.CENTER);
        leftPanel.add(leftFooter, BorderLayout.SOUTH);

        // --- CỘT PHẢI (VÙNG CHAT) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // Header chat
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setPreferredSize(new Dimension(0, 70));
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        lblTargetName = new JLabel(" Chọn một người hoặc nhóm để chat");
        lblTargetName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTargetName.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        chatHeader.add(lblTargetName, BorderLayout.CENTER);

        // Nội dung chat
        pnlChatContent = new JPanel();
        pnlChatContent.setLayout(new BoxLayout(pnlChatContent, BoxLayout.Y_AXIS));
        pnlChatContent.setBackground(new Color(245, 245, 245));
        JScrollPane scrollChat = new JScrollPane(pnlChatContent);
        scrollChat.setBorder(null);

        // Footer chat: Nhập tin nhắn
        JPanel chatFooter = new JPanel(new BorderLayout(10, 0));
        chatFooter.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        chatFooter.setBackground(Color.WHITE);
        txtMessage = new JTextField();
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnSend = new JButton("Gửi");
        btnSend.setBackground(new Color(106, 43, 226));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chatFooter.add(txtMessage, BorderLayout.CENTER);
        chatFooter.add(btnSend, BorderLayout.EAST);

        rightPanel.add(chatHeader, BorderLayout.NORTH);
        rightPanel.add(scrollChat, BorderLayout.CENTER);
        rightPanel.add(chatFooter, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    // --- CÁC HÀM HỖ TRỢ GIAO DIỆN ---

    public void addMessage(String text, boolean isMe) {
        addMessage(null, text, isMe, false);
    }

    public void addMessage(String senderName, String text, boolean isMe, boolean showSenderName) {
        JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 12, 2));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel messageBlock = new JPanel();
        messageBlock.setOpaque(false);
        messageBlock.setLayout(new BoxLayout(messageBlock, BoxLayout.Y_AXIS));
        messageBlock.setBorder(new EmptyBorder(0, isMe ? 0 : 6, 0, isMe ? 6 : 0));

        if (showSenderName && !isMe && senderName != null && !senderName.trim().isEmpty()) {
            JLabel lblName = new JLabel(senderName);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblName.setForeground(new Color(105, 105, 115));
            lblName.setBorder(new EmptyBorder(0, 8, 4, 0));
            lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
            messageBlock.add(lblName);
        }

        Color myBubble = new Color(106, 43, 226);
        Color otherBubble = new Color(236, 236, 238);
        RoundedPanel bubble = new RoundedPanel(20, isMe ? myBubble : otherBubble);
        bubble.setLayout(new BorderLayout());
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(toHtmlText(text, 260));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(isMe ? Color.WHITE : new Color(35, 35, 35));
        lbl.setBorder(new EmptyBorder(9, 14, 9, 14));

        bubble.add(lbl, BorderLayout.CENTER);
        messageBlock.add(bubble);
        row.add(messageBlock);

        pnlChatContent.add(row);
        pnlChatContent.add(Box.createVerticalStrut(4));
        pnlChatContent.revalidate();
        pnlChatContent.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) pnlChatContent.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private String toHtmlText(String text, int width) {
        if (text == null) return "<html><body style='width:" + width + "px;'></body></html>";
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width:" + width + "px;'>" + escaped + "</body></html>";
    }

    public void addFriendItem(User f) {
        allFriends.add(f);
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        item.setBackground(Color.WHITE);
        item.setMaximumSize(new Dimension(300, 60));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblStatus = new JLabel("●");
        lblStatus.setForeground("Online".equals(f.getStatus()) ? new Color(46, 204, 113) : Color.GRAY);
        
        item.add(lblStatus);
        item.add(new JLabel(f.getFullName()));
        
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedID = f.getUserID(); // ID dương
                lblTargetName.setText(" Đang chat với: " + f.getFullName());
                clearChat();
                try {
                    service.getOut().writeObject("GET_HISTORY|" + f.getUserID());
                    service.getOut().flush();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            @Override
            public void mouseEntered(MouseEvent e) { item.setBackground(new Color(245, 245, 245)); }
            @Override
            public void mouseExited(MouseEvent e) { item.setBackground(Color.WHITE); }
        });
        pnlFriendsList.add(item);
        pnlFriendsList.revalidate();
    }

    public void addGroupItem(int groupID, String groupName) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        item.setBackground(Color.WHITE);
        item.setMaximumSize(new Dimension(300, 60));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblIcon = new JLabel("👥");
        item.add(lblIcon);
        item.add(new JLabel(groupName));
        
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedID = -groupID; // ID âm
                System.out.println("DEBUG: Da chon Group ID = " + selectedID); // Thêm dòng này để kiểm tra
                lblTargetName.setText(" Nhóm: " + groupName);
                clearChat();
                try {
                    service.getOut().writeObject("GET_GROUP_HISTORY|" + groupID);
                    service.getOut().flush();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            @Override
            public void mouseEntered(MouseEvent e) { item.setBackground(new Color(245, 245, 245)); }
            @Override
            public void mouseExited(MouseEvent e) { item.setBackground(Color.WHITE); }
        });
        pnlFriendsList.add(item);
        pnlFriendsList.revalidate();
        pnlFriendsList.repaint();
    }

    private void handleCreateGroup() {
        String groupName = JOptionPane.showInputDialog(this, "Nhập tên nhóm chat mới:", "Tạo nhóm", JOptionPane.QUESTION_MESSAGE);
        if (groupName == null || groupName.trim().isEmpty()) return;
        DefaultListModel<User> listModel = new DefaultListModel<>();
        JList<User> list = new JList<>(allFriends.toArray(new User[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?>list,Object value,int index,boolean isSelected, boolean cellHasFocus){
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(value instanceof User){
                    setText(((User) value).getFullName());
                }
                return this;
            }
        });
        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Chọn thành viên nhóm", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.OK_OPTION){
            List<User> selectedUsers = list.getSelectedValuesList();
            if(selectedUsers.isEmpty()){
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một thành viên!");
                return;
            }
            StringBuilder ids = new StringBuilder();
            for(User u : selectedUsers){
                ids.append(u.getUserID()).append(",");
            }
            ids.append(currentUser.getUserID());
            try {
                service.getOut().writeObject("CREATE_GROUP|"+ groupName.trim()+"|"+ ids.toString());
                service.getOut().flush();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    private void handleLogout() {
        int opt = JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                    service.close();
                    SwingUtilities.invokeLater(() -> {
                client.ui.LoginGUI loginUI = new client.ui.LoginGUI();
                client.service.ClientService newService = new client.service.ClientService();
                new client.controller.LoginController(loginUI, newService).init();
                loginUI.setVisible(true);
            });
            this.dispose();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                System.exit(0);
            }
            new LoginController(new LoginGUI(), new client.service.ClientService()).init();
            dispose();
        }
    }

    private void clearChat() {
        pnlChatContent.removeAll();
        pnlChatContent.revalidate();
        pnlChatContent.repaint();
    }

    // Getters
    public JButton getBtnSend() { return btnSend; }
    public JTextField getTxtMessage() { return txtMessage; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JPanel getPnlFriendsList() { return pnlFriendsList; }
    public JPanel getPnlChatContent() { return pnlChatContent; }
    public int getSelectedID() { return selectedID; }
    public JLabel getLblTargetName() { return lblTargetName; }
}

