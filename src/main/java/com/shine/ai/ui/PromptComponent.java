package com.shine.ai.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.shine.ai.settings.AIAssistantSettingsState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromptComponent extends JBPanel<PromptComponent> {
    private static final Logger LOG = LoggerFactory.getLogger(PromptComponent.class);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    public JLabel withContentLabel;

    private final Project project;

    public JsonObject chatItemData;

    public String chatId;

    public MessageGroupComponent msgGroupComp;

    public Boolean showActions = true;

    public PromptComponent(Project project,JsonObject chatItem,MessageGroupComponent msgGroupComp) {
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
        String withContent = chatItemData.get("withContent").getAsString();

        JsonArray attachments = null;
//        if (chatItemData.has("attachments") && !chatItemData.get("attachments").isJsonNull()) {
//            attachments = chatItemData.get("attachments").getAsJsonArray();
//        }else {
//            attachments = new JsonArray();
//        }

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.setBorder(JBUI.Borders.empty());

        JPanel topPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.LEFT : FlowLayout.RIGHT));

        JLabel nameLabel = new JLabel();
        nameLabel.setBorder(JBUI.Borders.empty(0,6));
        nameLabel.setFont(JBUI.Fonts.create(null,14));
        nameLabel.setText(name);
        nameLabel.setVerticalAlignment(JLabel.BOTTOM);
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

        northPanel.add(topPanel,isMe ? BorderLayout.EAST : BorderLayout.WEST);
        add(northPanel,BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        RoundPanel messagePanel = new RoundPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(true);
        messagePanel.setMinimumSize(new Dimension(getMinimumSize().width,32));
        messagePanel.setBorder(JBUI.Borders.empty(2));

        JComponent messageTextarea = MessageTextareaComponent(content,isMe,attachments);
        messagePanel.add(messageTextarea,BorderLayout.CENTER);

        centerPanel.add(messagePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(JBUI.Borders.empty(2));

        JPanel southContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        withContentLabel = new JLabel();
        withContentLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26"))));
        withContentLabel.setFont(JBUI.Fonts.create(null,10));
        withContentLabel.setText(withContent);
        southContentPanel.add(withContentLabel);

        southPanel.add(southContentPanel,isMe ? BorderLayout.EAST : BorderLayout.WEST);

        add(southPanel,BorderLayout.SOUTH); // 将 MainPanel 添加到中心
    }

    public RoundPanel MessageTextareaComponent (String content, Boolean isMe, JsonArray attachments) {
        RoundPanel messagePanel = new RoundPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel,BoxLayout.Y_AXIS));
        messagePanel.add(new MessageTextareaComponent(content,isMe ? Color.decode("#b4d6ff") : Color.decode("#ffffff")));
//        for (int i = 0; i < attachments.size(); i++) {
//            JsonObject item = attachments.get(i).getAsJsonObject();
//            if (item.has("type") && StringUtil.equals(item.get("type").getAsString(),"image")) {
//                ImageViewInMessage imageView = new ImageViewInMessage(null,item.get("fileName").getAsString(),256);
//                messagePanel.add(Box.createRigidArea(new Dimension(0, 8))); // 上间距
//                messagePanel.add(imageView);
//                if (i != attachments.size() - 1) messagePanel.add(Box.createRigidArea(new Dimension(0, 8))); // 下间距
//            }
//        }
        return messagePanel;
    }
}
