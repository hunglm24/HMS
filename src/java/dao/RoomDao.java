package dao;

import model.Room;
import util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDao {

    public List<Room> findAllWithRoomTypeName() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, rt.name as room_type_name FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id ORDER BY r.room_number ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room r = mapRow(rs);
                r.setRoomTypeName(rs.getString("room_type_name"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
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

    public boolean insert(Room room) {
        String sql = "INSERT INTO rooms (room_type_id, room_number, floor_number, status, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_type_id = ?, room_number = ?, floor_number = ?, status = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
        Room r = new Room();
        r.setId(rs.getLong("id"));
        r.setRoomTypeId(rs.getLong("room_type_id"));
        r.setRoomNumber(rs.getString("room_number"));
        
        int floor = rs.getInt("floor_number");
        r.setFloorNumber(rs.wasNull() ? null : floor);
        
        r.setStatus(rs.getString("status"));
        r.setDescription(rs.getString("description"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }
}
