package com.shine.ai.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.shine.ai.core.SendAction;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTextAreaUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class MultilineInput extends JPanel {
    private final JBTextArea textArea;
    private final JButton clearButton;
    private final int MAX_HEIGHT = 96;
    private final int INITIAL_HEIGHT = 32;
    private final JScrollPane scrollPane;
    private final MainPanel mainPanel;
    public MultilineInput(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty());

        textArea = new JBTextArea();
        // 放在setting中设置
//        textArea.setLineWrap(true); // 自动换行
//        textArea.setWrapStyleWord(true); // 单词边界换行
        textArea.setFont(JBUI.Fonts.create(Font.SANS_SERIF,12));
        textArea.setBorder(JBUI.Borders.empty(4,4,4,12));

        // 限制最大高度，超过则显示滚动条
        scrollPane = new JBScrollPane(textArea);
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

        textArea.setUI(new BasicTextAreaUI() {
            @Override
            public void paintBackground(Graphics g) {
                super.paintBackground(g);
            }
        });

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
                SendAction sendAction = mainPanel.getProject().getService(SendAction.class);
                sendAction.doActionPerformed(mainPanel,content);
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

    public JBTextArea getTextarea() {
        return this.textArea;
    }

    private void adjustHeight() {
        int preferredHeight = textArea.getPreferredSize().height;
        int newHeight = Math.min(preferredHeight, MAX_HEIGHT);
        // 避免不必要的重绘
        if (scrollPane.getPreferredSize().height != newHeight) {
            scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, newHeight));
            setMinimumSize(new Dimension(getPreferredSize().width,newHeight));
            revalidate(); // 重新验证布局
            repaint(); // 重绘组件
        }
    }
}
