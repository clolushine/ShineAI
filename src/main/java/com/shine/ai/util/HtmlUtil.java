package com.shine.ai.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;


public class HtmlUtil {
    public static String md2html(String markdown) {
        // 创建 Parser 和 Renderer
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build(); // 可以添加扩展
        // 解析和渲染
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
