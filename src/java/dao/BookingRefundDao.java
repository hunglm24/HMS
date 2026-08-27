package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBConnectionUtil;

public class BookingRefundDao {
    public void initializeSchema() throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            ensureTable(conn);
        }
    }

    public List<Map<String, Object>> findAll(String status) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnectionUtil.getConnection()) {
            ensureTable(conn);
            String sql = """
                    SELECT rr.*, b.booking_code,
                           COALESCE(bg.full_name, '') AS guest_name
                    FROM booking_refund_requests rr
                    JOIN bookings b ON b.id = rr.booking_id
                    LEFT JOIN booking_guests bg ON bg.booking_id=b.id AND bg.is_primary_guest=1
                    WHERE (? IS NULL OR rr.status = ?)
                    ORDER BY CASE rr.status WHEN 'PENDING' THEN 0 ELSE 1 END, rr.created_at DESC
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (status == null || status.isBlank()) {
                    ps.setNull(1, java.sql.Types.VARCHAR);
                    ps.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(1, status);
                    ps.setString(2, status);
                }
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("bookingId", rs.getLong("booking_id"));
                        row.put("bookingCode", rs.getString("booking_code"));
                        row.put("guestName", rs.getString("guest_name"));
                        row.put("bankName", rs.getString("bank_name"));
                        row.put("accountNumber", rs.getString("account_number"));
                        row.put("accountHolder", rs.getString("account_holder"));
                        row.put("refundAmount", rs.getBigDecimal("refund_amount"));
                        row.put("status", rs.getString("status"));
                        row.put("reason", rs.getString("reason"));
                        row.put("createdAt", rs.getTimestamp("created_at"));
                        rows.add(row);
                    }
                }
            }
        }
        return rows;
    }

    public boolean updateStatus(long requestId, String status) throws SQLException {
        if (!("COMPLETED".equals(status) || "REJECTED".equals(status))) {
            throw new IllegalArgumentException("Trạng thái refund không hợp lệ.");
        }
        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            ensureTable(conn);
            try {
                long bookingId;
                String originalStatus;
                try (PreparedStatement find = conn.prepareStatement(
                        "SELECT booking_id, original_status FROM booking_refund_requests WHERE id=? AND status='PENDING' FOR UPDATE")) {
                    find.setLong(1, requestId);
                    try (java.sql.ResultSet rs = find.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        bookingId = rs.getLong("booking_id");
                        originalStatus = rs.getString("original_status");
                    }
                }
                try (PreparedStatement updateRequest = conn.prepareStatement(
                        "UPDATE booking_refund_requests SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
                    updateRequest.setString(1, status);
                    updateRequest.setLong(2, requestId);
                    updateRequest.executeUpdate();
                }
                if ("COMPLETED".equals(status)) {
                    try (PreparedStatement booking = conn.prepareStatement("""
                            UPDATE bookings SET status='CANCELLED',
                                cancellation_reason='Đã hủy thành công - Manager đã hoàn tiền',
                                cancelled_at=CURRENT_TIMESTAMP, updated_at=CURRENT_TIMESTAMP
                            WHERE id=? AND status='CANCELLATION_PENDING'
                            """)) {
                        booking.setLong(1, bookingId);
                        booking.executeUpdate();
                    }
                } else {
                    if (!("PENDING_PAYMENT".equals(originalStatus) || "CONFIRMED".equals(originalStatus)))
                        originalStatus = "CONFIRMED";
                    try (PreparedStatement booking = conn.prepareStatement("""
                            UPDATE bookings SET status=?, cancellation_reason=NULL,
                                cancelled_at=NULL, updated_at=CURRENT_TIMESTAMP
                            WHERE id=? AND status='CANCELLATION_PENDING'
                            """)) {
                        booking.setString(1, originalStatus);
                        booking.setLong(2, bookingId);
                        booking.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException("Không thể cập nhật yêu cầu hoàn tiền.", ex);
            }
        }
    }
    public void createPendingRefund(long bookingId, String bankName, String accountNumber,
                                    String accountHolder, BigDecimal refundAmount,
                                    Long requestedBy, String reason) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ensureTable(conn);
                String originalStatus;
                try (PreparedStatement lock = conn.prepareStatement(
                        "SELECT status FROM bookings WHERE id=? FOR UPDATE")) {
                    lock.setLong(1, bookingId);
                    try (java.sql.ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Booking không tồn tại.");
                        originalStatus = rs.getString("status");
                        if (!("PENDING_PAYMENT".equals(originalStatus) || "CONFIRMED".equals(originalStatus))) {
                            throw new SQLException("Chỉ có thể hủy booking đang chờ hoặc đã xác nhận.");
                        }
                    }
                }

                try (PreparedStatement insert = conn.prepareStatement("""
                        INSERT INTO booking_refund_requests
                            (booking_id, bank_name, account_number, account_holder,
                             refund_amount, status, original_status, requested_by, reason, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, 'PENDING', ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """)) {
                    insert.setLong(1, bookingId);
                    insert.setString(2, bankName);
                    insert.setString(3, accountNumber);
                    insert.setString(4, accountHolder);
                    insert.setBigDecimal(5, refundAmount);
                    insert.setString(6, originalStatus);
                    if (requestedBy == null) insert.setNull(7, java.sql.Types.BIGINT);
                    else insert.setLong(7, requestedBy);
                    insert.setString(8, reason);
                    insert.executeUpdate();
                }

                try (PreparedStatement cancel = conn.prepareStatement("""
                        UPDATE bookings
                        SET status='CANCELLATION_PENDING', cancellation_reason=?,
                            cancelled_at=NULL, updated_at=CURRENT_TIMESTAMP
                        WHERE id=?
                        """)) {
                    cancel.setString(1, "Đang chờ hủy - Yêu cầu hoàn tiền đang chờ Manager: " + reason);
                    cancel.setLong(2, bookingId);
                    cancel.executeUpdate();
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException("Không thể tạo yêu cầu hoàn tiền.", ex);
            }
        }
    }

    private void ensureTable(Connection conn) throws SQLException {
        ensureCancellationPendingStatus(conn);
        try (PreparedStatement ps = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS booking_refund_requests (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    booking_id BIGINT NOT NULL UNIQUE,
                    bank_name VARCHAR(100) NOT NULL,
                    account_number VARCHAR(40) NOT NULL,
                    account_holder VARCHAR(150) NOT NULL,
                    refund_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                    original_status VARCHAR(30) NULL,
                    requested_by BIGINT NULL,
                    reason VARCHAR(500) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """)) {
            ps.executeUpdate();
        }
        ensureOriginalStatusColumn(conn);
        try (PreparedStatement migrate = conn.prepareStatement("""
                UPDATE booking_refund_requests rr
                JOIN bookings b ON b.id=rr.booking_id
                SET rr.original_status=COALESCE(rr.original_status, 'CONFIRMED'),
                    b.status='CANCELLATION_PENDING',
                    b.cancellation_reason=CONCAT('Đang chờ hủy - ', COALESCE(rr.reason, 'Yêu cầu hoàn tiền')),
                    b.cancelled_at=NULL
                WHERE rr.status='PENDING' AND b.status='CANCELLED'
                """)) {
            migrate.executeUpdate();
        }
    }

    private void ensureCancellationPendingStatus(Connection conn) throws SQLException {
        boolean supported = false;
        try (PreparedStatement ps = conn.prepareStatement("SHOW COLUMNS FROM bookings LIKE 'status'");
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) supported = rs.getString("Type").contains("CANCELLATION_PENDING");
        }
        if (!supported) {
            try (PreparedStatement ps = conn.prepareStatement("""
                    ALTER TABLE bookings MODIFY status ENUM(
                        'PENDING_PAYMENT','CONFIRMED','CHECKED_IN','CHECKOUT_PENDING',
                        'CHECKED_OUT','CANCELLATION_PENDING','CANCELLED','NO_SHOW') NOT NULL
                    """)) {
                ps.executeUpdate();
            }
        }
    }

    private void ensureOriginalStatusColumn(Connection conn) throws SQLException {
        boolean exists = false;
        try (PreparedStatement ps = conn.prepareStatement(
                "SHOW COLUMNS FROM booking_refund_requests LIKE 'original_status'");
             java.sql.ResultSet rs = ps.executeQuery()) {
            exists = rs.next();
        }
        if (!exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "ALTER TABLE booking_refund_requests ADD COLUMN original_status VARCHAR(30) NULL AFTER status")) {
                ps.executeUpdate();
            }
        }
    }
}
