package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnectionUtil {
    private static final String HOST = setting("HMS_DB_HOST", "localhost");
    private static final String PORT = setting("HMS_DB_PORT", "3306");
    private static final String DB_NAME = setting("HMS_DB_NAME", "hms_db");
    private static final String USERNAME = setting("HMS_DB_USERNAME", "root");
    private static final String PASSWORD = setting("HMS_DB_PASSWORD", "123456");
    private static final String URL = "jdbc:mysql://" + HOST + ':' + PORT + '/' + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia%2FHo_Chi_Minh&useUnicode=true&characterEncoding=UTF-8";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError("Không tìm thấy MySQL JDBC Driver: " + ex.getMessage());
        }
    }

    private DBConnectionUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void closeConnection(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Prefer try-with-resources; this method remains for legacy callers.
        }
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public static void main(String[] args) {
        try (Connection ignored = getConnection()) {
            System.out.println("Kết nối MySQL thành công!");
        } catch (SQLException ex) {
            System.err.println("Kết nối MySQL thất bại: " + ex.getMessage());
        }
    }
}
