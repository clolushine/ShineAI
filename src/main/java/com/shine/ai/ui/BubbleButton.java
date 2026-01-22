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
import java.awt.event.ActionListener;
import com.intellij.util.ui.JBInsets;
import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI;

public class BubbleButton extends JButton {
    public BubbleButton(String text, Icon icon) {
        super(text, icon);
        setUI(new DarculaButtonUI()); // 或者其他UI

        JBInsets insets = new JBInsets(4, 8, 4, 8); // 设置内边距
        setMargin(insets);

        // 计算并设置合适的尺寸
        int width = icon.getIconWidth() + insets.left + insets.right;
        int height = icon.getIconHeight() + insets.top + insets.bottom;
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void addActionListener(ActionListener l) {
        super.addActionListener(l);
    }
}
