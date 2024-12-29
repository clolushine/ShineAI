package com.shine.ai.ui;

import com.google.gson.JsonObject;
import com.intellij.notification.impl.ui.NotificationsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.JBUI;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.HtmlUtil;
import com.shine.ai.util.StringUtil;
import com.shine.ai.util.TimeUtil;
//import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
//import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MessageComponent extends JBPanel<MessageComponent> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageComponent.class);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

//    private final MessagePanel component = new MessagePanel();

    private final URL cssResource;

    private JTextPane textPane;

//    private final RSyntaxTextAreaComponent RSyntaxPanel = new RSyntaxTextAreaComponent();

    public MessageActionsComponent messageActions;

    public JPanel actionPanel;

    public JLabel timeLabel;

    public JLabel statusLabel;

    public JLabel withContentLabel;

    private final Project project;

    public JsonObject chatItemData;

    public String chatId;

    public MessageGroupComponent msgGroupComp;

    public Boolean showActions = true;

    public MessageComponent(Project project,JsonObject chatItem,MessageGroupComponent msgGroupComp) {
        this.project = project;

        this.msgGroupComp = msgGroupComp;

        this.chatItemData = chatItem;

        this.cssResource = getClass().getResource("/css/darcula.min.css");

        setDoubleBuffered(true);
        setOpaque(true);
        setBorder(JBUI.Borders.empty(6));
        setLayout(new BorderLayout(JBUI.scale(7), 0));

        initComponent();
    }

    public void initComponent() {
        if (chatItemData.has("promptId")) {
            chatId = chatItemData.get("promptId").getAsString();
            showActions = false;
        }else {
            chatId = chatItemData.get("chatId").getAsString();
        }

        chatItemData.addProperty("chatId",chatId);

        String content = chatItemData.get("content").getAsString();
        boolean isMe = chatItemData.get("isMe").getAsBoolean();
        String name = chatItemData.get("name").getAsString();
        long time = chatItemData.get("time").getAsLong();
        String avatar = chatItemData.get("icon").getAsString();
        int status = chatItemData.get("status").getAsInt();
        String withContent = chatItemData.get("withContent").getAsString();

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.setBorder(JBUI.Borders.empty());

        JPanel topPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.LEFT : FlowLayout.RIGHT));

        if (stateStore.enableAvatar && showActions) {
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setOpaque(true);
//            Image imageIcon;
//            try {
//                InputStream inputStream = getClass().getResourceAsStream(avatar); // 注意开头的斜杠
//                if (inputStream != null) {
//                    imageIcon = new ImageIcon(ImageIO.read(inputStream)).getImage();
//                } else {
//                    imageIcon = isMe ? ImgUtils.iconToImage(AIAssistantIcons.ME) : ImgUtils.iconToImage(AIAssistantIcons.AI);
//                }
//            } catch (IOException e) {
//                imageIcon = isMe ? ImgUtils.iconToImage(AIAssistantIcons.ME) : ImgUtils.iconToImage(AIAssistantIcons.AI);
//            }
//            try {
//                imageIcon = ImgUtils.getImage(new URL(avatar));
//            } catch (Exception e) {
//                imageIcon = isMe ? ImgUtils.iconToImage(AIAssistantIcons.ME) : ImgUtils.iconToImage(AIAssistantIcons.AI);
//            }
//            Image scale = ImageLoader.scaleImage(imageIcon,32, 32);
//            RoundImage roundImg = new RoundImage(scale);
//            iconPanel.add(roundImg, BorderLayout.NORTH);

            new SwingWorker<>() {
                @Override
                protected Image doInBackground() throws Exception {
                    ImageIcon imageIcon;
                    try {
                        imageIcon = new ImageIcon(new URL(avatar));
                    } catch (Exception e) {
                        imageIcon = isMe ? new ImageIcon(new URL(AIAssistantIcons.ME_URL)) : new ImageIcon(new URL(AIAssistantIcons.AI_URL));
                    }
//                    // 确保图片加载完成 (如果需要)
//                    if (imageIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
//                        return null;
//                    }
                    return ImageLoader.scaleImage(imageIcon.getImage(), 32, 32);  // 在后台线程缩放图片
                }
                @Override
                protected void done() {
                    try {
                        Image scaledImage = (Image) get();
                        if (scaledImage != null) { // 检查图片尺寸
                            RoundImage roundImg = new RoundImage(scaledImage);
                            iconPanel.add(roundImg, BorderLayout.NORTH);
                            SwingUtilities.invokeLater(() -> {  // 在 EDT 中更新 UI
                                topPanel.repaint();
                                topPanel.updateUI();
                            });
                        }
                    } catch (Exception e) {
                        // ... 处理错误
                        System.out.println(e.getMessage());
                    }
                }
            }.execute();

            topPanel.add(iconPanel);
        }

        JLabel nameLabel = new JLabel();
        nameLabel.setBorder(JBUI.Borders.empty(0,6));
        nameLabel.setFont(JBUI.Fonts.create(null,14));
        nameLabel.setText(name);
        nameLabel.setVerticalAlignment(JLabel.BOTTOM);
        topPanel.add(nameLabel);

        actionPanel = new JPanel(new CardLayout());
        // 将 actions 添加到顶部（MainPanel 边框外部）

        if (showActions)  initActions();

        topPanel.add(actionPanel);

        List<Component> componentsToAdd = new ArrayList<>();
        for (int i = 0; i < topPanel.getComponentCount(); i++) {
            componentsToAdd.add(topPanel.getComponent(i));
        }

        if (isMe) Collections.reverse(componentsToAdd); // 只在 isMe 为 true 时反转

        topPanel.removeAll();

        for (Component component : componentsToAdd) {
            topPanel.add(component);
        }

        northPanel.add(topPanel,isMe ? BorderLayout.EAST : BorderLayout.WEST);
        add(northPanel,BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        RoundPanel messagePanel = new RoundPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(true);
        messagePanel.setMinimumSize(new Dimension(getMinimumSize().width,32));
        messagePanel.setBorder(JBUI.Borders.empty(6));

        SwingUtilities.invokeLater(() -> {
            messagePanel.setBackground(isMe ? new JBColor(Color.decode("#a5d6ff"),Color.decode("#b4d6ff")) : new JBColor(Color.decode("#f1f1e1"), Color.decode("#f1f1f1") /*2d2f30*/));
        });

        Component messageTextarea = isMe ? new messageTextarea(content) : createTextPaneComponent(content);
        messagePanel.add(messageTextarea,BorderLayout.CENTER);

        centerPanel.add(messagePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(JBUI.Borders.empty(2));

        JPanel southContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeLabel = new JLabel();
        timeLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Gray.x80, Gray.x8C)));
        timeLabel.setFont(JBUI.Fonts.create(null,10));
        timeLabel.setVisible(time != 0 && status != 0 && status != 2);
        timeLabel.setText(TimeUtil.timeFormat(time,null));
        southContentPanel.add(timeLabel);

        statusLabel = new JLabel();
        statusLabel.setFont(JBUI.Fonts.create(null,10));
        updateStatusContent(status);
        southContentPanel.add(statusLabel);

        withContentLabel = new JLabel();
        withContentLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26"))));
        withContentLabel.setFont(JBUI.Fonts.create(null,10));
        updateWithContent(withContent);
        southContentPanel.add(withContentLabel);

        southContentPanel.setComponentZOrder(timeLabel,isMe ? 2 : 0);

        southPanel.add(southContentPanel,isMe ? BorderLayout.EAST : BorderLayout.WEST);

        add(southPanel,BorderLayout.SOUTH); // 将 MainPanel 添加到中心
    }

