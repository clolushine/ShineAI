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

package com.shine.ai;

import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.*;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.*;
import com.shine.ai.ui.action.ChatCollectionAction;
import com.shine.ai.ui.action.GitHubAction;
import com.shine.ai.ui.action.SettingAction;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;


public class MyToolWindowFactory implements ToolWindowFactory {

    public static final Key ACTIVE_CONTENT = Key.create("ActiveContent");

    public static final String CLOUDFLARE_AI_CONTENT_NAME = "CloudflareAI";
    public static final String CLOUDFLARE_AI_KEY = "CLOUDFLARE";
    public static final String CLOUDFLARE_AI_NAME = "CF AI";
    public static final String CLOUDFLARE_AI_ICON = AIAssistantIcons.CF_AI_URL;
    public static final String CLOUDFLARE_AI_API = "/ai/aiChat";
    public static final String CLOUDFLARE_AI_LLM_API = "/ai/aiModels";
    public static final String CLOUDFLARE_SETTING_CLASS_NAME = CFAISettingPanel.class.getName();

    public static final String Google_AI_CONTENT_NAME = "GoogleAI";
    public static final String Google_AI_KEY = "GOOGLE";
    public static final String Google_AI_NAME = "GEMINI";
    public static final String Google_AI_ICON = AIAssistantIcons.GOOGLE_AI_URL;
    public static final String Google_AI_API = "/gem/geminiChat";
    public static final String Google_AI_LLM_API = "/gem/gems";
    public static final String Google_SETTING_CLASS_NAME = GoogleAISettingPanel.class.getName();

    public static final String GROQ_AI_CONTENT_NAME = "GroqAI";
    public static final String GROQ_AI_KEY = "GROQ";
    public static final String GROQ_AI_NAME = "GROQ";
    public static final String GROQ_AI_ICON = AIAssistantIcons.GROQ_AI_URL;
    public static final String GROQ_AI_API = "/gpt/gptChat";
    public static final String GROQ_AI_LLM_API = "/gpt/models";
    public static final String GROQ_SETTING_CLASS_NAME = GroqAISettingPanel.class.getName();

    // 新增AI
    public static final String OpenAI_CONTENT_NAME = "OpenAI";
    public static final String OpenAI_KEY = "OPENAI";
    public static final String OpenAI_NAME = "OPENAI";
    public static final String OpenAI_ICON = AIAssistantIcons.OPENAI_URL;
    public static final String OpenAI_AI_API = "/gpt/gptChat";
    public static final String OpenAI_LLM_API = "/gpt/models";
    public static final String OpenAI_SETTING_CLASS_NAME = OpenAISettingPanel.class.getName();

    public static final String Anthropic_AI_CONTENT_NAME = "AnthropicAI";
    public static final String Anthropic_AI_KEY = "ANTHROPIC";
    public static final String Anthropic_AI_NAME = "ANTHR";
    public static final String Anthropic_AI_ICON = AIAssistantIcons.ANTHROPIC_AI_URL;
    public static final String Anthropic_AI_API = "/gpt/gptChat";
    public static final String Anthropic_AI_LLM_API = "/gpt/models";
    public static final String Anthropic_SETTING_CLASS_NAME = AnthropicAISettingPanel.class.getName();

    public static final String OpenRouter_AI_CONTENT_NAME = "OpenRouterAI";
    public static final String OpenRouter_AI_KEY = "OPENROUTER";
    public static final String OpenRouter_AI_NAME = "ROUTER";
    public static final String OpenRouter_AI_ICON = AIAssistantIcons.OPENROUTER_AI_URL;
    public static final String OpenRouter_AI_API = "/gpt/gptChat";
    public static final String OpenRouter_AI_LLM_API = "/gpt/models";
    public static final String OpenRouter_SETTING_CLASS_NAME = OpenRouterAISettingPanel.class.getName();


    public static final Map<String, Class<? extends Configurable>> MAPPINGS = new HashMap<>() {{
        put(CLOUDFLARE_SETTING_CLASS_NAME, CFAISettingPanel.class);
        put(Google_SETTING_CLASS_NAME, GoogleAISettingPanel.class);
        put(GROQ_SETTING_CLASS_NAME, GroqAISettingPanel.class);
        put(OpenAI_SETTING_CLASS_NAME, OpenAISettingPanel.class);
        put(Anthropic_SETTING_CLASS_NAME, AnthropicAISettingPanel.class);
        put(OpenRouter_SETTING_CLASS_NAME, OpenRouterAISettingPanel.class);
    }};

