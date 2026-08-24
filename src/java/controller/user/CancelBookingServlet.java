package controller.user;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CancellationPolicyService;

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

            CancellationPolicyService policyService = new CancellationPolicyService();
            CancellationPolicyService.RefundResult refund =
                    policyService.calculateRefund(booking, java.time.LocalDate.now());

            String fullReason = reason
                    + " | Tỷ lệ hoàn: " + refund.getRefundRate().stripTrailingZeros().toPlainString() + "%"
                    + " | Số tiền hoàn dự kiến: " + refund.getRefundAmount() + " VND"
                    + " | Phí hủy dự kiến: " + refund.getCancellationFee() + " VND"
                    + " | Nguồn chính sách: " + (refund.isFromPolicy() ? "Manager" : "Mặc định hệ thống");
            
            boolean success = bookingDao.cancelBooking(bookingId, fullReason);
            if (success) {
                request.getSession().setAttribute("message",
                        "Hủy phòng thành công. Tỷ lệ hoàn "
                                + refund.getRefundRate().stripTrailingZeros().toPlainString()
                                + "%, số tiền hoàn dự kiến: " + refund.getRefundAmount()
                                + " VND, phí hủy: " + refund.getCancellationFee() + " VND.");
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
