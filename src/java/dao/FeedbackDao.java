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
        private String roomTypeNames;
        private Long primaryRoomId;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getBookingCode() { return bookingCode; }
        public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
        public String getRoomNumbers() { return roomNumbers; }
        public void setRoomNumbers(String roomNumbers) { this.roomNumbers = roomNumbers; }
        public String getRoomTypeNames() { return roomTypeNames; }
        public void setRoomTypeNames(String roomTypeNames) { this.roomTypeNames = roomTypeNames; }
        public Long getPrimaryRoomId() { return primaryRoomId; }
        public void setPrimaryRoomId(Long primaryRoomId) { this.primaryRoomId = primaryRoomId; }
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
        return findFeedbacks(null, null, null);
    }

    public List<FeedbackDto> findFeedbacks(String keyword, Integer rating, String status) throws SQLException {
        List<FeedbackDto> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT f.*, a.full_name as customer_name, b.booking_code, " +
            "(SELECT GROUP_CONCAT(rm.room_number SEPARATOR ', ') " +
            " FROM booking_rooms br JOIN rooms rm ON br.room_id = rm.id " +
            " WHERE br.booking_id = f.booking_id) as room_numbers, " +
            "(SELECT GROUP_CONCAT(DISTINCT rt.name SEPARATOR ', ') " +
            " FROM booking_rooms br JOIN rooms rm ON br.room_id = rm.id JOIN room_types rt ON rm.room_type_id = rt.id " +
            " WHERE br.booking_id = f.booking_id) as room_type_names, " +
            "(SELECT br2.room_id FROM booking_rooms br2 WHERE br2.booking_id = f.booking_id LIMIT 1) as primary_room_id " +
            "FROM feedbacks f " +
            "JOIN accounts a ON f.customer_id = a.id " +
            "JOIN bookings b ON f.booking_id = b.id " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kwPattern = "%" + keyword.trim() + "%";
            sql.append("AND (a.full_name LIKE ? OR b.booking_code LIKE ? OR f.comment LIKE ? OR EXISTS (SELECT 1 FROM booking_rooms br JOIN rooms rm ON br.room_id = rm.id WHERE br.booking_id = f.booking_id AND rm.room_number LIKE ?)) ");
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
        }

        if (rating != null && rating > 0) {
            sql.append("AND f.rating = ? ");
            params.add(rating);
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND f.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append("ORDER BY f.created_at DESC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDto(rs));
                }
            }
        }
        return list;
    }

    public List<FeedbackDto> findFeaturedFeedbacks(int limit) {
        List<FeedbackDto> list = new ArrayList<>();
        String sql = "SELECT f.*, a.full_name as customer_name, b.booking_code, " +
                     "(SELECT GROUP_CONCAT(rm.room_number SEPARATOR ', ') " +
                     " FROM booking_rooms br JOIN rooms rm ON br.room_id = rm.id " +
                     " WHERE br.booking_id = f.booking_id) as room_numbers, " +
                     "(SELECT GROUP_CONCAT(DISTINCT rt.name SEPARATOR ', ') " +
                     " FROM booking_rooms br JOIN rooms rm ON br.room_id = rm.id JOIN room_types rt ON rm.room_type_id = rt.id " +
                     " WHERE br.booking_id = f.booking_id) as room_type_names, " +
                     "(SELECT br2.room_id FROM booking_rooms br2 WHERE br2.booking_id = f.booking_id LIMIT 1) as primary_room_id " +
                     "FROM feedbacks f " +
                     "JOIN accounts a ON f.customer_id = a.id " +
                     "JOIN bookings b ON f.booking_id = b.id " +
                     "WHERE f.status = 'VISIBLE' " +
                     "ORDER BY (CASE WHEN f.comment IS NOT NULL AND TRIM(f.comment) <> '' THEN 1 ELSE 2 END), f.rating DESC, f.created_at DESC LIMIT ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateFeedbackStatus(long id, String status) throws SQLException {
        String sql = "UPDATE feedbacks SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public double getAverageRating() {
        String sql = "SELECT AVG(rating) FROM feedbacks WHERE status = 'VISIBLE'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 5.0;
    }

    public int countTotalFeedbacks() {
        String sql = "SELECT COUNT(*) FROM feedbacks WHERE status = 'VISIBLE'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private FeedbackDto mapDto(ResultSet rs) throws SQLException {
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
        try {
            dto.setRoomTypeNames(rs.getString("room_type_names"));
        } catch (SQLException ignored) {}
        long priRoom = rs.getLong("primary_room_id");
        if (!rs.wasNull()) {
            dto.setPrimaryRoomId(priRoom);
        }
        return dto;
    }
}
