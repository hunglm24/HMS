package dao;

import model.HotelPolicy;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HotelPolicyDao {
    public List<HotelPolicy> findAll() throws SQLException {
        String sql = "SELECT * FROM hotel_policies ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<HotelPolicy> policies = new ArrayList<>();
            // Read every row and map it into the domain model.
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

    public List<HotelPolicy> findHotelPolicyHistory() throws SQLException {
        List<HotelPolicy> policies = findAll();
        List<HotelPolicy> hotelPolicies = new ArrayList<>();
        for (HotelPolicy policy : policies) {
            // Keep only records that belong to the shared hotel-policy flow.
            if (isHotelPolicyRecord(policy)) {
                hotelPolicies.add(policy);
            }
        }
        return hotelPolicies;
    }

    public Optional<HotelPolicy> findLatestHotelPolicy() throws SQLException {
        List<HotelPolicy> policies = findHotelPolicyHistory();
        HotelPolicy firstMatch = null;
        for (HotelPolicy policy : policies) {
            // Prefer the newest active row so the guest page mirrors the manager view.
            if ("ACTIVE".equalsIgnoreCase(policy.getStatus())) {
                return Optional.of(policy);
            }
            if (firstMatch == null) {
                firstMatch = policy;
            }
        }
        return Optional.ofNullable(firstMatch);
    }

    public Optional<HotelPolicy> findActiveCancellationPolicy() throws SQLException {
        // Match the active policy by category/title keywords so the public flow can reuse it.
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

    private boolean isHotelPolicyRecord(HotelPolicy policy) {
        String haystack = normalize(policy.getTitle()) + " " + normalize(policy.getCategory()) + " " + normalize(policy.getContent());
        // Regex removes accents so keyword matching works with or without Vietnamese diacritics.
        return haystack.contains("noi quy")
                || haystack.contains("quy dinh")
                || haystack.contains("hotel policy")
                || haystack.contains("guest policy")
                || haystack.contains("policy chung");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
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
