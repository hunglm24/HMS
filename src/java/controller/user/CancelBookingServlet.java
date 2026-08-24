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
        // TODO: Xu ly hien thi trang JSP.

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
            request.getSession().setAttribute("error", "Vui long nhap ly do huy phong.");
            response.sendRedirect(request.getContextPath() + "/my-bookings");
            return;
        }

        try {
            long bookingId = Long.parseLong(bookingIdStr);
            dao.BookingDao bookingDao = new dao.BookingDao();
            model.Booking booking = bookingDao.findById(bookingId).orElse(null);

            if (booking == null || booking.getCustomerId() == null || booking.getCustomerId() != user.getId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Khong co quyen huy dat phong nay.");
                return;
            }

            if (!"PENDING_PAYMENT".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
                request.getSession().setAttribute("error", "Chi co the huy phong khi dang cho thanh toan hoac da xac nhan.");
                response.sendRedirect(request.getContextPath() + "/booking-detail?id=" + bookingId);
                return;
            }

            CancellationPolicyService policyService = new CancellationPolicyService();
            CancellationPolicyService.RefundResult refund =
                    policyService.calculateRefund(booking, java.time.LocalDate.now());

            String fullReason = reason
                    + " | Ty le hoan: " + refund.getRefundRate().stripTrailingZeros().toPlainString() + "%"
                    + " | So tien hoan du kien: " + refund.getRefundAmount() + " VND"
                    + " | Phi huy du kien: " + refund.getCancellationFee() + " VND"
                    + " | Nguon chinh sach: " + (refund.isFromPolicy() ? "Manager" : "Mac dinh he thong");

            boolean success = bookingDao.cancelBooking(bookingId, fullReason);
            if (success) {
                request.getSession().setAttribute("message",
                        "Huy phong thanh cong. Ty le hoan "
                                + refund.getRefundRate().stripTrailingZeros().toPlainString()
                                + "%, so tien hoan du kien: " + refund.getRefundAmount()
                                + " VND, phi huy: " + refund.getCancellationFee() + " VND.");
            } else {
                request.getSession().setAttribute("error", "He thong ban, vui long thu lai sau.");
            }

            response.sendRedirect(request.getContextPath() + "/booking-detail?id=" + bookingId);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
