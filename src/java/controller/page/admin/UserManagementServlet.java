package controller.page.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.AdminUserService;
import service.RoleService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/admin/users")
public class UserManagementServlet extends HttpServlet {
    private AdminUserService userService;
    private RoleService roleService;

    @Override
    public void init() {
        userService = new AdminUserService();
        roleService = new RoleService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadPage(request, response, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                userService.createUser(
                        request.getParameter("fullName"),
                        request.getParameter("email"),
                        request.getParameter("phone"),
                        request.getParameter("password"),
                        parseLong(request.getParameter("roleId")),
                        request.getParameter("status"));
                response.sendRedirect(request.getContextPath() + "/admin/users?success=created");
                return;
            }
            if ("update".equals(action)) {
                userService.updateUser(
                        parseLong(request.getParameter("id")),
                        request.getParameter("fullName"),
                        request.getParameter("phone"),
                        parseLong(request.getParameter("roleId")),
                        request.getParameter("status"));
                response.sendRedirect(request.getContextPath() + "/admin/users?success=updated");
                return;
            }
            loadPage(request, response, "Thao tác không hợp lệ.", null);
        } catch (IllegalArgumentException ex) {
            loadPage(request, response, ex.getMessage(), retainForm(request));
        } catch (SQLException ex) {
            getServletContext().log("Quản lý người dùng thất bại", ex);
            loadPage(request, response, "Không thể xử lý người dùng. Vui lòng thử lại sau.", retainForm(request));
        }
    }

    private void loadPage(HttpServletRequest request, HttpServletResponse response,
                          String error, User formUser) throws ServletException, IOException {
        try {
            request.setAttribute("users", userService.listUsers(request.getParameter("q")));
            request.setAttribute("roles", roleService.listRoles());
            request.setAttribute("editUser", loadEditUser(request, formUser));
            request.setAttribute("error", error);
            request.setAttribute("success", request.getParameter("success"));
            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Không thể tải trang quản lý người dùng", ex);
        }
    }

    private User loadEditUser(HttpServletRequest request, User formUser) throws SQLException {
        if (formUser != null) {
            return formUser;
        }
        String editId = request.getParameter("edit");
        if (editId == null || editId.isBlank()) {
            return null;
        }
        return userService.findById(parseLong(editId)).orElse(null);
    }

    private User retainForm(HttpServletRequest request) {
        User user = new User();
        user.setUserId((int) parseLong(request.getParameter("id")));
        user.setFullName(request.getParameter("fullName"));
        user.setEmail(request.getParameter("email"));
        user.setPhone(request.getParameter("phone"));
        user.setRoleId((int) parseLong(request.getParameter("roleId")));
        user.setStatus(request.getParameter("status"));
        request.setAttribute("formAction", request.getParameter("action"));
        return user;
    }

    private long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
