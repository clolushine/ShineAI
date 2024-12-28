package com.shine.ai.core;

import com.intellij.openapi.application.ApplicationManager;

import com.shine.ai.settings.AIAssistantSettingsState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;



public class TokenManager {
    private static final Logger LOG = LoggerFactory.getLogger(TokenManager.class);
    private final Map<String,String> headers = new HashMap<>();
    private final AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
    public static TokenManager getInstance() {
        return ApplicationManager.getApplication().getService(TokenManager.class);
    }

    public Map<String, String> getShineAIHeaders() {
        headers.put("Authorization","Bearer " + state.UserToken);
        headers.put("Content-Type","application/json");
        return headers;
    }
}
