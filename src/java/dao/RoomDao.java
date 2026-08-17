package dao;

import model.Room;
import util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDao {

    // Lấy danh sách tất cả các phòng KÈM THEO tên loại phòng tương ứng
    // Hàm này rất hữu ích khi muốn hiển thị tên loại phòng (ví dụ: Standard) thay vì ID loại phòng (ví dụ: 1) ra giao diện
    public List<Room> findAllWithRoomTypeName() {
        List<Room> list = new ArrayList<>();
        // JOIN bảng rooms (viết tắt là r) và bảng room_types (viết tắt là rt)
        // Điều kiện nối là: r.room_type_id = rt.id
        String sql = "SELECT r.*, rt.name as room_type_name FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id ORDER BY r.room_number ASC";
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Room r = mapRow(rs); // Chuyển dữ liệu của bảng rooms thành đối tượng Room
                // Lấy thêm cái tên loại phòng từ câu lệnh SQL và gán vào thuộc tính phụ (transient)
                r.setRoomTypeName(rs.getString("room_type_name")); 
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Lấy danh sách các phòng (chỉ dữ liệu thuần của bảng rooms)
    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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

    // Thêm mới một phòng vật lý
    public boolean insert(Room room) {
        String sql = "INSERT INTO rooms (room_type_id, room_number, floor_number, status, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());
            
            // Xử lý tầng: Nếu người dùng không nhập tầng (null) thì set giá trị NULL trong SQL
            if (room.getFloorNumber() != null) {
                ps.setInt(3, room.getFloorNumber());
            } else {
                ps.setNull(3, Types.INTEGER); // Gán giá trị NULL của kiểu INTEGER
            }
            
            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        room.setId(rs.getLong(1)); // Cập nhật ID tự động sinh
                    }
                }
                return true;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import model.Room;
import util.DBConnectionUtil;

public class RoomDAO {

    // UC10, UC11: Tìm kiếm phòng
    public List<Room> searchRooms(Date checkIn, Date checkOut, int guests, int roomTypeId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.* FROM room r JOIN room_type rt ON r.room_type_id = rt.room_type_id " +
                     "WHERE rt.max_occupancy >= ? AND r.status = 'Available' " +
                     "AND r.room_id NOT IN (" +
                     "    SELECT room_id FROM booking_room br JOIN booking b ON br.booking_id = b.booking_id " +
                     "    WHERE (b.check_in_date < ? AND b.check_out_date > ?) AND br.status != 'Cancelled'" +
                     ")";
        if (roomTypeId > 0) {
            sql += " AND r.room_type_id = ?";
        }
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, guests);
            ps.setDate(2, new java.sql.Date(checkOut.getTime()));
            ps.setDate(3, new java.sql.Date(checkIn.getTime()));
            
            if (roomTypeId > 0) {
                ps.setInt(4, roomTypeId);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    room.setRoomId(rs.getInt("room_id"));
                    room.setRoomNumber(rs.getString("room_number"));
                    room.setFloor(rs.getInt("floor"));
                    room.setRoomTypeId(rs.getInt("room_type_id"));
                    room.setStatus(rs.getString("status"));
                    room.setViewType(rs.getString("view_type"));
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin phòng vật lý
    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_type_id = ?, room_number = ?, floor_number = ?, status = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, room.getRoomTypeId());
            ps.setString(2, room.getRoomNumber());
            
            // Tương tự hàm insert, nếu tầng rỗng thì set NULL
            if (room.getFloorNumber() != null) {
                ps.setInt(3, room.getFloorNumber());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            ps.setString(4, room.getStatus());
            ps.setString(5, room.getDescription());
            ps.setLong(6, room.getId()); // ID của phòng cần cập nhật
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy danh sách các phòng trống trong khoảng thời gian cụ thể (Walk-in)
    public List<Room> findAvailableRoomsForWalkIn(Date checkIn, Date checkOut, Long roomTypeId, Integer guests) {
        List<Room> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.*, rt.name as room_type_name, rt.capacity FROM rooms r ");
        sql.append("JOIN room_types rt ON r.room_type_id = rt.id ");
        sql.append("WHERE r.status != 'MAINTENANCE' ");
        sql.append("AND rt.status = 'ACTIVE' ");
        
        if (roomTypeId != null) {
            sql.append("AND r.room_type_id = ? ");
        }
        
        // Không cho phép đặt lố số khách mặc định
        if (guests != null) {
            sql.append("AND rt.capacity >= ? ");
        }
        
        sql.append("AND r.id NOT IN ( ");
        sql.append("  SELECT br.room_id FROM booking_rooms br ");
        sql.append("  JOIN bookings b ON br.booking_id = b.id ");
        sql.append("  WHERE b.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') ");
        sql.append("  AND (b.check_in_date < ? AND b.check_out_date > ?) ");
        sql.append(") ORDER BY r.room_number ASC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (roomTypeId != null) {
                ps.setLong(paramIndex++, roomTypeId);
            }
            if (guests != null) {
                ps.setInt(paramIndex++, guests);
            }
            
            ps.setDate(paramIndex++, checkOut);
            ps.setDate(paramIndex++, checkIn);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Room r = mapRow(rs);
                    r.setRoomTypeName(rs.getString("room_type_name"));
                    // Dùng description tạm để pass capacity cho frontend hiển thị
                    r.setDescription("Sức chứa: " + rs.getInt("capacity") + " khách");
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Xóa một phòng (nhớ cẩn thận vì có thể phòng này đang có người đặt)
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
        Room r = new Room();
        r.setId(rs.getLong("id"));
        r.setRoomTypeId(rs.getLong("room_type_id"));
        r.setRoomNumber(rs.getString("room_number"));
        
        // Cần kiểm tra kĩ vì getInt("floor_number") có thể trả về 0 nếu trong database là NULL
        int floor = rs.getInt("floor_number");
        // rs.wasNull() kiểm tra xem cái cột đọc ra ngay trước đó (tức là floor_number) có phải bị NULL hay không
        r.setFloorNumber(rs.wasNull() ? null : floor); 
        
        r.setStatus(rs.getString("status"));
        r.setDescription(rs.getString("description"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
        return rooms;
    }

    // UC27: Xem sơ đồ phòng
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM room";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setFloor(rs.getInt("floor"));
                room.setRoomTypeId(rs.getInt("room_type_id"));
                room.setStatus(rs.getString("status"));
                room.setViewType(rs.getString("view_type"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }
}
