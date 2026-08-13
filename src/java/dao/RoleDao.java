package dao;

import model.Role;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDao {
    public List<Role> listRoles() throws SQLException {
        String sql = "SELECT id, name, description, created_at FROM roles ORDER BY name";
        List<Role> roles = new ArrayList<>();
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                roles.add(mapRole(resultSet));
            }
        }
        return roles;
    }

    public Optional<Role> findById(long id) throws SQLException {
        String sql = "SELECT id, name, description, created_at FROM roles WHERE id = ?";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRole(resultSet)) : Optional.empty();
            }
        }
    }

    public boolean existsByName(String name, Long excludedId) throws SQLException {
        String sql = "SELECT id FROM roles WHERE LOWER(name) = LOWER(?)"
                + (excludedId == null ? "" : " AND id <> ?") + " LIMIT 1";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            if (excludedId != null) {
                statement.setLong(2, excludedId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void createRole(String name, String description) throws SQLException {
        String sql = "INSERT INTO roles (name, description) VALUES (?, ?)";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, description);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Không thể tạo vai trò");
            }
        }
    }

    public void updateRole(long id, String name, String description) throws SQLException {
        String sql = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setLong(3, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Không thể cập nhật vai trò");
            }
        }
    }

    private Role mapRole(ResultSet resultSet) throws SQLException {
        Role role = new Role();
        role.setId(resultSet.getLong("id"));
        role.setName(resultSet.getString("name"));
        role.setDescription(resultSet.getString("description"));
        role.setCreatedAt(resultSet.getTimestamp("created_at"));
        return role;
    }
}
