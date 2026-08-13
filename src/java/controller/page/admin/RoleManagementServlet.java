package controller.page.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Role;
import service.RoleService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/admin/roles")
public class RoleManagementServlet extends HttpServlet {
    private RoleService roleService;

    @Override
    public void init() {
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
                roleService.createRole(request.getParameter("name"), request.getParameter("description"));
                response.sendRedirect(request.getContextPath() + "/admin/roles?success=created");
                return;
            }
            if ("update".equals(action)) {
                roleService.updateRole(parseLong(request.getParameter("id")),
                        request.getParameter("name"), request.getParameter("description"));
                response.sendRedirect(request.getContextPath() + "/admin/roles?success=updated");
                return;
            }
            loadPage(request, response, "Thao tác không hợp lệ.", null);
        } catch (IllegalArgumentException ex) {
            loadPage(request, response, ex.getMessage(), retainForm(request));
        } catch (SQLException ex) {
            getServletContext().log("Quản lý vai trò thất bại", ex);
            loadPage(request, response, "Không thể xử lý vai trò. Vui lòng thử lại sau.", retainForm(request));
        }
    }

    private void loadPage(HttpServletRequest request, HttpServletResponse response,
                          String error, Role formRole) throws ServletException, IOException {
        try {
            request.setAttribute("roles", roleService.listRoles());
            request.setAttribute("editRole", loadEditRole(request, formRole));
            request.setAttribute("error", error);
            request.setAttribute("success", request.getParameter("success"));
            request.getRequestDispatcher("/WEB-INF/views/admin/roles.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Không thể tải trang quản lý vai trò", ex);
        }
    }

    private Role loadEditRole(HttpServletRequest request, Role formRole) throws SQLException {
        if (formRole != null) {
            return formRole;
        }
        String editId = request.getParameter("edit");
        if (editId == null || editId.isBlank()) {
            return null;
        }
        return roleService.findById(parseLong(editId)).orElse(null);
    }

    private Role retainForm(HttpServletRequest request) {
        Role role = new Role();
        role.setId(parseLong(request.getParameter("id")));
        role.setName(request.getParameter("name"));
        role.setDescription(request.getParameter("description"));
        request.setAttribute("formAction", request.getParameter("action"));
        return role;
    }

    private long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
