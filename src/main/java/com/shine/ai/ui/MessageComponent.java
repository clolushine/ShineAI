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

package com.shine.ai.ui;

import cn.hutool.core.swing.clipboard.ClipboardUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.IconLoader;
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
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;


public class MessageComponent extends JBPanel<MessageComponent> implements MessageActionsComponent.MessageActionCallback, ImageViewInMessage.ImageActionCallback {

    private static final Logger LOG = LoggerFactory.getLogger(MessageComponent.class);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private RoundPanel messageAreaPanel;

    private TextPaneComponent textPane;

    private MessageTextareaComponent textArea;

    private MyScrollPane textScrollPane;

//    private final JbecfRender jbecfRenderPane = new JbecfRender();
//
//    private final MyScrollPane textScrollPane = new MyScrollPane(jbecfRenderPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

//    private RSyntaxTextAreaComponent currentRSyntaxTextArea;

    public MessageActionsComponent messageActions;

    public JPanel llmInfoPanel;

    public JPanel messageCyPanel;

    public JPanel actionPanel;

    public JLabel timeLabel;

    public JLabel statusLabel;

    public JLabel withContentLabel;

    private final Project project;

    public JsonObject chatItemData;

    public String chatId;

    public Boolean showActions = true;

    private MessageActionCallback messageActionCallback; // 持有接口引用

    private MyScrollPane globalScrollPane; // 全局scrollPane引用

    @Override
    public void ontShowBalloon(String msg, MessageType type) {
        BalloonUtil.showBalloon(msg,type,this);
    }

    @Override
    public void onPreviewImage(String imageName) {
        messageActionCallback.onPreviewImage(imageName,getImageList(null));
    }

    @Override
    public void onAddImageToEdit(JsonObject fileData) {
        messageActionCallback.onAddImageToEdit(fileData);
    }

    public interface MessageActionCallback {
        void onUpdateMessageState(String chatId, JsonObject itemData);
        void onSetProgressBar(boolean isShow);
        void onPreviewImage(String imageName,JsonArray imageList);
        void onAddImageToEdit(JsonObject fileData);
    }

    // 父组件通过此方法设置回调
    public void setActionCallback(MessageActionCallback callback) {
        this.messageActionCallback = callback;
    }

    // 或者通过setter方法设置
    public void setGlobalScrollPane(MyScrollPane globalScrollPane) {
        this.globalScrollPane = globalScrollPane;
    }

    public MessageComponent(Project project,JsonObject chatItem) {
        this.project = project;

        this.chatItemData = chatItem;

        setDoubleBuffered(true);
        setOpaque(true);
        setBorder(JBUI.Borders.empty(6));
        setLayout(new BorderLayout(JBUI.scale(8), 0));

        initComponent();
    }

