/*
 * ShineAI - An IntelliJ IDEA plugin for AI services.
 * Copyright (C) 2025 Shine Zhong
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
