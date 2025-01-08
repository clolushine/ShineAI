//package com.shine.ai.ui;
//
//import com.intellij.util.ui.JBUI;
//import com.shine.ai.settings.AIAssistantSettingsState;
//import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
//import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
//
//import java.awt.*;
//import java.util.Set;
//public class RSyntaxTextAreaComponent extends RSyntaxTextArea {
//
//    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();
//
//    private final Set<String> supportedLanguages = TokenMakerFactory.getDefaultInstance().keySet();
//
//    public RSyntaxTextAreaComponent() {
//        // 创建 RSyntaxTextArea
//        setEditable(false);
//        setLineWrap(true);
//        setWrapStyleWord(true);
//        setHighlightCurrentLine(false); // 去掉光标行高亮
//        setBorder(JBUI.Borders.empty(6));
//        // 在构造函数中进行一些初始化设置，例如字体、样式等
//        setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
//        setCodeFoldingEnabled(true); // 启用代码折叠（可选）
//    }
//
//    public void setLanguage(String language) {
//        setSyntaxEditingStyle(language); // 设置语言高亮样式,可根据需要更改
//    }
//
//    public void setContent(String content) {
//        setText(content);
//        setCaretPosition(getDocument().getLength());
//    }
//}
