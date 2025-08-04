package com.shine.ai.db.attachs;

import com.google.gson.JsonObject;
import com.shine.ai.db.DBPaths;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttachsManager {
    private final String dbUrl;
    private static final String TABLE_NAME = "attachs";

    // 推荐使用单例模式来管理数据库连接和操作
    private static final AttachsManager INSTANCE;

    static {
        try {
            // 明确地告诉JVM加载SQLite的JDBC驱动类
            Class.forName("org.sqlite.JDBC");

            // 只有驱动加载成功后，才创建实例
            INSTANCE = new AttachsManager();

        } catch (ClassNotFoundException e) {
            // 如果这行代码失败，说明 sqlite-jdbc.jar 没有在你的插件的类路径中。
            // 这是一个致命的配置错误，插件无法继续运行。
            throw new RuntimeException("Fatal Error: SQLite JDBC driver not found.", e);
        }
    }

    private AttachsManager() {
        this.dbUrl = DBPaths.getDatabaseUrl(); // 假设这个方法返回正确的 JDBC URL
        initDB();
    }

    public static AttachsManager getInstance() {
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
                + "    file_name TEXT,\n"
                + "    url TEXT,\n"
                + "    type TEXT,\n"
                + "    mime_type TEXT,\n"
                + "    chat_id TEXT NOT NULL,\n"
                + "    coll_id TEXT NOT NULL,\n"
                + "    meta_data TEXT\n" // Store JSON as a string
                + ");";

        // Index on collection ID and creation time is crucial for performance
        String indexSql = "CREATE INDEX IF NOT EXISTS idx_chat_id_coll_id_created ON " + TABLE_NAME + " (chat_id, coll_id, create_at);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(indexSql);
        } catch (SQLException e) {
            e.printStackTrace(); // Use IDEA logger in a real plugin
        }
    }

    // --- CRUD Operations ---

    public void addAttachs(Attachs attachs) {
//        String sql = "INSERT INTO " + TABLE_NAME + "(id, create_at, update_at, file_name, url, type, mime_type, chat_id, coll_id, meta_data) " +
//                "VALUES(?,?,?,?,?,?,?,?,?,?)";

        String sql = "INSERT INTO " + TABLE_NAME + " (id, create_at, update_at, file_name, url, type, mime_type, chat_id, coll_id, meta_data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "update_at = EXCLUDED.update_at, " + // 注意逗号后的空格
                "file_name = EXCLUDED.file_name, " +
                "url = EXCLUDED.url, " +
                "type = EXCLUDED.type, " +
                "mime_type = EXCLUDED.mime_type, " +
                "chat_id = EXCLUDED.chat_id, " +
                "coll_id = EXCLUDED.coll_id, " +
                "meta_data = EXCLUDED.meta_data";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            mapAttachsToStatement(pstmt, attachs);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Attachs findById(String id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        Attachs attachs = null;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                attachs = mapRowToAttachs(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attachs;
    }

    public List<JsonObject> findByChatId(String chatId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE chat_id = ? ORDER BY create_at ASC";
        List<JsonObject> attachs = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chatId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attachs.add(mapRowToAttachs(rs).getJsonObjectAll());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attachs;
    }

    public List<JsonObject> findByCollId(String collId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE coll_id = ? ORDER BY create_at ASC";
        List<JsonObject> attachs = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attachs.add(mapRowToAttachs(rs).getJsonObjectAll());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attachs;
    }

    public boolean updateAttachs(Attachs attachs) {
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + "create_at = ?, update_at = ?, file_name = ?, url = ?, type = ?, mime_type = ?, "
                + "chat_id = ?, coll_id = ?, meta_data = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Parameters are shifted by one due to the WHERE clause at the end
            pstmt.setLong(1, attachs.getCreateAt());
            pstmt.setLong(2, attachs.getUpdateAt());
            pstmt.setString(3, attachs.getFileName());
            pstmt.setString(4, attachs.getUrl());
            pstmt.setString(5, attachs.getType());
            pstmt.setString(6, attachs.getMimeType());
            pstmt.setString(7, attachs.getChatId());
            pstmt.setString(8, attachs.getCollId());
            pstmt.setString(9, attachs.getMetaDataAsJsonString());

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

    public void delByChatId(String chatId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE chat_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chatId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delByCollId(String collId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE coll_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // --- Helper Methods ---

    private void mapAttachsToStatement(PreparedStatement pstmt, Attachs c) throws SQLException {
        pstmt.setString(1, c.getId());
        pstmt.setLong(2, c.getCreateAt());
        pstmt.setLong(3, c.getUpdateAt());
        pstmt.setString(4, c.getFileName());
        pstmt.setString(5, c.getUrl());
        pstmt.setString(6, c.getType());
        pstmt.setString(7, c.getMimeType());
        pstmt.setString(8, c.getChatId());
        pstmt.setString(9, c.getCollId());
        pstmt.setString(10, c.getMetaDataAsJsonString());
    }

    private Attachs mapRowToAttachs(ResultSet rs) throws SQLException {
        return new Attachs(
                rs.getString("id"),
                rs.getLong("create_at"),
                rs.getLong("update_at"),
                rs.getString("file_name"),
                rs.getString("url"),
                rs.getString("type"),
                rs.getString("mime_type"),
                rs.getString("chat_id"),
                rs.getString("coll_id"),
                rs.getString("meta_data")
        );
    }
}