    public void initComponent() {
        if (chatItemData == null) return;

        if (chatItemData.has("promptId")) {
            chatId = chatItemData.get("promptId").getAsString();
            showActions = false;
        }else {
            chatId = chatItemData.get("id").getAsString();
        }

        chatItemData.addProperty("id",chatId);

        String content = chatItemData.get("content").getAsString();
        boolean isMe = StringUtil.equals(chatItemData.get("role").getAsString(),"user");
        String name = chatItemData.get("name").getAsString();
        long time = chatItemData.get("updateAt").getAsLong();
        String avatar = chatItemData.get("icon").getAsString();
        int status = chatItemData.get("status").getAsInt();
        String withContent = chatItemData.get("finishReason").getAsString();

        // 用于显示更详细的信息
        String fromLLM = chatItemData.get("fromLLM").getAsString();
        boolean webSearch = chatItemData.get("webSearch").getAsBoolean();

        JsonArray attachments;
        if (chatItemData.has("attachments") && !chatItemData.get("attachments").isJsonNull()) {
            attachments = chatItemData.get("attachments").getAsJsonArray();
        }else {
            attachments = new JsonArray();
        }

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.setBorder(JBUI.Borders.emptyBottom(JBUI.scale(6)));

        JPanel topPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.LEFT : FlowLayout.RIGHT));

        if (stateStore.enableAvatar && showActions) {
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setOpaque(true);
            SwingUtilities.invokeLater(()-> {
                ImageIcon imageIcon = null;
                try {
                    Image image = ImgUtils.loadImage(avatar);
                    imageIcon = new ImageIcon(image);
                } catch (Exception e) {
                    try {
                        imageIcon = isMe ? new ImageIcon(new URL((AIAssistantIcons.ME_URL))) : new ImageIcon(new URL(AIAssistantIcons.AI_URL));
                    } catch (MalformedURLException ex) {
                        // ... 处理错误
                        System.out.println(e.getMessage());
                    }
                }
                Image scaledImage = ImageLoader.scaleImage(imageIcon.getImage(), 32, 32);;
                // 检查图片尺寸
                RoundImage roundImg = new RoundImage(scaledImage);
                iconPanel.add(roundImg, BorderLayout.NORTH);
            });

            topPanel.add(iconPanel);
        }

        JLabel nameLabel = new JLabel();
        nameLabel.setBorder(JBUI.Borders.empty(0,6));
        nameLabel.setFont(JBUI.Fonts.create(null,stateStore.CHAT_PANEL_FONT_SIZE));
        nameLabel.setText(name);
        nameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        topPanel.add(nameLabel);

        List<Component> componentsToAdd = new ArrayList<>();
        for (int i = 0; i < topPanel.getComponentCount(); i++) {
            componentsToAdd.add(topPanel.getComponent(i));
        }

        if (isMe) Collections.reverse(componentsToAdd); // 只在 isMe 为 true 时反转

        topPanel.removeAll();

        for (Component component : componentsToAdd) {
            topPanel.add(component);
        }

        timeLabel = new JLabel();
        timeLabel.setForeground(new JBColor(Gray.x80, Gray.x8C));
        timeLabel.setFont(JBUI.Fonts.create(null,10));
        timeLabel.setVisible(time != 0 && status != 0 && status != 2);
        timeLabel.setText(TimeUtil.timeFormat(time,null));
        // 这里需要和下面的对话框一样设置
        timeLabel.setBorder(JBUI.Borders.emptyLeft(isMe ? JBUI.scale(64): 0));
        timeLabel.setVerticalAlignment(SwingConstants.BOTTOM);

        northPanel.add(timeLabel,isMe ? BorderLayout.WEST : BorderLayout.EAST);
        northPanel.add(topPanel,isMe ? BorderLayout.EAST : BorderLayout.WEST);
        add(northPanel,BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

//        messageCyPanel = new JPanel(new CardLayout());
//        // 将 messagePanel 添加控制显示
//        centerPanel.add(messageCyPanel,BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        messagePanel.setMinimumSize(new Dimension(64,32));
        messagePanel.setBorder(isMe ? JBUI.Borders.emptyLeft(JBUI.scale(64)) : JBUI.Borders.emptyRight(0));
        Component messageTextarea = isMe ? MessageTextareaComponent(content,attachments) : createMessageAreaComponent(content);
        messagePanel.add(messageTextarea,BorderLayout.CENTER);
//        // 添加到控制显示
//        messageCyPanel.add(messagePanel,"messagePanel");
//
//        // 创建一个空的 JPanel 作为占位符
//        JPanel messagePanelPlaceholder = new JPanel();
//        messagePanelPlaceholder.setOpaque(false); // 设置为透明
//        messageCyPanel.add(messagePanelPlaceholder,"messagePanelPlaceholder");
//
//        // 初始时显示占位符
//        CardLayout cl = (CardLayout)(messageCyPanel.getLayout());
//        cl.show(messageCyPanel, "messagePanelPlaceholder");

        centerPanel.add(messagePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(JBUI.Borders.empty(2));

        JPanel southContentPanel = new JPanel();
        southContentPanel.setLayout(new BoxLayout(southContentPanel, BoxLayout.Y_AXIS));

        if (stateStore.enableMsgPanelAIInfo) {
            llmInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            southContentPanel.add(llmInfoPanel);

            JLabel fromLLMLabel = new JLabel();
            fromLLMLabel.setForeground(new JBColor(Gray.x80, Gray.x8C));
            fromLLMLabel.setFont(JBUI.Fonts.create(null,12));
            fromLLMLabel.setIcon(IconLoader.getIcon("/icons/ai_label.svg", MessageComponent.class));
            fromLLMLabel.setText(fromLLM);
            fromLLMLabel.setAlignmentX(SwingConstants.CENTER);
            // 使图标和文本作为一个整体水平居中 ------
            fromLLMLabel.setHorizontalAlignment(SwingConstants.CENTER);
            // 调整图标和文本之间的间距（可选）
            fromLLMLabel.setIconTextGap(JBUI.scale(6));

            fromLLMLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!fromLLM.isBlank()) {
                        ClipboardUtil.setStr(fromLLM);
                        BalloonUtil.showBalloon("Copy successfully", MessageType.INFO,fromLLMLabel);
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    // 当鼠标进入组件区域时，设置光标为手型
                    fromLLMLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    // 当鼠标移出组件区域时，恢复光标为原来的值
                    fromLLMLabel.setCursor(Cursor.getDefaultCursor());
                }
            });

            llmInfoPanel.add(fromLLMLabel);

            JLabel webSearchLabel = new JLabel();
            webSearchLabel.setIcon(IconLoader.getIcon("/icons/web.svg", MessageComponent.class));
            webSearchLabel.setFont(JBUI.Fonts.create(null,12));
            webSearchLabel.setBorder(isMe ? JBUI.Borders.emptyLeft(6) : JBUI.Borders.emptyRight(6));
            webSearchLabel.setVisible(webSearch);
            llmInfoPanel.add(webSearchLabel);
            llmInfoPanel.setVisible(time != 0 && status != 0 && status != 2 && !isMe && !fromLLM.isBlank());
            llmInfoPanel.setComponentZOrder(webSearchLabel,isMe ? 1 : 0);
        }

        JPanel southContentSouthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southContentPanel.add(southContentSouthPanel);

        statusLabel = new JLabel();
        statusLabel.setFont(JBUI.Fonts.create(null,10));
        updateStatusContent(status);
        southContentSouthPanel.add(statusLabel);

        withContentLabel = new JLabel();
        withContentLabel.setForeground(new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26")));
        withContentLabel.setFont(JBUI.Fonts.create(null,10));
        updateWithContent(withContent);
        southContentSouthPanel.add(withContentLabel);

        actionPanel = new JPanel(new CardLayout());
        actionPanel.setOpaque(false);
        // 将 actions 添加到顶部（MainPanel 边框外部）

        if (showActions)  initActions();

        southPanel.add(actionPanel,BorderLayout.EAST);
        if (!isMe) southPanel.add(southContentPanel,BorderLayout.WEST);

        add(southPanel,BorderLayout.SOUTH); // 将 MainPanel 添加到中心

        // 是否显示操作栏
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // 当鼠标进入组件区域时
                CardLayout cl = (CardLayout)(actionPanel.getLayout());
                cl.show(actionPanel, "messageActions");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // 获取鼠标离开事件发生时鼠标在屏幕上的真实全局位置
                Point mousePoint = e.getPoint();
                Rectangle sourceRect = getVisibleRect();
                // 说明鼠标只是从父组件的“背景”区域移入了某个子组件（比如操作按钮本身），
                // 此时不应执行隐藏操作。
                // 鼠标仍在某个子组件上方，不隐藏
                if (!sourceRect.contains(mousePoint)) {
                    // 当鼠标移出组件区域时
                    CardLayout cl = (CardLayout)(actionPanel.getLayout());
                    cl.show(actionPanel, "placeholder");

                }
            }
        });
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
//        langLabel.setForeground( new JBColor(Color.GREEN,Color.CYAN));
//        topPanel.add(langLabel,BorderLayout.WEST);
//
//        area.add(topPanel,BorderLayout.NORTH);
//
//        return area;
//    }

    public MyScrollPane TextPaneAreaComponent (String content) {
        textPane = new TextPaneComponent();

        textScrollPane = new MyScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        textPane.updateText(content);

        SwingUtilities.invokeLater(()-> textScrollPane.getHorizontalScrollBar().setValue(0));

        return textScrollPane;
    }

