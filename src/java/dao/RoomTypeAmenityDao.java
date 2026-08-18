package dao;

import model.Amenity;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeAmenityDao {
    // Replace all amenity links for a room type in one transaction.
    public void replaceRoomTypeAmenities(Connection conn, long roomTypeId, List<Long> amenityIds) throws SQLException {
        deleteByRoomTypeId(conn, roomTypeId);

        if (amenityIds == null || amenityIds.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO room_type_amenity (room_type_id, amenity_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long amenityId : amenityIds) {
                if (amenityId == null || amenityId < 1L) {
                    continue;
                }
                ps.setLong(1, roomTypeId);
                ps.setLong(2, amenityId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Remove all amenity links for a room type.
    public void deleteByRoomTypeId(Connection conn, long roomTypeId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM room_type_amenity WHERE room_type_id = ?")) {
            ps.setLong(1, roomTypeId);
            ps.executeUpdate();
        }
    }

    // Load all amenities attached to a specific room type.
    public List<Amenity> findAmenitiesByRoomTypeId(long roomTypeId) {
        List<Amenity> amenities = new ArrayList<>();
        String sql = """
                SELECT a.id, a.name, a.description, a.icon, a.status, a.created_at, a.updated_at
                FROM room_type_amenity rta
                JOIN amenity a ON a.id = rta.amenity_id
                WHERE rta.room_type_id = ?
                ORDER BY a.name
                """;

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Amenity amenity = new Amenity();
                    amenity.setId(rs.getLong("id"));
                    amenity.setName(rs.getString("name"));
                    amenity.setDescription(rs.getString("description"));
                    amenity.setIcon(rs.getString("icon"));
                    amenity.setStatus(rs.getString("status"));
                    amenity.setCreatedAt(rs.getTimestamp("created_at"));
                    amenity.setUpdatedAt(rs.getTimestamp("updated_at"));
                    amenities.add(amenity);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return amenities;
    }
}
