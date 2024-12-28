
package com.shine.ai.settings;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.shine.ai.MyToolWindowFactory.*;

@State(
        name = "com.obiscr.chatgpt.settings.AIAssistantSettingsState",
        storages = @Storage("ShineAISettingsPlugin.xml")
)
public class AIAssistantSettingsState implements PersistentStateComponent<AIAssistantSettingsState> {
  // 最大存储空间大小 (MB) -  根据你的需求设置
  public static final double MAX_STORAGE_SIZE_MB = 10;

  public static final String[] promptsComboboxRolesString = {"user", "system", "assistant"};

  public int CHAT_PANEL_FONT_SIZE = 12;

  public String requestTimeout = "60000";
  public Boolean enableLineWarp = true;
  public Boolean enableAvatar = true;
  public Map<Integer,String> contentOrder = new HashMap<>(){{
    put(1, CLOUDFLARE_AI_CONTENT_NAME);
    put(2, Google_AI_CONTENT_NAME);
    put(3, GROQ_AI_CONTENT_NAME);
  }};

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
  public Boolean CFEnableStream = true;
  public Integer CFStreamSpeed = 50;
  public String CFCurrentModel = null;
  public Boolean CFEnablePrompts = false;
  @Tag("CFModels")
  public Map<String, String> CFModels = new HashMap<>();

  @Tag("CFPrompts")
  public List<String> CFPrompts = new ArrayList<>();

  public String CFSetOutputConf = setDefaultSetOutputConf();

  public JsonObject getCFSetOutputConf() {
    return gson.fromJson(CFSetOutputConf, JsonObject.class);
  }

  public <T> void setCFSetOutputConf(JsonObject newSetOutputConf) {
    try {
      JsonObject setOutputConf = gson.fromJson(CFSetOutputConf, JsonObject.class);
      // 遍历传入的 JsonObject
      for (String key : newSetOutputConf.keySet()) {
        JsonElement value = newSetOutputConf.get(key);
        setOutputConf.add(key, value); // 添加或替换值
      }
      this.CFSetOutputConf = gson.toJson(setOutputConf);
    } catch (Exception e) {
      // 处理潜在的异常，例如 JSON 解析错误
      System.err.println("Error updating CFSetOutputConf: " + e.getMessage());
      // 可以选择抛出异常或采取其他错误处理措施
    }
  }

  // Google AI
  public Boolean GOEnableStream = true;
  public Integer GOStreamSpeed = 50;
  public String GOCurrentModel = null;
  public Boolean GOEnablePrompts = false;
  @Tag("GOModels")
  public Map<String, String> GOModels = new HashMap<>();

  @Tag("GOPrompts")
  public List<String> GOPrompts = new ArrayList<>();

  public String GOSetOutputConf = setDefaultSetOutputConf();

  public JsonObject getGOSetOutputConf() {
    return gson.fromJson(GOSetOutputConf, JsonObject.class);
  }

  public <T> void setGOSetOutputConf(JsonObject newSetOutputConf) {
    try {
      JsonObject setOutputConf = gson.fromJson(GOSetOutputConf, JsonObject.class);
      // 遍历传入的 JsonObject
      for (String key : newSetOutputConf.keySet()) {
        JsonElement value = newSetOutputConf.get(key);
        setOutputConf.add(key, value); // 添加或替换值
      }
      this.GOSetOutputConf = gson.toJson(setOutputConf);
    } catch (Exception e) {
      // 处理潜在的异常，例如 JSON 解析错误
      System.err.println("Error updating GOSetOutputConf: " + e.getMessage());
      // 可以选择抛出异常或采取其他错误处理措施
    }
  }

  // GROQ AI
  public Boolean GREnableStream = true;
  public Integer GRStreamSpeed = 50;
  public String GRCurrentModel = null;
  public Boolean GREnablePrompts = false;
  @Tag("GRModels")
  public Map<String, String> GRModels = new HashMap<>();

  @Tag("GOPrompts")
  public List<String> GRPrompts = new ArrayList<>();

  public String GRSetOutputConf = setDefaultSetOutputConf();

  public JsonObject getGRSetOutputConf() {
    return gson.fromJson(GRSetOutputConf, JsonObject.class);
  }

