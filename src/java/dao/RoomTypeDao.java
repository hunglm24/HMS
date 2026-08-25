<<<<<<< Updated upstream
=======
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
        String sql = "SELECT * FROM room_types ORDER BY id ASC";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomTypes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    // Load all room types with the total number of physical rooms for popularity sorting.
    public List<RoomType> findAllWithRoomCounts() {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = """
                SELECT rt.*,
                       (
                           SELECT COUNT(*)
                           FROM rooms r
                           WHERE r.room_type_id = rt.id
                       ) AS total_quantity
                FROM room_types rt
                ORDER BY rt.id ASC
                """;

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoomType roomType = mapRow(rs);
                roomType.setTotalQuantity(rs.getInt("total_quantity"));
                roomTypes.add(roomType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    public List<RoomType> findActive() {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM room_types WHERE status = 'ACTIVE' ORDER BY id ASC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomTypes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load active room types from database.", e);
        }
        return roomTypes;
    }

    // Load a limited number of active room types for homepage highlights.
    public List<RoomType> findActive(int limit) {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM room_types WHERE status = 'ACTIVE' ORDER BY id ASC LIMIT " + Math.max(limit, 0);

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomTypes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load limited active room types from database.", e);
        }
        return roomTypes;
    }

    /** Homepage highlights that have at least one physical room free today. */
    public List<RoomType> findFeaturedAvailable(int limit) {
        List<RoomType> roomTypes = new ArrayList<>();
        if (limit <= 0) return roomTypes;

        String sql = """
                SELECT rt.*,
                       COUNT(r.id) AS total_quantity,
                       SUM(CASE WHEN r.status = 'AVAILABLE'
                           AND NOT EXISTS (
                               SELECT 1
                               FROM booking_rooms br
                               JOIN bookings b ON b.id = br.booking_id
                               WHERE br.room_id = r.id
                                 AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CHECKED_IN', 'CANCELLATION_PENDING')
                                 AND b.check_in_date <= CURRENT_DATE
                                 AND b.check_out_date > CURRENT_DATE
                           ) THEN 1 ELSE 0 END) AS available_quantity
                FROM room_types rt
                JOIN rooms r ON r.room_type_id = rt.id
                WHERE rt.status = 'ACTIVE'
                GROUP BY rt.id
                HAVING available_quantity > 0
                ORDER BY available_quantity DESC, rt.base_price DESC, rt.id ASC
                LIMIT ?
                """;

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomType roomType = mapRow(rs);
                    roomType.setTotalQuantity(rs.getInt("total_quantity"));
                    roomType.setAvailableQuantity(rs.getInt("available_quantity"));
                    roomTypes.add(roomType);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load featured available room types.", e);
        }
        return roomTypes;
    }

    // Load distinct room type statuses from the database for form dropdowns.
    public List<String> findDistinctStatuses() {
        List<String> statuses = new ArrayList<>();
        String sql = "SELECT DISTINCT status FROM room_types WHERE status IS NOT NULL AND status <> '' ORDER BY status";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                statuses.add(rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    // Load distinct bed types from the database for form dropdowns.
    public List<String> findDistinctBedTypes() {
        List<String> bedTypes = new ArrayList<>();
        String sql = "SELECT DISTINCT bed_type FROM room_types WHERE bed_type IS NOT NULL AND TRIM(bed_type) <> '' ORDER BY bed_type";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bedTypes.add(rs.getString("bed_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bedTypes;
    }

    public List<RoomType> findAvailableRoomTypes(java.time.LocalDate checkIn, java.time.LocalDate checkOut, int guests, int numRooms, Double minPrice, Double maxPrice, String sort, Long roomTypeId, int limit, int offset) {
        List<RoomType> roomTypes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT rt.*,
                   (
                       SELECT COUNT(*)
                       FROM rooms r
                       WHERE r.room_type_id = rt.id
                         AND r.status = 'AVAILABLE'
                         AND NOT EXISTS (
                             SELECT 1
                             FROM booking_rooms br
                             JOIN bookings b ON br.booking_id = b.id
                             WHERE br.room_id = r.id
                               AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CHECKED_IN', 'CANCELLATION_PENDING')
                               AND b.check_in_date < ?
                               AND b.check_out_date > ?
                         )
                   ) AS availableQuantity,
                   (
                       SELECT COUNT(*)
                       FROM rooms r
                       WHERE r.room_type_id = rt.id
                         AND r.status = 'AVAILABLE'
                   ) AS totalActiveRooms
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
        
        sql.append(" LIMIT ? OFFSET ?");

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
            ps.setInt(paramIdx++, limit);
            ps.setInt(paramIdx++, offset);
            
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

    public int countAvailableRoomTypes(java.time.LocalDate checkIn, java.time.LocalDate checkOut, int guests, int numRooms, Double minPrice, Double maxPrice, Long roomTypeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) FROM (
                SELECT rt.id,
                       (
                           SELECT COUNT(*)
                           FROM rooms r
                           WHERE r.room_type_id = rt.id
                             AND r.status = 'AVAILABLE'
                             AND NOT EXISTS (
                                 SELECT 1
                                 FROM booking_rooms br
                                 JOIN bookings b ON br.booking_id = b.id
                                 WHERE br.room_id = r.id
                                   AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CHECKED_IN', 'CANCELLATION_PENDING')
                                   AND b.check_in_date < ?
                                   AND b.check_out_date > ?
                             )
                       ) AS availableQuantity
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
        
        sql.append(" HAVING availableQuantity >= ?) AS subquery");

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
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
        try (Connection conn = DBConnectionUtil.getConnection()) {
            return insert(conn, roomType) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Insert a room type using the provided connection so callers can manage transactions.
    public long insert(Connection conn, RoomType roomType) throws SQLException {
        String sql = "INSERT INTO room_types (name, description, image_url, size_m2, bed_type, capacity, base_price, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setString(3, roomType.getImageUrl());
            ps.setBigDecimal(4, roomType.getSizeM2());
            ps.setString(5, roomType.getBedType());
            ps.setInt(6, roomType.getCapacity());
            ps.setBigDecimal(7, roomType.getBasePrice());
            ps.setString(8, roomType.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        roomType.setId(rs.getLong(1));
                        return rs.getLong(1);
                    }
                }
            }
        }
        throw new SQLException("Cannot create room type");
    }

    // Update a room type using the provided connection so callers can manage transactions.
    public int update(Connection conn, RoomType roomType) throws SQLException {
        String sql = "UPDATE room_types SET name = ?, description = ?, image_url = ?, size_m2 = ?, bed_type = ?, capacity = ?, base_price = ?, status = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setString(3, roomType.getImageUrl());
            ps.setBigDecimal(4, roomType.getSizeM2());
            ps.setString(5, roomType.getBedType());
            ps.setInt(6, roomType.getCapacity());
            ps.setBigDecimal(7, roomType.getBasePrice());
            ps.setString(8, roomType.getStatus());
            ps.setLong(9, roomType.getId());
            return ps.executeUpdate();
        }
    }

    // Cập nhật thông tin loại phòng
    public boolean update(RoomType roomType) {
        String sql = "UPDATE room_types SET name = ?, description = ?, image_url = ?, size_m2 = ?, bed_type = ?, capacity = ?, base_price = ?, status = ? "
                + "WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setString(3, roomType.getImageUrl());
            ps.setBigDecimal(4, roomType.getSizeM2());
            ps.setString(5, roomType.getBedType());
            ps.setInt(6, roomType.getCapacity());
            ps.setBigDecimal(7, roomType.getBasePrice());
            ps.setString(8, roomType.getStatus());
            ps.setLong(9, roomType.getId());

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
        roomType.setImageUrl(rs.getString("image_url"));
        roomType.setSizeM2(rs.getBigDecimal("size_m2"));
        roomType.setBedType(rs.getString("bed_type"));
        roomType.setCapacity(rs.getInt("capacity"));
        roomType.setBasePrice(rs.getBigDecimal("base_price"));
        roomType.setStatus(rs.getString("status"));
        roomType.setCreatedAt(rs.getTimestamp("created_at"));
        roomType.setUpdatedAt(rs.getTimestamp("updated_at"));
        return roomType;
    }
}
>>>>>>> Stashed changes
