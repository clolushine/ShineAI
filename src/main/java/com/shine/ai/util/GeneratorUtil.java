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

package com.shine.ai.util;
import java.util.Random;

public class GeneratorUtil {
    public static String generateUniqueId() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        String randomStr = Integer.toHexString(random.nextInt()); // 使用 Integer.toHexString 生成随机字符串
        return timestamp + "-" + randomStr;
    }

    // 更简洁的版本，使用 UUID (推荐)
    public static String generateWithUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    // 获取时间戳
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }
}
