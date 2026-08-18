package dao;

import model.Permission;
import model.Role;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoleDao {
    public List<Role> findAll() throws SQLException {
        ensurePermissionTables();
        String sql = "SELECT id, name, description, created_at FROM roles ORDER BY id";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Role> roles = new ArrayList<>();
            while (rs.next()) {
                roles.add(mapRole(rs));
            }
            return roles;
        }
    }

    public Role findById(long id) throws SQLException {
        String sql = "SELECT id, name, description, created_at FROM roles WHERE id = ?";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRole(rs);
                }
            }
        }
        return null;
    }

    public long save(Role role) throws SQLException {
        if (role.getId() == null) {
            return insert(role);
        }
        update(role);
        return role.getId();
    }

    public long insert(Role role) throws SQLException {
        String sql = "INSERT INTO roles (name, description) VALUES (?, ?)";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, role.getName());
            statement.setString(2, role.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Cannot create role");
    }

    public void update(Role role) throws SQLException {
        String sql = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.getName());
            statement.setString(2, role.getDescription());
            statement.setLong(3, role.getId());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Role not found");
            }
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM roles WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public List<Permission> findPermissionsForRole(long roleId) throws SQLException {
        ensurePermissionTables();
        String sql = """
                SELECT p.id, p.code, p.name, p.description, p.created_at,
                       CASE WHEN rp.role_id IS NULL THEN 0 ELSE 1 END AS assigned
                FROM permissions p
                LEFT JOIN role_permissions rp
                       ON rp.permission_id = p.id AND rp.role_id = ?
                ORDER BY p.code
                """;
        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roleId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Permission> permissions = new ArrayList<>();
                while (rs.next()) {
                    Permission permission = new Permission();
                    permission.setId(rs.getLong("id"));
                    permission.setCode(rs.getString("code"));
                    permission.setName(rs.getString("name"));
                    permission.setDescription(rs.getString("description"));
                    permission.setCreatedAt(rs.getTimestamp("created_at"));
                    permission.setAssigned(rs.getBoolean("assigned"));
                    permissions.add(permission);
                }
                return permissions;
            }
        }
    }

    public void replaceRolePermissions(long roleId, List<Long> permissionIds) throws SQLException {
        ensurePermissionTables();
        try (Connection connection = DBConnectionUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM role_permissions WHERE role_id = ?")) {
                    delete.setLong(1, roleId);
                    delete.executeUpdate();
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)")) {
                    for (Long permissionId : permissionIds) {
                        insert.setLong(1, roleId);
                        insert.setLong(2, permissionId);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void ensurePermissionTables() throws SQLException {
        try (Connection connection = DBConnectionUtil.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS permissions (
                      id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                      code VARCHAR(80) NOT NULL,
                      name VARCHAR(120) NOT NULL,
                      description VARCHAR(255) NULL,
                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (id),
                      UNIQUE KEY uq_permissions_code (code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS role_permissions (
                      role_id BIGINT UNSIGNED NOT NULL,
                      permission_id BIGINT UNSIGNED NOT NULL,
                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (role_id, permission_id),
                      CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id)
                        REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE,
                      CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id)
                        REFERENCES permissions(id) ON DELETE CASCADE ON UPDATE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);
            seedPermission(statement, "ADMIN_USERS", "Manage users", "Create, update, block and assign account roles");
            seedPermission(statement, "ADMIN_ROLES", "Manage roles", "Create roles and assign permissions");
            seedPermission(statement, "ADMIN_LOGS", "View logs", "View system activity logs");
            seedPermission(statement, "BOOKING_MANAGE", "Manage bookings", "Manage booking lifecycle");
            seedPermission(statement, "ROOM_MANAGE", "Manage rooms", "Manage rooms, room types and prices");
            seedPermission(statement, "HOUSEKEEPING_MANAGE", "Manage housekeeping", "Manage housekeeping tasks");
        }
    }

    private void seedPermission(Statement statement, String code, String name, String description) throws SQLException {
        statement.executeUpdate("INSERT IGNORE INTO permissions (code, name, description) VALUES ('"
                + code + "', '" + name + "', '" + description + "')");
    }

    private Role mapRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setCreatedAt(rs.getTimestamp("created_at"));
        return role;
    }
}
