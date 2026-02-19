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

package com.shine.ai.ui.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shine.ai.settings.ChatCollectionDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ChatCollectionAction extends AnAction {
    private final JComponent component;

    private boolean enabled = true;

    public ChatCollectionAction(JComponent component) {
        super(() -> "Chat Collection", AllIcons.Actions.AddList);
        this.component = component;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 这里不禁用状态了，toolwindow的刷新机制导致会有问题
         e.getPresentation().setEnabled(this.enabled);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 确保 update 方法在 EDT 上执行，这样 UI 更新是即时的。
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (enabled) {
            new ChatCollectionDialog(e.getProject()).openDialog(component);
        }
    }
}
