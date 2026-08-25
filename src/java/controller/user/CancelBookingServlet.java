package controller.user;

import dao.BookingDao;
import dao.BookingRefundDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import model.Booking;
import model.User;
import service.CancellationPolicyService;

@WebServlet(name = "CancelBookingServlet", urlPatterns = {"/user/cancel-booking"})
public class CancelBookingServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();
    private final BookingRefundDao refundDao = new BookingRefundDao();
    private final CancellationPolicyService policyService = new CancellationPolicyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Booking booking = ownedCancellableBooking(request);
            request.setAttribute("booking", booking);
            request.setAttribute("refund", policyService.calculateRefund(booking, LocalDate.now()));
            request.getRequestDispatcher("/WEB-INF/views/public/refund-request.jsp").forward(request, response);
        } catch (Exception ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/my-bookings");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Booking booking = ownedCancellableBooking(request);
            String bankName = required(request, "bankName", "Ngân hàng", 100);
            String accountHolder = required(request, "accountHolder", "Chủ tài khoản", 150)
                    .toUpperCase(java.util.Locale.ROOT);
            String accountNumber = required(request, "accountNumber", "Số tài khoản", 40)
                    .replaceAll("\\s+", "");
            String reason = required(request, "reason", "Lý do hủy", 500);
            if (!accountNumber.matches("[0-9]{6,30}"))
                throw new IllegalArgumentException("Số tài khoản phải gồm 6 đến 30 chữ số.");

            CancellationPolicyService.RefundResult refund =
                    policyService.calculateRefund(booking, LocalDate.now());
            User user = currentUser(request);
            refundDao.createPendingRefund(booking.getId(), bankName, accountNumber,
                    accountHolder, refund.getRefundAmount(), user.getId(), reason);
            request.getSession().setAttribute("message",
                    "Đã gửi yêu cầu hoàn tiền cho Manager. Số tiền dự kiến: "
                            + refund.getRefundAmount() + " VND.");
        } catch (Exception ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/my-bookings");
    }

    private Booking ownedCancellableBooking(HttpServletRequest request) throws Exception {
        User user = currentUser(request);
        String rawId = request.getParameter("bookingId");
        long bookingId;
        try { bookingId = Long.parseLong(rawId); }
        catch (Exception ex) { throw new IllegalArgumentException("Booking không hợp lệ."); }
        Booking booking = bookingDao.findById(bookingId).orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy booking."));
        if (booking.getCustomerId() == null || booking.getCustomerId().longValue() != user.getId())
            throw new SecurityException("Bạn không có quyền hủy booking này.");
        if (!("PENDING_PAYMENT".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())))
            throw new IllegalArgumentException("Booking ở trạng thái này không thể hủy.");
        return booking;
    }

    private User currentUser(HttpServletRequest request) {
        Object value = request.getSession().getAttribute("currentUser");
        if (!(value instanceof User)) throw new SecurityException("Vui lòng đăng nhập.");
        return (User) value;
    }

    private String required(HttpServletRequest request, String name, String label, int max) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " là bắt buộc.");
        value = value.trim();
        if (value.length() > max) throw new IllegalArgumentException(label + " quá dài.");
        return value;
    }
}
