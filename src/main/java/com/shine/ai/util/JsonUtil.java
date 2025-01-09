package com.shine.ai.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


public class JsonUtil {

    public static String prettyJson(JsonObject data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(data.asMap()); // Convert to Map
    }
}

