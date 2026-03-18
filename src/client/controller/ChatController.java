package client.controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import client.service.ClientService;
import client.ui.MainChatGUI;
import model.User;
import model.Message;

public class ChatController {
    private MainChatGUI view;
    private ClientService service;
    private User currentUser;

    public ChatController(MainChatGUI view, ClientService service, User user) {
        this.view = view;
        this.service = service;
        this.currentUser = user;
    }

    public void init() {
        // 1. Sự kiện Gửi tin nhắn (Enter & Click)
        view.getBtnSend().addActionListener(e -> sendMessage());
        view.getTxtMessage().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
            }
        });

        // 2. Sự kiện Tìm kiếm bạn bè
        view.getTxtSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String kw = view.getTxtSearch().getText().trim();
                    if (!kw.isEmpty()) {
                        try {
                            service.getOut().writeObject("SEARCH|" + kw);
                            service.getOut().flush();
                            view.getTxtSearch().setText("");
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }
            }
        });

        // 3. Luồng nhận dữ liệu DUY NHẤT từ Server
        new Thread(() -> {
            try {
                // ĐỌC DỮ LIỆU BAN ĐẦU (Thứ tự phải khớp với handleLogin ở Server)
                
                // Bước A: Đọc danh sách bạn bè
                Object objFriends = service.getIn().readObject();
                if (objFriends instanceof List) {
                    List<User> friends = (List<User>) objFriends;
                    SwingUtilities.invokeLater(() -> {
                        for (User u : friends) view.addFriendItem(u);
                    });
                }

                // Bước B: Đọc danh sách nhóm (List<String>)
                Object objGroups = service.getIn().readObject();
                if (objGroups instanceof List) {
                    List<String> groups = (List<String>) objGroups;
                    SwingUtilities.invokeLater(() -> {
                        for (String gStr : groups) {
                            String[] gParts = gStr.split("\\|");
                            view.addGroupItem(Integer.parseInt(gParts[0]), gParts[1]);
                        }
                    });
                }

                // VÒNG LẶP NHẬN DỮ LIỆU LIÊN TỤC (Tin nhắn, Kết quả tìm kiếm...)
                while (true) {
                    Object obj = service.getIn().readObject();
                    if (obj instanceof List) {
                        handleListResponse((List<?>) obj);
                    } else if (obj instanceof String) {
                        handleStringResponse((String) obj);
                    }
                }
            } catch (Exception e) {
                System.out.println("Mất kết nối với Server.");
            }
        }).start();
    }

    private void handleListResponse(List<?> list) {
        if (list.isEmpty()) return;

        // Xử lý Lịch sử Chat hoặc Kết quả tìm kiếm
        if (list.get(0) instanceof User) {
            showSearchDialog((List<User>) list);
        } else if (list.get(0) instanceof Message) {
            List<Message> history = (List<Message>) list;
            SwingUtilities.invokeLater(() -> {
                view.getPnlChatContent().removeAll();
                for (Message m : history) {
                    view.addMessage(m.getContent(), m.getSenderID() == currentUser.getUserID());
                }
                view.getPnlChatContent().revalidate();
                view.getPnlChatContent().repaint();
            });
        }
    }

    private void handleStringResponse(String res) {
        SwingUtilities.invokeLater(() -> {
            // Nhận tin nhắn cá nhân
            if (res.startsWith("RECEIVE_MSG")) {
                String[] p = res.split("\\|");
                String senderName = p[1];
                String content = p[2];
                if (view.getLblTargetName().getText().contains(senderName) || senderName.equals(currentUser.getFullName())) {
                    view.addMessage(content, senderName.equals(currentUser.getFullName()));
                }
            } 
            // Nhận tin nhắn Group
            else if (res.startsWith("RECEIVE_GROUP_MSG")) {
                String[] p = res.split("\\|");
                int gID = Integer.parseInt(p[1]);
                String senderName = p[2];
                String content = p[3];
                if (view.getSelectedID() == -gID) {
                    boolean isMe = senderName.equals(currentUser.getFullName());
                    view.addMessage((isMe ? "" : senderName + ": ") + content, isMe);
                }
            }
            // Xử lý tạo nhóm thành công (Hiện ngay lên giao diện)
            else if (res.startsWith("CREATE_GROUP_SUCCESS")) {
                String[] p = res.split("\\|");
                int gID = Integer.parseInt(p[1]);
                String gName = p[2];
                view.addGroupItem(gID, gName);
                JOptionPane.showMessageDialog(view, "Tạo nhóm '" + gName + "' thành công!");
            }
            else if (res.equals("ADD_FRIEND_SUCCESS")) {
                JOptionPane.showMessageDialog(view, "Đã kết bạn thành công!");
            }
        });
    }

    private void sendMessage() {
        System.out.println(">>> NUT GUI DA DUOC BAM!"); // Dòng này để check Event
        String content = view.getTxtMessage().getText().trim();
        int id = view.getSelectedID();
        if (content.isEmpty() || id == 0) return;

        try {
            if (id > 0) { // Chat đơn
                service.getOut().writeObject("MSG|" + id + "|" + content);
            } else { // Chat group
                service.getOut().writeObject("SEND_GROUP_MSG|" + Math.abs(id) + "|" + content);
            }
            service.getOut().flush();
            view.getTxtMessage().setText("");
            System.out.println("Client: Da gui tin nhan thanh cong!");
        } catch (Exception e) { e.printStackTrace(); 
            JOptionPane.showMessageDialog(view, "Loi ket noi: Khong the gui tin nhan!");
        }
    }

    private void showSearchDialog(List<User> results) {
        String[] options = results.stream()
                .map(u -> u.getFullName() + " (@" + u.getUsername() + ")")
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(view, "Kết quả tìm kiếm:", "Tìm bạn", 
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (selected != null) {
            int idx = java.util.Arrays.asList(options).indexOf(selected);
            User target = results.get(idx);
            try {
                service.getOut().writeObject("ADD_FRIEND|" + target.getUserID());
                service.getOut().flush();
                SwingUtilities.invokeLater(() -> view.addFriendItem(target));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
