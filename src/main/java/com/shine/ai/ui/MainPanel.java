package com.shine.ai.ui;
import com.google.gson.JsonObject;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.OnePixelSplitter;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.settings.ChatSettingDialog;
import com.shine.ai.settings.FindMatchDialog;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;

import static com.shine.ai.MyToolWindowFactory.MAPPINGS;


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

        initShortcutKeyTool();
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

    private void initShortcutKeyTool(){
        SwingUtilities.invokeLater(() -> {
            JRootPane rootPane = splitter.getRootPane();
            if (rootPane == null) return;

            this.lastRootPane = rootPane; // 保存 rootPane 以便稍后清理

            // 定义 KeyStroke：Ctrl + F
            KeyStroke ctrlFKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
            // 定义一个用于 KeyBinding 的唯一名称
            String FIND_ACTION_KEY = "find-text";

            // 定义 KeyStroke：Ctrl + D
            KeyStroke ctrlDKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
            // 定义一个用于 KeyBinding 的唯一名称
            String CHAT_SETTING_KEY = "chat-setting";

            // 定义 KeyStroke： Ctrl + Alt + A
            KeyStroke ctrlAltAKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
            // 定义一个用于 KeyBinding 的唯一名称
            String AI_SETTING_KEY = "ai-setting";

            // 将 KeyStroke 和 Action 绑定到组件的 InputMap 和 ActionMap
            // WHEN_FOCUSED_IN_WINDOW 表示：只要组件所在的窗口有焦点，并且它本身是可接受键盘输入的，这个绑定就可能触发
            InputMap inputMap = splitter.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = splitter.getRootPane().getActionMap();

            inputMap.put(ctrlFKeyStroke, FIND_ACTION_KEY);
            actionMap.put(FIND_ACTION_KEY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    FindMatchDialog dialog = contentPanel.findMatchDialog;
                    if (!getButton().isEnabled()) {
                        return;
                    }
                    // 1. 获取当前拥有焦点的组件
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                    String selectedText = null;

                    // 2. 判断它是否是文本组件并获取选中的文本
                    if (focusOwner instanceof JTextComponent textComponent) {
                        selectedText = textComponent.getSelectedText();
                    }

                    if (!dialog.isVisible()) {
                        dialog.openDialog(MainPanel.this,contentPanel.getRenderedChatList(), selectedText);
                    }
                }
            });

            inputMap.put(ctrlDKeyStroke, CHAT_SETTING_KEY);
            actionMap.put(CHAT_SETTING_KEY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!getButton().isEnabled()) {
                        return;
                    }
                    new ChatSettingDialog(myProject).openDialog((JComponent) contentPanel.getParent(),AIPanel);
                }
            });

            inputMap.put(ctrlAltAKeyStroke, AI_SETTING_KEY);
            actionMap.put(AI_SETTING_KEY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(myProject, MAPPINGS.get(AIPanel.getName()));
                }
            });
        });
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
