package com.shine.ai.util;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
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
        // 销毁balloon
        Timer timer = new Timer(fadeoutTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                balloon.dispose();
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
