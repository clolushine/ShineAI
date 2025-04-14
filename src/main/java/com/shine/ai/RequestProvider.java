package com.shine.ai;

import com.google.gson.JsonObject;
import com.shine.ai.core.TokenManager;
import com.shine.ai.core.builder.OfficialBuilder;
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
        provider.data = OfficialBuilder.buildShineAI(messageMy,mainPanel.getContentPanel());

        return provider;
    }
}
