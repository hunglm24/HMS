package dao;

import model.User;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

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
}
