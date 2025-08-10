package com.shine.ai.ui;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.ui.OnePixelSplitter;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;

public class MainPanel {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private MessageGroupComponent contentPanel;
    private PromptGroupComponent promptContentPanel;

    private OnePixelSplitter splitter;
    private final String panelName;
    private JRootPane lastRootPane; // 添加一个成员变量来保存 rootPane

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
        // 清理快捷键绑定
        if (lastRootPane != null) {
            InputMap inputMap = lastRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = lastRootPane.getActionMap();

            // 通过 put null 来移除绑定
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), null);
            actionMap.put("find-text", null);

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), null);
            actionMap.put("chat-setting", null);

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK), null);
            actionMap.put("ai-setting", null);

            lastRootPane = null; // 清理引用
        }

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
