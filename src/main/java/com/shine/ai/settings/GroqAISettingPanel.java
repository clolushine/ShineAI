package com.shine.ai.settings;

import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.ui.LoadingButton;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.ShineAIUtil;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static com.shine.ai.MyToolWindowFactory.ACTIVE_CONTENT;
import static com.shine.ai.MyToolWindowFactory.GROQ_AI_CONTENT_NAME;

public class GroqAISettingPanel implements Configurable, Disposable {
    private Project project;

    private JPanel myMainPanel;
    private JPanel streamTitledBorderBox;
    private JPanel modelTitledBorderBox;
    private JComboBox<String> modelsCombobox;
    private JCheckBox enableStreamCheckBox;
    private JSlider streamSpeedField;

    private JLabel streamHelpLabel;
    private JLabel streamSpeedHelpLabel;

    private JPanel refreshModelsPanel;
    private JLabel streamSpeedValueLabel;
    private LoadingButton refreshModelsButton;

    public GroqAISettingPanel() {
        createRefreshButton();
        init();
    }

    private void init() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        // 如果模型为空则刷新一下模型
        if (state.GRModels.isEmpty()) {
            updateModels();
        } else {
            assert modelsCombobox != null;
            modelsCombobox.setModel(modelsToComboBoxModel());
        }

        assert refreshModelsButton != null;
        refreshModelsButton.addActionListener(e -> {
            if (Boolean.FALSE.equals(checkUserAuthExists(refreshModelsButton))) {
                return;
            }
            refreshModelsButton.setLoading(true);
            CompletableFuture.runAsync(this::updateModels)
                    .thenRun(() -> refreshModelsButton.setLoading(false));
        });

        assert enableStreamCheckBox != null;
        enableStreamCheckBox.addChangeListener(e -> {
            streamSpeedField.setEnabled(enableStreamCheckBox.isSelected());
        });

        // 使用绑定，将 Label 的文本绑定到 Slider 的值
        streamSpeedField.addChangeListener(e -> {
            streamSpeedValueLabel.setText(String.valueOf(streamSpeedField.getValue()));
        });
    }

    @Override
    public void reset() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        enableStreamCheckBox.setSelected(state.GREnableStream);
        streamSpeedField.setValue(state.GRStreamSpeed);

        modelsCombobox.setSelectedItem(state.GRCurrentModel);

        initHelp();
    }

    @Override
    public @Nullable JComponent createComponent() {
        project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        return !state.GREnableStream == enableStreamCheckBox.isSelected() ||
                !(state.GRStreamSpeed == streamSpeedField.getValue()) ||
                !StringUtil.equals(state.GRCurrentModel, (String) modelsCombobox.getSelectedItem());
    }

    @Override
    public void apply() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        state.GREnableStream = enableStreamCheckBox.isSelected();
        state.GRStreamSpeed = streamSpeedField.getValue();
        state.GRCurrentModel = (String) modelsCombobox.getSelectedItem();

        refreshInfo();
    }

    private void refreshInfo() {
        MainPanel panel = (MainPanel) project.getUserData(ACTIVE_CONTENT);
        assert panel != null;
        panel.refreshInfo();
    }

    @Override
    public void dispose() {
    }


    @Override
    public String getDisplayName() {
        return MsgEntryBundle.message("ui.setting.server.groq.key");
    }

    private void createRefreshButton() {
        refreshModelsButton = new LoadingButton("refresh"); //  创建 LoadingButton
        refreshModelsPanel.add(refreshModelsButton);
    }

    private void createUIComponents() {
        streamTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator stBt = new TitledSeparator("Stream Settings");
        streamTitledBorderBox.add(stBt,BorderLayout.CENTER);

        modelTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator mdModel = new TitledSeparator(MsgEntryBundle.message("ui.setting.server.groq.models.title"));
        modelTitledBorderBox.add(mdModel,BorderLayout.CENTER);
    }

    public DefaultComboBoxModel<String> modelsToComboBoxModel() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
        DefaultComboBoxModel<String> comboboxModels = new DefaultComboBoxModel<>();
        comboboxModels.addElement(null);
        state.GRModels.values().forEach(comboboxModels::addElement); // 将值添加到模型
        return comboboxModels;
    }

    public HashMap<String,String> comboBoxModelToModels() {
        return new HashMap<>();
    }

    public void initHelp() {
        streamHelpLabel.setFont(JBUI.Fonts.smallFont());
        streamHelpLabel.setForeground(UIUtil.getContextHelpForeground());

        streamSpeedHelpLabel.setFont(JBUI.Fonts.smallFont());
        streamSpeedHelpLabel.setForeground(UIUtil.getContextHelpForeground());
    }

    public void updateModels() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
        assert modelsCombobox != null;
        String[] models = ShineAIUtil.getAIModels(GROQ_AI_CONTENT_NAME,refreshModelsButton);
        HashMap<String, String> cfModels = new HashMap<>();
        for (String model : models) {
            // 根据 model 获取对应的值，例如从另一个数组或其他数据源中获取
            cfModels.put(model,model);
        }
        int beforeLength = state.GRModels.size();
        int afterLength = cfModels.size();
        String notifyString = !cfModels.isEmpty() ? String.format("模型已刷新,新增%s条", afterLength - beforeLength) : "未获取到AI模型";
        MessageType notifyColor = !cfModels.isEmpty() ? MessageType.INFO : MessageType.WARNING;
        BalloonUtil.showBalloon(notifyString,notifyColor,modelsCombobox);
        if (!cfModels.isEmpty()) state.GRModels = cfModels;
        modelsCombobox.setModel(modelsToComboBoxModel());
        modelsCombobox.setSelectedItem(state.GRCurrentModel);
    }

    private Boolean checkUserAuthExists(JComponent component) {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
        if (StringUtil.isNotEmpty(state.UserToken) && !state.getUserInfo().isEmpty()) {
            return true;
        }
        BalloonUtil.showBalloon("UserAuth is none. Please login before retry.",MessageType.WARNING,component);
        return false;
    }
}
