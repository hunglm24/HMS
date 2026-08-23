package dao;

import model.Promotion;
import util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PromotionDao {
    public List<Promotion> findAll() throws SQLException {
        String sql = "SELECT * FROM promotions ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Promotion> promotions = new ArrayList<>();
            while (rs.next()) {
                promotions.add(mapRow(rs));
            }
            return promotions;
        }
    }

    public Optional<Promotion> findById(long id) throws SQLException {
        String sql = "SELECT * FROM promotions WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Promotion> findUsableByCode(String code, BigDecimal bookingAmount) throws SQLException {
        String sql = """
                SELECT *
                FROM promotions
                WHERE UPPER(code) = UPPER(?)
                  AND status = 'ACTIVE'
                  AND NOW() BETWEEN start_date AND end_date
                  AND min_booking_amount <= ?
                  AND (usage_limit IS NULL OR used_count < usage_limit)
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setBigDecimal(2, bookingAmount);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public void save(Promotion promotion) throws SQLException {
        if (promotion.getId() == null) {
            insert(promotion);
        } else {
            update(promotion);
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM promotions WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE promotions SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void incrementUsedCount(Connection conn, long promotionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE promotions SET used_count = used_count + 1 WHERE id = ?")) {
            ps.setLong(1, promotionId);
            ps.executeUpdate();
        }
    }

    private void insert(Promotion promotion) throws SQLException {
        String sql = """
                INSERT INTO promotions
                (code, name, description, discount_type, discount_value, max_discount_amount,
                 min_booking_amount, start_date, end_date, usage_limit, status, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, promotion, true);
            ps.executeUpdate();
        }
    }

    private void update(Promotion promotion) throws SQLException {
        String sql = """
                UPDATE promotions
                SET code = ?, name = ?, description = ?, discount_type = ?, discount_value = ?,
                    max_discount_amount = ?, min_booking_amount = ?, start_date = ?, end_date = ?,
                    usage_limit = ?, status = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, promotion, false);
            ps.setLong(12, promotion.getId());
            ps.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement ps, Promotion promotion, boolean includeCreatedBy) throws SQLException {
        ps.setString(1, promotion.getCode());
        ps.setString(2, promotion.getName());
        ps.setString(3, promotion.getDescription());
        ps.setString(4, promotion.getDiscountType());
        ps.setBigDecimal(5, promotion.getDiscountValue());
        ps.setBigDecimal(6, promotion.getMaxDiscountAmount());
        ps.setBigDecimal(7, promotion.getMinBookingAmount() == null ? BigDecimal.ZERO : promotion.getMinBookingAmount());
        ps.setTimestamp(8, promotion.getStartDate());
        ps.setTimestamp(9, promotion.getEndDate());
        if (promotion.getUsageLimit() == null) {
            ps.setNull(10, java.sql.Types.INTEGER);
        } else {
            ps.setInt(10, promotion.getUsageLimit());
        }
        ps.setString(11, promotion.getStatus());
        if (includeCreatedBy) {
            ps.setLong(12, promotion.getCreatedBy());
        }
    }

    private Promotion mapRow(ResultSet rs) throws SQLException {
        Promotion promotion = new Promotion();
        promotion.setId(rs.getLong("id"));
        promotion.setCode(rs.getString("code"));
        promotion.setName(rs.getString("name"));
        promotion.setDescription(rs.getString("description"));
        promotion.setDiscountType(rs.getString("discount_type"));
        promotion.setDiscountValue(rs.getBigDecimal("discount_value"));
        promotion.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        promotion.setMinBookingAmount(rs.getBigDecimal("min_booking_amount"));
        promotion.setStartDate(rs.getTimestamp("start_date"));
        promotion.setEndDate(rs.getTimestamp("end_date"));
        int usageLimit = rs.getInt("usage_limit");
        promotion.setUsageLimit(rs.wasNull() ? null : usageLimit);
        promotion.setUsedCount(rs.getInt("used_count"));
        promotion.setStatus(rs.getString("status"));
        promotion.setCreatedBy(rs.getLong("created_by"));
        promotion.setCreatedAt(rs.getTimestamp("created_at"));
        promotion.setUpdatedAt(rs.getTimestamp("updated_at"));
        return promotion;
    }
}
