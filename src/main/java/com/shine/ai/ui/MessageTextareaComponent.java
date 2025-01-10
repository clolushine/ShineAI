package com.shine.ai.ui;

import com.intellij.notification.impl.ui.NotificationsUtil;
import com.intellij.ui.JBColor;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.HtmlUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;

public class MessageTextareaComponent extends JEditorPane {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private final HTMLEditorKit textPaneKit;

    public MessageTextareaComponent(String content) {
        setEditable(false);
        setOpaque(false);
        setContentType("text/html; charset=UTF-8");
        setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
        setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#f1f1f1"), Color.decode("#000000"))));
        setEditable(false);
        setOpaque(false);
        setBorder(null);
        NotificationsUtil.configureHtmlEditorKit(this, true);

        textPaneKit = (HTMLEditorKit) getEditorKit();
        StyleSheet styleSheet = textPaneKit.getStyleSheet();

        styleSheet.addRule("body{ padding: 0;margin:0;}");
        String htmlContent = String.format("<div class=\"content\">%s</div>", HtmlUtil.md2html(content));
        styleSheet.addRule(String.format(".content{ padding: 6px 10px; color: #000000; background: %s; border-radius: 12px;}","#b4d6ff"));

        Document document = getDocument();

        try {
            textPaneKit.insertHTML((HTMLDocument) document, document.getLength(), htmlContent, 0, 0, null);
        } catch (BadLocationException | IOException e) {
            // 处理异常，例如打印错误信息或显示默认内容
            setText("Error rendering content: " + e.getMessage());
        }
    }

    public HTMLEditorKit getTextPaneKit() {
        return textPaneKit;
    }
}
