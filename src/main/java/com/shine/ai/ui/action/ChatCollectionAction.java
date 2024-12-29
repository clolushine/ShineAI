package com.shine.ai.ui.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.shine.ai.settings.ChatCollectionDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ChatCollectionAction extends DumbAwareAction {
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
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled); // 使用 enabled 字段控制 enabled 状态
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new ChatCollectionDialog(e.getProject()).openDialog(component);
    }
}
