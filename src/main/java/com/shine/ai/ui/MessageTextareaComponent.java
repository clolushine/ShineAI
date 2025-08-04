package com.shine.ai.ui;

import com.google.gson.JsonObject;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.shine.ai.settings.AIAssistantSettingsState;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class MessageTextareaComponent extends JPanel {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private final Color background;

    private final JTextArea textArea;

    public MessageTextareaComponent(String content,Color background) {
        this.background = background;

        setDoubleBuffered(true);
        setOpaque(true);
        setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
        textArea.setForeground(new JBColor(Color.decode("#060606"), Color.decode("#000000")));
        textArea.setBorder(JBUI.Borders.empty(6,12));

        Document document = textArea.getDocument();

        textArea.insert(content,document.getLength());

        add(textArea);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 先绘制默认背景
        super.paintComponent(g2);

        g2.setColor(this.background); // 使用面板的背景颜色

        // 绘制圆角矩形
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));

        g2.dispose();
    }

    public void highlightsAll(List<JsonObject> matches, int selectedGlobalMatchIndex) {
        Highlighter highlighter = textArea.getHighlighter();
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
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
    }

    public void scrollToLine(JComponent component,JBScrollPane parent, int startIndex) {
        try {
            int displayLine = textArea.getLineOfOffset(startIndex); // 真实显示行号
            int offset = textArea.getLineStartOffset(displayLine);
            Rectangle rect = (Rectangle) textArea.modelToView2D(offset);
            if (rect != null) {
                JViewport viewport = parent.getViewport();;
                int viewHeight = viewport.getExtentSize().height;
                int targetY = rect.y - (viewHeight - rect.height) / 2;
                scrollRectToVisible(new Rectangle(0, targetY, 1, viewHeight));
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

    public JTextArea getTextArea() {
        return textArea;
    }
}
