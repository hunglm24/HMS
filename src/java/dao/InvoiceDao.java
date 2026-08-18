package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Invoice;
import util.DBConnectionUtil;

public class InvoiceDao {

    public Invoice findByBookingId(long bookingId) {
        String sql = "SELECT * FROM invoices WHERE booking_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = new Invoice();
                    invoice.setId(rs.getLong("id"));
                    invoice.setInvoiceCode(rs.getString("invoice_code"));
                    invoice.setBookingId(rs.getLong("booking_id"));
                    invoice.setRoomAmount(rs.getBigDecimal("room_amount"));
                    invoice.setServiceAmount(rs.getBigDecimal("service_amount"));
                    invoice.setDamageAmount(rs.getBigDecimal("damage_amount"));
                    invoice.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    invoice.setTaxAmount(rs.getBigDecimal("tax_amount"));
                    invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                    invoice.setStatus(rs.getString("status"));
                    invoice.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return invoice;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoices (invoice_code, booking_id, room_amount, service_amount, damage_amount, discount_amount, tax_amount, total_amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invoice.getInvoiceCode());
            ps.setLong(2, invoice.getBookingId());
            ps.setBigDecimal(3, invoice.getRoomAmount());
            ps.setBigDecimal(4, invoice.getServiceAmount());
            ps.setBigDecimal(5, invoice.getDamageAmount());
            ps.setBigDecimal(6, invoice.getDiscountAmount());
            ps.setBigDecimal(7, invoice.getTaxAmount());
            ps.setBigDecimal(8, invoice.getTotalAmount());
            ps.setString(9, invoice.getStatus());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoice.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addDamageAmount(long bookingId, BigDecimal amount) {
        String sql = "UPDATE invoices " +
                     "SET damage_amount = COALESCE(damage_amount, 0) + ?, " +
                     "    total_amount = COALESCE(total_amount, 0) + ?, " +
                     "    status = 'PARTIALLY_PAID', " +
                     "    updated_at = CURRENT_TIMESTAMP " +
                     "WHERE booking_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setBigDecimal(2, amount);
            ps.setLong(3, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(long bookingId, String status) {
        String sql = "UPDATE invoices SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE booking_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public java.util.List<dto.InvoiceListDto> searchInvoices(String keyword, String status, int offset, int limit) {
        java.util.List<dto.InvoiceListDto> list = new java.util.ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT i.id, i.invoice_code, i.total_amount, i.status, i.updated_at, " +
            "       b.booking_code, " +
            "       COALESCE(bg.full_name, a.full_name, '') AS guest_name " +
            "FROM invoices i " +
            "JOIN bookings b ON i.booking_id = b.id " +
            "LEFT JOIN booking_guests bg ON bg.booking_id = b.id AND bg.is_primary_guest = TRUE " +
            "LEFT JOIN accounts a ON a.id = b.customer_id " +
            "WHERE 1=1 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (i.invoice_code LIKE ? OR b.booking_code LIKE ? OR bg.full_name LIKE ? OR a.full_name LIKE ? OR bg.phone LIKE ? OR a.phone LIKE ?) ");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND i.status = ? ");
        }
        sql.append("ORDER BY i.updated_at DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                for (int i=0; i<6; i++) {
                    ps.setString(paramIdx++, kw);
                }
            }
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIdx++, status);
            }
            ps.setInt(paramIdx++, limit);
            ps.setInt(paramIdx++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.InvoiceListDto dto = new dto.InvoiceListDto();
                    dto.setInvoiceId(rs.getLong("id"));
                    dto.setInvoiceCode(rs.getString("invoice_code"));
                    dto.setBookingCode(rs.getString("booking_code"));
                    dto.setGuestName(rs.getString("guest_name"));
                    dto.setTotalAmount(rs.getBigDecimal("total_amount"));
                    dto.setStatus(rs.getString("status"));
                    dto.setUpdatedAt(rs.getTimestamp("updated_at"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countInvoices(String keyword, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(i.id) FROM invoices i " +
            "JOIN bookings b ON i.booking_id = b.id " +
            "LEFT JOIN booking_guests bg ON bg.booking_id = b.id AND bg.is_primary_guest = TRUE " +
            "LEFT JOIN accounts a ON a.id = b.customer_id " +
            "WHERE 1=1 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (i.invoice_code LIKE ? OR b.booking_code LIKE ? OR bg.full_name LIKE ? OR a.full_name LIKE ? OR bg.phone LIKE ? OR a.phone LIKE ?) ");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND i.status = ? ");
        }

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                for (int i=0; i<6; i++) {
                    ps.setString(paramIdx++, kw);
                }
            }
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIdx++, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
