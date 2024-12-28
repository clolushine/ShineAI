package com.shine.ai.core.parser;

import com.intellij.openapi.project.Project;
import com.shine.ai.ui.MessageComponent;
import com.shine.ai.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;


public class OfficialParser {

    public static ParseResult parseShineAI(@NotNull Project project, MessageComponent component, String result) {
        ParseResult parseResult = new ParseResult();
        parseResult.source = result;
        parseResult.html = HtmlUtil.md2html(result);
        return parseResult;
    }

    public static class ParseResult {
        private String source;
        private String html;

        public String getSource() {
            return source;
        }

        public String getHtml() {
            return html;
        }
    }

}
