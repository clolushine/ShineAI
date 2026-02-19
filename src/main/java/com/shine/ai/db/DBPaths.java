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

package com.shine.ai.db;

import com.intellij.openapi.application.PathManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DBPaths {
    public static final Path dbPath = Paths.get(PathManager.getConfigPath(), "data");
    public static final String dbName = "shineai.db";

    public static Path getPluginDataPath() {
        // PathManager.getPluginsPath() 指向所有插件的安装目录
        // 例如：C:\Users\YourUser\AppData\Roaming\JetBrains\IntelliJIdea2023.3\plugins\ShineAI\data
        return dbPath;
    }

    public static String getDatabaseUrl() {
        Path pluginDataDir = getPluginDataPath();
        // 确保目录存在
        try {
            Files.createDirectories(pluginDataDir);
        } catch (IOException e) {
            // 在实际插件中，这里应该用 Logger 记录错误
            e.printStackTrace();
            // 返回一个内存数据库作为备用，防止完全崩溃
            return "jdbc:sqlite::memory:";
        }
        Path dbPath = pluginDataDir.resolve(dbName);
        // JDBC URL for SQLite
        return "jdbc:sqlite:" + dbPath.toAbsolutePath();
    }
}
