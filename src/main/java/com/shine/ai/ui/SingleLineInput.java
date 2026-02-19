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
