package com.shine.ai.settings;

import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.ui.IconButton;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MyScrollPane;
import com.shine.ai.ui.RoundPanel;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.FileUtil;
import com.shine.ai.util.StringUtil;
import com.shine.ai.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.Objects;

import static com.shine.ai.MyToolWindowFactory.ACTIVE_CONTENT;

public class ChatCollectionDialog extends JDialog {
    private final Project project;

    private final JPanel collectionList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane collectionScrollPane = new MyScrollPane(collectionList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private JPanel contentPane;
    private JPanel addChatTitledBorderBox;
    private JPanel openChatTitledBorderBox;
    private JButton openNewChatButton;
    private JLabel openNewChatHelpLabel;
    private JLabel openHistoryChatHelpLabel;
    private JPanel ListScrollPanel;

    public ChatCollectionDialog(Project project) {
        this.project = project;
        init();
        createHistoryList();
    }

    private void init() {
        setContentPane(contentPane);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 设置关闭操作
        contentPane.setBorder(JBUI.Borders.empty(12,32,32,32));

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assert openNewChatButton != null;
        openNewChatButton.setIcon(AllIcons.General.Add);
        openNewChatButton.addActionListener(e -> {
            addNewChat(openNewChatButton);
            refreshMessages();
            dispose();
        });

        collectionScrollPane.setBorder(JBUI.Borders.empty());
        ListScrollPanel.add(collectionScrollPane);
        collectionScrollPane.getVerticalScrollBar().setAutoscrolls(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
            @Override
            public void windowOpened(WindowEvent e) {
//                requestFocusInWindow(); // 将焦点设置到对话框本身
//                transferFocus();       // 立即移除焦点
                collectionScrollPane.getVerticalScrollBar().setValue(0);
            }
        });

        initHelp();
    }

    private void refreshMessages() {
        MainPanel panel = (MainPanel) project.getUserData(ACTIVE_CONTENT);
        assert panel != null;
        panel.refreshMessages();
    }

    public void openDialog(JComponent _component) {
        SwingUtilities.invokeLater(() -> { // 在事件调度线程中执行
            ChatCollectionDialog dialog = new ChatCollectionDialog(project);
            dialog.setTitle("Chat Collection");

            Dimension parentSize = _component.getSize();
            if (parentSize.width == 0 || parentSize.height == 0) { //Handle case where component doesn't have size yet
                parentSize = _component.getParent().getSize(); //Try to get size from parent.  Could still be 0,0.
            }

            dialog.setPreferredSize(new Dimension(getMinimumSize().width,(int) (parentSize.height * 0.8)));
            dialog.pack(); //  先调用 pack()
            dialog.setLocationRelativeTo(_component);
            dialog.setVisible(true);
        });
    }

    public void updateLayout() {
        LayoutManager layout = collectionList.getLayout();
        int componentCount = collectionList.getComponentCount();
        for (int i = 0 ; i< componentCount ; i++) {
            layout.removeLayoutComponent(collectionList.getComponent(i));
            layout.addLayoutComponent(null,collectionList.getComponent(i));
        }
        collectionList.revalidate();
        collectionList.repaint();
    }

    public void addNewChat(JComponent _component) {
        if (stateStore.AIChatCollection.size() >= 99) {
            BalloonUtil.showBalloon("无法再新增聊天，请先删除旧的聊天", MessageType.ERROR,_component);
        }
        stateStore.AIChatCollection.add(0,stateStore.createChatCollection());
        // 创建元素，插入第一个
        BalloonUtil.showBalloon("新增聊天成功",MessageType.INFO,_component);
    }

    public void createHistoryList() {
        if (!stateStore.AIChatCollection.isEmpty()) {
            for (String item : stateStore.AIChatCollection) {
                CollectionItemComponent collection = new CollectionItemComponent(stateStore.getJsonObject(item));
                collectionList.add(collection);
            }
            updateLayout();
        }
    }

