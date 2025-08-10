/*
 * ShineAI - An IntelliJ IDEA plugin for AI services.
 * Copyright (C) 2025 Shine Zhong
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

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.shine.ai.core.SendAction;
import com.shine.ai.message.MsgEntryBundle;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MultilineInput extends JPanel {
    private final JBTextArea textArea;
    private final JButton clearButton;
    private final int MAX_HEIGHT = 256;
    private final int INITIAL_HEIGHT = 32;
    private final JScrollPane scrollPane;
    private final MainPanel mainPanel;
    public MultilineInput(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty());

        textArea = new JBTextArea();
        // 放在setting中设置
//        textArea.setLineWrap(true); // 自动换行
//        textArea.setWrapStyleWord(true); // 单词边界换行
        textArea.setBorder(JBUI.Borders.empty(4,6,4,12));

        textArea.setTransferHandler(new ImageAwareTransferHandler());

        // 限制最大高度，超过则显示滚动条
        scrollPane = new JBScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(getWidth(), INITIAL_HEIGHT));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // 创建带图标的清除按钮 (同上一个例子，记得替换图标路径)
        clearButton = new JButton(AllIcons.Actions.DeleteTag);
        clearButton.setRolloverIcon(AllIcons.Actions.DeleteTagHover);
        clearButton.setBorder(null);
        clearButton.setPreferredSize(new Dimension(AllIcons.Actions.Close.getIconWidth() + 8, AllIcons.Actions.Close.getIconHeight() + 8));
        clearButton.setContentAreaFilled(false);
        clearButton.setOpaque(false);
        clearButton.setCursor(Cursor.getDefaultCursor());
        clearButton.addActionListener(e -> {
            textArea.setText("");
            adjustHeight(); // 关键：清除文本后重新调整高度
        });

        clearButton.setVisible(false);

        add(clearButton, BorderLayout.EAST);

        textArea.getDocument().addUndoableEditListener(e -> {
            adjustHeight();
        });

        // 监听文本变化，调整高度
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateClearButtonVisibility();
                adjustHeight();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateClearButtonVisibility();
                adjustHeight();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateClearButtonVisibility();
                adjustHeight();
            }
        });

        // 支持 Shift+Enter 换行
        InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textArea.getActionMap();

        KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(shiftEnter, "insert-break");  // 使用 "insert-break" action
        inputMap.put(ctrlEnter, "send-action");  // 使用 "send-action" action

        actionMap.put("insert-break", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.insert("\n", textArea.getCaretPosition());
            }
        });

        actionMap.put("send-action", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = textArea.getText();
                if (content == null || content.isBlank()) {
                    return;
                }
                if (!mainPanel.getButton().isEnabled()) {
                    return;
                }
                SendAction sendAction = mainPanel.getProject().getService(SendAction.class);
                sendAction.doActionPerformed(mainPanel,content,null);
            }
        });
    }

    private void updateClearButtonVisibility() {
        clearButton.setVisible(!textArea.getText().isEmpty());
        revalidate(); // 重新验证布局
        repaint(); // 重绘面板
    }

    public String getContent() {
      return textArea.getText();
    }

    public void setContent(String str) {
        this.textArea.setText(str);
    }

    public void clearBorder() {
        scrollPane.setBorder(null);
        clearButton.setBorder(null);
        revalidate(); // 重新验证布局
        repaint(); // 重绘组件
    }

    public JBTextArea getTextarea() {
        return this.textArea;
    }

    private void adjustHeight() {
        int preferredHeight = textArea.getPreferredSize().height;
        // 这里可以加上初始高度的判断，确保不会比初始值小
        int newHeight = Math.max(INITIAL_HEIGHT, Math.min(preferredHeight, MAX_HEIGHT));
        // 只有在高度确实需要变化时才执行后续操作
        if (scrollPane.getPreferredSize().height != newHeight) {
            scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, newHeight));
            // revalidate()会触发布局更新，这个更新是异步的。
            // 我们需要在布局更新完成后，再执行滚动操作。
            revalidate();
            // 使用 SwingUtilities.invokeLater 来确保滚动操作在布局管理器完成工作后执行。
            SwingUtilities.invokeLater(() -> {
                // 请求将当前组件（MultilineInput 面板）的整个区域滚动到可见范围
                this.scrollRectToVisible(this.getBounds());
            });
        }
    }

    private static class ImageAwareTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.imageFlavor);
        }
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            return false;
        }
    }

    public List<BufferedImage> pasteFromClipboardImage() {
        List<BufferedImage> imageList = new ArrayList<>();
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
               BufferedImage image = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
                imageList.add(image);
            } catch (Exception ex) {
                Notifications.Bus.notify(
                        new Notification(MsgEntryBundle.message("group.id"),
                                "Paste error",
                                "Cannot load this image.",
                                NotificationType.ERROR));
            }
        }
        return imageList;
    }
}
