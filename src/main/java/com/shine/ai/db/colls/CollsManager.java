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

package com.shine.ai.db.colls;

import com.google.gson.JsonObject;
import com.shine.ai.db.DBPaths;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollsManager {
    private final String dbUrl;
    private static final String TABLE_NAME = "colls";

    // 推荐使用单例模式来管理数据库连接和操作
    private static final CollsManager INSTANCE;

    static {
        try {
            // 明确地告诉JVM加载SQLite的JDBC驱动类
            Class.forName("org.sqlite.JDBC");

            // 只有驱动加载成功后，才创建实例
            INSTANCE = new CollsManager();

        } catch (ClassNotFoundException e) {
            // 如果这行代码失败，说明 sqlite-jdbc.jar 没有在你的插件的类路径中。
            // 这是一个致命的配置错误，插件无法继续运行。
            throw new RuntimeException("Fatal Error: SQLite JDBC driver not found.", e);
        }
    }

    private CollsManager() {
        this.dbUrl = DBPaths.getDatabaseUrl(); // 假设这个方法返回正确的 JDBC URL
        initDB();
    }

    public static CollsManager getInstance() {
        return INSTANCE;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initDB() {
        // 使用 TEXT for id (UUID) and INTEGER for boolean
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (\n"
                + "    id TEXT PRIMARY KEY,\n"
                + "    create_at INTEGER NOT NULL,\n"
                + "    update_at INTEGER NOT NULL,\n"
                + "    title TEXT,\n"
                + "    sub_title TEXT\n"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace(); // Use IDEA logger in a real plugin
        }
    }

    // --- CRUD Operations ---

    public void addColls(Colls colls) {
//        String sql = "INSERT INTO " + TABLE_NAME + " (id, create_at, update_at, title, sub_title) " +
//                "VALUES(?,?,?,?,?)";

        String sql = "INSERT INTO " + TABLE_NAME + " (id, create_at, update_at, title, sub_title) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "update_at = EXCLUDED.update_at, " + // 注意逗号后的空格
                "title = EXCLUDED.title, " +
                "sub_title = EXCLUDED.sub_title";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            mapCollsToStatement(pstmt, colls);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<JsonObject> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY update_at DESC";
        List<JsonObject> colls = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                colls.add(mapRowToColls(rs).getJsonObjectAll());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return colls;
    }

    public Colls findLatestOne() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY update_at DESC LIMIT 1";
        Colls colls = null;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                colls = mapRowToColls(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return colls;
    }

    public Colls findById(String id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        Colls colls = null;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                colls = mapRowToColls(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return colls;
    }

    public int findAllCounts() {
        String sql = "SELECT count(*) FROM " + TABLE_NAME + " ";

        int count = 0;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 执行查询，ResultSet会包含查询结果
            ResultSet rs = pstmt.executeQuery();
            // 检查是否有结果行，count(*) 查询总是会返回一行
            if (rs.next()) {
                // 从ResultSet中获取第一个（也是唯一一个）列的值，它就是计数
                count = rs.getInt(1); // 可以使用列索引（从1开始）
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count; // 返回获取到的数量
    }

    public boolean updateColls(Colls colls) {
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + "create_at = ?, update_at = ?, title = ?, sub_title = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Parameters are shifted by one due to the WHERE clause at the end
            pstmt.setLong(1, colls.getCreateAt());
            pstmt.setLong(2, colls.getUpdateAt());
            pstmt.setString(3, colls.getTitle());
            pstmt.setString(4, colls.getSubTitle());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delById(String id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Helper Methods ---

    private void mapCollsToStatement(PreparedStatement pstmt, Colls c) throws SQLException {
        pstmt.setString(1, c.getId());
        pstmt.setLong(2, c.getCreateAt());
        pstmt.setLong(3, c.getUpdateAt());
        pstmt.setString(4, c.getTitle());
        pstmt.setString(5, c.getSubTitle());
    }

    private Colls mapRowToColls(ResultSet rs) throws SQLException {
        return new Colls(
                rs.getString("id"),
                rs.getLong("create_at"),
                rs.getLong("update_at"),
                rs.getString("title"),
                rs.getString("sub_title")
        );
    }
}
