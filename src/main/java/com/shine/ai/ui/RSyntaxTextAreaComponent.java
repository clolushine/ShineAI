//package com.obiscr.chatgpt.ui;
//import com.intellij.util.ui.JBUI;
//import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
//import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
//import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
//import org.fife.ui.rtextarea.RTextScrollPane;
//
//import java.util.Set;
//
//public class RSyntaxTextAreaComponent {
//    public RSyntaxTextArea textArea;
//
//    public RSyntaxTextAreaComponent() {
//        // 创建 RSyntaxTextArea
//        textArea = new RSyntaxTextArea();
//        textArea.setEditable(false);
//        textArea.setLineWrap(true);
//        textArea.setWrapStyleWord(true);
//        // 在构造函数中进行一些初始化设置，例如字体、样式等
////        setFont(new Font("Monospaced", Font.PLAIN, 12)); // 设置等宽字体
//        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // 设置语言高亮样式,可根据需要更改
//        textArea.setCodeFoldingEnabled(true); // 启用代码折叠（可选）
//    }
//
//    public RSyntaxTextArea createComponent() {
//        // 将 RSyntaxTextArea 添加到 RTextScrollPane
////        return new RTextScrollPane(textArea);
//        return textArea;
//    }
//
//    // 从内容中提取代码块的语言
//    public void printSupportedLanguages() {
//        Set<String> supportedLanguages = TokenMakerFactory.getDefaultInstance().keySet();
//        System.out.println("Supported Languages:");
//        for (String language : supportedLanguages) {
//            System.out.println("- " + language);
//        }
//    }
//}
