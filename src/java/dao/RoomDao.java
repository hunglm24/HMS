package dao;

import model.Room;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDao {

    public List<Room> findAvailablePhysicalRooms(java.time.LocalDate checkIn, java.time.LocalDate checkOut, Long roomTypeId) {
        return findAvailablePhysicalRooms(checkIn, checkOut, roomTypeId, null);
    }

    public List<Room> findAvailablePhysicalRooms(java.time.LocalDate checkIn, java.time.LocalDate checkOut, Long roomTypeId, Long excludeBookingId) {
        List<Room> rooms = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT r.*, rt.name AS room_type_name, rt.base_price AS base_price
            FROM rooms r
            JOIN room_types rt ON r.room_type_id = rt.id
            WHERE r.status = 'AVAILABLE'
              AND rt.status = 'ACTIVE'
              AND NOT EXISTS (
                  SELECT 1
                  FROM booking_rooms br
                  JOIN bookings b ON br.booking_id = b.id
                  WHERE br.room_id = r.id
                    AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CHECKED_IN', 'CANCELLATION_PENDING')
                    AND b.check_in_date < ? AND b.check_out_date > ?
                    %s
              )
            """);

        String excludeClause = excludeBookingId != null && excludeBookingId > 0 ? " AND b.id <> ?" : "";
        sql = new StringBuilder(String.format(sql.toString(), excludeClause));

        if (roomTypeId != null && roomTypeId > 0) {
            sql.append(" AND r.room_type_id = ?");
        }
        
        sql.append(" ORDER BY r.room_number ASC");

        // --- BẮT ĐẦU IN LOG RA CONSOLE ĐỂ BẢO VỆ ĐỒ ÁN ---
        System.out.println("==================================================");
        System.out.println("[DEBUG-HMS] ĐANG TÌM PHÒNG TRỐNG CHO LỄ TÂN (RoomDao)");
        System.out.println("[DEBUG-HMS] Check-in Mới (Vào): " + checkIn);
        System.out.println("[DEBUG-HMS] Check-out Mới (Ra): " + checkOut);
        System.out.println("[DEBUG-HMS] CÂU SQL (CHỨA NOT EXISTS) SẮP CHẠY:");
        System.out.println(sql.toString());
        System.out.println("==================================================");
        // --- KẾT THÚC IN LOG ---

        try (Connection conn = DBConnectionUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            ps.setDate(1, java.sql.Date.valueOf(checkOut));
            ps.setDate(2, java.sql.Date.valueOf(checkIn));

            int paramIndex = 3;
            if (excludeBookingId != null && excludeBookingId > 0) {
                ps.setLong(paramIndex++, excludeBookingId);
            }

            if (roomTypeId != null && roomTypeId > 0) {
                ps.setLong(paramIndex, roomTypeId);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Room room = mapRow(rs);
                    room.setRoomTypeName(rs.getString("room_type_name"));
                    room.setRoomTypeBasePrice(rs.getBigDecimal("base_price"));
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Room> findAllWithRoomTypeName() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, rt.name AS room_type_name "
                + "FROM rooms r "
                + "JOIN room_types rt ON r.room_type_id = rt.id "
                + "ORDER BY r.room_number ASC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room room = mapRow(rs);
                room.setRoomTypeName(rs.getString("room_type_name"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Room> findAllWithRoomTypeNameAndBookingInfo() {
        List<Room> rooms = new ArrayList<>();
        String sql = """
                SELECT r.*,
                       rt.name AS room_type_name,
                       b.id AS current_booking_id,
                       b.booking_code AS current_booking_code,
                       COALESCE(bg.full_name, a.full_name, '') AS current_guest_name,
                       b.status AS current_booking_status
                FROM rooms r
                JOIN room_types rt ON r.room_type_id = rt.id
                LEFT JOIN (
                    SELECT br1.room_id, br1.booking_id
                    FROM booking_rooms br1
                    JOIN bookings b1 ON b1.id = br1.booking_id
                    WHERE b1.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKOUT_PENDING')
                      AND br1.id = (
                          SELECT MAX(br2.id)
                          FROM booking_rooms br2
                          JOIN bookings b2 ON b2.id = br2.booking_id
                          WHERE br2.room_id = br1.room_id
                            AND b2.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKOUT_PENDING')
                      )
                ) active_br ON active_br.room_id = r.id
                LEFT JOIN bookings b ON b.id = active_br.booking_id
                LEFT JOIN booking_guests bg ON bg.booking_id = b.id AND bg.is_primary_guest = TRUE
                LEFT JOIN accounts a ON a.id = b.customer_id
                ORDER BY r.room_number ASC
                """;

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room room = mapRow(rs);
                room.setRoomTypeName(rs.getString("room_type_name"));
                Long currentBookingId = nullableLong(rs, "current_booking_id");
                room.setCurrentBookingId(currentBookingId);
                room.setCurrentBookingCode(rs.getString("current_booking_code"));
                room.setCurrentGuestName(rs.getString("current_guest_name"));
                room.setCurrentBookingStatus(rs.getString("current_booking_status"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number ASC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public Optional<Room> findById(long id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";

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

    public Optional<Room> findById(Connection conn, long id) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean insert(Room room) {
        String sql = "INSERT INTO rooms (room_type_id, room_number, floor_number, status, description) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());

            // Nếu người dùng không nhập tầng thì lưu NULL vào database
            if (room.getFloorNumber() != null) {
                ps.setInt(3, room.getFloorNumber());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        // Cập nhật ID tự sinh cho đối tượng Room
                        room.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(Connection conn, Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_type_id, room_number, floor_number, status, description) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());
            if (room.getFloorNumber() != null) {
                ps.setInt(3, room.getFloorNumber());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        room.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_type_id = ?, room_number = ?, floor_number = ?, status = ?, description = ? "
                + "WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());

            // Tương tự insert, nếu tầng rỗng thì lưu NULL
            if (room.getFloorNumber() != null) {
                ps.setInt(3, room.getFloorNumber());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());
            ps.setLong(6, room.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Connection conn, Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_type_id = ?, room_number = ?, floor_number = ?, status = ?, description = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());
            if (room.getFloorNumber() != null) {
                ps.setInt(3, room.getFloorNumber());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());
            ps.setLong(6, room.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Connection conn, long roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM rooms WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomTypeId(rs.getLong("room_type_id"));
        room.setRoomNumber(rs.getString("room_number"));

        int floorNumber = rs.getInt("floor_number");
        room.setFloorNumber(rs.wasNull() ? null : floorNumber);

        room.setStatus(rs.getString("status"));
        room.setDescription(rs.getString("description"));
        room.setCreatedAt(rs.getTimestamp("created_at"));
        room.setUpdatedAt(rs.getTimestamp("updated_at"));
        return room;
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    public int getMaxFloor() {
        String sql = "SELECT COALESCE(MAX(floor_number), 3) FROM rooms WHERE status != 'INACTIVE'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return max > 0 ? max : 3;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 3;
    }

    public List<Integer> getDistinctFloors() {
        List<Integer> floors = new ArrayList<>();
        String sql = "SELECT DISTINCT floor_number FROM rooms WHERE floor_number IS NOT NULL AND floor_number > 0 ORDER BY floor_number ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                floors.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return floors.isEmpty() ? List.of(1, 2, 3) : floors;
    }
}
