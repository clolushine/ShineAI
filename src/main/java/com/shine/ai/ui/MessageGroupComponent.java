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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.NullableComponent;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.WrapLayout;
import com.shine.ai.db.DBUtil;
import com.shine.ai.db.chats.Chats;
import com.shine.ai.db.chats.ChatsManager;
import com.shine.ai.db.colls.Colls;
import com.shine.ai.db.colls.CollsManager;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.*;
import com.shine.ai.ui.listener.SendListener;
import com.shine.ai.util.*;
import okhttp3.Call;
import okhttp3.sse.EventSource;
import org.jetbrains.annotations.NotNull;
import cn.hutool.core.swing.clipboard.ClipboardUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextAreaUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.shine.ai.MyToolWindowFactory.*;
import static com.shine.ai.vendors.AIVendors.*;

public class MessageGroupComponent extends JBPanel<MessageGroupComponent> implements NullableComponent, Disposable, MessageComponent.MessageActionCallback {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    public final JPanel infoTopPanel = new JPanel(new BorderLayout());
    private final JPanel infoPanel = new JPanel(new BorderLayout());
    private final JPanel myList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane myScrollPane = new MyScrollPane(myList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);;
    //
    private volatile boolean shouldScrollToBottom = false;

    private final JLabel listCountsLabel;

    private IconButton chatSettingButton;

    private IconButton chatMatchButton;

    // 初始化查询窗
    public final FindMatchDialog findMatchDialog = new FindMatchDialog();

    private final IconButton uploadImgButton;
    private final IconButton webSearchButton;

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

    // chats db
    private final ChatsManager chatsManager = ChatsManager.getInstance();

    // colls db
    private final CollsManager collsManager = CollsManager.getInstance();


    public JsonObject AIInfo = new JsonObject();
    public JsonObject MyInfo = new JsonObject();
    public List<JsonObject> AIPrompts = new ArrayList<>();
    private final JsonObject AISetInfo = new JsonObject();
    private JsonObject AISetOutputInfo = new JsonObject();
    // 新增
    private JsonObject collsIt = new JsonObject();
    private List<JsonObject> chatList = new ArrayList<>();

    private final int visibleBufferSize = 5;
    private final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private boolean initialLoadComplete = false;

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
        uploadImgButton = getImageButton();
        uploadImgButton.setVisible(false); // 这里先隐藏，设置AI info函数一起设置
        webSearchButton = getWebSearchButton();
        webSearchButton.setVisible(false); // 这里先隐藏，设置AI info函数一起设置

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
        inputTextArea.setBorder(JBUI.Borders.emptyBottom(4));
//        new InputPlaceholder("Shift + Enter to line-warp, Ctrl + Enter to submit.", inputTextArea.getTextarea());  // 添加 placeholder
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
        updateInputTextAreaUI();

        actionSouthPanel = new RoundPanel(new BorderLayout());
        actionSouthPanel.setOpaque(false);
        actionSouthPanel.setArc(12,12);
        actionSouthPanel.setBorder(JBUI.Borders.empty(12,8,4,8));

        // 获取 Application-level 的 MessageBus 连接，并与当前 Disposable 绑定
        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(this);

        // 订阅 LafManagerListener.TOPIC
        messageBusConnection.subscribe(LafManagerListener.TOPIC, new LafManagerListener() {
            @Override
            public void lookAndFeelChanged(@NotNull LafManager source) {
                updateActionSouthPanelBg(); // 主题变化时更新背景
                updateInputTextAreaUI(); // 更新输入框ui

                // 修改主题色
                changeMessagePanelTheme();
            }
        });

        uploadListPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
        uploadListPanel.setOpaque(false);

        actionSouthPanelActions = new JPanel(new BorderLayout());
        actionSouthPanelActions.setOpaque(false);

        JPanel actionsLeft = new JPanel();
        actionsLeft.setOpaque(false);
        actionsLeft.setLayout(new BoxLayout(actionsLeft, BoxLayout.X_AXIS));
        actionsLeft.add(settingButton);
        actionsLeft.add(uploadImgButton);
        actionsLeft.add(webSearchButton);
        actionSouthPanelActions.add(actionsLeft, BorderLayout.WEST);
        actionSouthPanelActions.add(button, BorderLayout.EAST);
        actionSouthPanel.add(actionSouthPanelActions,BorderLayout.SOUTH);
        actionSouthPanel.add(uploadListPanel, BorderLayout.CENTER);
        actionSouthPanel.add(inputTextArea, BorderLayout.NORTH);


