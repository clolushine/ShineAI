package com.shine.ai.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    public static Path exportToJson(JsonObject data, String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // 美化输出
        Path documentsDir = Paths.get(System.getProperty("user.home"), "Documents");
        Path shineAIPath = documentsDir.resolve("ShineAI");
        Path filePath = shineAIPath.resolve(fileName + ".json");
        try {
            // 创建父目录，如果不存在
            Files.createDirectories(filePath.getParent());
            FileWriter writer = new FileWriter(filePath.toFile());
            gson.toJson(data.asMap(), writer); // Convert to Map
            writer.close();
            System.out.println("JSON 数据已成功导出到: " + filePath);
            return filePath;
        } catch (IOException e) {
            e.printStackTrace(); // 在实际应用中，你可能需要更精细的错误处理
            System.err.println("导出 JSON 数据失败: " + e.getMessage());
            return filePath;
        }
    }
}
