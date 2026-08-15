package dao;

import model.RoomType;
import util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomTypeDao {

    public List<RoomType> findAll() {
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT * FROM room_types ORDER BY id DESC";
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

    public Optional<RoomType> findById(long id) {
        String sql = "SELECT * FROM room_types WHERE id = ?";
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

    public boolean insert(RoomType roomType) {
        String sql = "INSERT INTO room_types (name, description, capacity, base_price, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setInt(3, roomType.getCapacity());
            ps.setBigDecimal(4, roomType.getBasePrice());
            ps.setString(5, roomType.getStatus());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
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

    public boolean update(RoomType roomType) {
        String sql = "UPDATE room_types SET name = ?, description = ?, capacity = ?, base_price = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    public boolean delete(long id) {
        String sql = "DELETE FROM room_types WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private RoomType mapRow(ResultSet rs) throws SQLException {
        RoomType rt = new RoomType();
        rt.setId(rs.getLong("id"));
        rt.setName(rs.getString("name"));
        rt.setDescription(rs.getString("description"));
        rt.setCapacity(rs.getInt("capacity"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setStatus(rs.getString("status"));
        rt.setCreatedAt(rs.getTimestamp("created_at"));
        rt.setUpdatedAt(rs.getTimestamp("updated_at"));
        return rt;
    }
}
