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
