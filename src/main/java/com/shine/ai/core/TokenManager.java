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
