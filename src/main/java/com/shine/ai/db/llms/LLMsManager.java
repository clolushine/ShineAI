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

package com.shine.ai.db.llms;

import com.shine.ai.db.DBPaths;

import java.sql.*;

public class LLMsManager {
    private final String dbUrl;
    private static final String TABLE_NAME = "llms";

    // 推荐使用单例模式来管理数据库连接和操作
    private static final LLMsManager INSTANCE;

    static {
        try {
            // 明确地告诉JVM加载SQLite的JDBC驱动类
            Class.forName("org.sqlite.JDBC");

            // 只有驱动加载成功后，才创建实例
            INSTANCE = new LLMsManager();

        } catch (ClassNotFoundException e) {
            // 如果这行代码失败，说明 sqlite-jdbc.jar 没有在你的插件的类路径中。
            // 这是一个致命的配置错误，插件无法继续运行。
            throw new RuntimeException("Fatal Error: SQLite JDBC driver not found.", e);
        }
    }

    private LLMsManager() {
        this.dbUrl = DBPaths.getDatabaseUrl(); // 假设这个方法返回正确的 JDBC URL
        initDB();
    }

    public static LLMsManager getInstance() {
        return INSTANCE;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initDB() {
        // 使用 TEXT for id (UUID) and INTEGER for boolean
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (\n"
                + "    key TEXT PRIMARY KEY,\n"
                + "    update_at INTEGER NOT NULL,\n"
                + "    provider TEXT,\n"
                + "    llms TEXT\n"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace(); // Use IDEA logger in a real plugin
        }
    }

    // --- CRUD Operations ---

    public void addLLMs(LLMs llms) {
//        String sql = "INSERT INTO " + TABLE_NAME + "(key, update_at, provider, llms) " +
//                "VALUES(?,?,?,?,?)";

        String sql = "INSERT INTO " + TABLE_NAME + " (key, update_at, provider, llms) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (key) DO UPDATE SET " +
                "update_at = EXCLUDED.update_at, " + // 注意逗号后的空格
                "provider = EXCLUDED.provider, " +
                "llms = EXCLUDED.llms";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            mapLLMsToStatement(pstmt, llms);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LLMs findByKey(String key) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE key = ?";
        LLMs llms = null;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                llms = mapRowToLLMs(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return llms;
    }

    public boolean updateLLMs(LLMs llms) {
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + "update_at = ?, key = ?, provider = ?, llms = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Parameters are shifted by one due to the WHERE clause at the end
            pstmt.setLong(1, llms.getUpdateAt());
            pstmt.setString(2, llms.getKey());
            pstmt.setString(3, llms.getProvider());
            pstmt.setString(4, llms.getLLMsAsJsonString());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delByKey(String key) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE key = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Helper Methods ---

    private void mapLLMsToStatement(PreparedStatement pstmt, LLMs c) throws SQLException {
        pstmt.setString(1, c.getKey());
        pstmt.setLong(2, c.getUpdateAt());
        pstmt.setString(3, c.getProvider());
        pstmt.setString(4, c.getLLMsAsJsonString());
    }

    private LLMs mapRowToLLMs(ResultSet rs) throws SQLException {
        return new LLMs(
                rs.getString("key"),
                rs.getLong("update_at"),
                rs.getString("provider"),
                rs.getString("llms")
        );
    }
}
