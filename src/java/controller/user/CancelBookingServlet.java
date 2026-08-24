package controller.user;

import dao.HotelConfigDao;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HotelConfig;

@WebServlet(name = "CancelBookingServlet", urlPatterns = {"/user/cancel-booking"})
public class CancelBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xử lý hiển thị trang JSP.

        request.getRequestDispatcher("/WEB-INF/views/public/booking-detail-guest.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        model.User user = (model.User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String bookingIdStr = request.getParameter("bookingId");
        String reason = request.getParameter("reason");

        if (bookingIdStr == null || bookingIdStr.isBlank() || reason == null || reason.trim().isEmpty()) {
            request.getSession().setAttribute("error", "Vui lòng nhập lý do hủy phòng.");
            response.sendRedirect(request.getContextPath() + "/my-bookings");
            return;
        }

        try {
            long bookingId = Long.parseLong(bookingIdStr);
            dao.BookingDao bookingDao = new dao.BookingDao();
            model.Booking booking = bookingDao.findById(bookingId).orElse(null);

            if (booking == null || booking.getCustomerId() == null || booking.getCustomerId() != user.getId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền hủy đặt phòng này.");
                return;
            }

            if (!"PENDING_PAYMENT".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
                request.getSession().setAttribute("error", "Chỉ có thể hủy phòng khi đang chờ thanh toán hoặc đã xác nhận.");
                response.sendRedirect(request.getContextPath() + "/booking-detail?id=" + bookingId);
                return;
            }

            java.time.LocalDate checkIn = booking.getCheckInDate().toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(today, checkIn);

            HotelConfig hotelConfig = resolveHotelConfig(request);
            BigDecimal refundRate = resolveRefundRate(hotelConfig, daysUntilCheckIn < 2);
            BigDecimal totalAmount = booking.getTotalAmount() == null ? BigDecimal.ZERO : booking.getTotalAmount();
            BigDecimal refundAmount = totalAmount
                    .multiply(refundRate)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            BigDecimal cancellationFee = totalAmount.subtract(refundAmount);

            String fullReason = reason
                    + " | Tỷ lệ hoàn tiền áp dụng: " + refundRate.stripTrailingZeros().toPlainString() + "%"
                    + " | Số tiền hoàn dự kiến: " + refundAmount + " VND"
                    + " | Phí hủy dự kiến: " + cancellationFee + " VND";

            boolean success = bookingDao.cancelBooking(bookingId, fullReason);
            if (success) {
                String feeMessage = cancellationFee.compareTo(BigDecimal.ZERO) > 0
                        ? "Phí hủy áp dụng: " + cancellationFee + " VND."
                        : "Bạn được miễn phí hủy phòng.";
                request.getSession().setAttribute("message",
                        "Hủy phòng thành công. Tỷ lệ hoàn tiền áp dụng: "
                                + refundRate.stripTrailingZeros().toPlainString()
                                + "%. " + feeMessage);
            } else {
                request.getSession().setAttribute("error", "Hệ thống bận, vui lòng thử lại sau.");
            }
            
            response.sendRedirect(request.getContextPath() + "/booking-detail?id=" + bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private HotelConfig resolveHotelConfig(HttpServletRequest request) {
        Object cached = request.getServletContext().getAttribute("hotelConfig");
        if (cached instanceof HotelConfig) {
            return (HotelConfig) cached;
        }

        HotelConfigDao hotelConfigDao = new HotelConfigDao();
        try {
            return hotelConfigDao.loadForEdit();
        } catch (Exception ex) {
            return hotelConfigDao.createDefaultConfig();
        }
    }

    private BigDecimal resolveRefundRate(HotelConfig config, boolean sameDayCancellation) {
        BigDecimal rate = sameDayCancellation
                ? config.getSameDayRefundRate()
                : config.getBeforeDayRefundRate();
        if (rate == null) {
            throw new IllegalStateException("Cấu hình hoàn tiền không hợp lệ.");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalStateException("Cấu hình hoàn tiền không hợp lệ.");
        }
        return rate;
    }
}
