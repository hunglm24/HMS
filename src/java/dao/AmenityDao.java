package dao;

import model.Amenity;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AmenityDao {
    // Load all active amenities for room type configuration.
    public List<Amenity> findActiveAmenities() {
        List<Amenity> amenities = new ArrayList<>();
        String sql = "SELECT * FROM amenity WHERE status = 'ACTIVE' ORDER BY name";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                amenities.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return amenities;
    }

    // Convert one DB row into an Amenity model.
    private Amenity mapRow(ResultSet rs) throws SQLException {
        Amenity amenity = new Amenity();
        amenity.setId(rs.getLong("id"));
        amenity.setName(rs.getString("name"));
        amenity.setDescription(rs.getString("description"));
        amenity.setIcon(rs.getString("icon"));
        amenity.setStatus(rs.getString("status"));
        amenity.setCreatedAt(rs.getTimestamp("created_at"));
        amenity.setUpdatedAt(rs.getTimestamp("updated_at"));
        return amenity;
    }
}
