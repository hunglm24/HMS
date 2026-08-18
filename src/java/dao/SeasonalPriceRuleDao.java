package dao;

import model.SeasonalPriceRule;
import util.DBConnectionUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeasonalPriceRuleDao {
    public List<SeasonalPriceRule> findAll() throws SQLException {
        String sql = """
                SELECT spr.*, rt.name AS room_type_name
                FROM seasonal_price_rules spr
                JOIN room_types rt ON rt.id = spr.room_type_id
                ORDER BY spr.start_date DESC, spr.id DESC
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<SeasonalPriceRule> rules = new ArrayList<>();
            while (rs.next()) {
                rules.add(mapRow(rs));
            }
            return rules;
        }
    }

    public Optional<SeasonalPriceRule> findById(long id) throws SQLException {
        String sql = "SELECT spr.*, rt.name AS room_type_name FROM seasonal_price_rules spr JOIN room_types rt ON rt.id = spr.room_type_id WHERE spr.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public BigDecimal calculateSubtotal(long roomTypeId, BigDecimal basePrice, java.time.LocalDate checkIn,
                                        java.time.LocalDate checkOut, int quantity) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        for (java.time.LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            BigDecimal nightlyPrice = findEffectiveNightlyPrice(roomTypeId, basePrice, date);
            total = total.add(nightlyPrice.multiply(BigDecimal.valueOf(quantity)));
        }
        return total.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal findEffectiveNightlyPrice(long roomTypeId, BigDecimal basePrice, java.time.LocalDate date) throws SQLException {
        String sql = """
                SELECT price_per_night, surcharge_percent
                FROM seasonal_price_rules
                WHERE room_type_id = ?
                  AND status = 'ACTIVE'
                  AND ? BETWEEN start_date AND end_date
                ORDER BY CASE rule_type WHEN 'HOLIDAY' THEN 0 ELSE 1 END, updated_at DESC, id DESC
                LIMIT 1
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return basePrice;
                }
                BigDecimal fixedPrice = rs.getBigDecimal("price_per_night");
                if (fixedPrice != null) {
                    return fixedPrice;
                }
                BigDecimal surchargePercent = rs.getBigDecimal("surcharge_percent");
                if (surchargePercent == null) {
                    return basePrice;
                }
                return basePrice.add(basePrice.multiply(surchargePercent).divide(BigDecimal.valueOf(100)));
            }
        }
    }

    public void save(SeasonalPriceRule rule) throws SQLException {
        if (rule.getId() == null) {
            insert(rule);
        } else {
            update(rule);
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM seasonal_price_rules WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void insert(SeasonalPriceRule rule) throws SQLException {
        String sql = """
                INSERT INTO seasonal_price_rules
                (room_type_id, rule_name, rule_type, start_date, end_date, price_per_night, surcharge_percent, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, rule);
            ps.executeUpdate();
        }
    }

    private void update(SeasonalPriceRule rule) throws SQLException {
        String sql = """
                UPDATE seasonal_price_rules
                SET room_type_id = ?, rule_name = ?, rule_type = ?, start_date = ?, end_date = ?,
                    price_per_night = ?, surcharge_percent = ?, status = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, rule);
            ps.setLong(9, rule.getId());
            ps.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement ps, SeasonalPriceRule rule) throws SQLException {
        ps.setLong(1, rule.getRoomTypeId());
        ps.setString(2, rule.getRuleName());
        ps.setString(3, rule.getRuleType());
        ps.setDate(4, rule.getStartDate());
        ps.setDate(5, rule.getEndDate());
        ps.setBigDecimal(6, rule.getPricePerNight());
        ps.setBigDecimal(7, rule.getSurchargePercent());
        ps.setString(8, rule.getStatus());
    }

    private SeasonalPriceRule mapRow(ResultSet rs) throws SQLException {
        SeasonalPriceRule rule = new SeasonalPriceRule();
        rule.setId(rs.getLong("id"));
        rule.setRoomTypeId(rs.getLong("room_type_id"));
        rule.setRoomTypeName(rs.getString("room_type_name"));
        rule.setRuleName(rs.getString("rule_name"));
        rule.setRuleType(rs.getString("rule_type"));
        rule.setStartDate(rs.getDate("start_date"));
        rule.setEndDate(rs.getDate("end_date"));
        rule.setPricePerNight(rs.getBigDecimal("price_per_night"));
        rule.setSurchargePercent(rs.getBigDecimal("surcharge_percent"));
        rule.setStatus(rs.getString("status"));
        rule.setCreatedAt(rs.getTimestamp("created_at"));
        rule.setUpdatedAt(rs.getTimestamp("updated_at"));
        return rule;
    }
}
