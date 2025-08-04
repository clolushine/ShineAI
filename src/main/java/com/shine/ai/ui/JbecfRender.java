//package com.shine.ai.ui;
//
//import com.intellij.openapi.Disposable;
//import com.intellij.ui.jcef.JBCefBrowser; // 导入 JBCefBrowser
//import com.intellij.ui.AncestorListenerAdapter;
//import com.intellij.util.ResourceUtil;
//import com.shine.ai.settings.AIAssistantSettingsState;
//
//import javax.swing.*;
//import javax.swing.event.AncestorEvent;
//import java.awt.*;
//import java.io.IOException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class JbecfRender extends JPanel implements Disposable {
//
//    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();
//    // 获取 CSS 和 JS 文件的 URL
//    private final URL templateURL = getClass().getResource("/html/index.html");
//
//    private final URL mdJsUrl = getClass().getResource("/html/js/markdown-it.min.js");
//    private final URL highlightJsUrl = getClass().getResource("/html/js/highlight.min.js"); // 你的主题CSS路径
//    private final URL darculacssUrl = getClass().getResource("/html/css/darcula.min.css"); // 你的主题CSS路径
//
//
//    private JBCefBrowser browser;
//
//    private final List<String> codeBlcokList = new ArrayList<>();
//
//    public JbecfRender() {
//        setOpaque(false);
//        setBorder(null);
//        setFont(new Font("Microsoft YaHei", Font.PLAIN, stateStore.CHAT_PANEL_FONT_SIZE));
//
//        // 2. 创建 JBCefBrowser 实例
//        browser = new JBCefBrowser();
//
//        // 3. 打开开发者工具 !!!
//        // browser.openDevtools();
//
//        // 4. 将 JBCefBrowser 的Swing组件添加到当前面板
//        add(browser.getComponent());
//
//        // 5. 重要：在组件（工具窗口）关闭时清理JBCefBrowser资源
//        addAncestorListener(new AncestorListenerAdapter() {
//            @Override
//            public void ancestorRemoved(AncestorEvent event) {
//                if (browser != null) {
//                    browser.dispose(); // 释放浏览器资源
//                    browser = null;
//                }
//            }
//        });
//    }
//
//    /**
//     * 加载并渲染Markdown内容。
//     * @param markdownContent 要渲染的Markdown字符串。
//     */
//    public void loadMarkdownContent(String markdownContent) {
//        try {
//            // 1. 从资源中读取HTML模板
//            if (templateURL == null) {
//                System.err.println("Error: markdown_viewer_template.html not found in resources/frontend/");
//                return;
//            }
//            String htmlTemplate = ResourceUtil.loadText(templateURL.openStream());
//
//            // 读取CSS和JS内容
//            String mdJs = ResourceUtil.loadText(mdJsUrl.openStream());
//            String highlightJs = ResourceUtil.loadText(highlightJsUrl.openStream());
//            String darculaCss = ResourceUtil.loadText(darculacssUrl.openStream());
//
//            // 替换资源引用为内联内容
//            htmlTemplate = htmlTemplate
//                    .replace("<link rel=\"stylesheet\" href=\"css/darcula.min.css\">",
//                            "<style>" + darculaCss + "</style>")
//                    .replace("<script src=\"js/highlight.min.js\"></script>",
//                            "<script>" + highlightJs + "</script>")
//                    .replace("<script src=\"js/markdown-it.min.js\"></script>",
//                            "<script>" + mdJs + "</script>");
//
//            // 2. 将原始Markdown内容注入到HTML模板中
//            String finalHtml = htmlTemplate.replace("<!-- MARKDOWN_PLACEHOLDER -->", markdownContent);
//
//            // 3. 加载组装好的HTML到JBCefBrowser
//            browser.loadHTML(finalHtml);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            // 可以在UI中显示错误信息
//            browser.loadHTML("<html><body><h1>Error Loading Markdown</h1><p>" +
//                    "Could not load or process markdown_viewer_template.html: " + e.getMessage() +
//                    "</p></body></html>");
//        }
//    }
//
//    public void updateContent(String content) {
//        if (content.isBlank()) return;
//        // 修改转换结果的htmlString值 用于正确给界面增加鼠标闪烁的效果
//        // 判断markdown中代码块标识符的数量是否为偶数
//        if (content.split("```").length % 2 != 0) {
//            String msgCtx = content;
//            if (content.toCharArray()[msgCtx.length() - 1] != '\n') {
//                msgCtx += '\n';
//            }
//            loadMarkdownContent(msgCtx);
//        } else {
//            loadMarkdownContent(content);
//        }
//    }
//
//    @Override
//    public void dispose() {
//        if (browser != null) {
//            browser.dispose();
//            browser = null;
//        }
//    }
//}
