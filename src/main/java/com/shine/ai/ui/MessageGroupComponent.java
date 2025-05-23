package com.shine.ai.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.NullableComponent;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.WrapLayout;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.*;
import com.shine.ai.ui.listener.SendListener;
import com.shine.ai.util.*;
import okhttp3.Call;
import okhttp3.sse.EventSource;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.shine.ai.MyToolWindowFactory.disabledCollectionAction;

public class MessageGroupComponent extends JBPanel<MessageGroupComponent> implements NullableComponent {
    private final JPanel infoTopPanel = new JPanel(new BorderLayout());
    private final JPanel infoPanel = new JPanel(new BorderLayout());
    private final JPanel myList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane myScrollPane = new MyScrollPane(myList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private int myScrollValue = 0;

    private final JLabel listCountsLabel;

    private IconButton chatSettingButton;

    public final MultilineInput inputTextArea;
    public final JButton button;
    private final JButton stopGenerating;
    private final JProgressBar progressBar;
    private ExecutorService executorService;
    private Object requestHolder;
    private final JPanel actionPanel;
    private final RoundPanel actionSouthPanel;
    private final JPanel actionSouthPanelActions;
    private final JPanel uploadListPanel;

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private final MyAdjustmentListener scrollListener = new MyAdjustmentListener();
    public final JsonObject AIInfo = new JsonObject();
    public final JsonObject MyInfo = new JsonObject();
    public List<String> AIPrompts = new ArrayList<>();
    private final JsonObject AISetInfo = new JsonObject();
    private JsonObject AISetOutputInfo = new JsonObject();
    private JsonObject chatCollection = new JsonObject();
    private List<JsonObject> chatList = new ArrayList<>();

    // 分页渲染相关参数
    private List<JsonObject> currentChatList = new ArrayList<>();; // 当前显示的数据
    private int currentIndex = 0; // 当前数据在总数据中的起始索引
    private final int PAGE_SIZE = 20; // 每页加载的数据量
    private final int MAX_SIZE = 40; // 最大显示数据量

    private final Project ThisProject;
    private final MainPanel ThisMainPanel;
    private final Class<?> AIVendorSet;
    public MessageGroupComponent(@NotNull Project project,Class<?> settingPanel,MainPanel mainP) {
        ThisProject = project;
        ThisMainPanel = mainP;
        AIVendorSet = settingPanel;

        SendListener listener = new SendListener(ThisMainPanel);

        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(JBUI.Borders.empty());
        setBackground(UIUtil.getListBackground());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        add(mainPanel,BorderLayout.CENTER);

        myList.setOpaque(true);
        myList.setBackground(UIUtil.getListBackground());
        myList.setBorder(JBUI.Borders.empty(0,10));

        actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBorder(JBUI.Borders.empty(2,8,10,8));

        button = new JButton(MsgEntryBundle.message("ui.toolwindow.send"), IconLoader.getIcon("/icons/send.svg",MainPanel.class));
        button.setOpaque(false);
        button.setToolTipText("Ctrl + Enter");
        button.addActionListener(listener);

        IconButton settingButton = getSettingButton(settingPanel);
        IconButton imageButton = getImageButton();

        stopGenerating = new JButton("Stop", AllIcons.Actions.Suspend);
        stopGenerating.setOpaque(false);
        stopGenerating.addActionListener(e -> {
            executorService.shutdownNow();
            aroundRequest(false);
            if (requestHolder instanceof EventSource) {
                ((EventSource)requestHolder).cancel();
            } else if (requestHolder instanceof Call) {
                ((Call) requestHolder).cancel();
            }
        });

        inputTextArea = new MultilineInput(ThisMainPanel);
        inputTextArea.setBorder(JBUI.Borders.emptyBottom(6));
        new InputPlaceholder("Shift + Enter to line-warp, Ctrl + Enter to submit.", inputTextArea.getTextarea());  // 添加 placeholder
        inputTextArea.setMinimumSize(new Dimension(inputTextArea.getWidth(), inputTextArea.getPreferredSize().height));
        inputTextArea.getTextarea().setLineWrap(stateStore.enableLineWarp);
        inputTextArea.getTextarea().setWrapStyleWord(stateStore.enableLineWarp);
        inputTextArea.getTextarea().setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));

