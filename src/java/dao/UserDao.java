package dao;

import model.User;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.time.LocalDateTime;

public class UserDao {
    private static final String FIND_BY_EMAIL = """
            SELECT user_id, full_name, email, phone, password_hash, role_id, status, created_at
            FROM (
                SELECT user_id, full_name, email, phone, password_hash, role_id, status, created_at
                FROM `user` WHERE LOWER(email) = LOWER(?)
                UNION ALL
                SELECT guest_id AS user_id, full_name, email, phone, password_hash,
                       0 AS role_id, 'ACTIVE' AS status, created_at
                FROM guest WHERE has_account = 1 AND LOWER(email) = LOWER(?)
            ) account
            LIMIT 1
            """;
    private static final String CREATE_CUSTOMER = """
            INSERT INTO guest (full_name, phone, email, has_account, password_hash, created_at)
            VALUES (?, ?, ?, 1, ?, CURRENT_TIMESTAMP)
            """;

    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection()) {
            if (connection == null) {
                throw new SQLException("Không thể kết nối tới cơ sở dữ liệu");
            }
            try (PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {
                statement.setString(1, email);
                statement.setString(2, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    User user = new User();
                    user.setUserId(resultSet.getInt("user_id"));
                    user.setFullName(resultSet.getString("full_name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPhone(resultSet.getString("phone"));
                    user.setPasswordHash(resultSet.getString("password_hash"));
                    user.setRoleId(resultSet.getInt("role_id"));
                    user.setStatus(resultSet.getString("status"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at"));
                    return Optional.of(user);
                }
            }
        }
    }

    public User createCustomer(String fullName, String email, String phone, String passwordHash)
            throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection()) {
            if (connection == null) {
                throw new SQLException("Không thể kết nối tới cơ sở dữ liệu");
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    CREATE_CUSTOMER, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, fullName);
                statement.setString(2, phone);
                statement.setString(3, email);
                statement.setString(4, passwordHash);
                if (statement.executeUpdate() == 0) {
                    throw new SQLException("Không thể tạo tài khoản khách hàng");
                }
            }
        }
        return findByEmail(email).orElseThrow(
                () -> new SQLException("Không đọc được tài khoản vừa tạo"));
    }

    public User findOrCreateGoogleCustomer(String fullName, String email, String randomPasswordHash)
            throws SQLException {
        Optional<User> existing = findByEmail(email);
        if (existing.isPresent()) return existing.get();
        return createCustomer(fullName, email, null, randomPasswordHash);
    }

    public void savePasswordResetToken(User user, String tokenHash, LocalDateTime expiresAt)
            throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection()) {
            if (connection == null) throw new SQLException("Không thể kết nối tới cơ sở dữ liệu");
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS password_reset_token (
                        token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        account_type VARCHAR(10) NOT NULL,
                        account_id INT NOT NULL,
                        token_hash CHAR(64) NOT NULL UNIQUE,
                        expires_at DATETIME NOT NULL,
                        used_at DATETIME NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_reset_token_hash (token_hash)
                    )
                    """);
            }
            try (PreparedStatement invalidate = connection.prepareStatement(
                    "UPDATE password_reset_token SET used_at = CURRENT_TIMESTAMP "
                            + "WHERE account_type = ? AND account_id = ? AND used_at IS NULL")) {
                invalidate.setString(1, user.getRoleId() == 0 ? "GUEST" : "USER");
                invalidate.setInt(2, user.getUserId());
                invalidate.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO password_reset_token(account_type, account_id, token_hash, expires_at) VALUES(?,?,?,?)")) {
                insert.setString(1, user.getRoleId() == 0 ? "GUEST" : "USER");
                insert.setInt(2, user.getUserId());
                insert.setString(3, tokenHash);
                insert.setTimestamp(4, java.sql.Timestamp.valueOf(expiresAt));
                insert.executeUpdate();
            }
        }
    }

    public Optional<User> consumePasswordResetToken(String tokenHash, String passwordHash)
            throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection()) {
            if (connection == null) throw new SQLException("Không thể kết nối tới cơ sở dữ liệu");
            connection.setAutoCommit(false);
            try {
                String type;
                int id;
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT account_type, account_id FROM password_reset_token "
                                + "WHERE token_hash=? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP FOR UPDATE")) {
                    select.setString(1, tokenHash);
                    try (ResultSet rs = select.executeQuery()) {
                        if (!rs.next()) { connection.rollback(); return Optional.empty(); }
                        type = rs.getString(1); id = rs.getInt(2);
                    }
                }
                String updateSql = "GUEST".equals(type)
                        ? "UPDATE guest SET password_hash=? WHERE guest_id=? AND has_account=1"
                        : "UPDATE `user` SET password_hash=? WHERE user_id=?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setString(1, passwordHash); update.setInt(2, id);
                    if (update.executeUpdate() == 0) { connection.rollback(); return Optional.empty(); }
                }
                try (PreparedStatement used = connection.prepareStatement(
                        "UPDATE password_reset_token SET used_at=CURRENT_TIMESTAMP WHERE token_hash=?")) {
                    used.setString(1, tokenHash); used.executeUpdate();
                }
                connection.commit();
                return Optional.of(new User());
            } catch (SQLException ex) {
                connection.rollback(); throw ex;
            } finally { connection.setAutoCommit(true); }
        }
    }

    public void updatePassword(User user, String passwordHash) throws SQLException {
        String sql = user.getRoleId() == 0
                ? "UPDATE guest SET password_hash = ? WHERE guest_id = ? AND has_account = 1"
                : "UPDATE `user` SET password_hash = ? WHERE user_id = ?";
        executeUpdate(sql, passwordHash, user.getUserId(), "Không thể cập nhật mật khẩu");
    }

    public void updateProfile(User user, String fullName, String phone) throws SQLException {
        String sql = user.getRoleId() == 0
                ? "UPDATE guest SET full_name = ?, phone = ? WHERE guest_id = ? AND has_account = 1"
                : "UPDATE `user` SET full_name = ?, phone = ? WHERE user_id = ?";
        try (Connection connection = DBConnectionUtil.getConnection()) {
            if (connection == null) throw new SQLException("Không thể kết nối tới cơ sở dữ liệu");
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, fullName);
                statement.setString(2, phone);
                statement.setInt(3, user.getUserId());
                if (statement.executeUpdate() == 0) throw new SQLException("Không thể cập nhật hồ sơ");
            }
        }
    }

    private void executeUpdate(String sql, String value, int id, String error) throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection()) {
            if (connection == null) throw new SQLException("Không thể kết nối tới cơ sở dữ liệu");
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, value);
                statement.setInt(2, id);
                if (statement.executeUpdate() == 0) throw new SQLException(error);
            }
        }
    }
}
