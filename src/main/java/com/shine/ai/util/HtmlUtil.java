package com.shine.ai.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.jsoup.nodes.Element;


public class HtmlUtil {
    public static String md2html(String markdown) {
        // 创建 Parser 和 Renderer
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build(); // 可以添加扩展
        // 解析和渲染
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    // 从<pre>或<code>标签中提取编程语言
    public static String extractLanguage(Element codeElement) {
        String language = codeElement.attr("class"); //尝试从class属性获取
        if (language.isEmpty()) {
            language = codeElement.parent().attr("class"); //尝试从父元素<pre>的class属性获取
            if (!language.isEmpty()){
                //如果class属性包含多个class，需要进行处理，例如：
                String[] classes = language.split("\\s+");
                for (String className : classes){
                    if (className.startsWith("language-")){
                        language = className.substring("language-".length());
                        break;
                    }
                }
            }
        }
        if (!language.isEmpty()){
            language = (language.split("language-"))[1].toLowerCase();
        }
        //  处理语言名称，例如将"java"转换为"Java"，或者处理其他可能的情况
        return language; //统一转为小写
    }
}
