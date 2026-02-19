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

package com.shine.ai.ui;

import com.google.gson.JsonObject;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromptComponent extends JBPanel<PromptComponent> {
    private static final Logger LOG = LoggerFactory.getLogger(PromptComponent.class);

    public JLabel withContentLabel;

    public JsonObject chatItemData;

    public String chatId;

    private MessageTextareaComponent textArea;

    public Boolean showActions = true;

    public PromptComponent(JsonObject chatItem) {

        setDoubleBuffered(true);
        setOpaque(true);
        setBorder(JBUI.Borders.empty(6));
        setLayout(new BorderLayout(JBUI.scale(8), 0));

        initComponent(chatItem);
    }

    public void initComponent(JsonObject chatItem) {
        chatItemData = chatItem;

        if (chatItemData.has("promptId")) {
            chatId = chatItemData.get("promptId").getAsString();
            showActions = false;
        }else {
            chatId = chatItemData.get("id").getAsString();
        }

        chatItemData.addProperty("id",chatId);

        String content = chatItemData.get("content").getAsString();
        boolean isMe = chatItemData.get("isMe").getAsBoolean();
        String name = chatItemData.get("name").getAsString();
        String withContent = chatItemData.get("withContent").getAsString();
        boolean isVisible = chatItem.has("enable") && chatItem.get("enable").getAsBoolean();

        setVisible(isVisible); // 设置是否可见

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.setBorder(JBUI.Borders.empty());

        JPanel topPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.LEFT : FlowLayout.RIGHT));

        JLabel nameLabel = new JLabel();
        nameLabel.setBorder(JBUI.Borders.empty(0,6));
        nameLabel.setFont(JBUI.Fonts.create(null,14));
        nameLabel.setText(name);
        nameLabel.setVerticalAlignment(JLabel.BOTTOM);
        topPanel.add(nameLabel);

        List<Component> componentsToAdd = new ArrayList<>();
        for (int i = 0; i < topPanel.getComponentCount(); i++) {
            componentsToAdd.add(topPanel.getComponent(i));
        }

        if (isMe) Collections.reverse(componentsToAdd); // 只在 isMe 为 true 时反转

        topPanel.removeAll();

        for (Component component : componentsToAdd) {
            topPanel.add(component);
        }

        northPanel.add(topPanel,isMe ? BorderLayout.EAST : BorderLayout.WEST);
        add(northPanel,BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        RoundPanel messagePanel = new RoundPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(true);
        messagePanel.setMinimumSize(new Dimension(getMinimumSize().width,32));
        messagePanel.setBorder(JBUI.Borders.empty(2));

        JComponent messageTextarea = MessageTextareaComponent(content,isMe);
        messagePanel.add(messageTextarea,BorderLayout.CENTER);

        centerPanel.add(messagePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(JBUI.Borders.empty(2));

        JPanel southContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        withContentLabel = new JLabel();
        withContentLabel.setForeground(new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26")));
        withContentLabel.setFont(JBUI.Fonts.create(null,10));
        withContentLabel.setText(withContent);
        southContentPanel.add(withContentLabel);

        southPanel.add(southContentPanel,isMe ? BorderLayout.EAST : BorderLayout.WEST);

        add(southPanel,BorderLayout.SOUTH); // 将 MainPanel 添加到中心
    }

    public RoundPanel MessageTextareaComponent (String content, Boolean isMe) {
        RoundPanel messagePanel = new RoundPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel,BoxLayout.Y_AXIS));

        textArea = new MessageTextareaComponent(content,isMe);

        messagePanel.add(textArea);
        return messagePanel;
    }
}