  public <T> void setGRSetOutputConf(JsonObject newSetOutputConf) {
    try {
      JsonObject setOutputConf = gson.fromJson(GRSetOutputConf, JsonObject.class);
      // 遍历传入的 JsonObject
      for (String key : newSetOutputConf.keySet()) {
        JsonElement value = newSetOutputConf.get(key);
        setOutputConf.add(key, value); // 添加或替换值
      }
      this.GRSetOutputConf = gson.toJson(setOutputConf);
    } catch (Exception e) {
      // 处理潜在的异常，例如 JSON 解析错误
      System.err.println("Error updating GRSetOutputConf: " + e.getMessage());
      // 可以选择抛出异常或采取其他错误处理措施
    }
  }

  public String setDefaultSetOutputConf() {
    JsonObject setInfo = new JsonObject();
    setInfo.addProperty("temperature",0.5);
    setInfo.addProperty("top_p",1.0);
    setInfo.addProperty("top_k",5);
    setInfo.addProperty("max_tokens",2048);
    setInfo.addProperty("history_length",6);
    setInfo.addProperty("summary_swi",true);
    setInfo.addProperty("summary_thr_len",512);
    return gson.toJson(setInfo);
  }

  // 聊天记录
  public List<String> AIChatCollection = new ArrayList<>();

  @Deprecated
  public List<String> customActionsPrefix = new ArrayList<>();

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

  public void saveState() {
    ApplicationManager.getApplication().saveSettings(); // 保存更改
  }

  public String createChatCollection() {
    JsonObject chatCollection = new JsonObject();
    chatCollection.addProperty("collId", GeneratorUtil.generateUniqueId());
    chatCollection.addProperty("createat",GeneratorUtil.getTimestamp());
    chatCollection.addProperty("updateat",GeneratorUtil.getTimestamp());
    chatCollection.addProperty("collectionTitle","");
    chatCollection.addProperty("collectionSubTitle","");
    chatCollection.add("collectionExtra",null);

    List<JsonObject> chatList = new ArrayList<>();
    JsonObject chatItem = new JsonObject();

    chatItem.addProperty("time", GeneratorUtil.getTimestamp());
    chatItem.addProperty("icon", AIAssistantIcons.AI_URL);
    chatItem.addProperty("name", MsgEntryBundle.message("ui.setting.server.default.name"));
    chatItem.addProperty("content", "有什么可以帮你的吗？");
    chatItem.addProperty("isMe", false);
    chatItem.addProperty("role", "assistant");
    chatItem.addProperty("status", 1);
    chatItem.addProperty("chatId", GeneratorUtil.generateUniqueId());
    chatItem.addProperty("isPin", false);
    chatItem.addProperty("showBtn", false);
    chatItem.addProperty("withContent", "");
    chatList.add(chatItem);

    // 使用JsonArray添加数组元素
    JsonArray jsonArrayChatList = new JsonArray();
    for (JsonObject item : chatList) {
      jsonArrayChatList.add(item);  // 将每个 JsonObject 添加到 JsonArray
    }

    chatCollection.add("chatList", jsonArrayChatList);

    return gson.toJson(chatCollection);
  }

  public void deleteChatCollectionById(String collectionId) {
    if (collectionId.isEmpty()) return;
    Iterator<String> iterator = AIChatCollection.iterator();
    while (iterator.hasNext()) {
      String item = iterator.next();
      JsonObject collectionItem = gson.fromJson(item, JsonObject.class);
      if (collectionItem.has("collId") && Objects.equals(collectionItem.get("collId").getAsString(), collectionId)) {
        iterator.remove();
      }
    }
  }

