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
