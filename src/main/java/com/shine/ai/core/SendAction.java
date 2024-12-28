package com.shine.ai.core;

import com.google.gson.JsonObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.shine.ai.AbstractHandler;
import com.shine.ai.CloudflareAIHandler;
import com.shine.ai.GoogleAIHandler;
import com.shine.ai.GroqAIHandler;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.settings.CFAISettingPanel;
import com.shine.ai.settings.GoogleAISettingPanel;
import com.shine.ai.settings.GroqAISettingPanel;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MessageComponent;
import com.shine.ai.ui.MessageGroupComponent;
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
        doActionPerformed((MainPanel) mainPanel, data);
    }

    private boolean presetCheck() {
        if (StringUtil.isEmpty(stateStore.Useremail) | StringUtil.isEmpty(stateStore.UserToken)) {
            Notifications.Bus.notify(
                    new Notification(MsgEntryBundle.message("group.id"),
                            "Wrong setting",
                            "Please login ShineAI first.",
                            NotificationType.ERROR));
            return false;
        }
        return true;
    }

    private boolean currentModelCheck(MainPanel mainPanel) {
        JsonObject AISetInfo = mainPanel.getContentPanel().getAISetInfo();
        if (AISetInfo.get("aiModel").isJsonNull() || AISetInfo.get("aiModel").getAsString().isEmpty()) {
            Notifications.Bus.notify(
                    new Notification(MsgEntryBundle.message("group.id"),
                            "Wrong setting",
                            "Please select a AI model first.",
                            NotificationType.ERROR));
            return false;
        }
        return true;
    }

    private JsonObject getAISetInfo(MainPanel mainPanel) {
        return mainPanel.getContentPanel().getAISetInfo();
    }

    private AbstractHandler getAIHandler(MainPanel mainPanel) {
        Class<?> AIPanel = mainPanel.getAIPanel();
        Project project = mainPanel.getProject();
        AbstractHandler AIHandler = null;
        if (AIPanel.equals(CFAISettingPanel.class)) {
            AIHandler = project.getService(CloudflareAIHandler.class);
        } else if (AIPanel.equals(GoogleAISettingPanel.class)) {
            AIHandler = project.getService(GoogleAIHandler.class);
        } else if (AIPanel.equals(GroqAISettingPanel.class)) {
            AIHandler = project.getService(GroqAIHandler.class);
        }
        return AIHandler;
    }

    public void doActionPerformed(MainPanel mainPanel, String content) {
        if (!presetCheck()) {
            return;
        }
        if (!currentModelCheck(mainPanel)) {
            return;
        }
        // Filter the empty text
        if (StringUtils.isEmpty(content)) {
            return;
        }
        // Reset the question container
        mainPanel.getInputTextArea().getTextarea().setText("");
        mainPanel.aroundRequest(true);

        MessageGroupComponent contentPanel = mainPanel.getContentPanel();

        JsonObject messageMy = mainPanel.getContentPanel().MyInfo.deepCopy();
        JsonObject messageAi =  mainPanel.getContentPanel().AIInfo.deepCopy();

        messageMy.addProperty("chatId", GeneratorUtil.generateWithUUID());
        messageMy.addProperty("time",GeneratorUtil.getTimestamp());
        messageMy.addProperty("content",content);
        MessageComponent messageMyComponent = contentPanel.add(messageMy);
        messageMyComponent.messageActions.setDisabledRerunAndTrash(true); // 禁用按钮

        messageAi.addProperty("chatId", GeneratorUtil.generateWithUUID());
        messageAi.addProperty("time",GeneratorUtil.getTimestamp());
        MessageComponent messageAIComponent = contentPanel.add(messageAi);
        messageAIComponent.messageActions.setDisabled(true); // 禁用按钮

        AbstractHandler AIHandler = getAIHandler(mainPanel);
        MessageComponent AIAnswer = mainPanel.getContentPanel().getLastItem(null);
        try {
            ExecutorService executorService = mainPanel.getExecutorService();
            // Request the server
            if (!getAISetInfo(mainPanel).get("aiStream").getAsBoolean()) {
                executorService.submit(() -> {
                    Call handle = AIHandler.handle(mainPanel, AIAnswer, content);
                    mainPanel.getContentPanel().setRequestHolder(handle);
                });
            } else {
                EventSource handle = AIHandler.handleStream(mainPanel, AIAnswer, content);
                mainPanel.getContentPanel().setRequestHolder(handle);
            }
        } catch (Exception e) {
            mainPanel.aroundRequest(false);
            LOG.error("AIAssistant: Request failed, error={}", e.getMessage());
        }
    }
}
