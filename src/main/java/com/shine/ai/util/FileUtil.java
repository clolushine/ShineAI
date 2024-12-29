package com.shine.ai.util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellij.openapi.ui.MessageType;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    public static void exportToJson(JsonObject data, String fileName, JComponent component) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path filePath = Paths.get(System.getProperty("user.home"), "Documents", "ShineAI", fileName + ".json");
        try {
            // 创建父目录，如果不存在
            Files.createDirectories(filePath.getParent());
            try (FileWriter writer = new FileWriter(filePath.toFile())) { // try-with-resources 自动关闭
                gson.toJson(data.asMap(), writer); // Convert to Map
            }
            BalloonUtil.showBalloon("Export successfully：" + filePath, MessageType.INFO,component);
            System.out.println("Export successfully: " + filePath);
        } catch (IOException e) {
            e.printStackTrace(); // 在实际应用中，你可能需要更精细的错误处理
            BalloonUtil.showBalloon("Export fail：" + e.getMessage(), MessageType.ERROR,component);
            System.err.println("Export fail：" + e.getMessage());
        }
    }
}
