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
