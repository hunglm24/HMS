package dao;

import model.RoomType;
import util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchDao {

    public List<RoomType> findAvailableRoomTypes(Date checkIn, Date checkOut, 
                                                 int totalGuests, int roomsCount, 
                                                 BigDecimal minPrice, BigDecimal maxPrice, 
                                                 List<Long> roomTypeIds, String sortBy) {
        List<RoomType> list = new ArrayList<>();
        
        // Calculate required capacity per room (ceil)
        int requiredCapacityPerRoom = (int) Math.ceil((double) totalGuests / roomsCount);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT rt.*, ");
        sql.append(" (SELECT COUNT(r.id) FROM rooms r WHERE r.room_type_id = rt.id AND r.status != 'MAINTENANCE') "); // We assume any room not in maintenance can be available if not booked
        sql.append(" - ");
        sql.append(" (SELECT COUNT(br.id) ");
        sql.append("  FROM booking_rooms br ");
        sql.append("  JOIN bookings b ON br.booking_id = b.id ");
        sql.append("  JOIN rooms r ON br.room_id = r.id ");
        sql.append("  WHERE r.room_type_id = rt.id ");
        sql.append("    AND b.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') ");
        sql.append("    AND (b.check_in_date < ? AND b.check_out_date > ?) ");
        sql.append(" ) AS available_count ");
        sql.append("FROM room_types rt ");
        sql.append("WHERE rt.status = 'ACTIVE' ");
        sql.append("  AND rt.capacity >= ? ");
        
        if (minPrice != null) {
            sql.append(" AND rt.base_price >= ? ");
        }
        if (maxPrice != null) {
            sql.append(" AND rt.base_price <= ? ");
        }
        if (roomTypeIds != null && !roomTypeIds.isEmpty()) {
            sql.append(" AND rt.id IN (");
            for (int i = 0; i < roomTypeIds.size(); i++) {
                sql.append("?");
                if (i < roomTypeIds.size() - 1) sql.append(",");
            }
            sql.append(") ");
        }

        sql.append("HAVING available_count >= ? ");
        
        if ("PRICE_DESC".equals(sortBy)) {
            sql.append("ORDER BY rt.base_price DESC");
        } else {
            sql.append("ORDER BY rt.base_price ASC");
        }

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            ps.setDate(paramIndex++, checkOut);
            ps.setDate(paramIndex++, checkIn);
            ps.setInt(paramIndex++, requiredCapacityPerRoom);
            
            if (minPrice != null) {
                ps.setBigDecimal(paramIndex++, minPrice);
            }
            if (maxPrice != null) {
                ps.setBigDecimal(paramIndex++, maxPrice);
            }
            if (roomTypeIds != null && !roomTypeIds.isEmpty()) {
                for (Long id : roomTypeIds) {
                    ps.setLong(paramIndex++, id);
                }
            }
            
            ps.setInt(paramIndex++, roomsCount); // HAVING available_count >= roomsCount
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomType rt = new RoomType();
                    rt.setId(rs.getLong("id"));
                    rt.setName(rs.getString("name"));
                    rt.setDescription(rs.getString("description"));
                    rt.setCapacity(rs.getInt("capacity"));
                    rt.setBasePrice(rs.getBigDecimal("base_price"));
                    rt.setStatus(rs.getString("status"));
                    rt.setAvailableCount(rs.getInt("available_count"));
                    list.add(rt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
