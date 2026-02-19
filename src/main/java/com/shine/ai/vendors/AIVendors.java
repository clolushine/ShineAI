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

    public static final String DeepSeek_AI_CONTENT_NAME = "DeepSeekAI";
    public static final String DeepSeek_AI_KEY = "DEEPSEEK";
    public static final String DeepSeek_AI_NAME = "DEEPSEEK";
    public static final String DeepSeek_AI_ICON = AIAssistantIcons.DEEPSEEK_AI_URL;
    public static final String DeepSeek_AI_API = "/gpt/gptChat";
    public static final String DeepSeek_AI_LLM_API = "/gpt/models";
    public static final String DeepSeek_SETTING_CLASS_NAME = DeepSeekAISettingPanel.class.getName();

    public static final String Nvidia_AI_CONTENT_NAME = "NvidiaAI";
    public static final String Nvidia_AI_KEY = "NVIDIA";
    public static final String Nvidia_AI_NAME = "NVIDIA";
    public static final String Nvidia_AI_ICON = AIAssistantIcons.NVIDIA_AI_URL;
    public static final String Nvidia_AI_API = "/gpt/gptChat";
    public static final String Nvidia_AI_LLM_API = "/gpt/models";
    public static final String Nvidia_SETTING_CLASS_NAME = NvidiaAISettingPanel.class.getName();


    public static final Map<String, Class<? extends Configurable>> MAPPINGS = new HashMap<>() {{
        put(CLOUDFLARE_SETTING_CLASS_NAME, CFAISettingPanel.class);
        put(Google_SETTING_CLASS_NAME, GoogleAISettingPanel.class);
        put(GROQ_SETTING_CLASS_NAME, GroqAISettingPanel.class);
        put(OpenAI_SETTING_CLASS_NAME, OpenAISettingPanel.class);
        put(Anthropic_SETTING_CLASS_NAME, AnthropicAISettingPanel.class);
        put(OpenRouter_SETTING_CLASS_NAME, OpenRouterAISettingPanel.class);
        put(DeepSeek_SETTING_CLASS_NAME, DeepSeekAISettingPanel.class);
        put(Nvidia_SETTING_CLASS_NAME, NvidiaAISettingPanel.class);
    }};
}
