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
import com.shine.ai.db.DBUtil;
import com.shine.ai.db.chats.ChatsManager;
import com.shine.ai.db.colls.Colls;
import com.shine.ai.db.colls.CollsManager;
import com.shine.ai.ui.IconButton;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MyScrollPane;
import com.shine.ai.ui.RoundPanel;
import com.shine.ai.util.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static com.shine.ai.MyToolWindowFactory.ACTIVE_CONTENT;

public class ChatCollectionDialog extends JDialog {
    private final Project project;

    private final JPanel collectionList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane collectionScrollPane = new MyScrollPane(collectionList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    // colls db
    private final CollsManager collsManager = CollsManager.getInstance();

    // chats db
    private final ChatsManager chatsManager = ChatsManager.getInstance();

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
        MainPanel panel = getMainPanel();
        assert panel != null;
        panel.refreshMessages();
    }

    private MainPanel getMainPanel() {
        return (MainPanel) project.getUserData(ACTIVE_CONTENT);
    }

    public void openDialog(JComponent _component) {
        SwingUtilities.invokeLater(() -> { // 在事件调度线程中执行
            ChatCollectionDialog dialog = new ChatCollectionDialog(project);
            dialog.setTitle("Chat Collection");

            Dimension parentSize = _component.getSize();
            if (parentSize.width == 0 || parentSize.height == 0) { //Handle case where component doesn't have size yet
                parentSize = _component.getParent().getSize(); //Try to get size from parent.  Could still be 0,0.
            }

            dialog.setPreferredSize(new Dimension(getMinimumSize().width, (int) (parentSize.height * 0.96)));
            dialog.pack(); //  先调用 pack()
            dialog.setLocationRelativeTo(_component);
            dialog.setVisible(true);
        });
    }

    public void updateLayout() {
        collectionList.revalidate();
        collectionList.repaint();
    }

    public void addNewChat(JComponent _component) {
        if (collsManager.findAllCounts() >= 99) {
            BalloonUtil.showBalloon("Cannot add more chat collection,\nPlease delete previous chat collection at first.", MessageType.WARNING,_component);
        }

        // 写入到db
        DBUtil.createCollsAndChats();

        // 创建元素，插入第一个
        // BalloonUtil.showBalloon("Add chat collection successfully",MessageType.INFO,_component);
    }

    public void createHistoryList() {
        // 查询db
        List<JsonObject> collsList = collsManager.findAll();

        if (!collsList.isEmpty()) {
            for (JsonObject item : collsList) {
                // 添加数量参数
                item.addProperty("chatsCount",chatsManager.findByCollIdCounts(item.get("id").getAsString()));

                CollectionItemComponent collection = new CollectionItemComponent(item);
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
            northPanel.setBorder(JBUI.Borders.empty(4));

            JPanel northLeftPanel = new JPanel(new BorderLayout());
            String titleLabelStr = collectionItem.get("title").getAsString().isEmpty() ? "null" : StringUtil.stringEllipsis(collectionItem.get("title").getAsString(),20);
            JLabel titleLabel = new JLabel(titleLabelStr);
            titleLabel.setBorder(JBUI.Borders.emptyBottom(2));
            titleLabel.setFont(JBUI.Fonts.create("Microsoft YaHei",stateStore.CHAT_PANEL_FONT_SIZE + 1));

            JLabel timeLabel = new JLabel();
            timeLabel.setFont(JBUI.Fonts.smallFont());
            timeLabel.setText(TimeUtil.timeFormat(collectionItem.get("createAt").getAsLong(),null));
            northLeftPanel.add(titleLabel,BorderLayout.NORTH);
            northLeftPanel.add(timeLabel,BorderLayout.SOUTH);
            northPanel.add(northLeftPanel,BorderLayout.WEST);

            JPanel northRightPanel = new JPanel(new BorderLayout());
            JLabel chatCountLabel = new JLabel();
            chatCountLabel.setFont(JBUI.Fonts.label());
            chatCountLabel.setText("total：" + collectionItem.get("chatsCount").getAsInt() + " dialogs");
            northRightPanel.add(chatCountLabel,BorderLayout.CENTER);
            northPanel.add(northRightPanel,BorderLayout.EAST);

            add(northPanel,BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.setBorder(JBUI.Borders.empty(4));

            RoundPanel contentTextPanel = new RoundPanel(new BorderLayout());
            contentTextPanel.setBackground(new JBColor(Color.decode("#f1f1e1"), Color.decode("#f1f1f1")));
            contentTextPanel.setMaximumSize(new Dimension(contentTextPanel.getWidth(),128));
            String contentTextAreaStr = collectionItem.get("subTitle").getAsString().isEmpty() ? "null" : StringUtil.stringEllipsis(collectionItem.get("subTitle").getAsString(), 192);
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
            // 查询db
            Colls colls = collsManager.findLatestOne();

            if (colls != null && StringUtil.equals(colls.getJsonObjectAll().get("id").getAsString(), item.get("id").getAsString())) {
                BalloonUtil.showBalloon("Open fail：Current chat collection already opened.",MessageType.WARNING,component);
                return;
            }

            // 写入到db
            DBUtil.updateCollsById(item.get("id").getAsString());

            refreshMessages();
            dispose();
        });
        return openAction;
    }

    private @NotNull IconButton getDeleteAction(JComponent component, JsonObject item) {
        IconButton deleteAction = new IconButton("delete",AllIcons.Actions.GC);
        deleteAction.addActionListener(e -> {
            // 查询db
            Colls colls = collsManager.findLatestOne();

            boolean yes = MessageDialogBuilder.yesNo("Are you sure you want to delete this chat?",
                            "There will be delete this dialogs.")
                    .yesText("Yes")
                    .noText("No").ask(component);
            if (yes) {
                if (colls != null && StringUtil.equals(colls.getJsonObjectAll().get("id").getAsString(), item.get("id").getAsString())) {
                    BalloonUtil.showBalloon("Delete fail：Can not delete already opened chat collection.",MessageType.ERROR,component);
                    return;
                }
                BalloonUtil.showBalloon("Delete successfully",MessageType.INFO,ListScrollPanel);

                // 写入到db
                DBUtil.delCollsById(item.get("id").getAsString());

                collectionList.remove(component);

                updateLayout();
            }
        });
        return deleteAction;
    }

    private @NotNull IconButton getExportAction(JComponent component, JsonObject item) {
        IconButton exportAction = new IconButton("export",AllIcons.Actions.Upload);
        exportAction.addActionListener(e -> {
            String collTitle = item.get("title").getAsString();
            String collUpdateTime = TimeUtil.timeFormat(item.get("updateAt").getAsLong(),"YYYYMMDDHHmmss");

            // 查询db
            List<JsonObject> chatList = chatsManager.findByCollIdAll(item.get("id").getAsString());

            // 整合数据
            JsonObject exportData = new JsonObject();
            exportData.add("chatList", JsonUtil.getJsonArray(chatList));
            JsonObject targetExportData = JsonUtil.mergeJsonObject(item,exportData);

            assert collTitle != null;
            String fileName = !collTitle.isEmpty() ? collTitle.substring(0, Math.min(collTitle.length(), 20)) + collUpdateTime : item.get("id").getAsString() + collUpdateTime;
            FileUtil.exportToJson(targetExportData,fileName,component);
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
            setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
            setForeground(new JBColor(Color.decode("#060606"), Color.decode("#000000")));
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
