package com.shine.ai.ui;

import com.intellij.ui.components.IconLabelButton;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class IconButton extends JPanel {
    public IconLabelButton button;

    // 添加 ActionListener 列表和触发方法
    private final ArrayList<ActionListener> listeners = new ArrayList<>();

    public IconButton(String text,@NotNull Icon icon) {
        setOpaque(true);

        // 计算并设置合适的尺寸
        int width = icon.getIconWidth() + 8;
        int height = icon.getIconHeight() + 8;

        // 存储 button 实例
        button = new IconLabelButton(icon, component1 -> null);
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(JBUI.Borders.empty());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(text);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEnabled()) {  // 只在启用状态下触发事件
                    fireActionPerformed();
                }
            }
        });

        add(button);
    }

    @Override  //  正确重写 setEnabled 方法
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); // 调用父类的 setEnabled 方法
        button.setEnabled(enabled);
    }

    public void setIcon(Icon icon) {
        button.setIcon(icon);
    }

    public void addActionListener(ActionListener listener) {
        this.listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        this.listeners.remove(listener);
    }

    private void fireActionPerformed() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }
}
