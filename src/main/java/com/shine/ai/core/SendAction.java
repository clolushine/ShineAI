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

package com.shine.ai.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.shine.ai.AIHandler;

import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MessageComponent;
import com.shine.ai.ui.MessageGroupComponent;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.StringUtil;
import okhttp3.Call;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static com.shine.ai.MyToolWindowFactory.ACTIVE_CONTENT;


public class SendAction extends AnAction {

    private static final Logger LOG = LoggerFactory.getLogger(SendAction.class);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private String data;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Object mainPanel = project.getUserData(ACTIVE_CONTENT);
        doActionPerformed((MainPanel) mainPanel, data,null);
    }

    private boolean presetCheck(MainPanel mainPanel) {
        if (stateStore == null) {
            BalloonUtil.showBalloon("Please login ShineAI first.", MessageType.WARNING, mainPanel.getContentPanel());
            return false;
        }
        if (StringUtil.isEmpty(stateStore.Useremail) || StringUtil.isEmpty(stateStore.UserToken)) {
            BalloonUtil.showBalloon("Please login ShineAI first.", MessageType.WARNING, mainPanel.getContentPanel());
            return false;
        }
        return true;
    }

    private boolean currentModelCheck(MainPanel mainPanel) {
        JsonObject AISetInfo = mainPanel.getContentPanel().getAISetInfo();
        if (AISetInfo.get("aiModel").isJsonNull() || AISetInfo.get("aiModel").getAsString().isEmpty()) {
            BalloonUtil.showBalloon("Please select a AI model first.", MessageType.WARNING, mainPanel.getContentPanel());
            return false;
        }
        return true;
    }

    private JsonObject getAISetInfo(MainPanel mainPanel) {
        return mainPanel.getContentPanel().getAISetInfo();
    }

    public void doActionPerformed(MainPanel mainPanel, String content, JsonArray attachments) {
        if (!presetCheck(mainPanel)) {
            return;
        }
        if (!currentModelCheck(mainPanel)) {
            return;
        }
        // Filter the empty text
        if (StringUtils.isEmpty(content)) {
            return;
        }
        boolean isRerun = attachments != null;

        // Reset the question container
        if(!isRerun) mainPanel.getInputTextArea().getTextarea().setText("");
        mainPanel.aroundRequest(true);

        MessageGroupComponent contentPanel = mainPanel.getContentPanel();
        JsonObject messageMy = contentPanel.MyInfo.deepCopy();
        JsonObject messageAi = contentPanel.AIInfo.deepCopy();

        String collId = contentPanel.getChatCollection().get("id").getAsString();
        String fromLLM = contentPanel.getAISetInfo().get("aiModel").getAsString();
        String fromProvider = mainPanel.getAIKey().toLowerCase();
        boolean webSearch = contentPanel.getAISetInfo().get("online").getAsBoolean();

        String chatId = GeneratorUtil.generateWithUUID();

        messageMy.addProperty("id", chatId);
        messageMy.addProperty("collId", collId);
        messageMy.addProperty("createAt",GeneratorUtil.getTimestamp());
        messageMy.addProperty("updateAt",GeneratorUtil.getTimestamp());
        messageMy.addProperty("content",content);
        messageMy.addProperty("fromLLM",fromLLM);
        messageMy.addProperty("fromProvider",fromProvider);
        messageMy.addProperty("webSearch",webSearch);

        if (!isRerun) {
            // 写入一些额外信息
            JsonArray newAttachments = new JsonArray();
            for (JsonElement attachment: contentPanel.getUploadList()) {
                JsonObject attach = attachment.getAsJsonObject();
                attach.addProperty("id",GeneratorUtil.generateWithUUID());
                attach.addProperty("createAt",GeneratorUtil.getTimestamp());
                attach.addProperty("updateAt",GeneratorUtil.getTimestamp());
                attach.addProperty("chatId",chatId);
                attach.addProperty("collId",collId);
                newAttachments.add(attach);
            }
            messageMy.add("attachments",newAttachments); // 重发也不添加上传附件
            contentPanel.removeUploadList(); // 重发不清空上传附件，否则清空
        }else {
            messageMy.add("attachments",attachments);
        }
        
        MessageComponent messageMyComponent = contentPanel.add(messageMy);
        messageMyComponent.messageActions.setDisabledRerun(true); // 禁用按钮

        messageAi.addProperty("id", GeneratorUtil.generateWithUUID());
        messageAi.addProperty("collId", collId);
        messageAi.addProperty("createAt",GeneratorUtil.getTimestamp());
        messageAi.addProperty("updateAt",GeneratorUtil.getTimestamp());
        messageAi.addProperty("fromLLM",fromLLM);
        messageAi.addProperty("fromProvider",fromProvider);
        messageAi.addProperty("webSearch",webSearch);

        MessageComponent messageAIComponent = contentPanel.add(messageAi);
        messageAIComponent.messageActions.setDisabled(true); // 禁用按钮

        MessageComponent AIAnswer = contentPanel.getLastItem(null);
        try {
            ExecutorService executorService = mainPanel.getExecutorService();
            AIHandler AIHandler = new AIHandler();

            // Request the server
            if (!getAISetInfo(mainPanel).get("aiStream").getAsBoolean()) {
                executorService.submit(() -> {
                    Call handle = AIHandler.handle(mainPanel, AIAnswer, messageMy);
                    contentPanel.setRequestHolder(handle);
                });
            } else {
                EventSource handle = AIHandler.handleStream(mainPanel, AIAnswer, messageMy);
                contentPanel.setRequestHolder(handle);
            }
        } catch (Exception e) {
            mainPanel.aroundRequest(false);
            LOG.error("AIAssistant: Request failed, error={}", e.getMessage());
        }
    }
}
