<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Booking" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt phòng của tôi - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #f2f2f2; }
        .status-badge { padding: 5px 10px; border-radius: 3px; font-size: 0.9em; font-weight: bold; }
        .status-PENDING_PAYMENT { background: #fcf8e3; color: #8a6d3b; }
        .status-CONFIRMED { background: #dff0d8; color: #3c763d; }
        .status-CANCELLED { background: #f2dede; color: #a94442; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1>Lịch sử Đặt phòng của tôi</h1>
        
        <%
            String currentStatus = (String) request.getAttribute("currentStatus");
            if (currentStatus == null) currentStatus = "ALL";
            String bookingCode = (String) request.getAttribute("bookingCode");
            java.sql.Date fromDate = (java.sql.Date) request.getAttribute("fromDate");
            java.sql.Date toDate = (java.sql.Date) request.getAttribute("toDate");
        %>
        
        <form action="<%= request.getContextPath() %>/my-bookings" method="GET" style="margin-bottom: 20px; background: #f9f9f9; padding: 15px; border-radius: 8px; border: 1px solid #ddd;">
            <div style="display: flex; gap: 10px; margin-bottom: 15px;">
                <input type="text" name="bookingCode" placeholder="Mã đơn đặt phòng..." value="<%= bookingCode != null ? bookingCode : "" %>" style="flex: 1; padding: 8px;">
                <input type="date" name="fromDate" value="<%= fromDate != null ? fromDate.toString() : "" %>" style="padding: 8px;">
                <input type="date" name="toDate" value="<%= toDate != null ? toDate.toString() : "" %>" style="padding: 8px;">
                <button type="submit" class="btn btn-primary">Tìm kiếm</button>
            </div>
            <div style="display: flex; gap: 5px; flex-wrap: wrap;">
                <input type="hidden" name="status" id="statusField" value="<%= currentStatus %>">
                <%
                    String[][] tabs = {
                        {"ALL", "Tất cả"},
                        {"PENDING_PAYMENT", "Chờ thanh toán"},
                        {"CONFIRMED", "Đã xác nhận"},
                        {"CHECKED_IN", "Đang ở"},
                        {"CHECKED_OUT", "Đã hoàn thành"},
                        {"CANCELLED", "Đã hủy"}
                    };
                    for (String[] tab : tabs) {
                        String activeClass = tab[0].equals(currentStatus) ? "background: #007bff; color: white;" : "background: #e9ecef; color: #333;";
                %>
                <button type="button" onclick="document.getElementById('statusField').value='<%= tab[0] %>'; this.form.submit();" 
                        style="padding: 8px 15px; border: none; border-radius: 20px; cursor: pointer; <%= activeClass %>">
                    <%= tab[1] %>
                </button>
                <% } %>
            </div>
        </form>
        
        <% if (bookings != null && !bookings.isEmpty()) { %>
            <table>
                <thead>
                    <tr>
                        <th>Mã Đặt phòng</th>
                        <th>Ngày đặt</th>
                        <th>Nhận phòng</th>
                        <th>Trả phòng</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Booking b : bookings) { %>
                        <tr>
                            <td><strong><%= b.getBookingCode() %></strong></td>
                            <td><%= b.getCreatedAt() != null ? sdfDateTime.format(b.getCreatedAt()) : "" %></td>
                            <td><%= b.getCheckInDate() != null ? sdfDate.format(b.getCheckInDate()) : "" %></td>
                            <td><%= b.getCheckOutDate() != null ? sdfDate.format(b.getCheckOutDate()) : "" %></td>
                            <td><%= currencyFormat.format(b.getTotalAmount()) %></td>
                            <td>
                                <span class="status-badge status-<%= b.getStatus() %>"><%= b.getStatus() %></span>
                            </td>
                            <td>
                                <a href="<%= request.getContextPath() %>/booking-confirmation?id=<%= b.getId() %>">Xem chi tiết</a>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else { %>
            <div style="padding: 30px; text-align: center; border: 1px solid #ddd; background: #fff; border-radius: 8px;">
                <p>Không tìm thấy đơn đặt phòng nào phù hợp.</p>
                <a href="<%= request.getContextPath() %>/search" class="btn btn-primary" style="margin-top: 10px; display: inline-block;">Khám phá các phòng trống</a>
            </div>
        <% } %>
    </main>
</body>
</html>
