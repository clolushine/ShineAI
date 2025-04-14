package com.shine.ai.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.JBUI;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;


public class MessageComponent extends JBPanel<MessageComponent> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageComponent.class);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private RoundPanel messageAreaPanel;

    private final TextPaneComponent textPane = new TextPaneComponent();

    private final MyScrollPane textScrollPane = new MyScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

//    private RSyntaxTextAreaComponent currentRSyntaxTextArea;

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

        setDoubleBuffered(true);
        setOpaque(true);
        setBorder(JBUI.Borders.empty(6));
        setLayout(new BorderLayout(JBUI.scale(8), 0));

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

        JsonArray attachments;
        if (chatItemData.has("attachments") && !chatItemData.get("attachments").isJsonNull()) {
            attachments = chatItemData.get("attachments").getAsJsonArray();
        }else {
            attachments = new JsonArray();
        }

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.setBorder(JBUI.Borders.empty());

        JPanel topPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.LEFT : FlowLayout.RIGHT));

        if (stateStore.enableAvatar && showActions) {
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setOpaque(true);
            new SwingWorker<>() {
                @Override
                protected Image doInBackground() throws Exception {
                    ImageIcon imageIcon;
                    try {
                        Image image = ImgUtils.loadImage(avatar);
                        imageIcon = new ImageIcon(image);
                    } catch (Exception e) {
                        imageIcon = isMe ? new ImageIcon(new URL((AIAssistantIcons.ME_URL))) : new ImageIcon(new URL(AIAssistantIcons.AI_URL));
                    }
                    return ImageLoader.scaleImage(imageIcon.getImage(), 32, 32);
                }
                @Override
                protected void done() {
                    try {
                        Image scaledImage = (Image) get();
                        if (scaledImage != null) { // 检查图片尺寸
                            RoundImage roundImg = new RoundImage(scaledImage);
                            iconPanel.add(roundImg, BorderLayout.NORTH);
                        }
                        SwingUtilities.invokeLater(topPanel::updateUI);
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
        messagePanel.setOpaque(false);
        messagePanel.setMinimumSize(new Dimension(getMinimumSize().width,32));
        messagePanel.setBorder(isMe ? JBUI.Borders.emptyLeft(48) : JBUI.Borders.emptyRight(0));

        Component messageTextarea = isMe ? MessageTextareaComponent(content,attachments) : createMessageAreaComponent(content);
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
//
//    public JPanel RSyntaxTextAreaComponentPanel(Element codeElement) {
//        JPanel area = new JPanel(new BorderLayout());
//
//        currentRSyntaxTextArea = new RSyntaxTextAreaComponent();
//        String codeText = Objects.requireNonNull(codeElement).text(); //提取代码文本
//        String lang = currentRSyntaxTextArea.extractLanguage(codeElement);
//        currentRSyntaxTextArea.setLanguage(currentRSyntaxTextArea.getSyntaxStyle(lang));
//        currentRSyntaxTextArea.setContent(codeText);
//        area.add(currentRSyntaxTextArea,BorderLayout.SOUTH);
//
//        RoundPanel topPanel = new RoundPanel(new BorderLayout());
//        topPanel.setBorder(JBUI.Borders.empty(0,12));
//        SwingUtilities.invokeLater(()-> {
//            topPanel.setBackground(new JBColor(Color.decode("#f1f1e1"),Color.decode("#6f757c")));
//        });
//
//        IconButton copyAction = new IconButton("copy", AllIcons.Actions.Copy);
//        copyAction.addActionListener(e -> {
//            ClipboardUtil.setStr(codeText);
//            BalloonUtil.showBalloon("Copy successfully", MessageType.INFO,area);
//        });
//        topPanel.add(copyAction,BorderLayout.EAST);
//
//        JLabel langLabel = new JLabel(lang);
//        langLabel.setFont(JBUI.Fonts.create(null,14));
//        langLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.GREEN,Color.CYAN)));
//        topPanel.add(langLabel,BorderLayout.WEST);
//
//        area.add(topPanel,BorderLayout.NORTH);
//
//        return area;
//    }

    public MyScrollPane TextPaneAreaComponent (String content) {
        textPane.updateContent(content);

        SwingUtilities.invokeLater(()-> {
            textScrollPane.getHorizontalScrollBar().setValue(0);
        });

        return textScrollPane;
    }

    public RoundPanel MessageTextareaComponent (String content,JsonArray attachments) {
        RoundPanel messagePanel = new RoundPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel,BoxLayout.Y_AXIS));
        messagePanel.add(new MessageTextareaComponent(content,Color.decode("#b4d6ff")));
        for (int i = 0; i < attachments.size(); i++) {
            JsonObject item = attachments.get(i).getAsJsonObject();
            if (item.has("type") && StringUtil.equals(item.get("type").getAsString(),"image")) {
                ImageViewInMessage imageView = new ImageViewInMessage(null,item.get("fileName").getAsString(),256);
                messagePanel.add(Box.createRigidArea(new Dimension(0, 8))); // 上间距
                messagePanel.add(imageView);
                if (i != attachments.size() - 1) messagePanel.add(Box.createRigidArea(new Dimension(0, 8))); // 下间距
            }
        }
        return messagePanel;
    }

    public RoundPanel createMessageAreaComponent(String content) {
        messageAreaPanel = new RoundPanel();
        messageAreaPanel.setLayout(new BoxLayout(messageAreaPanel, BoxLayout.Y_AXIS)); // 设置纵向排列

//        String htmlContent = HtmlUtil.md2html(content);
//        Document doc = Jsoup.parse(htmlContent);
//        Elements codeElements = doc.select("pre > code");
//        StringBuilder currentHtml = new StringBuilder();
//
//        // 提取代码块和其它元素
//        for (Element element : doc.body().children()) {
//            if (element.tagName().equals("pre") && element.child(0).tagName().equals("code")) {
//                // 遇到代码块
//                if (!currentHtml.isEmpty()) {
//                    // 将累积的 HTML 内容添加到列表
//                    messageAreaPanel.add(TextPaneAreaComponent(currentHtml.toString()));
//                    currentHtml.setLength(0); // 清空 StringBuilder
//                }
//                messageAreaPanel.add(Box.createRigidArea(new Dimension(0, 8))); // 上间距
//                messageAreaPanel.add(RSyntaxTextAreaComponentPanel(element.child(0)));
//                messageAreaPanel.add(Box.createRigidArea(new Dimension(0, 8))); // 下间距
//            } else {
//                // 遇到其他元素
//                currentHtml.append(element.outerHtml()); // 将 HTML 内容累积到 StringBuilder
//            }
//        }
//
//        // 处理循环结束后剩余的 HTML 内容
//        if (!currentHtml.isEmpty()) {
//            messageAreaPanel.add(TextPaneAreaComponent(currentHtml.toString()));
//        }

        messageAreaPanel.add(TextPaneAreaComponent(content));

        return messageAreaPanel; // 返回 JTextPane
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
                updateContentToState();
                break;
            case "error":
                if (!textPane.getText().isBlank() && !message.isJsonNull()) {
                    try {
                        updateMessageContent(String.format("```json\n%s\n```", JsonUtil.prettyJson(message)));
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
                updateContentToState();
                break;
            case "message:done":
                if (message.has("content")) {
                    updateMessageContent(message.get("content").getAsString());
                }
                // 处理一下带finishReason的
                updateWithContent(withContent);
                updateStatusContent(1);
                updateContentToState();
            break;
            case "message:err":
                if (message.isJsonObject() && !message.has("content")) {
                    try {
                        updateMessageContent(String.format("```json\n%s\n```",JsonUtil.prettyJson(message)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                updateStatusContent(-2);
                updateContentToState();
                break;
        }
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
            Rectangle visibleRect = getVisibleRect();
            if (visibleRect.height > 64) { // 做个可视滚动，输出内容时不完全限制滚动
                Rectangle bounds = getBounds();
                scrollRectToVisible(bounds);
            }
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
                textPane.updateContent(message);
                scrollToBottom();
                textPane.updateUI();
            } catch (Exception e) {
                LOG.error("ShineAI Exception in processing response: response:{} error: {}", message, e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
