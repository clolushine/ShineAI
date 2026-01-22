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

package com.shine.ai.util;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;


public class JsonUtil {

    public static Gson gson = new Gson(); // json类方法

    public static String prettyJson(JsonObject data) {
        Gson gs = new GsonBuilder().setPrettyPrinting().create();
        return gs.toJson(data.asMap()); // Convert to Map
    }

    public static JsonObject getJsonObject(String jsonString) {
        JsonObject outputObject = new JsonObject();
        try {
            outputObject = gson.fromJson(jsonString,JsonObject.class);
        } catch (Exception e) {
            System.out.println("getJsonObject: exception" + e.getMessage());
        }
        return outputObject;
    }

    public static String getJsonString(JsonObject object) {
        String outputString = "";
        try {
            outputString = gson.toJson(object);
        } catch (Exception e) {
            System.out.println("getJsonString: exception" + e.getMessage());
        }
        return outputString;
    }

    public static String getJsonArrayToString(JsonArray array) {
        String outputString = "";
        try {
            outputString = gson.toJson(array);
        } catch (Exception e) {
            System.out.println("getJsonString: exception" + e.getMessage());
        }
        return outputString;
    }

    public static JsonArray getStringToJsonArray(String arrayString) {
        JsonArray outputArray = new JsonArray();
        try {
            outputArray = gson.fromJson(arrayString,JsonArray.class);
        } catch (Exception e) {
            System.out.println("getJsonString: exception" + e.getMessage());
        }
        return outputArray;
    }

    public static JsonArray getJsonArrayByListString(List<String> list) {
        JsonArray jsonArray = new JsonArray();
        for (String item : list) {
            try {
                jsonArray.add(gson.fromJson(item,JsonObject.class));
            } catch (Exception e) {
                System.err.println("getJsonArrayByString: exception" + e.getMessage());
            }
        }
        return jsonArray;
    }


    public static <T> JsonArray createJsonArray(List<T> list) {
        JsonArray jsonArray = new JsonArray();
        for (T item : list) {
            try {
                jsonArray.add(gson.toJsonTree(item));
            } catch (Exception e) {
                System.err.println("createJsonArray: exception" + e.getMessage());
            }
        }
        return jsonArray;
    }

    public static JsonObject mergeJsonObject(JsonObject target, JsonObject source) {
        JsonObject merged = new JsonObject();//创建一个新的JsonObject
        target.entrySet().forEach(entry-> merged.add(entry.getKey(), entry.getValue()));
        source.entrySet().forEach(entry -> merged.add(entry.getKey(), entry.getValue()));
        return merged;
    }


    public static JsonArray getJsonArray(List<JsonObject> list) {
        JsonArray jsonArray = new JsonArray();
        for (JsonObject item : list) {
            try {
                jsonArray.add(item);
            } catch (Exception e) {
                System.err.println("getJsonArray: exception" + e.getMessage());
            }
        }
        return jsonArray;
    }

    public static List<JsonObject> getListJsonObject(JsonArray array) {
        List<JsonObject> list = new ArrayList<>();
        for (int i = 0 ; i < array.size() ; i++) {
            try {
                if (array.get(i).isJsonObject()) {
                    list.add(i,array.get(i).getAsJsonObject());
                }
            } catch (Exception e) {
                System.err.println("getJsonArray: exception" + e.getMessage());
            }
        }
        return list;
    }

    public static String getJsonStringDefault(JsonObject obj, String key, String defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }

    public static long getJsonLongDefault(JsonObject obj, String key, long defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsLong();
        }
        return defaultValue;
    }

    public static int getJsonIntDefault(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return defaultValue;
    }

    public static boolean getJsonBooleanDefault(JsonObject obj, String key, boolean defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    public static JsonArray getJsonArrayDefault(JsonObject obj, String key, JsonArray defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsJsonArray();
        }
        return defaultValue;
    }

    public static JsonObject getJsonObjectDefault(JsonObject obj, String key, JsonObject defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsJsonObject();
        }
        return defaultValue;
    }
}

