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
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.ui.OnePixelSplitter;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.ExecutorService;

public class MainPanel {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private MessageGroupComponent contentPanel;
    private PromptGroupComponent promptContentPanel;

    private OnePixelSplitter splitter;
    private final String panelName;

    private final Project myProject;
    private final Class<?> AIPanel;
    public MainPanel(@NotNull Project project,Class<?> settingPanel,String panelName) {
        this.myProject = project;
        this.AIPanel = settingPanel;
        this.panelName = panelName;
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

    public void init() {
        Integer promptsPos = stateStore.promptsPos;

        if (splitter != null) {
            splitter.dispose();
        }

        // 每次init都创建一个全新的实例
        splitter = new OnePixelSplitter(false);

        splitter.setDividerWidth(2);
        splitter.setProportion(promptsPos <= 0 ? 0.12f : 0.88f);

        promptContentPanel = new PromptGroupComponent(this);

        contentPanel = new MessageGroupComponent(myProject,AIPanel,this);

        JComponent FirstComponent = promptsPos <= 0 ? promptContentPanel : contentPanel;

        JComponent SecondComponent = promptsPos <= 0 ? contentPanel : promptContentPanel;

        splitter.setFirstComponent(FirstComponent);
        splitter.setSecondComponent(SecondComponent);

    }

    public JPanel getContent() {
        return splitter;
    }

    public void disposeContent() {
        if (contentPanel != null) {
            contentPanel.dispose();
            contentPanel = null; // 显式地将引用置空
        }
        if (promptContentPanel != null) {
            promptContentPanel.dispose();
            promptContentPanel = null; // 显式地将引用置空
        }

        if (splitter != null) {
            splitter.dispose();
            splitter = null; // 显式地将引用置空
        }
    }

    public JButton getButton() {
        return contentPanel.button;
    }

    public Class<?> getAIPanel() {
        return AIPanel;
    }

    public String getAIPanelClassName() {
        String panelClassName = ""; // 使用泛型通配符 ?
        JsonObject aiItem = stateStore.getAIVenderList().stream()
                .filter(item -> StringUtil.equals(item.get("title").getAsString(), this.panelName))
                .findFirst().orElse(null);
        if (aiItem == null) {
            panelClassName = AIPanel.getName();
        }else {
            panelClassName = aiItem.get("className").getAsString();
        }
        return panelClassName;
    }

    public String getAIName() {
        JsonObject aiItem = stateStore.getAIVenderList().stream()
                .filter(item -> StringUtil.equals(item.get("title").getAsString(), this.panelName))
                .findFirst().orElse(null);
        if (aiItem != null) {
            return aiItem.get("name").getAsString();
        }
        return "";
    }

    public String getPanelName() {
        return this.panelName;
    }

    public String getAIApi() {
        JsonObject aiItem = stateStore.getAIVenderList().stream()
                .filter(item -> StringUtil.equals(item.get("title").getAsString(), this.panelName))
                .findFirst().orElse(null);
        if (aiItem != null) {
            return aiItem.get("api").getAsString();
        }
        return "";
    }

    public String getAIKey() {
        JsonObject aiItem = stateStore.getAIVenderList().stream()
                .filter(item -> StringUtil.equals(item.get("title").getAsString(), this.panelName))
                .findFirst().orElse(null);
        if (aiItem != null) {
            return aiItem.get("key").getAsString();
        }
        return "";
    }

    public String getAIIcon() {
        JsonObject aiItem = stateStore.getAIVenderList().stream()
                .filter(item -> StringUtil.equals(item.get("title").getAsString(), this.panelName))
                .findFirst().orElse(null);
        if (aiItem != null) {
            return aiItem.get("icon").getAsString();
        }
        return "";
    }

    public void aroundRequest(boolean status) {
        contentPanel.aroundRequest(status);
    }

    public ExecutorService getExecutorService() {
        return contentPanel.getExecutorService();
    }

    public void refreshInfo() {
        if (contentPanel != null) {
            contentPanel.removeInfo();
            contentPanel.initAISetInfo();
            contentPanel.initAIInfoPanel();
            contentPanel.initAIFunctionIcon();
        }
    }

    public void refreshMessages() {
        if (contentPanel != null) {
            contentPanel.removeList();
            contentPanel.initChatList();
        }
    }
}