//    public Component createContentComponent(String content) {
//        component.setEditable(false);
//        component.setContentType("text/html; charset=UTF-8");
//        component.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, java.lang.Boolean.TRUE);
//        component.setOpaque(false);
//        component.setBorder(null);
//        component.addHyperlinkListener(new BrowserHyperlinkListener());
//
//        // 配置字体和样式
//        HTMLEditorKit kit = (HTMLEditorKit) component.getEditorKit();
//        StyleSheet styleSheet = kit.getStyleSheet();
//        styleSheet.importStyleSheet(cssResource);
//        component.setEditorKit(kit);
//
//        NotificationsUtil.configureHtmlEditorKit(component, true);
//        component.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, StringUtil.unescapeXmlEntities(StringUtil.stripHtml(content, " ")));
//
//        component.updateMessage(content);
//
//        component.setEditable(false);
//
//        if (component.getCaret() != null) {
//            component.setCaretPosition(0);
//        }
//
//        component.revalidate();
//        component.repaint();
//
//        return component;
//    }

    public JTextPane createTextPaneComponent(String content) {
        textPane = new JTextPane(); // 使用 JTextPane
        textPane.setContentType("text/html; charset=UTF-8");
        textPane.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#ffffff"), Color.decode("#000000"))));
        textPane.setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
        textPane.setMinimumSize(new Dimension(textPane.getPreferredSize().width,32));
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBorder(null);
        textPane.addHyperlinkListener(new BrowserHyperlinkListener());

        HTMLEditorKit kit = new HTMLEditorKit();
//        StyleSheet styleSheet = kit.getStyleSheet();
//        styleSheet.importStyleSheet(cssResource);
        textPane.setEditorKit(kit);

//        String htmlContent = String.format("<body style=\"font-size: %spx; padding: 6px 12px; background-color: #f0f0f0; border-radius: 10px;\">%s</body>",
//                stateStore.CHAT_PANEL_FONT_SIZE, HtmlUtil.md2html(content));

        NotificationsUtil.configureHtmlEditorKit(textPane, true);
        textPane.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, StringUtil.unescapeXmlEntities(StringUtil.stripHtml(content, " ")));

        StyledDocument doc = textPane.getStyledDocument();

        try {
            kit.insertHTML((HTMLDocument) doc, doc.getLength(), HtmlUtil.md2html(content), 0, 0, null);
        } catch (BadLocationException | IOException e) {
            // 处理异常，例如打印错误信息或显示默认内容
            textPane.setText("Error rendering content: " + e.getMessage());
        }

        textPane.setCaretPosition(0);  // 设置光标位置

        return textPane; // 返回 JTextPane
    }
