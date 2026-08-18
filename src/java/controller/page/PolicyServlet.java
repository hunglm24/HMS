package controller.page;

import dao.HotelPolicyDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HotelPolicy;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

@WebServlet(urlPatterns = {
        "/manager/policies",
        "/manager/policies/save",
        "/manager/policies/delete"
})
public class PolicyServlet extends HttpServlet {
    private final HotelPolicyDao policyDao = new HotelPolicyDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preparePage(request);
        request.getRequestDispatcher("/WEB-INF/views/manager/policies.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if ("/manager/policies/save".equals(request.getServletPath())) {
                savePolicy(request);
                flash(request, "Đã lưu chính sách.", "success");
            } else if ("/manager/policies/delete".equals(request.getServletPath())) {
                policyDao.delete(ValidationUtil.requirePositiveLong(request.getParameter("id"), "Chính sách"));
                flash(request, "Đã xóa chính sách.", "success");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/manager/policies");
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/manager/policies");
        } catch (SQLException ex) {
            throw new ServletException("Cannot update hotel policy", ex);
        }
    }

    private void preparePage(HttpServletRequest request) throws ServletException {
        try {
            request.setAttribute("policies", policyDao.findAll());
        } catch (SQLException ex) {
            throw new ServletException("Cannot load hotel policies", ex);
        }
    }

    private void savePolicy(HttpServletRequest request) throws SQLException {
        HotelPolicy policy = new HotelPolicy();
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Chính sách");
        if (id != null && id > 0) {
            policy.setId(id);
        }
        policy.setTitle(ValidationUtil.requireText(request.getParameter("title"), "Tiêu đề", 2, 150));
        policy.setCategory(ValidationUtil.requireText(request.getParameter("category"), "Nhóm chính sách", 2, 80));
        policy.setContent(ValidationUtil.requireText(request.getParameter("content"), "Nội dung", 5, 2000));
        policy.setStatus(ValidationUtil.requireStatus(request.getParameter("status"), "Trạng thái", Set.of("ACTIVE", "INACTIVE")));
        policyDao.save(policy);
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", "success".equals(type) ? "toast-success" : "toast-error");
    }
}
