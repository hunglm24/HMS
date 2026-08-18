package dao;

import model.Equipment;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipmentDao {
    // Load every equipment row for the management list.
    public List<Equipment> findAll() {
        List<Equipment> equipments = new ArrayList<>();
        String sql = "SELECT * FROM equipment ORDER BY id ASC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                equipments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipments;
    }

    // Find one equipment row by its primary key.
    public Optional<Equipment> findById(long id) {
        String sql = "SELECT * FROM equipment WHERE id = ?";

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

    // Insert a new equipment row using the provided connection.
    public long insert(Connection conn, Equipment equipment) throws SQLException {
        String sql = """
                INSERT INTO equipment (name, description, image_url, default_compensation_price, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, equipment.getName());
            ps.setString(2, equipment.getDescription());
            ps.setString(3, equipment.getImageUrl());
            ps.setBigDecimal(4, equipment.getDefaultCompensationPrice());
            ps.setString(5, equipment.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        equipment.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        }

        throw new SQLException("Cannot create equipment.");
    }

    // Update an existing equipment row using the provided connection.
    public int update(Connection conn, Equipment equipment) throws SQLException {
        String sql = """
                UPDATE equipment
                   SET name = ?, description = ?, image_url = ?, default_compensation_price = ?, status = ?
                 WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, equipment.getName());
            ps.setString(2, equipment.getDescription());
            ps.setString(3, equipment.getImageUrl());
            ps.setBigDecimal(4, equipment.getDefaultCompensationPrice());
            ps.setString(5, equipment.getStatus());
            ps.setLong(6, equipment.getId());
            return ps.executeUpdate();
        }
    }

    // Convert one database row into an Equipment model.
    private Equipment mapRow(ResultSet rs) throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setId(rs.getLong("id"));
        equipment.setName(rs.getString("name"));
        equipment.setDescription(rs.getString("description"));
        equipment.setImageUrl(rs.getString("image_url"));
        equipment.setDefaultCompensationPrice(rs.getBigDecimal("default_compensation_price"));
        equipment.setStatus(rs.getString("status"));
        equipment.setCreatedAt(rs.getTimestamp("created_at"));
        equipment.setUpdatedAt(rs.getTimestamp("updated_at"));
        return equipment;
    }
}
