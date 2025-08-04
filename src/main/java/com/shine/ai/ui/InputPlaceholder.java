package com.shine.ai.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class InputPlaceholder extends JLabel implements FocusListener, DocumentListener {
    private final JTextComponent textComponent;
    private final String placeholder;

    public InputPlaceholder(String placeholder, JTextComponent component) {
        this.textComponent = component;
        this.placeholder = placeholder;

        init();
        attachListeners();
        updateVisibility();
    }

    private void init() {
        setText(placeholder);
        setForeground(JBColor.GRAY);

        if (textComponent instanceof JTextArea) {
            textComponent.setLayout(new BorderLayout());
            textComponent.add(this, BorderLayout.NORTH);
        } else {
            textComponent.setLayout(new BorderLayout());
            textComponent.add(this, BorderLayout.WEST);
        }
    }

    private void attachListeners() {
        textComponent.addFocusListener(this);
        textComponent.getDocument().addDocumentListener(this);
    }

    private void updateVisibility() {
        setVisible(textComponent.getText().isEmpty());
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
