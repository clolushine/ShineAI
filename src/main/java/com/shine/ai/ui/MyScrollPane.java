package com.shine.ai.ui;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import java.awt.*;


public class MyScrollPane extends JBScrollPane {

    public MyScrollPane (Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        setBorder(JBUI.Borders.empty());
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    @Override
    public void setCorner(String key, Component corner) {
        super.setCorner(key, corner);
    }
}
