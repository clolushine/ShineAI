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

package com.shine.ai;

import com.google.gson.JsonObject;
import com.shine.ai.core.TokenManager;
import com.shine.ai.core.builder.OptionBuilder;
import com.shine.ai.ui.MainPanel;
import static com.shine.ai.settings.AIAssistantSettingsPanel.SHINE_AI_BASE_URL;

import java.util.Map;


public class RequestProvider {

    public static String baseUrl = SHINE_AI_BASE_URL;

    private String url;
    private String data;
    private Map<String, String> header;

    public String getUrl() {
        return url;
    }

    public String getData() {
        return data;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public RequestProvider create(MainPanel mainPanel, JsonObject messageMy, String url) {
        RequestProvider provider = new RequestProvider();

        provider.url = baseUrl + url;
        provider.header = TokenManager.getInstance().getShineAIHeaders();
        provider.data = OptionBuilder.buildShineAI(messageMy,mainPanel.getContentPanel(),mainPanel.getPromptsPanel());

        return provider;
    }
}
