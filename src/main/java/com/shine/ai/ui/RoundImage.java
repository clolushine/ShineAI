package com.shine.ai.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundImage extends JComponent {

    private Image image;
    private int arcWidth = 10; // 圆角弧度宽度
    private int arcHeight = 10; // 圆角弧度高度

    public RoundImage(Image image) {
        this.image = image;
        setOpaque(false);
    }

    public void setArc(int arcWidth, int arcHeight) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        repaint(); // 重新绘制
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 创建圆角矩形
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, width, height, arcWidth, arcHeight);
        g2d.setClip(roundRect);

        g2d.drawImage(image, 0, 0, width, height, this);
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(null), image.getHeight(null));
    }
}

