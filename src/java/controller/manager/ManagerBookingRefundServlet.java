package controller.manager;

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

@WebServlet(name = "ManagerBookingRefundServlet", urlPatterns = {"/manager/bookings/refund"})
public class ManagerBookingRefundServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();
    private final BookingRefundDao refundDao = new BookingRefundDao();
    private final CancellationPolicyService policyService = new CancellationPolicyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long bookingId = parseBookingId(request);
            Booking booking = bookingDao.findById(bookingId).orElseThrow(
                    () -> new IllegalArgumentException("Không tìm thấy booking."));
            ensureCancellable(booking);
            request.setAttribute("booking", booking);
            request.setAttribute("refund", policyService.calculateRefund(booking, LocalDate.now()));
            request.getRequestDispatcher("/WEB-INF/views/manager/booking-refund.jsp")
                    .forward(request, response);
        } catch (Exception ex) {
            request.getSession().setAttribute("error", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/manager/bookings");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            long bookingId = parseBookingId(request);
            Booking booking = bookingDao.findById(bookingId).orElseThrow(
                    () -> new IllegalArgumentException("Không tìm thấy booking."));
            ensureCancellable(booking);
            String bankName = required(request, "bankName", "Ngân hàng", 100);
            String accountHolder = required(request, "accountHolder", "Chủ tài khoản", 150)
                    .toUpperCase(java.util.Locale.ROOT);
            String accountNumber = required(request, "accountNumber", "Số tài khoản", 40)
                    .replaceAll("\\s+", "");
            String reason = required(request, "reason", "Lý do hủy", 500);
            if (!accountNumber.matches("[0-9]{6,30}")) {
                throw new IllegalArgumentException("Số tài khoản phải gồm 6 đến 30 chữ số.");
            }

            CancellationPolicyService.RefundResult refund =
                    policyService.calculateRefund(booking, LocalDate.now());
            User manager = (User) request.getSession().getAttribute("currentUser");
            refundDao.createPendingRefund(bookingId, bankName, accountNumber, accountHolder,
                    refund.getRefundAmount(), manager == null ? null : manager.getId(), reason);
            request.getSession().setAttribute("toastMessage",
                    "Đã hủy booking và tạo yêu cầu hoàn tiền đang chờ xử lý.");
            request.getSession().setAttribute("toastType", "toast-success");
        } catch (Exception ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/manager/bookings");
    }

    private long parseBookingId(HttpServletRequest request) {
        try {
            long id = Long.parseLong(request.getParameter("id"));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Booking không hợp lệ.");
        }
    }

    private void ensureCancellable(Booking booking) {
        if (!("PENDING_PAYMENT".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus()))) {
            throw new IllegalArgumentException("Booking ở trạng thái này không thể hủy.");
        }
    }

    private String required(HttpServletRequest request, String name, String label, int max) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(label + " là bắt buộc.");
        value = value.trim();
        if (value.length() > max) throw new IllegalArgumentException(label + " quá dài.");
        return value;
    }
}
