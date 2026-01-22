/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (usually in the LICENSE file).
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.shine.ai.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundImage extends JComponent {

    private Image image;
    private int arcWidth = 8; // 圆角弧度宽度
    private int arcHeight = 8; // 圆角弧度高度

    public RoundImage(Image image) {
        this.image = image;
        setOpaque(true);
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

