package dao;

import model.HotelConfig;
import util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Optional;

public class HotelConfigDao {
    public Optional<HotelConfig> findById(long id) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            return findById(conn, id);
        }
    }

    public Optional<HotelConfig> findCurrent() throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            return findCurrent(conn);
        }
    }

    public HotelConfig loadForEdit() throws SQLException {
        return findCurrent().orElseGet(this::createDefaultConfig);
    }

    public HotelConfig save(HotelConfig config) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            Optional<HotelConfig> existingById = config.getId() == null ? Optional.empty() : findById(conn, config.getId());
            if (existingById.isPresent()) {
                config.setId(existingById.get().getId());
                update(conn, config);
                return config;
            }

            Optional<HotelConfig> current = findCurrent(conn);
            if (current.isPresent()) {
                config.setId(current.get().getId());
                update(conn, config);
            } else {
                insert(conn, config);
            }
            return config;
        }
    }

    public HotelConfig createDefaultConfig() {
        HotelConfig config = new HotelConfig();
        config.setHotelName("HMS Hotel");
        config.setAddress("123 Hotel Street, Ho Chi Minh City");
        config.setPhone("0900 000 000");
        config.setEmail("noreply@hms.local");
        config.setCheckInTime(Time.valueOf("14:00:00"));
        config.setCheckOutTime(Time.valueOf("12:00:00"));
        config.setSameDayRefundRate(new BigDecimal("20"));
        config.setBeforeDayRefundRate(new BigDecimal("50"));
        config.setTaxRate(new BigDecimal("10"));
        config.setServiceFeeRate(new BigDecimal("5"));
        return config;
    }

    private Optional<HotelConfig> findById(Connection conn, long id) throws SQLException {
        String sql = "SELECT * FROM hotel_config WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    private Optional<HotelConfig> findCurrent(Connection conn) throws SQLException {
        String sql = "SELECT * FROM hotel_config ORDER BY id ASC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    private void insert(Connection conn, HotelConfig config) throws SQLException {
        String sql = """
                INSERT INTO hotel_config (
                    hotel_name, address, phone, email,
                    check_in_time, check_out_time,
                    same_day_refund_rate, before_day_refund_rate,
                    tax_rate, service_fee_rate,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, config);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    config.setId(rs.getLong(1));
                }
            }
        }
    }

    private void update(Connection conn, HotelConfig config) throws SQLException {
        String sql = """
                UPDATE hotel_config
                SET hotel_name = ?, address = ?, phone = ?, email = ?,
                    check_in_time = ?, check_out_time = ?,
                    same_day_refund_rate = ?, before_day_refund_rate = ?,
                    tax_rate = ?, service_fee_rate = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, config);
            ps.setLong(11, config.getId());
            ps.executeUpdate();
        }
    }

    private void fillStatement(PreparedStatement ps, HotelConfig config) throws SQLException {
        ps.setString(1, config.getHotelName());
        ps.setString(2, config.getAddress());
        ps.setString(3, config.getPhone());
        ps.setString(4, config.getEmail());
        ps.setTime(5, config.getCheckInTime());
        ps.setTime(6, config.getCheckOutTime());
        ps.setBigDecimal(7, config.getSameDayRefundRate());
        ps.setBigDecimal(8, config.getBeforeDayRefundRate());
        ps.setBigDecimal(9, config.getTaxRate());
        ps.setBigDecimal(10, config.getServiceFeeRate());
    }

    private HotelConfig mapRow(ResultSet rs) throws SQLException {
        HotelConfig config = new HotelConfig();
        config.setId(rs.getLong("id"));
        config.setHotelName(rs.getString("hotel_name"));
        config.setAddress(rs.getString("address"));
        config.setPhone(rs.getString("phone"));
        config.setEmail(rs.getString("email"));
        config.setCheckInTime(rs.getTime("check_in_time"));
        config.setCheckOutTime(rs.getTime("check_out_time"));
        config.setSameDayRefundRate(rs.getBigDecimal("same_day_refund_rate"));
        config.setBeforeDayRefundRate(rs.getBigDecimal("before_day_refund_rate"));
        config.setTaxRate(rs.getBigDecimal("tax_rate"));
        config.setServiceFeeRate(rs.getBigDecimal("service_fee_rate"));
        config.setCreatedAt(rs.getTimestamp("created_at"));
        config.setUpdatedAt(rs.getTimestamp("updated_at"));
        return config;
    }
}