//
//    public RSyntaxTextArea RSyntaxComponent(String content) {
//        RSyntaxPanel.textArea.setFont(JBUI.Fonts.create(Font.SANS_SERIF,stateStore.CHAT_PANEL_FONT_SIZE));
//        RSyntaxPanel.textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, java.lang.Boolean.TRUE);
//        RSyntaxPanel.textArea.setOpaque(false);
//        RSyntaxPanel.textArea.setBorder(null);
//        RSyntaxPanel.textArea.addHyperlinkListener(new BrowserHyperlinkListener());
//        RSyntaxPanel.textArea.setAutoIndentEnabled(true);
//
//        RSyntaxPanel.textArea.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, StringUtil.unescapeXmlEntities(StringUtil.stripHtml(content, " ")));
//
//        RSyntaxPanel.textArea.setText(content);
//
//        RSyntaxPanel.textArea.setEditable(false);
//
//        if (RSyntaxPanel.textArea.getCaret() != null) {
//            RSyntaxPanel.textArea.setCaretPosition(0);
//        }
//
//        RSyntaxPanel.textArea.revalidate();
//        RSyntaxPanel.textArea.repaint();
//
//        return RSyntaxPanel.createComponent();
//    }

    public class messageTextarea extends JTextArea {
        public messageTextarea(String content) {
            setEditable(false);
            setOpaque(false);
            setBorder(null);
            setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#f1f1f1"), Color.decode("#000000"))));
            setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
            setLineWrap(true);
            setWrapStyleWord(true);
            setText(content);
        }
    }

    public void updateContent(JsonObject newData) {
        this.chatItemData = newData;

        removeAll();

        initComponent();

        revalidate(); // 如果大小发生变化
        repaint();
    }

    public void updateActions(JsonObject newData) {
        this.chatItemData = newData;

        actionPanel.removeAll();

        initActions();

        actionPanel.revalidate(); // 如果大小发生变化
        actionPanel.repaint();
    }

    public void updateStatusContent(int status) {
        chatItemData.addProperty("status",status);
        timeLabel.setVisible(chatItemData.get("time").getAsLong() != 0 && status != 0 && status != 2);
        statusLabel.setVisible(chatItemData.get("time").getAsLong() != 0 && !chatItemData.get("isMe").getAsBoolean());
        Color colorBlue = Color.decode("#4db2dd");
        Color colorRed = Color.decode("#dd524d");
        Color setColor = status < 0 ? colorRed : colorBlue;
        statusLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(setColor,setColor)));
        switch (status) {
            case 0:
                statusLabel.setText("requesting...");
                break;
            case 1:
                statusLabel.setText("request completed");
                break;
            case 2:
                statusLabel.setText("generating...");
                break;
            case -1:
                statusLabel.setText("generate error");
                break;
            case -2:
                statusLabel.setText("request error");
                break;
            case -3:
                statusLabel.setText("aborted");
                break;
        }
    }

    public void updateWithContent(String withContent) {
        chatItemData.addProperty("withContent",withContent);
        withContentLabel.setVisible(!withContent.isBlank());
        withContentLabel.setText(withContent);
    }

    public void updateMessageContent(String content) {
        setContent(content);
        chatItemData.addProperty("content",content);
    }

    public void initActions() {
        JPanel actionDirPanel = new JPanel(new BorderLayout());
        messageActions = new MessageActionsComponent(project,chatItemData,this);
        actionDirPanel.add(messageActions,chatItemData.get("isMe").getAsBoolean() ? BorderLayout.WEST : BorderLayout.EAST);
        actionPanel.add(actionDirPanel,"messageActions");

        // 创建一个空的 JPanel 作为占位符
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false); // 可选：设置为透明
        actionPanel.add(placeholder,"placeholder");

        // 初始时显示占位符
        CardLayout cl = (CardLayout)(actionPanel.getLayout());
        cl.show(actionPanel, "messageActions");
    }

    public void updateMessage(JsonObject dck) {
        // dck, 有ai回复格式，也有自定义格式，ai格式为正常格式，自定义格式主要是为error出错设定的，
        String event = dck.get("event").getAsString();
        JsonObject message = new JsonObject();
        String withContent = "";
        if (dck.has("message")) {
            message = dck.get("message").getAsJsonObject();
        }
        if (dck.has("finishReason")) {
            if (!dck.get("finishReason").isJsonNull()) {
                withContent = String.format("finishReason: %s", dck.get("finishReason").getAsString());
            }
        }
        switch (event) {
            case "message":
                if (message.has("content")) {
                    updateMessageContent(chatItemData.get("content").getAsString() + message.get("content").getAsString());
                }
                // 处理一下带finishReason的
                updateWithContent(withContent);
                updateStatusContent(2);
            break;
            case "done":
                updateStatusContent(1);
                break;
            case "error":
                if (!textPane.getText().isBlank() && !message.isJsonNull()) {
                    try {
                        updateMessageContent(message.asMap().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    updateStatusContent(-2);
                } else {
                    updateStatusContent(-1);
                }
                updateContentToState();
                break;
            case "abort":
                updateStatusContent(-3);
                break;
            case "message:done":
                if (message.has("content")) {
                    updateMessageContent(message.get("content").getAsString());
                }
                // 处理一下带finishReason的
                updateWithContent(withContent);
                updateStatusContent(1);
            break;
            case "message:err":
                if (message.isJsonObject() && !message.has("content")) {
                    try {
                        updateMessageContent(message.asMap().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                updateStatusContent(-2);
                break;
        }
        updateContentToState();
    }

    public void updateContentToState() {
        if (!msgGroupComp.isNull()) {
            msgGroupComp.updateMessageState(chatId,chatItemData);
        }
    }

    public void setContent(String content) {
        new MessageWorker(content).execute();
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            Rectangle bounds = getBounds();
            scrollRectToVisible(bounds);
        });
    }

    class MessageWorker extends SwingWorker<Void, String> {
        private final String message;

        public MessageWorker(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground() throws Exception {
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
//                component.updateMessage(message);
//                scrollToBottom();
//                component.updateUI();
                textPane.setText(HtmlUtil.md2html(message));
                scrollToBottom();
                textPane.updateUI();
            } catch (Exception e) {
                LOG.error("ShineAI Exception in processing response: response:{} error: {}", message, e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
