package com.shine.ai.db.colls;

import com.google.gson.JsonObject;
import com.shine.ai.util.GeneratorUtil;
import com.shine.ai.util.JsonUtil;

public class Colls {
    private String id; // UUID, Primary Key
    private long createAt;
    private long updateAt;
    private String title;
    private String subTitle;

    // Constructor for creating a new object before DB insertion
    public Colls() {
        this.id = GeneratorUtil.generateWithUUID();
        this.createAt = GeneratorUtil.getTimestamp();
        this.updateAt = GeneratorUtil.getTimestamp();
        this.title = "";
        this.subTitle = "";
    }

    // Full constructor, typically used when mapping from a database record
    public Colls(String id, long createAt, long updateAt, String title, String subTitle) {
        this.id = id;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.title = title;
        this.subTitle = subTitle;
    }

    public Colls(JsonObject colls) {
        this.id = JsonUtil.getJsonStringDefault(colls,"id", GeneratorUtil.generateWithUUID());
        this.createAt = JsonUtil.getJsonLongDefault(colls,"createAt", GeneratorUtil.getTimestamp());
        this.updateAt = JsonUtil.getJsonLongDefault(colls,"updateAt", GeneratorUtil.getTimestamp());
        this.title = JsonUtil.getJsonStringDefault(colls,"title", "");
        this.subTitle = JsonUtil.getJsonStringDefault(colls,"subTitle", "");
    }

    // --- Getters and Setters ---
    // ... (generate all getters and setters for all fields)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getCreateAt() { return createAt; }
    public void setCreateAt(long createAt) { this.createAt = createAt; }
    public long getUpdateAt() { return updateAt; }
    public void setUpdateAt(long updateAt) { this.updateAt = updateAt; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubTitle() { return subTitle; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }

    public JsonObject getJsonObjectAll() {
        JsonObject colls = new JsonObject();
        colls.addProperty("id",this.id);
        colls.addProperty("createAt",this.createAt);
        colls.addProperty("updateAt",this.updateAt);
        colls.addProperty("title",this.title);
        colls.addProperty("subTitle",this.subTitle);
        return colls;
    }
}
