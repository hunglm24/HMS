package controller.page.manager;

import config.AppConstants;
import dao.HotelPolicyDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;
import model.HotelPolicy;
import service.AuditLogService;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

@WebServlet(urlPatterns = {
        "/manager/hotel-policy",
        "/manager/hotel-policy/edit",
        "/manager/hotel-policy/save",
        "/manager/hotel-policy/toggle-status"
})
public class HotelPolicyManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final HotelPolicyDao policyDao = new HotelPolicyDao();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isHotelManager(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = request.getServletPath();
        if ("/manager/hotel-policy".equals(path) || "/manager/hotel-policy/edit".equals(path)) {
            try {
                preparePolicyForm(request);
                request.getRequestDispatcher("/WEB-INF/views/manager/hotel-policy-form.jsp").forward(request, response);
            } catch (SQLException ex) {
                throw new ServletException("Cannot load hotel policy page", ex);
            }
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!isHotelManager(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = request.getServletPath();
        try {
            if ("/manager/hotel-policy/save".equals(path)) {
                savePolicy(request);
                flash(request, "Đã lưu nội quy khách sạn.", "success");
                response.sendRedirect(request.getContextPath() + "/manager/hotel-policy");
                return;
            }

            if ("/manager/hotel-policy/toggle-status".equals(path)) {
                long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Nội quy");
                toggleStatus(request);
                auditLogService.log(request, "TOGGLE_POLICY_STATUS", "POLICY", id,
                        "Changed hotel policy status to " + request.getParameter("status"));
                flash(request, "Đã cập nhật trạng thái nội quy.", "success");
                response.sendRedirect(request.getContextPath() + "/manager/hotel-policy");
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            String id = request.getParameter("id");
            String redirectPath = ValidationUtil.isBlank(id)
                    ? "/manager/hotel-policy"
                    : "/manager/hotel-policy/edit?id=" + id;
            response.sendRedirect(request.getContextPath() + redirectPath);
        } catch (SQLException ex) {
            throw new ServletException("Cannot update hotel policy", ex);
        }
    }

    private void preparePolicyForm(HttpServletRequest request) throws SQLException {
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Nội quy");
        HotelPolicy policy;
        boolean editMode;

        if (id != null) {
            policy = policyDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nội quy."));
            editMode = true;
        } else {
            policy = policyDao.findLatestHotelPolicy().orElse(null);
            editMode = policy != null;
            if (policy == null) {
                policy = new HotelPolicy();
            }
        }

        request.setAttribute("policy", policy);
        request.setAttribute("isEditMode", editMode);
        request.setAttribute("pageMode", editMode ? "edit" : "create");
        request.setAttribute("pageTitle", editMode ? "Sửa nội quy khách sạn | HMS" : "Tạo nội quy khách sạn | HMS");
        request.setAttribute("pageHeading", editMode ? "Sửa nội quy" : "Tạo nội quy");
        request.setAttribute(
                "pageSubtitle",
                editMode
                        ? "Cập nhật bản nội quy hiện đang áp dụng cho khách."
                        : "Tạo bản nội quy đầu tiên để khách có thể xem trên trang public."
        );
        request.setAttribute("submitLabel", editMode ? "Sửa nội quy" : "Tạo nội quy");
        request.setAttribute("backUrl", "/manager/hotel-policy");
    }

    private void savePolicy(HttpServletRequest request) throws SQLException {
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Nội quy");
        HotelPolicy policy = new HotelPolicy();
        boolean updating = false;

        if (id != null) {
            policy.setId(id);
            updating = true;
        } else {
            HotelPolicy latestPolicy = policyDao.findLatestHotelPolicy().orElse(null);
            if (latestPolicy != null) {
                policy.setId(latestPolicy.getId());
                updating = true;
            }
        }

        policy.setTitle(ValidationUtil.requirePatternText(
                request.getParameter("title"),
                "Tiêu đề",
                2,
                150,
                AppConstants.POLICY_TITLE_PATTERN,
                "Tiêu đề chỉ được chứa chữ, số, khoảng trắng và một số ký tự đặc biệt hợp lệ."
        ));
        policy.setContent(ValidationUtil.requirePatternText(
                request.getParameter("content"),
                "Nội dung",
                10,
                5000,
                AppConstants.POLICY_CONTENT_PATTERN,
                "Nội dung chỉ được chứa chữ, số, khoảng trắng, xuống dòng và một số ký tự đặc biệt hợp lệ."
        ));
        policy.setCategory("Nội quy");
        policy.setStatus("ACTIVE");
        policyDao.save(policy);

        auditLogService.log(
                request,
                updating ? "UPDATE_POLICY" : "CREATE_POLICY",
                "POLICY",
                policy.getId(),
                (updating ? "Updated" : "Created") + " hotel policy " + policy.getTitle()
        );
    }

    private void toggleStatus(HttpServletRequest request) throws SQLException {
        long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Nội quy");
        String status = ValidationUtil.requireStatus(
                request.getParameter("status"),
                "Trạng thái",
                Set.of("ACTIVE", "INACTIVE")
        );
        policyDao.updateStatus(id, status);
    }

    private boolean isHotelManager(HttpServletRequest request) {
        Object currentUser = request.getSession(false) == null ? null : request.getSession(false).getAttribute("currentUser");
        if (!(currentUser instanceof Account)) {
            return false;
        }
        Account account = (Account) currentUser;
        return "HOTEL_MANAGER".equalsIgnoreCase(account.getRoleName());
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", "success".equals(type) ? "toast-success" : "toast-error");
    }
}
