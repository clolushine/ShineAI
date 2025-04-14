package com.shine.ai.core.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.ui.MessageGroupComponent;


public class OfficialBuilder {
    public static String buildShineAI(JsonObject messageObject, MessageGroupComponent component) {
        AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

        JsonObject options = new JsonObject();
        JsonObject UserInfo = stateStore.getUserInfo();
        JsonObject AISetInfo = component.getAISetInfo();
        JsonObject ChatCollection = component.getChatCollection();
        JsonObject OutputConf = component.getAISetOutputInfo();

        options.addProperty("content",messageObject.get("content").getAsString());
        options.add("attachments", messageObject.get("attachments").getAsJsonArray());

        options.addProperty("name",UserInfo.get("name").getAsString());
        options.addProperty("uid",UserInfo.get("id").getAsString());
        options.addProperty("collId", ChatCollection.get("collId").getAsString());

        options.addProperty("aimodel", AISetInfo.get("aiModel").getAsString());
        options.addProperty("stream",AISetInfo.get("aiStream").getAsBoolean());
        options.addProperty("streamSpeed",AISetInfo.get("streamSpeed").getAsInt());
        options.addProperty("outputConf", stateStore.getJsonString(OutputConf));
        if (AISetInfo.get("promptsCutIn").getAsBoolean()) {
            JsonArray prompts = stateStore.getJsonArrayByString(component.AIPrompts);
            options.add("prompts", prompts);
        }
        return stateStore.getJsonString(options);
    }
}
