package com.shine.ai.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadingButton extends JButton {
    private final Icon originalIcon;
    private final String originalText;
    private Timer loadingTimer;

    private final Icon[] stepIcons = {
            AllIcons.Process.Step_1,
            AllIcons.Process.Step_2,
            AllIcons.Process.Step_3,
            AllIcons.Process.Step_4,
            AllIcons.Process.Step_5,
            AllIcons.Process.Step_6,
            AllIcons.Process.Step_7,
            AllIcons.Process.Step_8,
    };

    public LoadingButton(String text, Icon icon) {
        super(text, icon);
        originalIcon = icon;
        originalText = text;
        setUI(new DarculaButtonUI()); // 或者其他UI
    }

    public LoadingButton(String text) {
        super(text);
        originalText = text;
        originalIcon = getIcon();
        setUI(new DarculaButtonUI()); // 或者其他UI
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            setEnabled(false);
            requestFocusInWindow(false);
            loadingTimer = new Timer(50, new ActionListener() {
                private int currentStep = 0; // 声明为字段
                @Override
                public void actionPerformed(ActionEvent e) {
                    setIcon(stepIcons[currentStep % stepIcons.length]);
                    revalidate();
                    repaint();     //  强制重绘组件
                    currentStep++;
                }
            });
            loadingTimer.start();
        } else {
            if (loadingTimer != null) {
                loadingTimer.stop();
                loadingTimer = null;
            }
            setEnabled(true);
            requestFocusInWindow(); // 重新获得焦点
            setIcon(originalIcon); // 恢复原始图标（可能为 null）
            setText(originalText);
        }
    }

    @Override
    public void addActionListener(ActionListener l) {
        super.addActionListener(l);
    }
}
