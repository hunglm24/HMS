package dao;

import model.Booking;
import model.BookingRoom;
import util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDao {

    public boolean insertBooking(Booking booking, List<BookingRoom> rooms) {
        String insertBookingSql = "INSERT INTO bookings (booking_code, customer_id, booking_source, check_in_date, check_out_date, " +
                "check_in_datetime, check_out_datetime, total_room_amount, total_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String insertBookingRoomSql = "INSERT INTO booking_rooms (booking_id, room_id, price_per_night, number_of_nights, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";
                
        // Query to find an available room id for a given room_type_id
        String findAvailableRoomSql = "SELECT r.id FROM rooms r " +
                "WHERE r.room_type_id = ? AND r.status = 'AVAILABLE' " +
                "AND r.id NOT IN ( " +
                "  SELECT br.room_id FROM booking_rooms br " +
                "  JOIN bookings b ON br.booking_id = b.id " +
                "  WHERE b.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') " +
                "  AND (b.check_in_date < ? AND b.check_out_date > ?) " +
                ") LIMIT 1";

        Connection conn = null;
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false); // Transaction start
            
            long bookingId = 0;
            
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, booking.getBookingCode());
                if (booking.getCustomerId() != null) {
                    ps.setLong(2, booking.getCustomerId());
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, booking.getBookingSource());
                ps.setDate(4, booking.getCheckInDate());
                ps.setDate(5, booking.getCheckOutDate());
                ps.setTimestamp(6, booking.getCheckInDatetime());
                ps.setTimestamp(7, booking.getCheckOutDatetime());
                ps.setBigDecimal(8, booking.getTotalRoomAmount());
                ps.setBigDecimal(9, booking.getTotalAmount());
                ps.setString(10, booking.getStatus());
                
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookingId = rs.getLong(1);
                        booking.setId(bookingId);
                    }
                }
            }
            
            // For each room item, we need to assign an actual room_id
            for (BookingRoom br : rooms) {
                long actualRoomId = 0;
                // We stored roomTypeId temporarily in br.getRoomId() for the cart (this is a hack, or we should have a DTO)
                // Wait, it's better if `BookingRoom` has the actual `room_id`. 
                // Let's resolve the `room_id` using the roomTypeId which we pass in.
                // Assuming we pass `roomTypeId` in `roomId` field temporarily before inserting.
                long roomTypeId = br.getRoomId();
                
                try (PreparedStatement psFind = conn.prepareStatement(findAvailableRoomSql)) {
                    psFind.setLong(1, roomTypeId);
                    psFind.setDate(2, booking.getCheckOutDate());
                    psFind.setDate(3, booking.getCheckInDate());
                    try (ResultSet rsFind = psFind.executeQuery()) {
                        if (rsFind.next()) {
                            actualRoomId = rsFind.getLong(1);
                        } else {
                            // No room available
                            conn.rollback();
                            return false;
                        }
                    }
                }
                
                try (PreparedStatement psRoom = conn.prepareStatement(insertBookingRoomSql)) {
                    psRoom.setLong(1, bookingId);
                    psRoom.setLong(2, actualRoomId);
                    psRoom.setBigDecimal(3, br.getPricePerNight());
                    psRoom.setInt(4, br.getNumberOfNights());
                    psRoom.setBigDecimal(5, br.getSubtotal());
                    psRoom.executeUpdate();
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public List<Booking> findByCustomerId(long customerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY id DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Booking> findById(long id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public Optional<Booking> findByBookingCode(String bookingCode) {
        String sql = "SELECT * FROM bookings WHERE booking_code = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean updateStatus(long id, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getLong("id"));
        b.setBookingCode(rs.getString("booking_code"));
        b.setCustomerId(rs.getLong("customer_id") == 0 ? null : rs.getLong("customer_id"));
        b.setBookingSource(rs.getString("booking_source"));
        b.setCheckInDate(rs.getDate("check_in_date"));
        b.setCheckOutDate(rs.getDate("check_out_date"));
        b.setCheckInDatetime(rs.getTimestamp("check_in_datetime"));
        b.setCheckOutDatetime(rs.getTimestamp("check_out_datetime"));
        b.setTotalRoomAmount(rs.getBigDecimal("total_room_amount"));
        b.setTotalServiceAmount(rs.getBigDecimal("total_service_amount"));
        b.setTotalDamageAmount(rs.getBigDecimal("total_damage_amount"));
        b.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setStatus(rs.getString("status"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}
