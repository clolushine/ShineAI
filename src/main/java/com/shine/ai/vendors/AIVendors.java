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

package com.shine.ai.vendors;

import com.intellij.openapi.options.Configurable;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.settings.*;

import java.util.HashMap;
import java.util.Map;

public class AIVendors {
    public static final String CLOUDFLARE_AI_CONTENT_NAME = "CloudflareAI";
    public static final String CLOUDFLARE_AI_KEY = "CLOUDFLARE";
    public static final String CLOUDFLARE_AI_NAME = "CF AI";
    public static final String CLOUDFLARE_AI_ICON = AIAssistantIcons.CF_AI_URL;
    public static final String CLOUDFLARE_AI_API = "/ai/aiChat";
    public static final String CLOUDFLARE_AI_LLM_API = "/ai/aiModels";
    public static final String CLOUDFLARE_SETTING_CLASS_NAME = CFAISettingPanel.class.getName();

    public static final String Google_AI_CONTENT_NAME = "GoogleAI";
    public static final String Google_AI_KEY = "GOOGLE";
    public static final String Google_AI_NAME = "GEMINI";
    public static final String Google_AI_ICON = AIAssistantIcons.GOOGLE_AI_URL;
    public static final String Google_AI_API = "/gem/geminiChat";
    public static final String Google_AI_LLM_API = "/gem/gems";
    public static final String Google_SETTING_CLASS_NAME = GoogleAISettingPanel.class.getName();

    public static final String GROQ_AI_CONTENT_NAME = "GroqAI";
    public static final String GROQ_AI_KEY = "GROQ";
    public static final String GROQ_AI_NAME = "GROQ";
    public static final String GROQ_AI_ICON = AIAssistantIcons.GROQ_AI_URL;
    public static final String GROQ_AI_API = "/gpt/gptChat";
    public static final String GROQ_AI_LLM_API = "/gpt/models";
    public static final String GROQ_SETTING_CLASS_NAME = GroqAISettingPanel.class.getName();

    // 新增AI
    public static final String OpenAI_CONTENT_NAME = "OpenAI";
    public static final String OpenAI_KEY = "OPENAI";
    public static final String OpenAI_NAME = "OPENAI";
    public static final String OpenAI_ICON = AIAssistantIcons.OPENAI_URL;
    public static final String OpenAI_AI_API = "/gpt/gptChat";
    public static final String OpenAI_LLM_API = "/gpt/models";
    public static final String OpenAI_SETTING_CLASS_NAME = OpenAISettingPanel.class.getName();

    public static final String Anthropic_AI_CONTENT_NAME = "AnthropicAI";
    public static final String Anthropic_AI_KEY = "ANTHROPIC";
    public static final String Anthropic_AI_NAME = "ANTHR";
    public static final String Anthropic_AI_ICON = AIAssistantIcons.ANTHROPIC_AI_URL;
    public static final String Anthropic_AI_API = "/gpt/gptChat";
    public static final String Anthropic_AI_LLM_API = "/gpt/models";
    public static final String Anthropic_SETTING_CLASS_NAME = AnthropicAISettingPanel.class.getName();

    public static final String OpenRouter_AI_CONTENT_NAME = "OpenRouterAI";
    public static final String OpenRouter_AI_KEY = "OPENROUTER";
    public static final String OpenRouter_AI_NAME = "ROUTER";
    public static final String OpenRouter_AI_ICON = AIAssistantIcons.OPENROUTER_AI_URL;
    public static final String OpenRouter_AI_API = "/gpt/gptChat";
    public static final String OpenRouter_AI_LLM_API = "/gpt/models";
    public static final String OpenRouter_SETTING_CLASS_NAME = OpenRouterAISettingPanel.class.getName();


    public static final Map<String, Class<? extends Configurable>> MAPPINGS = new HashMap<>() {{
        put(CLOUDFLARE_SETTING_CLASS_NAME, CFAISettingPanel.class);
        put(Google_SETTING_CLASS_NAME, GoogleAISettingPanel.class);
        put(GROQ_SETTING_CLASS_NAME, GroqAISettingPanel.class);
        put(OpenAI_SETTING_CLASS_NAME, OpenAISettingPanel.class);
        put(Anthropic_SETTING_CLASS_NAME, AnthropicAISettingPanel.class);
        put(OpenRouter_SETTING_CLASS_NAME, OpenRouterAISettingPanel.class);
    }};
}