    // 定义一个用于存储 Action 实例的 Key
    public static ChatCollectionAction chatCollectionAction = null;

    public List<AIToolWindow> AIToolWindows = new ArrayList<>();

    public String lastActiveTab = null;

    // 初始化图片窗
    public static final PreviewImageDialog previewImageDialog = new PreviewImageDialog();

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

        AIAssistantSettingsState settingsState = AIAssistantSettingsState.getInstance();
        Map<Integer, String> contentSort = settingsState.contentOrder;
        List<JsonObject> AIVenderList = settingsState.getAIVenderList();

        // 最多渲染3个
        for (int i = 0 ; i < contentSort.size() ; i++) {
            String contentName = contentSort.get(i + 1);
            JsonObject aiItem = AIVenderList.stream()
                    .filter(item -> StringUtil.equals(item.get("title").getAsString(), contentName))
                    .findFirst().orElse(null);
            assert aiItem != null;
            AIToolWindow AIToolWindow = new AIToolWindow(contentName,project, MAPPINGS.get(aiItem.get("className").getAsString()));
            Content AIContent = contentFactory.createContent(AIToolWindow.getContent(), contentName, false);
            toolWindow.getContentManager().addContent(AIContent, i);
            AIContent.setCloseable(false);
            AIToolWindows.add(AIToolWindow);
        }

        // Set the default component. It require the 1st container
        String firstContentName = contentSort.get(1);
        lastActiveTab = firstContentName;

        // 初始化第一个窗口
        for (AIToolWindow aiToolWindow : AIToolWindows) {
            String contentName = aiToolWindow.getPanelName();
            if (StringUtil.equals(contentName, firstContentName)) {
                // 加载窗口内容
                aiToolWindow.contentInit();
                project.putUserData(ACTIVE_CONTENT, aiToolWindow.getPanel());
            }
        }

        // Add the selection listener
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                String displayName = event.getContent().getDisplayName();
                if (StringUtil.equals(lastActiveTab, displayName)) return;
                // 刷新窗口
                for (AIToolWindow toolWindow : AIToolWindows) {
                    String contentName = toolWindow.getPanelName();
                    if (StringUtil.equals(contentName, displayName)) {
                        lastActiveTab = displayName;
                        // 加载窗口内容
                        toolWindow.contentInit();
                        project.putUserData(ACTIVE_CONTENT, toolWindow.getPanel());
                    }else {
                        // 销毁已经已经切换的窗口
                        toolWindow.contentDispose();
                    }
                }
            }
        });

       chatCollectionAction = new ChatCollectionAction(toolWindow.getComponent());

        List<AnAction> actionList = new ArrayList<>();
        actionList.add(chatCollectionAction); // 添加到工具窗动作列表
        actionList.add(new GitHubAction());
        actionList.add(new SettingAction(MsgEntryBundle.message("action.settings")));
        toolWindow.setTitleActions(actionList);


        // 监听 ToolWindow 的激活事件
        // Option 1: ToolWindowManagerListener (更全局)
//         project.getMessageBus().connect().subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
//             @Override
//             public void toolWindowShown(@NotNull ToolWindow activatedToolWindow) {
//                 if (activatedToolWindow == toolWindow) { // 确保是你的 ToolWindow
//                     String displayName = toolWindow.getContentManager().getSelectedContent().getDisplayName();
//                     AIToolWindow toolWin = getCurrentAIToolWindow(displayName);
//                     if (toolWin != null) {
//
//                     }
//                 }
//             }
//         });
    }

//    private AIToolWindow getCurrentAIToolWindow(String panelName) {
//        AIToolWindow toolWin = null;
//        for (AIToolWindow toolWindow : AIToolWindows) {
//            String contentName = toolWindow.getPanelName();
//            if (StringUtil.equals(contentName, panelName)) {
//                toolWin = toolWindow;
//                break;
//            }
//        }
//        return toolWin;
//    }

    public static void disabledCollectionAction(Boolean disable) {
        if (chatCollectionAction != null) {
            chatCollectionAction.setEnabled(!disable); // 禁用 action
        }
    }
}
