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

package com.shine.ai.ui.help;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchRegexHandler {

    public List<JsonObject> searchList(List<JsonObject> list, String regexPattern) {
        List<JsonObject> matchedList = new ArrayList<>();
        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            return matchedList;
        }

        // Pattern.CASE_INSENSITIVE 忽略大小写
        // Pattern.UNICODE_CASE 处理Unicode字符的大小写
        Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        int globalMatchIndex = 0; // 全局计数

        for (JsonObject item : list) {
            if (item.has("content") && !item.get("content").isJsonNull()) {
                String content = item.get("content").getAsString();
                String id = item.get("id").getAsString();
                Matcher matcher = pattern.matcher(content);

                int matchOrder = 0; // 新增：记录当前文本第几个匹配

                // 遍历所有的匹配结果
                while (matcher.find()) { // find() 会在每次调用时查找下一个匹配项
                    JsonObject matchedItem = new JsonObject();

                    matchedItem.addProperty("id", id);
                    // 将匹配到的子字符串和位置信息添加到副本中
                    matchedItem.addProperty("matchedSubstring", matcher.group());
                    matchedItem.addProperty("matchStartIndex", matcher.start());
                    matchedItem.addProperty("matchEndIndex", matcher.end());

                    matchedItem.addProperty("matchOrder", matchOrder); // 新增：匹配序号（本条消息内）
                    matchedItem.addProperty("globalMatchIndex", globalMatchIndex); // 新增：全局序号

                    matchedList.add(matchedItem);

                    matchOrder++;
                    globalMatchIndex++;
                }
            }
        }
        return matchedList;
    }
}
