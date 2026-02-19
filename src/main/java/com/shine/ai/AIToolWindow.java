/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.shine.ai;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.shine.ai.settings.ChatSettingDialog;
import com.shine.ai.settings.FindMatchDialog;
import com.shine.ai.ui.MainPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static com.shine.ai.vendors.AIVendors.*;

public class AIToolWindow {
    private final MainPanel mainPanel;

    private final Project project;

    private String panelName = "AI";

    private final JPanel rootPanel;

    public AIToolWindow(String pName,@NotNull Project project, @NotNull Class<?> settingPanel) {
        panelName = pName;
        mainPanel = new MainPanel(project, settingPanel, panelName);
        this.project = project;

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

        initShortcutKeyTool();
    }

    public void contentDispose() {
        mainPanel.disposeContent();

        reinitializeShortcuts();
    }

    public MainPanel getPanel() {
        return mainPanel;
    }

    public String getPanelName() {
        return panelName;
    }

    private void initShortcutKeyTool(){
        SwingUtilities.invokeLater(() -> {

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
            InputMap inputMap = rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = rootPanel.getActionMap();

            inputMap.put(ctrlFKeyStroke, FIND_ACTION_KEY);
            actionMap.put(FIND_ACTION_KEY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    FindMatchDialog dialog = mainPanel.getContentPanel().findMatchDialog;
                    if (!mainPanel.getButton().isEnabled()) {
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
                        dialog.openDialog(mainPanel,mainPanel.getContentPanel().getRenderedChatList(), selectedText);
                    }
                }
            });

            inputMap.put(ctrlDKeyStroke, CHAT_SETTING_KEY);
            actionMap.put(CHAT_SETTING_KEY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!mainPanel.getButton().isEnabled()) {
                        return;
                    }
                    new ChatSettingDialog(project).openDialog((JComponent) mainPanel.getContentPanel().getParent(),mainPanel.getAIPanel());
                }
            });

            inputMap.put(ctrlAltAKeyStroke, AI_SETTING_KEY);
            actionMap.put(AI_SETTING_KEY, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, MAPPINGS.get(mainPanel.getAIPanel().getName()));
                }
            });
        });
    }

    private void reinitializeShortcuts() {
        // 先清理旧的绑定，确保不会重复绑定或绑定到错误的RootPane
        // 如果是重新绑定，应该先清理
        InputMap inputMap = rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPanel.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), null);
        actionMap.put("find-text", null);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), null);
        actionMap.put("chat-setting", null);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK), null);
        actionMap.put("ai-setting", null);
    }
}
