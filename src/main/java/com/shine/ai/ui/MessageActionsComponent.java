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

import cn.hutool.core.swing.clipboard.ClipboardUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.ui.JBUI;
import com.shine.ai.core.SendAction;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.shine.ai.MyToolWindowFactory.ACTIVE_CONTENT;

public class MessageActionsComponent extends JPanel {

    private final Project _project;

    private IconButton editAction;
    private IconButton refreshAction;
    private IconButton trashAction;
    private IconButton pinAction;
    private IconButton copyAction;

    private List<IconButton> actionButtons = new ArrayList<>();

    private JsonObject _chatItem;

    private MessageActionCallback messageActionCallback; // 持有接口引用

    public interface MessageActionCallback {
        void ontShowBalloon(String msg, MessageType type);
    }

    // 父组件通过此方法设置回调
    public void setActionCallback(MessageActionCallback callback) {
        this.messageActionCallback = callback;
    }

    public MessageActionsComponent(Project project, JsonObject chatItem) {
        _project = project;
        _chatItem = chatItem;

        boolean isMe = StringUtil.equals(_chatItem.get("role").getAsString(),"user");
        String content = _chatItem.get("content").getAsString();

        String chatId = _chatItem.get("id").getAsString();

        JsonArray attachments;
        if (_chatItem.has("attachments") && !_chatItem.get("attachments").isJsonNull()) {
            attachments = _chatItem.get("attachments").getAsJsonArray();
        }else {
            attachments = new JsonArray();
        }

        setDoubleBuffered(true);
        setOpaque(true);
        setBorder(JBUI.Borders.empty());

        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT,0,0);
        setLayout(flowLayout); // 从左到右排列

        editAction = getEditAction(_chatItem,attachments);
        add(editAction);
        actionButtons.add(editAction);

        if (Boolean.TRUE.equals(isMe)) {
            refreshAction = getRefreshAction(content,attachments);
            add(refreshAction);
            actionButtons.add(refreshAction);
        }

        trashAction = getTrashAction(chatId);
        add(trashAction);
        actionButtons.add(trashAction);

        pinAction = getPinAction( _chatItem);
        add(pinAction);
        actionButtons.add(pinAction);

        copyAction = getCopyAction(_chatItem);
        add(copyAction);
        actionButtons.add(copyAction);
    }

    private @NotNull IconButton getRefreshAction(String contentStr,JsonArray attachments) {
        IconButton refreshAction = new IconButton("rerun",AllIcons.Actions.Refresh);
        refreshAction.addActionListener(e -> {
            SendAction sendAction = _project.getService(SendAction.class);
            sendAction.doActionPerformed(getMainPanel(),contentStr,attachments);
        });
        return refreshAction;
    }

    private @NotNull IconButton getPinAction(JsonObject chatItem) {
        Icon showIcon = chatItem.get("isPin").getAsBoolean() ? AllIcons.Actions.IntentionBulb : AllIcons.Actions.IntentionBulbGrey;
        IconButton pinAction = new IconButton("prompt",showIcon);
        pinAction.addActionListener(e -> {
            if (chatItem.get("isPin").getAsBoolean()) {
                getMainPanel().getContentPanel().deletePin(chatItem.get("id").getAsString());
                getMainPanel().getPromptsPanel().delete(chatItem.get("id").getAsString());
            }else {
                if (getMainPanel().getContentPanel().AIPrompts.size() >=32) {
                    messageActionCallback.ontShowBalloon("Cannot add more prompt！！！", MessageType.ERROR);
                    return;
                }
                getMainPanel().getContentPanel().addPin(chatItem);
                getMainPanel().getPromptsPanel().addPrompt(chatItem);
                messageActionCallback.ontShowBalloon("Add prompt successfully", MessageType.INFO);
            }
        });
        return pinAction;
    }

    private @NotNull IconButton getTrashAction(String chatId) {
        IconButton trashAction = new IconButton("delete",AllIcons.Actions.GC);
        trashAction.addActionListener(e -> {
            getMainPanel().getContentPanel().delete(chatId);
        });
        return trashAction;
    }

    private @NotNull IconButton getEditAction(JsonObject chatItem,JsonArray attachments) {
        IconButton editAction = new IconButton("edit",AllIcons.Actions.Edit);
        editAction.addActionListener(e -> {
            getMainPanel().getInputTextArea().setContent(chatItem.get("content").getAsString());
            getMainPanel().getContentPanel().addImageListFromCache(attachments);
            getMainPanel().getInputTextArea().getTextarea().requestFocus();
        });
        return editAction;
    }

    private @NotNull IconButton getCopyAction(JsonObject chatItem) {
        IconButton copyAction = new IconButton("copy",AllIcons.Actions.Copy);
        copyAction.addActionListener(e -> {
            ClipboardUtil.setStr(chatItem.get("content").getAsString());
            messageActionCallback.ontShowBalloon("Copy successfully", MessageType.INFO);
        });
        return copyAction;
    }

    public void setUpdate(JsonObject chatItem) {
        for (String key : chatItem.keySet()) {
            JsonElement value = chatItem.get(key);
            _chatItem.add(key, value); // 添加或替换值
        }
        for (IconButton button : actionButtons) {
            if (button == pinAction) {
                Icon showIcon = _chatItem.get("isPin").getAsBoolean() ? AllIcons.Actions.IntentionBulb : AllIcons.Actions.IntentionBulbGrey;
                pinAction.setIcon(showIcon);
            }
        }
    }


    public void setDisabled(Boolean disable) {
        for (IconButton button : actionButtons) {
            button.setEnabled(!disable); // 禁用或启用按钮
        }
    }

    public void setDisabledRerun(Boolean disable) {
        if (refreshAction!=null) {
            refreshAction.setEnabled(!disable);
        }
        if (pinAction!=null) {
            pinAction.setEnabled(!disable);
        }
    }

    private MainPanel getMainPanel() {
        return (MainPanel) _project.getUserData(ACTIVE_CONTENT);
    }

    /**
     * 清理方法
     */
    public void cleanup() {
        actionButtons.clear();

        removeAll();
    }
}
