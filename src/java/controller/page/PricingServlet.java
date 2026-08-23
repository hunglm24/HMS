package controller.page;

import dao.PromotionDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;
import model.Promotion;
import util.MoneyUtil;
import util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@WebServlet(urlPatterns = {
        "/manager/pricing",
        "/manager/pricing/promotion/create",
        "/manager/pricing/promotion/edit",
        "/manager/pricing/promotion/save",
        "/manager/pricing/promotion/toggle-status",
        "/manager/pricing/promotion/delete"
})
public class PricingServlet extends HttpServlet {
    private final PromotionDao promotionDao = new PromotionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/manager/pricing/promotion/create".equals(request.getServletPath())) {
            request.getRequestDispatcher("/WEB-INF/views/manager/promotion-create.jsp").forward(request, response);
            return;
        }
        if ("/manager/pricing/promotion/edit".equals(request.getServletPath())) {
            try {
                prepareEditPage(request);
                request.getRequestDispatcher("/WEB-INF/views/manager/promotion-create.jsp").forward(request, response);
            } catch (IllegalArgumentException ex) {
                flash(request, ex.getMessage(), "error");
                response.sendRedirect(request.getContextPath() + "/manager/pricing");
            }
            return;
        }
        preparePage(request);
        request.getRequestDispatcher("/WEB-INF/views/manager/pricing.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        try {
            if ("/manager/pricing/promotion/save".equals(path)) {
                savePromotion(request);
                flash(request, "Đã lưu mã giảm giá.", "success");
            } else if ("/manager/pricing/promotion/toggle-status".equals(path)) {
                togglePromotionStatus(request);
                flash(request, "Đã cập nhật trạng thái mã giảm giá.", "success");
            } else if ("/manager/pricing/promotion/delete".equals(path)) {
                promotionDao.delete(ValidationUtil.requirePositiveLong(request.getParameter("id"), "Mã giảm giá"));
                flash(request, "Đã xóa mã giảm giá.", "success");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/manager/pricing");
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            String id = request.getParameter("id");
            String redirectPath;
            if ("/manager/pricing/promotion/save".equals(path)) {
                redirectPath = ValidationUtil.isBlank(id)
                        ? "/manager/pricing/promotion/create"
                        : "/manager/pricing/promotion/edit?id=" + id;
            } else {
                redirectPath = "/manager/pricing";
            }
            response.sendRedirect(request.getContextPath() + redirectPath);
        } catch (SQLException ex) {
            throw new ServletException("Cannot update manager pricing data", ex);
        }
    }

    private void prepareEditPage(HttpServletRequest request) throws ServletException {
        try {
            long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Mã giảm giá");
            Promotion promotion = promotionDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã giảm giá."));
            request.setAttribute("promotion", promotion);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new ServletException("Cannot load promotion edit page", ex);
        }
    }

    private void preparePage(HttpServletRequest request) throws ServletException {
        try {
            request.setAttribute("promotions", promotionDao.findAll());
        } catch (SQLException ex) {
            throw new ServletException("Cannot load manager pricing page", ex);
        }
    }

    private void savePromotion(HttpServletRequest request) throws SQLException {
        Promotion promotion = new Promotion();
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Mã giảm giá");
        if (id != null && id > 0) {
            promotion.setId(id);
        }
        promotion.setCode(ValidationUtil.requireText(
                ValidationUtil.normalizeUpper(request.getParameter("code")).replaceAll("\\s+", ""),
                "Mã giảm giá", 3, 50));
        promotion.setName(ValidationUtil.requireText(request.getParameter("name"), "Tên mã", 2, 150));
        promotion.setDescription(ValidationUtil.optionalText(request.getParameter("description"), 500));
        promotion.setDiscountType(ValidationUtil.requireStatus(
                request.getParameter("discountType"), "Loại giảm", Set.of("PERCENT", "FIXED_AMOUNT")));
        promotion.setDiscountValue(MoneyUtil.parseVndMoney(request.getParameter("discountValue"), "Mức giảm"));
        if ("PERCENT".equals(promotion.getDiscountType())) {
            ValidationUtil.requireTrue(promotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) <= 0,
                    "Mức giảm theo phần trăm không được vượt quá 100%.");
        } else {
            ValidationUtil.requireTrue(promotion.getDiscountValue().signum() >= 0,
                    "Mức giảm theo số tiền không được âm.");
        }
        promotion.setMaxDiscountAmount(null);
        promotion.setMinBookingAmount(parseOptionalMoney(request.getParameter("minBookingAmount"), "Đơn tối thiểu"));
        promotion.setStartDate(Timestamp.valueOf(parseDateTime(request.getParameter("startDate"), "Ngày bắt đầu")));
        promotion.setEndDate(Timestamp.valueOf(parseDateTime(request.getParameter("endDate"), "Ngày kết thúc")));
        ValidationUtil.requireTrue(promotion.getEndDate().after(promotion.getStartDate()), "Ngày kết thúc phải sau ngày bắt đầu.");
        promotion.setUsageLimit(ValidationUtil.optionalPositiveInt(request.getParameter("usageLimit"), "Số lượt dùng"));
        promotion.setStatus(ValidationUtil.requireStatus(request.getParameter("status"), "Trạng thái", Set.of("ACTIVE", "INACTIVE")));

        Account user = (Account) request.getSession().getAttribute("currentUser");
        promotion.setCreatedBy(user == null ? 1L : user.getId());
        promotionDao.save(promotion);
    }

    private void togglePromotionStatus(HttpServletRequest request) throws SQLException {
        long id = ValidationUtil.requirePositiveLong(request.getParameter("id"), "Mã giảm giá");
        String status = ValidationUtil.requireStatus(request.getParameter("status"), "Trạng thái", Set.of("ACTIVE", "INACTIVE"));
        promotionDao.updateStatus(id, status);
    }

    private BigDecimal parseOptionalMoney(String value, String fieldName) {
        if (ValidationUtil.isBlank(value)) {
            return null;
        }
        return MoneyUtil.parseVndMoney(value, fieldName);
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        try {
            String normalized = ValidationUtil.requireText(value, fieldName, 10, 16);
            return normalized.length() == 10 ? LocalDate.parse(normalized).atStartOfDay() : LocalDateTime.parse(normalized);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", "success".equals(type) ? "toast-success" : "toast-error");
    }
}
