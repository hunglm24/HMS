package dao;

import model.RoomType;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchDao {

    public List<RoomType> findAvailableRoomTypes(Date checkIn, Date checkOut) {
        List<RoomType> list = new ArrayList<>();
        
        String sql = "SELECT rt.*, " +
                     " (SELECT COUNT(r.id) FROM rooms r WHERE r.room_type_id = rt.id AND r.status = 'AVAILABLE') " +
                     " - " +
                     " (SELECT COUNT(br.id) " +
                     "  FROM booking_rooms br " +
                     "  JOIN bookings b ON br.booking_id = b.id " +
                     "  JOIN rooms r ON br.room_id = r.id " +
                     "  WHERE r.room_type_id = rt.id " +
                     "    AND b.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') " +
                     "    AND (b.check_in_date < ? AND b.check_out_date > ?) " +
                     " ) AS available_count " +
                     "FROM room_types rt " +
                     "WHERE rt.status = 'ACTIVE' " +
                     "HAVING available_count > 0 " +
                     "ORDER BY rt.base_price ASC";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, checkOut); // Notice how we compare b.check_in_date < checkOut
            ps.setDate(2, checkIn);  // and b.check_out_date > checkIn
            
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
