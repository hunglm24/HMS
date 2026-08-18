package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.Payment;
import util.DBConnectionUtil;

public class PaymentDao {

    // UC18: Thanh toán online
    public boolean addPayment(Payment payment) {
        String sql = "INSERT INTO payments (booking_id, payment_type, payment_method, amount, status, transaction_code, paid_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, payment.getBookingId());
            ps.setString(2, payment.getPaymentType());
            ps.setString(3, payment.getPaymentMethod());
            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getStatus());
            ps.setString(6, payment.getTransactionCode());
            ps.setTimestamp(7, payment.getPaidAt());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
