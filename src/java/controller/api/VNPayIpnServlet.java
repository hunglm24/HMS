package controller.api;

import config.VNPayConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.VNPayService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/** Server-to-server payment notification. This endpoint must be publicly reachable over HTTPS. */
@WebServlet(name = "VNPayIpnServlet", urlPatterns = {"/api/vnpay/ipn"})
public class VNPayIpnServlet extends HttpServlet {
    private final VNPayService vnPayService = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            String value = request.getParameter(name);
            if (name.startsWith("vnp_") && value != null && !value.isEmpty()) fields.put(name, value);
        }
        String secureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        try {
            if (!vnPayService.verifySignature(fields, secureHash)
                    || !VNPayConfig.vnp_TmnCode.equals(fields.get("vnp_TmnCode"))) {
                reply(response, "97", "Invalid checksum");
                return;
            }
            String bookingCode = fields.get("vnp_TxnRef");
            BigDecimal paidAmount = new BigDecimal(fields.get("vnp_Amount")).movePointLeft(2);
            try (Connection conn = util.DBConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement find = conn.prepareStatement(
                        "SELECT id, total_amount, status FROM bookings WHERE booking_code=? FOR UPDATE")) {
                    find.setString(1, bookingCode);
                    try (ResultSet rs = find.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            reply(response, "01", "Order not found");
                            return;
                        }
                        long bookingId = rs.getLong("id");
                        if (rs.getBigDecimal("total_amount").compareTo(paidAmount) != 0) {
                            conn.rollback();
                            reply(response, "04", "Invalid amount");
                            return;
                        }
                        if (!"PENDING_PAYMENT".equals(rs.getString("status"))) {
                            conn.rollback();
                            reply(response, "02", "Order already confirmed");
                            return;
                        }
                        boolean success = "00".equals(fields.get("vnp_ResponseCode"))
                                && "00".equals(fields.get("vnp_TransactionStatus"));
                        if (success) {
                            try (PreparedStatement update = conn.prepareStatement(
                                    "UPDATE bookings SET status='CONFIRMED', updated_at=CURRENT_TIMESTAMP WHERE id=?");
                                 PreparedStatement insert = conn.prepareStatement(
                                    "INSERT INTO payments (booking_id, amount, payment_method, payment_type, transaction_code, status, paid_at) "
                                            + "VALUES (?, ?, 'ONLINE_PAYMENT', 'BOOKING_PAYMENT', ?, 'SUCCESS', CURRENT_TIMESTAMP)")) {
                                update.setLong(1, bookingId);
                                update.executeUpdate();
                                insert.setLong(1, bookingId);
                                insert.setBigDecimal(2, paidAmount);
                                insert.setString(3, fields.get("vnp_TransactionNo"));
                                insert.executeUpdate();
                            }
                        }
                        conn.commit();
                        reply(response, "00", "Confirm success");
                    }
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            }
        } catch (Exception ex) {
            getServletContext().log("VNPay IPN processing failed", ex);
            reply(response, "99", "Unknown error");
        }
    }

    private void reply(HttpServletResponse response, String code, String message) throws IOException {
        response.getWriter().write("{\"RspCode\":\"" + code + "\",\"Message\":\"" + message + "\"}");
    }
}
