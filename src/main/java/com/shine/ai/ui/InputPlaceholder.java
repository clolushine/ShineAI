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
