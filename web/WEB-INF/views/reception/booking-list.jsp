<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Booking" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
    String keyword = (String) request.getAttribute("keyword");
    String currentStatus = (String) request.getAttribute("currentStatus");
    if (currentStatus == null) currentStatus = "ALL";
    String dateType = (String) request.getAttribute("dateType");
    java.sql.Date fromDate = (java.sql.Date) request.getAttribute("fromDate");
    java.sql.Date toDate = (java.sql.Date) request.getAttribute("toDate");
    String bookingSource = (String) request.getAttribute("bookingSource");
    
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Đơn đặt phòng - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        .filter-container { background: #f9f9f9; padding: 20px; border-radius: 8px; border: 1px solid #ddd; margin-bottom: 20px; }
        .filter-row { display: flex; gap: 15px; margin-bottom: 15px; flex-wrap: wrap; }
        .filter-group { flex: 1; min-width: 200px; display: flex; flex-direction: column; }
        .filter-group label { margin-bottom: 5px; font-weight: bold; font-size: 0.9em; }
        .filter-group input, .filter-group select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .status-tabs { display: flex; gap: 5px; margin-bottom: 20px; border-bottom: 2px solid #ddd; padding-bottom: 10px; }
        .tab-btn { padding: 8px 15px; border: none; border-radius: 20px; cursor: pointer; background: #e9ecef; }
        .tab-btn.active { background: #007bff; color: white; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #f2f2f2; }
        .status-badge { padding: 4px 8px; border-radius: 3px; font-size: 0.85em; font-weight: bold; }
        .status-PENDING_PAYMENT { background: #fcf8e3; color: #8a6d3b; }
        .status-CONFIRMED { background: #dff0d8; color: #3c763d; }
        .status-CHECKED_IN { background: #d9edf7; color: #31708f; }
        .status-CHECKED_OUT { background: #dff0d8; color: #3c763d; }
        .status-CANCELLED { background: #f2dede; color: #a94442; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container" style="max-width: 1400px;">
        <h1>Quản lý Đơn đặt phòng (Lễ tân)</h1>
        
        <div class="filter-container">
            <form action="<%= request.getContextPath() %>/reception/bookings" method="GET">
                <input type="hidden" name="status" id="statusField" value="<%= currentStatus %>">
                
                <div class="status-tabs">
                    <%
                        String[][] tabs = {
                            {"ALL", "Tất cả"},
                            {"PENDING_PAYMENT", "Chờ thanh toán"},
                            {"CONFIRMED", "Đã duyệt/Cọc"},
                            {"CHECKED_IN", "Đang ở"},
                            {"CHECKED_OUT", "Đã trả phòng"},
                            {"CANCELLED", "Đã hủy"}
                        };
                        for (String[] tab : tabs) {
                            String activeClass = tab[0].equals(currentStatus) ? "active" : "";
                    %>
                    <button type="button" class="tab-btn <%= activeClass %>" onclick="document.getElementById('statusField').value='<%= tab[0] %>'; this.form.submit();">
                        <%= tab[1] %>
                    </button>
                    <% } %>
                </div>
                
                <div class="filter-row">
                    <div class="filter-group" style="flex: 2;">
                        <label>Tìm kiếm nhanh</label>
                        <input type="text" name="keyword" placeholder="Mã booking, Tên khách, SĐT, Số phòng..." value="<%= keyword != null ? keyword : "" %>">
                    </div>
                    <div class="filter-group">
                        <label>Nguồn đặt phòng</label>
                        <select name="bookingSource">
                            <option value="ALL">Tất cả</option>
                            <option value="ONLINE" <%= "ONLINE".equals(bookingSource) ? "selected" : "" %>>Online</option>
                            <option value="RECEPTION" <%= "RECEPTION".equals(bookingSource) ? "selected" : "" %>>Tại quầy (Walk-in)</option>
                        </select>
                    </div>
                </div>
                
                <div class="filter-row">
                    <div class="filter-group">
                        <label>Lọc theo thời gian</label>
                        <select name="dateType">
                            <option value="">-- Chọn mốc thời gian --</option>
                            <option value="CREATED" <%= "CREATED".equals(dateType) ? "selected" : "" %>>Ngày tạo đơn</option>
                            <option value="CHECKIN" <%= "CHECKIN".equals(dateType) ? "selected" : "" %>>Ngày đến (Check-in)</option>
                            <option value="CHECKOUT" <%= "CHECKOUT".equals(dateType) ? "selected" : "" %>>Ngày đi (Check-out)</option>
                            <option value="STAY" <%= "STAY".equals(dateType) ? "selected" : "" %>>Ngày lưu trú</option>
                        </select>
                    </div>
                    <div class="filter-group">
                        <label>Từ ngày</label>
                        <input type="date" name="fromDate" value="<%= fromDate != null ? fromDate.toString() : "" %>">
                    </div>
                    <div class="filter-group">
                        <label>Đến ngày</label>
                        <input type="date" name="toDate" value="<%= toDate != null ? toDate.toString() : "" %>">
                    </div>
                    <div class="filter-group" style="justify-content: flex-end;">
                        <button type="submit" class="btn btn-primary" style="padding: 9px;">Áp dụng bộ lọc</button>
                    </div>
                </div>
            </form>
        </div>
        
        <div style="background: white; border-radius: 8px; border: 1px solid #ddd; overflow: hidden;">
            <% if (bookings != null && !bookings.isEmpty()) { %>
                <table>
                    <thead>
                        <tr>
                            <th>Mã Booking</th>
                            <th>Khách hàng</th>
                            <th>Phòng</th>
                            <th>Ngày đến</th>
                            <th>Ngày đi</th>
                            <th>Nguồn</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Booking b : bookings) { %>
                            <tr>
                                <td><strong><%= b.getBookingCode() %></strong><br><small style="color: #888;"><%= sdfDateTime.format(b.getCreatedAt()) %></small></td>
                                <td>
                                    <%= b.getCustomerName() != null ? b.getCustomerName() : "Khách lẻ" %><br>
                                    <small><%= b.getCustomerPhone() != null ? b.getCustomerPhone() : "" %></small>
                                </td>
                                <td><%= b.getRoomNumbers() != null ? b.getRoomNumbers() : "Chưa gán" %></td>
                                <td><%= b.getCheckInDate() != null ? sdfDate.format(b.getCheckInDate()) : "" %></td>
                                <td><%= b.getCheckOutDate() != null ? sdfDate.format(b.getCheckOutDate()) : "" %></td>
                                <td><%= b.getBookingSource() %></td>
                                <td>
                                    <span class="status-badge status-<%= b.getStatus() %>"><%= b.getStatus() %></span>
                                </td>
                                <td>
                                    <a href="<%= request.getContextPath() %>/reception/booking-detail?id=<%= b.getId() %>" class="btn btn-sm btn-primary">Chi tiết</a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <div style="padding: 40px; text-align: center;">
                    <p>Không có dữ liệu phù hợp với bộ lọc.</p>
                </div>
            <% } %>
        </div>
    </main>
</body>
</html>
