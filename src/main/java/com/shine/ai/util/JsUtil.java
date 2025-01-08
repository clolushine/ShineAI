//package com.shine.ai.util;
//
//import org.graalvm.polyglot.Context;
//import org.graalvm.polyglot.Source;
//import org.graalvm.polyglot.Value;
//
//import java.io.IOException;
//import java.net.URL;
//
//public class JsUtil {
//    private static Context context; //  静态 Context 对象
//    private static Value highlighter; //  静态 highlight 函数
//
//    static { // 静态代码块，在类加载时执行一次
//        try {
//            importHighlightJs();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static void importHighlightJs() throws IOException {
//        context = Context.create();
//        // 从资源文件加载 highlight.js
//        URL highlightJsUrl = JsUtil.class.getResource("/js/highlight.min.js");
//
//        Source source = Source.newBuilder("js", highlightJsUrl).build(); // 创建 Source 对象
//
//        context.eval(source); // 执行 JavaScript 代码
//
//        // 定义高亮函数
//        String highlightFunction = """
//                (function highlight(code) {
//                     hljs.configure({   // optionally configure highlight.js
//                          ignoreUnescapedHTML: true
//                     });
//                     return hljs.highlightAuto(code).value;
//                })
//                """;
//        highlighter = context.eval("js", highlightFunction);
//    }
//
//    public static String highlight(String code) {
//        String highlightedCode = highlighter.execute(code).asString();
//        if (!highlightedCode.isEmpty()) return highlightedCode;
//        return "";
//    }
//}
