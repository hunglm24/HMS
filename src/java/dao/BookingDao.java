package dao;

import model.CheckInBookingSummary;
import model.RoomType;
import util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BookingDao {

    public List<model.Booking> findBookingsByCustomerId(long customerId, String bookingCode, String status, String fromDate, String toDate, int limit, int offset) throws SQLException {
        List<model.Booking> bookings = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM bookings WHERE customer_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(customerId);

        if (bookingCode != null && !bookingCode.trim().isEmpty()) {
            sql.append(" AND booking_code LIKE ?");
            params.add("%" + bookingCode.trim() + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("UPCOMING".equalsIgnoreCase(status.trim())) {
                sql.append(" AND status IN ('PENDING_PAYMENT', 'CONFIRMED')");
            } else {
                sql.append(" AND status = ?");
                params.add(status.trim());
            }
        }
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append(" AND check_in_date >= ?");
            params.add(java.sql.Date.valueOf(fromDate));
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append(" AND check_in_date <= ?");
            params.add(java.sql.Date.valueOf(toDate));
        }
        
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (java.sql.Connection conn = DBConnectionUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Booking b = new model.Booking();
                    b.setId(rs.getLong("id"));
                    b.setBookingCode(rs.getString("booking_code"));
                    b.setCheckInDate(rs.getDate("check_in_date"));
                    b.setCheckOutDate(rs.getDate("check_out_date"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setStatus(rs.getString("status"));
                    b.setCancellationReason(rs.getString("cancellation_reason"));
                    b.setCancelledAt(rs.getTimestamp("cancelled_at"));
                    b.setCreatedAt(rs.getTimestamp("created_at"));
                    bookings.add(b);
                }
            }
        }
        return bookings;
    }
    
    public int countBookingsByCustomerId(long customerId, String bookingCode, String status, String fromDate, String toDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM bookings WHERE customer_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(customerId);

        if (bookingCode != null && !bookingCode.trim().isEmpty()) {
            sql.append(" AND booking_code LIKE ?");
            params.add("%" + bookingCode.trim() + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("UPCOMING".equalsIgnoreCase(status.trim())) {
                sql.append(" AND status IN ('PENDING_PAYMENT', 'CONFIRMED')");
            } else {
                sql.append(" AND status = ?");
                params.add(status.trim());
            }
        }
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append(" AND check_in_date >= ?");
            params.add(java.sql.Date.valueOf(fromDate));
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append(" AND check_in_date <= ?");
            params.add(java.sql.Date.valueOf(toDate));
        }
        
        try (java.sql.Connection conn = DBConnectionUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public Optional<model.Booking> findById(long id) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (java.sql.Connection conn = DBConnectionUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    model.Booking b = new model.Booking();
                    b.setId(rs.getLong("id"));
                    b.setBookingCode(rs.getString("booking_code"));
                    b.setBookingSource(rs.getString("booking_source"));
                    b.setCustomerId(rs.getLong("customer_id"));
                    b.setCheckInDate(rs.getDate("check_in_date"));
                    b.setCheckOutDate(rs.getDate("check_out_date"));
                    b.setCheckInDatetime(rs.getTimestamp("check_in_datetime"));
                    b.setCheckOutDatetime(rs.getTimestamp("check_out_datetime"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setStatus(rs.getString("status"));
                    b.setCancellationReason(rs.getString("cancellation_reason"));
                    b.setCancelledAt(rs.getTimestamp("cancelled_at"));
                    b.setCreatedAt(rs.getTimestamp("created_at"));
                    b.setCreatedBy(rs.getLong("created_by"));
                    return Optional.of(b);
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateBookingStatus(long bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (java.sql.Connection conn = DBConnectionUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelBooking(long bookingId, String reason) throws SQLException {
        String sql = "UPDATE bookings SET status = 'CANCELLED', cancellation_reason = ?, cancelled_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (java.sql.Connection conn = DBConnectionUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setLong(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

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
                   b.cancellation_reason,
                   b.cancelled_at,
                   b.total_amount,
                   b.note,
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
        QueryParts query = filters(null, null, null, null, bookingId, null, null, null);
        String sql = BASE_SELECT + query.whereClause()
                + " GROUP BY b.id, b.booking_code, b.customer_id, guest_name, phone, email, b.note,"
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
                                                           int offset, int limit,
                                                           String fromDate, String toDate, String source) throws SQLException {
        QueryParts query = filters(keyword, bookingStatus, roomTypeId, scope, null, fromDate, toDate, source);
        String sql = BASE_SELECT + query.whereClause()
                + " GROUP BY b.id, b.booking_code, b.customer_id, guest_name, phone, email, b.note,"
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
                                    String scope, String fromDate, String toDate, String source) throws SQLException {
        QueryParts query = filters(keyword, bookingStatus, roomTypeId, scope, null, fromDate, toDate, source);
        String sql = BASE_COUNT + query.whereClause();
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, query.parameters());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private QueryParts filters(String keyword, String bookingStatus, Integer roomTypeId,
                               String scope, Integer bookingId,
                               String fromDate, String toDate, String source) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        
        // Remove the default IN condition because the user wants ALL bookings including CANCELLED to be filtered
        // if no specific status is requested. We'll only add it if we aren't filtering by CANCELLED.
        if (bookingStatus == null || bookingStatus.isEmpty()) {
            // Default behavior if we want to show everything in Dashboard
        }

        if (bookingId != null && bookingId > 0) {
            conditions.add("b.id = ?");
            parameters.add(bookingId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
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
                case "checkout_today" -> conditions.add("DATE(b.check_out_date) = CURDATE()");
                case "checkout_upcoming" -> conditions.add("DATE(b.check_out_date) > CURDATE()");
                case "checkout_overdue" -> conditions.add("DATE(b.check_out_date) < CURDATE() AND b.status IN ('CHECKED_IN', 'CHECKOUT_PENDING')");
                case "checkout_pending" -> conditions.add("b.status = 'CHECKOUT_PENDING'");
                default -> {
                }
            }
        }
        
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            conditions.add("DATE(b.check_in_date) >= ?");
            parameters.add(java.sql.Date.valueOf(fromDate));
        }
        
        if (toDate != null && !toDate.trim().isEmpty()) {
            conditions.add("DATE(b.check_in_date) <= ?");
            parameters.add(java.sql.Date.valueOf(toDate));
        }
        
        if (source != null && !source.trim().isEmpty()) {
            conditions.add("b.booking_source = ?");
            parameters.add(source);
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
            case "CheckoutPending" -> "CHECKOUT_PENDING";
            case "CheckedOut" -> "CHECKED_OUT";
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
            if (parameter instanceof Integer) {
                statement.setInt(index++, (Integer) parameter);
            } else if (parameter instanceof java.sql.Date) {
                statement.setDate(index++, (java.sql.Date) parameter);
            } else if (parameter instanceof java.sql.Timestamp) {
                statement.setTimestamp(index++, (java.sql.Timestamp) parameter);
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
        booking.setNote(resultSet.getString("note"));
        booking.setCreatedAt(resultSet.getTimestamp("created_at"));
        booking.setRoomCount(resultSet.getInt("room_count"));
        booking.setRoomTypes(resultSet.getString("room_types"));
        booking.setRoomNumbers(resultSet.getString("room_numbers"));
        return booking;
    }

    public List<Map<String, Object>> getInspectionSummary(long bookingId) throws SQLException {
        String sql = """
                SELECT br.id AS booking_room_id, r.id AS room_id, r.room_number, rt.name AS room_type_name,
                       ri.id AS inspection_id, ri.status AS inspection_status, ri.note AS inspection_note,
                       ht.id AS task_id, ht.status AS task_status,
                       COALESCE(a.full_name, 'Chưa chỉ định') AS staff_name
                FROM booking_rooms br
                JOIN rooms r ON br.room_id = r.id
                JOIN room_types rt ON r.room_type_id = rt.id
                LEFT JOIN room_inspections ri ON ri.booking_room_id = br.id
                LEFT JOIN housekeeping_tasks ht ON ht.id = ri.housekeeping_task_id
                LEFT JOIN accounts a ON ht.assigned_to = a.id
                WHERE br.booking_id = ?
                ORDER BY r.room_number ASC
                """;
        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("bookingRoomId", rs.getLong("booking_room_id"));
                    map.put("roomId", rs.getLong("room_id"));
                    map.put("roomNumber", rs.getString("room_number"));
                    map.put("roomTypeName", rs.getString("room_type_name"));
                    map.put("inspectionId", rs.getObject("inspection_id"));
                    map.put("inspectionStatus", rs.getString("inspection_status"));
                    map.put("inspectionNote", rs.getString("inspection_note"));
                    map.put("taskId", rs.getObject("task_id"));
                    map.put("taskStatus", rs.getString("task_status"));
                    map.put("staffName", rs.getString("staff_name"));
                    list.add(map);
                }
                return list;
            }
        }
    }

    public List<Map<String, Object>> getDamageReports(long bookingId) throws SQLException {
        String sql = """
                SELECT dr.id, dr.damage_type, dr.compensation_amount, dr.charge_status, dr.note,
                       e.name AS equipment_name, r.room_number
                FROM damage_reports dr
                JOIN room_equipment re ON dr.room_equipment_id = re.id
                JOIN equipment e ON re.equipment_id = e.id
                JOIN rooms r ON re.room_id = r.id
                WHERE dr.booking_id = ?
                ORDER BY dr.id ASC
                """;
        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getLong("id"));
                    map.put("damageType", rs.getString("damage_type"));
                    map.put("compensationAmount", rs.getBigDecimal("compensation_amount"));
                    map.put("chargeStatus", rs.getString("charge_status"));
                    map.put("note", rs.getString("note"));
                    map.put("equipmentName", rs.getString("equipment_name"));
                    map.put("roomNumber", rs.getString("room_number"));
                    list.add(map);
                }
                return list;
            }
        }
    }

    public BigDecimal getTotalDamageAmount(long bookingId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(compensation_amount), 0) FROM damage_reports WHERE booking_id = ? AND charge_status != 'WAIVED'";
        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
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
