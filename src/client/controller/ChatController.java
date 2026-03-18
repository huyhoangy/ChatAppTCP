package client.controller;

import client.service.ClientService;
import client.ui.MainChatGUI;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import model.Message;
import model.User;

public class ChatController {
    private static final String CTX_SEARCH = "SEARCH";
    private static final String CTX_ADD_MEMBER = "ADD_MEMBER";
    private static final String CTX_REMOVE_MEMBER = "REMOVE_MEMBER";

    private final MainChatGUI view;
    private final ClientService service;
    private final User currentUser;
    private String pendingUserListContext = CTX_SEARCH;
    private int pendingGroupID = 0;

    public ChatController(MainChatGUI view, ClientService service, User user) {
        this.view = view;
        this.service = service;
        this.currentUser = user;
    }

    public void init() {
        view.getBtnSend().addActionListener(e -> sendMessage());
        view.getTxtMessage().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        view.getBtnGroupSettings().addActionListener(e -> showGroupMenu());

        view.getTxtSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String kw = view.getTxtSearch().getText().trim();
                    if (!kw.isEmpty()) {
                        try {
                            pendingUserListContext = CTX_SEARCH;
                            pendingGroupID = 0;
                            service.getOut().writeObject("SEARCH|" + kw);
                            service.getOut().flush();
                            view.getTxtSearch().setText("");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        new Thread(() -> {
            try {
                Object objFriends = service.getIn().readObject();
                if (objFriends instanceof List) {
                    List<User> friends = (List<User>) objFriends;
                    SwingUtilities.invokeLater(() -> friends.forEach(view::addFriendItem));
                }

                Object objGroups = service.getIn().readObject();
                if (objGroups instanceof List) {
                    List<String> groups = (List<String>) objGroups;
                    SwingUtilities.invokeLater(() -> {
                        for (String gStr : groups) {
                            String[] parts = gStr.split("\\|", 2);
                            if (parts.length == 2) {
                                view.addGroupItem(Integer.parseInt(parts[0]), parts[1]);
                            }
                        }
                    });
                }

                while (true) {
                    Object obj = service.getIn().readObject();
                    if (obj instanceof List) {
                        handleListResponse((List<?>) obj);
                    } else if (obj instanceof String) {
                        handleStringResponse((String) obj);
                    }
                }
            } catch (Exception e) {
                System.out.println("Mat ket noi voi server.");
            }
        }).start();
    }

    private void handleListResponse(List<?> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        if (list.get(0) instanceof User) {
            handleUserListResponse((List<User>) list);
            return;
        }

        if (list.get(0) instanceof Message) {
            List<Message> history = (List<Message>) list;
            SwingUtilities.invokeLater(() -> {
                view.getPnlChatContent().removeAll();
                boolean isGroupChat = view.getSelectedID() < 0;

                for (Message m : history) {
                    boolean isMe = m.getSenderID() == currentUser.getUserID();
                    if (isGroupChat) {
                        view.addMessage(m.getSenderName(), m.getContent(), isMe, true);
                    } else {
                        view.addMessage(m.getContent(), isMe);
                    }
                }
                view.getPnlChatContent().revalidate();
                view.getPnlChatContent().repaint();
            });
        }
    }

    private void handleStringResponse(String res) {
        SwingUtilities.invokeLater(() -> {
            if (res.startsWith("RECEIVE_MSG")) {
                String[] p = res.split("\\|", 3);
                if (p.length < 3) {
                    return;
                }
                String senderName = p[1];
                String content = p[2];
                boolean showingThisThread = view.getLblTargetName().getText().contains(senderName)
                        || senderName.equals(currentUser.getFullName());
                if (showingThisThread) {
                    view.addMessage(content, senderName.equals(currentUser.getFullName()));
                }
                return;
            }

            if (res.startsWith("RECEIVE_GROUP_MSG")) {
                String[] p = res.split("\\|", 4);
                if (p.length < 4) {
                    return;
                }
                int groupID = Integer.parseInt(p[1]);
                String senderName = p[2];
                String content = p[3];
                if (view.getSelectedID() == -groupID) {
                    boolean isMe = senderName.equals(currentUser.getFullName());
                    view.addMessage(senderName, content, isMe, true);
                }
                return;
            }

            if (res.startsWith("CREATE_GROUP_SUCCESS")) {
                String[] p = res.split("\\|", 3);
                if (p.length < 3) {
                    return;
                }
                int groupID = Integer.parseInt(p[1]);
                String groupName = p[2];
                view.addGroupItem(groupID, groupName);
                JOptionPane.showMessageDialog(view, "Tao nhom '" + groupName + "' thanh cong!");
                return;
            }

            if (res.startsWith("NOTIFY_RENAME")) {
                String[] p = res.split("\\|", 3);
                if (p.length < 3) {
                    return;
                }
                int groupID = Integer.parseInt(p[1]);
                String newName = p[2];
                view.renameGroupItem(groupID, newName);
                return;
            }

            if (res.startsWith("GROUP_DISBANDED")) {
                String[] p = res.split("\\|", 2);
                if (p.length < 2) {
                    return;
                }
                int groupID = Integer.parseInt(p[1]);
                view.removeGroupItem(groupID);
                JOptionPane.showMessageDialog(view, "Nhom da bi giai tan.");
                return;
            }

            if ("ADD_FRIEND_SUCCESS".equals(res)) {
                JOptionPane.showMessageDialog(view, "Da ket ban thanh cong!");
                return;
            }

            if ("ADD_FRIEND_FAILED".equals(res)) {
                JOptionPane.showMessageDialog(view, "Khong the ket ban.");
                return;
            }

            if (res.startsWith("GROUP_ACTION_FAILED")) {
                JOptionPane.showMessageDialog(view, "Khong the thuc hien thao tac nhom.");
                pendingUserListContext = CTX_SEARCH;
                pendingGroupID = 0;
                return;
            }

            if (res.startsWith("GROUP_MEMBER_ADDED")) {
                JOptionPane.showMessageDialog(view, "Them thanh vien thanh cong.");
                pendingUserListContext = CTX_SEARCH;
                pendingGroupID = 0;
                return;
            }

            if (res.startsWith("GROUP_MEMBER_REMOVED")) {
                JOptionPane.showMessageDialog(view, "Xoa thanh vien thanh cong.");
                pendingUserListContext = CTX_SEARCH;
                pendingGroupID = 0;
            }
        });
    }

    private void sendMessage() {
        String content = view.getTxtMessage().getText().trim();
        int targetID = view.getSelectedID();
        if (content.isEmpty() || targetID == 0) {
            return;
        }

        try {
            if (targetID > 0) {
                service.getOut().writeObject("MSG|" + targetID + "|" + content);
            } else {
                service.getOut().writeObject("SEND_GROUP_MSG|" + Math.abs(targetID) + "|" + content);
            }
            service.getOut().flush();
            view.getTxtMessage().setText("");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Loi ket noi: Khong the gui tin nhan!");
        }
    }

    private void handleUserListResponse(List<User> results) {
        if (CTX_ADD_MEMBER.equals(pendingUserListContext)) {
            showPickMemberDialog(results, "Chon thanh vien de them", "ADD_GROUP_MEMBER");
            return;
        }

        if (CTX_REMOVE_MEMBER.equals(pendingUserListContext)) {
            showPickMemberDialog(results, "Chon thanh vien de xoa", "REMOVE_GROUP_MEMBER");
            return;
        }

        showSearchDialog(results);
    }

    private void showSearchDialog(List<User> results) {
        if (results == null || results.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Khong tim thay nguoi dung phu hop.");
            return;
        }

        String[] options = results.stream()
                .map(u -> u.getFullName() + " (@" + u.getUsername() + ")")
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                view,
                "Ket qua tim kiem:",
                "Tim ban",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selected == null) {
            return;
        }

        int idx = Arrays.asList(options).indexOf(selected);
        if (idx < 0) {
            return;
        }

        User target = results.get(idx);
        try {
            service.getOut().writeObject("ADD_FRIEND|" + target.getUserID());
            service.getOut().flush();
            SwingUtilities.invokeLater(() -> view.addFriendItem(target));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPickMemberDialog(List<User> users, String title, String actionCmd) {
        if (users == null || users.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Khong co du lieu thanh vien phu hop.");
            pendingUserListContext = CTX_SEARCH;
            pendingGroupID = 0;
            return;
        }

        String[] options = users.stream()
                .map(u -> u.getFullName() + " (@" + u.getUsername() + ")")
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                view,
                title,
                "Quan ly thanh vien",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selected != null) {
            int idx = Arrays.asList(options).indexOf(selected);
            if (idx >= 0) {
                User picked = users.get(idx);
                sendCmd(actionCmd + "|" + pendingGroupID + "|" + picked.getUserID());
            }
        }

        pendingUserListContext = CTX_SEARCH;
        pendingGroupID = 0;
    }

    private void showGroupMenu() {
        if (view.getSelectedID() >= 0) {
            return;
        }

        JPopupMenu menu = new JPopupMenu();

        JMenuItem addMember = new JMenuItem("Them thanh vien");
        JMenuItem removeMember = new JMenuItem("Xoa thanh vien");
        JMenuItem rename = new JMenuItem("Doi ten nhom");
        JMenuItem deleteGroup = new JMenuItem("Giai tan nhom");

        menu.add(addMember);
        menu.add(removeMember);
        menu.addSeparator();
        menu.add(rename);
        menu.addSeparator();
        menu.add(deleteGroup);

        menu.show(view.getBtnGroupSettings(), 0, view.getBtnGroupSettings().getHeight());

        addMember.addActionListener(e -> {
            pendingUserListContext = CTX_ADD_MEMBER;
            pendingGroupID = Math.abs(view.getSelectedID());
            sendCmd("GET_ADDABLE_MEMBERS|" + pendingGroupID);
        });

        removeMember.addActionListener(e -> {
            pendingUserListContext = CTX_REMOVE_MEMBER;
            pendingGroupID = Math.abs(view.getSelectedID());
            sendCmd("GET_GROUP_MEMBERS|" + pendingGroupID);
        });

        rename.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(view, "Nhap ten nhom moi:");
            if (newName != null && !newName.trim().isEmpty()) {
                sendCmd("RENAME_GROUP|" + Math.abs(view.getSelectedID()) + "|" + newName.trim());
            }
        });

        deleteGroup.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view, "Ban co chac muon giai tan nhom?");
            if (confirm == JOptionPane.YES_OPTION) {
                sendCmd("DISBAND_GROUP|" + Math.abs(view.getSelectedID()));
            }
        });
    }

    private void sendCmd(String cmd) {
        try {
            service.getOut().writeObject(cmd);
            service.getOut().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
