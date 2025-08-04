package com.shine.ai;

import com.intellij.openapi.project.Project;
import com.shine.ai.ui.MainPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class AIToolWindow {
    private final MainPanel mainPanel;

    private String panelName = "AI";

    private final JPanel rootPanel;

    public AIToolWindow(String pName,@NotNull Project project, @NotNull Class<?> settingPanel) {
        panelName = pName;
        mainPanel = new MainPanel(project, settingPanel, panelName);

        // 立即创建占位符面板，并设置一个布局管理器
        rootPanel = new JPanel(new BorderLayout());
    }

    public JPanel getContent() {
        return rootPanel;
    }

    public void contentInit() {
        mainPanel.init();

        // 将重量级组件添加到占位符中
        rootPanel.removeAll(); // 如果有"Loading..."标签，先移除
        rootPanel.add(mainPanel.getContent(), BorderLayout.CENTER);
        rootPanel.revalidate();
        rootPanel.repaint();
    }

    public void contentDispose() {
        mainPanel.disposeContent();
    }

    public MainPanel getPanel() {
        return mainPanel;
    }

    public String getPanelName() {
        return panelName;
    }

    public void refreshInfo() {
        mainPanel.refreshInfo();
    }

    public void refreshMessages() {
        mainPanel.refreshMessages();
    }
}
