package service;

import dao.RoleDao;
import model.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RoleService {
    private final RoleDao roleDao;

    public RoleService() {
        this(new RoleDao());
    }

    public RoleService(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public List<Role> listRoles() throws SQLException {
        return roleDao.listRoles();
    }

    public Optional<Role> findById(long id) throws SQLException {
        return roleDao.findById(id);
    }

    public void createRole(String name, String description) throws SQLException {
        String normalizedName = validateName(name);
        if (roleDao.existsByName(normalizedName, null)) {
            throw new IllegalArgumentException("Tên vai trò đã tồn tại.");
        }
        roleDao.createRole(normalizedName, normalizeDescription(description));
    }

    public void updateRole(long id, String name, String description) throws SQLException {
        if (id <= 0 || roleDao.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy vai trò.");
        }
        String normalizedName = validateName(name);
        if (roleDao.existsByName(normalizedName, id)) {
            throw new IllegalArgumentException("Tên vai trò đã tồn tại.");
        }
        roleDao.updateRole(id, normalizedName, normalizeDescription(description));
    }

    private String validateName(String name) {
        String value = name == null ? "" : name.trim().toUpperCase(java.util.Locale.ROOT);
        if (!value.matches("[A-Z_]{2,40}")) {
            throw new IllegalArgumentException("Tên vai trò chỉ gồm chữ in hoa và dấu gạch dưới, từ 2 đến 40 ký tự.");
        }
        return value;
    }

    private String normalizeDescription(String description) {
        String value = description == null ? "" : description.trim();
        return value.isEmpty() ? null : value;
    }
}
