package dao;

import model.Feedback;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDao {
    
    public static class FeedbackDto extends Feedback {
        private String customerName;
        private String bookingCode;
        private String roomNumbers;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getBookingCode() { return bookingCode; }
        public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
        public String getRoomNumbers() { return roomNumbers; }
        public void setRoomNumbers(String roomNumbers) { this.roomNumbers = roomNumbers; }
    }

    public void insertFeedback(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedbacks (booking_id, customer_id, rating, comment, status, created_at, updated_at) VALUES (?, ?, ?, ?, 'VISIBLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, feedback.getBookingId());
            ps.setLong(2, feedback.getCustomerId());
            ps.setInt(3, feedback.getRating());
            ps.setString(4, feedback.getComment());
            ps.executeUpdate();
        }
    }

    public boolean hasFeedback(long bookingId, long customerId) throws SQLException {
        String sql = "SELECT 1 FROM feedbacks WHERE booking_id = ? AND customer_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setLong(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<FeedbackDto> findAllFeedbacks() throws SQLException {
        List<FeedbackDto> list = new ArrayList<>();
        String sql = "SELECT f.*, a.full_name as customer_name, b.booking_code, " +
                     "(SELECT GROUP_CONCAT(rm.room_number SEPARATOR ', ') " +
                     " FROM booking_rooms br JOIN rooms rm ON br.room_id = rm.id " +
                     " WHERE br.booking_id = f.booking_id) as room_numbers " +
                     "FROM feedbacks f " +
                     "JOIN accounts a ON f.customer_id = a.id " +
                     "JOIN bookings b ON f.booking_id = b.id " +
                     "ORDER BY f.created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FeedbackDto dto = new FeedbackDto();
                dto.setId(rs.getLong("id"));
                dto.setBookingId(rs.getLong("booking_id"));
                dto.setCustomerId(rs.getLong("customer_id"));
                dto.setRating(rs.getInt("rating"));
                dto.setComment(rs.getString("comment"));
                dto.setStatus(rs.getString("status"));
                dto.setCreatedAt(rs.getTimestamp("created_at"));
                dto.setUpdatedAt(rs.getTimestamp("updated_at"));
                dto.setCustomerName(rs.getString("customer_name"));
                dto.setBookingCode(rs.getString("booking_code"));
                dto.setRoomNumbers(rs.getString("room_numbers"));
                list.add(dto);
            }
        }
        return list;
    }
}
