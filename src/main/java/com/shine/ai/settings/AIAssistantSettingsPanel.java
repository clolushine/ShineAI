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

package com.shine.ai.settings;

import cn.hutool.core.swing.clipboard.ClipboardUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.MessageType;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.ui.LoadingButton;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.ShineAIUtil;
import com.shine.ai.util.TimeUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.shine.ai.MyToolWindowFactory.*;

public class AIAssistantSettingsPanel implements Configurable, Disposable {
    private MessageBusConnection connection;

    private JPanel myMainPanel;
    private JSpinner requestTimeoutField;
    private JLabel requestTimeoutHelpLabel;
    private JPanel requestTitledBorderBox;
    private JComboBox<String> firstCombobox;
    private JComboBox<String> secondCombobox;
    private JComboBox<String> thirdCombobox;
    private JCheckBox enableLineWarpCheckBox;
    private JCheckBox enableAvatarCheckBox;
    private JCheckBox enableAIInfoCheckBox;
    private JPanel contentTitledBorderBox;
    private JLabel contentOrderHelpLabel;
    private JPanel userOptions;
    private JPanel userAuthPanel;
    private JLabel UseremailField;
    private JPanel loginTitledBorderBox;
    private JLabel LastLoginTimeField;
    private JLabel UsernameField;
    private JLabel UserIDField;
    private JProgressBar storageUsedInfoField;
    private JPanel storageTitledBorderBox;
    private JLabel storageUsedInfoStringField;
    private JLabel storageUsedMBField;
    private JLabel storageHelpLabel;

    private JPanel loginPanel;
    private JPanel logoutPanel;
    private JSpinner dialogFontSizeSpinner;
    private JPanel signPanel;
    private JComboBox<String> PromptsPosComboBox;
    private JLabel cacheUsedInfoStringField;

    private LoadingButton logoutButton;
    private LoadingButton loginButton;
    private LinkLabel<String> signLink;

    private final String[] comboboxItemsString = {
            OpenAI_CONTENT_NAME,
            Google_AI_CONTENT_NAME,
            Anthropic_AI_CONTENT_NAME,
            CLOUDFLARE_AI_CONTENT_NAME,
            GROQ_AI_CONTENT_NAME,
            OpenRouter_AI_CONTENT_NAME
    };

    private final Map<Integer,String> promptsComboBoxItemsPos = new HashMap<>(){{
        put(0,"left");
        put(1,"right");
        put(2,"hide");
    }};

    private boolean needRestart = false;

    public static final String SHINE_AI_BASE_URL = "https://v.34343433.xyz/api";

    public AIAssistantSettingsPanel() {
        createSignLink();
        createUserAuthButton();
        init();
    }

