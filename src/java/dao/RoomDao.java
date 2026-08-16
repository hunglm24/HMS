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

    // Lấy danh sách tất cả các phòng kèm theo tên loại phòng tương ứng
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
                    WHERE b1.status IN ('CONFIRMED', 'CHECKED_IN')
                      AND br1.id = (
                          SELECT MAX(br2.id)
                          FROM booking_rooms br2
                          JOIN bookings b2 ON b2.id = br2.booking_id
                          WHERE br2.room_id = br1.room_id
                            AND b2.status IN ('CONFIRMED', 'CHECKED_IN')
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

    // Lấy danh sách các phòng, chỉ dữ liệu thuần của bảng rooms
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

    // Tìm một phòng cụ thể theo ID
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

    // Thêm mới một phòng vật lý
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

    // Cập nhật thông tin phòng vật lý
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

    public boolean updateStatus(Connection conn, long roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // Xóa một phòng
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

    // Hàm phụ trợ chuyển đổi ResultSet thành đối tượng Room
    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomTypeId(rs.getLong("room_type_id"));
        room.setRoomNumber(rs.getString("room_number"));

        // getInt có thể trả về 0 nếu giá trị trong database là NULL
        int floorNumber = rs.getInt("floor_number");
        // wasNull() kiểm tra cột vừa đọc có phải NULL hay không
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
}
