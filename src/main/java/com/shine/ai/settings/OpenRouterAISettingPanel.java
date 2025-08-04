package com.shine.ai.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shine.ai.db.DBUtil;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.ui.AIApikeyComponent;
import com.shine.ai.ui.LoadingButton;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MyScrollPane;
import com.shine.ai.util.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.shine.ai.MyToolWindowFactory.*;

public class OpenRouterAISettingPanel implements Configurable, Disposable {
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

    private JPanel apikeyTitledBorderBox;
    private final JPanel apikeyList = new JPanel(new VerticalLayout(JBUI.scale(10)));
    private final MyScrollPane apikeyScrollPane = new MyScrollPane(apikeyList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private JPanel apikeyListPanel;
    private JButton addApikeyButton;
    private JLabel apikeyDataHelpLabel;
    private static List<JsonObject> thisApiKeys;

    public OpenRouterAISettingPanel() {
        createRefreshButton();
        createApikeyList();
        init();
    }

    private void init() {

        // 如果模型为空则刷新一下模型
        assert modelsCombobox != null;
        if (DBUtil.getLLMsByKey(OpenRouter_AI_KEY).isEmpty()) {
            updateModels();
        } else {
            modelsCombobox.setModel(modelsToComboBoxModel());
        }

        assert refreshModelsButton != null;
        refreshModelsButton.addActionListener(e -> {
            if (Boolean.FALSE.equals(checkUserAuthExists(refreshModelsButton))) {
                return;
            }
            updateModels();
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

        JsonObject settingInfo = state.getAISettingInfo(OpenRouter_AI_CONTENT_NAME);

        enableStreamCheckBox.setSelected(settingInfo.get("aiStream").getAsBoolean());
        streamSpeedField.setValue(settingInfo.get("streamSpeed").getAsInt());

        modelsCombobox.setSelectedItem(settingInfo.get("aiModel").getAsString());

        thisApiKeys = JsonUtil.getListJsonObject(settingInfo.get("apiKeys").getAsJsonArray());

        initApiKeysPanel();

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

        JsonObject settingInfo = state.getAISettingInfo(OpenRouter_AI_CONTENT_NAME);

        return !settingInfo.get("aiStream").getAsBoolean() == enableStreamCheckBox.isSelected() ||
                !(settingInfo.get("streamSpeed").getAsInt() == streamSpeedField.getValue()) ||
                !StringUtil.equals(settingInfo.get("aiModel").getAsString(), (String) modelsCombobox.getSelectedItem());
    }

    @Override
    public void apply() {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

        JsonObject setInfo = new JsonObject();

        setInfo.addProperty("aiStream",enableStreamCheckBox.isSelected());
        setInfo.addProperty("streamSpeed",streamSpeedField.getValue());
        setInfo.addProperty("aiModel",(String) modelsCombobox.getSelectedItem());

        state.setAISettingInfo(OpenRouter_AI_CONTENT_NAME,setInfo);

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
    public void disposeUIResources() {
        refreshInfo();
    }

    @Override
    public String getDisplayName() {
        return OpenRouter_AI_CONTENT_NAME;
    }

    private void createRefreshButton() {
        refreshModelsButton = new LoadingButton("refresh"); //  创建 LoadingButton
        refreshModelsPanel.add(refreshModelsButton);
    }

    private void createApikeyList() {
        assert apikeyListPanel != null;;
        apikeyListPanel.setPreferredSize(new Dimension(apikeyListPanel.getWidth(),192));
        apikeyScrollPane.setBorder(JBUI.Borders.empty());
        apikeyListPanel.add(apikeyScrollPane);
        apikeyScrollPane.getVerticalScrollBar().setAutoscrolls(true);

        assert addApikeyButton!= null;
        addApikeyButton.setIcon(AllIcons.General.Add);
        addApikeyButton.addActionListener(e -> {
            addNewApikey(apikeyList);
        });
    }

    private void createUIComponents() {
        streamTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator stBt = new TitledSeparator("Stream Settings");
        streamTitledBorderBox.add(stBt,BorderLayout.CENTER);

        modelTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator mdModel = new TitledSeparator(MsgEntryBundle.message("ui.setting.server.router.models.title"));
        modelTitledBorderBox.add(mdModel,BorderLayout.CENTER);

        apikeyTitledBorderBox = new JPanel(new BorderLayout());
        TitledSeparator atBt = new TitledSeparator(MsgEntryBundle.message("ui.setting.server.router.apikey.title"));
        apikeyTitledBorderBox.add(atBt,BorderLayout.CENTER);
    }

    public DefaultComboBoxModel<String> modelsToComboBoxModel() {

        JsonArray models = DBUtil.getLLMsByKey(OpenRouter_AI_KEY);

        HashMap<String, String> setModels = new HashMap<>();
        for (JsonElement model : models) {
            // 根据 model 获取对应的值，例如从另一个数组或其他数据源中获取
            JsonObject llm = model.getAsJsonObject();
            if (llm.has("architecture") && llm.get("architecture").isJsonObject()) {
                JsonObject architectureObject = llm.get("architecture").getAsJsonObject();
                if (architectureObject.has("input_modalities") && architectureObject.get("input_modalities").isJsonArray()) {
                    JsonArray input_modalities = architectureObject.get("input_modalities").getAsJsonArray();
                    if (input_modalities.contains(new JsonPrimitive("text")) || input_modalities.contains(new JsonPrimitive("image"))) {
                        if (llm.has("id")) {
                            // 根据 model 获取对应的值，例如从另一个数组或其他数据源中获取
                            String mod = llm.get("id").getAsString();
                            setModels.put(mod,mod);
                        }
                    }
                }
            }
        }
        DefaultComboBoxModel<String> comboboxModels = new DefaultComboBoxModel<>();
        comboboxModels.addElement("");
        setModels.values().stream().sorted().forEach(comboboxModels::addElement); // 将值添加到模型
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

        apikeyDataHelpLabel.setFont(JBUI.Fonts.smallFont());
        apikeyDataHelpLabel.setForeground(new JBColor(Color.decode("#ee9e26"), Color.decode("#ee9e26")));
    }

    public void updateModels() {
        AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

        // 1. 在开始异步操作前，先在 EDT 上设置按钮为加载状态
        if (refreshModelsButton != null) {
            ApplicationManager.getApplication().invokeLater(() -> refreshModelsButton.setLoading(true));
        }

        CompletableFuture.runAsync(() -> {
            JsonArray localModels = DBUtil.getLLMsByKey(OpenRouter_AI_KEY);

            assert modelsCombobox != null;
            JsonArray models = ShineAIUtil.getAIModels(OpenRouter_AI_KEY,OpenRouter_AI_LLM_API,refreshModelsButton);

            int beforeLength = localModels.size();
            int afterLength = models.size();

            String notifyString = !models.isEmpty() ? String.format("Model refreshed, %s new records added.", afterLength - beforeLength) : "Could not retrieve AI model.";
            MessageType notifyColor = !models.isEmpty() ? MessageType.INFO : MessageType.WARNING;
            BalloonUtil.showBalloon(notifyString,notifyColor,modelsCombobox);
            if (!models.isEmpty()) DBUtil.setLLMsByKey(OpenRouter_AI_KEY,models);
            modelsCombobox.setModel(modelsToComboBoxModel());
            JsonElement currentModel = stateStore.getAISettingInfoByKey(OpenRouter_AI_CONTENT_NAME,"aiModel");

            if (!StringUtil.equals(currentModel.getAsString(), "")) {
                if (OtherUtil.isValidModelInComboBox(modelsCombobox, currentModel.getAsString())) { // 使用辅助方法验证
                    modelsCombobox.setSelectedItem(currentModel.getAsString());
                } else {
                    modelsCombobox.setSelectedItem(""); // 如果之前的模型不存在，设置为空
                }
            } else {
                modelsCombobox.setSelectedItem(""); // 如果没有存储的模型，设置为空
            }

            // 最后，在 EDT 上停止加载状态
            if (refreshModelsButton != null) {
                refreshModelsButton.setLoading(false);
            }

        }).exceptionally(ex -> {
            // 3. 异步任务失败时的处理，回到 EDT 更新 UI
            ApplicationManager.getApplication().invokeLater(() -> {
                if (refreshModelsButton != null) {
                    refreshModelsButton.setLoading(false);
                }
                String errorMessage = "Failed to refresh models.";
                if (ex != null && ex.getMessage() != null) {
                    errorMessage += " Error: " + ex.getMessage();
                }
                BalloonUtil.showBalloon(errorMessage, MessageType.ERROR, modelsCombobox);
            });
            return null; // 返回null，表示异常已被处理
        });
    }

    private Boolean checkUserAuthExists(JComponent component) {
        AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
        if (StringUtil.isNotEmpty(state.UserToken) && !state.getUserInfo().isEmpty()) {
            return true;
        }
        BalloonUtil.showBalloon("UserAuth is none. Please login before retry.",MessageType.WARNING,component);
        return false;
    }

    private void addNewApikey(JComponent _component) {
        AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

        if (apikeyList.getComponentCount() >= 10) {
            BalloonUtil.showBalloon("Cannot add more apikey！！！", MessageType.ERROR,apikeyListPanel);
            return;
        }
        JsonObject apikeyInfo = new JsonObject();
        apikeyInfo.addProperty("tag", "");
        apikeyInfo.addProperty("id",GeneratorUtil.generateWithUUID());
        apikeyInfo.addProperty("apiId","");
        apikeyInfo.addProperty("apiKey","");
        apikeyInfo.addProperty("weight",1);
        apikeyList.add(new AIApikeyComponent(apikeyInfo,thisApiKeys,_component,OpenRouter_AI_CONTENT_NAME,false),0);
        thisApiKeys.add(0,apikeyInfo);
        // 这里需要写入state
        stateStore.setAISettingInfoByKey(OpenRouter_AI_CONTENT_NAME,"apiKeys", JsonUtil.getJsonArray(thisApiKeys));
        updateLayout();
        scrollTop();
    }

    public void initApiKeysPanel() {
        if (!thisApiKeys.isEmpty()) {
            for (JsonObject item : thisApiKeys) {
                AIApikeyComponent apiKeyItem = new AIApikeyComponent(item,thisApiKeys,apikeyList,OpenRouter_AI_CONTENT_NAME,false);
                apikeyList.add(apiKeyItem);
            }
            updateLayout();
        }
    }

    public void updateLayout() {
        apikeyList.revalidate();
        apikeyList.repaint();
    }

    public void scrollTop() {
        SwingUtilities.invokeLater(() -> { // 在Swing事件调度线程上执行
            JScrollBar verticalScrollBar = apikeyScrollPane.getVerticalScrollBar();
            int min = verticalScrollBar.getMinimum();
            if (min <= 0) { // 避免在内容为空的情况下的异常
                verticalScrollBar.setValue(min);
            }
        });
    }
}
