package com.shine.ai.ui;

import cn.hutool.core.swing.clipboard.ClipboardUtil;
import com.google.gson.JsonObject;
import com.intellij.notification.impl.ui.NotificationsUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBUI;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.HtmlUtil;
//import com.shine.ai.util.JsUtil;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.apache.commons.text.StringEscapeUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextPaneComponent extends JEditorPane {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private final HTMLEditorKit textPaneKit;

    private final List<Element> codeElementList = new ArrayList<>();

    // 正则表达式来匹配Markdown表格的分割线，用于识别表格
    // 例如：|------|:------:|------:|
    private static final Pattern TABLE_SEP_LINE_PATTERN = Pattern.compile("^\\s*\\|?\\s*(:?-+:?(\\s*\\|\\s*:?-+:?)*)?\\s*\\|?\\s*$");

    // ****** 新增：数学公式的正则表达式 ******
    // 行内公式: $formula$ (不匹配被反斜杠转义的 $)
    private static final Pattern INLINE_MATH_PATTERN = Pattern.compile("(?<!\\\\)\\$(.*?)(?<!\\\\)\\$", Pattern.DOTALL);
    // 块级公式: $$formula$$ (不匹配被反斜杠转义的 $$)
    private static final Pattern BLOCK_MATH_PATTERN = Pattern.compile("(?<!\\\\)\\$\\$(.*?)(?<!\\\\)\\$\\$", Pattern.DOTALL);
    // ****** 数学公式正则表达式结束 ******

    public TextPaneComponent() {
        setEditable(false);
        setOpaque(false);
        setBorder(null);
        setContentType("text/html; charset=UTF-8");
        setFont(new Font("Microsoft YaHei", Font.PLAIN, stateStore.CHAT_PANEL_FONT_SIZE));
        addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String href = e.getDescription();
                if (href.startsWith("code-data-index-")) {
                    String codeIndex = href.substring("code-data-index-".length());
                    Element code = codeElementList.get(Integer.parseInt(codeIndex)).getElementsByTag("code").first();
                    if (code != null) {
                        ClipboardUtil.setStr(code.text());
                        BalloonUtil.showBalloon("Copy successfully", MessageType.INFO, this);
                    }
                } else {
                    // 跳转链接
                    new BrowserHyperlinkListener().hyperlinkUpdate(e);
                }
            }
        });
        NotificationsUtil.configureHtmlEditorKit(this, true);

        textPaneKit = (HTMLEditorKit) getEditorKit();
        StyleSheet styleSheet = textPaneKit.getStyleSheet();

        // styleSheet.loadRules(new InputStreamReader(cssURL.openStream()), this.getClass().getResource("/css/darcula.min.css")); // 使用URL作为ref参数
        styleSheet.addRule("body{padding: 0;margin:0;max-width: 100%}");
        styleSheet.addRule(".content{ padding: 10px; color: #000000; border-radius: 8px;background: #ffffff;}");

        // pre code样式
        styleSheet.addRule(".code-container{color:#888;border-radius: 10px;background: #2b2b2b;}");
        styleSheet.addRule(".code-container .code-copy{padding:6px;background:#0d1117;border-radius: 10px;color:#00bcbc;padding-right:12px;font-size: 11px;}");
        styleSheet.addRule(".code-container .code-copy .copy-btn{background:#9ad5ef;text-align:left;margin-right:10px;text-decoration: none;color: #000000;cursor: pointer;padding:0 6px;border-radius:22px;}");
        styleSheet.addRule(".code-container .code-copy .copy-btn:hover{text-decoration: none;}");
        styleSheet.addRule(".code-container pre:not(.math-block){padding:2px 6px;margin-bottom:4px; white-space: pre-wrap;word-wrap: break-word;}");
        styleSheet.addRule(".code-container code {padding:2px 6px;display: block;line-height: 1.2;}");

        // ****** 新增：表格样式 ******
        styleSheet.addRule("table { " +
                "border-collapse: collapse; " + // 合并边框
                "width: 100%; " + // 宽度占满父容器
                "margin: 1em 0; " + // 上下边距
                "font-size: 0.9em; " + // 字体略小
                "border-radius: 8px; " + // 圆角
                "overflow: hidden; " + // 确保内容不溢出圆角
                "}");
        styleSheet.addRule("th, td { " +
                "padding: 8px 12px; " +
                "text-align: left; " + // 默认左对齐，也可以根据分隔线中的冒号调整
                "border: 1px solid #555; " + // 边框颜色
                "vertical-align: top; " + // 顶部对齐
                "}");
        styleSheet.addRule("th { " +
                "font-weight: bold; " +
                "white-space: nowrap; " + // 防止表头文字换行，可能导致布局问题
                "}");
        // ****** 表格样式结束 ******

        // ****** 新增：公式样式 ******
        styleSheet.addRule("pre.math-formula { margin: 2px 4px; vertical-align: bottom; display: inline; }"); // 行内公式
        styleSheet.addRule("pre.math-block { " +
                "text-align: center; " + // 居中公式
                "margin-top: 0.5em; " +
                "margin-bottom: 0.5em; " +
                "padding: 0; " +        // 移除 <pre> 默认的内边距
                "border: none; " +      // 移除 <pre> 默认的边框
                "background: none; " +   // 移除 <pre> 默认的背景
                "font-family: inherit; " + // 继承body字体，而不是等宽字体
                "white-space: pre; " + // 明确保留 <pre> 的不换行行为（非常重要！）
                "overflow: visible; " + // 确保 <pre> 自身不创建滚动条，而是依赖 JScrollPane
                "}");
        // ****** 公式样式结束 ******

        HTMLDocument doc = (HTMLDocument) getDocument();

        try {
            textPaneKit.insertHTML(doc, doc.getLength(), String.format("<div class=\"content\">%s</div>", " "), 0, 0, null);
        } catch (BadLocationException | IOException e) {
            // 处理异常，例如打印错误信息或显示默认内容
            setText("Error rendering content: " + e.getMessage());
        }

        // StyledDocument document = getStyledDocument();

        // try {
        //    textPaneKit.insertHTML((HTMLDocument) document, document.getLength(), String.format("<div class=\"content\">%s</div>", " "), 0, 0, null);
        // } catch (BadLocationException | IOException e) {
            // 处理异常，例如打印错误信息或显示默认内容
        //    setText("Error rendering content: " + e.getMessage());
        //}
    }

    public HTMLEditorKit getTextPaneKit() {
        return textPaneKit;
    }

    public void updateContent(String content) {
        if (content.isBlank()) return;
        // 修改转换结果的htmlString值 用于正确给界面增加鼠标闪烁的效果
        // 判断markdown中代码块标识符的数量是否为偶数
        if (content.split("```").length % 2 != 0) {
            String msgCtx = content;
            if (content.toCharArray()[msgCtx.length() - 1] != '\n') {
                msgCtx += '\n';
            }
            updateText(msgCtx);
        } else {
            updateText(content);
        }
    }


    public void updateText(String content) {
        String htmlContent = String.format("<div class=\"content\">%s</div>", HtmlUtil.md2html(content));

        Document doc = Jsoup.parse(htmlContent);

        // 解析渲染代码块
        processMarkdownCodes(doc);

        // 解析渲染表格
        processMarkdownTables(doc);

        // 解析渲染数学公式
        processMarkdownFormulas(doc);

        setText(doc.body().html());
    }

    public String getPlainText() {
        try {
            return getDocument().getText(0, getDocument().getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }

    private @NotNull Element getHtmlCode(Element element) {
        // 创建 code-container div
        Element codeContainer = new Element(Tag.valueOf("div"), "");
        codeContainer.addClass("code-container");

        // 创建 code-copy div
        Element codeCopy = new Element(Tag.valueOf("div"), "");
        codeCopy.addClass("code-copy");

        // 创建 copy-btn 链接
        Element copyBtn = new Element(Tag.valueOf("a"), "");
        copyBtn.addClass("copy-btn");
        copyBtn.attr("href", "code-data-index-" + codeElementList.size()); // 设置 href 属性
        copyBtn.attr("title", "copy");
        copyBtn.text("copy");

        // 将 copy-btn 添加到 code-copy
        codeCopy.appendChild(copyBtn);

        String language = HtmlUtil.extractLanguage(element); //HtmlUtil.extractLanguage 需要你自己实现
        codeCopy.attr("lang", language);
        codeCopy.appendText(language); // 添加语言文本

        // 创建 pre 元素
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.addClass("hljs");
        pre.addClass("code-pre");

        // 创建 code 元素
        Element code = new Element(Tag.valueOf("code"), "");
        code.addClass("code");

        code.text(element.text());  // 设置 HTML 内容

        // 将 code 添加到 pre
        pre.appendChild(code);

        // 将 code-copy 和 pre 添加到 code-container
        codeContainer.appendChild(codeCopy);
        codeContainer.appendChild(pre);

        return codeContainer;
    }

    private @NotNull Element updateHtmlCode(Element element, Element targetElement) {
        String language = HtmlUtil.extractLanguage(element);
        if (!language.isEmpty()) {
            Element codeCopy = targetElement.getElementsByClass("code-copy").first();
            if (codeCopy != null) {
                // 添加语言文本
                codeCopy.attr("lang", language);
                codeCopy.childNode(1).replaceWith(new TextNode(language));
            }
        }

        // 创建 code 元素
        Element code = targetElement.getElementsByTag("code").first();

        code.text(element.text());

        return targetElement;
    }

    private void processMarkdownCodes(Document doc) {
        Elements codeElements = doc.select("pre > code");

        // 提取代码块
        for (int i = 0; i < codeElements.size(); i++) {
            Element element = codeElements.get(i);
            // 遇到代码块
            if (i < codeElementList.size()) {
                Element htmlCode = updateHtmlCode(element, codeElementList.get(i));
                element.replaceWith(htmlCode);
            } else {
                Element htmlCode = getHtmlCode(element);
                element.replaceWith(htmlCode);
                codeElementList.add(i, htmlCode);
            }
        }
    }

    private void processMarkdownTables(Document doc) {
        // 查找所有 <p> 标签，它们可能是Markdown表格的宿主
        Elements paragraphs = doc.select("p");

        // 倒序遍历，因为在循环中会替换元素，避免 ConcurrentModificationException 或索引错位
        for (int i = paragraphs.size() - 1; i >= 0; i--) {
            Element p = paragraphs.get(i);
            String pText = p.wholeText().trim(); // 获取段落内的所有文本，包括换行符

            // 分割段落文本为行，并过滤空行
            List<String> lines = Arrays.stream(pText.split("\\R")) // \\R 匹配所有换行符 (CR, LF, CRLF, etc.)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            // 检查是否至少有两行（表头和分隔线）且第二行是有效的分隔线
            if (lines.size() >= 2 && TABLE_SEP_LINE_PATTERN.matcher(lines.get(1)).matches()) {
                // 看起来是Markdown表格，开始转换
                Element tableElement = convertMarkdownTableToHtmlTable(lines);
                if (tableElement != null) {
                    p.replaceWith(tableElement); // 用生成的<table>替换原始的<p>
                }
            }
        }
    }

    /**
     * 将Markdown表格的行数据转换为HTML <table> 元素。
     *
     * @param tableLines 包含Markdown表格每一行的列表 (已经trim过)
     *                   例如：
     *                   [
     *                       "| 语言 | 用途 |",
     *                       "|------|------|",
     *                       "| Java | ... |",
     *                       "| JavaScript | ... |"
     *                   ]
     * @return HTML <table> Element，如果转换失败则返回null
     */
    private Element convertMarkdownTableToHtmlTable(List<String> tableLines) {
        if (tableLines.size() < 2) {
            return null; // 至少需要表头和分隔线
        }

        Element table = new Element(Tag.valueOf("table"), "");
        Element thead = new Element(Tag.valueOf("thead"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");

        // 表头 (第一行)
        String headerLine = tableLines.get(0);
        Element headerRow = new Element(Tag.valueOf("tr"), "");
        // 移除首尾可能的多余'|'，然后按未转义的'|'分割，并trim每个单元格
        // 使用 -1 限制 split 结果，即使有空列也能保留
        Arrays.stream(headerLine.replaceAll("^\\|", "").replaceAll("\\|$", "").split("\\|", -1))
                .map(String::trim)
                .forEach(cellText -> {
                    Element th = new Element(Tag.valueOf("th"), "");
                    th.html(cellText); // 使用html()以保留HtmlUtil.md2html可能生成的<b>等内联HTML
                    headerRow.appendChild(th);
                });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        // 分隔线不渲染 (第二行)
        // String separatorLine = tableLines.get(1); // 这一行用于确定对齐方式，但我们这里简化不处理对齐

        // 表格数据 (从第三行开始)
        for (int i = 2; i < tableLines.size(); i++) {
            String dataLine = tableLines.get(i);
            Element dataRow = new Element(Tag.valueOf("tr"), "");
            // 移除首尾可能的多余'|'，然后按未转义的'|'分割，并trim每个单元格
            Arrays.stream(dataLine.replaceAll("^\\|", "").replaceAll("\\|$", "").split("\\|", -1))
                    .map(String::trim)
                    .forEach(cellText -> {
                        Element td = new Element(Tag.valueOf("td"), "");
                        td.html(cellText); // 使用html()以保留HtmlUtil.md2html可能生成的<b>等内联HTML
                        dataRow.appendChild(td);
                    });
            tbody.appendChild(dataRow);
        }
        table.appendChild(tbody);

        return table;
    }

    /**
     * 【核心修改版】
     * 解析并渲染文档中的Markdown数学公式。
     * 此版本通过遍历文本节点进行替换，比操作整个HTML字符串更稳健。
     * @param doc Jsoup文档对象
     */
    private void processMarkdownFormulas(Document doc) {
        // 选择所有可能包含公式的元素
        Elements elementsToProcess = doc.select("p, h1, h2, h3, h4, h5, h6, li, td, th, blockquote");

        for (Element element : elementsToProcess) {
            // 获取元素下的所有子节点
            List<Node> nodes = new ArrayList<>(element.childNodes());
            for (Node node : nodes) {
                // 我们只处理文本节点
                if (node instanceof TextNode textNode) {
                    String text = textNode.getWholeText();

                    // 优先处理块级公式，因为它可能包含行内公式的定界符
                    String processedText = processFormulasInText(text, BLOCK_MATH_PATTERN, true);

                    // 接着处理行内公式
                    processedText = processFormulasInText(processedText, INLINE_MATH_PATTERN, false);

                    // 如果文本内容发生了变化，就替换节点
                    if (!text.equals(processedText)) {
                        // Jsoup会自动解析HTML片段并替换原始节点
                        textNode.replaceWith(new Element("span").html(processedText));
                    }
                }
            }
        }
    }

    /**
     * 辅助方法：在一段纯文本中查找并替换所有公式。
     *
     * @param text          纯文本内容
     * @param pattern       要使用的正则表达式 (行内或块级)
     * @param isBlock       是否为块级公式
     * @return              替换了公式图片HTML后的字符串
     */
    private String processFormulasInText(String text, Pattern pattern, boolean isBlock) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return text; // 没有匹配项，直接返回
        }

        StringBuilder sb = new StringBuilder();
        // 重置匹配器以从头开始查找
        matcher.reset();
        while (matcher.find()) {
            String latex = matcher.group(1);

            // 关键：由于我们从TextNode获取文本，Jsoup已经为我们处理了HTML实体解码。
            // 例如 `&lt;` 已经是 `<`。所以 unescapeHtml4 不再严格需要，但保留也无妨，作为双重保障。
            latex = preprocessLatexForJLatexMath(latex);

            String htmlImage = renderLatexToHtmlImage(latex, isBlock);

            // 进行替换，并处理好美元符号$的转义
            matcher.appendReplacement(sb, Matcher.quoteReplacement(htmlImage));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 预处理从HTML中提取的LaTeX字符串，使其兼容jlatexmath。
     * 1. 反转义HTML实体（&lt; -> <, &gt; -> >, &amp; -> &）。
     * 2. 转义LaTeX中的特殊字符 '&' 为 '\&' (如果它不是已经被用于对齐)。
     *    注意：这里我们简单地替换所有 '&'，这对于简单公式通常有效，
     *    但如果公式中确实需要对齐符，用户必须在原始Markdown中就写成 '&&' 来表示。
     *    对于大多数常见数学公式，这种简单替换是安全的。
     * @param latex 原始LaTeX字符串
     * @return 预处理后的LaTeX字符串
     */
    private String preprocessLatexForJLatexMath(String latex) {
        // **健壮性改进：首先进行trim，避免因为首尾空格导致的匹配问题**
        latex = latex.trim();

        // 1. 反转义HTML实体。这是最关键的一步，确保 `jlatexmath` 看到的是原始的 `<>&"` 而不是 `&lt;&gt;&amp;&quot;`
        // 示例： `\rho &lt; 0` -> `\rho < 0`
        // 确保你的项目中引入了 org.apache.commons:commons-text 或使用其他HTML实体反转义库。
        latex = StringEscapeUtils.unescapeHtml4(latex);

        // 2. 转义 LaTeX 中的 '&' 字符为 '\&'，除非它已经被转义为 '\&'。
        // 这是为了解决 'Character '&' is only available in array mode !'
        // 注意：这将处理所有非转义的 '&'。如果用户期望真正的对齐符（如在 `array` 环境中），
        // 建议用户遵循 LaTeX 规范使用 `&` 作为对齐符，或者确保它被适当的 `array` 或 `align` 环境包裹。
        // 对于大多数公式外面出现而导致的错误，此替换是安全的。
        // 替换所有非反斜杠开头的 '&'
        latex = latex.replaceAll("(?<!\\\\)&", "\\\\&"); // 替换不是 \\& 的 &

        // 3. 将常见的数学乘法符号 '*' 替换为 LaTeX 中的 '\cdot'。
        // 这是为了解决 'Unknown symbol or command or predefined TeXFormula: '*''。
        // jlatexmath 在数学模式下不默认识别裸露的 '*' 为乘法。
        // 这里采用一个相对安全的替换策略：只替换未被反斜杠转义的 `*`。
        // 注意：如果 `*` 有其他特殊用途（例如在字面字符串中），这种替换可能会改变其含义，但对于常见的数学表达式，这是合理的。
        latex = latex.replaceAll("(?<!\\\\)\\*", "\\\\cdot "); // 替换所有未被转义的`*`为`\cdot`
        // 注意：`\\` 是Java中表示一个反斜杠，所以`\\\\`是两个反斜杠

        // **健壮性改进：处理 # 符号**
        // LaTeX 中的 # 是特殊字符（用于宏定义），在公式中通常需要转义。
        latex = latex.replaceAll("(?<!\\\\)#", "\\\\#"); // 替换不是 \\# 的 #

        // **健壮性改进：处理 % 符号**
        // LaTeX 中的 % 是注释符，在公式中也可能需要转义。
        latex = latex.replaceAll("(?<!\\\\)%","\\\\%");

        return latex;
    }

    /**
     * 将LaTeX公式渲染为Base64编码的PNG图片，并返回嵌入HTML的<img>标签。
     *
     * @param latex    LaTeX公式字符串
     * @param isBlock  是否为块级公式（影响渲染样式和HTML类）
     * @return 包含Base64编码图片数据的HTML <img> 标签
     */
    private String renderLatexToHtmlImage(String latex, boolean isBlock) {
        try {
            if (latex == null || latex.trim().isEmpty()) {
                return "";
            }

            // JLaTeXMath 支持两种模式：inline 和 display
            // display模式适用于块级公式，通常会使公式更大，并在垂直方向上居中
            TeXFormula formula = new TeXFormula(isBlock ? "\\displaystyle " + latex : latex);

            // 根据 isBlock 参数选择样式
            int style = isBlock ? TeXConstants.STYLE_DISPLAY : TeXConstants.STYLE_TEXT;

            float baseFontSize = stateStore.CHAT_PANEL_FONT_SIZE;
            // 1.2f 是一个相对缩放因子，你可以根据需要调整
            float renderSize = baseFontSize * 1.4f;

            // 使用正确的 style 参数
            TeXIcon icon = formula.createTeXIcon(style,renderSize,2,new JBColor(Gray.x80, Gray.x8C)); // 公式颜色

            // 确保渲染的颜色匹配主题（如果需要的话，这里简单设为黑色前景，透明背景）
            icon.setInsets(JBUI.insets(2)); // 在公式内容周围增加内边距
            // icon.setBackground(new Color(0,0,0,0)); // 透明背景，但Better to render on white and then use JEditorPane's background

            int width = icon.getIconWidth();
            int height = icon.getIconHeight();

            // 创建BufferedImage来绘制公式，使用ARGB类型支持透明度
            BufferedImage image = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();

            // 设置高质量渲染提示
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // 填充背景（如果希望透明则不需要）
            g2.setColor(new JBColor(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0))); // 完全透明
            g2.fillRect(0, 0, width, height);

            // 绘制公式
            icon.paintIcon(this, g2, 0, 0); // 'this' 作为 ImageObserver
            g2.dispose(); // 释放Graphics资源

            // 将BufferedImage转换为PNG并通过Base64编码
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // 构建HTML<img>标签
            String className = isBlock ? "math-block" : "math-formula";
            // 使用 StringEscapeUtils.escapeHtml4 确保alt文本安全

            // 注意：JEditorPane对CSS的支持有限，特别是对display:block的<img>
            // 块级公式最好放在一个<div>中，并对div应用样式
            if (isBlock) {
                // <pre> 标签会强制其内部内容不换行，从而让 JEditorPane 正确计算宽度
                return String.format("<pre>" + "<div class=\"%s\"><img src=\"data:image/png;base64,%s\" alt=\"%s\" /></div>" + "</pre>",
                        className, base64Image, latex);
            } else {
                // 行内公式保持不变
                return String.format("<pre>" + "<span class=\"%s\"><img src=\"data:image/png;base64,%s\" alt=\"%s\" /></span>" + "</pre>",
                        className, base64Image, latex);
            }


        } catch (Exception e) {
            System.err.println("Error rendering LaTeX: " + latex + " Exception: " + e.getMessage());
            // 渲染失败时，返回带有错误信息的span，并转义原始LaTeX，避免HTML注入
            return "<span style=\"color: red; background-color: #ffeaea; padding: 2px 4px; border-radius: 3px;\">[Math Error: " + latex + "]</span>";
        }
    }



    public void highlightsAll(List<JsonObject> matches, int selectedGlobalMatchIndex) {
        Highlighter highlighter = getHighlighter();
        highlighter.removeAllHighlights();

        Color defaultHighlightColor = new JBColor(new Color(255, 255, 0, 180), new Color(255, 255, 0, 180)); // 明亮黄
        Color selectedBackgroundColor = new JBColor(new Color(80, 180, 255, 255), new Color(80, 180, 255, 255)); // 明显蓝
        Color selectedBorderColor = new JBColor(new Color(255, 69, 0), new Color(255, 69, 0)); // 橙红

        Highlighter.HighlightPainter defaultPainter = new DefaultHighlighter.DefaultHighlightPainter(defaultHighlightColor);
        Highlighter.HighlightPainter selectedPainter = new BorderHighlightPainter(selectedBackgroundColor, selectedBorderColor);

        for (JsonObject match : matches) {
            int start = match.get("matchStartIndex").getAsInt();
            int end = match.get("matchEndIndex").getAsInt();
            int globalIndex = match.get("globalMatchIndex").getAsInt();

            Highlighter.HighlightPainter painterToApply = (globalIndex == selectedGlobalMatchIndex)
                    ? selectedPainter : defaultPainter;

            try {
                highlighter.addHighlight(start, end, painterToApply);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearHighlights() {
        Highlighter highlighter = getHighlighter();
        highlighter.removeAllHighlights();
    }

    public void scrollToLine(JComponent component,JBScrollPane parentScrollPane,JBScrollPane ownScrollPane,int startIndex) {
        try {
            Rectangle2D rect2d = modelToView2D(startIndex);
            if (rect2d != null) {
                Rectangle rect = rect2d.getBounds();

                // 计算 component 在 parentScrollPane 视图中的偏移
                JViewport parentViewport = parentScrollPane.getViewport();
                Point compOriginInParent = SwingUtilities.convertPoint(component.getParent(), component.getLocation(), parentViewport.getView());

                // rect 在 component 内部，所以要加上 component 在视图中的偏移
                int rectXInParent = compOriginInParent.x + rect.x;
                int rectYInParent = compOriginInParent.y + rect.y;

                int parentViewportWidth = parentViewport.getWidth();
                int parentViewportHeight = parentViewport.getHeight();

                // 居中目标高亮区域rect（横向和纵向都以parentScrollPane的视口为准）
                int newX = rectXInParent + rect.width / 2 - parentViewportWidth / 2;
                int newY = rectYInParent + rect.height / 2 - parentViewportHeight / 2;

                // 边界检查
                Component parentView = parentViewport.getView();
                if (parentView != null) {
                    int maxX = parentView.getWidth() - parentViewportWidth;
                    int maxY = parentView.getHeight() - parentViewportHeight;
                    newX = Math.max(0, Math.min(newX, Math.max(0, maxX)));
                    newY = Math.max(0, Math.min(newY, Math.max(0, maxY)));
                }

                // 设置 parentScrollPane 的视口位置（横竖都居中）
                parentViewport.setViewPosition(new Point(newX, newY));

                // 横向滚动（ownScrollPane），用 parentScrollPane 的视口宽度来居中
                JViewport ownViewport = ownScrollPane.getViewport();
                Component ownView = ownViewport.getView();

                int ownViewportY = ownViewport.getViewPosition().y; // 保持Y不变
                int ownViewWidth = (ownView != null) ? ownView.getWidth() : parentViewportWidth;

                int ownNewX = rectXInParent + rect.width / 2 - parentViewportWidth / 2;
                int ownMaxX = ownViewWidth - parentViewportWidth;
                ownNewX = Math.max(0, Math.min(ownNewX, Math.max(0, ownMaxX)));

                ownViewport.setViewPosition(new Point(ownNewX, ownViewportY));

            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // 自定义高亮器，绘制背景色和边框
    private static class BorderHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        private final Color borderColor;

        public BorderHighlightPainter(Color backgroundColor, Color borderColor) {
            super(backgroundColor);
            this.borderColor = borderColor;
        }

        @Override
        public Shape paintLayer(Graphics g, int p0, int p1, Shape viewBounds, JTextComponent editor, View view) {
            // 1. 先调用父类方法绘制背景
            Shape s = super.paintLayer(g, p0, p1, viewBounds, editor, view);

            // 2. 检查返回的 Shape 是否为空，这是问题的核心所在！
            if (s != null) {
                // 3. 然后绘制边框
                Rectangle r = s.getBounds();
                g.setColor(borderColor);
                // 绘制矩形边框，宽度和高度减1是为了避免绘制在区域外
                g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
            }
            return s;
        }
    }
}
