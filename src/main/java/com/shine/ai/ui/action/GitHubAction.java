package com.shine.ai.ui.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;



public class GitHubAction extends DumbAwareAction {

    public GitHubAction() {
        super(() -> "ShineAI GitHub", AllIcons.Vcs.Vendors.Github);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.browse("https://github.com/cloudshinezhong/ShineAI");
    }
}
