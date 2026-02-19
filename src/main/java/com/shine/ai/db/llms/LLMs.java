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

package com.shine.ai.db.llms;

import com.google.gson.JsonArray;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.JsonUtil;

public class LLMs {
    private String key; // Primary Key
    private long updateAt;
    private String provider;
    private JsonArray llms;

    public LLMs(String key, String llms) {
        this.key = key;
        this.updateAt = GeneratorUtil.getTimestamp();
        this.provider = key.toUpperCase();
        if (llms != null && !llms.isEmpty()) {
            this.llms = JsonUtil.getStringToJsonArray(llms);
        } else {
            this.llms = new JsonArray();
        }
    }

    public LLMs(String key, long updateAt, String provider, String llms) {
        this.key = key;
        this.updateAt = updateAt;
        this.provider = provider;
        if (llms != null && !llms.isEmpty()) {
            this.llms = JsonUtil.getStringToJsonArray(llms);
        } else {
            this.llms = new JsonArray();
        }
    }

    // --- Getters and Setters ---
    // ... (generate all getters and setters for all fields)
    public long getUpdateAt() { return updateAt; }
    public void setUpdateAt(long updateAt) { this.updateAt = updateAt; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public JsonArray getLLMs() { return llms; }
    public void setLLMs(JsonArray llms) { this.llms = llms; }
    public String getLLMsAsJsonString() {
        return (this.llms != null) ? JsonUtil.getJsonArrayToString(this.llms) : "[]";
    }
    public void setLLMsFromJsonString(String json) {
        if (json != null && !json.trim().isEmpty()) {
            this.llms = JsonUtil.getStringToJsonArray(json);
        } else {
            this.llms = new JsonArray();
        }
    }
}
