package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.InvoiceItem;
import util.DBConnectionUtil;

public class InvoiceItemDao {

    public boolean addInvoiceItem(InvoiceItem item) {
        String sql = "INSERT INTO invoice_items (invoice_id, damage_report_id, item_type, description, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, item.getInvoiceId());
            if (item.getDamageReportId() != null) {
                ps.setLong(2, item.getDamageReportId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, item.getItemType());
            ps.setString(4, item.getDescription());
            ps.setInt(5, item.getQuantity());
            ps.setBigDecimal(6, item.getUnitPrice());
            ps.setBigDecimal(7, item.getTotalPrice());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public java.util.List<InvoiceItem> findByInvoiceId(long invoiceId) {
        java.util.List<InvoiceItem> items = new java.util.ArrayList<>();
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, invoiceId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setId(rs.getLong("id"));
                    item.setInvoiceId(rs.getLong("invoice_id"));
                    item.setDamageReportId(rs.getLong("damage_report_id") == 0 ? null : rs.getLong("damage_report_id"));
                    item.setItemType(rs.getString("item_type"));
                    item.setDescription(rs.getString("description"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
