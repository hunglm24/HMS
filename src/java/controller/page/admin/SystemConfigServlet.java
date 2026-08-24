package controller.page.admin;

import config.AppConstants;
import dao.AuditLogDao;
import dao.HotelConfigDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AuditLog;
import model.HotelConfig;
import service.AuditLogService;
import util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@WebServlet(urlPatterns = {"/admin/system-config"})
public class SystemConfigServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String AUDIT_ACTION = "SAVE_HOTEL_CONFIG";

    private HotelConfigDao hotelConfigDao;
    private AuditLogDao auditLogDao;
    private AuditLogService auditLogService;

    @Override
    public void init() {
        hotelConfigDao = new HotelConfigDao();
        auditLogDao = new AuditLogDao();
        auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            HotelConfig before = hotelConfigDao.findCurrent().orElse(null);
            HotelConfig config = readConfig(request);
            if (before != null && config.getId() == null) {
                config.setId(before.getId());
            }
            HotelConfig saved = hotelConfigDao.save(config);
            request.getServletContext().setAttribute("hotelConfig", saved);
            auditLogService.log(request, AUDIT_ACTION, "HOTEL_CONFIG", saved.getId(), buildChangeDetail(before, saved));
            flash(request, "Đã lưu cấu hình khách sạn.", "success");
            response.sendRedirect(request.getContextPath() + "/admin/system-config");
        } catch (IllegalArgumentException ex) {
            flash(request, ex.getMessage(), "error");
            response.sendRedirect(request.getContextPath() + "/admin/system-config");
        } catch (SQLException ex) {
            getServletContext().log("Cannot save hotel config", ex);
            throw new ServletException("Cannot save hotel config", ex);
        }
    }

    private void loadPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HotelConfig config;
        try {
            config = hotelConfigDao.loadForEdit();
        } catch (SQLException ex) {
            getServletContext().log("Cannot load hotel config", ex);
            config = hotelConfigDao.createDefaultConfig();
            request.setAttribute("error", "Không thể tải cấu hình. Vui lòng kiểm tra kết nối cơ sở dữ liệu.");
        }
        request.getServletContext().setAttribute("hotelConfig", config);
        request.setAttribute("config", config);

        try {
            request.setAttribute("recentConfigLogs", auditLogDao.findByAction(AUDIT_ACTION, 5));
        } catch (SQLException ex) {
            getServletContext().log("Cannot load hotel config audit logs", ex);
            request.setAttribute("recentConfigLogs", new ArrayList<AuditLog>());
        }
        request.getRequestDispatcher("/WEB-INF/views/admin/system-config.jsp").forward(request, response);
    }

    private HotelConfig readConfig(HttpServletRequest request) {
        HotelConfig config = new HotelConfig();
        Long id = ValidationUtil.optionalPositiveLong(request.getParameter("id"), "Cấu hình");
        if (id != null) {
            config.setId(id);
        }

        config.setHotelName(ValidationUtil.requireText(request.getParameter("hotelName"), "Tên khách sạn", 2, 150));
        config.setAddress(ValidationUtil.requireText(request.getParameter("address"), "Địa chỉ", 5, 255));
        config.setPhone(validatePhone(request.getParameter("phone")));
        config.setEmail(validateEmail(request.getParameter("email")));
        config.setCheckInTime(parseTime(request.getParameter("checkInTime"), "Giờ nhận phòng"));
        config.setCheckOutTime(parseTime(request.getParameter("checkOutTime"), "Giờ trả phòng"));
        ValidationUtil.requireTrue(!Objects.equals(config.getCheckInTime(), config.getCheckOutTime()),
                "Giờ nhận phòng và giờ trả phòng không được trùng nhau.");
        config.setSameDayRefundRate(parseRate(request.getParameter("sameDayRefundRate"), "Tỷ lệ hoàn tiền cùng ngày"));
        config.setBeforeDayRefundRate(parseRate(request.getParameter("beforeDayRefundRate"), "Tỷ lệ hoàn tiền trước ngày"));
        config.setTaxRate(parseRate(request.getParameter("taxRate"), "Thuế"));
        config.setServiceFeeRate(parseRate(request.getParameter("serviceFeeRate"), "Phí dịch vụ"));
        return config;
    }

    private Time parseTime(String value, String fieldName) {
        if (ValidationUtil.isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " bắt buộc.");
        }
        try {
            return Time.valueOf(LocalTime.parse(value.trim()));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " phải có định dạng HH:mm.");
        }
    }

    private BigDecimal parseRate(String value, String fieldName) {
        BigDecimal rate = ValidationUtil.optionalBigDecimal(value, fieldName);
        if (rate == null) {
            throw new IllegalArgumentException(fieldName + " bắt buộc.");
        }
        ValidationUtil.requireTrue(rate.compareTo(BigDecimal.ZERO) >= 0,
                fieldName + " không được nhỏ hơn 0.");
        ValidationUtil.requireTrue(rate.compareTo(BigDecimal.valueOf(100)) <= 0,
                fieldName + " không được lớn hơn 100.");
        return rate;
    }

    private String validateEmail(String value) {
        String email = ValidationUtil.requireText(value, "Email", 5, 150).toLowerCase(Locale.ROOT);
        ValidationUtil.requireTrue(AppConstants.EMAIL_PATTERN.matcher(email).matches(),
                "Email không đúng định dạng.");
        return email;
    }

    private String validatePhone(String value) {
        String phone = ValidationUtil.requireText(value, "Số điện thoại", 5, 30);
        ValidationUtil.requireTrue(AppConstants.PHONE_PATTERN.matcher(phone).matches(),
                "Số điện thoại không đúng định dạng.");
        return phone;
    }

    private String buildChangeDetail(HotelConfig before, HotelConfig after) {
        if (before == null) {
            return "Khởi tạo cấu hình khách sạn.";
        }
        List<String> changes = new ArrayList<>();
        addChange(changes, "Tên khách sạn", before.getHotelName(), after.getHotelName());
        addChange(changes, "Địa chỉ", before.getAddress(), after.getAddress());
        addChange(changes, "Số điện thoại", before.getPhone(), after.getPhone());
        addChange(changes, "Email", before.getEmail(), after.getEmail());
        addChange(changes, "Giờ nhận phòng", formatTime(before.getCheckInTime()), formatTime(after.getCheckInTime()));
        addChange(changes, "Giờ trả phòng", formatTime(before.getCheckOutTime()), formatTime(after.getCheckOutTime()));
        addChange(changes, "Tỷ lệ hoàn tiền cùng ngày", formatRate(before.getSameDayRefundRate()), formatRate(after.getSameDayRefundRate()));
        addChange(changes, "Tỷ lệ hoàn tiền trước ngày", formatRate(before.getBeforeDayRefundRate()), formatRate(after.getBeforeDayRefundRate()));
        addChange(changes, "Thuế", formatRate(before.getTaxRate()), formatRate(after.getTaxRate()));
        addChange(changes, "Phí dịch vụ", formatRate(before.getServiceFeeRate()), formatRate(after.getServiceFeeRate()));
        return changes.isEmpty() ? "Lưu cấu hình không thay đổi." : String.join(" | ", changes);
    }

    private void addChange(List<String> changes, String label, String before, String after) {
        if (!Objects.equals(before, after)) {
            changes.add(label + ": " + before + " -> " + after);
        }
    }

    private String formatTime(Time time) {
        return time == null ? "-" : time.toLocalTime().withSecond(0).withNano(0).toString();
    }

    private String formatRate(BigDecimal rate) {
        return rate == null ? "-" : rate.stripTrailingZeros().toPlainString() + "%";
    }

    private void flash(HttpServletRequest request, String message, String type) {
        request.getSession().setAttribute("toastMessage", message);
        request.getSession().setAttribute("toastType", "success".equalsIgnoreCase(type) ? "toast-success" : "toast-error");
    }
}
