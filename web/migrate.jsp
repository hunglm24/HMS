<%@ page import="java.sql.*, util.*" %>
<%
    try (Connection conn = DBConnectionUtil.getConnection();
         Statement stmt = conn.createStatement()) {
        try {
            stmt.executeUpdate("ALTER TABLE bookings ADD COLUMN note TEXT NULL");
            out.print("SUCCESS: column added.");
        } catch (Exception e) {
            out.print("ERROR: " + e.getMessage());
        }
    } catch (Exception e) {
        out.print("DB ERROR: " + e.getMessage());
    }
%>
