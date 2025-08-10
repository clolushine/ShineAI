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
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.util.ui.JBUI;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.JsonUtil;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

public class AIApikeyComponent extends JPanel {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    public JPanel contentPanel;

    public AIApikeyComponent(JsonObject apikeyInfo, List<JsonObject> apiKeys, JComponent listContainer, String panelName, boolean hasApiId) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(6));

        JPanel weightPanel = new JPanel(new BorderLayout());

        JLabel weightLabel = new JLabel("weight: ");
        weightPanel.add(weightLabel,BorderLayout.WEST);

        SpinnerNumberModel weightModel = new SpinnerNumberModel(1, 1, 100, 2);
        JSpinner weightSpinner = new JSpinner(weightModel);
        weightSpinner.setValue(apikeyInfo.get("weight").getAsInt());
        weightSpinner.setPreferredSize(new Dimension(64, weightSpinner.getPreferredSize().height));
        weightSpinner.addChangeListener(e -> {
            int weightValue = (int) weightSpinner.getValue();
            for (int i = 0; i < apiKeys.size(); i++) {
                JsonObject currentApikey = apiKeys.get(i);
                if (Objects.equals(currentApikey.get("id").getAsString(), apikeyInfo.get("id").getAsString())) {
                    currentApikey.addProperty("weight", weightValue);
                    apiKeys.set(i, currentApikey); // 使用 set() 方法修改列表元素
                    // 这里需要写入state
                    stateStore.setAISettingInfoByKey(panelName,"apiKeys", JsonUtil.getJsonArray(apiKeys));
                    break; // 可选：如果 promptId 是唯一的，找到后可以跳出循环
                }
            }
        });
        weightPanel.add(weightSpinner,BorderLayout.CENTER);
        // 添加权重选择器
        add(weightPanel,BorderLayout.WEST);

        contentPanel = new JPanel(new GridLayout(1,2));
        contentPanel.setBorder(JBUI.Borders.empty(0,16,0,32));

        if (hasApiId) {
            JPanel apiIdPanel = new JPanel(new BorderLayout());

            JLabel apiIdLabel = new JLabel("id: ");
            apiIdPanel.add(apiIdLabel,BorderLayout.WEST);

            JTextField apiIdFiled = new JTextField();
            apiIdFiled.setText(apikeyInfo.get("apiId").getAsString());
            apiIdFiled.setToolTipText("Please enter the AI apiId account.");
            // 添加监听器
            apiIdFiled.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateContent();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateContent();
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateContent();
                }
                private void updateContent() {
                    String apiIdContent = apiIdFiled.getText();
                    for (int i = 0; i < apiKeys.size(); i++) {
                        JsonObject currentApikey = apiKeys.get(i);
                        if (Objects.equals(currentApikey.get("id").getAsString(), apikeyInfo.get("id").getAsString())) {
                            currentApikey.addProperty("apiId", apiIdContent);
                            apiKeys.set(i, currentApikey); // 使用 set() 方法修改列表元素
                            // 这里需要写入state
                            stateStore.setAISettingInfoByKey(panelName,"apiKeys", JsonUtil.getJsonArray(apiKeys));
                            break; // 可选：如果 promptId 是唯一的，找到后可以跳出循环
                        }
                    }
                }
            });
            apiIdPanel.add(apiIdFiled,BorderLayout.CENTER);

            contentPanel.add(apiIdPanel);
        }


        JPanel apikeyPanel = new JPanel(new BorderLayout());

        JLabel apikeyLabel = new JLabel("key: ");
        apikeyPanel.add(apikeyLabel,BorderLayout.WEST);

        JPasswordField apikeyFiled = new JPasswordField();
        apikeyFiled.setText(apikeyInfo.get("apiKey").getAsString());
        apikeyFiled.setToolTipText("Please enter the AI apiKey.");
        char defaultEchoChar = apikeyFiled.getEchoChar();
        // 添加监听器
        apikeyFiled.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateContent();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateContent();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateContent();
            }
            private void updateContent() {
                String apikeyContent = apikeyFiled.getText();
                for (int i = 0; i < apiKeys.size(); i++) {
                    JsonObject currentApikey = apiKeys.get(i);
                    if (Objects.equals(currentApikey.get("id").getAsString(), apikeyInfo.get("id").getAsString())) {
                        currentApikey.addProperty("apiKey", apikeyContent);
                        apiKeys.set(i, currentApikey); // 使用 set() 方法修改列表元素
                        // 这里需要写入state
                        stateStore.setAISettingInfoByKey(panelName,"apiKeys", JsonUtil.getJsonArray(apiKeys));
                        break; // 可选：如果 promptId 是唯一的，找到后可以跳出循环
                    }
                }
            }
        });
        apikeyPanel.add(apikeyFiled,BorderLayout.CENTER);


        JLabel eyeButton = getEyeButton(apikeyFiled, defaultEchoChar);
        eyeButton.setBorder(JBUI.Borders.emptyLeft(6));
        apikeyPanel.add(eyeButton,BorderLayout.EAST);

        contentPanel.add(apikeyPanel);

        add(contentPanel,BorderLayout.CENTER);

        BubbleButton deleteP = getDeletePAction(apikeyInfo,apiKeys,listContainer,panelName);
        add(deleteP,BorderLayout.EAST);
    }

    private JLabel getEyeButton(JPasswordField apikeyFiled, char defaultEchoChar) {
        JLabel eyeButton = new JLabel(AllIcons.Actions.ToggleVisibility);
        // 添加鼠标监听器到图标
        eyeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 按下时显示密码
                apikeyFiled.setEchoChar((char) 0);
                eyeButton.setIcon(AllIcons.Actions.Show);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 松开时隐藏密码
                apikeyFiled.setEchoChar(defaultEchoChar);
                eyeButton.setIcon(AllIcons.Actions.ToggleVisibility);
            }
        });
        return eyeButton;
    }

    private @NotNull BubbleButton getDeletePAction(JsonObject apikeyInfo, List<JsonObject> apiKeys, JComponent listContainer, String panelName) {
        BubbleButton deletePAction = new BubbleButton("", AllIcons.Actions.DeleteTagHover);
        deletePAction.addActionListener(e -> {
            boolean yes = MessageDialogBuilder.yesNo("Are you sure you want to delete this apikey?",
                            "There will be delete this apikey.")
                    .yesText("Yes")
                    .noText("No").ask(listContainer);
            if (yes) {
                listContainer.remove(this);

                apiKeys.removeIf(it -> StringUtil.equals(it.get("id").getAsString(), apikeyInfo.get("id").getAsString()));
                // 这里需要写入state
                stateStore.setAISettingInfoByKey(panelName,"apiKeys", JsonUtil.getJsonArray(apiKeys));
            }
        });
        return deletePAction;
    }
}
