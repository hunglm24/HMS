package dao;

import model.Booking;
import model.CheckInBookingSummary;
import model.RoomType;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDao {
    private static final String BASE_SELECT = """
            SELECT b.booking_id,
                   CONCAT('BK', LPAD(b.booking_id, 6, '0')) AS booking_code,
                   b.guest_id,
                   g.full_name AS guest_name,
                   g.phone,
                   g.email,
                   b.booking_type,
                   b.check_in_date,
                   b.check_out_date,
                   b.status,
                   b.total_amount,
                   b.deposit_amount,
                   b.created_at,
                   COUNT(DISTINCT br.booking_room_id) AS room_count,
                   GROUP_CONCAT(DISTINCT rt.type_name ORDER BY rt.type_name SEPARATOR ', ') AS room_types,
                   GROUP_CONCAT(DISTINCT r.room_number ORDER BY r.room_number SEPARATOR ', ') AS room_numbers
            FROM booking b
            JOIN guest g ON g.guest_id = b.guest_id
            LEFT JOIN booking_room br ON br.booking_id = b.booking_id
            LEFT JOIN room r ON r.room_id = br.room_id
            LEFT JOIN room_type rt ON rt.room_type_id = r.room_type_id
            """;
    private static final String BASE_COUNT = """
            SELECT COUNT(DISTINCT b.booking_id)
            FROM booking b
            JOIN guest g ON g.guest_id = b.guest_id
            LEFT JOIN booking_room br ON br.booking_id = b.booking_id
            LEFT JOIN room r ON r.room_id = br.room_id
            LEFT JOIN room_type rt ON rt.room_type_id = r.room_type_id
            """;

    public List<RoomType> findRoomTypes() throws SQLException {
        String sql = """
                SELECT room_type_id, type_name, description, base_price, max_occupancy, image_url
                FROM room_type
                ORDER BY type_name
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<RoomType> roomTypes = new ArrayList<>();
            while (resultSet.next()) {
                RoomType roomType = new RoomType();
                roomType.setRoomTypeId(resultSet.getInt("room_type_id"));
                roomType.setTypeName(resultSet.getString("type_name"));
                roomType.setDescription(resultSet.getString("description"));
                roomType.setBasePrice(resultSet.getDouble("base_price"));
                roomType.setMaxOccupancy(resultSet.getInt("max_occupancy"));
                roomType.setImageUrl(resultSet.getString("image_url"));
                roomTypes.add(roomType);
            }
            return roomTypes;
        }
    }

    public Optional<CheckInBookingSummary> findCheckInBookingById(int bookingId) throws SQLException {
        QueryParts query = filters(null, null, null, null, bookingId);
        String sql = BASE_SELECT + query.whereClause() + " GROUP BY "
                + "b.booking_id, g.guest_id, g.full_name, g.phone, g.email, b.booking_type, "
                + "b.check_in_date, b.check_out_date, b.status, b.total_amount, b.deposit_amount, b.created_at"
                + " LIMIT 1";
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, query.parameters());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapCheckInSummary(resultSet)) : Optional.empty();
            }
        }
    }

    public List<CheckInBookingSummary> findCheckInBookings(String keyword, String bookingStatus,
                                                           Integer roomTypeId, String scope,
                                                           String sortColumn, String sortDirection,
                                                           int offset, int limit) throws SQLException {
        QueryParts query = filters(keyword, bookingStatus, roomTypeId, scope, null);
        String sql = BASE_SELECT + query.whereClause()
                + " GROUP BY b.booking_id, g.guest_id, g.full_name, g.phone, g.email, b.booking_type,"
                + " b.check_in_date, b.check_out_date, b.status, b.total_amount, b.deposit_amount, b.created_at"
                + " ORDER BY " + sortColumn + " " + sortDirection + ", b.booking_id DESC"
                + " LIMIT ? OFFSET ?";
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = bind(statement, query.parameters());
            statement.setInt(index++, limit);
            statement.setInt(index, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CheckInBookingSummary> bookings = new ArrayList<>();
                while (resultSet.next()) {
                    bookings.add(mapCheckInSummary(resultSet));
                }
                return bookings;
            }
        }
    }

    public int countCheckInBookings(String keyword, String bookingStatus, Integer roomTypeId,
                                    String scope) throws SQLException {
        QueryParts query = filters(keyword, bookingStatus, roomTypeId, scope, null);
        String sql = BASE_COUNT + query.whereClause();
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, query.parameters());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private QueryParts filters(String keyword, String bookingStatus, Integer roomTypeId,
                               String scope, Integer bookingId) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        conditions.add("b.status IN ('Pending', 'Confirmed', 'CheckedIn')");
        if (bookingId != null && bookingId > 0) {
            conditions.add("b.booking_id = ?");
            parameters.add(bookingId);
        }
        if (keyword != null) {
            conditions.add("""
                    (
                        LOWER(g.full_name) LIKE ?
                        OR LOWER(g.phone) LIKE ?
                        OR LOWER(g.email) LIKE ?
                        OR LOWER(CONCAT('bk', LPAD(b.booking_id, 6, '0'))) LIKE ?
                        OR CAST(b.booking_id AS CHAR) LIKE ?
                    )
                    """);
            String pattern = "%" + keyword.toLowerCase() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (bookingStatus != null) {
            conditions.add("b.status = ?");
            parameters.add(bookingStatus);
        }
        if (roomTypeId != null) {
            conditions.add("EXISTS (SELECT 1 FROM booking_room br2 JOIN room r2 ON r2.room_id = br2.room_id WHERE br2.booking_id = b.booking_id AND r2.room_type_id = ?)");
            parameters.add(roomTypeId);
        }
        if (scope != null) {
            switch (scope) {
                case "today" -> conditions.add("DATE(b.check_in_date) = CURDATE()");
                case "upcoming" -> conditions.add("DATE(b.check_in_date) > CURDATE()");
                case "overdue" -> conditions.add("DATE(b.check_in_date) < CURDATE() AND b.status IN ('Pending', 'Confirmed')");
                default -> {
                }
            }
        }
        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return new QueryParts(whereClause, parameters);
    }

    private int bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        int index = 1;
        for (Object parameter : parameters) {
            if (parameter instanceof Integer value) {
                statement.setInt(index++, value);
            } else {
                statement.setString(index++, String.valueOf(parameter));
            }
        }
        return index;
    }

    private CheckInBookingSummary mapCheckInSummary(ResultSet resultSet) throws SQLException {
        CheckInBookingSummary booking = new CheckInBookingSummary();
        booking.setBookingId(resultSet.getInt("booking_id"));
        booking.setBookingCode(resultSet.getString("booking_code"));
        booking.setGuestId(resultSet.getInt("guest_id"));
        booking.setGuestName(resultSet.getString("guest_name"));
        booking.setPhone(resultSet.getString("phone"));
        booking.setEmail(resultSet.getString("email"));
        booking.setBookingType(resultSet.getString("booking_type"));
        booking.setCheckInDate(resultSet.getTimestamp("check_in_date"));
        booking.setCheckOutDate(resultSet.getTimestamp("check_out_date"));
        booking.setStatus(resultSet.getString("status"));
        booking.setTotalAmount(resultSet.getDouble("total_amount"));
        booking.setDepositAmount(resultSet.getDouble("deposit_amount"));
        booking.setCreatedAt(resultSet.getTimestamp("created_at"));
        booking.setRoomCount(resultSet.getInt("room_count"));
        booking.setRoomTypes(resultSet.getString("room_types"));
        booking.setRoomNumbers(resultSet.getString("room_numbers"));
        return booking;
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) {
            throw new SQLException("KhÃ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u");
        }
        return connection;
    }

    private record QueryParts(String whereClause, List<Object> parameters) {
    }
}