    private void init() {
        SpinnerNumberModel requestTimeoutModel = new SpinnerNumberModel(60000, 5000, 60000, 1000);
        requestTimeoutField.setModel(requestTimeoutModel);

        firstCombobox.setModel(new DefaultComboBoxModel<>(comboboxItemsString));
        secondCombobox.setModel(new DefaultComboBoxModel<>(comboboxItemsString));
        thirdCombobox.setModel(new DefaultComboBoxModel<>(comboboxItemsString));

        SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(12, 8, 20, 1);
        dialogFontSizeSpinner.setModel(fontSizeModel);

        PromptsPosComboBox.setModel(new DefaultComboBoxModel<>(promptsComboBoxItemsPos.values().toArray(new String[0])));

        updateCacheUsedInfo(); // 更新缓存使用情况

        updateStorageUsedInfo(); // 更新storage缓存使用信息

        // 注册监听器
        connection = ApplicationManager.getApplication().getMessageBus().connect();
        connection.subscribe(LoginDialog.LoginSuccessListener.TOPIC, (LoginDialog.LoginSuccessListener) this::updateLoginUserInfo);

        assert UseremailField != null;
        UseremailField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!UseremailField.getText().isBlank()) {
                    ClipboardUtil.setStr(UseremailField.getText());
                    BalloonUtil.showBalloon("Copy successfully", MessageType.INFO,UseremailField);
                }
            }
        });

        assert UserIDField != null;
        UserIDField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!UserIDField.getText().isBlank()) {
                    ClipboardUtil.setStr(UserIDField.getText());
                    BalloonUtil.showBalloon("Copy successfully", MessageType.INFO,UserIDField);
                }
            }
        });

        assert signLink != null;
        signLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse("https://unishine.pages.dev/#/pages/login/register");
            }
        });

        assert loginButton != null;
        loginButton.addActionListener(e -> {
            new LoginDialog().openDialog("");
        });

        assert logoutButton != null;
        logoutButton.addActionListener(e -> {
            boolean yes = MessageDialogBuilder.yesNo("Are you sure you want to logout?",
                            "There will be logout.")
                    .yesText("Yes")
                    .noText("No").ask(logoutButton);
            if (yes) {
                logoutButton.setLoading(true);
                CompletableFuture.runAsync(() -> ShineAIUtil.logOutUser(logoutButton))
                        .thenRun(() -> {
                            logoutButton.setLoading(false);
                            updateLoginUserInfo();
                        });
            }
        });
    }

    private String checkTokenExists() {
        if (StringUtil.isNotEmpty(AIAssistantSettingsState.getInstance().UserToken)) {
            return AIAssistantSettingsState.getInstance().UserToken;
        }
        return null;
    }

    @Override
    public void reset() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        requestTimeoutField.setValue(state.requestTimeout);

        firstCombobox.setSelectedItem(state.contentOrder.get(1));
        secondCombobox.setSelectedItem(state.contentOrder.get(2));
        thirdCombobox.setSelectedItem(state.contentOrder.get(3));

        PromptsPosComboBox.setSelectedItem(promptsComboBoxItemsPos.get(state.promptsPos));

        dialogFontSizeSpinner.setValue(state.CHAT_PANEL_FONT_SIZE);

        enableLineWarpCheckBox.setSelected(state.enableLineWarp);
        enableAvatarCheckBox.setSelected(state.enableAvatar);
        enableAIInfoCheckBox.setSelected(state.enableMsgPanelAIInfo);

        UseremailField.setText(state.Useremail);

        updateLoginUserInfo();

        initHelp();
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        AIAssistantSettingsState state =  AIAssistantSettingsState.getInstance();

        // If you change the order, you need to restart the IDE to take effect
        needRestart = !StringUtil.equals(state.contentOrder.get(1), (String)firstCombobox.getSelectedItem())||
                !StringUtil.equals(state.contentOrder.get(2), (String)secondCombobox.getSelectedItem())||
                !StringUtil.equals(state.contentOrder.get(3), (String)thirdCombobox.getSelectedItem()) ||
                state.CHAT_PANEL_FONT_SIZE != (int) dialogFontSizeSpinner.getValue() ||
                promptsComboBoxItemsPos.get(state.promptsPos) != PromptsPosComboBox.getSelectedItem() ||
                !state.enableAvatar == enableAvatarCheckBox.isSelected() ||
                !state.enableLineWarp == enableLineWarpCheckBox.isSelected() ||
                !state.enableMsgPanelAIInfo == enableAIInfoCheckBox.isSelected()
        ;

        return !Objects.equals(state.requestTimeout, requestTimeoutField.getValue()) ||
                promptsComboBoxItemsPos.get(state.promptsPos) != PromptsPosComboBox.getSelectedItem() ||
                !state.enableAvatar == enableAvatarCheckBox.isSelected() ||
                !state.enableLineWarp == enableLineWarpCheckBox.isSelected() ||
                !state.enableMsgPanelAIInfo == enableAIInfoCheckBox.isSelected() ||
                state.CHAT_PANEL_FONT_SIZE != (int) dialogFontSizeSpinner.getValue() ||
                !StringUtil.equals(state.contentOrder.get(1), (String)firstCombobox.getSelectedItem()) ||
                !StringUtil.equals(state.contentOrder.get(2), (String)secondCombobox.getSelectedItem()) ||
                !StringUtil.equals(state.contentOrder.get(3), (String)thirdCombobox.getSelectedItem());
    }

    @Override
    public void apply() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        state.requestTimeout = (Integer) requestTimeoutField.getValue();

        Integer promptsPosItem = promptsComboBoxItemsPos.entrySet()
                .stream()
                .filter(entry ->  entry.getValue() == PromptsPosComboBox.getSelectedItem())
                .map(Map.Entry::getKey)
                .findFirst().orElse(0);

        // 默认使用头像
        state.CHAT_PANEL_FONT_SIZE = (int) dialogFontSizeSpinner.getValue();
        state.promptsPos =  promptsPosItem;
        state.enableLineWarp = enableLineWarpCheckBox.isSelected();
        state.enableAvatar = enableAvatarCheckBox.isSelected();
        state.enableMsgPanelAIInfo = enableAIInfoCheckBox.isSelected();

        state.Useremail = UseremailField.getText();

        updateLoginUserInfo();

        String firstSelected = (String) firstCombobox.getSelectedItem();
        String secondSelected = (String) secondCombobox.getSelectedItem();
        String thirdSelected = (String) thirdCombobox.getSelectedItem();

        // Determine whether each location has a different Content
        List<String> strings = new ArrayList<>(3);
        strings.add(firstSelected);
        strings.add(secondSelected);
        strings.add(thirdSelected);
        List<String> collect = strings.stream().distinct().toList();
        if (collect.size() != strings.size()) {
            MessageDialogBuilder.yesNo("Duplicate Content exists!", "The content of " +
                            "each position must be unique, please re-adjust the order")
                    .yesText("Ok")
                    .noText("Close").ask(myMainPanel);
            return;
        }

        state.contentOrder.put(1, firstSelected);
        state.contentOrder.put(2, secondSelected);
        state.contentOrder.put(3, thirdSelected);

        if (needRestart) {
            boolean yes = MessageDialogBuilder.yesNo("Content order changed!", "Changing " +
                            "the content order requires restarting the IDE to take effect. Do you " +
                            "want to restart to apply the settings?")
                    .yesText("Restart")
                    .noText("Not Now").ask(myMainPanel);
            if (yes) {
                ApplicationManagerEx.getApplicationEx().restart(true);
            }
        }
    }

    @Override
    public void dispose() {
        connection.disconnect(); //  释放资源
    }

    @Override
    public String getDisplayName() {
        return MsgEntryBundle.message("ui.setting.menu.text");
    }

    private void createUserAuthButton() {
        loginButton = new LoadingButton("login");
        logoutButton = new LoadingButton("logout");
        loginPanel.add(loginButton);
        logoutPanel.add(logoutButton);
    }

    private void createSignLink() {
        signLink = new LinkLabel<>("Sign in",null);
        signLink.setBorder(JBUI.Borders.empty(12));
        signPanel.add(signLink);
    }

    private void createUIComponents() {
        requestTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsConnection = new TitledSeparator(MsgEntryBundle.message("ui.setting.server.title"));
        requestTitledBorderBox.add(tsConnection,BorderLayout.CENTER);

        contentTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator tsUrl = new TitledSeparator("Tool Window Settings");
        contentTitledBorderBox.add(tsUrl,BorderLayout.CENTER);

        storageTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator sgBt = new TitledSeparator("Storage Used Info");
        storageTitledBorderBox.add(sgBt,BorderLayout.CENTER);

        loginTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator oaUrl = new TitledSeparator("ShineAI Tools UserAuth");
        loginTitledBorderBox.add(oaUrl,BorderLayout.CENTER);
    }

    public void updateLoginUserInfo() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        loginButton.setEnabled(StringUtil.isEmpty(checkTokenExists()));

        logoutButton.setEnabled(!StringUtil.isEmpty(checkTokenExists()));

        assert UseremailField != null;
        if (!state.Useremail.isBlank()) {
            UseremailField.setText(state.Useremail);
        }

        assert LastLoginTimeField != null;
        if (state.getUserInfo().has("login_ts")) {
            long ts = state.getUserInfo().get("login_ts").getAsLong();
            LastLoginTimeField.setVisible(true);
            LastLoginTimeField.setText(TimeUtil.timeFormat(ts,null));
        }else {
            LastLoginTimeField.setVisible(false);
        }

        assert UserIDField != null;
        if (state.getUserInfo().has("id")) {
            String uid = state.getUserInfo().get("id").getAsString();
            UserIDField.setVisible(true);
            UserIDField.setText(uid);
        }else {
            UserIDField.setVisible(false);
        }

        assert UsernameField != null;
        if (state.getUserInfo().has("name")) {
            String name = state.getUserInfo().get("name").getAsString();
            UsernameField.setVisible(true);
            UsernameField.setText(name);
        }else {
            UsernameField.setVisible(false);
        }
    }

    public void initHelp() {
        requestTimeoutHelpLabel.setFont(JBUI.Fonts.smallFont());
        requestTimeoutHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        contentOrderHelpLabel.setFont(JBUI.Fonts.smallFont());
        contentOrderHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        storageHelpLabel.setFont(JBUI.Fonts.smallFont());
        storageHelpLabel.setForeground(UIUtil.getContextHelpForeground());
    }

    public void updateStorageUsedInfo() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
//        assert storageUsedInfoField != null;
//        int percentage = state.getStorageUsagePercentage();
//        storageUsedInfoField.setValue(percentage);
//        Color storageUsedInfoStringColor = Color.decode(percentage <= 20 ? "#4ddd87" : percentage <= 80 ? "#10AEFF" : "#ff0000");
//        storageUsedInfoStringField.setForeground(storageUsedInfoStringColor);
//        storageUsedInfoStringField.setText(percentage + "%");
        assert storageUsedMBField != null;
        storageUsedMBField.setText(state.getStorageUsageMBInfo());
    }

    public void updateCacheUsedInfo() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
        assert cacheUsedInfoStringField != null;
        String infoStr = state.getCacheUsageMBInfo();
        cacheUsedInfoStringField.setText(infoStr);
    }
}
