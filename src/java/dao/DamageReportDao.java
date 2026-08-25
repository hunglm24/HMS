package dao;

import dto.DamageReportDto;
import util.DBConnectionUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DamageReportDao {

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        }
        return connection;
    }

    private static final String SELECT_BASE = """
            SELECT dr.id, dr.booking_id, dr.room_equipment_id, dr.damage_type,
                   dr.compensation_amount, dr.charge_status, dr.note,
                   dr.created_at, dr.updated_at,
                   b.booking_code,
                   cust.full_name AS customer_name,
                   r.id AS room_id, r.room_number, r.floor_number,
                   e.name AS equipment_name, e.default_compensation_price, e.is_maintainable,
                   ii.note AS inspection_item_note,
                   hk.full_name AS inspected_by_name
            FROM damage_reports dr
            JOIN bookings b ON b.id = dr.booking_id
            LEFT JOIN accounts cust ON cust.id = b.customer_id
            JOIN room_equipment re ON re.id = dr.room_equipment_id
            JOIN equipment e ON e.id = re.equipment_id
            JOIN rooms r ON r.id = re.room_id
            LEFT JOIN inspection_items ii ON ii.id = dr.inspection_item_id
            LEFT JOIN room_inspections ri ON ri.id = ii.inspection_id
            LEFT JOIN accounts hk ON hk.id = ri.inspected_by
            """;

    public List<DamageReportDto> findDamageReports(String keyword, String status, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND dr.charge_status = ? ");
            params.add(status.trim().toUpperCase());
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(b.booking_code) LIKE ? OR LOWER(r.room_number) LIKE ? OR LOWER(e.name) LIKE ? OR LOWER(COALESCE(dr.note, '')) LIKE ? OR LOWER(COALESCE(cust.full_name, '')) LIKE ?) ");
            String pat = "%" + keyword.toLowerCase().trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY CASE WHEN dr.charge_status = 'PENDING' THEN 0 ELSE 1 END, dr.id DESC LIMIT ? OFFSET ? ");
        params.add(limit);
        params.add(offset);

        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer num) {
                    ps.setInt(i + 1, num);
                } else if (p instanceof Long num) {
                    ps.setLong(i + 1, num);
                } else {
                    ps.setString(i + 1, String.valueOf(p));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<DamageReportDto> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapDto(rs));
                }
                return list;
            }
        }
    }

    public int countDamageReports(String keyword, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM damage_reports dr
                JOIN bookings b ON b.id = dr.booking_id
                LEFT JOIN accounts cust ON cust.id = b.customer_id
                JOIN room_equipment re ON re.id = dr.room_equipment_id
                JOIN equipment e ON e.id = re.equipment_id
                JOIN rooms r ON r.id = re.room_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND dr.charge_status = ? ");
            params.add(status.trim().toUpperCase());
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(b.booking_code) LIKE ? OR LOWER(r.room_number) LIKE ? OR LOWER(e.name) LIKE ? OR LOWER(COALESCE(dr.note, '')) LIKE ? OR LOWER(COALESCE(cust.full_name, '')) LIKE ?) ");
            String pat = "%" + keyword.toLowerCase().trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, String.valueOf(params.get(i)));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countPendingReports() throws SQLException {
        String sql = "SELECT COUNT(*) FROM damage_reports WHERE charge_status = 'PENDING'";
        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Optional<DamageReportDto> findById(long id) throws SQLException {
        String sql = SELECT_BASE + " WHERE dr.id = ?";
        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapDto(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Executes SQL Transaction to process a damage report (CHARGE or WAIVE).
     */
    public boolean processDamageReport(long reportId, String action, BigDecimal finalAmount, String note, Long processedBy) throws SQLException {
        String lockReportSql = """
                SELECT dr.id, dr.booking_id, dr.room_equipment_id, dr.damage_type, dr.charge_status,
                       b.booking_code, b.total_room_amount,
                       e.name AS equipment_name
                FROM damage_reports dr
                JOIN bookings b ON b.id = dr.booking_id
                JOIN room_equipment re ON re.id = dr.room_equipment_id
                JOIN equipment e ON e.id = re.equipment_id
                WHERE dr.id = ?
                FOR UPDATE
                """;

        try (Connection conn = requireConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                long bookingId;
                String bookingCode;
                BigDecimal roomAmount;
                String equipName;
                String currentStatus;

                try (PreparedStatement ps = conn.prepareStatement(lockReportSql)) {
                    ps.setLong(1, reportId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Không tìm thấy báo cáo hư hại ID #" + reportId);
                        }
                        bookingId = rs.getLong("booking_id");
                        bookingCode = rs.getString("booking_code");
                        roomAmount = rs.getBigDecimal("total_room_amount");
                        if (roomAmount == null) roomAmount = BigDecimal.ZERO;
                        equipName = rs.getString("equipment_name");
                        currentStatus = rs.getString("charge_status");
                    }
                }

                if ("PAID".equalsIgnoreCase(currentStatus)) {
                    throw new SQLException("Báo cáo này đã được thanh toán hoàn tất lúc check-out, không thể thay đổi.");
                }

                if ("WAIVE".equalsIgnoreCase(action)) {
                    // 1. Update damage_report
                    String updateDmg = "UPDATE damage_reports SET charge_status = 'WAIVED', compensation_amount = 0, note = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateDmg)) {
                        ps.setString(1, note);
                        ps.setLong(2, reportId);
                        ps.executeUpdate();
                    }

                    // 2. Remove from invoice_items if exists
                    String delItem = "DELETE FROM invoice_items WHERE damage_report_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(delItem)) {
                        ps.setLong(1, reportId);
                        ps.executeUpdate();
                    }

                    // 3. Recalculate invoice & booking
                    recalculateInvoiceAndBooking(conn, bookingId, bookingCode, roomAmount);

                } else if ("CHARGE".equalsIgnoreCase(action)) {
                    if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                        finalAmount = BigDecimal.ZERO;
                    }

                    // 1. Update damage_report
                    String updateDmg = "UPDATE damage_reports SET charge_status = 'CHARGED', compensation_amount = ?, note = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateDmg)) {
                        ps.setBigDecimal(1, finalAmount);
                        ps.setString(2, note);
                        ps.setLong(3, reportId);
                        ps.executeUpdate();
                    }

                    // 2. Ensure invoice exists for this booking
                    long invoiceId = ensureInvoice(conn, bookingId, bookingCode, roomAmount);

                    // 3. Upsert invoice_items
                    String itemDesc = (note != null && !note.isBlank())
                            ? note
                            : "Bồi thường hư hại: " + equipName;

                    String checkItemSql = "SELECT id FROM invoice_items WHERE invoice_id = ? AND damage_report_id = ?";
                    long existingItemId = 0;
                    try (PreparedStatement ps = conn.prepareStatement(checkItemSql)) {
                        ps.setLong(1, invoiceId);
                        ps.setLong(2, reportId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) existingItemId = rs.getLong("id");
                        }
                    }

                    if (existingItemId > 0) {
                        String updateItem = "UPDATE invoice_items SET item_type = 'DAMAGE', description = ?, quantity = 1, unit_price = ?, total_price = ? WHERE id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateItem)) {
                            ps.setString(1, itemDesc);
                            ps.setBigDecimal(2, finalAmount);
                            ps.setBigDecimal(3, finalAmount);
                            ps.setLong(4, existingItemId);
                            ps.executeUpdate();
                        }
                    } else {
                        String insertItem = "INSERT INTO invoice_items (invoice_id, damage_report_id, item_type, description, quantity, unit_price, total_price) VALUES (?, ?, 'DAMAGE', ?, 1, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                            ps.setLong(1, invoiceId);
                            ps.setLong(2, reportId);
                            ps.setString(3, itemDesc);
                            ps.setBigDecimal(4, finalAmount);
                            ps.setBigDecimal(5, finalAmount);
                            ps.executeUpdate();
                        }
                    }

                    // 4. Recalculate invoice & booking
                    recalculateInvoiceAndBooking(conn, bookingId, bookingCode, roomAmount);
                } else {
                    throw new IllegalArgumentException("Hành động không hợp lệ: " + action);
                }

                conn.commit();
                return true;
            } catch (SQLException | IllegalArgumentException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    private long ensureInvoice(Connection conn, long bookingId, String bookingCode, BigDecimal roomAmount) throws SQLException {
        String findSql = "SELECT id FROM invoices WHERE booking_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        String invCode = "INV-" + (bookingCode != null ? bookingCode : ("BK" + bookingId));
        String insertSql = """
                INSERT INTO invoices (invoice_code, booking_id, room_amount, service_amount, damage_amount, discount_amount, tax_amount, total_amount, status)
                VALUES (?, ?, ?, 0, 0, 0, 0, ?, 'UNPAID')
                """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invCode);
            ps.setLong(2, bookingId);
            ps.setBigDecimal(3, roomAmount);
            ps.setBigDecimal(4, roomAmount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("Không thể tạo hóa đơn cho booking #" + bookingId);
    }

    private void recalculateInvoiceAndBooking(Connection conn, long bookingId, String bookingCode, BigDecimal roomAmount) throws SQLException {
        long invoiceId = ensureInvoice(conn, bookingId, bookingCode, roomAmount);

        // Calculate damage sum from invoice_items
        String sumSql = "SELECT COALESCE(SUM(total_price), 0) FROM invoice_items WHERE invoice_id = ? AND item_type = 'DAMAGE'";
        BigDecimal totalDamage = BigDecimal.ZERO;
        try (PreparedStatement ps = conn.prepareStatement(sumSql)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalDamage = rs.getBigDecimal(1);
            }
        }

        // Update invoice
        String updateInvoiceSql = """
                UPDATE invoices
                SET damage_amount = ?,
                    total_amount = room_amount + COALESCE(service_amount, 0) + ? - COALESCE(discount_amount, 0) + COALESCE(tax_amount, 0),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(updateInvoiceSql)) {
            ps.setBigDecimal(1, totalDamage);
            ps.setBigDecimal(2, totalDamage);
            ps.setLong(3, invoiceId);
            ps.executeUpdate();
        }

        // Update bookings table totalDamageAmount and totalAmount
        String updateBookingSql = """
                UPDATE bookings
                SET total_damage_amount = ?,
                    total_amount = COALESCE(total_room_amount, 0) + COALESCE(total_service_amount, 0) + ? - COALESCE(discount_amount, 0),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
            ps.setBigDecimal(1, totalDamage);
            ps.setBigDecimal(2, totalDamage);
            ps.setLong(3, bookingId);
            ps.executeUpdate();
        }
    }

    private DamageReportDto mapDto(ResultSet rs) throws SQLException {
        DamageReportDto dto = new DamageReportDto();
        dto.setId(rs.getLong("id"));
        dto.setBookingId(rs.getLong("booking_id"));
        dto.setBookingCode(rs.getString("booking_code"));
        dto.setCustomerName(rs.getString("customer_name"));
        dto.setRoomId(rs.getLong("room_id"));
        dto.setRoomNumber(rs.getString("room_number"));
        dto.setFloorNumber(rs.getInt("floor_number"));
        dto.setRoomEquipmentId(rs.getLong("room_equipment_id"));
        dto.setEquipmentName(rs.getString("equipment_name"));
        
        BigDecimal defaultPrice = rs.getBigDecimal("default_compensation_price");
        if (defaultPrice == null) defaultPrice = BigDecimal.ZERO;
        dto.setDefaultPrice(defaultPrice);

        boolean isMaintainable = rs.getBoolean("is_maintainable");
        dto.setMaintainable(isMaintainable);

        String damageType = rs.getString("damage_type");
        dto.setDamageType(damageType);

        // Compute suggested amount:
        BigDecimal suggested = BigDecimal.ZERO;
        if ("MISSING".equalsIgnoreCase(damageType)) {
            suggested = defaultPrice;
        } else if ("DAMAGED".equalsIgnoreCase(damageType)) {
            if (isMaintainable) {
                suggested = defaultPrice.multiply(new BigDecimal("0.30")).setScale(-3, RoundingMode.HALF_UP);
            } else {
                suggested = BigDecimal.ZERO;
            }
        }
        dto.setSuggestedAmount(suggested);

        BigDecimal compAmount = rs.getBigDecimal("compensation_amount");
        if (compAmount == null) compAmount = suggested;
        dto.setCompensationAmount(compAmount);

        dto.setChargeStatus(rs.getString("charge_status"));
        dto.setNote(rs.getString("note"));
        dto.setHousekeeperNote(rs.getString("inspection_item_note"));
        dto.setInspectedByName(rs.getString("inspected_by_name"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));

        return dto;
    }
}
