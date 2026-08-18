package dao;

import model.AuditLog;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class AuditLogDao {
    public void log(Long actorId, String action, String targetType, Long targetId,
                    String detail, String ipAddress) {
        try {
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
            // Logging should be best-effort and must not break the user flow.
        }
    }

    public List<AuditLog> findRecent(int limit, String keyword) throws SQLException {
        return findRecentPage(limit, 0, keyword);
    }

    public List<AuditLog> findRecentPage(int pageSize, int offset, String keyword) throws SQLException {
        if (!tableExists()) {
            return new ArrayList<>();
        }
        int safeLimit = Math.max(10, Math.min(pageSize, 500));
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
        sql += " ORDER BY l.created_at DESC, l.id DESC LIMIT ? OFFSET ?";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                statement.setString(index++, like);
                statement.setString(index++, like);
                statement.setString(index++, like);
                statement.setString(index++, like);
            }
            statement.setInt(index++, safeLimit);
            statement.setInt(index, Math.max(0, offset));
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

    public int countRecent(String keyword) throws SQLException {
        if (!tableExists()) {
            return 0;
        }
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String sql = """
                SELECT COUNT(*)
                FROM system_logs l
                LEFT JOIN accounts a ON a.id = l.actor_id
                """;
        if (hasKeyword) {
            sql += " WHERE l.action LIKE ? OR l.target_type LIKE ? OR l.detail LIKE ? OR a.full_name LIKE ? ";
        }
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
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AuditLog> findByAction(String action, int limit) throws SQLException {
        if (!tableExists()) {
            return new ArrayList<>();
        }
        int safeLimit = Math.max(10, Math.min(limit, 500));
        String sql = """
                SELECT l.id, l.actor_id, a.full_name AS actor_name, l.action,
                       l.target_type, l.target_id, l.detail, l.ip_address, l.created_at
                FROM system_logs l
                LEFT JOIN accounts a ON a.id = l.actor_id
                WHERE l.action = ?
                ORDER BY l.created_at DESC, l.id DESC
                """ + " LIMIT " + safeLimit;
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, action);
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

    public List<AuditLog> findRoomChangeHistory(String bookingCode, LocalDate fromDate,
                                                LocalDate toDate, Long receptionistId,
                                                int limit) throws SQLException {
        if (!tableExists()) {
            return new ArrayList<>();
        }
        int safeLimit = Math.max(10, Math.min(limit, 500));
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT l.id, l.actor_id, a.full_name AS actor_name, l.action, ");
        sql.append("l.target_type, l.target_id, b.booking_code, l.detail, l.ip_address, l.created_at ");
        sql.append("FROM system_logs l ");
        sql.append("LEFT JOIN accounts a ON a.id = l.actor_id ");
        sql.append("LEFT JOIN bookings b ON b.id = l.target_id ");
        sql.append("WHERE l.action = 'ROOM_CHANGE'");
        List<Object> params = new ArrayList<>();
        if (bookingCode != null && !bookingCode.isBlank()) {
            sql.append(" AND b.booking_code LIKE ? ");
            params.add("%" + bookingCode.trim() + "%");
        }
        if (fromDate != null) {
            sql.append(" AND DATE(l.created_at) >= ? ");
            params.add(java.sql.Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append(" AND DATE(l.created_at) <= ? ");
            params.add(java.sql.Date.valueOf(toDate));
        }
        if (receptionistId != null) {
            sql.append(" AND l.actor_id = ? ");
            params.add(receptionistId);
        }
        sql.append(" ORDER BY l.created_at DESC, l.id DESC LIMIT ?");
        params.add(safeLimit);

        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                int index = i + 1;
                if (param instanceof String) {
                    statement.setString(index, (String) param);
                } else if (param instanceof java.sql.Date) {
                    statement.setDate(index, (java.sql.Date) param);
                } else if (param instanceof Long) {
                    statement.setLong(index, (Long) param);
                } else if (param instanceof Integer) {
                    statement.setInt(index, (Integer) param);
                } else {
                    statement.setObject(index, param);
                }
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
                    log.setBookingCode(rs.getString("booking_code"));
                    log.setDetail(rs.getString("detail"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    logs.add(log);
                }
                return logs;
            }
        }
    }

    private boolean tableExists() throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection();
             ResultSet rs = connection.getMetaData().getTables(null, null, "system_logs", null)) {
            return rs.next();
        }
    }
}
