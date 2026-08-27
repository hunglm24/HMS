package controller.page;

import dao.HotelPolicyDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HotelPolicy;
import service.AuditLogService;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

@WebServlet(urlPatterns = {
        "/manager/hotel-policy",
        "/manager/hotel-policy/create",
        "/manager/hotel-policy/edit",
        "/manager/hotel-policy/save",
        "/manager/hotel-policy/toggle-status"
})
public class HotelPolicyServlet extends HttpServlet {
    private static final String INDEX_VIEW = "/WEB-INF/views/manager/hotel-policy/index.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/manager/hotel-policy/form.jsp";

    private final HotelPolicyDao policyDao = new HotelPolicyDao();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        try {
            if ("/manager/hotel-policy/create".equals(servletPath) || "/manager/hotel-policy/edit".equals(servletPath)) {
                prepareFormPage(request, servletPath);
                request.getRequestDispatcher(FORM_VIEW).forward(request, response);
                return;
            }
            prepareIndexPage(request);
            request.getRequestDispatcher(INDEX_VIEW).forward(request, response);
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/manager/hotel-policy");
        } catch (SQLException ex) {
            throw new ServletException("Cannot load hotel policy page", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if ("/manager/hotel-policy/save".equals(request.getServletPath())) {
                HotelPolicy policy = savePolicy(request);
                auditLogService.log(
                        request,
                        request.getParameter("id") == null ? "CREATE_HOTEL_POLICY" : "UPDATE_HOTEL_POLICY",
                        "HOTEL_POLICY",
                        policy.getId(),
                        "Saved hotel policy " + policy.getTitle());
                flash(request, "Đã lưu policy khách sạn.", "success");
            } else if ("/manager/hotel-policy/toggle-status".equals(request.getServletPath())) {
                toggleStatus(request);
                flash(request, "Đã cập nhật trạng thái policy.", "success");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/manager/hotel-policy");
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/manager/hotel-policy");
        } catch (SQLException ex) {
            throw new ServletException("Cannot save hotel policy", ex);
        }
    }

    private void prepareIndexPage(HttpServletRequest request) throws SQLException {
        request.setAttribute("policy", policyDao.findLatest().orElse(null));
    }

    private void prepareFormPage(HttpServletRequest request, String servletPath) throws SQLException {
        HotelPolicy policy = resolvePolicyForForm(request, servletPath);
        request.setAttribute("policy", policy);
        request.setAttribute("isEdit", policy != null && policy.getId() != null);
    }

    private HotelPolicy resolvePolicyForForm(HttpServletRequest request, String servletPath) throws SQLException {
        if ("/manager/hotel-policy/edit".equals(servletPath)) {
            Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Policy");
            if (id != null) {
                return policyDao.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy policy khách sạn."));
            }
            return policyDao.findLatest()
                    .orElseThrow(() -> new IllegalArgumentException("Chưa có policy nào để chỉnh sửa."));
        }
        return policyDao.findLatest().orElseGet(HotelPolicy::new);
    }

    private HotelPolicy savePolicy(HttpServletRequest request) throws SQLException {
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Policy");
        if (id == null) {
            id = policyDao.findLatest().map(HotelPolicy::getId).orElse(null);
        }

        HotelPolicy policy = new HotelPolicy();
        if (id != null) {
            policy.setId(id);
        }
        policy.setTitle(ValidationUtil.requireText(request.getParameter("title"), "Tiêu đề", 2, 150));
        policy.setContent(ValidationUtil.requireText(request.getParameter("content"), "Nội dung", 10, 5000));
        policy.setStatus(ValidationUtil.requireStatus(
                request.getParameter("status"),
                "Trạng thái",
                Set.of("ACTIVE", "INACTIVE")));
        policy.setCategory("Nội quy chung");
        Long savedId = policyDao.save(policy);
        if (policy.getId() == null && savedId != null) {
            policy.setId(savedId);
        }
        return policy;
    }

    private void toggleStatus(HttpServletRequest request) throws SQLException {
        long id = resolveCurrentPolicyId(request);
        String status = ValidationUtil.requireStatus(
                request.getParameter("status"),
                "Trạng thái",
                Set.of("ACTIVE", "INACTIVE"));
        policyDao.updateStatus(id, status);
        auditLogService.log(request, "TOGGLE_HOTEL_POLICY_STATUS", "HOTEL_POLICY", id,
                "Changed hotel policy status to " + status);
    }

    private long resolveCurrentPolicyId(HttpServletRequest request) throws SQLException {
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Policy");
        if (id != null) {
            return id;
        }
        return policyDao.findLatest()
                .map(HotelPolicy::getId)
                .orElseThrow(() -> new IllegalArgumentException("Chưa có policy khách sạn."));
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", "success".equals(type) ? "toast-success" : "toast-error");
    }
}
