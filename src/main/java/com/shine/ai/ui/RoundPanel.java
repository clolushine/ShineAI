package com.shine.ai.ui;

import com.intellij.openapi.Disposable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundPanel extends JComponent implements Disposable {
    private int arcWidth = 6;
    private int arcHeight = 6;

    public RoundPanel(LayoutManager borderLayout) {
        super();
        setDoubleBuffered(true);
        setLayout(borderLayout);
    }

    public RoundPanel() {
        super();
        setDoubleBuffered(true);
    }

    public void setArc(int arcWidth, int arcHeight) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        repaint(); // 重新绘制
    }

    public void setBG(Color color) {
        setBackground(color);
        repaint(); // 重新绘制
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 先绘制默认背景
        super.paintComponent(g2);

        g2.setColor(getBackground()); // 使用面板的背景颜色

        // 绘制圆角矩形
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), this.arcWidth, this.arcHeight));

        g2.dispose();
    }

    @Override
    public void dispose() {
        super.getGraphics().dispose();
    }
}
