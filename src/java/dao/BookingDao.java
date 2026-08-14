package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Booking;
import util.DBConnectionUtil;

public class BookingDAO {

    // UC16, UC17, UC26: Tạo booking
    public int createBooking(Booking booking) {
        String sql = "INSERT INTO booking (guest_id, booking_type, check_in_date, check_out_date, status, total_amount, deposit_amount) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getGuestId());
            ps.setString(2, booking.getBookingType());
            ps.setDate(3, new java.sql.Date(booking.getCheckInDate().getTime()));
            ps.setDate(4, new java.sql.Date(booking.getCheckOutDate().getTime()));
            ps.setString(5, booking.getStatus());
            ps.setDouble(6, booking.getTotalAmount());
            ps.setDouble(7, booking.getDepositAmount());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Return new booking_id
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // UC20: Lịch sử đặt phòng của Khách
    public List<Booking> getBookingsByGuestId(int guestId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE guest_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setBookingId(rs.getInt("booking_id"));
                    b.setGuestId(rs.getInt("guest_id"));
                    b.setStatus(rs.getString("status"));
                    b.setTotalAmount(rs.getDouble("total_amount"));
                    // ... set other fields
                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // UC23, UC24, UC25: Quản lý booking của Lễ tân
    public void updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE booking SET status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
