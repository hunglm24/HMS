package dao;

import model.HotelPolicy;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HotelPolicyDao {
    private static volatile boolean tableChecked = false;

    private void ensureTableExists(Connection conn) {
        if (tableChecked) return;
        String sql = """
            CREATE TABLE IF NOT EXISTS hotel_policies (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              title VARCHAR(255) NOT NULL,
              category VARCHAR(100) DEFAULT NULL,
              content TEXT DEFAULT NULL,
              status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;
        try (java.sql.Statement st = conn.createStatement()) {
            st.execute(sql);
            tableChecked = true;
        } catch (Exception ignored) {
        }
    }

    public List<HotelPolicy> findAll() throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            ensureTableExists(conn);
            String sql = "SELECT * FROM hotel_policies ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<HotelPolicy> policies = new ArrayList<>();
                while (rs.next()) {
                    policies.add(mapRow(rs));
                }
                return policies;
            }
        }
    }

    public Optional<HotelPolicy> findById(long id) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM hotel_policies WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<HotelPolicy> findActiveCancellationPolicy() throws SQLException {
        String sql = """
                SELECT *
                FROM hotel_policies
                WHERE status = 'ACTIVE'
                  AND (LOWER(category) LIKE ? OR LOWER(title) LIKE ?
                       OR LOWER(category) LIKE ? OR LOWER(title) LIKE ?)
                ORDER BY updated_at DESC, created_at DESC
                LIMIT 1
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%hủy%");
            ps.setString(2, "%hủy%");
            ps.setString(3, "%huy%");
            ps.setString(4, "%huy%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public void save(HotelPolicy policy) throws SQLException {
        if (policy.getId() == null) {
            insert(policy);
        } else {
            update(policy);
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM hotel_policies WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE hotel_policies SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private void insert(HotelPolicy policy) throws SQLException {
        String sql = "INSERT INTO hotel_policies (title, category, content, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, policy);
            ps.executeUpdate();
        }
    }

    private void update(HotelPolicy policy) throws SQLException {
        String sql = "UPDATE hotel_policies SET title = ?, category = ?, content = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, policy);
            ps.setLong(5, policy.getId());
            ps.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement ps, HotelPolicy policy) throws SQLException {
        ps.setString(1, policy.getTitle());
        ps.setString(2, policy.getCategory());
        ps.setString(3, policy.getContent());
        ps.setString(4, policy.getStatus());
    }

    private HotelPolicy mapRow(ResultSet rs) throws SQLException {
        HotelPolicy policy = new HotelPolicy();
        policy.setId(rs.getLong("id"));
        policy.setTitle(rs.getString("title"));
        policy.setCategory(rs.getString("category"));
        policy.setContent(rs.getString("content"));
        policy.setStatus(rs.getString("status"));
        policy.setCreatedAt(rs.getTimestamp("created_at"));
        policy.setUpdatedAt(rs.getTimestamp("updated_at"));
        return policy;
    }
}
