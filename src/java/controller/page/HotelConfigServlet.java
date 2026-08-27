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
        "/manager/hotel-configs",
        "/manager/hotel-configs/create",
        "/manager/hotel-configs/edit",
        "/manager/hotel-configs/save",
        "/manager/hotel-configs/toggle-status",
        "/manager/hotel-configs/delete"
})
public class HotelConfigServlet extends HttpServlet {
    private final HotelPolicyDao policyDao = new HotelPolicyDao();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/manager/hotel-configs/create".equals(request.getServletPath())) {
            request.getRequestDispatcher("/WEB-INF/views/manager/hotel-config-form.jsp").forward(request, response);
            return;
        }
        if ("/manager/hotel-configs/edit".equals(request.getServletPath())) {
            try {
                prepareEditPage(request);
                request.getRequestDispatcher("/WEB-INF/views/manager/hotel-config-form.jsp").forward(request, response);
            } catch (IllegalArgumentException ex) {
                flash(request, ex.getMessage(), "error");
                response.sendRedirect(request.getContextPath() + "/manager/hotel-configs");
            }
            return;
        }
        preparePage(request);
        request.getRequestDispatcher("/WEB-INF/views/manager/hotel-config-list.jsp").forward(request, response);
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
            if ("/manager/hotel-configs/save".equals(request.getServletPath())) {
                String idRaw = request.getParameter("id");
                savePolicy(request);
                auditLogService.log(request, ValidationUtil.isBlank(idRaw) ? "CREATE_POLICY" : "UPDATE_POLICY",
                        "POLICY", ValidationUtil.optionalPositiveLong(idRaw, "ChÃ­nh sÃ¡ch"),
                        "Saved policy " + request.getParameter("title"));
                flash(request, "ÄÃ£ lÆ°u chÃ­nh sÃ¡ch.", "success");
            } else if ("/manager/hotel-configs/toggle-status".equals(request.getServletPath())) {
                long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "ChÃ­nh sÃ¡ch");
                togglePolicyStatus(request);
                auditLogService.log(request, "TOGGLE_POLICY_STATUS", "POLICY", id,
                        "Changed policy status to " + request.getParameter("status"));
                flash(request, "ÄÃ£ cáº­p nháº­t tráº¡ng thÃ¡i chÃ­nh sÃ¡ch.", "success");
            } else if ("/manager/hotel-configs/delete".equals(request.getServletPath())) {
                long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "ChÃ­nh sÃ¡ch");
                policyDao.delete(id);
                auditLogService.log(request, "DELETE_POLICY", "POLICY", id, "Deleted policy");
                flash(request, "ÄÃ£ xÃ³a chÃ­nh sÃ¡ch.", "success");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/manager/hotel-configs");
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            String id = request.getParameter("id");
            String redirectPath = "/manager/hotel-configs/save".equals(request.getServletPath())
                    ? (ValidationUtil.isBlank(id) ? "/manager/hotel-configs/create" : "/manager/hotel-configs/edit?id=" + id)
                    : "/manager/hotel-configs";
            response.sendRedirect(request.getContextPath() + redirectPath);
        } catch (SQLException ex) {
            throw new ServletException("Cannot update hotel policy", ex);
        }
    }

    private void prepareEditPage(HttpServletRequest request) throws ServletException {
        try {
            long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "ChÃ­nh sÃ¡ch");
            HotelPolicy policy = policyDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y chÃ­nh sÃ¡ch."));
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
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "ChÃ­nh sÃ¡ch");
        if (id != null && id > 0) {
            policy.setId(id);
        }
        policy.setTitle(ValidationUtil.requireText(request.getParameter("title"), "TiÃªu Ä‘á»", 2, 150));
        policy.setCategory(ValidationUtil.requireText(request.getParameter("category"), "NhÃ³m chÃ­nh sÃ¡ch", 2, 80));
        if (isCancellationPolicy(policy)) {
            policy.setContent(buildCancellationPolicyContent(request));
        } else {
            policy.setContent(ValidationUtil.requireText(request.getParameter("content"), "Ná»™i dung", 5, 2000));
        }
        policy.setStatus(ValidationUtil.requireStatus(request.getParameter("status"), "Tráº¡ng thÃ¡i", Set.of("ACTIVE", "INACTIVE")));
        policyDao.save(policy);
    }

    private boolean isCancellationPolicy(HotelPolicy policy) {
        String category = policy.getCategory() == null ? "" : policy.getCategory().toLowerCase(java.util.Locale.ROOT);
        String title = policy.getTitle() == null ? "" : policy.getTitle().toLowerCase(java.util.Locale.ROOT);
        return category.contains("há»§y") || title.contains("há»§y") || category.contains("huy") || title.contains("huy");
    }

    private String buildCancellationPolicyContent(HttpServletRequest request) {
        int fullDays = ValidationUtil.requirePositiveInt(request.getParameter("fullRefundDays"), "Sá»‘ ngÃ y hoÃ n 100%");
        int fullRate = requirePercent(request.getParameter("fullRefundRate"), "Tá»· lá»‡ hoÃ n cao nháº¥t");
        int partialDays = ValidationUtil.requirePositiveInt(request.getParameter("partialRefundDays"), "Sá»‘ ngÃ y hoÃ n má»™t pháº§n");
        int partialRate = requirePercent(request.getParameter("partialRefundRate"), "Tá»· lá»‡ hoÃ n má»™t pháº§n");
        int sameDayRate = requirePercent(request.getParameter("sameDayRefundRate"), "Tá»· lá»‡ hoÃ n trong ngÃ y check-in");
        ValidationUtil.requireTrue(fullDays > partialDays,
                "Má»‘c hoÃ n cao nháº¥t pháº£i lá»›n hÆ¡n má»‘c hoÃ n má»™t pháº§n.");
        ValidationUtil.requireTrue(fullRate >= partialRate && partialRate >= sameDayRate,
                "Tá»· lá»‡ hoÃ n tiá»n pháº£i giáº£m dáº§n theo thá»i gian há»§y.");
        return CancellationPolicyService.buildCancellationContent(
                fullDays, fullRate, partialDays, partialRate, sameDayRate);
    }

    private int requirePercent(String value, String fieldName) {
        Integer percent = ValidationUtil.optionalPositiveInt(value, fieldName);
        if (percent == null) {
            throw new IllegalArgumentException(fieldName + " báº¯t buá»™c.");
        }
        ValidationUtil.requireTrue(percent <= 100, fieldName + " khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 100%.");
        return percent;
    }

    private void togglePolicyStatus(HttpServletRequest request) throws SQLException {
        long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "ChÃ­nh sÃ¡ch");
        String status = ValidationUtil.requireStatus(request.getParameter("status"), "Tráº¡ng thÃ¡i", Set.of("ACTIVE", "INACTIVE"));
        policyDao.updateStatus(id, status);
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", "success".equals(type) ? "toast-success" : "toast-error");
    }
}
