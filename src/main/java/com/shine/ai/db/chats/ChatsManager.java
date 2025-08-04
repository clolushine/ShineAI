package com.shine.ai.db.chats;

import com.google.gson.JsonObject;
import com.shine.ai.db.DBPaths;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatsManager {
    private final String dbUrl;
    private static final String TABLE_NAME = "chats";

    // 推荐使用单例模式来管理数据库连接和操作
    private static final ChatsManager INSTANCE;

    static {
        try {
            // 明确地告诉JVM加载SQLite的JDBC驱动类
            Class.forName("org.sqlite.JDBC");

            // 只有驱动加载成功后，才创建实例
            INSTANCE = new ChatsManager();

        } catch (ClassNotFoundException e) {
            // 如果这行代码失败，说明 sqlite-jdbc.jar 没有在你的插件的类路径中。
            // 这是一个致命的配置错误，插件无法继续运行。
            throw new RuntimeException("Fatal Error: SQLite JDBC driver not found.", e);
        }
    }

    private ChatsManager() {
        this.dbUrl = DBPaths.getDatabaseUrl(); // 假设这个方法返回正确的 JDBC URL
        initDB();
    }

    public static ChatsManager getInstance() {
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
                + "    name TEXT,\n"
                + "    icon TEXT,\n"
                + "    content TEXT,\n"
                + "    role TEXT NOT NULL,\n"
                + "    status INTEGER,\n"
                + "    finish_reason TEXT,\n"
                + "    from_llm TEXT,\n"
                + "    from_provider TEXT,\n"
                + "    coll_id TEXT NOT NULL,\n"
                + "    web_search INTEGER DEFAULT 0,\n"  // 0 for false, 1 for true
                + "    attachments TEXT\n" // Store JSON as a string
                + ");";

        // Index on collection ID and creation time is crucial for performance
        String indexSql = "CREATE INDEX IF NOT EXISTS idx_coll_id_created ON " + TABLE_NAME + " (coll_id, create_at);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(indexSql);
        } catch (SQLException e) {
            e.printStackTrace(); // Use IDEA logger in a real plugin
        }
    }

    // --- CRUD Operations ---

    public void addChats(Chats chats) {
//        String sql = "INSERT INTO " + TABLE_NAME + "(id, create_at, update_at, name, icon, content, role, status, finish_reason, from_llm, from_provider, coll_id, web_search, attachments) " +
//                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        String sql = "INSERT INTO " + TABLE_NAME + " (id, create_at, update_at, name, icon, content, role, status, finish_reason, from_llm, from_provider, coll_id, web_search, attachments) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "update_at = EXCLUDED.update_at, " + // 注意逗号后的空格
                "name = EXCLUDED.name, " +
                "icon = EXCLUDED.icon, " +
                "content = EXCLUDED.content, " +
                "role = EXCLUDED.role, " +
                "status = EXCLUDED.status, " +
                "finish_reason = EXCLUDED.finish_reason, " +
                "from_llm = EXCLUDED.from_llm, " +
                "from_provider = EXCLUDED.from_provider, " +
                "coll_id = EXCLUDED.coll_id, " +
                "web_search = EXCLUDED.web_search, " +
                "attachments = EXCLUDED.attachments";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            mapChatsToStatement(pstmt, chats);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Chats findById(String id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        Chats chats = null;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                chats = mapRowToChats(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chats;
    }

    public List<JsonObject> findByCollIdAll(String collId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE coll_id = ? ORDER BY update_at ASC";
        List<JsonObject> chats = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                chats.add(mapRowToChats(rs).getJsonObjectAll());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chats;
    }

    public List<JsonObject> findByCollId(String collId, int page, int pageSize, boolean ascOrder) {
        String order = ascOrder ? "ASC" : "DESC";
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE coll_id = ? ORDER BY update_at " + order + " LIMIT ? OFFSET ?";

        List<JsonObject> chats = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collId);   // WHERE coll_id = ?
            pstmt.setInt(2, pageSize);    // LIMIT ?
            pstmt.setInt(3, offset);      // OFFSET ?

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                chats.add(mapRowToChats(rs).getJsonObjectAll());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chats;
    }

    public int findByCollIdCounts(String collId) {
        String sql = "SELECT count(*) FROM " + TABLE_NAME + " WHERE coll_id = ?";

        int count = 0;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collId);
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

    public boolean updateChats(Chats chats) {
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + "create_at = ?, update_at = ?, name = ?, icon = ?, content = ?, role = ?, "
                + "status = ?, finish_reason = ?, from_llm = ?, from_provider = ?, "
                + "coll_id = ?, web_search = ?, attachments = ? "
                + "WHERE id = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Parameters are shifted by one due to the WHERE clause at the end
            pstmt.setLong(1, chats.getCreateAt());
            pstmt.setLong(2, chats.getUpdateAt());
            pstmt.setString(3, chats.getName());
            pstmt.setString(4, chats.getIcon());
            pstmt.setString(5, chats.getContent());
            pstmt.setString(6, chats.getRole());
            pstmt.setInt(7, chats.getStatus());
            pstmt.setString(8, chats.getFinishReason());
            pstmt.setString(9, chats.getFromLLM());
            pstmt.setString(10, chats.getFromProvider());
            pstmt.setString(11, chats.getCollId());
            pstmt.setInt(12, chats.isWebSearch() ? 1 : 0);
            pstmt.setString(13, chats.getAttachmentsAsJsonString());
            pstmt.setString(14, chats.getId()); // WHERE clause parameter

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

    private void mapChatsToStatement(PreparedStatement pstmt, Chats c) throws SQLException {
        pstmt.setString(1, c.getId());
        pstmt.setLong(2, c.getCreateAt());
        pstmt.setLong(3, c.getUpdateAt());
        pstmt.setString(4, c.getName());
        pstmt.setString(5, c.getIcon());
        pstmt.setString(6, c.getContent());
        pstmt.setString(7, c.getRole());
        pstmt.setInt(8, c.getStatus());
        pstmt.setString(9, c.getFinishReason());
        pstmt.setString(10, c.getFromLLM());
        pstmt.setString(11, c.getFromProvider());
        pstmt.setString(12, c.getCollId());
        pstmt.setInt(13, c.isWebSearch() ? 1 : 0);
        pstmt.setString(14, c.getAttachmentsAsJsonString());
    }

    private Chats mapRowToChats(ResultSet rs) throws SQLException {
        return new Chats(
                rs.getString("id"),
                rs.getLong("create_at"),
                rs.getLong("update_at"),
                rs.getString("name"),
                rs.getString("icon"),
                rs.getString("content"),
                rs.getString("role"),
                rs.getInt("status"),
                rs.getString("finish_reason"),
                rs.getString("from_llm"),
                rs.getString("from_provider"),
                rs.getString("coll_id"),
                rs.getInt("web_search") == 1,
                rs.getString("attachments")
        );
    }
}