//    public MyScrollPane TextPaneAreaComponent (String content) {
//        jbecfRenderPane.updateContent(content);
//
//        SwingUtilities.invokeLater(()-> {
//            textScrollPane.getHorizontalScrollBar().setValue(0);
//        });
//
//        return textScrollPane;
//    }

    public RoundPanel MessageTextareaComponent (String content,JsonArray attachments) {
        RoundPanel messagePanel = new RoundPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel,BoxLayout.Y_AXIS));
        textArea = new MessageTextareaComponent(content,true);
        messagePanel.add(textArea);

        // 筛出图片文件
        JsonArray imageAttachments = getImageList(attachments);
        for (int i = 0; i < imageAttachments.size(); i++) {
            JsonObject imageItem = imageAttachments.get(i).getAsJsonObject();
            ImageViewInMessage imageView = new ImageViewInMessage(null,imageItem);
            imageView.setActionCallback(this);
            messagePanel.add(Box.createRigidArea(new Dimension(0, 8))); // 上间距
            messagePanel.add(imageView);
            if (i != attachments.size() - 1) messagePanel.add(Box.createRigidArea(new Dimension(0, 8))); // 下间距
        }

        // TODO 渲染其他的文件

        return messagePanel;
    }

    public RoundPanel createMessageAreaComponent(String content) {
        messageAreaPanel = new RoundPanel();
        messageAreaPanel.setLayout(new BoxLayout(messageAreaPanel, BoxLayout.Y_AXIS)); // 设置纵向排列

        messageAreaPanel.add(TextPaneAreaComponent(content));

        return messageAreaPanel; // 返回 JTextPane
    }

    public void updateActions(JsonObject newData) {
        chatItemData = newData;

        messageActions.setUpdate(chatItemData);

        actionPanel.revalidate(); // 如果大小发生变化
        actionPanel.repaint();
    }

    public void updateStatusContent(int status) {
        chatItemData.addProperty("status",status);

        long time = chatItemData.get("updateAt").getAsLong();
        boolean isMe = StringUtil.equals(chatItemData.get("role").getAsString(),"user");
        String fromLLM = chatItemData.get("fromLLM").getAsString();
        long refreshTimestamp = GeneratorUtil.getTimestamp();

        Color colorBlue = Color.decode("#4db2dd");
        Color colorRed = Color.decode("#dd524d");

        Color setStatusLabelColor = status < 0 ? colorRed : colorBlue;
        boolean showProgressBar = status == 2;

        SwingUtilities.invokeLater(() -> {
            // 更新时间
            timeLabel.setText(TimeUtil.timeFormat(refreshTimestamp,null));
            chatItemData.addProperty("updateAt",refreshTimestamp);

            if (llmInfoPanel != null) {
                llmInfoPanel.setVisible(time != 0 && status != 0 && status != 2 && !isMe && !fromLLM.isBlank());
            }

            timeLabel.setVisible(time != 0 && status != 0 && status != 2);
            statusLabel.setVisible(time != 0 && !isMe);

            statusLabel.setForeground(new JBColor(setStatusLabelColor,setStatusLabelColor));

            // 通知父组件
            messageActionCallback.onSetProgressBar(showProgressBar);

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
            // 滚动到底部
            scrollToBottom();
        });
    }

    public void updateWithContent(String withContent) {
        chatItemData.addProperty("finishReason",withContent);

        SwingUtilities.invokeLater(() -> {
            withContentLabel.setVisible(!withContent.isBlank());
            withContentLabel.setText(withContent);
        });
    }

    public void updateMessageContent(String content) {
        var fullContent = chatItemData.get("content").getAsString() + content;
        chatItemData.addProperty("content",fullContent);
        setContent(fullContent);
    }

    public void initActions() {
        JPanel actionDirPanel = new JPanel(new BorderLayout());

        messageActions = new MessageActionsComponent(project,chatItemData);
        messageActions.setActionCallback(this); // 设置回调对象引用

        actionDirPanel.add(messageActions,StringUtil.equals(chatItemData.get("role").getAsString(), "user") ? BorderLayout.WEST : BorderLayout.EAST);
        actionPanel.add(actionDirPanel,"messageActions");

        // 创建一个空的 JPanel 作为占位符
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false); // 可选：设置为透明
        actionPanel.add(placeholder,"placeholder");

        // 初始时显示占位符
        CardLayout cl = (CardLayout)(actionPanel.getLayout());
        cl.show(actionPanel, "placeholder");
    }

    public void updateMessage(JsonObject dck) {
        // dck, 有ai回复格式，也有自定义格式，ai格式为正常格式，自定义格式主要是为error出错设定的，
        String event = dck.get("event").getAsString();
        JsonObject message = new JsonObject();
        String withContent = "";

        // 消息状态
        int status = chatItemData.get("status").getAsInt();

        if (dck.has("message")) {
            message = dck.get("message").getAsJsonObject();
        }
        if (dck.has("finishReason")) {
            if (!dck.get("finishReason").isJsonNull() && !StringUtil.equals(dck.get("finishReason").getAsString(),"")) {
                withContent = String.format("finishReason: %s", dck.get("finishReason").getAsString());
            }
        }
        switch (event) {
            case "message":
                status = 2;
                if (message.has("content")) {
                    updateMessageContent(message.get("content").getAsString());
                }
                // 处理一下带finishReason的
                updateWithContent(withContent);
            break;
            case "done":
                status = 1;
                break;
            case "error":
                if (chatItemData.get("content").getAsString().isBlank() && !message.isJsonNull()) {
                    try {
                        updateMessageContent(String.format("```json\n%s\n```", JsonUtil.prettyJson(message)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    status = -2;
                } else {
                    status = -1;
                }
                break;
            case "abort":
                status = -3;
                break;
            case "message:done":
                status = 1;
                if (message.has("content")) {
                    updateMessageContent(message.get("content").getAsString());
                }
                // 处理一下带finishReason的
                updateWithContent(withContent);
            break;
            case "message:err":
                status = -2;
                if (message.isJsonObject() && !message.has("content")) {
                    try {
                        updateMessageContent(String.format("```json\n%s\n```",JsonUtil.prettyJson(message)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
        }

        updateStatusContent(status);

        if (status != 2) {
            updateContentToState();
        }
    }

    public void updateContentToState() {
        messageActionCallback.onUpdateMessageState(chatId,chatItemData);
    }

    public void scrollToBottom() {
        Rectangle visibleRect = getVisibleRect();
        if (visibleRect.height > 64) { // 做个可视滚动，输出内容时不完全限制滚动
            Rectangle bounds = getBounds();
            scrollRectToVisible(bounds);
        }
    }

    public void changeTheme(boolean isBright) {
        SwingUtilities.invokeLater(() -> {
            if (textPane != null) {
                textPane.changeTextPaneTheme(isBright);
                textPane.updateText(chatItemData.get("content").getAsString());
                textScrollPane.getHorizontalScrollBar().setValue(0);
            }
        });
    }

    public void setHighlightsAll(List<JsonObject> matches, int selectedGlobalMatchIndex) {
        // 直接在主线程调用，防抖逻辑交给textPane
        SwingUtilities.invokeLater(() -> {
            if (textPane != null) {
                textPane.highlightsAll(matches,selectedGlobalMatchIndex);
            }else if (textArea != null) {
                textArea.highlightsAll(matches,selectedGlobalMatchIndex);
            }
        });
    }

    public void setScrollToHighlight(MessageComponent targetComponent,int startIndex,int endIndex) {
        // 直接在主线程调用，防抖逻辑交给textPane
        SwingUtilities.invokeLater(() -> {
            if (textArea != null) {
                textArea.scrollToLine(targetComponent,globalScrollPane,startIndex);
            }else if (textPane != null) {
                textPane.scrollToLine(targetComponent,globalScrollPane,textScrollPane,startIndex);
            }
        });
    }

    public void clearHighlighter() {
        // 直接在主线程调用，防抖逻辑交给textPane
        SwingUtilities.invokeLater(() -> {
            if (textPane != null) {
                textPane.clearHighlights();
            }else if (textArea != null) {
                textArea.clearHighlights();
            }
        });
    }

    public void setContent(String content) {
        if (content.isBlank()) return;
        // 直接在主线程调用，防抖逻辑交给textPane
        SwingUtilities.invokeLater(() -> {
            textPane.updateText(content);
            textScrollPane.getHorizontalScrollBar().setValue(0);
            scrollToBottom();
        });
    }

    public String getContent() {
        String content = "";
        if (textPane != null) {
            content = textPane.getPlainText();
        }else if (textArea != null) {
            content = textArea.getTextArea().getText();
        }
        return content;
    }

    public JsonArray getImageList(JsonArray attachments) {
        JsonArray attachs = attachments;
        if (attachs == null) {
            if (chatItemData.has("attachments") && !chatItemData.get("attachments").isJsonNull()) {
                attachs = chatItemData.get("attachments").getAsJsonArray();
            }else {
                attachs = new JsonArray();
            }
        }
        // 筛出图片文件
        JsonArray imageAttachments = new JsonArray();
        for(JsonElement attachment: attachs) {
            if (attachment.isJsonObject()) {
                JsonObject imageItem = attachment.getAsJsonObject();
                if (imageItem.has("type") && "image".equals(imageItem.get("type").getAsString())) {
                    imageAttachments.add(imageItem);
                }
            }
        }
        return imageAttachments;
    }

    /**
     * 清理方法
     */
    public void cleanup() {
        // 1. 清空对外部类（如父控制器）的回调引用
        setActionCallback(null);
        setGlobalScrollPane(null);

        // 2. 清理 MessageActionsComponent
        if (messageActions != null) {
            messageActions.setActionCallback(null);
            messageActions.cleanup();
            messageActions = null; // 清空对 MessageActionsComponent 的引用
        }

        for (MouseListener m : getMouseListeners()) {
            removeMouseListener(m);
        }

        if (textPane != null) {
            textPane.clearHighlights(); // 清理文本高亮
        }
        if (textArea != null) {
            textArea.clearHighlights();
        }

        removeAll();
    }
}
