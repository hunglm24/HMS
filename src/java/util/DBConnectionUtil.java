package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionUtil {

    // Cấu hình thông tin kết nối MySQL
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "ten_database_cua_ban"; // Thay bằng tên CSDL của bạn
    private static final String USERNAME = "root";               // Username MySQL (mặc định là root)
    private static final String PASSWORD = "";                   // Mật khẩu MySQL (để trống nếu không có)

    // Chuỗi URL kết nối (đã cấu hình múi giờ và hỗ trợ tiếng Việt UTF-8)
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

    /**
     * Phương thức lấy kết nối đến MySQL
     * @return Connection đối tượng kết nối SQL
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Đăng ký JDBC Driver của MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Khởi tạo kết nối
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver! Hãy kiểm tra file .jar trong Libraries.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Kết nối thất bại! Kiểm tra lại URL, Username hoặc Password.");
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Phương thức đóng kết nối an toàn
     * @param conn Đối tượng Connection cần đóng
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Hàm main dùng để kiểm tra chạy thử kết nối
    public static void main(String[] args) {
        Connection conn = DBConnectionUtil.getConnection();
        if (conn != null) {
            System.out.println("Kết nối MySQL thành công!");
            DBConnectionUtil.closeConnection(conn);
        } else {
            System.out.println("Kết nối MySQL thất bại!");
        }
    }
}