<%@ page import="java.sql.*, util.*" %>
<%
    out.println("<h3>Latest Bookings:</h3><table border='1'><tr><th>ID</th><th>Code</th><th>Status</th><th>Note</th><th>Customer_ID</th><th>Guest_Name</th></tr>");
    try (Connection conn = DBConnectionUtil.getConnection();
         Statement stmt = conn.createStatement()) {
        
        ResultSet rs = stmt.executeQuery("SELECT b.id, b.booking_code, b.status, b.note, b.customer_id, bg.full_name FROM bookings b LEFT JOIN booking_guests bg ON bg.booking_id = b.id ORDER BY b.id DESC LIMIT 5");
        while(rs.next()) {
            out.println("<tr><td>" + rs.getLong(1) + "</td><td>" + rs.getString(2) + "</td><td>" + rs.getString(3) + "</td><td>" + rs.getString(4) + "</td><td>" + rs.getLong(5) + "</td><td>" + rs.getString(6) + "</td></tr>");
        }
    } catch (Exception e) {
        out.println("ERROR: " + e.getMessage());
    }
    out.println("</table>");
%>
