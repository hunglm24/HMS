package controller.user;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

            // Calculate Fee
            java.time.LocalDate checkIn = booking.getCheckInDate().toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(today, checkIn);
            
            java.math.BigDecimal fee = java.math.BigDecimal.ZERO;
            if (daysUntilCheckIn < 2) {
                // Phạt 20% nếu hủy sát ngày (dưới 48h)
                fee = booking.getTotalAmount().multiply(new java.math.BigDecimal("0.20"));
            }

            String fullReason = reason + " | Phí hủy dự kiến: " + fee + " VND";
            
            boolean success = bookingDao.cancelBooking(bookingId, fullReason);
            if (success) {
                request.getSession().setAttribute("message", "Hủy phòng thành công. " + (fee.compareTo(java.math.BigDecimal.ZERO) > 0 ? "Phí hủy áp dụng: " + fee + " VND." : "Bạn được miễn phí hủy phòng."));
            } else {
                request.getSession().setAttribute("error", "Hệ thống bận, vui lòng thử lại sau.");
            }
            
            response.sendRedirect(request.getContextPath() + "/booking-detail?id=" + bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
