package dao;

import model.BookingRoom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BookingRoomDao {

    public Optional<BookingRoom> findByBookingIdAndRoomId(Connection conn, long bookingId, long roomId)
            throws SQLException {
        String sql = """
                SELECT id, booking_id, room_id, price_per_night, number_of_nights, subtotal, created_at
                FROM booking_rooms
                WHERE booking_id = ? AND room_id = ?
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setLong(2, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateRoomId(Connection conn, long bookingRoomId, long newRoomId) throws SQLException {
        String sql = "UPDATE booking_rooms SET room_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newRoomId);
            ps.setLong(2, bookingRoomId);
            return ps.executeUpdate() > 0;
        }
    }

    private BookingRoom mapRow(ResultSet rs) throws SQLException {
        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setId(rs.getLong("id"));
        bookingRoom.setBookingId(rs.getLong("booking_id"));
        bookingRoom.setRoomId(rs.getLong("room_id"));
        bookingRoom.setPricePerNight(rs.getBigDecimal("price_per_night"));
        bookingRoom.setNumberOfNights(rs.getInt("number_of_nights"));
        bookingRoom.setSubtotal(rs.getBigDecimal("subtotal"));
        bookingRoom.setCreatedAt(rs.getTimestamp("created_at"));
        return bookingRoom;
    }
}
