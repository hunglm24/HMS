package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import util.DBConnectionUtil;

/** Cancels unpaid bookings after the same 15-minute window used by VNPay. */
public class PendingPaymentExpirationService {
    public static final int EXPIRATION_MINUTES = 15;

    public int expireOverdueBookings() throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // A payment row is normally created only after success, but mark
                // any explicit pending transaction as failed if one exists.
                try (PreparedStatement payments = conn.prepareStatement("""
                        UPDATE payments p
                        JOIN bookings b ON b.id = p.booking_id
                        SET p.status='FAILED'
                        WHERE b.status='PENDING_PAYMENT'
                          AND b.created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 MINUTE)
                          AND p.status='PENDING'
                        """)) {
                    payments.executeUpdate();
                }

                int expired;
                try (PreparedStatement bookings = conn.prepareStatement("""
                        UPDATE bookings
                        SET status='CANCELLED',
                            cancellation_reason='Thanh toán quá hạn 15 phút',
                            cancelled_at=CURRENT_TIMESTAMP,
                            updated_at=CURRENT_TIMESTAMP
                        WHERE status='PENDING_PAYMENT'
                          AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 MINUTE)
                        """)) {
                    expired = bookings.executeUpdate();
                }
                conn.commit();
                return expired;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException("Không thể hủy booking quá hạn.", ex);
            }
        }
    }
}
