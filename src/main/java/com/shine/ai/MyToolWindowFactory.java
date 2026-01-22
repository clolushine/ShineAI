/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.*;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.*;
import com.shine.ai.ui.action.ChatCollectionAction;
import com.shine.ai.ui.action.GitHubAction;
import com.shine.ai.ui.action.SettingAction;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;

import static com.shine.ai.vendors.AIVendors.MAPPINGS;


public class MyToolWindowFactory implements ToolWindowFactory {

    public static final Key ACTIVE_CONTENT = Key.create("ActiveContent");

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

    }

    public static void disabledCollectionAction(Boolean disable) {
        if (chatCollectionAction != null) {
            chatCollectionAction.setEnabled(!disable); // 禁用 action
        }
    }
}
