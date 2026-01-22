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

package com.shine.ai.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import com.shine.ai.ui.LoadingButton;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.ShineAIUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CompletableFuture;

public class LoginDialog extends JDialog {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private JPanel contentPane;
    private JPanel userAuthPanel;
    private JTextField UseremailField;
    private JBPasswordField PasswordField;
    private JPanel loginPanel;

    private LoadingButton loginButton;

    public LoginDialog() {
        createUserAuthButton();
        init();
    }

    private void init() {
        setContentPane(contentPane);
        setModal(true);

        // 事件总线
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();

        assert loginButton != null;
        loginButton.addActionListener(e -> {
            Boolean auth = checkUserAuth();
            if (Boolean.FALSE.equals(auth)) {
                BalloonUtil.showBalloon("Useremail and Password is required. \nPlease provide them before proceeding.", MessageType.WARNING,userAuthPanel);
                return;
            }
            loginButton.setLoading(true);
            CompletableFuture.runAsync(() -> ShineAIUtil.loginUser(UseremailField.getText(),PasswordField.getText(),userAuthPanel))
                    .thenRun(() -> {
                        loginButton.setLoading(false);
                        if (checkLoginSuccess()) {
                            // 在 1 秒后执行代码
                            Timer timer = new Timer(1500, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    dispose();
                                    ShineAIUtil.isLoginDialogShown = false; // 将状态改回
                                    SwingUtilities.invokeLater(() -> {
                                        // 在 EDT 中执行你的代码
                                        messageBus.syncPublisher(LoginSuccessListener.TOPIC).loginSuccessful();
                                    });
                                    ((Timer)e.getSource()).stop(); //  停止 Timer，只执行一次
                                }
                            });
                            timer.setRepeats(false); //  设置 Timer 只执行一次
                            timer.start(); //  启动 Timer
                        }
                    });
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
            @Override
            public void windowOpened(WindowEvent e) {
                reset();
            }
        });
    }

    public void reset() {
        UseremailField.setText(stateStore.Useremail);
    }

    // 定义一个事件接口
    interface LoginSuccessListener {
        Topic<LoginSuccessListener> TOPIC = Topic.create("Login Success", LoginSuccessListener.class);
        void loginSuccessful();
    }

    private void createUserAuthButton() {
        loginButton = new LoadingButton("login");
        loginPanel.add(loginButton);
    }

    private Boolean checkUserAuth() {
        if (StringUtil.isNotEmpty(UseremailField.getText())) {
            stateStore.Useremail = UseremailField.getText();
        }
        return StringUtil.isNotEmpty(UseremailField.getText()) & StringUtil.isNotEmpty(PasswordField.getText());
    }

    private Boolean checkLoginSuccess() {
        return StringUtil.isNotEmpty(stateStore.UserToken) & !stateStore.getUserInfo().isJsonNull();
    }

    public void openDialog(String withContent) {
        SwingUtilities.invokeLater(() -> { // 在事件调度线程中执行
            LoginDialog dialog = new LoginDialog();
            dialog.setTitle("Login：" + (withContent.isBlank() ? "ShineAI" : withContent));

            dialog.pack(); //  先调用 pack()
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });
    }
}
