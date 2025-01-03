package com.shine.ai.ui;

import cn.hutool.core.swing.clipboard.ClipboardUtil;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.ui.JBUI;
import com.shine.ai.core.SendAction;
import com.shine.ai.util.BalloonUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.shine.ai.MyToolWindowFactory.ACTIVE_CONTENT;

public class MessageActionsComponent extends JPanel {

    private static Project _project;

    private IconButton editAction;
    private IconButton refreshAction;
    private IconButton trashAction;
    private IconButton pinAction;
    private IconButton copyAction;

    private List<IconButton> actionButtons = new ArrayList<>();

    public MessageActionsComponent(Project project, JsonObject chatItem, JComponent component) {
        _project = project;

        boolean isMe = chatItem.get("isMe").getAsBoolean();
        String content = chatItem.get("content").getAsString();

        String chatId = chatItem.get("chatId").getAsString();

        setDoubleBuffered(true);
        setOpaque(true);
        setBorder(JBUI.Borders.empty());

        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
        setLayout(flowLayout); // 从左到右排列

        editAction = getEditAction(chatItem);
        add(editAction);
        actionButtons.add(editAction);

        if (Boolean.TRUE.equals(isMe)) {
            refreshAction = getRefreshAction(component, content);
            add(refreshAction);
            actionButtons.add(refreshAction);
        }

        trashAction = getTrashAction(component, chatId);
        add(trashAction);
        actionButtons.add(trashAction);

        pinAction = getPinAction(component, chatItem);
        add(pinAction);
        actionButtons.add(pinAction);

        copyAction = getCopyAction(component, chatItem);
        add(copyAction);
        actionButtons.add(copyAction);
    }

    private static @NotNull IconButton getRefreshAction(JComponent component, String contentStr) {
        IconButton refreshAction = new IconButton("rerun",AllIcons.Actions.Refresh);
        refreshAction.addActionListener(e -> {
            SendAction sendAction = _project.getService(SendAction.class);
            sendAction.doActionPerformed(getMainPanel(),contentStr);
        });
        return refreshAction;
    }

    private static @NotNull IconButton getPinAction(JComponent component, JsonObject chatItem) {
        Icon showIcon = chatItem.get("isPin").getAsBoolean() ? AllIcons.Actions.IntentionBulb : AllIcons.Actions.IntentionBulbGrey;
        IconButton pinAction = new IconButton("prompt",showIcon);
        pinAction.addActionListener(e -> {
            if (chatItem.get("isPin").getAsBoolean()) {
                getMainPanel().getContentPanel().deletePin(chatItem.get("chatId").getAsString(),component);
                getMainPanel().getPromptsPanel().delete(chatItem.get("chatId").getAsString(),component);
            }else {
                if (getMainPanel().getContentPanel().AIPrompts.size() >=9) {
                    BalloonUtil.showBalloon("Cannot add more prompt！！！", MessageType.ERROR,component);
                    return;
                }
                getMainPanel().getContentPanel().addPin(chatItem,component);
                getMainPanel().getPromptsPanel().addPrompt(chatItem);
                BalloonUtil.showBalloon("Add prompt successfully", MessageType.INFO,component);
            }
        });
        return pinAction;
    }

    private static @NotNull IconButton getTrashAction(JComponent component, String chatId) {
        IconButton trashAction = new IconButton("delete",AllIcons.Actions.GC);
        trashAction.addActionListener(e -> {
            getMainPanel().getContentPanel().delete(chatId,component);
        });
        return trashAction;
    }

    private static @NotNull IconButton getEditAction(JsonObject chatItem) {
        IconButton editAction = new IconButton("edit",AllIcons.Actions.Edit);
        editAction.addActionListener(e -> {
            getMainPanel().getInputTextArea().setContent(chatItem.get("content").getAsString());
            getMainPanel().getInputTextArea().getTextarea().requestFocus();
        });
        return editAction;
    }

    private static @NotNull IconButton getCopyAction(JComponent component, JsonObject chatItem) {
        IconButton copyAction = new IconButton("copy",AllIcons.Actions.Copy);
        copyAction.addActionListener(e -> {
            ClipboardUtil.setStr(chatItem.get("content").getAsString());
            BalloonUtil.showBalloon("Copy successfully", MessageType.INFO,component);
        });
        return copyAction;
    }

    public void setDisabled(Boolean disable) {
        for (IconButton button : actionButtons) {
            button.setEnabled(!disable); // 禁用或启用按钮
        }
    }

    public void setDisabledRerunAndTrash(Boolean disable) {
        if (refreshAction!=null) {
            refreshAction.setEnabled(!disable);
        }
        if (trashAction!=null) {
            trashAction.setEnabled(!disable);
        }
    }

    private static MainPanel getMainPanel() {
        return (MainPanel) _project.getUserData(ACTIVE_CONTENT);
    }
}
