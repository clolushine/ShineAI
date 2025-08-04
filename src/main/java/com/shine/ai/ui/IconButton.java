package com.shine.ai.ui;

import com.intellij.ui.components.IconLabelButton;
import com.intellij.util.ui.ImageUtil;
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

    public JLabel label;

    public Icon buttonIcon;

    // 添加 ActionListener 列表和触发方法
    private final ArrayList<ActionListener> listeners = new ArrayList<>();

    public IconButton(String text,@NotNull Icon icon) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT));

        this.buttonIcon = icon;

        // 计算并设置合适的尺寸
        int width = icon.getIconWidth() + 8;
        int height = icon.getIconHeight() + 8;

        // 存储 button 实例
        button = new IconLabelButton(icon, component1 -> null);
        button.setDisabledIcon(adjustTransparency(icon,0.3f));
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

        label = new JLabel();
        add(label);
    }

    @Override  //  正确重写 setEnabled 方法
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); // 调用父类的 setEnabled 方法
        button.setEnabled(enabled);
    }

    public void setIcon(Icon icon) {
        buttonIcon = icon;
        button.setIcon(buttonIcon);
    }

    public void setToolTipText(String text) {
        // 约定：用 \n 分隔内容和快捷键
        String[] parts = text.split("\n", 2);
        String mainText = parts[0];
        String shortcutText = (parts.length > 1) ? parts[1] : null;

        // 1. 从主题获取颜色
        Color mainColor = UIManager.getColor("ToolTip.foreground");     // 主文本颜色
        Color shortcutColor = UIManager.getColor("Label.disabledForeground"); // 快捷键灰色

        String mainColorHex = UIUtil.colorToHex(mainColor);
        String shortcutColorHex = UIUtil.colorToHex(shortcutColor);

        // 2. 构建HTML
        String toolTipHtml;
        if (shortcutText != null) {
            // 同时包含主内容和快捷键，用&nbsp;隔开
            toolTipHtml = String.format(
                    "<html><b style='color:%s'>%s</b>&nbsp;&nbsp;<span style='color:%s'>%s</span></html>",
                    mainColorHex, mainText, shortcutColorHex, shortcutText
            );
        } else {
            // 仅主内容
            toolTipHtml = String.format("<html><b style='color:%s'>%s</b></html>", mainColorHex, mainText);
        }

        button.setToolTipText(toolTipHtml);
    }

    public void setText(String text) {
        button.setText(text);
    }

    public void setLabel(String text) {
        label.setText(text);
    }

    public void doClick() {
        // 手动创建一个 MouseEvent 对象
        MouseEvent me = new MouseEvent(button, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 1, false);
        // 直接调用 mouseListener 的 mouseClicked 方法
        button.dispatchEvent(me);
    }

    private Icon createGrayIcon(Icon icon) {
        // 创建灰度过滤器
        ImageFilter filter = new GrayFilter(true, 20); //  50 是灰度级别，可以根据需要调整
        // 使用过滤器创建新的图标
        ImageProducer producer = new FilteredImageSource(getIconImage(icon).getSource(), filter);
        return new ImageIcon(Toolkit.getDefaultToolkit().createImage(producer));
    }

    public Icon adjustTransparency(Icon icon, float alpha) {
        BufferedImage image = (BufferedImage) getIconImage(icon);
        if (image == null) return icon; //处理无法转换的情况
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return new ImageIcon(ImageUtil.scaleImage(result,buttonIcon.getIconWidth() - 2,buttonIcon.getIconHeight() - 2));
    }

    private Image getIconImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            // 如果不是 ImageIcon，则手动绘制图标到 Image
            BufferedImage image = ImageUtil.createImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
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