        inputTextArea.getTextarea().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_V && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    pasteImageUpload();
                }
            }
        });


        actionSouthPanel = new RoundPanel(new BorderLayout());
        actionSouthPanel.setOpaque(false);
        actionSouthPanel.setBorder(JBUI.Borders.empty(12,8,4,8));

        SwingUtilities.invokeLater(()-> {
            actionSouthPanel.setBackground(UIUtil.getTextFieldBackground());
        });

        uploadListPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
        uploadListPanel.setOpaque(false);

        actionSouthPanelActions = new JPanel(new BorderLayout());
        actionSouthPanelActions.setOpaque(false);

        JPanel actionsLeft = new JPanel();
        actionsLeft.setOpaque(false);
        actionsLeft.setLayout(new BoxLayout(actionsLeft, BoxLayout.X_AXIS));
        actionsLeft.add(settingButton);
        actionsLeft.add(imageButton);
        actionSouthPanelActions.add(actionsLeft, BorderLayout.WEST);
        actionSouthPanelActions.add(button, BorderLayout.EAST);
        actionSouthPanel.add(actionSouthPanelActions,BorderLayout.SOUTH);
        actionSouthPanel.add(uploadListPanel, BorderLayout.CENTER);
        actionSouthPanel.add(inputTextArea, BorderLayout.NORTH);


        JPanel actionNorthPanel = new JPanel(new BorderLayout());
        listCountsLabel = new JLabel();
        listCountsLabel.setBorder(JBUI.Borders.empty(2,0));
        listCountsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        listCountsLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Gray.x80, Gray.x8C)));
        listCountsLabel.setFont(JBUI.Fonts.create(null,11));
        actionNorthPanel.add(listCountsLabel,BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        actionNorthPanel.add(progressBar,BorderLayout.SOUTH);

        actionPanel.add(actionNorthPanel, BorderLayout.NORTH);
        actionPanel.add(actionSouthPanel, BorderLayout.SOUTH);
        mainPanel.add(actionPanel,BorderLayout.SOUTH);

        init();

        infoTopPanel.setOpaque(true);
        Border infoTopOuterBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, new JBColor(Color.lightGray,  Color.decode("#6c6c6c"))); // 使用背景颜色
        Border infoTopInnerBorder = JBUI.Borders.empty(12,20);
        Border compoundBorder = BorderFactory.createCompoundBorder(infoTopOuterBorder,infoTopInnerBorder);
        infoTopPanel.setBorder(compoundBorder);

        initAIInfoPanel(); // 这里写出方法好修改渲染数据

        mainPanel.add(infoTopPanel, BorderLayout.NORTH);

        myScrollPane.setBorder(JBUI.Borders.empty());
        JViewport myScrollViewport = myScrollPane.getViewport();
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
//            if (value == minValue) {
//                // 向上滚动到顶部
//                loadPreviousData();
//            } else if (value + visibleAmount == maxValue) {
//                // 向下滚动到底部
//                loadNextData();
//            }
        });

        myScrollViewport.addChangeListener(e -> {
            Rectangle visibleRect = myScrollViewport.getViewRect();
            // 创建扩展后的可见区域
            Rectangle extendedVisibleRect = new Rectangle(
                    visibleRect.x,
                    visibleRect.y + 32,
                    visibleRect.width,
                    visibleRect.height - 64
            );
            for (int i = 0; i < myList.getComponentCount(); i++) {
                Component component = myList.getComponent(i);
                if (component instanceof MessageComponent messageComponent) {
                    Rectangle panelBounds = messageComponent.getBounds();
                    Rectangle actionsPanel = new Rectangle(panelBounds.x,panelBounds.y,panelBounds.width,32);
                    // 检查组件是否在显示区域内
                    boolean isVisible = extendedVisibleRect.intersects(actionsPanel);
                    CardLayout cl = (CardLayout)(messageComponent.actionPanel.getLayout());
                    cl.show(messageComponent.actionPanel, isVisible ? "messageActions":"placeholder");
                }
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
        initAiInfo();
        initMyInfo();
        initAISetInfo();
        initChatList();
        refreshListCounts();
    }

    public void setItemsDisabledRerunAndTrash(Boolean disabled) {
        for (int i = 0; i < myList.getComponentCount(); i++) { // 从后往前循环
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                messageComponent.messageActions.setDisabledRerunAndTrash(disabled);
            }
        }
    }

    public void setItemsDisabled(Boolean disabled) {
        for (int i = 0; i < myList.getComponentCount(); i++) { // 从后往前循环
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                messageComponent.messageActions.setDisabled(disabled);
            }
        }
    }

    public MessageComponent add(JsonObject message) {
        chatList.add(message);
        chatCollection.add("chatList",stateStore.getJsonArray(chatList));
        stateStore.updateChatCollectionInfo(chatCollection);
        MessageComponent messageComponentItem = new MessageComponent(ThisProject,message,this);
        myList.add(messageComponentItem);
        updateLayout();
        scrollToBottom();
        updateUI();
        return messageComponentItem;
    }

    public void updateMessageState(String chatId,JsonObject newData) {
        for (JsonObject chatItem : chatList) {
            if (StringUtil.equals(chatItem.get("chatId").getAsString(), chatId)) {
                chatItem = stateStore.mergeJsonObject(chatItem,newData);
                chatCollection.add("chatList",stateStore.getJsonArray(chatList));
                stateStore.updateChatCollectionInfo(chatCollection);
                break; // 找到后立即退出循环
            }
        }
    }

    public MessageComponent getLastItem(String chatId) {
        MessageComponent comp = null;
        if (chatId == null || chatId.isBlank()) {
            return (MessageComponent) myList.getComponent(myList.getComponentCount() - 1);
        }
        for (int i = 0; i < myList.getComponentCount(); i++) {
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                if (StringUtil.equals(messageComponent.chatId, chatId)) {
                    comp = messageComponent;
                    break;
                }
            }
        }
        return comp;
    }

    public void modifyListItemInfo(JsonObject chatItemInfo) {
        for (int i = 0; i < myList.getComponentCount(); i++) {
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                JsonObject imf = chatList.stream()
                        .filter(item -> StringUtil.equals(item.get("chatId").getAsString(), chatItemInfo.get("chatId").getAsString()))
                        .findFirst().orElse(null);
                if (StringUtil.equals(messageComponent.chatId, chatItemInfo.get("chatId").getAsString()) && imf != null) {
                    JsonObject updateItemInfo = stateStore.mergeJsonObject(imf,chatItemInfo);
                    // 修改 MessageComponent 的内容
                    messageComponent.updateContent(updateItemInfo);
                    break; // 找到后退出循环
                }
            }
        }
    }

    public void refreshListCounts() {
        listCountsLabel.setText("total：" + chatList.size() + " dialogs");
    }

    public void addPin(JsonObject message,JComponent component) {
        String chatId = message.get("chatId").getAsString();
        String content = message.get("content").getAsString();
        String role = message.get("role").getAsString();

        int setIdx = IntStream.range(0, AIPrompts.size())
                .filter(i -> StringUtil.equals(stateStore.getJsonObject(AIPrompts.get(i)).get("promptId").getAsString(), chatId))
                .findFirst()
                .orElse(-1);
        if (setIdx >= 0) return;

        JsonObject prompt = new JsonObject();
        prompt.addProperty("promptId", chatId);
        prompt.addProperty("role",role);
        prompt.addProperty("content",content);

        AIPrompts.add(AIPrompts.size(),stateStore.getJsonString(prompt));

        for (JsonObject element : chatList) {
            if (StringUtil.equals(element.get("chatId").getAsString(), chatId)) {
                element.addProperty("isPin",true);
                MessageComponent messageComponent = (MessageComponent) component;
                messageComponent.updateActions(element);
                break; // 找到后立即退出循环
            }
        }
    }

    public void deletePin(String chatId,JComponent component) {
        int delIdx = IntStream.range(0, AIPrompts.size())
                .filter(i -> StringUtil.equals(stateStore.getJsonObject(AIPrompts.get(i)).get("promptId").getAsString(), chatId))
                .findFirst()
                .orElse(-1);

        if (delIdx >= 0) {
            AIPrompts.remove(delIdx);
        }
        for (JsonObject element : chatList) {
            if (StringUtil.equals(element.get("chatId").getAsString(), chatId)) {
                element.addProperty("isPin",false);
                MessageComponent messageComponent = (MessageComponent) component;
                messageComponent.updateActions(element);
                break; // 找到后立即退出循环
            }
        }
    }

    public void delete(String chatId,JComponent component) {
        chatList.removeIf(it -> StringUtil.equals(it.get("chatId").getAsString(),chatId));
        chatCollection.add("chatList",stateStore.getJsonArray(chatList));
        stateStore.updateChatCollectionInfo(chatCollection);
        myList.remove(component);
        updateLayout();
        updateUI();
    }

    public void removeInfo() {
        infoPanel.removeAll();
        infoTopPanel.removeAll();
    }

    public void removeList() {
        myList.removeAll();
    }

    public void updateChatList() {
        for (JsonObject chatItem : chatList) {
            int promptIdx = IntStream.range(0, AIPrompts.size())
                    .filter(i -> StringUtil.equals(stateStore.getJsonObject(AIPrompts.get(i)).get("promptId").getAsString(), chatItem.get("chatId").getAsString()))
                    .findFirst()
                    .orElse(-1);
            int status = chatItem.get("status").getAsInt();
            chatItem.addProperty("isPin", promptIdx >= 0); // 把是否提示词判断下
            chatItem.addProperty("status", (status == 0 || status == 2) ? -1 : status); // 把进行时状态改成-1
            MessageComponent messageComponentItem = new MessageComponent(ThisProject,chatItem,this);
            myList.add(messageComponentItem);
        }

        updateLayout();
        scrollToBottom();
        updateUI();
    }

    public void initChatList() {
        if (stateStore.AIChatCollection.isEmpty()) {
            String newChatCollection = stateStore.createChatCollection();
            chatCollection = stateStore.getJsonObject(newChatCollection);
            stateStore.AIChatCollection.add(0,newChatCollection);
        }else {
            chatCollection = stateStore.getJsonObject(stateStore.AIChatCollection.get(0));
        }
        chatList = chatCollection.get("chatList").getAsJsonArray().asList().stream()
                .map(JsonElement::getAsJsonObject)
                .collect(Collectors.toList());

        updateChatList();
    }

    private void loadInitialChatlist() {
        currentChatList.addAll(chatList.subList(Math.max(chatList.size() - PAGE_SIZE, 0), chatList.size()));
    }

    private void loadPreviousData() {
        if (currentIndex == 0) return;
        int newIndex = Math.max(0, currentIndex - PAGE_SIZE);
        int loadSize = Math.min(PAGE_SIZE, currentIndex - newIndex);
        List<JsonObject> newData = chatList.subList(newIndex, newIndex + loadSize);
        currentChatList.addAll(0, newData);
        if (currentChatList.size() > MAX_SIZE) {
            currentChatList.subList(MAX_SIZE, currentChatList.size()).clear(); // 删除尾部
        }
        currentIndex = newIndex;
        updateChatList();
    }

    private void loadNextData() {
        if (currentIndex + currentChatList.size() >= chatList.size()) return;
        int newIndex = Math.min(chatList.size() - 1, currentIndex + PAGE_SIZE);
        int loadSize = Math.min(PAGE_SIZE, chatList.size() - newIndex);
        List<JsonObject> newData = chatList.subList(newIndex, newIndex + loadSize);
        currentChatList.addAll(newData);
        if (currentChatList.size() > MAX_SIZE) {
            currentChatList.subList(0, currentChatList.size() - MAX_SIZE).clear(); // 删除头部
        }
        currentIndex = newIndex - (Math.max(currentChatList.size() - MAX_SIZE, 0));
        updateChatList();
    }

    public void initAIInfoPanel() {
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setBorder(JBUI.Borders.emptyRight(12));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));// 从左到右排列
        chatSettingButton = new IconButton("Chat Setting",AllIcons.Actions.RefactoringBulb);
        chatSettingButton.addActionListener(e -> {
            new ChatSettingDialog(ThisProject).openDialog((JComponent) ThisMainPanel.getContentPanel().getParent(),AIVendorSet);
        });
        actionPanel.add(chatSettingButton);
        eastPanel.add(actionPanel,BorderLayout.NORTH);

        infoTopPanel.add(eastPanel, BorderLayout.EAST);

        JBLabel currentModel = new JBLabel();
        String currentModelStr = AISetInfo.get("aiModel").isJsonNull() ? "LLM：" : "LLM：" + AISetInfo.get("aiModel").getAsString();
        currentModel.setText(currentModelStr);
        currentModel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Gray.x80, Gray.x8C)));
        currentModel.setFont(JBUI.Fonts.create(Font.DIALOG,14));
        currentModel.setBorder(JBUI.Borders.emptyBottom(4));
        infoPanel.add(currentModel,BorderLayout.NORTH);

        JPanel southInfoPanel = new JPanel();
        southInfoPanel.setLayout(new BoxLayout(southInfoPanel, BoxLayout.X_AXIS));
        JLabel streamInfo = new JLabel();
        String streamInfoStr = AISetInfo.get("aiStream").getAsBoolean() ? "Stream Speed：" + AISetInfo.get("streamSpeed") : "Stream：Off";
        streamInfo.setText(streamInfoStr);
        streamInfo.setFont(JBUI.Fonts.toolbarFont());
        southInfoPanel.add(streamInfo);

        JLabel promptsInfo = new JLabel();
        boolean promptsCutIn = AISetInfo.get("promptsCutIn").getAsBoolean();
        promptsInfo.setBorder(JBUI.Borders.emptyLeft(12));
        promptsInfo.setForeground(JBColor.namedColor("Label.infoForeground",promptsCutIn ? new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26")) : new JBColor(Gray.x80, Gray.x8C)));
        String promptsInfoStr = promptsCutIn ? "Prompts：On" : "Prompts：Off";
        promptsInfo.setText(promptsInfoStr);
        promptsInfo.setFont(JBUI.Fonts.toolbarFont());
        southInfoPanel.add(promptsInfo);

        infoPanel.add(southInfoPanel,BorderLayout.SOUTH);


        infoTopPanel.add(infoPanel,BorderLayout.WEST);
    }

    public void initAiInfo() {
        AIInfo.addProperty("time","");
        AIInfo.addProperty("content","");
        AIInfo.addProperty("isMe",false);
        AIInfo.addProperty("role","assistant");
        AIInfo.addProperty("status",0); // 0 加载中 1加载完成 -1生成出错, -2网络错 -3输出中止 2持续输出
        AIInfo.addProperty("isPin",false);
        AIInfo.addProperty("showBtn",false);
        AIInfo.addProperty("withContent",""); // 内容相关, 例如违法条例等
        AIInfo.add("attachments",new JsonArray()); // 附件相关, 例如图片文件等

        if (AIVendorSet.equals(CFAISettingPanel.class)) {
            AIInfo.addProperty("name", MsgEntryBundle.message("ui.setting.server.cloudflare.name"));
            AIInfo.addProperty("icon",AIAssistantIcons.CF_AI_URL);
        } else if (AIVendorSet.equals(GoogleAISettingPanel.class)) {
            AIInfo.addProperty("name",MsgEntryBundle.message("ui.setting.server.google.name"));
            AIInfo.addProperty("icon",AIAssistantIcons.GOOGLE_AI_URL);
        } else if (AIVendorSet.equals(GroqAISettingPanel.class)) {
            AIInfo.addProperty("name",MsgEntryBundle.message("ui.setting.server.groq.name"));
            AIInfo.addProperty("icon",AIAssistantIcons.GROQ_AI_URL);
        }
    }

    public void initMyInfo() {
        MyInfo.addProperty("time","");
        MyInfo.addProperty("icon", AIAssistantIcons.ME_URL);
        MyInfo.addProperty("name","我");
        MyInfo.addProperty("content","");
        MyInfo.addProperty("isMe",true);
        MyInfo.addProperty("role","user");
        MyInfo.addProperty("status",1); // 0 加载中 1加载完成 -1生成出错, -2网络错 -3输出中止 2持续输出
        MyInfo.addProperty("isPin",false);
        MyInfo.addProperty("showBtn",false);
        MyInfo.addProperty("withContent",""); // 内容相关, 例如违法条例等

        MyInfo.add("attachments",new JsonArray()); // 附件相关, 例如图片文件等
    }

    public void initAISetInfo() {
        if (AIVendorSet.equals(CFAISettingPanel.class)) {
            AISetInfo.addProperty("aiModel",stateStore.CFCurrentModel);
            AISetInfo.addProperty("aiStream",stateStore.CFEnableStream);
            AISetInfo.addProperty("streamSpeed",stateStore.CFStreamSpeed);
            AISetInfo.addProperty("promptsCutIn",stateStore.CFEnablePrompts);
            AISetOutputInfo = stateStore.getCFSetOutputConf();
            AIPrompts = stateStore.CFPrompts;
        } else if (AIVendorSet.equals(GoogleAISettingPanel.class)) {
            AISetInfo.addProperty("aiModel",stateStore.GOCurrentModel);
            AISetInfo.addProperty("aiStream",stateStore.GOEnableStream);
            AISetInfo.addProperty("streamSpeed",stateStore.GOStreamSpeed);
            AISetInfo.addProperty("promptsCutIn",stateStore.GOEnablePrompts);
            AISetOutputInfo = stateStore.getGOSetOutputConf();
            AIPrompts = stateStore.GOPrompts;
        } else if (AIVendorSet.equals(GroqAISettingPanel.class)) {
            AISetInfo.addProperty("aiModel",stateStore.GRCurrentModel);
            AISetInfo.addProperty("aiStream",stateStore.GREnableStream);
            AISetInfo.addProperty("streamSpeed",stateStore.GRStreamSpeed);
            AISetInfo.addProperty("promptsCutIn",stateStore.GREnablePrompts);
            AISetOutputInfo = stateStore.getGRSetOutputConf();
            AIPrompts = stateStore.GRPrompts;
        }
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> { // 在Swing事件调度线程上执行
            JScrollBar verticalScrollBar = myScrollPane.getVerticalScrollBar();
            int max = verticalScrollBar.getMaximum();
            if (max > 0) { // 避免在内容为空的情况下的异常
                verticalScrollBar.setValue(max);
            }
        });
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

    private @NotNull IconButton getSettingButton(Class<?> settingPanel) {
        IconButton settingButton = new IconButton("setting", IconLoader.getIcon("/icons/setting.svg",MainPanel.class));
        settingButton.addActionListener(e -> {
            if (settingPanel.equals(CFAISettingPanel.class)) {
                ShowSettingsUtil.getInstance().showSettingsDialog(ThisProject, CFAISettingPanel.class);
            } else if (settingPanel.equals(GoogleAISettingPanel.class)) {
                ShowSettingsUtil.getInstance().showSettingsDialog(ThisProject, GoogleAISettingPanel.class);
            } else if (settingPanel.equals(GroqAISettingPanel.class)) {
                ShowSettingsUtil.getInstance().showSettingsDialog(ThisProject, GroqAISettingPanel.class);
            }
        });
        return settingButton;
    }

    private @NotNull IconButton getImageButton() {
        IconButton imageButton = new IconButton("choose image", IconLoader.getIcon("/icons/image.svg",MainPanel.class));
        imageButton.addActionListener(e -> handleImageUpload());
        return imageButton;
    }

    private void handleImageUpload() {
        if (!checkImagesLen()) return;
        Image img = ImgUtils.chooseImage(ThisProject);
        if (img != null) {
            addImage(img);
        }
    }

    private void pasteImageUpload() {
        if (!checkImagesLen()) return;
        Image img = inputTextArea.pasteFromClipboardImage();
        if (img != null) {
            addImage(img);
        }
    }

    private void addImage(Image img) {
        if (img != null) {
            ImageView imagePanel = new ImageView(img);
            doUploadImage(img,imagePanel);

            imagePanel.getDeleteButton().addActionListener(e -> {
                removeImage(imagePanel);
            });

            imagePanel.getReUploadButton().addActionListener(e -> {
                doUploadImage(img,imagePanel);
            });

            uploadListPanel.add(imagePanel);
            uploadListPanel.updateUI();
        }
    }

    private void doUploadImage(Image img,ImageView imagePanel) {
        new SwingWorker<>() {
            @Override
            protected JsonObject doInBackground() {
                imagePanel.setLoading(true);
                return ShineAIUtil.uploadImg(img,imagePanel);
            }
            @Override
            protected void done() {
                try {
                    JsonObject resData = (JsonObject) get(); // 获取 doInBackground() 的结果，如果发生异常，会在这里抛出
                    String fileName = resData.get("fileName").getAsString();
                    String url = resData.has("url") ? resData.get("url").getAsString() : null;
                    imagePanel.setImage(url,fileName);
                    imagePanel.setLoading(false);
                } catch (InterruptedException | ExecutionException e) {
                    imagePanel.setImage(null,null);
                    imagePanel.setLoading(false);
                    System.out.println("doUploadImage error：" + e.getMessage());
                }
            }
        }.execute();
    }

    private Boolean checkImagesLen() {
        if (uploadListPanel.getComponentCount() > 9) {
            Notifications.Bus.notify(new Notification(MsgEntryBundle.message("group.id"),"Add error","Cannot add more images.",NotificationType.ERROR));
            return false;
        }
        return true;
    }

    private void removeImage(JComponent imageItem) {
        if (imageItem != null) {
            uploadListPanel.remove(imageItem);
            uploadListPanel.updateUI();
        }
    }

    public JsonArray getUploadList() {
        JsonArray upLoadList = new JsonArray();
        for (int i = 0; i < uploadListPanel.getComponentCount(); i++) {
            Component component = uploadListPanel.getComponent(i);
            if (component instanceof ImageView ImageComponent) {
                if (ImageComponent.isUploaded && ImageComponent.getImage() != null) {
                    JsonObject attachmentItem = new JsonObject();
                    attachmentItem.addProperty("fileName",ImageComponent.getName());
                    attachmentItem.addProperty("type","image");
                    attachmentItem.addProperty("mimeType","image/jpg"); // 强制jpg
                    attachmentItem.addProperty("url",ImageComponent.getUrl());
                    upLoadList.add(attachmentItem);
                }
            }
        }
        return upLoadList;
    }

    public void removeUploadList() {
        uploadListPanel.removeAll();
    }

    public void setRequestHolder(Object eventSource) {
        this.requestHolder = eventSource;
    }

    public ExecutorService getExecutorService() {
        executorService = Executors.newFixedThreadPool(1);
        return executorService;
    }

    public void aroundRequest(boolean status) {
        progressBar.setIndeterminate(status);
        progressBar.setVisible(status);
        button.setEnabled(!status);
        if (status) {
            setItemsDisabledRerunAndTrash(true);
            disabledCollectionAction(true);
            chatSettingButton.setEnabled(false);
            actionSouthPanelActions.remove(button);
            actionSouthPanelActions.add(stopGenerating,BorderLayout.EAST);
        } else {
            ExecutorService service = getExecutorService();
            if (service != null) {
                service.shutdown();
            }
            setItemsDisabled(false);
            disabledCollectionAction(false);
            chatSettingButton.setEnabled(true);
            actionSouthPanelActions.remove(stopGenerating);
            actionSouthPanelActions.add(button,BorderLayout.EAST);
        }
        actionPanel.updateUI();
    }

    public void addScrollListener() {
        myScrollPane.getVerticalScrollBar().
                addAdjustmentListener(scrollListener);
    }

    public void removeScrollListener() {
        myScrollPane.getVerticalScrollBar().
                removeAdjustmentListener(scrollListener);
    }

    public JsonObject getAISetInfo() {
        return AISetInfo;
    }

    public JsonObject getChatCollection () {
        return chatCollection;
    }

    public JsonObject getAISetOutputInfo() {
        return AISetOutputInfo;
    }
}
