package controller.page.admin;

import dao.RoleDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Role;
import model.User;
import service.AuditLogService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/admin/roles", "/admin/roles/save", "/admin/roles/delete", "/admin/roles/permissions"})
public class RoleManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RoleDao roleDao;
    private AuditLogService auditLogService;

    @Override
    public void init() {
        roleDao = new RoleDao();
        auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/admin/roles".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        loadPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (request.getServletPath()) {
                case "/admin/roles/save" -> saveRole(request, response);
                case "/admin/roles/delete" -> deleteRole(request, response);
                case "/admin/roles/permissions" -> savePermissions(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/admin/roles");
        } catch (SQLException ex) {
            getServletContext().log("Admin role management failed", ex);
            flash(request, "Database error: " + ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/admin/roles");
        }
    }

    private void loadPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long selectedRoleId = parseSelectedRole(request);
            List<Role> roles = findManageableRoles();
            if (selectedRoleId != 0 && !containsRoleId(roles, selectedRoleId)) {
                selectedRoleId = 0;
            }
            request.setAttribute("roles", roles);
            request.setAttribute("selectedRoleId", selectedRoleId);
            request.setAttribute("permissions",
                    selectedRoleId == 0 ? List.of() : roleDao.findPermissionsForRole(selectedRoleId));
            request.getRequestDispatcher("/WEB-INF/views/admin/roles.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("Cannot load admin roles", ex);
            request.setAttribute("error", "Cannot load roles. Check database connection.");
            request.getRequestDispatcher("/WEB-INF/views/admin/roles.jsp").forward(request, response);
        }
    }

    private void saveRole(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Role role = new Role();
        String idValue = request.getParameter("id");
        if (idValue != null && !idValue.isBlank()) {
            role.setId(Long.parseLong(idValue));
            ensureManageableRole(role.getId());
        }
        role.setName(required(request, "name").toUpperCase(Locale.ROOT));
        if ("ADMIN".equalsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException("Không được tạo hoặc chỉnh sửa role ADMIN.");
        }
        role.setDescription(request.getParameter("description"));
        long id = roleDao.save(role);
        auditLogService.log(request, role.getId() == null ? "CREATE_ROLE" : "UPDATE_ROLE",
                "ROLE", id, "Saved role " + role.getName());
        flash(request, "Role saved.", "success");
        response.sendRedirect(request.getContextPath() + "/admin/roles?roleId=" + id);
    }

    private void deleteRole(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        long id = Long.parseLong(required(request, "id"));
        ensureManageableRole(id);
        roleDao.delete(id);
        auditLogService.log(request, "DELETE_ROLE", "ROLE", id, "Deleted role");
        flash(request, "Role deleted.", "success");
        response.sendRedirect(request.getContextPath() + "/admin/roles");
    }

    private void savePermissions(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        long roleId = Long.parseLong(required(request, "roleId"));
        ensureManageableRole(roleId);
        String[] selected = request.getParameterValues("permissionId");
        List<Long> permissionIds = new ArrayList<>();
        if (selected != null) {
            for (String value : selected) {
                permissionIds.add(Long.parseLong(value));
            }
        }
        roleDao.replaceRolePermissions(roleId, permissionIds);
        refreshCurrentSessionPermissions(request, roleId);
        auditLogService.log(request, "ASSIGN_ROLE_PERMISSIONS", "ROLE", roleId,
                "Assigned " + permissionIds.size() + " permissions");
        flash(request, "Permissions updated.", "success");
        response.sendRedirect(request.getContextPath() + "/admin/roles?roleId=" + roleId);
    }

    private long parseSelectedRole(HttpServletRequest request) {
        try {
            return Long.parseLong(request.getParameter("roleId"));
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    private List<Role> findManageableRoles() throws SQLException {
        return roleDao.findAll().stream()
                .filter(role -> role != null && !"ADMIN".equalsIgnoreCase(role.getName()))
                .collect(Collectors.toList());
    }

    private boolean containsRoleId(List<Role> roles, long roleId) {
        if (roles == null) {
            return false;
        }
        for (Role role : roles) {
            if (role != null && role.getId() != null && role.getId() == roleId) {
                return true;
            }
        }
        return false;
    }

    private void ensureManageableRole(long roleId) throws SQLException {
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Không tìm thấy role.");
        }
        if ("ADMIN".equalsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException("Không được chỉnh sửa role ADMIN.");
        }
    }

    private void refreshCurrentSessionPermissions(HttpServletRequest request, long roleId) throws SQLException {
        Object currentUser = request.getSession().getAttribute("currentUser");
        if (currentUser instanceof User && ((User) currentUser).getRoleId() == roleId) {
            request.getSession().setAttribute("permissionCodes", roleDao.findPermissionCodesForRole(roleId));
        }
    }

    private String required(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: " + name);
        }
        return value.trim();
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", type);
    }
}

