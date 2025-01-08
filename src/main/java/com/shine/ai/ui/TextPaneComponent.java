package com.shine.ai.ui;

import cn.hutool.core.swing.clipboard.ClipboardUtil;
import com.intellij.notification.impl.ui.NotificationsUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.BrowserHyperlinkListener;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.HtmlUtil;
//import com.shine.ai.util.JsUtil;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TextPaneComponent extends JTextPane {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private HTMLEditorKit textPaneKit;

    private List<String> codeList = new ArrayList<>();

    public TextPaneComponent() {
        setContentType("text/html; charset=UTF-8");
        setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
        setEditable(false);
        setOpaque(false);
        setBorder(null);
        addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String href = e.getDescription();
                if (href.startsWith("code-data-index-")) {
                    String codeIndex = href.substring("code-data-index-".length());
                    ClipboardUtil.setStr(codeList.get(Integer.parseInt(codeIndex)));
                    BalloonUtil.showBalloon("Copy successfully", MessageType.INFO,this);
                } else {
                    // 跳转链接
                    new BrowserHyperlinkListener().hyperlinkUpdate(e);
                }
            }
        });
        NotificationsUtil.configureHtmlEditorKit(this, true);

        textPaneKit = (HTMLEditorKit) getEditorKit();
        StyleSheet styleSheet = textPaneKit.getStyleSheet();
        styleSheet.importStyleSheet(this.getClass().getResource("/css/darcula.min.css")); // 引入darcula代码样式
//        styleSheet.importStyleSheet(this.getClass().getResource("/css/github-dark.min.css")); // 引入darcula代码样式

//        styleSheet.loadRules(new InputStreamReader(cssURL.openStream()), this.getClass().getResource("/css/darcula.min.css")); // 使用URL作为ref参数
        styleSheet.addRule("body{padding: 0;margin:0;}");
        styleSheet.addRule(".content{ padding:6px 10px 10px 10px; color: #000000; border-radius: 12px;background: #ffffff;}");

        // pre code样式
        styleSheet.addRule(".code-container{color: #888;border-radius: 10px;background: #2b2b2b;}");
        styleSheet.addRule(".code-container .code-copy{font-size: 10px; color:#00bcbc;text-align:right;padding:6px;background:#0d1117;border-radius: 10px;}");
        styleSheet.addRule(".code-container a{text-decoration: none;color: #f1f1f1;cursor: pointer;padding:0 10px;}");
        styleSheet.addRule(".code-container a:hover{text-decoration: none;}");
        styleSheet.addRule(".code-container pre{padding:2px 8px;margin-bottom:4px;}");
        styleSheet.addRule(".code-container code{padding:2px 8px;}");

        StyledDocument document = getStyledDocument();

        try {
            textPaneKit.insertHTML((HTMLDocument) document, document.getLength(), String.format("<div class=\"content\">%s</div>", " "), 0, 0, null);
        } catch (BadLocationException | IOException e) {
            // 处理异常，例如打印错误信息或显示默认内容
            setText("Error rendering content: " + e.getMessage());
        }
    }

    public HTMLEditorKit getTextPaneKit() {
        return textPaneKit;
    }

    public void updateContent(String content){
        if (content.isBlank()) return;
        // 修改转换结果的htmlString值 用于正确给界面增加鼠标闪烁的效果
        // 判断markdown中代码块标识符的数量是否为偶数
        if (content.split("```").length % 2 != 0) {
            String msgCtx = content;
            if (content.toCharArray()[msgCtx.length() - 1] != '\n') {
                msgCtx += '\n';
            }
            updateText(msgCtx);
        }else {
            updateText(content);
        }
    }


    public void updateText(String content) {
        String htmlContent = String.format("<div class=\"content\">%s</div>", HtmlUtil.md2html(content));

        Document doc = Jsoup.parse(htmlContent);
        Elements codeElements = doc.select("pre > code");

        // 提取代码块
        for (Element element : codeElements) {
            // 遇到代码块
            codeList.add(element.text());
//            String html = highlight ? JsUtil.highlight(element.text()) : element.text();
            String html = element.text();
            Element htmlCode = getHtmlCode(html,element);
            element.replaceWith(htmlCode);
        }

        setText(doc.body().html());

        setCaretPosition(getDocument().getLength());
    }

//    private @NotNull String getHtmlCode(String html, Element element) {
//        String htmlCode = "<div class=\"code-container\">";
//        htmlCode += "<div class=\"code-copy\">";
//        htmlCode += String.format("%s<a class=\"copy-btn\" href=\"code-data-index-%s\" title=\"copy\"> copy </a>",HtmlUtil.extractLanguage(element),codeList.size() - 1);
//        htmlCode += "</div>";
//        htmlCode += String.format("<pre class=\"hljs code-pre\"><code>%s</code></pre>", html);
//        htmlCode += "</div>";
//        return htmlCode;
//    }

    private @NotNull Element getHtmlCode(String codeText, Element element) {
        // 创建 code-container div
        Element codeContainer = new Element(Tag.valueOf("div"), "");
        codeContainer.addClass("code-container");

        // 创建 code-copy div
        Element codeCopy = new Element(Tag.valueOf("div"), "");
        codeCopy.addClass("code-copy");

        String language = HtmlUtil.extractLanguage(element); //HtmlUtil.extractLanguage 需要你自己实现
        codeCopy.appendText(language); // 添加语言文本

        // 创建 copy-btn 链接
        Element copyBtn = new Element(Tag.valueOf("a"), "");
        copyBtn.addClass("copy-btn");
        copyBtn.attr("href", "code-data-index-" + (codeList.size() - 1)); // 设置 href 属性
        copyBtn.attr("title", "copy");
        copyBtn.text("copy");

        // 将 copy-btn 添加到 code-copy
        codeCopy.appendChild(copyBtn);

        // 创建 pre 元素
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.addClass("hljs");
        pre.addClass("code-pre");

        // 创建 code 元素
        Element code = new Element(Tag.valueOf("code"), "");
        code.addClass("code");
        switch (language) {
            case "html", "xml":
                code.text(codeText);  // 设置 HTML 内容
                break;
            default:
                code.html(codeText);  // 设置 HTML 内容
                break;
        }

        // 将 code 添加到 pre
        pre.appendChild(code);

        // 将 code-copy 和 pre 添加到 code-container
        codeContainer.appendChild(codeCopy);
        codeContainer.appendChild(pre);

        return codeContainer;
    }
}