        JPanel actionNorthPanel = new JPanel(new BorderLayout());
        listCountsLabel = new JLabel();
        listCountsLabel.setBorder(JBUI.Borders.empty(2,0));
        listCountsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        listCountsLabel.setForeground(new JBColor(Gray.x80, Gray.x8C));
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

        initAIFunctionIcon(); // 设置底部AI功能图标

        mainPanel.add(infoTopPanel, BorderLayout.NORTH);

        mainPanel.add(myScrollPane,BorderLayout.CENTER);

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

        // 为 myList 添加尺寸变化监听器
        myList.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 只有在我们主动请求滚动时，才执行操作
                if (shouldScrollToBottom) {
                    // 此时 myList 的尺寸已经更新，我们现在可以安全地滚动了
                    scrollToBottom();
                    // **非常重要**: 任务完成后，立刻将标志位复位！
                    // 这样可以防止用户后续拖动窗口大小时意外触发滚动。
                    shouldScrollToBottom = false;
                }
            }
        });

        // 设置底部栏的背景
        updateActionSouthPanelBg();

//        myScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
//            int value = e.getValue();
//            int minValue = myScrollPane.getVerticalScrollBar().getMinimum();
//            if (value <= minValue + 128 && initialLoadComplete && !shouldScrollToBottom) {
//                // 向上滚动到顶部
//                loadPreviousData();
//            }
//        });

