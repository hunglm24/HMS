package controller.api;

import config.VNPayConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(name = "VNPayDemoServlet", urlPatterns = {"/vnpay-demo"})
public class VNPayDemoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!VNPayConfig.isPaymentTestMode()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("pendingBookingId") == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        request.setAttribute("bookingCode", session.getAttribute("pendingBookingCode"));
        request.setAttribute("amount", session.getAttribute("pendingPaymentAmount"));
        request.getRequestDispatcher("/WEB-INF/views/public/vnpay-demo.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!VNPayConfig.isPaymentTestMode()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        Long bookingId = (Long) session.getAttribute("pendingBookingId");
        String bookingCode = (String) session.getAttribute("pendingBookingCode");
        BigDecimal amount = (BigDecimal) session.getAttribute("pendingPaymentAmount");
        if (bookingId == null || bookingCode == null || amount == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if (!"pay".equals(request.getParameter("action"))) {
            request.setAttribute("paymentStatus", "FAILED");
            request.getRequestDispatcher("/WEB-INF/views/public/payment-redirect.jsp").forward(request, response);
            return;
        }

        try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (java.sql.PreparedStatement bookingUpdate = conn.prepareStatement(
                    "UPDATE bookings SET status='CONFIRMED', updated_at=CURRENT_TIMESTAMP WHERE id=? AND status='PENDING_PAYMENT'");
                 java.sql.PreparedStatement paymentInsert = conn.prepareStatement(
                    "INSERT INTO payments (booking_id, amount, payment_method, payment_type, transaction_code, status, paid_at) "
                            + "VALUES (?, ?, 'VNPAY_DEMO', 'BOOKING_PAYMENT', ?, 'SUCCESS', CURRENT_TIMESTAMP)")) {
                bookingUpdate.setLong(1, bookingId);
                int updated = bookingUpdate.executeUpdate();
                if (updated != 1) {
                    throw new IllegalStateException("Booking không còn ở trạng thái chờ thanh toán.");
                }
                paymentInsert.setLong(1, bookingId);
                paymentInsert.setBigDecimal(2, amount);
                paymentInsert.setString(3, "DEMO-" + bookingCode);
                paymentInsert.executeUpdate();
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }

            session.removeAttribute("cart");
            session.removeAttribute("appliedPromotion");
            session.removeAttribute("pendingBookingId");
            session.removeAttribute("pendingBookingCode");
            session.removeAttribute("pendingPaymentAmount");
            request.setAttribute("paymentStatus", "SUCCESS");
        } catch (Exception ex) {
            getServletContext().log("VNPay demo payment failed", ex);
            request.setAttribute("paymentStatus", "FAILED");
        }

        request.getRequestDispatcher("/WEB-INF/views/public/payment-redirect.jsp").forward(request, response);
    }
}
