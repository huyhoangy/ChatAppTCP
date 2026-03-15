package client.ui.component;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private int radius;
    private Color backgroundColor;

    /**
     * @param radius Độ bo góc (ví dụ: 30 là bo vừa, 50 là bo tròn mạnh)
     * @param bgColor Màu nền của panel
     */
    public RoundedPanel(int radius, Color bgColor) {
        this.radius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false); // Quan trọng: Để Java không vẽ nền vuông mặc định đè lên phần bo
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Bật chế độ khử răng cưa (Anti-aliasing) để đường cong mượt, không bị vỡ hạt
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ hình chữ nhật bo góc
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
    }
    
    // Nếu bạn muốn đổi màu nền sau khi khởi tạo
    @Override
    public void setBackground(Color bg) {
        this.backgroundColor = bg;
        super.setBackground(bg);
    }
}