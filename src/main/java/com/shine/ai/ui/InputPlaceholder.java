package com.shine.ai.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class InputPlaceholder extends JLabel implements FocusListener, DocumentListener {
    private JTextArea textArea;
    private String placeholder;

    public InputPlaceholder(String placeholder, JTextArea textArea) {
        this.textArea = textArea;
        this.placeholder = placeholder;
        setText(placeholder);
        setForeground(JBColor.namedColor("Label.infoForeground", JBColor.GRAY));

        textArea.addFocusListener(this);
        textArea.getDocument().addDocumentListener(this);

        //  将 TextPrompt 添加到 JTextArea 的 viewport 中
        textArea.setLayout(new BorderLayout());
        textArea.add(this, BorderLayout.WEST); //  调整位置根据需要

        //  根据 JTextArea 的初始状态显示或隐藏 placeholder
        updateVisibility();
    }


    private void updateVisibility() {
        setVisible(textArea.getText().isEmpty());
    }


    @Override
    public void focusLost(FocusEvent e) {
        updateVisibility();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateVisibility();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateVisibility();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateVisibility();
    }

    @Override
    public void focusGained(FocusEvent e) {
        updateVisibility();
    }
}
