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

package com.shine.ai.db.attachs;

import com.google.gson.JsonObject;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.JsonUtil;

public class Attachs {
    private String id; // UUID, Primary Key
    private long createAt;
    private long updateAt;
    private String fileName;
    private String url;

    private String type;
    private String mimeType;
    private String chatId;
    private String collId;
    private JsonObject metaData;

    // Constructor for creating a new object before DB insertion
    public Attachs() {
        this.id = GeneratorUtil.generateWithUUID();
        this.createAt = GeneratorUtil.getTimestamp();
        this.updateAt = GeneratorUtil.getTimestamp();
        this.fileName = "";
        this.url = "";
        this.type = "";
        this.mimeType = "";
        this.chatId = "";
        this.collId = "";
        this.metaData = new JsonObject();
    }

    public Attachs(JsonObject attachs) {
        this.id = JsonUtil.getJsonStringDefault(attachs,"id", GeneratorUtil.generateWithUUID());
        this.createAt = JsonUtil.getJsonLongDefault(attachs,"createAt", GeneratorUtil.getTimestamp());
        this.updateAt = JsonUtil.getJsonLongDefault(attachs,"updateAt", GeneratorUtil.getTimestamp());
        this.fileName = JsonUtil.getJsonStringDefault(attachs,"fileName", "");
        this.url = JsonUtil.getJsonStringDefault(attachs,"url", "");
        this.type = JsonUtil.getJsonStringDefault(attachs,"type", "");
        this.mimeType = JsonUtil.getJsonStringDefault(attachs,"mimeType", "");
        this.chatId = JsonUtil.getJsonStringDefault(attachs,"chatId", "");
        this.collId = JsonUtil.getJsonStringDefault(attachs,"collId", "");
        this.metaData = JsonUtil.getJsonObjectDefault(attachs,"metaData", new JsonObject());
    }

    // Full constructor, typically used when mapping from a database record
    public Attachs(String id, long createAt, long updateAt, String fileName, String url,String type, String mimeType,String chatId, String collId,String metaData) {
        this.id = id;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.fileName = fileName;
        this.url = url;
        this.type = type;
        this.mimeType = mimeType;
        this.chatId = chatId;
        this.collId = collId;
        if (metaData != null && !metaData.isEmpty()) {
            this.metaData = JsonUtil.getJsonObject(metaData);
        } else {
            this.metaData = new JsonObject();
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
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getCollId() { return collId; }
    public void setCollId(String subTitle) { this.collId = collId; }
    public JsonObject getMetaData() { return metaData; }
    public void setMetaData(JsonObject metaData) { this.metaData = metaData; }
    public String getMetaDataAsJsonString() {
        return (this.metaData != null) ? JsonUtil.getJsonString(this.metaData) : "{}";
    }
    public void setMetaDataFromJsonString(String json) {
        if (json != null && !json.trim().isEmpty()) {
            this.metaData = JsonUtil.getJsonObject(json);
        } else {
            this.metaData = new JsonObject();
        }
    }

    public JsonObject getJsonObjectAll() {
        JsonObject attachs = new JsonObject();
        attachs.addProperty("id",this.id);
        attachs.addProperty("createAt",this.createAt);
        attachs.addProperty("updateAt",this.updateAt);
        attachs.addProperty("fileName",this.fileName);
        attachs.addProperty("url",this.url);
        attachs.addProperty("type",this.type);
        attachs.addProperty("mimeType",this.mimeType);
        attachs.addProperty("chatId",this.chatId);
        attachs.addProperty("collId",this.collId);
        attachs.add("metaData",this.metaData);
        return attachs;
    }
}
