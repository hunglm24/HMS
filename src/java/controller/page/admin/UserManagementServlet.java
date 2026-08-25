package controller.page.admin;

import dao.RoleDao;
import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Role;
import model.User;
import service.AuditLogService;
import util.PasswordUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = {
        "/admin/users", "/admin/users/create", "/admin/users/edit", "/admin/users/save", "/admin/users/status",
        "/admin/users/delete"
})
public class UserManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 5;
    private UserDao userDao;
    private RoleDao roleDao;
    private AuditLogService auditLogService;

    @Override
    public void init() {
        userDao = new UserDao();
        roleDao = new RoleDao();
        auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/admin/users":
                loadList(request, response);
                break;
            case "/admin/users/edit":
                loadEditForm(request, response);
                break;
            case "/admin/users/create":
                loadCreateForm(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (request.getServletPath()) {
                case "/admin/users/save":
                    saveUser(request, response);
                    break;
                case "/admin/users/status":
                    updateStatus(request, response);
                    break;
                case "/admin/users/delete":
                    deleteUser(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (IllegalArgumentException ex) {
            request.getSession().setAttribute("toastMessage", ex.getMessage());
            request.getSession().setAttribute("toastType", "error");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (SQLException ex) {
            getServletContext().log("Admin user management failed", ex);
            request.getSession().setAttribute("toastMessage",
                    "Database error: " + ex.getMessage());
            request.getSession().setAttribute("toastType", "error");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void loadList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String keyword = request.getParameter("q");
            String role = request.getParameter("role");
            String status = request.getParameter("status");
            int pageSize = PAGE_SIZE;
            int totalItems = userDao.countAll(keyword, role, status);
            int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) pageSize));
            int page = Math.min(parsePage(request.getParameter("page")), totalPages);
            int offset = (page - 1) * pageSize;

            request.setAttribute("users", userDao.findPage(keyword, role, status, offset, pageSize));
            request.setAttribute("roles", roleDao.findAll());
            request.setAttribute("q", keyword == null ? "" : keyword);
            request.setAttribute("selectedRole", role == null ? "" : role);
            request.setAttribute("selectedStatus", status == null ? "" : status);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", totalItems);
            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("Cannot load admin users", ex);
            request.setAttribute("error", "Cannot load users. Check database connection.");
            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
        }
    }

    private void loadEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long id = parseLong(request.getParameter("id"), "Invalid user");
            User user = userDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            ensureNotAdminAccount(user);
            request.setAttribute("editUser", user);
            request.setAttribute("roles", roleDao.findAll());
            request.getRequestDispatcher("/WEB-INF/views/admin/user-edit.jsp").forward(request, response);
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (SQLException ex) {
            getServletContext().log("Cannot load admin user edit form", ex);
            throw new ServletException("Cannot load user edit form", ex);
        }
    }

    private void saveUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String idValue = request.getParameter("id");
        String fullName = required(request, "fullName");
        String email = required(request, "email").toLowerCase(Locale.ROOT);
        String phone = blankToNull(request.getParameter("phone"));
        long roleId = parseLong(request.getParameter("roleId"), "Invalid role");
        String status = required(request, "status");

        if (idValue == null || idValue.isBlank()) {
            String password = required(request, "password");
            String confirmPassword = required(request, "confirmPassword");
            Role selectedRole = roleDao.findById(roleId);
            if (selectedRole == null) {
                throw new IllegalArgumentException("Invalid role");
            }
            if ("ADMIN".equalsIgnoreCase(selectedRole.getName())) {
                throw new IllegalArgumentException("ADMIN role cannot be assigned.");
            }
            validateNewPassword(password, confirmPassword);
            long id = userDao.createAccount(fullName, email, phone, roleId, status, PasswordUtil.hash(password));
            auditLogService.log(request, "CREATE_USER", "ACCOUNT", id, "Created account " + email);
            flash(request, "User account created.", "success");
        } else {
            long id = parseLong(idValue, "Invalid user");
            User existing = userDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            ensureNotAdminAccount(existing);
            Role selectedRole = roleDao.findById(roleId);
            if (selectedRole == null) {
                throw new IllegalArgumentException("Invalid role");
            }
            if ("ADMIN".equalsIgnoreCase(selectedRole.getName())
                    && !"ADMIN".equalsIgnoreCase(existing.getRoleName())) {
                throw new IllegalArgumentException("ADMIN role cannot be assigned.");
            }
            userDao.updateAccount(id, fullName, email, phone, roleId, status);
            auditLogService.log(request, "UPDATE_USER", "ACCOUNT", id, "Updated account " + email);
            flash(request, "User account updated.", "success");
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void validateNewPassword(String password, String confirmPassword) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }

    private void loadCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("roles", roleDao.findAll());
            request.getRequestDispatcher("/WEB-INF/views/admin/user-create.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("Cannot load admin user create form", ex);
            throw new ServletException("Cannot load user create form", ex);
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        long id = parseLong(request.getParameter("id"), "Invalid user");
        User existing = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        ensureNotAdminAccount(existing);
        String status = required(request, "status");
        userDao.updateStatus(id, status);
        auditLogService.log(request, "UPDATE_USER_STATUS", "ACCOUNT", id, "Status changed to " + status);
        flash(request, "User status updated.", "success");
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        long id = parseLong(request.getParameter("id"), "Invalid user");
        User existing = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        ensureNotAdminAccount(existing);
        userDao.deleteAccount(id);
        auditLogService.log(request, "DELETE_USER", "ACCOUNT", id, "Deleted account");
        flash(request, "User account deleted.", "success");
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void ensureNotAdminAccount(User user) {
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRoleName())) {
            throw new IllegalArgumentException("ADMIN accounts cannot be edited, blocked, or deleted.");
        }
    }

    private String required(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: " + name);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private long parseLong(String value, String message) {
        try {
            return Long.parseLong(value);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private int parsePage(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(1, parsed);
        } catch (RuntimeException ex) {
            return 1;
        }
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", type);
    }
}

