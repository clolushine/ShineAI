package com.shine.ai.ui;

import com.intellij.ui.components.IconLabelButton;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.util.ArrayList;

public class IconButton extends JPanel {
    public IconLabelButton button;

    public Icon buttonIcon;

    // 添加 ActionListener 列表和触发方法
    private final ArrayList<ActionListener> listeners = new ArrayList<>();

    public IconButton(String text,@NotNull Icon icon) {
        setOpaque(true);

        this.buttonIcon = icon;

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
//        button.setEnabled(enabled);
        if (enabled) {
            // 启用状态，使用原始图标
            button.setIcon(buttonIcon);


        } else {
            // 禁用状态，使用灰度图标
            Icon grayIcon = createGrayIcon(buttonIcon);
            button.setIcon(grayIcon);
        }
    }

    public void setIcon(Icon icon) {
        button.setIcon(icon);
        buttonIcon = icon;
    }

    private static Icon createGrayIcon(Icon icon) {
        // 创建灰度过滤器
        ImageFilter filter = new GrayFilter(true, 10); //  50 是灰度级别，可以根据需要调整
        // 使用过滤器创建新的图标
        ImageProducer producer = new FilteredImageSource(getIconImage(icon).getSource(), filter);
        return new ImageIcon(Toolkit.getDefaultToolkit().createImage(producer));
    }

    private static Image getIconImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            // 如果不是 ImageIcon，则手动绘制图标到 Image
            BufferedImage image = UIUtil.createImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
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
