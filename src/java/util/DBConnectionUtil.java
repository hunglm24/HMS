package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionUtil {


    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "hmss_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";


    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";


    public static Connection getConnection() {
        Connection conn = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            

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


    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


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