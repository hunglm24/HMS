package controller.page;

import dao.HotelPolicyDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HotelPolicy;
import service.CancellationPolicyService;
import service.AuditLogService;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

@WebServlet(urlPatterns = {
        "/manager/policies",
        "/manager/policies/create",
        "/manager/policies/edit",
        "/manager/policies/save",
        "/manager/policies/toggle-status",
        "/manager/policies/delete"
})
public class PolicyServlet extends HttpServlet {
    private final HotelPolicyDao policyDao = new HotelPolicyDao();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/manager/policies/create".equals(request.getServletPath())) {
            request.getRequestDispatcher("/WEB-INF/views/manager/policy-form.jsp").forward(request, response);
            return;
        }
        if ("/manager/policies/edit".equals(request.getServletPath())) {
            try {
                prepareEditPage(request);
                request.getRequestDispatcher("/WEB-INF/views/manager/policy-form.jsp").forward(request, response);
            } catch (IllegalArgumentException ex) {
                flash(request, ex.getMessage(), "error");
                response.sendRedirect(request.getContextPath() + "/manager/policies");
            }
            return;
        }
        preparePage(request);
        request.getRequestDispatcher("/WEB-INF/views/manager/policies.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!isManagerPath(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            if ("/manager/policies/save".equals(request.getServletPath())) {
                String idRaw = request.getParameter("id");
                savePolicy(request);
                auditLogService.log(request, ValidationUtil.isBlank(idRaw) ? "CREATE_POLICY" : "UPDATE_POLICY",
                        "POLICY", ValidationUtil.optionalPositiveLong(idRaw, "Chính sách"),
                        "Saved policy " + request.getParameter("title"));
                flash(request, "Đã lưu chính sách.", "success");
            } else if ("/manager/policies/toggle-status".equals(request.getServletPath())) {
                long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Chính sách");
                togglePolicyStatus(request);
                auditLogService.log(request, "TOGGLE_POLICY_STATUS", "POLICY", id,
                        "Changed policy status to " + request.getParameter("status"));
                flash(request, "Đã cập nhật trạng thái chính sách.", "success");
            } else if ("/manager/policies/delete".equals(request.getServletPath())) {
                long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Chính sách");
                policyDao.delete(id);
                auditLogService.log(request, "DELETE_POLICY", "POLICY", id, "Deleted policy");
                flash(request, "Đã xóa chính sách.", "success");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/manager/policies");
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            String id = request.getParameter("id");
            String redirectPath = "/manager/policies/save".equals(request.getServletPath())
                    ? (ValidationUtil.isBlank(id) ? "/manager/policies/create" : "/manager/policies/edit?id=" + id)
                    : "/manager/policies";
            response.sendRedirect(request.getContextPath() + redirectPath);
        } catch (SQLException ex) {
            throw new ServletException("Cannot update hotel policy", ex);
        }
    }

    private void prepareEditPage(HttpServletRequest request) throws ServletException {
        try {
            long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Chính sách");
            HotelPolicy policy = policyDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chính sách."));
            request.setAttribute("policy", policy);
            request.setAttribute("cancellationRule",
                    CancellationPolicyService.parseRuleOrDefault(policy.getContent()));
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new ServletException("Cannot load policy edit page", ex);
        }
    }

    private void preparePage(HttpServletRequest request) throws ServletException {
        try {
            request.setAttribute("policies", policyDao.findAll());
        } catch (SQLException ex) {
            throw new ServletException("Cannot load hotel policies", ex);
        }
    }

    private boolean isManagerPath(HttpServletRequest request) {
        return request.getServletPath() != null && request.getServletPath().startsWith("/manager/");
    }

    private void savePolicy(HttpServletRequest request) throws SQLException {
        HotelPolicy policy = new HotelPolicy();
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Chính sách");
        if (id != null && id > 0) {
            policy.setId(id);
        }
        policy.setTitle(ValidationUtil.requireText(request.getParameter("title"), "Tiêu đề", 2, 150));
        policy.setCategory(ValidationUtil.requireText(request.getParameter("category"), "Nhóm chính sách", 2, 80));
        if (isCancellationPolicy(policy)) {
            policy.setContent(buildCancellationPolicyContent(request));
        } else {
            policy.setContent(ValidationUtil.requireText(request.getParameter("content"), "Nội dung", 5, 2000));
        }
        policy.setStatus(ValidationUtil.requireStatus(request.getParameter("status"), "Trạng thái", Set.of("ACTIVE", "INACTIVE")));
        policyDao.save(policy);
    }

    private boolean isCancellationPolicy(HotelPolicy policy) {
        String category = policy.getCategory() == null ? "" : policy.getCategory().toLowerCase(java.util.Locale.ROOT);
        String title = policy.getTitle() == null ? "" : policy.getTitle().toLowerCase(java.util.Locale.ROOT);
        return category.contains("hủy") || title.contains("hủy") || category.contains("huy") || title.contains("huy");
    }

    private String buildCancellationPolicyContent(HttpServletRequest request) {
        int fullDays = ValidationUtil.requirePositiveInt(request.getParameter("fullRefundDays"), "Số ngày hoàn 100%");
        int fullRate = requirePercent(request.getParameter("fullRefundRate"), "Tỷ lệ hoàn cao nhất");
        int partialDays = ValidationUtil.requirePositiveInt(request.getParameter("partialRefundDays"), "Số ngày hoàn một phần");
        int partialRate = requirePercent(request.getParameter("partialRefundRate"), "Tỷ lệ hoàn một phần");
        int sameDayRate = requirePercent(request.getParameter("sameDayRefundRate"), "Tỷ lệ hoàn trong ngày check-in");
        ValidationUtil.requireTrue(fullDays > partialDays,
                "Mốc hoàn cao nhất phải lớn hơn mốc hoàn một phần.");
        ValidationUtil.requireTrue(fullRate >= partialRate && partialRate >= sameDayRate,
                "Tỷ lệ hoàn tiền phải giảm dần theo thời gian hủy.");
        return CancellationPolicyService.buildCancellationContent(
                fullDays, fullRate, partialDays, partialRate, sameDayRate);
    }

    private int requirePercent(String value, String fieldName) {
        Integer percent = ValidationUtil.optionalPositiveInt(value, fieldName);
        if (percent == null) {
            throw new IllegalArgumentException(fieldName + " bắt buộc.");
        }
        ValidationUtil.requireTrue(percent <= 100, fieldName + " không được vượt quá 100%.");
        return percent;
    }

    private void togglePolicyStatus(HttpServletRequest request) throws SQLException {
        long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Chính sách");
        String status = ValidationUtil.requireStatus(request.getParameter("status"), "Trạng thái", Set.of("ACTIVE", "INACTIVE"));
        policyDao.updateStatus(id, status);
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", "success".equals(type) ? "toast-success" : "toast-error");
    }
}
