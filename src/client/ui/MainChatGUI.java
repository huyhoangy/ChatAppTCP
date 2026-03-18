package client.ui;

import client.controller.LoginController;
import client.service.ClientService;
import client.ui.component.RoundedPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import model.User;

public class MainChatGUI extends JFrame {
    private static final Color BG_APP = new Color(243, 245, 249);
    private static final Color BG_LEFT = Color.WHITE;
    private static final Color BG_CHAT = new Color(248, 249, 252);
    private static final Color PRIMARY = new Color(77, 124, 255);
    private static final Color PRIMARY_DARK = new Color(59, 99, 230);
    private static final Color TEXT_MAIN = new Color(35, 40, 52);
    private static final Color TEXT_SUB = new Color(112, 118, 135);

    private final User currentUser;
    private final ClientService service;

    private JPanel pnlFriendsList;
    private JPanel pnlChatContent;
    private JTextField txtMessage;
    private JTextField txtSearch;
    private JButton btnSend;
    private JButton btnGroupSettings;
    private JLabel lblTargetName;
    private JScrollPane chatScrollPane;

    private int selectedID = 0;
    private final List<User> allFriends = new ArrayList<>();
    private final Map<Integer, JPanel> groupItems = new HashMap<>();
    private final Map<Integer, JLabel> groupNameLabels = new HashMap<>();

    public MainChatGUI(User user, ClientService service) {
        this.currentUser = user;
        this.service = service;
        initComponents();
    }

