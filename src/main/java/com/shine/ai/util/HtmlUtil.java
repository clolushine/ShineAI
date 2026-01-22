/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
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

import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.ext.tables.TablesExtension; // 导入表格扩展
import com.vladsch.flexmark.ext.emoji.EmojiExtension; // 导入表情
import com.vladsch.flexmark.util.data.MutableDataSet; // 用于配置选项
import com.vladsch.flexmark.util.misc.Extension; // 用于扩展列表

import com.vladsch.flexmark.util.ast.Node;
import org.jsoup.nodes.Element;

import java.util.List;


public class HtmlUtil {

    public static String md2html(String markdown) {
        // 1. 定义要启用的扩展
        List<Extension> extensions = List.of(
                TablesExtension.create(), // 启用表格扩展
                // 如果还需要其他扩展，例如GFM（GitHub Flavored Markdown）的其他特性，可以继续添加
                EmojiExtension.create(),
                TypographicExtension.create() // 示例：启用排版优化，如智能引号

        );

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, extensions);

        // 3. 使用配置选项创建 Parser 和 Renderer
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

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
