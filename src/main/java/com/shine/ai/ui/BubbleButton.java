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