    public class CollectionItemComponent extends JBPanel<CollectionItemComponent> {
        private JsonObject collectionItem;
        public CollectionItemComponent(JsonObject collection) {
            this.collectionItem = collection;
            setDoubleBuffered(true);
            setOpaque(true);
            setBorder(JBUI.Borders.empty(10));
            setLayout(new BorderLayout(JBUI.scale(7), 0));

            JPanel northPanel = new JPanel(new BorderLayout());
            northPanel.setOpaque(false);
            northPanel.setBorder(JBUI.Borders.empty(6));

            JPanel northLeftPanel = new JPanel(new BorderLayout());
            String titleLabelStr = collectionItem.get("collectionTitle").getAsString().isEmpty() ? "null" : StringUtil.stringEllipsis(collectionItem.get("collectionTitle").getAsString(),20);
            JLabel titleLabel = new JLabel(titleLabelStr);
            titleLabel.setFont(JBUI.Fonts.create("Microsoft YaHei",stateStore.CHAT_PANEL_FONT_SIZE + 1));

            JLabel timeLabel = new JLabel();
            timeLabel.setFont(JBUI.Fonts.smallFont());
            timeLabel.setText(TimeUtil.timeFormat(collectionItem.get("createat").getAsLong(),null));
            northLeftPanel.add(titleLabel,BorderLayout.NORTH);
            northLeftPanel.add(timeLabel,BorderLayout.SOUTH);
            northPanel.add(northLeftPanel,BorderLayout.WEST);

            JPanel northRightPanel = new JPanel(new BorderLayout());
            JLabel chatCountLabel = new JLabel();
            chatCountLabel.setFont(JBUI.Fonts.label());
            chatCountLabel.setText("total：" + collectionItem.get("chatList").getAsJsonArray().size() + " dialogs");
            northRightPanel.add(chatCountLabel,BorderLayout.NORTH);
            northPanel.add(northRightPanel,BorderLayout.EAST);

            add(northPanel,BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.setBorder(JBUI.Borders.empty(6));

            RoundPanel contentTextPanel = new RoundPanel(new BorderLayout());
            contentTextPanel.setBackground(new JBColor(Color.decode("#5a6775"), Color.decode("#f1f1f1")));
            String contentTextAreaStr = collectionItem.get("collectionSubTitle").getAsString().isEmpty() ? "null" : StringUtil.stringEllipsis(collectionItem.get("collectionSubTitle").getAsString(), 192);
            LimitedTextAreaV contentTextArea = new LimitedTextAreaV(contentTextAreaStr);
            contentTextPanel.add(contentTextArea);

            centerPanel.add(contentTextPanel,BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);

            JPanel actionPanel = new JPanel(new BorderLayout());
            actionPanel.setOpaque(false);
            actionPanel.setBorder(JBUI.Borders.empty());

            JPanel actionLeftPanel = new JPanel(new BorderLayout());
            FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
            actionLeftPanel.setLayout(flowLayout); // 从左到右排列

            IconButton editAction = getDeleteAction(this, collectionItem);
            actionLeftPanel.add(editAction);
            IconButton exportAction = getExportAction(this, collectionItem);
            actionLeftPanel.add(exportAction);
            IconButton openAction = getOpenAction(this, collectionItem);
            actionLeftPanel.add(openAction);

            actionPanel.add(actionLeftPanel,BorderLayout.EAST);
            add(actionPanel,BorderLayout.SOUTH);
        }
    }

    private @NotNull IconButton getOpenAction(JComponent component, JsonObject item) {
        IconButton openAction = new IconButton("open",AllIcons.General.OpenInToolWindow);
        openAction.addActionListener(e -> {
            JsonObject firstCollectionItem = stateStore.getJsonObject(stateStore.AIChatCollection.get(0));
            if (Objects.equals(firstCollectionItem.get("collId").getAsString(), item.get("collId").getAsString())) {
                BalloonUtil.showBalloon("Open fail：Current chat collection already opened.",MessageType.WARNING,collectionList);
                return;
            }
            for (int i = 0; i < stateStore.AIChatCollection.size(); i++) {
                JsonObject currentCollection = stateStore.getJsonObject(stateStore.AIChatCollection.get(i));
                if (Objects.equals(currentCollection.get("collId").getAsString(), item.get("collId").getAsString())) {
                    stateStore.AIChatCollection.set(i, stateStore.getJsonString(currentCollection)); // 使用 set() 方法修改列表元素
                    stateStore.updateChatCollectionInfo(currentCollection);
                    break; // 可选：如果 promptId 是唯一的，找到后可以跳出循环
                }
            }
            refreshMessages();
            dispose();
        });
        return openAction;
    }

