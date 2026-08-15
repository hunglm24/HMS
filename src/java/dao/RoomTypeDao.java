package dao;

import model.RoomType;
import util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomTypeDao {

    // Lấy tất cả loại phòng từ database
    public List<RoomType> findAll() {
        List<RoomType> list = new ArrayList<>();
        // Sắp xếp ID giảm dần để loại phòng mới tạo sẽ hiện lên đầu
        String sql = "SELECT * FROM room_types ORDER BY id DESC";
        
        // Sử dụng try-with-resources để tự động đóng kết nối sau khi dùng xong
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            // Duyệt từng dòng kết quả trả về từ database
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // In lỗi ra console nếu có lỗi DB
        }
        return list;
    }

    // Tìm một loại phòng theo ID (dùng khi cần sửa thông tin)
    public Optional<RoomType> findById(long id) {
        String sql = "SELECT * FROM room_types WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id); // Truyền tham số ID vào dấu chấm hỏi
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs)); // Nếu có dữ liệu thì trả về đối tượng
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // Trả về rỗng nếu không tìm thấy
    }

    // Thêm mới một loại phòng vào database
    public boolean insert(RoomType roomType) {
        String sql = "INSERT INTO room_types (name, description, capacity, base_price, status) VALUES (?, ?, ?, ?, ?)";
        
        // RETURN_GENERATED_KEYS dùng để lấy ra cái ID vừa được database tự động tạo (AUTO_INCREMENT)
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Gán các giá trị tương ứng vào dấu ?
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setInt(3, roomType.getCapacity());
            ps.setBigDecimal(4, roomType.getBasePrice());
            ps.setString(5, roomType.getStatus());
            
            int affected = ps.executeUpdate(); // Thực thi câu lệnh Insert
            
            if (affected > 0) { // Nếu insert thành công ít nhất 1 dòng
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        roomType.setId(rs.getLong(1)); // Cập nhật lại ID cho đối tượng Java
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin loại phòng
    public boolean update(RoomType roomType) {
        String sql = "UPDATE room_types SET name = ?, description = ?, capacity = ?, base_price = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setInt(3, roomType.getCapacity());
            ps.setBigDecimal(4, roomType.getBasePrice());
            ps.setString(5, roomType.getStatus());
            ps.setLong(6, roomType.getId()); // Đừng quên gán ID cho điều kiện WHERE
            
            // executeUpdate trả về số dòng bị ảnh hưởng. Nếu > 0 nghĩa là update thành công
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa một loại phòng (lưu ý: có thể báo lỗi khóa ngoại nếu loại phòng này đang có phòng vật lý)
    public boolean delete(long id) {
        String sql = "DELETE FROM room_types WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Hàm phụ trợ (Helper): Chuyển đổi 1 dòng dữ liệu (ResultSet) thành đối tượng RoomType
    private RoomType mapRow(ResultSet rs) throws SQLException {
        RoomType rt = new RoomType();
        rt.setId(rs.getLong("id"));
        rt.setName(rs.getString("name"));
        rt.setDescription(rs.getString("description"));
        rt.setCapacity(rs.getInt("capacity"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setStatus(rs.getString("status"));
        rt.setCreatedAt(rs.getTimestamp("created_at"));
        rt.setUpdatedAt(rs.getTimestamp("updated_at"));
        return rt;
    }
}
