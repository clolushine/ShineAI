/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

//package com.shine.ai.ui;

//import com.intellij.util.ui.HtmlPanel;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.util.HtmlUtil;
import com.shine.ai.util.StringUtil;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//public class MessagePanel extends HtmlPanel {
//    private String message = "";
//    private final List<String> codeDataList = new ArrayList<>(); //存储代码块内容
//
//    @Override
//    protected @NotNull @Nls String getBody() {
//        return StringUtil.isEmpty(message) ? "" : message;
//    }
//
//    @Override
//    protected @NotNull Font getBodyFont() {
//        return UIUtil.getLabelFont();
//    }
//
//    private void copyCodeToClipboard(String code) {
//        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
//    }
//
//    public String renderMarkdown(String markdown) {
//        codeDataList.clear(); // 清空代码数据
//
//        // 配置 Markdown 解析器
//        MutableDataSet options = new MutableDataSet();
//        options.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));
//        options.set(Parser.EXTENSIONS, Collections.singletonList(TypographicExtension.create()));
//        options.set(Parser.EXTENSIONS, Collections.singletonList(StrikethroughExtension.create()));
//        // 添加高亮扩展
//
//        Parser parser = Parser.builder(options).build();
//        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
//
//        // 解析 Markdown
//        Node document = parser.parse(markdown);
//        return renderer.render(document);
//
//        // 使用 highlight.js 进行代码高亮，并添加复制按钮
//        // 更新 JEditorPane 的内容
////        return highlight(html);
//    }
//
//    // 使用 highlight.js 进行代码高亮，并添加复制按钮
//    public String highlight(String str) {
//        return str;
//    }
//
//    public void updateMessage(String updateMessage) {
//        this.message = HtmlUtil.md2html(updateMessage);
//        update();
//    }
//}