    private @NotNull IconButton getDeleteAction(JComponent component, JsonObject item) {
        IconButton deleteAction = new IconButton("delete",AllIcons.Actions.GC);
        deleteAction.addActionListener(e -> {
            JsonObject firstCollectionItem = stateStore.getJsonObject(stateStore.AIChatCollection.get(0));
            boolean yes = MessageDialogBuilder.yesNo("Are you sure you want to delete this chat?",
                            "There will be delete this dialogs.")
                    .yesText("Yes")
                    .noText("No").ask(component);
            if (yes) {
                if (Objects.equals(firstCollectionItem.get("collId").getAsString(), item.get("collId").getAsString())) {
                    BalloonUtil.showBalloon("Delete fail：Can not delete already opened chat collection.",MessageType.ERROR,collectionList);
                    return;
                }
                BalloonUtil.showBalloon("Delete successfully",MessageType.INFO,collectionList);
                stateStore.deleteChatCollectionById(item.get("collId").getAsString());
                stateStore.updateChatCollectionInfo(item);
                collectionList.remove(component);
                updateLayout();
            }
        });
        return deleteAction;
    }

    private @NotNull IconButton getExportAction(JComponent component, JsonObject item) {
        IconButton exportAction = new IconButton("export",AllIcons.Actions.Upload);
        exportAction.addActionListener(e -> {
            String collTitle = item.get("collectionTitle").getAsString();
            String collUpdateTime = TimeUtil.timeFormat(item.get("updateat").getAsLong(),"YYYYMMDDHHmmss");
            assert collTitle != null;
            String fileName = !collTitle.isEmpty() ? collTitle.substring(0,5) + collUpdateTime : item.get("collId").getAsString() + collUpdateTime;
            Path result = FileUtil.exportToJson(item,fileName);
            String popupContent = "Export successfully，Path：" + result.toString();
            BalloonUtil.showBalloon(popupContent,MessageType.INFO,component);
        });
        return exportAction;
    }

    public static class LimitedTextArea extends JTextArea {
        private final Document doc;
        private final int maxLines = 4;
        public LimitedTextArea(String content) {
            super();
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
            setEnabled(false);
            doc = getDocument();
            doc.addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { checkLength(); }
                @Override
                public void removeUpdate(DocumentEvent e) { checkLength(); }
                @Override
                public void changedUpdate(DocumentEvent e) { checkLength(); }

            });
            setText(content);
            SwingUtilities.invokeLater(LimitedTextArea.this::checkLength);
        }
        private void checkLength() {
            try {
                int numLines = doc.getDefaultRootElement().getElementCount();
                System.out.println("checkLength numLines " + numLines);
                if (numLines > maxLines) {
                    Element root = doc.getDefaultRootElement();
                    int start = root.getElement(maxLines - 1).getEndOffset(); // 改进：从最后一行末尾开始删除
                    int end = doc.getLength();
                    doc.remove(start, end - start);
                    append("...");
                }
            } catch (BadLocationException ex) {
                System.out.println("BadLocationException" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public class LimitedTextAreaV extends JTextArea {
        public LimitedTextAreaV(String content) {
            setEditable(false); // 启用自动换行
            setOpaque(false); // 按单词换行
            setBorder(JBUI.Borders.empty(6));
            setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(Color.decode("#f1f1f1"), Color.decode("#000000"))));
            setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
            setLineWrap(true);
            setWrapStyleWord(true);
            setText(content);
        }
    }

    public static class TruncatedLabel extends JLabel {
        public TruncatedLabel(String text) {
            super("<html><div style='width: 50%; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;'>" + text + "</div></html>");
        }
    }

    private void createUIComponents() {
        addChatTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator stBt = new TitledSeparator("Open New Chat");
        addChatTitledBorderBox.add(stBt,BorderLayout.CENTER);

        openChatTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator mdModel = new TitledSeparator("History Chat Collection");
        openChatTitledBorderBox.add(mdModel,BorderLayout.CENTER);
    }

    public void initHelp() {
        openNewChatHelpLabel.setFont(JBUI.Fonts.smallFont());
        openNewChatHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        openHistoryChatHelpLabel.setFont(JBUI.Fonts.smallFont());
        openHistoryChatHelpLabel.setForeground(UIUtil.getContextHelpForeground());
    }
}
