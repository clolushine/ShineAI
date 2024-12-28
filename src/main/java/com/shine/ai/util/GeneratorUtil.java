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
