package com.shine.ai;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.*;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.settings.CFAISettingPanel;
import com.shine.ai.settings.GoogleAISettingPanel;
import com.shine.ai.settings.GroqAISettingPanel;
import com.shine.ai.ui.action.ChatCollectionAction;
import com.shine.ai.ui.action.SettingAction;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class MyToolWindowFactory implements ToolWindowFactory {

    public static final Key ACTIVE_CONTENT = Key.create("ActiveContent");

    public static final String CLOUDFLARE_AI_CONTENT_NAME = "CloudflareAI";
    public static final String Google_AI_CONTENT_NAME = "GoogleAI";
    public static final String GROQ_AI_CONTENT_NAME = "GroqAI";

    public static ChatCollectionAction chatCollectionAction = null;

    public String lastActiveTab = null;

    public MyToolWindowFactory() {

    }

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();

        CloudflareAIToolWindow cloudflareAIToolWindow = new CloudflareAIToolWindow(project, CFAISettingPanel.class);
        Content cloudflareAIContent = contentFactory.createContent(cloudflareAIToolWindow.getContent(), CLOUDFLARE_AI_CONTENT_NAME, false);
        cloudflareAIContent.setCloseable(false);

        GoogleAIToolWindow googleAIToolWindow = new GoogleAIToolWindow(project, GoogleAISettingPanel.class);
        Content googleAIContent = contentFactory.createContent(googleAIToolWindow.getContent(), Google_AI_CONTENT_NAME, false);
        googleAIContent.setCloseable(false);

        GroqAIToolWindow groqAIToolWindow = new GroqAIToolWindow(project, GroqAISettingPanel.class);
        Content groqAIContent = contentFactory.createContent(groqAIToolWindow.getContent(), GROQ_AI_CONTENT_NAME, false);
        groqAIContent.setCloseable(false);

        AIAssistantSettingsState settingsState = AIAssistantSettingsState.getInstance();
        Map<Integer, String> contentSort = settingsState.contentOrder;

        for (int i = 0 ; i <= 2 ; i++) {
            toolWindow.getContentManager().addContent(getContent(contentSort.get(i + 1), cloudflareAIContent,
                    googleAIContent, groqAIContent), i);
        }


        // Set the default component. It require the 1st container
        String firstContentName = contentSort.get(1);
        lastActiveTab = firstContentName;

        switch (firstContentName) {
            case CLOUDFLARE_AI_CONTENT_NAME:
                project.putUserData(ACTIVE_CONTENT, cloudflareAIToolWindow.getPanel());
                break;
            case Google_AI_CONTENT_NAME:
                project.putUserData(ACTIVE_CONTENT, googleAIToolWindow.getPanel());
                break;
            case GROQ_AI_CONTENT_NAME:
                project.putUserData(ACTIVE_CONTENT, groqAIToolWindow.getPanel());
                break;
            default:
                throw new RuntimeException("Error content name, content name must be one of ChatGPT, GPT-3.5-Turbo, Online ChatGPT");
        }

        // Add the selection listener
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                String displayName = event.getContent().getDisplayName();
                if (StringUtil.equals(lastActiveTab, displayName)) return;
                if (CLOUDFLARE_AI_CONTENT_NAME.equals(displayName)) {
                    lastActiveTab = displayName;
                    cloudflareAIToolWindow.refreshInfo();
                    cloudflareAIToolWindow.refreshMessages();
                    project.putUserData(ACTIVE_CONTENT,cloudflareAIToolWindow.getPanel());
                } else if (Google_AI_CONTENT_NAME.equals(displayName)) {
                    lastActiveTab = displayName;
                    googleAIToolWindow.refreshInfo();
                    googleAIToolWindow.refreshMessages();
                    project.putUserData(ACTIVE_CONTENT,googleAIToolWindow.getPanel());
                } else if (GROQ_AI_CONTENT_NAME.equals(displayName)) {
                    lastActiveTab = displayName;
                    groqAIToolWindow.refreshInfo();
                    groqAIToolWindow.refreshMessages();
                    project.putUserData(ACTIVE_CONTENT,groqAIToolWindow.getPanel());
                }
            }
        });

        chatCollectionAction = new ChatCollectionAction(toolWindow.getComponent());

        List<AnAction> actionList = new ArrayList<>();
        actionList.add(chatCollectionAction);
        actionList.add(new SettingAction(MsgEntryBundle.message("action.settings")));
        toolWindow.setTitleActions(actionList);
    }

    private Content getContent(String key, Content cloudflareAIContent ,
                                 Content googleAIContent,
                               Content groqAIContent) {
        if (CLOUDFLARE_AI_CONTENT_NAME.equals(key)) {
            return cloudflareAIContent;
        } else if (Google_AI_CONTENT_NAME.equals(key)) {
            return googleAIContent;
        } else if (GROQ_AI_CONTENT_NAME.equals(key)) {
            return groqAIContent;
        }
        return null;
    }

    public static void disabledCollectionAction(Boolean disable) {
        if (chatCollectionAction != null) {
            chatCollectionAction.setEnabled(!disable); // 禁用 action
        }
    }
}
