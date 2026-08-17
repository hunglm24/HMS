package dao;

import model.Booking;
import model.BookingRoom;
import model.CheckInBookingSummary;
import model.RoomType;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDao {

    // ==========================================
    // SECTION 1: Booking Management & Creation
    // ==========================================

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

    public List<Booking> findByCustomerIdWithFilters(long customerId, String status, String bookingCode, Date fromDate, Date toDate) {
        List<Booking> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM bookings WHERE customer_id = ? ");
        
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            sql.append(" AND status = ? ");
        }
        if (bookingCode != null && !bookingCode.isEmpty()) {
            sql.append(" AND booking_code LIKE ? ");
        }
        if (fromDate != null) {
            sql.append(" AND check_in_date >= ? ");
        }
        if (toDate != null) {
            sql.append(" AND check_in_date <= ? ");
        }
        sql.append(" ORDER BY id DESC");
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            ps.setLong(paramIndex++, customerId);
            
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                ps.setString(paramIndex++, status);
            }
            if (bookingCode != null && !bookingCode.isEmpty()) {
                ps.setString(paramIndex++, "%" + bookingCode + "%");
            }
            if (fromDate != null) {
                ps.setDate(paramIndex++, fromDate);
            }
            if (toDate != null) {
                ps.setDate(paramIndex++, toDate);
            }
            
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

    public List<Booking> searchBookingsForReception(String keyword, String status, String dateType, Date fromDate, Date toDate, String bookingSource, String paymentStatus) {
        List<Booking> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.*, a.full_name AS customer_name, a.phone AS customer_phone, a.email AS customer_email, ");
        sql.append(" (SELECT GROUP_CONCAT(r.room_number SEPARATOR ', ') FROM booking_rooms br JOIN rooms r ON br.room_id = r.id WHERE br.booking_id = b.id) AS room_numbers ");
        sql.append("FROM bookings b ");
        sql.append("LEFT JOIN accounts a ON b.customer_id = a.id ");
        sql.append("WHERE 1=1 ");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (b.booking_code LIKE ? OR a.full_name LIKE ? OR a.phone LIKE ? OR a.email LIKE ? OR b.id IN (SELECT br.booking_id FROM booking_rooms br JOIN rooms r ON br.room_id = r.id WHERE r.room_number LIKE ?)) ");
        }
        
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            sql.append(" AND b.status = ? ");
        }
        
        if (bookingSource != null && !bookingSource.isEmpty() && !bookingSource.equals("ALL")) {
            sql.append(" AND b.booking_source = ? ");
        }
        
        if (dateType != null && !dateType.isEmpty() && fromDate != null && toDate != null) {
            if (dateType.equals("CREATED")) {
                sql.append(" AND DATE(b.created_at) >= ? AND DATE(b.created_at) <= ? ");
            } else if (dateType.equals("CHECKIN")) {
                sql.append(" AND b.check_in_date >= ? AND b.check_in_date <= ? ");
            } else if (dateType.equals("CHECKOUT")) {
                sql.append(" AND b.check_out_date >= ? AND b.check_out_date <= ? ");
            } else if (dateType.equals("STAY")) {
                sql.append(" AND (b.check_in_date <= ? AND b.check_out_date >= ?) ");
            }
        }
        
        sql.append(" ORDER BY b.id DESC");
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
            }
            
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                ps.setString(paramIndex++, status);
            }
            
            if (bookingSource != null && !bookingSource.isEmpty() && !bookingSource.equals("ALL")) {
                ps.setString(paramIndex++, bookingSource);
            }
            
            if (dateType != null && !dateType.isEmpty() && fromDate != null && toDate != null) {
                ps.setDate(paramIndex++, fromDate);
                ps.setDate(paramIndex++, toDate);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapRow(rs);
                    b.setCustomerName(rs.getString("customer_name"));
                    b.setCustomerPhone(rs.getString("customer_phone"));
                    b.setCustomerEmail(rs.getString("customer_email"));
                    b.setRoomNumbers(rs.getString("room_numbers"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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

    // ==========================================
    // SECTION 2: Reception & Check-In Summary
    // ==========================================

    private static final String BASE_SELECT = """
            SELECT b.id AS booking_id,
                   b.booking_code,
                   b.customer_id,
                   COALESCE(bg.full_name, a.full_name, '') AS guest_name,
                   COALESCE(bg.phone, a.phone, '') AS phone,
                   COALESCE(a.email, '') AS email,
                   b.booking_source AS booking_type,
                   b.check_in_date,
                   b.check_out_date,
                   b.status,
                   b.total_amount,
                   COALESCE((
                       SELECT SUM(p.amount)
                       FROM payments p
                       WHERE p.booking_id = b.id
                         AND p.payment_type = 'DEPOSIT'
                         AND p.status = 'SUCCESS'
                   ), 0) AS deposit_amount,
                   b.created_at,
                   COUNT(DISTINCT br.id) AS room_count,
                   GROUP_CONCAT(DISTINCT rt.name ORDER BY rt.name SEPARATOR ', ') AS room_types,
                   GROUP_CONCAT(DISTINCT r.room_number ORDER BY r.room_number SEPARATOR ', ') AS room_numbers
            FROM bookings b
            LEFT JOIN booking_guests bg
                   ON bg.booking_id = b.id
                  AND bg.is_primary_guest = TRUE
            LEFT JOIN accounts a
                   ON a.id = b.customer_id
            LEFT JOIN booking_rooms br
                   ON br.booking_id = b.id
            LEFT JOIN rooms r
                   ON r.id = br.room_id
            LEFT JOIN room_types rt
                   ON rt.id = r.room_type_id
            """;

    private static final String BASE_COUNT = """
            SELECT COUNT(DISTINCT b.id)
            FROM bookings b
            LEFT JOIN booking_guests bg
                   ON bg.booking_id = b.id
                  AND bg.is_primary_guest = TRUE
            LEFT JOIN accounts a
                   ON a.id = b.customer_id
            LEFT JOIN booking_rooms br
                   ON br.booking_id = b.id
            LEFT JOIN rooms r
                   ON r.id = br.room_id
            LEFT JOIN room_types rt
                   ON rt.id = r.room_type_id
            """;

    public List<RoomType> findRoomTypes() throws SQLException {
        String sql = """
                SELECT id, name, description, capacity, base_price, status, created_at, updated_at
                FROM room_types
                ORDER BY name
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<RoomType> roomTypes = new ArrayList<>();
            while (resultSet.next()) {
                RoomType roomType = new RoomType();
                roomType.setId(resultSet.getLong("id"));
                roomType.setName(resultSet.getString("name"));
                roomType.setDescription(resultSet.getString("description"));
                roomType.setCapacity(resultSet.getInt("capacity"));
                roomType.setBasePrice(resultSet.getBigDecimal("base_price"));
                roomType.setStatus(resultSet.getString("status"));
                roomType.setCreatedAt(resultSet.getTimestamp("created_at"));
                roomType.setUpdatedAt(resultSet.getTimestamp("updated_at"));
                roomTypes.add(roomType);
            }
            return roomTypes;
        }
    }

    public Optional<CheckInBookingSummary> findCheckInBookingById(int bookingId) throws SQLException {
        QueryParts query = filters(null, null, null, null, bookingId);
        String sql = BASE_SELECT + query.whereClause()
                + " GROUP BY b.id, b.booking_code, b.customer_id, guest_name, phone, email,"
                + " b.booking_source, b.check_in_date, b.check_out_date, b.status, b.total_amount, b.created_at"
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
                + " GROUP BY b.id, b.booking_code, b.customer_id, guest_name, phone, email,"
                + " b.booking_source, b.check_in_date, b.check_out_date, b.status, b.total_amount, b.created_at"
                + " ORDER BY " + normalizeSortColumn(sortColumn) + " " + normalizeSortDirection(sortDirection)
                + ", b.id DESC"
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
        conditions.add("b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CHECKED_IN')");

        if (bookingId != null && bookingId > 0) {
            conditions.add("b.id = ?");
            parameters.add(bookingId);
        }

        if (keyword != null) {
            conditions.add("""
                    (
                        LOWER(COALESCE(bg.full_name, a.full_name, '')) LIKE ?
                        OR LOWER(COALESCE(bg.phone, a.phone, '')) LIKE ?
                        OR LOWER(COALESCE(a.email, '')) LIKE ?
                        OR LOWER(b.booking_code) LIKE ?
                        OR CAST(b.id AS CHAR) LIKE ?
                    )
                    """);
            String pattern = "%" + keyword.toLowerCase() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }

        String normalizedStatus = normalizeBookingStatus(bookingStatus);
        if (normalizedStatus != null) {
            conditions.add("b.status = ?");
            parameters.add(normalizedStatus);
        }

        if (roomTypeId != null) {
            conditions.add("""
                    EXISTS (
                        SELECT 1
                        FROM booking_rooms br2
                        JOIN rooms r2 ON r2.id = br2.room_id
                        WHERE br2.booking_id = b.id
                          AND r2.room_type_id = ?
                    )
                    """);
            parameters.add(roomTypeId);
        }

        if (scope != null) {
            switch (scope) {
                case "today" -> conditions.add("DATE(b.check_in_date) = CURDATE()");
                case "upcoming" -> conditions.add("DATE(b.check_in_date) > CURDATE()");
                case "overdue" -> conditions.add("DATE(b.check_in_date) < CURDATE() AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED')");
                default -> {
                }
            }
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return new QueryParts(whereClause, parameters);
    }

    private String normalizeBookingStatus(String bookingStatus) {
        if (bookingStatus == null || bookingStatus.isBlank()) {
            return null;
        }
        return switch (bookingStatus) {
            case "Pending" -> "PENDING_PAYMENT";
            case "Confirmed" -> "CONFIRMED";
            case "CheckedIn" -> "CHECKED_IN";
            case "Cancelled" -> "CANCELLED";
            default -> bookingStatus;
        };
    }

    private String normalizeSortColumn(String sortColumn) {
        if (sortColumn == null || sortColumn.isBlank()) {
            return "b.created_at";
        }
        return switch (sortColumn) {
            case "b.created_at" -> "b.created_at";
            case "b.check_in_date" -> "b.check_in_date";
            case "b.check_out_date" -> "b.check_out_date";
            case "g.full_name" -> "guest_name";
            case "b.status" -> "b.status";
            case "room_types" -> "room_types";
            default -> "b.created_at";
        };
    }

    private String normalizeSortDirection(String sortDirection) {
        return "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
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
        booking.setGuestId(resultSet.getInt("customer_id"));
        booking.setGuestName(resultSet.getString("guest_name"));
        booking.setPhone(resultSet.getString("phone"));
        booking.setEmail(resultSet.getString("email"));
        booking.setBookingType(resultSet.getString("booking_type"));
        booking.setCheckInDate(resultSet.getDate("check_in_date"));
        booking.setCheckOutDate(resultSet.getDate("check_out_date"));
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
            throw new SQLException("Could not connect to database");
        }
        return connection;
    }

    private record QueryParts(String whereClause, List<Object> parameters) {
    }
}