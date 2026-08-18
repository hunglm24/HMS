package controller.page;

import dao.PromotionDao;
import dao.RoomTypeDao;
import dao.SeasonalPriceRuleDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;
import model.Promotion;
import model.SeasonalPriceRule;
import util.MoneyUtil;
import util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@WebServlet(urlPatterns = {
        "/manager/pricing",
        "/manager/pricing/promotion/save",
        "/manager/pricing/promotion/delete",
        "/manager/pricing/rule/save",
        "/manager/pricing/rule/delete"
})
public class PricingServlet extends HttpServlet {
    private final PromotionDao promotionDao = new PromotionDao();
    private final SeasonalPriceRuleDao priceRuleDao = new SeasonalPriceRuleDao();
    private final RoomTypeDao roomTypeDao = new RoomTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
            } else if ("/manager/pricing/promotion/delete".equals(path)) {
                promotionDao.delete(ValidationUtil.requirePositiveLong(request.getParameter("id"), "Mã giảm giá"));
                flash(request, "Đã xóa mã giảm giá.", "success");
            } else if ("/manager/pricing/rule/save".equals(path)) {
                savePriceRule(request);
                flash(request, "Đã lưu bảng giá mùa/ngày lễ.", "success");
            } else if ("/manager/pricing/rule/delete".equals(path)) {
                priceRuleDao.delete(ValidationUtil.requirePositiveLong(request.getParameter("id"), "Bảng giá"));
                flash(request, "Đã xóa bảng giá.", "success");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/manager/pricing");
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/manager/pricing");
        } catch (SQLException ex) {
            throw new ServletException("Cannot update manager pricing data", ex);
        }
    }

    private void preparePage(HttpServletRequest request) throws ServletException {
        try {
            request.setAttribute("roomTypes", roomTypeDao.findActive());
            request.setAttribute("promotions", promotionDao.findAll());
            request.setAttribute("priceRules", priceRuleDao.findAll());
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

    private void savePriceRule(HttpServletRequest request) throws SQLException {
        SeasonalPriceRule rule = new SeasonalPriceRule();
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Bảng giá");
        if (id != null && id > 0) {
            rule.setId(id);
        }
        rule.setRoomTypeId(ValidationUtil.requirePositiveLong(request.getParameter("roomTypeId"), "Loại phòng"));
        rule.setRuleName(ValidationUtil.requireText(request.getParameter("ruleName"), "Tên bảng giá", 2, 150));
        rule.setRuleType(ValidationUtil.requireStatus(request.getParameter("ruleType"), "Loại bảng giá", Set.of("SEASON", "HOLIDAY")));
        rule.setStartDate(Date.valueOf(parseDate(request.getParameter("startDate"), "Ngày bắt đầu")));
        rule.setEndDate(Date.valueOf(parseDate(request.getParameter("endDate"), "Ngày kết thúc")));
        ValidationUtil.requireTrue(!rule.getEndDate().before(rule.getStartDate()), "Ngày kết thúc không được trước ngày bắt đầu.");
        rule.setPricePerNight(null);
        rule.setSurchargePercent(null);
        rule.setStatus(ValidationUtil.requireStatus(request.getParameter("status"), "Trạng thái", Set.of("ACTIVE", "INACTIVE")));
        priceRuleDao.save(rule);
    }

    private BigDecimal parseOptionalMoney(String value, String fieldName) {
        if (ValidationUtil.isBlank(value)) {
            return null;
        }
        return MoneyUtil.parseVndMoney(value, fieldName);
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(ValidationUtil.requireText(value, fieldName, 10, 10));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
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
