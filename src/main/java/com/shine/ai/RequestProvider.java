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

package com.shine.ai;

import com.google.gson.JsonObject;
import com.shine.ai.core.TokenManager;
import com.shine.ai.core.builder.OptionBuilder;
import com.shine.ai.ui.MainPanel;
import static com.shine.ai.settings.AIAssistantSettingsPanel.SHINE_AI_BASE_URL;

import java.util.Map;


public class RequestProvider {

    public static String baseUrl = SHINE_AI_BASE_URL;

    private String url;
    private String data;
    private Map<String, String> header;

    public String getUrl() {
        return url;
    }

    public String getData() {
        return data;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public RequestProvider create(MainPanel mainPanel, JsonObject messageMy, String url) {
        RequestProvider provider = new RequestProvider();

        provider.url = baseUrl + url;
        provider.header = TokenManager.getInstance().getShineAIHeaders();
        provider.data = OptionBuilder.buildShineAI(messageMy,mainPanel.getContentPanel(),mainPanel.getPromptsPanel());

        return provider;
    }
}
