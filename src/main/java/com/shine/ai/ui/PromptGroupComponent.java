package com.shine.ai.ui;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.NullableComponent;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.settings.CFAISettingPanel;
import com.shine.ai.settings.GoogleAISettingPanel;
import com.shine.ai.settings.GroqAISettingPanel;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PromptGroupComponent extends JBPanel<PromptGroupComponent> implements NullableComponent {
    private final JLabel listCountsLabel;

    private final JPanel myList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane myScrollPane = new MyScrollPane(myList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private int myScrollValue = 0;

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    public List<JsonObject> promptList = new ArrayList<>();
    public Boolean enablePrompts;

    private final Project ThisProject;
    private final MainPanel ThisMainPanel;
    private final Class<?> AIVendorSet;
    public PromptGroupComponent(@NotNull Project project,Class<?> settingPanel,MainPanel mainP) {
        ThisProject = project;
        ThisMainPanel = mainP;
        AIVendorSet = settingPanel;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(JBUI.Borders.empty());
        setBackground(UIUtil.getListBackground());

        JPanel mainPanel = new JPanel(new BorderLayout());

        add(mainPanel,BorderLayout.CENTER);

        myList.setOpaque(true);
        myList.setBackground(UIUtil.getListBackground());
        myList.setBorder(JBUI.Borders.empty(0,10));

        listCountsLabel = new JLabel();
        Border infoTopOuterBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, new JBColor(Color.lightGray,  Color.decode("#6c6c6c"))); // 使用背景颜色
        Border infoTopInnerBorder = JBUI.Borders.empty(8,24);
        Border compoundBorder = BorderFactory.createCompoundBorder(infoTopOuterBorder,infoTopInnerBorder);
        listCountsLabel.setBorder(compoundBorder);
        listCountsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        listCountsLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Gray.x80, Gray.x8C)));
        listCountsLabel.setFont(JBUI.Fonts.create(null,13));
        mainPanel.add(listCountsLabel,BorderLayout.NORTH);

        init();

        myScrollPane.setBorder(JBUI.Borders.empty());
        mainPanel.add(myScrollPane,BorderLayout.CENTER);

        myScrollPane.getVerticalScrollBar().setAutoscrolls(true);
        myScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            int value = e.getValue();
            if (myScrollValue == 0 && value > 0 || myScrollValue > 0 && value == 0) {
                myScrollValue = value;
                repaint();
            }
            else {
                myScrollValue = value;
            }
        });

        myList.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                refreshListCounts();
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                refreshListCounts();
            }
        });
    }

    public void init() {
        initPromptList();
        refreshListCounts();
    }

    public void refreshStatus() {
        removeList();
        initPromptList();
        refreshListCounts();
    }

    public void addPrompt(JsonObject prompt) {
        JsonObject cPrompt = prompt.deepCopy();

        String content = cPrompt.get("content").getAsString();
        String role = cPrompt.get("role").getAsString();
        String chatId = cPrompt.get("chatId").getAsString();

        cPrompt.remove("chatId");
        cPrompt.addProperty("promptId",chatId);
        cPrompt.addProperty("icon", StringUtil.equals(role, "user") ?  AIAssistantIcons.ME_PATH : AIAssistantIcons.AI_PATH);
        cPrompt.addProperty("name", role);
        cPrompt.addProperty("isMe", StringUtil.equals(role, "user"));
        cPrompt.addProperty("status", 1);
        cPrompt.addProperty("time", 0);
        cPrompt.addProperty("isPin", false);
        cPrompt.addProperty("withContent", content.isBlank() ? "无效提示词" : "预设提示词"); // 把进行时状态改成1

        promptList.add(cPrompt);
        MessageComponent messageComponentItem = new MessageComponent(ThisProject,cPrompt,null);
        myList.add(messageComponentItem);
        updateLayout();
        updateUI();
    }

    public void refreshListCounts() {
        if (enablePrompts) {
            listCountsLabel.setText("total：" + promptList.size() + " prompts");
            listCountsLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26"))));
            setVisible(true);
            myList.setVisible(true);
        }else {
            setVisible(false);
            myList.setVisible(false);
        }
    }

    public void delete(String chatId,JComponent component) {
        promptList.removeIf(it -> StringUtil.equals(it.get("promptId").getAsString(),chatId));
        for (Component comp : myList.getComponents()) {
            if (comp instanceof MessageComponent messageItem) {
                if (StringUtil.equals(messageItem.chatId, chatId)) {
                    myList.remove(messageItem); //使用remove(Component)方法
                }
            }
        }
        updateLayout();
        updateUI();
    }

    public void removeList() {
        myList.removeAll();
    }

    public void openPromptList() {
        for (JsonObject chatItem : promptList) {
            String content = chatItem.get("content").getAsString();
            String role = chatItem.get("role").getAsString();

            chatItem.addProperty("icon", StringUtil.equals(role, "user") ?  AIAssistantIcons.ME_PATH : AIAssistantIcons.AI_PATH);
            chatItem.addProperty("name", role);
            chatItem.addProperty("isMe", StringUtil.equals(role, "user"));
            chatItem.addProperty("status", 1);
            chatItem.addProperty("time", 0);
            chatItem.addProperty("isPin", false);
            chatItem.addProperty("withContent", content.isBlank() ? "无效提示词" : "预设提示词"); // 把进行时状态改成1
            MessageComponent promptComponentItem = new MessageComponent(ThisProject,chatItem,null);

            myList.add(promptComponentItem);
        }
        updateLayout();
        updateUI();
    }


    public void initPromptList() {
        List<String> prompts = new ArrayList<>();
        if (AIVendorSet.equals(CFAISettingPanel.class)) {
            enablePrompts = stateStore.CFEnablePrompts;
            prompts = stateStore.CFPrompts;
        } else if (AIVendorSet.equals(GoogleAISettingPanel.class)) {
            enablePrompts = stateStore.GOEnablePrompts;
            prompts = stateStore.GOPrompts;
        } else if (AIVendorSet.equals(GroqAISettingPanel.class)) {
            enablePrompts = stateStore.GREnablePrompts;
            prompts = stateStore.GRPrompts;
        }
        promptList = prompts.stream()
                .map(stateStore::getJsonObject)
                .collect(Collectors.toList());
        openPromptList();
    }

    public void updateLayout() {
        LayoutManager layout = myList.getLayout();
        int componentCount = myList.getComponentCount();
        for (int i = 0 ; i< componentCount ; i++) {
            layout.removeLayoutComponent(myList.getComponent(i));
            layout.addLayoutComponent(null,myList.getComponent(i));
        }
        myList.revalidate();
        myList.repaint();
        myScrollPane.revalidate();
        myScrollPane.repaint();  // 确保ScrollPane也重新验证布局和重绘
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (myScrollValue > 0) {
            g.setColor(JBColor.border());
            int y = myScrollPane.getY() - 1;
            g.drawLine(0, y, getWidth(), y);
        }
    }

    @Override
    public boolean isNull() {
        return !isVisible();
    }

    static class MyAdjustmentListener implements AdjustmentListener {

        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            JScrollBar source = (JScrollBar) e.getSource();
            if (!source.getValueIsAdjusting()) {
                source.setValue(source.getMaximum());
            }
        }
    }
}
