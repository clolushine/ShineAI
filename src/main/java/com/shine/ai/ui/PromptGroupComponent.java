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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.NullableComponent;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.JsonUtil;
import com.shine.ai.util.StringUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PromptGroupComponent extends JBPanel<PromptGroupComponent> implements NullableComponent, Disposable {
    private final JLabel listCountsLabel;

    private final JPanel myList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane myScrollPane = new MyScrollPane(myList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    public List<JsonObject> promptList = new ArrayList<>();
    public Boolean enablePrompts;

    public MainPanel ThisMainPanel;
    public PromptGroupComponent(MainPanel mainP) {
        ThisMainPanel = mainP;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(JBUI.Borders.empty());
        setBackground(UIUtil.getListBackground());

        JPanel mainPanel = new JPanel(new BorderLayout());

        add(mainPanel,BorderLayout.CENTER);

        myList.setOpaque(true);
        myList.setBackground(UIUtil.getListBackground());
        myList.setBorder(JBUI.Borders.empty(0,10));

        listCountsLabel = new JLabel();
        Border infoTopOuterBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, new JBColor(Color.lightGray,  Color.decode("#6c6c6c"))); // 使用背景颜色
        Border infoTopInnerBorder = JBUI.Borders.empty(8,24);
        Border compoundBorder = BorderFactory.createCompoundBorder(infoTopOuterBorder,infoTopInnerBorder);
        listCountsLabel.setBorder(compoundBorder);
        listCountsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        listCountsLabel.setForeground(new JBColor(Gray.x80, Gray.x8C));
        listCountsLabel.setFont(JBUI.Fonts.create(null,13));
        mainPanel.add(listCountsLabel,BorderLayout.NORTH);

        init();

        myScrollPane.setBorder(JBUI.Borders.empty());
        mainPanel.add(myScrollPane,BorderLayout.CENTER);

        myList.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                refreshListCounts();
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                refreshListCounts();
            }
        });
    }

    public void init() {
        initPromptList();
    }

    public void refreshStatus() {
        refreshListCounts();
    }

    public void addPrompt(JsonObject prompt) {
        JsonObject cPrompt = prompt.deepCopy();

        String content = cPrompt.get("content").getAsString();
        String role = cPrompt.get("role").getAsString();

        String promptId = "";

        // 先查看是否是chat
        if (cPrompt.has("id") && !cPrompt.get("id").isJsonNull()) {
            promptId = cPrompt.get("id").getAsString();
            cPrompt.remove("id");
        }else {
            promptId = cPrompt.get("promptId").getAsString();
        }

        // 设置id
        cPrompt.addProperty("promptId",promptId);

        cPrompt.addProperty("icon", StringUtil.equals(role, "user") ?  AIAssistantIcons.ME_PATH : AIAssistantIcons.AI_PATH);
        cPrompt.addProperty("name", role);
        cPrompt.addProperty("isMe", StringUtil.equals(role, "user"));
        cPrompt.addProperty("status", 1);
        cPrompt.addProperty("time", 0);
        cPrompt.addProperty("isPin", false);
        cPrompt.addProperty("withContent", content.isBlank() ? "invalid prompt" : "preset prompt"); // 把进行时状态改成1
        cPrompt.addProperty("enable",true);

        promptList.add(cPrompt);
        // 这里需要写入state
        stateStore.setAISettingInfoByKey(ThisMainPanel.getPanelName(),"prompts", JsonUtil.getJsonArray(promptList));
        PromptComponent messageComponentItem = new PromptComponent(cPrompt);
        myList.add(messageComponentItem);

        updateLayout();
    }

    public void refreshListCounts() {
        JsonObject settingInfo = stateStore.getAISettingInfo(ThisMainPanel.getPanelName());

        enablePrompts = settingInfo.get("promptsCutIn").getAsBoolean();

        listCountsLabel.setText("total：" + myList.getComponentCount() + " prompts");

        setVisible(enablePrompts);

        myList.setVisible(enablePrompts);
    }

    public void updatePrompt(JsonObject promptItem) {
        String updateId = promptItem.get("promptId").getAsString();

        int updateIdx = IntStream.range(0, promptList.size())
                .filter(i -> StringUtil.equals(promptList.get(i).get("promptId").getAsString(), updateId))
                .findFirst()
                .orElse(-1);

        if (updateIdx >= 0) {
            promptList.set(updateIdx,promptItem);
        }

        // 这里需要写入state
        stateStore.setAISettingInfoByKey(ThisMainPanel.getPanelName(),"prompts", JsonUtil.getJsonArray(promptList));

        removeList();

        initPromptList();

        updateLayout();
    }

    public void delete(String chatId) {
        promptList.removeIf(it -> StringUtil.equals(it.get("promptId").getAsString(),chatId));
        // 这里需要写入state
        stateStore.setAISettingInfoByKey(ThisMainPanel.getPanelName(),"prompts", JsonUtil.getJsonArray(promptList));
        for (Component comp : myList.getComponents()) {
            if (comp instanceof PromptComponent messageItem) {
                if (StringUtil.equals(messageItem.chatId, chatId)) {
                    myList.remove(messageItem); //使用remove(Component)方法
                }
            }
        }
        updateLayout();
    }

    public void removeList() {
        myList.removeAll();
    }

    public void openPromptList() {
        for (JsonObject chatItem : promptList) {
            if (!(chatItem.has("enable") && chatItem.get("enable").getAsBoolean())) {
                continue;
            }

            String content = chatItem.get("content").getAsString();
            String role = chatItem.get("role").getAsString();

            chatItem.addProperty("icon", StringUtil.equals(role, "user") ?  AIAssistantIcons.ME_PATH : AIAssistantIcons.AI_PATH);
            chatItem.addProperty("name", role);
            chatItem.addProperty("isMe", StringUtil.equals(role, "user"));
            chatItem.addProperty("status", 1);
            chatItem.addProperty("time", 0);
            chatItem.addProperty("isPin", false);
            chatItem.addProperty("withContent", content.isBlank() ? "invalid prompt" : "preset prompt"); // 把进行时状态改成1
            PromptComponent promptComponentItem = new PromptComponent(chatItem);

            myList.add(promptComponentItem);
        }

        refreshListCounts();

        updateLayout();
    }


    public void initPromptList() {
        JsonObject settingInfo = stateStore.getAISettingInfo(ThisMainPanel.getPanelName());

        if (!settingInfo.isJsonNull()) {
            JsonArray prompts = settingInfo.get("prompts").getAsJsonArray();
            promptList = prompts.asList().stream()
                    .filter(JsonElement::isJsonObject)   // 过滤出所有是 JsonObject 的元素
                    .map(JsonElement::getAsJsonObject)   // 将这些元素强制转换为 JsonObject
                    .collect(Collectors.toList());
        }

        openPromptList();
    }

    public void updateLayout() {
        myList.revalidate();
        myList.repaint();
    }

    @Override
    public boolean isNull() {
        return !isVisible();
    }

    public List<JsonObject> getPromptList() {
        return promptList;
    }

    public JsonArray getEnabledPromptList() {
        JsonArray prompts = new JsonArray();
        for (JsonObject prompt: promptList) {
            if (prompt.has("enable") && prompt.get("enable").getAsBoolean()) {
                prompts.add(prompt);
            }
        }
        return prompts;
    }

    @Override
    public void dispose() {
        removeList();

        ThisMainPanel = null;
    }
}