//        JViewport myScrollViewport = myScrollPane.getViewport();
//        myScrollViewport.addChangeListener(e -> {
//            Rectangle visibleRect = myScrollViewport.getViewRect();
//            // 创建扩展后的可见区域
//            Rectangle extendedVisibleRect = new Rectangle(
//                    visibleRect.x,
//                    visibleRect.y,
//                    visibleRect.width,
//                    visibleRect.height
//            );
//            int totalSize = myList.getComponentCount();
//            List<Integer> visibleIndexList = new ArrayList<>();
//            for (int i = 0; i < totalSize; i++) {
//                MessageComponent component = (MessageComponent) myList.getComponent(i);
//                Rectangle panelBounds = component.getBounds();
//                // 检查组件是否在显示区域内
//                boolean currentIsVisible = extendedVisibleRect.intersects(panelBounds);
//                if (currentIsVisible) {
//                    // 添加当前index及上下bufferSize个元素
//                    int start = Math.max(0, i - visibleBufferSize);
//                    int end = Math.min(totalSize - 1, i + visibleBufferSize);
//                    for (int j = start; j <= end; j++) {
//                        if (!visibleIndexList.contains(j)) {
//                            visibleIndexList.add(j);
//                        }
//                    }
//                }
//                CardLayout cl = (CardLayout)(component.messageCyPanel.getLayout());
//                cl.show(component.messageCyPanel,visibleIndexList.contains(i) ? "messagePanel" : "messagePanelPlaceholder");
//            }
//        });
    }

    public void init() {
        initAiInfo();
        initMyInfo();
        initAISetInfo();
        initChatList();
        refreshListCounts();
    }

    public void changeMessagePanelTheme() {
        for (int i = 0; i < myList.getComponentCount(); i++) { // 从后往前循环
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                messageComponent.changeTheme();
            }
        }
    }

    public void setItemsDisabledRerunAndTrash(Boolean disabled) {
        for (int i = 0; i < myList.getComponentCount(); i++) { // 从后往前循环
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                messageComponent.messageActions.setDisabledRerun(disabled);
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

    public void clearAllHighLights() {
        for (int i = 0; i < myList.getComponentCount(); i++) { // 从后往前循环
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                messageComponent.clearHighlighter();
            }
        }
    }

    public void highlightsAll(List<JsonObject> matchList, int selectedGlobalMatchIndex) {
        for (int i = 0; i < myList.getComponentCount(); i++) {
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                List<JsonObject> matches = getMatchesForComponent(messageComponent, matchList);
                messageComponent.setHighlightsAll(matches, selectedGlobalMatchIndex);
            }
        }
    }

    private List<JsonObject> getMatchesForComponent(MessageComponent mc,List<JsonObject> matchList) {
        String id = mc.chatId; // 或者你实际获取 id 的方式
        return matchList.stream()
                .filter(j -> StringUtil.equals(j.get("id").getAsString(),id))
                .collect(Collectors.toList());
    }

    public MessageComponent add(JsonObject message) {
        chatList.add(message);

        // 写入到db
        chatsManager.addChats(new Chats(message));
        DBUtil.addAttachsBatch(message.get("attachments").getAsJsonArray());
        DBUtil.updateCollsById(collsIt.get("id").getAsString());

        MessageComponent messageComponentItem = new MessageComponent(ThisProject,message);
        messageComponentItem.setActionCallback(this); // 设置回调对象引用
        messageComponentItem.setGlobalScrollPane(myScrollPane); // 设置引用

        myList.add(messageComponentItem);

        scrollToBottomAfterLayout();

        return messageComponentItem;
    }

    public void updateMessageState(String chatId,JsonObject newData) {
        for (JsonObject chatItem : chatList) {
            if (StringUtil.equals(chatItem.get("id").getAsString(), chatId)) {
                chatItem = JsonUtil.mergeJsonObject(chatItem,newData);

                // 写入到db
                chatsManager.addChats(new Chats(chatItem));
                DBUtil.updateCollsById(collsIt.get("id").getAsString());

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

    public MessageComponent getItemById(String chatId) {
        MessageComponent comp = null;
        if (chatId == null || chatId.isBlank()) {
            return null;
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

    public List<JsonObject> getRenderedChatList() {
        List<JsonObject> list = new ArrayList<>();
        for (int i = 0; i < myList.getComponentCount(); i++) {
            Component component = myList.getComponent(i);
            JsonObject chatItem = new JsonObject();
            if (component instanceof MessageComponent messageComponent) {
                chatItem.addProperty("id", messageComponent.chatId);
                chatItem.addProperty("content", messageComponent.getContent());
                list.add(chatItem);
            }
        }
        return list;
    }

    public void refreshListCounts() {
        listCountsLabel.setText("total：" + chatList.size() + " dialogs");
    }

    public void addPin(JsonObject message) {
        String chatId = message.get("id").getAsString();
        String content = message.get("content").getAsString();
        String role = message.get("role").getAsString();

        int setIdx = IntStream.range(0, AIPrompts.size())
                .filter(i -> StringUtil.equals(AIPrompts.get(i).get("promptId").getAsString(), chatId))
                .findFirst()
                .orElse(-1);
        if (setIdx >= 0) return;

        JsonObject prompt = new JsonObject();
        prompt.addProperty("promptId", chatId);
        prompt.addProperty("role",role);
        prompt.addProperty("content",content);

        AIPrompts.add(AIPrompts.size(),prompt);

        for (JsonObject element : chatList) {
            if (StringUtil.equals(element.get("id").getAsString(), chatId)) {
                element.addProperty("isPin",true);
                MessageComponent messageComponent = getByChatId(chatId);;
                if (messageComponent != null) {
                    messageComponent.updateActions(element);
                    break; // 找到后立即退出循环
                }
            }
        }
    }

    public void deletePin(String chatId) {
        int delIdx = IntStream.range(0, AIPrompts.size())
                .filter(i -> StringUtil.equals(AIPrompts.get(i).get("promptId").getAsString(), chatId))
                .findFirst()
                .orElse(-1);

        if (delIdx >= 0) {
            AIPrompts.remove(delIdx);
        }
        for (JsonObject element : chatList) {
            if (StringUtil.equals(element.get("id").getAsString(), chatId)) {
                element.addProperty("isPin",false);
                MessageComponent messageComponent = getByChatId(chatId);
                if (messageComponent != null) {
                    messageComponent.updateActions(element);
                    break; // 找到后立即退出循环
                }
            }
        }
    }

    public void delete(String chatId) {
        chatList.removeIf(it -> StringUtil.equals(it.get("id").getAsString(),chatId));

        // 写入到db
        DBUtil.delChatsById(chatId);
        DBUtil.updateCollsById(collsIt.get("id").getAsString());

        deleteByChatId(chatId);

        updateLayout();
    }

    public void deleteByChatId(String chatId) {
        // 删除该子元素
        for (int i = 0; i < myList.getComponentCount(); i++) {
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                if (StringUtil.equals(messageComponent.chatId,chatId)) {
                    messageComponent.cleanup();
                    myList.remove(messageComponent);
                }
            }
        }
    }

    public MessageComponent getByChatId(String chatId) {
        MessageComponent messageComponent = null;
        for (int i = 0; i < myList.getComponentCount(); i++) {
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageCom) {
                if (StringUtil.equals(messageCom.chatId,chatId)) {
                    messageComponent = messageCom;
                }
            }
        }
        return messageComponent;
    }

    public void removeInfo() {
        infoPanel.removeAll();
        infoTopPanel.removeAll();
    }

    public void removeList() {
        for (int i = 0; i < myList.getComponentCount(); i++) {
            Component component = myList.getComponent(i);
            if (component instanceof MessageComponent messageComponent) {
                messageComponent.cleanup();
                myList.remove(messageComponent);
            }
        }

        myList.removeAll();
    }

    public void updateChatList() {
        for (JsonObject chatItem : chatList) {
            int promptIdx = IntStream.range(0, AIPrompts.size())
                    .filter(i -> StringUtil.equals(AIPrompts.get(i).get("promptId").getAsString(), chatItem.get("id").getAsString()))
                    .findFirst()
                    .orElse(-1);
            int status = chatItem.get("status").getAsInt();
            chatItem.addProperty("isPin", promptIdx >= 0); // 把是否提示词判断下
            chatItem.addProperty("status", (status == 0 || status == 2) ? -1 : status); // 把进行时状态改成-1

            MessageComponent messageComponentItem = new MessageComponent(ThisProject,chatItem);
            messageComponentItem.setActionCallback(this); // 设置回调对象引用
            messageComponentItem.setGlobalScrollPane(myScrollPane); // 设置引用

            myList.add(messageComponentItem);
        }

        updateLayout();

        scrollToBottomAfterLayout();
    }

    public void initChatList() {
        // 查询db
        Colls colls = collsManager.findLatestOne();

        if (colls == null) {
            // 写入到db
            DBUtil.createCollsAndChats();
            collsIt = collsManager.findLatestOne().getJsonObjectAll();

        }else {
            // 查询db
            collsIt = colls.getJsonObjectAll();
        }

        // 查询db
        chatList = chatsManager.findByCollIdAll(collsIt.get("id").getAsString());

        updateChatList();

        initialLoadComplete = true;
    }


//    private void loadPreviousData() {
//        currentPage++; // 增加页码，获取下一页更旧的数据
//        List<JsonObject> olderChats = chatsManager.findByCollId(collsIt.get("id").getAsString(), currentPage, PAGE_SIZE,false);
//
//        if (!olderChats.isEmpty()) {
//            updateChatList();
//        }
//    }

//    private void loadInitialChatlist() {
//        currentChatList.addAll(chatList.subList(Math.max(chatList.size() - PAGE_SIZE, 0), chatList.size()));
//        System.out.println(currentChatList);
//    }
//
//    private void loadPreviousData() {
//        if (currentIndex == 0) return;
//        int newIndex = Math.max(0, currentIndex - PAGE_SIZE);
//        int loadSize = Math.min(PAGE_SIZE, currentIndex - newIndex);
//        List<JsonObject> newData = chatList.subList(newIndex, newIndex + loadSize);
//        currentChatList.addAll(0, newData);
//        if (currentChatList.size() > MAX_SIZE) {
//            currentChatList.subList(MAX_SIZE, currentChatList.size()).clear(); // 删除尾部
//        }
//        currentIndex = newIndex;
//        System.out.println(currentChatList);
//        updateChatList();
//    }
//
//    private void loadNextData() {
//        if (currentIndex + currentChatList.size() >= chatList.size()) return;
//        int newIndex = Math.min(chatList.size() - 1, currentIndex + PAGE_SIZE);
//        int loadSize = Math.min(PAGE_SIZE, chatList.size() - newIndex);
//        List<JsonObject> newData = chatList.subList(newIndex, newIndex + loadSize);
//        currentChatList.addAll(newData);
//        if (currentChatList.size() > MAX_SIZE) {
//            currentChatList.subList(0, currentChatList.size() - MAX_SIZE).clear(); // 删除头部
//        }
//        currentIndex = newIndex - (Math.max(currentChatList.size() - MAX_SIZE, 0));
//        System.out.println(currentChatList);
//        updateChatList();
//    }

//    private static class ListViewModel extends AbstractListModel<JsonObject> {
//        private final List<JsonObject> list;
//
//        public ListViewModel() {
//            this.list = new ArrayList<>();
//        }
//
//        @Override
//        public int getSize() {
//            return list.size();
//        }
//
//        @Override
//        public JsonObject getElementAt(int index) {
//            return list.get(index);
//        }
//
//        public void addList(JsonObject item) {
//            // 通常新消息添加到末尾
//            int index = list.size();
//            list.add(item);
//            // 通知JList数据在末尾添加了新的元素
//            fireIntervalAdded(this, index, index);
//        }
//    }
//
//    private static class ListViewRender extends JBPanel implements ListCellRenderer<JsonObject> {
//        private MessageComponent messageComponent;
//        private Project projectContext; // 通常渲染器需要引用一些全局上下文
//
//        public ListViewRender(Project project) {
//            this.projectContext = project;
//            // 1. 设置渲染器自身的布局 (通常是 BorderLayout 或 FlowLayout)
//            setLayout(new BorderLayout());
//            setOpaque(true); // 确保背景颜色能显示
//
//            // 2. 在构造函数中，只实例化一个 MessageComponent 实例。
//            //    这个实例会被 JList 反复地复用。
//            messageComponent = new MessageComponent(projectContext, null, null);
//            add(messageComponent, BorderLayout.CENTER);
//        }
//
//        @Override
//        public Component getListCellRendererComponent(JList list, JsonObject value, int index, boolean isSelected, boolean cellHasFocus) {
//            messageComponent.updateContent(value);
//            return this; // 返回自身作为渲染组件
//        }
//    }

    public void initAIFunctionIcon() {
        String panelName = ThisMainPanel.getPanelName();
        String currentModel = AISetInfo.get("aiModel").getAsString();
        JsonArray modelList = DBUtil.getLLMsByKey(ThisMainPanel.getAIKey());

        // 设置底部图片上传icon
        uploadImgButton.setVisible(stateStore.currentModelCanImage(panelName, currentModel, modelList));

        // 设置底部联网icon
        if (stateStore.currentModelCanOnline(panelName,currentModel)) {
            boolean activeOnline = AISetInfo.get("online").getAsBoolean();
            webSearchButton.setVisible(true);
            webSearchButton.setIcon(IconLoader.getIcon(activeOnline ? "/icons/web_search_active.svg" : "/icons/web_search.svg", MainPanel.class));
        }
    }

    public void initAIInfoPanel() {
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setBorder(JBUI.Borders.emptyRight(JBUI.scale(8)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));// 从左到右排列
        chatMatchButton = new IconButton("",AllIcons.Actions.Find);
        chatMatchButton.setToolTipText("Match Content\nCtrl+F");
        chatMatchButton.addActionListener(e -> {
            findMatchDialog.openDialog(ThisMainPanel,getRenderedChatList(),null);
        });
        actionPanel.add(chatMatchButton);

        chatSettingButton = new IconButton("Chat Setting",AllIcons.Actions.RefactoringBulb);
        chatSettingButton.setToolTipText("Chat Setting\nCtrl+D");
        chatSettingButton.addActionListener(e -> {
            new ChatSettingDialog(ThisProject).openDialog((JComponent) ThisMainPanel.getContentPanel().getParent(),AIVendorSet);
        });
        actionPanel.add(chatSettingButton);
        eastPanel.add(actionPanel,BorderLayout.NORTH);

        infoTopPanel.add(eastPanel, BorderLayout.EAST);

        JLabel currentModel = new JLabel();
        String currentModelStr = AISetInfo.get("aiModel").isJsonNull() ? "LLM：" : "LLM：" + AISetInfo.get("aiModel").getAsString();
        currentModel.setText(currentModelStr);
        currentModel.setForeground(new JBColor(Gray.x80, Gray.x8C));
        currentModel.setFont(JBUI.Fonts.create(Font.DIALOG,14));
        currentModel.setBorder(JBUI.Borders.emptyBottom(4));

        currentModel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!AISetInfo.get("aiModel").isJsonNull() && !AISetInfo.get("aiModel").getAsString().isBlank()) {
                    ClipboardUtil.setStr(AISetInfo.get("aiModel").getAsString());
                    BalloonUtil.showBalloon("Copy successfully", MessageType.INFO,currentModel);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                // 当鼠标进入组件区域时，设置光标为手型
                currentModel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // 当鼠标移出组件区域时，恢复光标为原来的值
                currentModel.setCursor(Cursor.getDefaultCursor());
            }
        });

        infoPanel.add(currentModel,BorderLayout.NORTH);

        JPanel southInfoPanel = new JPanel();
        southInfoPanel.setLayout(new BoxLayout(southInfoPanel, BoxLayout.X_AXIS));
        boolean isStream = AISetInfo.get("aiStream").getAsBoolean();
        JLabel streamInfo = new JLabel();
        String streamInfoStr = isStream ? "Stream Speed：" + AISetInfo.get("streamSpeed") : "Stream：Off";
        streamInfo.setText(streamInfoStr);
        streamInfo.setFont(JBUI.Fonts.toolbarFont());
        streamInfo.setForeground(isStream ? new JBColor(Color.decode("#4db2dd"),Color.decode("#4db2dd")) : streamInfo.getForeground());
        southInfoPanel.add(streamInfo);

        JLabel promptsInfo = new JLabel();
        boolean promptsCutIn = AISetInfo.get("promptsCutIn").getAsBoolean();
        promptsInfo.setBorder(JBUI.Borders.emptyLeft(12));
        promptsInfo.setForeground(promptsCutIn ? new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26")) : new JBColor(Gray.x80, Gray.x8C));
        String promptsInfoStr = promptsCutIn ? "Prompts：On" : "Prompts：Off";
        promptsInfo.setText(promptsInfoStr);
        promptsInfo.setFont(JBUI.Fonts.toolbarFont());
        southInfoPanel.add(promptsInfo);

        infoPanel.add(southInfoPanel,BorderLayout.SOUTH);


        infoTopPanel.add(infoPanel,BorderLayout.WEST);
    }

    public void initAiInfo() {
        AIInfo = DBUtil.initAIInfo().deepCopy();

        AIInfo.addProperty("name",ThisMainPanel.getAIName());
        AIInfo.addProperty("icon",ThisMainPanel.getAIIcon());
    }

    public void initMyInfo() {
        MyInfo = DBUtil.initMyInfo().deepCopy();
    }

    public void initAISetInfo() {
        JsonObject settingInfo = stateStore.getAISettingInfo(ThisMainPanel.getPanelName());

        AISetInfo.addProperty("aiModel",settingInfo.get("aiModel").getAsString());
        AISetInfo.addProperty("aiStream",settingInfo.get("aiStream").getAsBoolean());
        AISetInfo.addProperty("online",settingInfo.get("online").getAsBoolean());
        AISetInfo.addProperty("streamSpeed",settingInfo.get("streamSpeed").getAsInt());
        AISetInfo.addProperty("promptsCutIn",settingInfo.get("promptsCutIn").getAsBoolean());
        AISetInfo.add("apiKeys",settingInfo.get("apiKeys").getAsJsonArray());
        AISetOutputInfo = settingInfo.get("outputConf").getAsJsonObject();
        AIPrompts = settingInfo.get("prompts").getAsJsonArray().asList().stream() // 将 JsonArray 转换为 List<JsonElement> 并创建流
                .filter(JsonElement::isJsonObject)   // 过滤出所有是 JsonObject 的元素
                .map(JsonElement::getAsJsonObject)   // 将这些元素强制转换为 JsonObject
                .collect(Collectors.toList());
    }

    public void scrollToBottom() {
        // SwingUtilities.invokeLater(() -> { // 在Swing事件调度线程上执行
        // 这个极快地“绘制错误位置” -> “滚动” -> “绘制正确位置”的过程，在人眼中就表现为一次闪屏。
        //    JScrollBar verticalScrollBar = myScrollPane.getVerticalScrollBar();
        //    int max = verticalScrollBar.getMaximum();
        //     if (max > 0) { // 避免在内容为空的情况下的异常
        //        verticalScrollBar.setValue(max);
        //    }
        //});

        // 请求 myList (内容面板) 将这个矩形区域滚动到可见位置
        Rectangle bottomRect = new Rectangle(0, myList.getHeight() - 1, 1, 1);
        // Swing 会自动计算 JScrollPane 需要滚动多少
        myList.scrollRectToVisible(bottomRect);
    }

    public void scrollToBottomAfterLayout() {
        shouldScrollToBottom = true;
        revalidate();
        repaint();
    }

    public void scrollToTarget(MessageComponent component,int startIndex,int endIndex) {
        component.setScrollToHighlight(component,startIndex,endIndex);
    }

    public void updateLayout() {
        myList.revalidate();
        myList.repaint();
    }

    @Override
    public boolean isNull() {
        return !isVisible();
    }

    private @NotNull IconButton getSettingButton(Class<?> settingPanel) {
        IconButton settingButton = new IconButton("", IconLoader.getIcon("/icons/setting.svg",MainPanel.class));
        settingButton.setToolTipText("Setting\nCtrl+Alt+A");
        settingButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> ShowSettingsUtil.getInstance().showSettingsDialog(ThisProject, MAPPINGS.get(settingPanel.getName())));
        });
        return settingButton;
    }

    private @NotNull IconButton getImageButton() {
        IconButton imageButton = new IconButton("Choose Image", IconLoader.getIcon("/icons/image.svg",MainPanel.class));
        imageButton.addActionListener(e -> handleImageUpload());
        return imageButton;
    }

    private @NotNull IconButton getWebSearchButton() {
        IconButton webButton = new IconButton("Web Search", IconLoader.getIcon("/icons/web_search.svg",MainPanel.class));
        webButton.addActionListener(e -> {
            boolean setStatus = !AISetInfo.get("online").getAsBoolean();
            String setIcon = setStatus ? "/icons/web_search_active.svg" : "/icons/web_search.svg";
            AISetInfo.addProperty("online", setStatus);
            webButton.setIcon(IconLoader.getIcon(setIcon,MainPanel.class));
            // 这里需要写入state
            stateStore.setAISettingInfoByKey(ThisMainPanel.getPanelName(),"online", new JsonPrimitive(setStatus));

            if (setStatus) {
                BalloonUtil.showBalloon("Web search is active!", MessageType.INFO, webButton);
            }
        });
        return webButton;
    }

    private void handleImageUpload() {
        if (!checkImagesLen()) return;
        List<BufferedImage> imageList = ImgUtils.chooseImage(ThisProject);
        if (!imageList.isEmpty()) {
            addImage(imageList);
        }
    }

    private void pasteImageUpload() {
        if (!checkImagesLen() && uploadImgButton.isVisible()) {
            return;
        }
        List<BufferedImage> imageList = inputTextArea.pasteFromClipboardImage();
        addImage(imageList);
    }

    private void handleAddImageFromCache(JsonObject fileData) {
        if (!checkImagesLen()) return;
        if (!fileData.isJsonNull()) {
            String fileName = fileData.get("fileName").getAsString();
            String url = fileData.has("url") ? fileData.get("url").getAsString() : null;
            boolean isAdded = checkImageAdded(fileName);

            if (!isAdded) {
                ImageView imagePanel = new ImageView(null);
                imagePanel.setMessageGroupCom(this); // 加上一个全局引用

                imagePanel.setImage(url,fileName,fileData);

                imagePanel.getDeleteButton().addActionListener(e -> {
                    removeImage(imagePanel);
                });

                uploadListPanel.add(imagePanel);

                uploadListPanel.updateUI();
            }

        }
    }

    public void addImageListFromCache(JsonArray attachments) {
        removeUploadList();

        for (JsonElement attachment: attachments) {
            if (attachment.isJsonObject()) {
                JsonObject attachmentItem = attachment.getAsJsonObject();

                if (attachmentItem.has("type") && "image".equals(attachmentItem.get("type").getAsString())) {

                    String fileName = attachmentItem.get("fileName").getAsString();
                    String url = attachmentItem.has("url") ? attachmentItem.get("url").getAsString() : null;

                    ImageView imagePanel = new ImageView(null);
                    imagePanel.setMessageGroupCom(this); // 加上一个全局引用

                    imagePanel.setImage(url,fileName,attachmentItem);

                    imagePanel.getDeleteButton().addActionListener(e -> {
                        removeImage(imagePanel);
                    });

                    uploadListPanel.add(imagePanel);
                }
            }
        }
        uploadListPanel.updateUI();
    }

    private void addImage(List<BufferedImage> imageList) {
        for (BufferedImage image: imageList) {
            ImageView imagePanel = new ImageView(image);
            imagePanel.setMessageGroupCom(this); // 加上一个全局引用

            doUploadImage(image,imagePanel);

            imagePanel.getDeleteButton().addActionListener(e -> {
                removeImage(imagePanel);
            });

            imagePanel.getReUploadButton().addActionListener(e -> {
                doUploadImage(image,imagePanel);
            });
            uploadListPanel.add(imagePanel);
        }
        uploadListPanel.updateUI();
    }

    private void doUploadImage(Image img,ImageView imagePanel) {
        new SwingWorker<>() {
            @Override
            protected JsonObject doInBackground() {
                imagePanel.setLoading(true);
                return ShineAIUtil.uploadImg(img);
            }
            @Override
            protected void done() {
                try {
                    JsonObject resData = (JsonObject) get(); // 获取 doInBackground() 的结果，如果发生异常，会在这里抛出
                    //
                    System.out.println(resData);

                    String fileName = resData.get("fileName").getAsString();
                    String url = resData.has("url") ? resData.get("url").getAsString() : null;
                    imagePanel.setImage(url,fileName,resData);
                    imagePanel.setLoading(false);
                } catch (InterruptedException | ExecutionException e) {
                    imagePanel.setImage(null,null, null);
                    imagePanel.setLoading(false);
                    System.out.println("doUploadImage error：" + e.getMessage());
                }
            }
        }.execute();
    }

    private Boolean checkImagesLen() {
        if (uploadListPanel.getComponentCount() >= 9) {
            BalloonUtil.showBalloon("Cannot add more images.", MessageType.WARNING,uploadListPanel);
            return false;
        }
        return true;
    }

    private Boolean checkImageAdded(String fileName) {
        boolean isAdded = false;

        if (fileName.isBlank()) {
            BalloonUtil.showBalloon("Add this image error", MessageType.ERROR,uploadListPanel);
            return true;
        }

        for (int i = 0; i < uploadListPanel.getComponentCount(); i++) {
            Component component = uploadListPanel.getComponent(i);
            if (component instanceof ImageView ImageComponent) {
                if (StringUtil.equals(ImageComponent.getName(),fileName)) {
                    isAdded = true;
                    break;
                }
            }
        }

        if (isAdded) {
            BalloonUtil.showBalloon("Please do not add it repeatedly.", MessageType.WARNING,uploadListPanel);
        }

        return isAdded;
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
                    attachmentItem.addProperty("mimeType",ImageComponent.getFileData().get("mimeType").getAsString());
                    attachmentItem.addProperty("url",ImageComponent.getUrl());
                    attachmentItem.add("metaData",ImageComponent.getFileData().getAsJsonObject());
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
        button.setEnabled(!status);
        if (status) {
            setItemsDisabledRerunAndTrash(true);
            disabledCollectionAction(true);
            chatSettingButton.setEnabled(false);
            chatMatchButton.setEnabled(false);
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
            chatMatchButton.setEnabled(true);
            actionSouthPanelActions.remove(stopGenerating);
            actionSouthPanelActions.add(button,BorderLayout.EAST);
        }
        actionPanel.updateUI();
    }

    private void updateActionSouthPanelBg() {
        SwingUtilities.invokeLater(() -> {
            actionSouthPanel.setBackground(UIUtil.getTextFieldBackground());
        });
    }

    private void updateInputTextAreaUI() {
        SwingUtilities.invokeLater(() -> {
            inputTextArea.getTextarea().setUI(new BasicTextAreaUI());
            inputTextArea.clearBorder();
            new InputPlaceholder("Shift + Enter to line-warp, Ctrl + Enter to submit.", inputTextArea.getTextarea());  // 添加 placeholder
        });
    }

    public JsonObject getAISetInfo() {
        return AISetInfo;
    }

    public String getAIVendorKey() {
        return ThisMainPanel.getAIKey();
    }

    public JsonObject getChatCollection () {
        return collsIt;
    }

    public JsonObject getAISetOutputInfo() {
        return AISetOutputInfo;
    }

    public JsonObject getWeightedApikey() {
        if (AISetInfo.has("apiKeys")) {
            JsonArray apiKeys = AISetInfo.get("apiKeys").getAsJsonArray();
            return OtherUtil.weightedRandomTarget(JsonUtil.getListJsonObject(apiKeys));
        }
        return new JsonObject();
    }

    @Override
    public void dispose() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }

        findMatchDialog.dispose();

        removeInfo();
        removeUploadList();
        removeList();
    }

    @Override
    public void onUpdateMessageState(String chatId, JsonObject itemData) {
        updateMessageState(chatId,itemData);
    }

    @Override
    public void onSetProgressBar(boolean isShow) {
        progressBar.setIndeterminate(isShow);
        progressBar.setVisible(isShow);
    }

    @Override
    public void onPreviewImage(String imageName, JsonArray imageList) {
        previewImageDialog.showDialog(imageName,imageList);
    }

    @Override
    public void onAddImageToEdit(JsonObject fileData) {
        handleAddImageFromCache(fileData);
    }
}
