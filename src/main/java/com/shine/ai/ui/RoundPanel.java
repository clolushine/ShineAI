package com.shine.ai.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundPanel extends JComponent {
    private int arcWidth = 8;
    private int arcHeight = 8;

    public RoundPanel(BorderLayout borderLayout) {
        setLayout(borderLayout);
    }

    public RoundPanel() {}

    public void setArc(int arcWidth, int arcHeight) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        repaint(); // 重新绘制
    }

//    @Override
//    public void setBackground(Color bg) {
//        super.setBackground(bg);
//        repaint();
//    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground()); // 使用面板的背景颜色

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置圆角剪裁区域
        g2.setClip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arcWidth, arcHeight));
        // 绘制圆角矩形
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }
}
