package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.RoomType;
import util.DBConnectionUtil;

public class RoomTypeDAO {

    // UC12: Xem chi tiết loại phòng
    public RoomType getRoomTypeById(int id) {
        String sql = "SELECT * FROM room_type WHERE room_type_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RoomType type = new RoomType();
                    type.setRoomTypeId(rs.getInt("room_type_id"));
                    type.setTypeName(rs.getString("type_name"));
                    type.setDescription(rs.getString("description"));
                    type.setBasePrice(rs.getDouble("base_price"));
                    type.setMaxOccupancy(rs.getInt("max_occupancy"));
                    type.setImageUrl(rs.getString("image_url"));
                    return type;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
