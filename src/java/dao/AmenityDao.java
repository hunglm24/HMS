package dao;

import model.Amenity;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    // Load every amenity row for the management list.
    public List<Amenity> findAll() {
        List<Amenity> amenities = new ArrayList<>();
        String sql = "SELECT * FROM amenity ORDER BY id ASC";

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

    // Find one amenity row by its primary key.
    public Optional<Amenity> findById(long id) {
        String sql = "SELECT * FROM amenity WHERE id = ?";

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

    // Insert a new amenity row using the provided connection.
    public long insert(Connection conn, Amenity amenity) throws SQLException {
        String sql = """
                INSERT INTO amenity (name, description, icon, status)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, amenity.getName());
            ps.setString(2, amenity.getDescription());
            ps.setString(3, amenity.getIcon());
            ps.setString(4, amenity.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        amenity.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        }

        throw new SQLException("Cannot create amenity.");
    }

    // Update an existing amenity row using the provided connection.
    public int update(Connection conn, Amenity amenity) throws SQLException {
        String sql = """
                UPDATE amenity
                   SET name = ?, description = ?, icon = ?, status = ?
                 WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, amenity.getName());
            ps.setString(2, amenity.getDescription());
            ps.setString(3, amenity.getIcon());
            ps.setString(4, amenity.getStatus());
            ps.setLong(5, amenity.getId());
            return ps.executeUpdate();
        }
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
