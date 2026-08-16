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
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final String FIND_BY_EMAIL = """
            SELECT a.id, a.full_name, a.email, a.phone, a.password,
                   a.role_id, r.name AS role_name, a.status, a.created_at
            FROM accounts a
            INNER JOIN roles r ON r.id = a.role_id
            WHERE LOWER(a.email) = LOWER(?)
            LIMIT 1
            """;
    private static final String FIND_CUSTOMER_ROLE =
            "SELECT id FROM roles WHERE name = 'CUSTOMER' LIMIT 1";
    private static final String CREATE_CUSTOMER = """
            INSERT INTO accounts (role_id, full_name, phone, email, password, status)
            VALUES (?, ?, ?, ?, ?, 'ACTIVE')
            """;

    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {
                statement.setString(1, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    User user = new User();
                    user.setUserId(resultSet.getInt("id"));
                    user.setFullName(resultSet.getString("full_name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPhone(resultSet.getString("phone"));
                    user.setPasswordHash(resultSet.getString("password"));
                    user.setRoleId(resultSet.getInt("role_id"));
                    user.setRoleName(resultSet.getString("role_name"));
                    user.setStatus(resultSet.getString("status"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at"));
                    return Optional.of(user);
                }
            }
        }
    }

    public List<User> findAll(String keyword, String roleName, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT a.id, a.full_name, a.email, a.phone, a.password,
                       a.role_id, r.name AS role_name, a.status, a.created_at, a.updated_at
                FROM accounts a
                INNER JOIN roles r ON r.id = a.role_id
                WHERE 1 = 1
                """);
        List<String> values = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(a.full_name) LIKE ? OR LOWER(a.email) LIKE ? OR a.phone LIKE ?) ");
            String like = "%" + keyword.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            values.add(like);
            values.add(like);
            values.add("%" + keyword.trim() + "%");
        }
        if (roleName != null && !roleName.isBlank()) {
            sql.append(" AND r.name = ? ");
            values.add(roleName.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND a.status = ? ");
            values.add(status.trim());
        }
        sql.append(" ORDER BY a.created_at DESC, a.id DESC");

        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                statement.setString(i + 1, values.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
                return users;
            }
        }
    }

    public Optional<User> findById(long id) throws SQLException {
        String sql = """
                SELECT a.id, a.full_name, a.email, a.phone, a.password,
                       a.role_id, r.name AS role_name, a.status, a.created_at, a.updated_at
                FROM accounts a
                INNER JOIN roles r ON r.id = a.role_id
                WHERE a.id = ?
                """;
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        }
    }

    public long createAccount(String fullName, String email, String phone, long roleId,
                              String status, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO accounts (role_id, full_name, phone, email, password, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, roleId);
            statement.setString(2, fullName);
            statement.setString(3, phone);
            statement.setString(4, email);
            statement.setString(5, passwordHash);
            statement.setString(6, status);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Cannot create account");
    }

    public void updateAccount(long id, String fullName, String email, String phone,
                              long roleId, String status) throws SQLException {
        String sql = """
                UPDATE accounts
                SET role_id = ?, full_name = ?, phone = ?, email = ?, status = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roleId);
            statement.setString(2, fullName);
            statement.setString(3, phone);
            statement.setString(4, email);
            statement.setString(5, status);
            statement.setLong(6, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Account not found");
            }
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE accounts SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Account not found");
            }
        }
    }

    public void updatePassword(long id, String passwordHash) throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE accounts SET password = ? WHERE id = ?")) {
            statement.setString(1, passwordHash);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Account not found");
            }
        }
    }

    public void deleteAccount(long id) throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM accounts WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public User createCustomer(String fullName, String email, String phone, String passwordHash)
            throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    CREATE_CUSTOMER, Statement.RETURN_GENERATED_KEYS)) {
                long customerRoleId;
                try (PreparedStatement roleStatement = connection.prepareStatement(FIND_CUSTOMER_ROLE);
                     ResultSet roleResult = roleStatement.executeQuery()) {
                    if (!roleResult.next()) {
                        throw new SQLException("Không tìm thấy vai trò CUSTOMER trong bảng roles");
                    }
                    customerRoleId = roleResult.getLong(1);
                }
                statement.setLong(1, customerRoleId);
                statement.setString(2, fullName);
                statement.setString(3, phone);
                statement.setString(4, email);
                statement.setString(5, passwordHash);
                if (statement.executeUpdate() == 0) {
                    throw new SQLException("Không thể tạo tài khoản khách hàng");
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
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
                invalidate.setString(1, "ACCOUNT");
                invalidate.setInt(2, user.getUserId());
                invalidate.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO password_reset_token(account_type, account_id, token_hash, expires_at) VALUES(?,?,?,?)")) {
                insert.setString(1, "ACCOUNT");
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
                String updateSql = "UPDATE accounts SET password=? WHERE id=?";
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
        String sql = "UPDATE accounts SET password = ? WHERE id = ?";
        executeUpdate(sql, passwordHash, user.getUserId(), "Không thể cập nhật mật khẩu");
    }

    public void updateProfile(User user, String fullName, String phone) throws SQLException {
        String sql = "UPDATE accounts SET full_name = ?, phone = ? WHERE id = ?";
        try (Connection connection = DBConnectionUtil.getConnection()) {
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
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, value);
                statement.setInt(2, id);
                if (statement.executeUpdate() == 0) throw new SQLException(error);
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setPasswordHash(resultSet.getString("password"));
        user.setRoleId(resultSet.getInt("role_id"));
        user.setRoleName(resultSet.getString("role_name"));
        user.setStatus(resultSet.getString("status"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        try {
            user.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        } catch (SQLException ignored) {
            // Some legacy queries do not select updated_at.
        }
        return user;
    }
}
