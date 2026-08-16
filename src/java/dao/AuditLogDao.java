package dao;

import model.AuditLog;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {
    public void log(Long actorId, String action, String targetType, Long targetId,
                    String detail, String ipAddress) {
        try {
            ensureTable();
            String sql = """
                    INSERT INTO system_logs(actor_id, action, target_type, target_id, detail, ip_address)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            try (Connection connection = DBConnectionUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                if (actorId == null) statement.setNull(1, java.sql.Types.BIGINT);
                else statement.setLong(1, actorId);
                statement.setString(2, action);
                statement.setString(3, targetType);
                if (targetId == null) statement.setNull(4, java.sql.Types.BIGINT);
                else statement.setLong(4, targetId);
                statement.setString(5, detail);
                statement.setString(6, ipAddress);
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {
            // Logging must not break the user workflow.
        }
    }

    public List<AuditLog> findRecent(int limit, String keyword) throws SQLException {
        ensureTable();
        int safeLimit = Math.max(10, Math.min(limit, 500));
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String sql = """
                SELECT l.id, l.actor_id, a.full_name AS actor_name, l.action,
                       l.target_type, l.target_id, l.detail, l.ip_address, l.created_at
                FROM system_logs l
                LEFT JOIN accounts a ON a.id = l.actor_id
                """;
        if (hasKeyword) {
            sql += " WHERE l.action LIKE ? OR l.target_type LIKE ? OR l.detail LIKE ? OR a.full_name LIKE ? ";
        }
        sql += " ORDER BY l.created_at DESC, l.id DESC LIMIT " + safeLimit;
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                statement.setString(1, like);
                statement.setString(2, like);
                statement.setString(3, like);
                statement.setString(4, like);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<AuditLog> logs = new ArrayList<>();
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getLong("id"));
                    long actorId = rs.getLong("actor_id");
                    log.setActorId(rs.wasNull() ? null : actorId);
                    log.setActorName(rs.getString("actor_name"));
                    log.setAction(rs.getString("action"));
                    log.setTargetType(rs.getString("target_type"));
                    long targetId = rs.getLong("target_id");
                    log.setTargetId(rs.wasNull() ? null : targetId);
                    log.setDetail(rs.getString("detail"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    logs.add(log);
                }
                return logs;
            }
        }
    }

    public void ensureTable() throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS system_logs (
                      id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                      actor_id BIGINT UNSIGNED NULL,
                      action VARCHAR(80) NOT NULL,
                      target_type VARCHAR(80) NULL,
                      target_id BIGINT UNSIGNED NULL,
                      detail VARCHAR(1000) NULL,
                      ip_address VARCHAR(64) NULL,
                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (id),
                      KEY idx_system_logs_created_at (created_at),
                      KEY idx_system_logs_actor (actor_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);
        }
    }
}
