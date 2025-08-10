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
