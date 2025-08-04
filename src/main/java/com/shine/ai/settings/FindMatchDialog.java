package com.shine.ai.settings;

import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.JBColor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.ui.help.SearchRegexHandler;
import com.shine.ai.ui.*;
import com.shine.ai.util.BalloonUtil;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FindMatchDialog extends JDialog {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private JPanel contentPane; // 声明了，但需要在 init() 中初始化

    private final RoundPanel controlPanel = new RoundPanel();
    private JLabel countsLabel;

    private SingleLineInput matchInput;

    private IconButton prevButton;
    private IconButton nextButton;
    private IconButton closeButton;

    private MainPanel mainPanel; // 父组件，用于定位

    // 内容匹配器
    private final SearchRegexHandler searchRegexHandler = new SearchRegexHandler();

    private String matchPatten = "";
    private List<JsonObject> searchList = new ArrayList<>();
    private List<JsonObject> matchList = new ArrayList<>();

    private int currentIndex = -1;

    private int lastIndex = 0;

    // 声明一个共享的 Timer 实例作为成员变量，只需创建一次
    private final Timer sharedDebounceTimer = new Timer(true); // true 表示是守护线程

    private TimerTask currentRenderTask; // 用于追踪当前任务，以便取消

    public FindMatchDialog() {
        init();
    }

    private void init() {
        setContentPane(contentPane);

        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true); // 移除窗口边框和标题栏，实现自定义外观
        // 将对话框自身背景设为透明
        setBackground(new JBColor(new Color(0, 0, 0, 0), new Color(0,0,0,0))); // R G B A (透明度为0)

        // 设置对话框的最小尺寸 (高度至少48)
        setMinimumSize(new Dimension(400, 64)); // 可以适当设置一个最小宽度，例如300

        contentPane.setLayout(new BorderLayout());
        contentPane.setOpaque(false);
        contentPane.setBorder(null);

        // Escape 键关闭对话框
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) { hideDialog(); }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // *** 关键修改：使用 GridBagLayout ***
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setOpaque(false);
        controlPanel.setArc(8,8); // JBPanel 特有方法

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; // 所有组件都在第一行 (row 0)
        gbc.insets = JBUI.insets(2, 4); // 为所有组件设置默认的四周边距 (top, left, bottom, right)
        gbc.anchor = GridBagConstraints.CENTER; // 将所有组件在它们的单元格内垂直居中对齐

        // 1. matchInput (输入框)
        matchInput = new SingleLineInput();
        matchInput.getTextField().setFont(new Font("Microsoft YaHei", Font.PLAIN, stateStore.CHAT_PANEL_FONT_SIZE));
        matchInput.setMinimumSize(new Dimension(getMinimumSize().width,getMinimumSize().height));
        matchInput.setPreferredSize(new Dimension(128,getMinimumSize().height - 16));

        gbc.gridx = 0; // 第一列
        gbc.weightx = 1.0; // 占据所有剩余的水平空间 (动态宽度)
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充其单元格
        controlPanel.add(matchInput, gbc);

        // 2. countsLabel
        countsLabel = new JLabel("0/0");
        countsLabel.setForeground(JBColor.GRAY);
        countsLabel.setPreferredSize(new Dimension(64,countsLabel.getPreferredSize().height));
        gbc.gridx = 1; // 第二列
        gbc.weightx = 0.0; // 固定宽度
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持其首选大小
        controlPanel.add(countsLabel, gbc);

        // 3. prevButton (固定宽度)
        prevButton = new IconButton("", AllIcons.Chooser.Top);
        prevButton.setToolTipText("Prev Matched\nCtrl+Up");
        prevButton.addActionListener(e -> setPrevMatched());
        gbc.gridx = 2; // 第三列
        gbc.weightx = 0.0; // 固定宽度
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持其首选大小
        controlPanel.add(prevButton, gbc);

        // 4. nextButton (固定宽度)
        nextButton = new IconButton("", AllIcons.Chooser.Bottom);
        nextButton.setToolTipText("Next Matched\nCtrl+Down");
        nextButton.addActionListener(e -> setNextMatched());
        gbc.gridx = 3; // 第四列
        gbc.weightx = 0.0; // 固定宽度
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持其首选大小
        controlPanel.add(nextButton, gbc);

        // 5. closeButton (固定宽度)
        closeButton = new IconButton("", AllIcons.Actions.Cancel);
        closeButton.setToolTipText("Close\nEsc");
        closeButton.addActionListener(e -> hideDialog());
        gbc.gridx = 4; // 第五列
        gbc.weightx = 0.0; // 固定宽度
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持其首选大小
        controlPanel.add(closeButton, gbc);

        contentPane.add(controlPanel, BorderLayout.CENTER);

        // 更新背景
        updateControlPanelUI();

        pack(); // 第一次 pack() 用于计算对话框的初始首选大小 初始位置设置（此时 messageGroupComponent 可能为 null，所以可能在 (0,0)）
        // 实际位置会在 showDialog() 中更新
        setWindowLocation();

        setVisible(false); // 初始状态为隐藏

        // 允许拖动自定义窗口 (鼠标监听器)
        // 原始代码中鼠标监听器有重复定义，合并为一个
        MouseAdapter dragListener = new MouseAdapter() {
            private Point mouseClickPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseClickPoint != null) {
                    Point newPoint = new Point(e.getLocationOnScreen().x - mouseClickPoint.x,
                            e.getLocationOnScreen().y - mouseClickPoint.y);
                    setLocation(newPoint);
                }
            }
        };
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);


        // 窗口焦点丢失时隐藏对话框
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                // 确保只有在对话框可见时才隐藏，避免不必要的隐藏
                hideDialog();
            }
            @Override
            public void windowDeactivated(WindowEvent e) {
                hideDialog();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                hideDialog();
            }
        });

        matchInput.getTextField().getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                debouncedDoSearch();

                lastIndex = 0;
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                debouncedDoSearch();

                lastIndex = 0;
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        // 禁用回车换行
        matchInput.getTextField().addActionListener(e -> {
            // 可以在这里添加回车键触发的事件
            setNextMatched();
        });

        matchInput.getClearButton().addActionListener(e -> {
            debouncedDoSearch();
        });


        // 1. 定义动作 (Action)
        // Action for Previous Image
        Action prevMatchedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPrevMatched(); // 调用实际的逻辑
            }
        };

        // Action for Next Image
        Action nextMatchedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNextMatched(); // 调用实际的逻辑
            }
        };

        InputMap inputMap = controlPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = controlPanel.getActionMap();

        // 创建 KeyStroke 并关联到 Action
        KeyStroke upArrow = KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK);
        inputMap.put(upArrow, "prevMatched"); // "prevImage" 是一个任意的字符串键
        actionMap.put("prevMatched", prevMatchedAction);

        KeyStroke downArrow = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK);
        inputMap.put(downArrow, "nextMatched"); // "prevImage" 是一个任意的字符串键
        actionMap.put("nextMatched", nextMatchedAction);

        // 获取 Application-level 的 MessageBus 连接，并与当前 Disposable 绑定
        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(controlPanel);

        // 订阅 LafManagerListener.TOPIC
        messageBusConnection.subscribe(LafManagerListener.TOPIC, new LafManagerListener() {
            @Override
            public void lookAndFeelChanged(@NotNull LafManager source) {
                updateControlPanelUI(); // 主题变化时更新背景
            }
        });
    }

    /**
     * 根据 messageGroupComponent 调整对话框位置和最大宽度。
     * 每次对话框显示前都应调用。
     */
    private void setWindowLocation() {
        if (mainPanel != null) {
            JPanel mainContentPanel = mainPanel.getContent();
            MessageGroupComponent messageGroupComponent = mainPanel.getContentPanel();

            // 获取组件在屏幕上的绝对位置
            Point componentLocation = mainContentPanel.getLocationOnScreen();

            // 计算最大宽度并应用，确保 pack() 或布局管理器会尊重它
            int maxWidth = (int) (mainContentPanel.getWidth() * 0.86);
            // Integer.MAX_VALUE 确保高度不受最大值限制
            setPreferredSize(new Dimension(maxWidth, getHeight()));

            // 重新 pack() 以便 dialogue 能够根据新的最大宽度调整自身大小
            // 如果 contentPane 的内容尺寸可能会动态变化，这里调用 pack() 是必要的
            // 否则，在 init() 中调用一次就足够了
            pack(); // 重新计算尺寸以适应 maxWidth

            // 计算对话框位置（组件顶部居中）
            int dialogX = componentLocation.x + (mainContentPanel.getWidth() - getWidth()) / 2;
            int dialogY = componentLocation.y + (messageGroupComponent.infoTopPanel != null ? messageGroupComponent.infoTopPanel.getHeight() : 0);

            // 确保对话框不会超出屏幕顶部，如果超出则放在组件下方
            GraphicsConfiguration gc = getGraphicsConfiguration(); // 使用对话框自己的GraphicsConfiguration
            if (gc != null) {
                Rectangle screenBounds = gc.getBounds();
                if (dialogY < screenBounds.y) {
                    dialogY = componentLocation.y + messageGroupComponent.getHeight(); // 放在组件下方
                }
            }


            setLocation(dialogX, dialogY);
        }
    }

    // 执行搜索逻辑
    public void doSearch() {
        matchPatten = matchInput.getContent();

        matchList = searchRegexHandler.searchList(searchList, matchPatten);

        if (matchList.isEmpty()) {
            currentIndex = -1;
            lastIndex = 0;
        }else {
            currentIndex = lastIndex;
        }

        setMatched(currentIndex);
    }

    /**
     * 显示对话框并设置其内容和位置。
     * @param mainPanel 父级 MainPanel, 用于定位
     * @param list 搜索列表
     */
    public void openDialog(MainPanel mainPanel, List<JsonObject> list, String matchPatten) {
        this.mainPanel = mainPanel;
        this.searchList = list;

        if (matchPatten != null && !matchPatten.isBlank()) {
            if (!matchPatten.equals(matchInput.getContent())) {
                matchInput.setContent(matchPatten);
            }
        }

        // 在 EDT (Event Dispatch Thread) 上执行 UI 更新和搜索逻辑
        SwingUtilities.invokeLater(() -> {

            // 执行搜索，可能更新UI
            doSearch();

            // 在显示前，再次设置位置和尺寸，确保是基于最新的父组件信息
            setWindowLocation();

            // 设置可见
            setVisible(true);

            // 确保输入框获得焦点
            matchInput.getTextField().requestFocusInWindow();
        });
    }

    private void setPrevMatched() {
        if (matchList.isEmpty()) return;

        currentIndex --;

        if (currentIndex < 0) currentIndex = matchList.size() - 1;

        lastIndex = currentIndex;

        setMatched(currentIndex);
    }

    private void setNextMatched() {
        if (matchList.isEmpty()) return;

        currentIndex ++;

        if (currentIndex > matchList.size() - 1) currentIndex = 0;

        lastIndex = currentIndex;

        setMatched(currentIndex);
    }

    private void setMatched(int index) {
        // 1. 先清除所有高亮（所有组件都清空，不只是当前组件）
        clearAllHighlights();

        if (index >= 0 && index < matchList.size()) {
            MessageGroupComponent messageGroupComponent = mainPanel.getContentPanel();

            // 2. 遍历所有组件，为所有匹配加普通高亮
            messageGroupComponent.highlightsAll(matchList,index);

            // 3. 滚动到当前选中的项
            String matchedId = matchList.get(index).get("id").getAsString();
            int matchedStartIndex = matchList.get(index).get("matchStartIndex").getAsInt();
            int matchEndIndex = matchList.get(index).get("matchEndIndex").getAsInt();
            MessageComponent matchedItem = messageGroupComponent.getItemById(matchedId);

            if (matchedItem != null) {
                messageGroupComponent.scrollToTarget(matchedItem,matchedStartIndex,matchEndIndex);
            } else {
                BalloonUtil.showBalloon("Matched load error!", MessageType.ERROR, this);
            }
        }

        updateCountsLabel(index);
    }

    private void updateCountsLabel(int currentIdx) {
        if (currentIdx<0 && !matchPatten.isBlank()) {
            countsLabel.setText("0 results");
            countsLabel.setForeground(JBColor.RED);
        }else {
            String countsLabelStr = (currentIdx + 1) + "/" + this.matchList.size();
            countsLabel.setText(countsLabelStr);
            countsLabel.setForeground(JBColor.GRAY);
        }
    }

    public void hideDialog() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            clearAllHighlights();
        });
    }

    private void clearAllHighlights() {
        if (mainPanel != null) {
            MessageGroupComponent messageGroupComponent = mainPanel.getContentPanel();

            messageGroupComponent.clearAllHighLights();

            cancelPendingUpdate();
        }
    }

    private synchronized void debouncedDoSearch() {
        if (currentRenderTask != null) {
            currentRenderTask.cancel(); // 取消上一个任务
        }
        currentRenderTask = new TimerTask() {
            @Override
            public void run() {
                doSearch();
                currentRenderTask = null; // 任务完成后清空引用
            }
        };
        sharedDebounceTimer.schedule(currentRenderTask, 32);
    }

    /**
     * 手动取消任何正在等待执行的防抖更新任务。
     */
    public synchronized void cancelPendingUpdate() {
        if (currentRenderTask != null) {
            currentRenderTask.cancel();
            currentRenderTask = null;
        }
    }

    // 阶梯防抖间隔
    private int getDebounceDelay(int contentLength) {
        if (contentLength < 500) return 0;
        if (contentLength < 1000) return 6;
        if (contentLength < 5000) return 12;
        if (contentLength < 10000) return 32;
        if (contentLength < 30000) return 48;
        if (contentLength < 50000) return 64;
        return 128;
    }

    private void updateControlPanelUI() {
        SwingUtilities.invokeLater(() -> {
            matchInput.clearBorder();
            controlPanel.setBackground(UIUtil.getTextFieldBackground());
            new InputPlaceholder("Search", matchInput.getTextField());  // 添加 placeholder
        });
    }
 }
