//package com.shine.ai.ui;
//
//import com.shine.ai.settings.AIAssistantSettingsState;
//import com.shine.ai.util.HtmlUtil;
//import javafx.application.Platform;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.concurrent.Worker;
//import javafx.embed.swing.JFXPanel;
//import javafx.scene.Scene;
//import javafx.scene.web.WebEngine;
//import javafx.scene.web.WebView;
//
//import javax.swing.*;
//import java.awt.*;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//public class WebViewRender extends JFXPanel {
//    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();
//
//    private WebView webView;
//    private WebEngine webEngine;
//
//    // 获取 CSS 和 JS 文件的 URL
//    private final URL mainCssUrl = getClass().getResource("/css/main.css");
//    private final URL darculacssUrl = getClass().getResource("/css/darcula.min.css"); // 你的主题CSS路径
//    private final URL highlightJsUrl = getClass().getResource("/js/highlight.min.js"); // 你的主题CSS路径
//
//    private final List<String> codeBlcokList = new ArrayList<>();
//
//    private final int MIN_CONTENT_HEIGHT = 32;
//
//    private final int MAX_CONTENT_HEIGHT = 640;
//
//    /**
//     * 构造函数，初始化 JavaFX WebView 组件。
//     * JavaFX 组件的初始化必须在 JavaFX Application Thread 上进行。
//     */
//    public WebViewRender() {
//        // JFXPanel 的初始化必须在 Swing EDT 上完成
//        // 而 WebView 和 Scene 的初始化必须在 JavaFX Application Thread 上完成
//        Platform.runLater(() -> {
//            setOpaque(false);
//            setLayout(new BorderLayout());
//            setFont(new Font("Microsoft YaHei", Font.PLAIN, stateStore.CHAT_PANEL_FONT_SIZE));
//            setPreferredSize(new Dimension(getWidth(),MIN_CONTENT_HEIGHT));
//
//            webView = new WebView();
//            webEngine = webView.getEngine();
//
//            // 为 WebView 设置一个初始场景
//            Scene scene = new Scene(webView);
//            setScene(scene); // 将 JavaFX Scene 设置到 JFXPanel
//
//            // 监听页面加载完成事件，以执行JavaScript
//            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
//                @Override
//                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
//                    if (newValue == Worker.State.SUCCEEDED) {
//                        // 使用document.documentElement.scrollHeight获取整个文档的滚动高度
//                        Integer sheight = (Integer) webEngine.executeScript("document.body.scrollHeight");
//                        SwingUtilities.invokeLater(() -> {
//                             if (sheight>=0) {
//                                 setPreferredSize(new Dimension(getWidth(),Math.min(MAX_CONTENT_HEIGHT,sheight)));
//                                 revalidate();
//                                 repaint();
//                             }
//                        });
//
//                        // 调用JS函数设置智能滚动
//                        webEngine.executeScript("hljs.highlightAll();");
//
//                        // 滚动到页面底部
//                        webEngine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
//                    }
//                }
//            });
//        });
//    }
//
//    private void loadContent(String content) {
//        // 确保 WebEngine 操作在 JavaFX Application Thread 上执行
//        Platform.runLater(() -> {
//            if (webEngine != null) {
//                // 如果是HTML内容，使用 loadContent
//                // 如果是URL，使用 load
//                if (content.startsWith("http://") || content.startsWith("https://") || content.startsWith("file://")) {
//                    webEngine.load(content);
//                } else {
//                    webEngine.loadContent(content);
//                }
//            } else {
//                // 异常情况：WebEngine尚未初始化，通常不应该发生
//                System.err.println("WebViewRender: WebEngine is not initialized when trying to update content.");
//            }
//        });
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
//            updateText(msgCtx);
//        } else {
//            updateText(content);
//        }
//    }
//
//
//    public void updateText(String content) {
//        String htmlContent = String.format("<div class=\"content-container\">%s</div>", HtmlUtil.md2html(content));
//
//        String head = String.format(
//                "<head>" +
//                        "<link rel=\"stylesheet\" href=\"%s\">" +
//                        "<link rel=\"stylesheet\" href=\"%s\">" + // 引入 highlight.js 的 CSS 主题
//                        "<script src=\"%s\"></script>" +         // 引入 highlight.js 库
//                        "</head>",
//                mainCssUrl.toExternalForm(),
//                darculacssUrl.toExternalForm(),
//                highlightJsUrl.toExternalForm()
//        );
//
//        // 完整 HTML 内容
//        String fullHtml = "<html>" + head + "<body>" + htmlContent + "</body></html>";
//
//        loadContent(fullHtml);
//    }
//
//    // 辅助方法：简单地转义JavaScript字符串，防止注入问题
//    private String escapeJsString(String str) {
//        return str.replace("\\", "\\\\")
//                .replace("`", "\\`") // 转义反引号
//                .replace("\"", "\\\"")
//                .replace("\n", "\\n")
//                .replace("\r", "\\r");
//    }
//}
