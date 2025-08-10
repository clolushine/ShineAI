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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;

public class SingleLineInput extends JPanel {
    private final JTextField textField;
    private final JButton clearButton;
    private final JScrollPane scrollPane;

    public SingleLineInput() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty());

        textField = new JTextField();
        textField.setBorder(JBUI.Borders.empty(0,6,0,12));
        textField.setUI(new BasicTextFieldUI());

        scrollPane = new JBScrollPane(textField);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        clearButton = new JButton(AllIcons.Actions.DeleteTag);
        clearButton.setRolloverIcon(AllIcons.Actions.DeleteTagHover);
        clearButton.setPreferredSize(new Dimension(
                AllIcons.Actions.Close.getIconWidth() + 4,
                AllIcons.Actions.Close.getIconHeight() + 4));
        clearButton.setContentAreaFilled(false);
        clearButton.setOpaque(false);
        clearButton.setCursor(Cursor.getDefaultCursor());
        clearButton.addActionListener(e -> textField.setText(""));
        clearButton.setVisible(false);

        add(clearButton, BorderLayout.EAST);

        clearBorder();

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateUI(); }
            @Override public void removeUpdate(DocumentEvent e) { updateUI(); }
            @Override public void changedUpdate(DocumentEvent e) { updateUI(); }

            private void updateUI() {
                clearButton.setVisible(!textField.getText().isEmpty());
                scrollToEnd();
                revalidate();
                repaint();
            }
        });
    }

    public void scrollToEnd() {
        SwingUtilities.invokeLater(() -> {
            // 确保滚动条滚动到最右
            JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
            horizontal.setValue(horizontal.getMaximum());
        });
    }

    public String getContent() { return textField.getText(); }
    public void setContent(String str) { textField.setText(str); }
    public JTextField getTextField() { return textField; }
    public JButton getClearButton() { return clearButton; }

    public void clearBorder() {
        scrollPane.setBorder(null);
        clearButton.setBorder(null);
        revalidate(); // 重新验证布局
        repaint(); // 重绘组件
    }
}
