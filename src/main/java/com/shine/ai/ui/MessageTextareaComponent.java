package com.shine.ai.ui;

import com.intellij.notification.impl.ui.NotificationsUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.HtmlUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

public class MessageTextareaComponent extends JPanel {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private final Color background;

//    private final HTMLEditorKit textPaneKit;

//    public MessageTextareaComponent(String content) {
//        setEditable(false);
//        setOpaque(false);
//        setContentType("text/html; charset=UTF-8");
//        setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
//        setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#f1f1f1"), Color.decode("#000000"))));
//        setBorder(null);
//        NotificationsUtil.configureHtmlEditorKit(this, true);
//
//        textPaneKit = (HTMLEditorKit) getEditorKit();
//        StyleSheet styleSheet = textPaneKit.getStyleSheet();
//
//        styleSheet.addRule("body{ padding: 0;margin:0;}");
//        String htmlContent = String.format("<div class=\"content\">%s</div>", HtmlUtil.md2html(content));
//        styleSheet.addRule(String.format(".content{ padding: 6px 10px; color: #000000; background: %s; border-radius: 12px;}","#b4d6ff"));
//
//        Document document = getDocument();
//
//        try {
//            textPaneKit.insertHTML((HTMLDocument) document, document.getLength(), htmlContent, 0, 0, null);
//        } catch (BadLocationException | IOException e) {
//            // 处理异常，例如打印错误信息或显示默认内容
//            setText("Error rendering content: " + e.getMessage());
//        }
//    }
//
//    public HTMLEditorKit getTextPaneKit() {
//        return textPaneKit;
//    }

    public MessageTextareaComponent(String content,Color background) {
        this.background = background;

        setDoubleBuffered(true);
        setOpaque(true);
        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
        textArea.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#060606"), Color.decode("#000000"))));
        textArea.setBorder(JBUI.Borders.empty(6,12));

        Document document = textArea.getDocument();

        textArea.insert(content,document.getLength());

        add(textArea);

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 先绘制默认背景
        super.paintComponent(g2);

        g2.setColor(this.background); // 使用面板的背景颜色

        // 绘制圆角矩形
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));

        g2.dispose();
    }
}
