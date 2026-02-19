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

package com.shine.ai.core.builder;

import com.google.gson.JsonObject;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.ui.MessageGroupComponent;
import com.shine.ai.ui.PromptGroupComponent;
import com.shine.ai.util.JsonUtil;


public class OptionBuilder {
    public static String buildShineAI(JsonObject messageObject, MessageGroupComponent mComponent, PromptGroupComponent pComponent) {
        AIAssistantSettingsState stateStore = AIAssistantSettingsState.getInstance();

        JsonObject options = new JsonObject();
        JsonObject UserInfo = stateStore.getUserInfo();
        JsonObject AISetInfo = mComponent.getAISetInfo();
        JsonObject ChatCollection = mComponent.getChatCollection();
        JsonObject OutputConf = mComponent.getAISetOutputInfo();
        JsonObject apiKeyItem = mComponent.getWeightedApikey();

        options.addProperty("content",messageObject.get("content").getAsString());
        options.add("attachments", messageObject.get("attachments").getAsJsonArray());

        options.addProperty("name",UserInfo.get("name").getAsString());
        options.addProperty("uid",UserInfo.get("id").getAsString());
        options.addProperty("collId", ChatCollection.get("id").getAsString());

        options.addProperty("aimodel", AISetInfo.get("aiModel").getAsString());
        options.addProperty("stream",AISetInfo.get("aiStream").getAsBoolean());
        options.addProperty("streamSpeed",AISetInfo.get("streamSpeed").getAsInt());

        options.add("outputConf", OutputConf);

        options.addProperty("online", AISetInfo.get("online").getAsBoolean());
        options.addProperty("vendor", mComponent.getAIVendorKey().toLowerCase());

        options.addProperty("apiId","");
        options.addProperty("apiKey","");

        if (apiKeyItem != null && !apiKeyItem.isJsonNull()) {
            options.addProperty("apiId", apiKeyItem.get("apiId").getAsString());
            options.addProperty("apiKey", apiKeyItem.get("apiKey").getAsString());
        }

        options.addProperty("modelIsolation", true);

        if (AISetInfo.get("promptsCutIn").getAsBoolean()) {
            options.add("prompts", pComponent.getEnabledPromptList());
        }

        return JsonUtil.getJsonString(options);
    }
}
