package com.shine.ai.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.OnePixelSplitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.ExecutorService;


public class MainPanel {
    public final MessageGroupComponent contentPanel;
    public final PromptGroupComponent promptContentPanel;

    public final OnePixelSplitter splitter;
    private final Project myProject;
    private final Class<?> AIPanel;
    public MainPanel(@NotNull Project project,Class<?> settingPanel) {
        this.myProject = project;
        this.AIPanel = settingPanel;

        splitter = new OnePixelSplitter(false,0.22f);
        splitter.setDividerWidth(2);

        promptContentPanel = new PromptGroupComponent(project,settingPanel,this);

        contentPanel = new MessageGroupComponent(project,settingPanel,this);

        splitter.setFirstComponent(promptContentPanel);
        splitter.setSecondComponent(contentPanel);
    }

    public Project getProject() {
        return myProject;
    }

    public MultilineInput getInputTextArea() {
        return contentPanel.inputTextArea;
    }

    public MessageGroupComponent getContentPanel() {
        return contentPanel;
    }

    public PromptGroupComponent getPromptsPanel() {
        return promptContentPanel;
    }

    public JPanel init() {
        return splitter;
    }

    public JButton getButton() {
        return contentPanel.button;
    }

    public Class<?> getAIPanel() {
        return AIPanel;
    }

    public void aroundRequest(boolean status) {
        contentPanel.aroundRequest(status);
    }

    public ExecutorService getExecutorService() {
        return contentPanel.getExecutorService();
    }

    public void refreshInfo() {
        MessageGroupComponent messageGroupPanel = contentPanel;
        messageGroupPanel.removeInfo();
        messageGroupPanel.initAISetInfo();
        messageGroupPanel.initAIInfoPanel();
    }

    public void refreshMessages() {
        MessageGroupComponent messageGroupPanel = contentPanel;
        messageGroupPanel.removeList();
        messageGroupPanel.initChatList();
        messageGroupPanel.refreshListCounts();
    }
}
