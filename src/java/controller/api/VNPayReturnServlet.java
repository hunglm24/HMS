package controller.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import config.VNPayConfig;
import service.VNPayService;
import service.AuditLogService;

@WebServlet(name = "VNPayReturnServlet", urlPatterns = {"/payment-return"})
public class VNPayReturnServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final VNPayService vnPayService = new VNPayService();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        HttpSession session = request.getSession();
        Long bookingId = (Long) session.getAttribute("pendingBookingId");
        String bookingCode = (String) session.getAttribute("pendingBookingCode");
        java.math.BigDecimal amount = (java.math.BigDecimal) session.getAttribute("pendingPaymentAmount");

        try {
            boolean valid = vnPayService.verifySignature(fields, vnp_SecureHash);
            boolean matchingRef = bookingCode != null && bookingCode.equals(request.getParameter("vnp_TxnRef"));
            boolean matchingMerchant = VNPayConfig.vnp_TmnCode.equals(request.getParameter("vnp_TmnCode"));
            boolean matchingAmount = amount != null && amount.movePointRight(2).toBigIntegerExact().toString()
                    .equals(request.getParameter("vnp_Amount"));
            if (valid && matchingMerchant && matchingRef && matchingAmount && bookingId != null
                    && "00".equals(responseCode) && "00".equals(transactionStatus)) {
                try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                    conn.setAutoCommit(false);
                    try (java.sql.PreparedStatement bookingUpdate = conn.prepareStatement(
                            "UPDATE bookings SET status='CONFIRMED', updated_at=CURRENT_TIMESTAMP WHERE id=? AND status='PENDING_PAYMENT'");
                         java.sql.PreparedStatement paymentInsert = conn.prepareStatement(
                            "INSERT INTO payments (booking_id, amount, payment_method, payment_type, transaction_code, status, paid_at) "
                                    + "VALUES (?, ?, 'ONLINE_PAYMENT', 'BOOKING_PAYMENT', ?, 'SUCCESS', CURRENT_TIMESTAMP)")) {
                        bookingUpdate.setLong(1, bookingId);
                        int updated = bookingUpdate.executeUpdate();
                        if (updated == 1) {
                            paymentInsert.setLong(1, bookingId);
                            paymentInsert.setBigDecimal(2, amount);
                            paymentInsert.setString(3, request.getParameter("vnp_TransactionNo"));
                            paymentInsert.executeUpdate();
                            auditLogService.log(request, "BOOKING_PAYMENT_SUCCESS", "PAYMENT", bookingId,
                                    "VNPay payment confirmed for booking " + bookingCode + " amount=" + amount);
                        }
                        conn.commit();
                        
                        // Gửi email xác nhận
                        dao.BookingDao bookingDao = new dao.BookingDao();
                        if (updated == 1) bookingDao.findById(bookingId).ifPresent(booking -> {
                            try {
                                bookingDao.findCheckInBookingById(bookingId.intValue()).ifPresent(summary -> {
                                    service.EmailService emailService = new service.EmailService();
                                    emailService.sendBookingConfirmationAsync(booking, summary.getEmail(), summary.getGuestName());
                                });
                            } catch (Exception ignored) {}
                        });
                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    }
                }
                session.removeAttribute("cart");
                session.removeAttribute("pendingBookingId");
                session.removeAttribute("pendingBookingCode");
                session.removeAttribute("pendingPaymentAmount");
                request.setAttribute("paymentStatus", "SUCCESS");
            } else {
                request.setAttribute("paymentStatus", "FAILED");
            }
        } catch (Exception ex) {
            getServletContext().log("Không thể xác minh kết quả thanh toán VNPay", ex);
            request.setAttribute("paymentStatus", "FAILED");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/public/payment-redirect.jsp").forward(request, response);
    }
}
