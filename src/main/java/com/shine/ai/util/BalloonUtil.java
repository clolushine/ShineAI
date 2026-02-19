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

package com.shine.ai.util;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BalloonUtil {
    public static <Component> void showBalloon(String text, int fadeoutTime, MessageType type, @NotNull Component component) {
        Balloon balloon = JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type.getDefaultIcon(), type.getPopupBackground(), null)
                .setFadeoutTime(fadeoutTime)
                .createBalloon();
        balloon.show(RelativePoint.getCenterOf((JComponent) component), Balloon.Position.above);
        // 销毁balloon
        Timer timer = new Timer(fadeoutTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(balloon::dispose);
                ((Timer)e.getSource()).stop(); //  停止 Timer，只执行一次
            }
        });
        timer.setRepeats(false); //  设置 Timer 只执行一次
        timer.start(); //  启动 Timer
    }

    // 重载方法，使用默认淡出时间 (例如 3000 毫秒)
    public static void showBalloon(String text, MessageType type,Component component) {
        showBalloon(text, 3000, type,component);
    }

    public static void showBalloon(Component component,String text){
        showBalloon(text,MessageType.INFO,component);
    }

}
