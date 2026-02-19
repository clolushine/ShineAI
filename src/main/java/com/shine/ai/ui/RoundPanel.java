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

import com.intellij.openapi.Disposable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundPanel extends JComponent implements Disposable {
    private int arcWidth = 8;
    private int arcHeight = 8;

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
