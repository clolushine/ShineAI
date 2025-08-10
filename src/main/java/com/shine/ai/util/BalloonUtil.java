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
