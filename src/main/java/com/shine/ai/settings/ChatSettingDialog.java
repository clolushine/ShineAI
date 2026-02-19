/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.shine.ai.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.ui.BubbleButton;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MyScrollPane;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.shine.ai.MyToolWindowFactory.ACTIVE_CONTENT;

public class ChatSettingDialog extends JDialog {
    private final AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

    private final Project project;
    private static Class<?> settingPanel;
    private static JsonObject SetOutputConf;
    private static Boolean enablePrompts;
    private static List<JsonObject> thisPrompts;

    private final JPanel promptList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane promptScrollPane = new MyScrollPane(promptList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private JPanel upStreamTitledBorderBox;
    private JPanel downStreamTitledBorderBox;
    private JLabel temperatureHelpLabel;
    private JSpinner temperatureSpinner;
    private JSpinner topPSpinner;
    private JSpinner topKSpinner;
    private JSpinner freqPSpinner;
    private JSpinner presPSpinner;
    private JSlider maxTokenSlider;
    private JSlider historyLengthSlider;
    private JSlider zipHistoryBySizeSlider;
    private JLabel topPHelpLabel;
    private JLabel topKHelpLabel;
    private JLabel freqPHelpLabel;
    private JLabel presPHelpLabel;
    private JLabel maxTokensHelpLabel;
    private JLabel historyLengthHelpLabel;
    private JLabel zipHistoryBySizeHelpLabel;
    private JCheckBox enableZipHistoryBySize;
    private JLabel maxTokensValueLabel;
    private JLabel historyLengthValueLabel;
    private JLabel zipHistoryBySizeValueLabel;
    private JButton resetSetOutputConfButton;
    private JLabel setDefaultHelpLabel;
    private JCheckBox enablePromptsCheckBox;
    private JLabel promptsHelpLabel;
    private JButton addPromptButton;
    private JPanel promptsListPanel;
    private JLabel promptsCountLabel;
    private JLabel promptsDataHelpLabel;

    public ChatSettingDialog(Project project) {
        this.project = project;
        init();
    }

    private void onOK() {
        // add your code here
        apply();
        dispose();
        getMainPanel().refreshInfo();
        // 刷新显示状态
        getMainPanel().getPromptsPanel().refreshStatus();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
        // 刷新显示状态
        getMainPanel().getPromptsPanel().refreshStatus();
    }

    private MainPanel getMainPanel() {
        return (MainPanel) project.getUserData(ACTIVE_CONTENT);
    }

    private void init() {
        setContentPane(contentPane);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 设置关闭操作
        getRootPane().setDefaultButton(buttonOK);
        contentPane.setBorder(JBUI.Borders.empty(6,16,16,16));

        assert promptsListPanel != null;
        promptsListPanel.setPreferredSize(new Dimension(promptsListPanel.getWidth(),192));
        promptScrollPane.setBorder(JBUI.Borders.empty());
        promptsListPanel.add(promptScrollPane);
        promptScrollPane.getVerticalScrollBar().setAutoscrolls(true);

        SpinnerNumberModel temperatureModel = new SpinnerNumberModel(0.0, 0.0, 2.0, 0.1);
        SpinnerNumberModel topPModel = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
        SpinnerNumberModel topKModel = new SpinnerNumberModel(1, 1, 50, 1);
        SpinnerNumberModel freqPModel = new SpinnerNumberModel(0.0, -2.0, 2.0, 0.1);
        SpinnerNumberModel presPModel = new SpinnerNumberModel(0.0, -2.0, 2.0, 0.1);

        assert temperatureSpinner != null;
        temperatureSpinner.setModel(temperatureModel);

        assert addPromptButton!= null;
        addPromptButton.setIcon(AllIcons.General.Add);
        addPromptButton.addActionListener(e -> {
            addNewPrompt();
        });

        promptList.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                promptsCountLabel.setText("total：" + promptList.getComponentCount() + " prompts");
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                promptsCountLabel.setText("total：" + promptList.getComponentCount() + " prompts");
            }
        });

        assert enablePromptsCheckBox!= null;
        enablePromptsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePrompts = enablePromptsCheckBox.isSelected();
            }
        });

        assert topPSpinner != null;
        topPSpinner.setModel(topPModel);

        assert topKSpinner != null;
        topKSpinner.setModel(topKModel);

        assert freqPSpinner != null;
        freqPSpinner.setModel(freqPModel);

        assert presPSpinner != null;
        presPSpinner.setModel(presPModel);

        assert buttonOK != null;
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        assert buttonCancel != null;
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        assert resetSetOutputConfButton != null;
        resetSetOutputConfButton.setIcon(AllIcons.General.Reset);
        resetSetOutputConfButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean yes = MessageDialogBuilder.yesNo("Are you sure you want to reset?",
                                "There will be reset downstream dialog default setting.")
                        .yesText("Yes")
                        .noText("No").ask(resetSetOutputConfButton);
                if (yes) {
                    resetByDefault();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
            @Override
            public void windowOpened(WindowEvent e) {
//                requestFocusInWindow(); // 将焦点设置到对话框本身
//                transferFocus();       // 立即移除焦点
                reset(); // 在对话框显示时重置为默认值
                initPromptsPanel();
            }
        });

        // call onCancel() on ESCAPE
        assert contentPane != null;
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assert maxTokenSlider != null;
        maxTokenSlider.addChangeListener(e -> {
            maxTokensValueLabel.setText(String.valueOf(maxTokenSlider.getValue()));
        });

        assert historyLengthSlider != null;
        historyLengthSlider.addChangeListener(e -> {
            historyLengthValueLabel.setText(String.valueOf(historyLengthSlider.getValue()));
        });

        assert zipHistoryBySizeSlider != null;
        zipHistoryBySizeSlider.addChangeListener(e -> {
            zipHistoryBySizeValueLabel.setText(String.valueOf(zipHistoryBySizeSlider.getValue()));
        });

        assert enableZipHistoryBySize != null;
        enableZipHistoryBySize.addChangeListener(e -> {
            zipHistoryBySizeSlider.setEnabled(enableZipHistoryBySize.isSelected());
        });

        enableZipHistoryBySize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zipHistoryBySizeSlider.setEnabled(enableZipHistoryBySize.isSelected());
            }
        });

        initHelp();
    }

    private void createUIComponents() {
        upStreamTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator stBt = new TitledSeparator(MsgEntryBundle.message("ui.setting.chat.upstream.title"));
        upStreamTitledBorderBox.add(stBt,BorderLayout.CENTER);

        downStreamTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator mdModel = new TitledSeparator(MsgEntryBundle.message("ui.setting.chat.downstream.title"));
        downStreamTitledBorderBox.add(mdModel,BorderLayout.CENTER);
    }

    public void openDialog(JComponent component,Class<?> settingPanel) {
        ChatSettingDialog.settingPanel = settingPanel;
        SwingUtilities.invokeLater(() -> { // 在事件调度线程中执行
            ChatSettingDialog dialog = new ChatSettingDialog(project);
            dialog.setTitle("Chat Settings：" + settingPanel.getSimpleName());

            dialog.pack(); //  先调用 pack()
            dialog.setLocationRelativeTo(component);
            dialog.setVisible(true);
        });
    }

    public void initPromptsPanel() {
        if (!thisPrompts.isEmpty()) {
            for (JsonObject item : thisPrompts) {
                PromptItemComponent promptItem = new PromptItemComponent(item);
                promptList.add(promptItem);
            }

            updateLayout();
            scrollToBottom();
        }
    }

    public void updateLayout() {
        promptList.revalidate();
        promptList.repaint();
    }

    public void apply() {
        setSetOutputConf();
    }

    public void resetByDefault() {
        SetOutputConf = stateStore.defaultSetOutputConf().deepCopy();
        stateStore.setAISettingInfoByKey(getMainPanel().getPanelName(),"outputConf",SetOutputConf);
        reset();
    }

    public void reset() {
        getSetOutputConf();
        enablePromptsCheckBox.setSelected(enablePrompts);
        temperatureSpinner.setValue(SetOutputConf.get("temperature").getAsDouble());
        topPSpinner.setValue(SetOutputConf.get("top_p").getAsDouble());
        topKSpinner.setValue(SetOutputConf.get("top_k").getAsInt());
        freqPSpinner.setValue(SetOutputConf.get("frequency_penalty").getAsDouble());
        presPSpinner.setValue(SetOutputConf.get("presence_penalty").getAsDouble());
        maxTokenSlider.setValue(SetOutputConf.get("max_tokens").getAsInt());
        historyLengthSlider.setValue(SetOutputConf.get("history_length").getAsInt());
        enableZipHistoryBySize.setSelected(SetOutputConf.get("summary_swi").getAsBoolean());
        zipHistoryBySizeSlider.setValue(SetOutputConf.get("summary_thr_len").getAsInt());
    }

    private void setSetOutputConf() {
        SetOutputConf.addProperty("temperature",(double) temperatureSpinner.getValue());
        SetOutputConf.addProperty("top_p",(double) topPSpinner.getValue());
        SetOutputConf.addProperty("top_k",(int) topKSpinner.getValue());
        SetOutputConf.addProperty("frequency_penalty",(double) freqPSpinner.getValue());
        SetOutputConf.addProperty("presence_penalty",(double) presPSpinner.getValue());
        SetOutputConf.addProperty("max_tokens",maxTokenSlider.getValue());
        SetOutputConf.addProperty("history_length",historyLengthSlider.getValue());
        SetOutputConf.addProperty("summary_swi",enableZipHistoryBySize.isSelected());
        SetOutputConf.addProperty("summary_thr_len",zipHistoryBySizeSlider.getValue());

        // 创建
        JsonObject setInfo = new JsonObject();

        setInfo.addProperty("promptsCutIn", enablePrompts);

        // 这里是独立响应的
        //setInfo.add("prompts", JsonUtil.getJsonArray(thisPrompts));

        setInfo.add("outputConf", SetOutputConf);
        stateStore.setAISettingInfo(getMainPanel().getPanelName(),setInfo);
    }

    private void getSetOutputConf() {
        JsonObject settingInfo = stateStore.getAISettingInfo(getMainPanel().getPanelName());
        SetOutputConf = settingInfo.get("outputConf").getAsJsonObject();
        enablePrompts = settingInfo.get("promptsCutIn").getAsBoolean();
        thisPrompts = settingInfo.get("prompts").getAsJsonArray().asList().stream() // 将 JsonArray 转换为 List<JsonElement> 并创建流
                .filter(JsonElement::isJsonObject)   // 过滤出所有是 JsonObject 的元素
                .map(JsonElement::getAsJsonObject)   // 将这些元素强制转换为 JsonObject
                .collect(Collectors.toList());
    }

    public class PromptItemComponent extends JPanel {
        public PromptItemComponent(JsonObject promptItem) {

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(JBUI.Borders.empty(6));

            JCheckBox enableCheckBox = new JCheckBox();
            enableCheckBox.setSelected(promptItem.has("enable") && promptItem.get("enable").getAsBoolean());
            enableCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean isEnable = enableCheckBox.isSelected();
                    for (int i = 0; i < thisPrompts.size(); i++) {
                        JsonObject currentPrompt = thisPrompts.get(i);
                        if (Objects.equals(currentPrompt.get("promptId").getAsString(), promptItem.get("promptId").getAsString())) {
                            currentPrompt.addProperty("enable", isEnable);
                            thisPrompts.set(i, currentPrompt); // 使用 set() 方法修改列表元素

                            getMainPanel().getPromptsPanel().updatePrompt(currentPrompt);
                            break; // 可选：如果 promptId 是唯一的，找到后可以跳出循环
                        }
                    }
                }
            });
            add(enableCheckBox);

            JComboBox<String> roleSelect = new ComboBox<>();
            DefaultComboBoxModel<String> comboBoxModels = new DefaultComboBoxModel<>();
            Arrays.stream(AIAssistantSettingsState.promptsComboboxRolesString).forEach(comboBoxModels::addElement);
            roleSelect.setModel(comboBoxModels);
            roleSelect.setSelectedItem(promptItem.get("role").getAsString());
            roleSelect.setPreferredSize(new Dimension(92, roleSelect.getPreferredSize().height));
            // 添加监听器
            roleSelect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedRole = (String) roleSelect.getSelectedItem();
                    for (int i = 0; i < thisPrompts.size(); i++) {
                        JsonObject currentPrompt = thisPrompts.get(i);
                        if (Objects.equals(currentPrompt.get("promptId").getAsString(), promptItem.get("promptId").getAsString())) {
                            currentPrompt.addProperty("role", selectedRole);
                            thisPrompts.set(i, currentPrompt); // 使用 set() 方法修改列表元素

                            getMainPanel().getPromptsPanel().updatePrompt(currentPrompt);
                            break; // 可选：如果 promptId 是唯一的，找到后可以跳出循环
                        }
                    }
                }
            });
            add(roleSelect);

            ExpandableTextField promptFiled = new ExpandableTextField();
            promptFiled.setFont(new Font("Microsoft YaHei", Font.PLAIN,stateStore.CHAT_PANEL_FONT_SIZE));
            promptFiled.setText(promptItem.get("content").getAsString());
            promptFiled.setCaretPosition(0);
            // 添加监听器
            promptFiled.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateContent();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateContent();
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateContent();
                }
                private void updateContent() {
                    String promptContent = promptFiled.getText();
                    for (int i = 0; i < thisPrompts.size(); i++) {
                        JsonObject currentPrompt = thisPrompts.get(i);
                        if (Objects.equals(currentPrompt.get("promptId").getAsString(), promptItem.get("promptId").getAsString())) {
                            currentPrompt.addProperty("content", promptContent);
                            thisPrompts.set(i, currentPrompt); // 使用 set() 方法修改列表元素

                            getMainPanel().getPromptsPanel().updatePrompt(currentPrompt);
                            break; // 可选：如果 promptId 是唯一的，找到后可以跳出循环
                        }
                    }
                }
            });
            add(promptFiled);

            BubbleButton deleteP = getDeletePAction(this,promptItem);
            add(deleteP);
        }
    }

    private @NotNull BubbleButton getDeletePAction(JComponent component, JsonObject item) {
        BubbleButton deletePAction = new BubbleButton("", AllIcons.Actions.DeleteTagHover);
        deletePAction.addActionListener(e -> {
            promptList.remove(component);
            thisPrompts.removeIf(it -> StringUtil.equals(it.get("promptId").getAsString(), item.get("promptId").getAsString()));

            getMainPanel().getContentPanel().deletePin(item.get("promptId").getAsString());
            getMainPanel().getPromptsPanel().delete(item.get("promptId").getAsString());
            updateLayout();
        });
        return deletePAction;
    }

    public void addNewPrompt() {
        if (thisPrompts.size() >= 32) {
            BalloonUtil.showBalloon("Cannot add more prompt！！！", MessageType.ERROR,promptsListPanel);
            return;
        }
        JsonObject prompt = new JsonObject();
        prompt.addProperty("promptId", GeneratorUtil.generateUniqueId());
        prompt.addProperty("role","system");
        prompt.addProperty("content","");
        prompt.addProperty("enable",true);
        promptList.add(new PromptItemComponent(prompt));
        thisPrompts.add(prompt);

        getMainPanel().getPromptsPanel().addPrompt(prompt);

        updateLayout();
        scrollToBottom();
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> { // 在Swing事件调度线程上执行
            JScrollBar verticalScrollBar = promptScrollPane.getVerticalScrollBar();
            int max = verticalScrollBar.getMaximum();
            if (max > 0) { // 避免在内容为空的情况下的异常
                verticalScrollBar.setValue(max);
            }
        });
    }

    public void initHelp() {
        promptsHelpLabel.setFont(JBUI.Fonts.smallFont());
        promptsHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        setDefaultHelpLabel.setFont(JBUI.Fonts.smallFont());
        setDefaultHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        temperatureHelpLabel.setFont(JBUI.Fonts.smallFont());
        temperatureHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        topPHelpLabel.setFont(JBUI.Fonts.smallFont());
        topPHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        topKHelpLabel.setFont(JBUI.Fonts.smallFont());
        topKHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        freqPHelpLabel.setFont(JBUI.Fonts.smallFont());
        freqPHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        presPHelpLabel.setFont(JBUI.Fonts.smallFont());
        presPHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        maxTokensHelpLabel.setFont(JBUI.Fonts.smallFont());
        maxTokensHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        historyLengthHelpLabel.setFont(JBUI.Fonts.smallFont());
        historyLengthHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        zipHistoryBySizeHelpLabel.setFont(JBUI.Fonts.smallFont());
        zipHistoryBySizeHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        promptsDataHelpLabel.setFont(JBUI.Fonts.smallFont());
        promptsDataHelpLabel.setForeground(new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26")));
    }
}
