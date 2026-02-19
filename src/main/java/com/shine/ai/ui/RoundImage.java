/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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