    private void initComponents() {
        setTitle("Message - " + currentUser.getFullName());
        setSize(1140, 730);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_APP);

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(320, 0));
        left.setBackground(BG_LEFT);
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(224, 228, 238)));

        JPanel leftHeader = new JPanel(new BorderLayout(0, 10));
        leftHeader.setBackground(BG_LEFT);
        leftHeader.setBorder(new EmptyBorder(16, 16, 12, 16));

        JPanel titleRow = new JPanel(new BorderLayout(10, 0));
        titleRow.setBackground(BG_LEFT);

        JLabel lblTitle = new JLabel("Doan chat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT_MAIN);

        JButton btnCreateGroup = new JButton("+ Nhom");
        styleSecondaryButton(btnCreateGroup);
        btnCreateGroup.addActionListener(e -> handleCreateGroup());

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(btnCreateGroup, BorderLayout.EAST);

        txtSearch = new JTextField();
        styleInput(txtSearch, "Tim ban be...");

        leftHeader.add(titleRow, BorderLayout.NORTH);
        leftHeader.add(txtSearch, BorderLayout.SOUTH);

        pnlFriendsList = new JPanel();
        pnlFriendsList.setLayout(new BoxLayout(pnlFriendsList, BoxLayout.Y_AXIS));
        pnlFriendsList.setBackground(BG_LEFT);

        JScrollPane scrollFriends = new JScrollPane(pnlFriendsList);
        scrollFriends.setBorder(null);
        scrollFriends.getVerticalScrollBar().setUnitIncrement(18);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(0, 58));
        footer.setBackground(BG_LEFT);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 234, 242)));

        JButton btnLogout = new JButton("Dang xuat");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(new Color(221, 67, 67));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());

        footer.add(btnLogout, BorderLayout.CENTER);

        left.add(leftHeader, BorderLayout.NORTH);
        left.add(scrollFriends, BorderLayout.CENTER);
        left.add(footer, BorderLayout.SOUTH);
        return left;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG_CHAT);

        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 72));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 228, 238)));

        lblTargetName = new JLabel(" Chon mot nguoi hoac nhom de chat");
        lblTargetName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTargetName.setForeground(TEXT_MAIN);
        lblTargetName.setBorder(new EmptyBorder(0, 18, 0, 0));

        btnGroupSettings = new JButton("...");
        styleSecondaryButton(btnGroupSettings);
        btnGroupSettings.setPreferredSize(new Dimension(62, 34));
        btnGroupSettings.setVisible(false);

        JPanel rightBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 18));
        rightBtnWrap.setOpaque(false);
        rightBtnWrap.add(btnGroupSettings);

        header.add(lblTargetName, BorderLayout.CENTER);
        header.add(rightBtnWrap, BorderLayout.EAST);

        pnlChatContent = new JPanel();
        pnlChatContent.setLayout(new BoxLayout(pnlChatContent, BoxLayout.Y_AXIS));
        pnlChatContent.setBackground(BG_CHAT);
        pnlChatContent.setBorder(new EmptyBorder(12, 10, 14, 10));

        chatScrollPane = new JScrollPane(pnlChatContent);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(18);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 14, 10, 14));

        txtMessage = new JTextField();
        styleMessageInput(txtMessage, "Nhap tin nhan...");

        btnSend = new JButton("Gui");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setForeground(Color.WHITE);
        btnSend.setBackground(PRIMARY);
        btnSend.setBorder(new EmptyBorder(9, 20, 9, 20));
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footer.add(txtMessage, BorderLayout.CENTER);
        footer.add(btnSend, BorderLayout.EAST);

        right.add(header, BorderLayout.NORTH);
        right.add(chatScrollPane, BorderLayout.CENTER);
        right.add(footer, BorderLayout.SOUTH);
        return right;
    }

    private void styleInput(JTextField field, String tooltip) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_MAIN);
        field.setBorder(new EmptyBorder(10, 12, 10, 12));
        field.setBackground(new Color(244, 247, 253));
        field.setToolTipText(tooltip);
    }

    private void styleMessageInput(JTextField field, String tooltip) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_MAIN);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(188, 198, 220), 2),
                new EmptyBorder(9, 12, 9, 12)
        ));
        field.setToolTipText(tooltip);
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT_MAIN);
        button.setBackground(new Color(240, 243, 250));
        button.setBorder(new EmptyBorder(7, 12, 7, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void addMessage(String text, boolean isMe) {
        addMessage(null, text, isMe, false);
    }

    public void addMessage(String senderName, String text, boolean isMe, boolean showSenderName) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel flow = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 2));
        flow.setOpaque(false);

        JPanel msgBlock = new JPanel();
        msgBlock.setOpaque(false);
        msgBlock.setLayout(new BoxLayout(msgBlock, BoxLayout.Y_AXIS));

        if (!isMe && showSenderName && senderName != null && !senderName.trim().isEmpty()) {
            JLabel lblName = new JLabel(senderName);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblName.setForeground(TEXT_SUB);
            lblName.setBorder(new EmptyBorder(0, 8, 3, 0));
            msgBlock.add(lblName);
        }

        RoundedPanel bubble = new RoundedPanel(20, isMe ? PRIMARY : new Color(235, 238, 245));
        bubble.setLayout(new BorderLayout());

        JLabel lblMsg = new JLabel(toHtmlText(text, 285));
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(isMe ? Color.WHITE : TEXT_MAIN);
        lblMsg.setBorder(new EmptyBorder(9, 14, 9, 14));

        bubble.add(lblMsg, BorderLayout.CENTER);

        if (!isMe && showSenderName) {
            JPanel contentRow = new JPanel(new BorderLayout(8, 0));
            contentRow.setOpaque(false);
            contentRow.add(createAvatar(senderName), BorderLayout.WEST);
            contentRow.add(bubble, BorderLayout.CENTER);
            msgBlock.add(contentRow);
        } else {
            msgBlock.add(bubble);
        }

        flow.add(msgBlock);
        row.add(flow, BorderLayout.CENTER);

        pnlChatContent.add(row);
        pnlChatContent.add(Box.createVerticalStrut(5));
        pnlChatContent.revalidate();
        pnlChatContent.repaint();
        scrollChatToBottom();
    }

    private JPanel createAvatar(String senderName) {
        JPanel avatar = new JPanel(new GridLayout(1, 1));
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setMaximumSize(new Dimension(30, 30));
        avatar.setBackground(PRIMARY_DARK);

        JLabel initials = new JLabel(getInitials(senderName), JLabel.CENTER);
        initials.setForeground(Color.WHITE);
        initials.setFont(new Font("Segoe UI", Font.BOLD, 11));
        avatar.add(initials);
        return avatar;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String toHtmlText(String text, int width) {
        if (text == null) {
            text = "";
        }
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width:" + width + "px;'>" + escaped + "</body></html>";
    }

    private void scrollChatToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    public void addFriendItem(User f) {
        if (f == null || hasFriend(f.getUserID())) {
            return;
        }

        allFriends.add(f);

        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(BG_LEFT);
        item.setBorder(new EmptyBorder(10, 14, 10, 14));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel dot = new JLabel("●");
        dot.setForeground("Online".equalsIgnoreCase(f.getStatus()) ? new Color(50, 181, 84) : new Color(158, 164, 176));
        dot.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel name = new JLabel(f.getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        name.setForeground(TEXT_MAIN);

        item.add(dot, BorderLayout.WEST);
        item.add(name, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedID = f.getUserID();
                btnGroupSettings.setVisible(false);
                lblTargetName.setText(" Dang chat voi: " + f.getFullName());
                clearChat();
                try {
                    service.getOut().writeObject("GET_HISTORY|" + f.getUserID());
                    service.getOut().flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(246, 248, 253));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(BG_LEFT);
            }
        });

        pnlFriendsList.add(item);
        pnlFriendsList.revalidate();
        pnlFriendsList.repaint();
    }

    public void addGroupItem(int groupID, String groupName) {
        if (groupItems.containsKey(groupID)) {
            renameGroupItem(groupID, groupName);
            return;
        }

        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(BG_LEFT);
        item.setBorder(new EmptyBorder(10, 14, 10, 14));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel icon = new JLabel("#");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        icon.setForeground(PRIMARY_DARK);

        JLabel name = new JLabel(groupName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        name.setForeground(TEXT_MAIN);

        item.add(icon, BorderLayout.WEST);
        item.add(name, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedID = -groupID;
                btnGroupSettings.setVisible(true);
                lblTargetName.setText(" Nhom: " + groupNameLabels.get(groupID).getText());
                clearChat();
                try {
                    service.getOut().writeObject("GET_GROUP_HISTORY|" + groupID);
                    service.getOut().flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(246, 248, 253));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(BG_LEFT);
            }
        });

        groupItems.put(groupID, item);
        groupNameLabels.put(groupID, name);
        pnlFriendsList.add(item);
        pnlFriendsList.revalidate();
        pnlFriendsList.repaint();
    }

    public void renameGroupItem(int groupID, String newName) {
        JLabel lbl = groupNameLabels.get(groupID);
        if (lbl == null) {
            return;
        }
        lbl.setText(newName);
        if (selectedID == -groupID) {
            lblTargetName.setText(" Nhom: " + newName);
        }
        pnlFriendsList.revalidate();
        pnlFriendsList.repaint();
    }

    public void removeGroupItem(int groupID) {
        JPanel item = groupItems.remove(groupID);
        groupNameLabels.remove(groupID);
        if (item != null) {
            pnlFriendsList.remove(item);
            pnlFriendsList.revalidate();
            pnlFriendsList.repaint();
        }

        if (selectedID == -groupID) {
            selectedID = 0;
            btnGroupSettings.setVisible(false);
            lblTargetName.setText(" Chon mot nguoi hoac nhom de chat");
            clearChat();
        }
    }

    private boolean hasFriend(int userID) {
        return allFriends.stream().anyMatch(u -> u.getUserID() == userID);
    }

    private void handleCreateGroup() {
        String groupName = JOptionPane.showInputDialog(this, "Nhap ten nhom chat moi:", "Tao nhom", JOptionPane.QUESTION_MESSAGE);
        if (groupName == null || groupName.trim().isEmpty()) {
            return;
        }

        JList<User> list = new JList<>(allFriends.toArray(new User[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(jlist, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    setText(((User) value).getFullName());
                }
                return this;
            }
        });

        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Chon thanh vien", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        List<User> selectedUsers = list.getSelectedValuesList();
        if (selectedUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon it nhat mot thanh vien!");
            return;
        }

        StringBuilder ids = new StringBuilder();
        for (User u : selectedUsers) {
            ids.append(u.getUserID()).append(',');
        }
        ids.append(currentUser.getUserID());

        try {
            service.getOut().writeObject("CREATE_GROUP|" + groupName.trim() + "|" + ids);
            service.getOut().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        int opt = JOptionPane.showConfirmDialog(this, "Ban muon dang xuat?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.close();
            SwingUtilities.invokeLater(() -> {
                LoginGUI loginUI = new LoginGUI();
                ClientService newService = new ClientService();
                new LoginController(loginUI, newService).init();
                loginUI.setVisible(true);
            });
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void clearChat() {
        pnlChatContent.removeAll();
        pnlChatContent.revalidate();
        pnlChatContent.repaint();
    }

    public JButton getBtnSend() {
        return btnSend;
    }

    public JTextField getTxtMessage() {
        return txtMessage;
    }

    public JTextField getTxtSearch() {
        return txtSearch;
    }

    public JPanel getPnlFriendsList() {
        return pnlFriendsList;
    }

    public JPanel getPnlChatContent() {
        return pnlChatContent;
    }

    public JButton getBtnGroupSettings() {
        return btnGroupSettings;
    }

    public int getSelectedID() {
        return selectedID;
    }

    public JLabel getLblTargetName() {
        return lblTargetName;
    }
}
