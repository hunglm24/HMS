package dao;

import model.RoomEquipment;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomEquipmentDao {

    public List<RoomEquipment> findByRoomId(long roomId) {
        List<RoomEquipment> roomEquipments = new ArrayList<>();
        String sql = """
                SELECT re.id,
                       re.room_id,
                       re.equipment_id,
                       re.quantity,
                       re.status,
                       re.note,
                       re.updated_by,
                       re.updated_at,
                       e.name AS equipment_name
                FROM room_equipment re
                JOIN equipment e ON e.id = re.equipment_id
                WHERE re.room_id = ?
                ORDER BY e.name ASC, re.id ASC
                """;

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roomEquipments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomEquipments;
    }

    public List<RoomEquipment> findByRoomId(Connection conn, long roomId) throws SQLException {
        List<RoomEquipment> roomEquipments = new ArrayList<>();
        String sql = """
                SELECT re.id,
                       re.room_id,
                       re.equipment_id,
                       re.quantity,
                       re.status,
                       re.note,
                       re.updated_by,
                       re.updated_at,
                       e.name AS equipment_name
                FROM room_equipment re
                JOIN equipment e ON e.id = re.equipment_id
                WHERE re.room_id = ?
                ORDER BY e.name ASC, re.id ASC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roomEquipments.add(mapRow(rs));
                }
            }
        }
        return roomEquipments;
    }

    public List<RoomEquipment> findByRoomIds(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }

        List<RoomEquipment> roomEquipments = new ArrayList<>();
        String placeholders = String.join(",", Collections.nCopies(roomIds.size(), "?"));
        String sql = """
                SELECT re.id,
                       re.room_id,
                       re.equipment_id,
                       re.quantity,
                       re.status,
                       re.note,
                       re.updated_by,
                       re.updated_at,
                       e.name AS equipment_name
                FROM room_equipment re
                JOIN equipment e ON e.id = re.equipment_id
                WHERE re.room_id IN (%s)
                ORDER BY re.room_id ASC, e.name ASC, re.id ASC
                """.formatted(placeholders);

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < roomIds.size(); i++) {
                ps.setLong(i + 1, roomIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roomEquipments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomEquipments;
    }

    public int deleteByRoomId(Connection conn, long roomId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM room_equipment WHERE room_id = ?")) {
            ps.setLong(1, roomId);
            return ps.executeUpdate();
        }
    }

    public int deleteByRoomId(long roomId) {
        String sql = "DELETE FROM room_equipment WHERE room_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long insert(Connection conn, RoomEquipment roomEquipment) throws SQLException {
        String sql = """
                INSERT INTO room_equipment (room_id, equipment_id, quantity, status, note, updated_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, roomEquipment.getRoomId());
            ps.setLong(2, roomEquipment.getEquipmentId());
            ps.setInt(3, roomEquipment.getQuantity());
            ps.setString(4, roomEquipment.getStatus());
            ps.setString(5, roomEquipment.getNote());
            if (roomEquipment.getUpdatedBy() != null) {
                ps.setLong(6, roomEquipment.getUpdatedBy());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        roomEquipment.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        }

        throw new SQLException("Cannot save room equipment.");
    }

    private RoomEquipment mapRow(ResultSet rs) throws SQLException {
        RoomEquipment roomEquipment = new RoomEquipment();
        roomEquipment.setId(rs.getLong("id"));
        roomEquipment.setRoomId(rs.getLong("room_id"));
        roomEquipment.setEquipmentId(rs.getLong("equipment_id"));
        roomEquipment.setQuantity(rs.getInt("quantity"));
        roomEquipment.setStatus(rs.getString("status"));
        roomEquipment.setNote(rs.getString("note"));
        long updatedBy = rs.getLong("updated_by");
        roomEquipment.setUpdatedBy(rs.wasNull() ? null : updatedBy);
        roomEquipment.setUpdatedAt(rs.getTimestamp("updated_at"));
        roomEquipment.setEquipmentName(rs.getString("equipment_name"));
        return roomEquipment;
    }
}
