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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellij.openapi.ui.MessageType;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class FileUtil {
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]"); // Windows and many other systems

    public static final Path cachePath = Paths.get(System.getProperty("user.home"), "Documents", "ShineAI","cache");

    public static final Path exportPath = Paths.get(System.getProperty("user.home"), "Documents", "ShineAI","export");

    public static Path getCachePath(String fileName) {
        Path filePath = Paths.get(String.valueOf(cachePath));
        if (fileName != null && !fileName.isEmpty()) {
            filePath = Paths.get(String.valueOf(cachePath),fileName);
        }
        // 创建父目录，如果不存在
        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            System.err.println("getCachePath fail：" + e.getMessage());
            System.err.println("getCachePath fail：" + filePath);
        }
        return filePath;
    }

    public static long getFolderSize(File folder) {
        long size = 0;
        if (folder.isFile()) {
            size = folder.length();
        } else if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += getFolderSize(file);
                }
            }
        }
        return size;
    }

    public static void exportToJson(JsonObject data, String fileName, JComponent component) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String name = createSafeFileName(fileName);
            Path filePath = Paths.get(String.valueOf(exportPath), name + ".json");
            // 创建父目录，如果不存在
            Files.createDirectories(filePath.getParent());
            try (FileWriter writer = new FileWriter(filePath.toFile())) { // try-with-resources 自动关闭
                gson.toJson(data.asMap(), writer); // Convert to Map
            }
            BalloonUtil.showBalloon("Export successfully：" + filePath, MessageType.INFO,component);
        } catch (IOException e) {
            BalloonUtil.showBalloon("Export fail：" + e.getMessage(), MessageType.ERROR,component);
            System.err.println("Export fail：" + e.getMessage());
        }
    }

    public static String createSafeFileName(String input) {
        if (input == null || input.isEmpty()) {
            return "untitled"; // or throw an exception
        }
        String fileName = INVALID_FILENAME_CHARS.matcher(input).replaceAll("_"); // Replace invalid chars with underscore
        //Further optional sanitization:
        fileName = fileName.trim(); // remove leading/trailing whitespace
        fileName = fileName.replaceAll("\\s+", "_"); // replace multiple spaces with underscore
        //Check for excessively long filenames (OS specific limits)
        if (fileName.length() > 255) { // Adjust limit as needed for your target OS
            fileName = fileName.substring(0, 255);
        }
        // Validate the resulting filename using Paths.get() to catch any remaining issues.  This is crucial!
        try {
            Path path = Paths.get(fileName);
            //If it gets here, the filename is likely valid for the current OS.
            return path.getFileName().toString(); //Return only the filename part
        } catch (InvalidPathException e) {
            //Handle invalid path exceptions.  Log the error, and return a default or throw an exception.
            System.err.println("Invalid filename generated: " + e.getMessage());
            return "invalid_filename"; // Or throw a custom exception
        }
    }

    public static String readJsFile(String path) throws IOException {
        URL url = FileUtil.class.getResource(path);
        try {
            if (url == null) {
                throw new IOException("文件未找到: " + path);
            }
            return Files.readString(Paths.get(url.toURI()), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
