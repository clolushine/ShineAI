/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
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

package com.shine.ai.db.chats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.JsonUtil;
import com.shine.ai.util.StringUtil;

public class Chats {
    private String id; // UUID, Primary Key
    private long createAt;
    private long updateAt;
    private String name;
    private String icon;
    private String content;
    private String role; // e.g., "user", "assistant", "system"
    private int status; // 0: in-progress, 1: completed, -1: error
    private String finishReason; // e.g., "stop", "length"
    private String fromLLM; // e.g., "gpt-4-turbo"
    private String fromProvider; // e.g., "openai"
    private String collId; // Collection ID, for grouping messages
    private boolean webSearch;
    private JsonArray attachments; // Stored as JSON string in DB

    // Constructor for creating a new object before DB insertion
    public Chats(String role) {
        boolean isUser = StringUtil.equals(role,"user");

        this.id = GeneratorUtil.generateWithUUID();
        this.createAt = GeneratorUtil.getTimestamp();
        this.updateAt = GeneratorUtil.getTimestamp();
        this.name = isUser ? "我" : "AI";
        this.collId = "";
        this.role = isUser ? "user" : "assistant";
        this.content = isUser ? "" : "有什么可以帮你的吗？";
        this.status = isUser ? 1 : 0; // 0 加载中 1加载完成 -1生成出错, -2网络错 -3输出中止 2持续输出
        this.icon = isUser ? AIAssistantIcons.ME_URL : AIAssistantIcons.AI_URL;
        this.finishReason = "";
        this.fromLLM = "";
        this.fromProvider = "";
        this.webSearch = false;
        this.attachments = new JsonArray(); // Default to empty array
    }

    public Chats(JsonObject chats) {
        this.id = JsonUtil.getJsonStringDefault(chats,"id", GeneratorUtil.generateWithUUID());
        this.createAt = JsonUtil.getJsonLongDefault(chats,"createAt", GeneratorUtil.getTimestamp());
        this.updateAt = JsonUtil.getJsonLongDefault(chats,"updateAt", GeneratorUtil.getTimestamp());
        this.name = JsonUtil.getJsonStringDefault(chats,"name", "我");
        this.collId = JsonUtil.getJsonStringDefault(chats,"collId", "");
        this.role = JsonUtil.getJsonStringDefault(chats,"role", "user");
        this.content = JsonUtil.getJsonStringDefault(chats,"content", "");
        this.status = JsonUtil.getJsonIntDefault(chats,"status", 1);
        this.icon = JsonUtil.getJsonStringDefault(chats,"icon", AIAssistantIcons.ME_URL);
        this.finishReason = JsonUtil.getJsonStringDefault(chats,"finishReason", "");
        this.fromLLM = JsonUtil.getJsonStringDefault(chats,"fromLLM", "");
        this.fromProvider = JsonUtil.getJsonStringDefault(chats,"fromProvider", "");
        this.webSearch = JsonUtil.getJsonBooleanDefault(chats,"webSearch", false);
        this.attachments = JsonUtil.getJsonArrayDefault(chats,"attachments", new JsonArray()); // Default to empty array
    }

    // Full constructor, typically used when mapping from a database record
    public Chats(String id, long createAt, long updateAt, String name, String icon, String content, String role, int status, String finishReason, String fromLLM, String fromProvider, String collId, boolean webSearch, String attachments) {
        this.id = id;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.name = name;
        this.icon = icon;
        this.content = content;
        this.role = role;
        this.status = status;
        this.finishReason = finishReason;
        this.fromLLM = fromLLM;
        this.fromProvider = fromProvider;
        this.collId = collId;
        this.webSearch = webSearch;
        if (attachments != null && !attachments.isEmpty()) {
            this.attachments = JsonUtil.getStringToJsonArray(attachments);
        } else {
            this.attachments = new JsonArray();
        }
    }

    // --- Getters and Setters ---
    // ... (generate all getters and setters for all fields)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getCreateAt() { return createAt; }
    public void setCreateAt(long createAt) { this.createAt = createAt; }
    public long getUpdateAt() { return updateAt; }
    public void setUpdateAt(long updateAt) { this.updateAt = updateAt; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    public String getFromLLM() { return fromLLM; }
    public void setFromLLM(String fromLLM) { this.fromLLM = fromLLM; }
    public String getFromProvider() { return fromProvider; }
    public void setFromProvider(String fromProvider) { this.fromProvider = fromProvider; }
    public String getCollId() { return collId; }
    public void setCollId(String collId) { this.collId = collId; }
    public boolean isWebSearch() { return webSearch; }
    public void setWebSearch(boolean webSearch) { this.webSearch = webSearch; }
    public JsonArray getAttachments() { return attachments; }
    public void setAttachments(JsonArray attachments) {
        this.attachments = attachments;
    }
    public String getAttachmentsAsJsonString() {
        return (this.attachments != null) ? JsonUtil.getJsonArrayToString(this.attachments) : "[]";
    }
    public void setAttachmentsFromJsonString(String json) {
        if (json != null && !json.trim().isEmpty()) {
            this.attachments = JsonUtil.getStringToJsonArray(json);
        } else {
            this.attachments = new JsonArray();
        }
    }

    public JsonObject getJsonObjectAll() {
        JsonObject chats = new JsonObject();
        chats.addProperty("id",this.id);
        chats.addProperty("createAt",this.createAt);
        chats.addProperty("updateAt",this.updateAt);
        chats.addProperty("name",this.name);
        chats.addProperty("icon",this.icon);
        chats.addProperty("content",this.content);
        chats.addProperty("role",this.role);
        chats.addProperty("status",this.status);
        chats.addProperty("finishReason",this.finishReason);
        chats.addProperty("fromLLM",this.fromLLM);
        chats.addProperty("fromProvider",this.fromProvider);
        chats.addProperty("collId",this.collId);
        chats.addProperty("webSearch",this.webSearch);
        chats.add("attachments",this.attachments);
        return chats;
    }
}
