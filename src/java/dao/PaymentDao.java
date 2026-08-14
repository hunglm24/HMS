package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Payment;
import util.DBConnectionUtil;

public class PaymentDAO {

    // UC18: Thanh toán online
    public boolean addPayment(Payment payment) {
        String sql = "INSERT INTO payment (booking_id, payment_type, method, amount, status, transaction_code, paid_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment.getBookingId());
            ps.setString(2, payment.getPaymentType());
            ps.setString(3, payment.getMethod());
            ps.setDouble(4, payment.getAmount());
            ps.setString(5, payment.getStatus());
            ps.setString(6, payment.getTransactionCode());
            ps.setDate(7, payment.getPaidAt() != null ? new java.sql.Date(payment.getPaidAt().getTime()) : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
