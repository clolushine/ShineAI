package com.shine.ai.db;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shine.ai.db.attachs.Attachs;
import com.shine.ai.db.attachs.AttachsManager;
import com.shine.ai.db.chats.Chats;
import com.shine.ai.db.chats.ChatsManager;
import com.shine.ai.db.colls.Colls;
import com.shine.ai.db.colls.CollsManager;
import com.shine.ai.db.llms.LLMs;
import com.shine.ai.db.llms.LLMsManager;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.JsonUtil;
import com.shine.ai.util.StringUtil;

import java.util.List;

public class DBUtil {
    // chats db
    private static final ChatsManager chatsManager = ChatsManager.getInstance();

    // colls db
    private static final CollsManager collsManagers = CollsManager.getInstance();

    // llms db
    private static final LLMsManager llmsManager = LLMsManager.getInstance();

    // attachs db
    private static final AttachsManager attachsManagers = AttachsManager.getInstance();

    public static void addAttachsBatch(JsonArray attachsList) {
        for (JsonElement attachs: attachsList) {
            JsonObject attachsItem = attachs.getAsJsonObject();
            attachsManagers.addAttachs(new Attachs(attachsItem));
        }
    }

    public static void setLLMsByKey(String key, JsonArray llms) {
        llmsManager.addLLMs(new LLMs(key.toLowerCase(), JsonUtil.getJsonArrayToString(llms)));
    }

    public static JsonArray getLLMsByKey(String key) {
        LLMs llms = llmsManager.findByKey(key.toLowerCase());
        if (llms == null) {
            return new JsonArray();
        }
        return llms.getLLMs();
    }

    public static void createCollsAndChats() {
        Colls colls = new Colls();
        Chats chats = new Chats("assistant");

        chats.setCollId(colls.getId());
        chats.setStatus(1); // 设置已完成状态

        collsManagers.addColls(colls);
        chatsManager.addChats(chats);
    }

    public static void delChatsById(String chatId) {
        chatsManager.delById(chatId);
        attachsManagers.delByChatId(chatId);
    }

    public static void delCollsById(String collId) {
        collsManagers.delById(collId);
        chatsManager.delByCollId(collId);
        attachsManagers.delByCollId(collId);
    }

    public static void updateCollsById(String collId) {
        JsonObject updateCollsInfo = new JsonObject();

        JsonObject colls = collsManagers.findById(collId).getJsonObjectAll();

        updateCollsInfo.addProperty("id", collId);
        updateCollsInfo.addProperty("updateAt", GeneratorUtil.getTimestamp());

        if (StringUtil.equals(colls.get("title").getAsString(),"") || StringUtil.equals(colls.get("subTitle").getAsString(),"")) {
            List<JsonObject> chatsList = chatsManager.findByCollId(collId,1,20, true);
            for (int i = 0 ; i< chatsList.size() ; i++) {
                JsonObject chatsItem = chatsList.get(i).getAsJsonObject();
                if (StringUtil.equals(chatsItem.get("role").getAsString(),"user") && !chatsItem.get("content").isJsonNull()) {
                    String tContent = chatsItem.get("content").getAsString();
                    String title = tContent.isBlank() ? "" : tContent;
                    updateCollsInfo.addProperty("title", title);
                }
                if (!StringUtil.equals(chatsItem.get("role").getAsString(),"user") && !chatsItem.get("content").isJsonNull() && i != 0) {
                    String sContent = chatsItem.get("content").getAsString();
                    String subTitle = sContent.isBlank() ? "" : sContent;
                    updateCollsInfo.addProperty("subTitle", subTitle);
                }
            }
        }else {
            updateCollsInfo.addProperty("title", colls.get("title").getAsString());
            updateCollsInfo.addProperty("subTitle", colls.get("subTitle").getAsString());
        }

        collsManagers.addColls(new Colls(updateCollsInfo));
    }

    public static JsonObject initAIInfo() {
        JsonObject aiInfo = new Chats("assistant").getJsonObjectAll();

        aiInfo.addProperty("content", "");
        aiInfo.addProperty("isPin",false);

        return aiInfo;
    }

    public static JsonObject initMyInfo() {
        JsonObject myInfo = new Chats("user").getJsonObjectAll();

        myInfo.addProperty("isPin",false);

        return myInfo;
    }
}
