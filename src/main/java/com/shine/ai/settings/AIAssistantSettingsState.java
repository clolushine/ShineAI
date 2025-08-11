
/*
 * ShineAI - An IntelliJ IDEA plugin for AI services.
 * Copyright (C) 2025 Shine Zhong
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

package com.shine.ai.settings;

import com.google.gson.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.shine.ai.db.DBPaths;
import com.shine.ai.util.FileUtil;
import com.shine.ai.util.JsonUtil;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.shine.ai.MyToolWindowFactory.*;

@State(
        name = "com.shine.ai.settings.AIAssistantSettingsState",
        storages = @Storage("shineAISettingsPlugin.xml")
)
public class AIAssistantSettingsState implements PersistentStateComponent<AIAssistantSettingsState> {
  // 最大存储空间大小 (MB) -  根据你的需求设置
  public static final double MAX_STORAGE_SIZE_MB = 1024;

  public static final String[] promptsComboboxRolesString = {"user", "system", "assistant"};

  public int CHAT_PANEL_FONT_SIZE = 14;

  public Integer requestTimeout = 60000;
  public Boolean enableHighlightCode = false;
  public Integer promptsPos = 1;
  public Boolean enableLineWarp = true;
  public Boolean enableAvatar = true;
  public Boolean enableMsgPanelAIInfo = true;

  public Map<Integer,String> contentOrder = new HashMap<>(){{
    put(1, OpenAI_CONTENT_NAME );
    put(2, Google_AI_CONTENT_NAME);
    put(3, Anthropic_AI_CONTENT_NAME);
  }};

  public Map<Integer,String> AITitleList = new HashMap<>(){{
    put(1, OpenAI_CONTENT_NAME );
    put(2, Google_AI_CONTENT_NAME);
    put(3, Anthropic_AI_CONTENT_NAME);
    put(4, CLOUDFLARE_AI_CONTENT_NAME);
    put(5, GROQ_AI_CONTENT_NAME);
    put(6, OpenRouter_AI_CONTENT_NAME);
  }};

  public Map<Integer,String> AIKeyList = new HashMap<>(){{
    put(1, OpenAI_KEY);
    put(2, Google_AI_KEY);
    put(3, Anthropic_AI_KEY);
    put(4, CLOUDFLARE_AI_KEY);
    put(5, GROQ_AI_KEY);
    put(6, OpenRouter_AI_KEY);
  }};

  public Map<Integer,String> AINameList = new HashMap<>(){{
    put(1, OpenAI_NAME);
    put(2, Google_AI_NAME);
    put(3, Anthropic_AI_NAME);
    put(4, CLOUDFLARE_AI_NAME);
    put(5, GROQ_AI_NAME);
    put(6, OpenRouter_AI_NAME);
  }};

  public Map<Integer,String> AIIconList = new HashMap<>(){{
    put(1, OpenAI_ICON);
    put(2, Google_AI_ICON);
    put(3, Anthropic_AI_ICON);
    put(4, CLOUDFLARE_AI_ICON);
    put(5, GROQ_AI_ICON);
    put(6, OpenRouter_AI_ICON);
  }};

  public Map<Integer,String> AIAPIList = new HashMap<>(){{
    put(1, OpenAI_AI_API);
    put(2, Google_AI_API);
    put(3, Anthropic_AI_API);
    put(4, CLOUDFLARE_AI_API);
    put(5, GROQ_AI_API);
    put(6, OpenRouter_AI_API);
  }};

  public Map<Integer,String> AIClassNameList = new HashMap<>(){{
    put(1, OpenAI_SETTING_CLASS_NAME);
    put(2, Google_SETTING_CLASS_NAME);
    put(3, Anthropic_SETTING_CLASS_NAME);
    put(4, CLOUDFLARE_SETTING_CLASS_NAME);
    put(5, GROQ_SETTING_CLASS_NAME);
    put(6, OpenRouter_SETTING_CLASS_NAME);
  }};

  public List<String> aiVenderList = initAIVenderList();

  public Gson gson = new Gson(); // json类方法

  public String Useremail = "";
  public String UserToken = "";
  public String userInfoJson = "{}"; // 存储 userInfo 的 JSON 字符串

  //提供getter/setter方法访问userInfo,维持对外接口不变
  public JsonObject getUserInfo() {
    return gson.fromJson(userInfoJson, JsonObject.class);
  }

  public void setUserInfo(JsonObject userInfo) {
    this.userInfoJson = gson.toJson(userInfo);
  }

  // CF AI
  public String CFAISettingInfo = initAISettingInfo();

  // Google AI
  public String GoogleAISettingInfo = initAISettingInfo();

  // Groq AI
  public String GroqAISettingInfo = initAISettingInfo();

  // OpenAI
  public String OpenAISettingInfo = initAISettingInfo();

  // OpenRouter AI
  public String OpenRouterAISettingInfo = initAISettingInfo();

  // Anthropic AI
  public String AnthropicAISettingInfo = initAISettingInfo();


  private String initAISettingInfo(){
    JsonObject settingInfo = new JsonObject();
    settingInfo.addProperty("aiModel", "");
    settingInfo.addProperty("aiStream", true);
    settingInfo.addProperty("streamSpeed", 80);
    settingInfo.addProperty("aiType", "");
    settingInfo.add("prompts", new JsonArray());
    settingInfo.addProperty("promptsCutIn", false);
    settingInfo.addProperty("online", false);
    settingInfo.add("outputConf", defaultSetOutputConf());
    settingInfo.addProperty("serverApi", "");
    settingInfo.addProperty("modelsApi", "");
    settingInfo.add("apiKeys", new JsonArray());
    return JsonUtil.getJsonString(settingInfo);
  }

  public JsonObject getAISettingInfo(String panelName) {
    String getTarget = null;
    switch (panelName) {
      case Google_AI_CONTENT_NAME -> getTarget = GoogleAISettingInfo;
      case GROQ_AI_CONTENT_NAME -> getTarget = GroqAISettingInfo;
      case OpenAI_CONTENT_NAME -> getTarget = OpenAISettingInfo;
      case OpenRouter_AI_CONTENT_NAME -> getTarget = OpenRouterAISettingInfo;
      case Anthropic_AI_CONTENT_NAME -> getTarget = AnthropicAISettingInfo;
      default -> getTarget = CFAISettingInfo;
    }
    return JsonUtil.getJsonObject(getTarget);
  }

  public JsonElement getAISettingInfoByKey(String panelName,String keyName) {
    String getTarget = null;
    switch (panelName) {
      case Google_AI_CONTENT_NAME -> getTarget = GoogleAISettingInfo;
      case GROQ_AI_CONTENT_NAME -> getTarget = GroqAISettingInfo;
      case OpenAI_CONTENT_NAME -> getTarget = OpenAISettingInfo;
      case OpenRouter_AI_CONTENT_NAME -> getTarget = OpenRouterAISettingInfo;
      case Anthropic_AI_CONTENT_NAME -> getTarget = AnthropicAISettingInfo;
      default -> getTarget = CFAISettingInfo;
    }
    JsonObject targetAISettingInfo = JsonUtil.getJsonObject(getTarget);
    return targetAISettingInfo.get(keyName);
  }

  public void setAISettingInfo(String panelName,JsonObject settingInfo) {
    switch (panelName) {
      case Google_AI_CONTENT_NAME -> GoogleAISettingInfo = setJsonObjectAction(GoogleAISettingInfo,settingInfo);
      case GROQ_AI_CONTENT_NAME -> GroqAISettingInfo = setJsonObjectAction(GroqAISettingInfo,settingInfo);
      case OpenAI_CONTENT_NAME -> OpenAISettingInfo = setJsonObjectAction(OpenAISettingInfo,settingInfo);
      case OpenRouter_AI_CONTENT_NAME -> OpenRouterAISettingInfo = setJsonObjectAction(OpenRouterAISettingInfo,settingInfo);
      case Anthropic_AI_CONTENT_NAME -> AnthropicAISettingInfo = setJsonObjectAction(AnthropicAISettingInfo,settingInfo);
      default -> CFAISettingInfo = setJsonObjectAction(CFAISettingInfo,settingInfo);
    }
  }

  public void setAISettingInfoByKey(String panelName,String key,JsonElement value) {
    //
    JsonObject settingInfo = new JsonObject();
    settingInfo.add(key, value);

    switch (panelName) {
      case Google_AI_CONTENT_NAME -> GoogleAISettingInfo = setJsonObjectAction(GoogleAISettingInfo,settingInfo);
      case GROQ_AI_CONTENT_NAME -> GroqAISettingInfo = setJsonObjectAction(GroqAISettingInfo,settingInfo);
      case OpenAI_CONTENT_NAME -> OpenAISettingInfo = setJsonObjectAction(OpenAISettingInfo,settingInfo);
      case OpenRouter_AI_CONTENT_NAME -> OpenRouterAISettingInfo = setJsonObjectAction(OpenRouterAISettingInfo,settingInfo);
      case Anthropic_AI_CONTENT_NAME -> AnthropicAISettingInfo = setJsonObjectAction(AnthropicAISettingInfo,settingInfo);
      default -> CFAISettingInfo = setJsonObjectAction(CFAISettingInfo,settingInfo);
    }
  }

  private String setJsonObjectAction(String target,JsonObject setInfo) {
    JsonObject targetAISettingInfo = JsonUtil.getJsonObject(target);
    // 遍历传入的 JsonObject
    for (String key : setInfo.keySet()) {
      JsonElement value = setInfo.get(key);
      targetAISettingInfo.add(key, value); // 添加或替换值
    }
    return JsonUtil.getJsonString(targetAISettingInfo);
  }

  public JsonObject defaultSetOutputConf() {
    JsonObject setInfo = new JsonObject();
    setInfo.addProperty("temperature",1.0);
    setInfo.addProperty("top_p",1.0);
    setInfo.addProperty("top_k",5);
    setInfo.addProperty("frequency_penalty",0);
    setInfo.addProperty("presence_penalty",0);
    setInfo.addProperty("max_tokens",4096);
    setInfo.addProperty("history_length",6);
    setInfo.addProperty("summary_swi",true);
    setInfo.addProperty("summary_thr_len",512);
    return setInfo;
  }

  public static AIAssistantSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(AIAssistantSettingsState.class);
  }

  @Nullable
  @Override
  public AIAssistantSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull AIAssistantSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public void reload() {
    loadState(this);
  }

  public static void saveState() {
    ApplicationManager.getApplication().saveSettings(); // 保存更改
  }

  public boolean currentModelCanOnline(String panelName,String model) {
    boolean isCan = false;
    switch (panelName) {
      case Google_AI_CONTENT_NAME -> isCan = true;
      case GROQ_AI_CONTENT_NAME, OpenAI_CONTENT_NAME, OpenRouter_AI_CONTENT_NAME, Anthropic_AI_CONTENT_NAME -> {}
      default -> {}
    }
    return isCan;
  }

  public boolean currentModelCanImage(String panelName,String model,JsonArray modelList) {
    boolean isCan = false;
    switch (panelName) {
      case Google_AI_CONTENT_NAME -> isCan = true;
      case GROQ_AI_CONTENT_NAME -> {
        for (JsonElement mod : modelList) {
          // 根据 model 获取对应的值，例如从另一个数组或其他数据源中获取
          JsonObject llm = mod.getAsJsonObject();
            if (llm.has("id")) {
              String llmId = llm.get("id").getAsString();
              if (llmId.contains("llama-4") && llmId.equals(model)) {
                isCan = true;
              }
            }
        }
      }
      case OpenRouter_AI_CONTENT_NAME -> {
        for (JsonElement mod : modelList) {
          // 根据 model 获取对应的值，例如从另一个数组或其他数据源中获取
          JsonObject llm = mod.getAsJsonObject();
          if (llm.has("architecture") && llm.get("architecture").isJsonObject()) {
            JsonObject architectureObject = llm.get("architecture").getAsJsonObject();
            if (architectureObject.has("input_modalities") && architectureObject.get("input_modalities").isJsonArray()) {
              JsonArray input_modalities = architectureObject.get("input_modalities").getAsJsonArray();
              if (input_modalities.contains(new JsonPrimitive("image"))) {
                if (llm.has("id")) {
                  // 根据 model 获取对应的值，例如从另一个数组或其他数据源中获取
                  if (StringUtil.equals(model,llm.get("id").getAsString())) {
                    isCan = true;
                  }
                }
              }
            }
          }
        }
      }
      case OpenAI_CONTENT_NAME, Anthropic_AI_CONTENT_NAME -> {}
      default -> {}
    }
    return isCan;
  }

  private List<String> initAIVenderList() {
    List<String> AIList = new ArrayList<>();
    for (int i = 0 ; i < AITitleList.size() ; i++) {
      JsonObject obj = new JsonObject();
      obj.addProperty("title", AITitleList.get(i+1));
      obj.addProperty("key", AIKeyList.get(i+1));
      obj.addProperty("name", AINameList.get(i+1));
      obj.addProperty("icon", AIIconList.get(i+1));
      obj.addProperty("className", AIClassNameList.get(i+1));
      obj.addProperty("api", AIAPIList.get(i+1));
      AIList.add(JsonUtil.getJsonString(obj));
    }
    return AIList;
  }

  public List<JsonObject> getAIVenderList() {
    return aiVenderList.stream()
            .map(JsonUtil::getJsonObject)   // 将这些元素强制转换为 JsonObject
            .collect(Collectors.toList());
  }

//  public Integer getStorageUsagePercentage() {
//    try {
//      Path optionDir = Path.of(PathManager.getOptionsPath());
//      Path settingsFile = Paths.get(optionDir.toString(), "shineAISettingsPlugin.xml");
//      Path dbDataFile = Paths.get(PathManager.getConfigPath(),"data");
//      long fileSizeBytes = Files.size(settingsFile) + Files.size(dbDataFile);
//      double fileSizeMB = fileSizeBytes / (1024.0 * 1024.0);
//      // 计算百分比，并限制在 0-100 之间
//      return (int) Math.max(0, Math.min(100, (fileSizeMB / MAX_STORAGE_SIZE_MB) * 100));
//    } catch (Exception e) {
//      System.out.println("getStorageUsagePercentage: exception" + e.getMessage());
//    }
//    return 0;
//  }

  public String getStorageUsageMBInfo() {
    try {
      Path optionDir = Path.of(PathManager.getOptionsPath());
      Path settingsFile = Paths.get(optionDir.toString(), "shineAISettingsPlugin.xml");
      Path dbDataDir = DBPaths.dbPath;
      long fileSizeBytes = Files.size(settingsFile) + FileUtil.getFolderSize(dbDataDir.toFile());
      double fileSizeMB = fileSizeBytes / (1024.0 * 1024.0);
      return String.format("%.2f",fileSizeMB) + "mb";
    } catch (Exception e) {
      System.out.println("getStorageUsageMBInfo: exception" + e.getMessage());
    }
    return "0mb";
  }

  public String getCacheUsageMBInfo() {
    try {
      Path cacheDir = FileUtil.cachePath;
      long fileSizeBytes = FileUtil.getFolderSize(cacheDir.toFile());
      double fileSizeMB = fileSizeBytes / (1024.0 * 1024.0);
      return String.format("%.2f",fileSizeMB) + "mb";
    } catch (Exception e) {
      System.out.println("getCacheUsageMBInfo: exception" + e.getMessage());
    }
    return "0mb";
  }
}
