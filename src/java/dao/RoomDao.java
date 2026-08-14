package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import model.Room;
import util.DBConnectionUtil;

public class RoomDAO {

    // UC10, UC11: Tìm kiếm phòng
    public List<Room> searchRooms(Date checkIn, Date checkOut, int guests, int roomTypeId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.* FROM room r JOIN room_type rt ON r.room_type_id = rt.room_type_id " +
                     "WHERE rt.max_occupancy >= ? AND r.status = 'Available' " +
                     "AND r.room_id NOT IN (" +
                     "    SELECT room_id FROM booking_room br JOIN booking b ON br.booking_id = b.booking_id " +
                     "    WHERE (b.check_in_date < ? AND b.check_out_date > ?) AND br.status != 'Cancelled'" +
                     ")";
        if (roomTypeId > 0) {
            sql += " AND r.room_type_id = ?";
        }
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, guests);
            ps.setDate(2, new java.sql.Date(checkOut.getTime()));
            ps.setDate(3, new java.sql.Date(checkIn.getTime()));
            
            if (roomTypeId > 0) {
                ps.setInt(4, roomTypeId);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    room.setRoomId(rs.getInt("room_id"));
                    room.setRoomNumber(rs.getString("room_number"));
                    room.setFloor(rs.getInt("floor"));
                    room.setRoomTypeId(rs.getInt("room_type_id"));
                    room.setStatus(rs.getString("status"));
                    room.setViewType(rs.getString("view_type"));
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    // UC27: Xem sơ đồ phòng
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM room";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setFloor(rs.getInt("floor"));
                room.setRoomTypeId(rs.getInt("room_type_id"));
                room.setStatus(rs.getString("status"));
                room.setViewType(rs.getString("view_type"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }
}
