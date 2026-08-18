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
    public List<HotelPolicy> findAll() throws SQLException {
        String sql = "SELECT * FROM hotel_policies ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<HotelPolicy> policies = new ArrayList<>();
            while (rs.next()) {
                policies.add(mapRow(rs));
            }
            return policies;
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
