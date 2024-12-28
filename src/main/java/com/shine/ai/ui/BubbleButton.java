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
