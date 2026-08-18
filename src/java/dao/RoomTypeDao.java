package dao;

import model.RoomType;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomTypeDao {

    // Lấy tất cả loại phòng từ database
    public List<RoomType> findAll() {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM room_types ORDER BY id DESC";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomTypes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    public List<RoomType> findAvailableRoomTypes(java.time.LocalDate checkIn, java.time.LocalDate checkOut, int guests, int numRooms, Double minPrice, Double maxPrice, String sort, Long roomTypeId) {
        List<RoomType> roomTypes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT rt.*, 
                   (SELECT COUNT(*) FROM rooms r WHERE r.room_type_id = rt.id AND r.status = 'AVAILABLE') AS totalActiveRooms,
                   (SELECT COUNT(*) FROM rooms r WHERE r.room_type_id = rt.id AND r.status = 'AVAILABLE')
                   - 
                   (SELECT COUNT(br.room_id)
                    FROM booking_rooms br
                    JOIN bookings b ON br.booking_id = b.id
                    JOIN rooms r ON br.room_id = r.id
                    WHERE r.room_type_id = rt.id
                      AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CHECKED_IN')
                      AND b.check_in_date < ? AND b.check_out_date > ?) AS availableQuantity
            FROM room_types rt
            WHERE (rt.capacity * ?) >= ? AND rt.status = 'ACTIVE'
            """);

        if (roomTypeId != null && roomTypeId > 0) {
            sql.append(" AND rt.id = ?");
        }
        if (minPrice != null) {
            sql.append(" AND rt.base_price >= ?");
        }
        if (maxPrice != null) {
            sql.append(" AND rt.base_price <= ?");
        }
        
        sql.append(" HAVING availableQuantity >= ?");
        
        if ("PRICE_ASC".equals(sort)) {
            sql.append(" ORDER BY rt.base_price ASC");
        } else if ("PRICE_DESC".equals(sort)) {
            sql.append(" ORDER BY rt.base_price DESC");
        }

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            ps.setDate(paramIdx++, java.sql.Date.valueOf(checkOut));
            ps.setDate(paramIdx++, java.sql.Date.valueOf(checkIn));
            ps.setInt(paramIdx++, numRooms);
            ps.setInt(paramIdx++, guests);
            
            if (roomTypeId != null && roomTypeId > 0) {
                ps.setLong(paramIdx++, roomTypeId);
            }
            if (minPrice != null) {
                ps.setDouble(paramIdx++, minPrice);
            }
            if (maxPrice != null) {
                ps.setDouble(paramIdx++, maxPrice);
            }
            
            ps.setInt(paramIdx++, numRooms);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomType rt = mapRow(rs);
                    rt.setAvailableQuantity(rs.getInt("availableQuantity"));
                    rt.setTotalQuantity(rs.getInt("totalActiveRooms"));
                    roomTypes.add(rt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    // Tìm một loại phòng theo ID
    public Optional<RoomType> findById(long id) {
        String sql = "SELECT * FROM room_types WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

    // Thêm mới một loại phòng vào database
    public boolean insert(RoomType roomType) {
        String sql = "INSERT INTO room_types (name, description, capacity, base_price, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setInt(3, roomType.getCapacity());
            ps.setBigDecimal(4, roomType.getBasePrice());
            ps.setString(5, roomType.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        // Cập nhật ID tự sinh cho đối tượng RoomType
                        roomType.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin loại phòng
    public boolean update(RoomType roomType) {
        String sql = "UPDATE room_types SET name = ?, description = ?, capacity = ?, base_price = ?, status = ? "
                + "WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setInt(3, roomType.getCapacity());
            ps.setBigDecimal(4, roomType.getBasePrice());
            ps.setString(5, roomType.getStatus());
            ps.setLong(6, roomType.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa một loại phòng
    public boolean delete(long id) {
        String sql = "DELETE FROM room_types WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Hàm phụ trợ chuyển đổi 1 dòng dữ liệu thành đối tượng RoomType
    private RoomType mapRow(ResultSet rs) throws SQLException {
        RoomType roomType = new RoomType();
        roomType.setId(rs.getLong("id"));
        roomType.setName(rs.getString("name"));
        roomType.setDescription(rs.getString("description"));
        roomType.setCapacity(rs.getInt("capacity"));
        roomType.setBasePrice(rs.getBigDecimal("base_price"));
        roomType.setStatus(rs.getString("status"));
        roomType.setCreatedAt(rs.getTimestamp("created_at"));
        roomType.setUpdatedAt(rs.getTimestamp("updated_at"));
        return roomType;
    }
}