  public void updateChatCollectionInfo(JsonObject collectionItem) {
    JsonArray chatList = collectionItem.get("chatList").getAsJsonArray();
    String collId = collectionItem.get("collId").getAsString();
    String collTitle = "";
    String collSubTitle = "";
    if (!chatList.isEmpty()) {
      int idxj = IntStream.range(0, chatList.size())
              .filter(i -> chatList.get(i).getAsJsonObject().get("isMe").getAsBoolean() && !chatList.get(i).getAsJsonObject().get("content").getAsString().isEmpty())
              .findFirst()
              .orElse(-1);
      if (idxj >= 0) collTitle = chatList.get(idxj).getAsJsonObject().get("content").getAsString();
      int idxk = IntStream.range(0, chatList.size())
              .filter(i -> i !=0 && !chatList.get(i).getAsJsonObject().get("isMe").getAsBoolean() && !chatList.get(i).getAsJsonObject().get("content").getAsString().isEmpty())
              .findFirst()
              .orElse(-1);
      if (idxk >= 0) collSubTitle = chatList.get(idxk).getAsJsonObject().get("content").getAsString();
    }
    int upIndex = IntStream.range(0, AIChatCollection.size())
            .filter(i -> StringUtil.equals(getJsonObject(AIChatCollection.get(i)).get("collId").getAsString(), collId))
            .findFirst()
            .orElse(-1);
    if (upIndex >= 0) {
      JsonObject setCollectionItem = getJsonObject(AIChatCollection.get(upIndex));
      setCollectionItem.addProperty("updateat",GeneratorUtil.getTimestamp());
      setCollectionItem.addProperty("collectionTitle",collTitle);
      setCollectionItem.addProperty("collectionSubTitle",collSubTitle);
      setCollectionItem.add("chatList",chatList);
      AIChatCollection.set(upIndex,getJsonString(setCollectionItem));
    }
    AIChatCollection = AIChatCollection.stream()
            .sorted(((o1, o2) -> Long.compare(getJsonObject(o2).get("updateat").getAsLong(), getJsonObject(o1).get("updateat").getAsLong())))
            .collect(Collectors.toList());
  }

  public void deleteChatCollectionByItem(String collectionItem) {
    if (collectionItem.isEmpty()) return;
    AIChatCollection.remove(collectionItem);
  }

  public JsonObject getJsonObject(String jsonString) {
    JsonObject outputObject = new JsonObject();
    try {
      outputObject = gson.fromJson(jsonString,JsonObject.class);
    } catch (Exception e) {
      System.out.println("getJsonObject: exception" + e.getMessage());
    }
    return outputObject;
  }

  public JsonArray getJsonArray(List<JsonObject> list) {
    JsonArray jsonArray = new JsonArray();
    for (JsonObject item : list) {
      try {
        jsonArray.add(item);
      } catch (Exception e) {
        System.err.println("getJsonArray: exception" + e.getMessage());
      }
    }
    return jsonArray;
  }

  public JsonArray getJsonArrayByString(List<String> list) {
    JsonArray jsonArray = new JsonArray();
    for (String item : list) {
      try {
        jsonArray.add(gson.fromJson(item,JsonObject.class));
      } catch (Exception e) {
        System.err.println("getJsonArrayByString: exception" + e.getMessage());
      }
    }
    return jsonArray;
  }

  public String getJsonString(JsonObject object) {
    String outputString = "";
    try {
      outputString = gson.toJson(object);
    } catch (Exception e) {
      System.out.println("getJsonString: exception" + e.getMessage());
    }
    return outputString;
  }

  public JsonObject mergeJsonObject(JsonObject target, JsonObject source) {
    JsonObject merged = new JsonObject();//创建一个新的JsonObject
    target.entrySet().forEach(entry-> merged.add(entry.getKey(), entry.getValue()));
    source.entrySet().forEach(entry -> merged.add(entry.getKey(), entry.getValue()));
    return merged;
  }

  public Integer getStorageUsagePercentage() {
    try {
      Path optionDir = Path.of(PathManager.getOptionsPath());
      Path storageFile = Paths.get(optionDir.toString(), "ShineAISettingsPlugin.xml");
      long fileSizeBytes = Files.size(storageFile);
      double fileSizeMB = fileSizeBytes / (1024.0 * 1024.0);
      // 计算百分比，并限制在 0-100 之间
      return (int) Math.max(0, Math.min(100, (fileSizeMB / MAX_STORAGE_SIZE_MB) * 100));
    } catch (Exception e) {
      System.out.println("getStorageUsagePercentage: exception" + e.getMessage());
    }
    return 0;
  }

  public String getStorageUsageMBInfo() {
    try {
      Path optionDir = Path.of(PathManager.getOptionsPath());
      Path storageFile = Paths.get(optionDir.toString(), "ShineAISettingsPlugin.xml");
      long fileSizeBytes = Files.size(storageFile);
      double fileSizeMB = fileSizeBytes / (1024.0 * 1024.0);
      return String.format("%.2f",fileSizeMB) + "mb" + "/" + MAX_STORAGE_SIZE_MB + "mb";
    } catch (Exception e) {
      System.out.println("getStorageUsageMBInfo: exception" + e.getMessage());
    }
    return "0mb" + "/" + MAX_STORAGE_SIZE_MB + "mb";
  }
}
