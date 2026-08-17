<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.RoomType" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.math.BigDecimal" %>
<%
    List<RoomType> availableRooms = (List<RoomType>) request.getAttribute("availableRooms");
    String checkIn = (String) request.getAttribute("checkIn");
    String checkOut = (String) request.getAttribute("checkOut");
    
    Integer guests = (Integer) request.getAttribute("guests");
    if (guests == null) guests = 2;
    Integer rooms = (Integer) request.getAttribute("rooms");
    if (rooms == null) rooms = 1;
    
    BigDecimal minPrice = (BigDecimal) request.getAttribute("minPrice");
    BigDecimal maxPrice = (BigDecimal) request.getAttribute("maxPrice");
    String sortBy = (String) request.getAttribute("sortBy");
    
    List<Long> selectedRoomTypes = (List<Long>) request.getAttribute("selectedRoomTypes");
    if (selectedRoomTypes == null) selectedRoomTypes = new java.util.ArrayList<>();
    
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kết quả tìm kiếm - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        .search-container { display: flex; gap: 30px; margin-top: 20px; }
        .sidebar { flex: 0 0 300px; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background: #f9f9f9; }
        .results { flex: 1; }
        .filter-group { margin-bottom: 20px; }
        .filter-group label { display: block; margin-bottom: 8px; font-weight: bold; }
        .filter-group input[type="number"], .filter-group select, .filter-group input[type="date"] { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
        .filter-group .checkbox-label { font-weight: normal; display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
        .room-list { display: flex; flex-direction: column; gap: 20px; }
        .room-card { border: 1px solid #ccc; padding: 15px; border-radius: 5px; display: flex; justify-content: space-between; align-items: center; background: white; }
        .room-info h3 { margin: 0 0 10px 0; }
        .room-price { font-size: 1.2em; font-weight: bold; color: #d9534f; margin-bottom: 10px; }
        .btn-apply { width: 100%; padding: 10px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .btn-apply:hover { background: #0056b3; }
        .header-results { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        
        <div class="search-container">
            <aside class="sidebar">
                <h3>Bộ lọc tìm kiếm</h3>
                <form action="<%= request.getContextPath() %>/search" method="GET" id="searchForm">
                    <div class="filter-group">
                        <label>Ngày nhận phòng</label>
                        <input type="date" name="checkIn" value="<%= checkIn %>" required>
                    </div>
                    <div class="filter-group">
                        <label>Ngày trả phòng</label>
                        <input type="date" name="checkOut" value="<%= checkOut %>" required>
                    </div>
                    <div class="filter-group">
                        <label>Số khách</label>
                        <input type="number" name="guests" min="1" value="<%= guests %>">
                    </div>
                    <div class="filter-group">
                        <label>Số phòng</label>
                        <input type="number" name="rooms" min="1" value="<%= rooms %>">
                    </div>
                    
                    <hr style="margin: 20px 0; border: 0; border-top: 1px solid #ddd;">
                    
                    <div class="filter-group">
                        <label>Sắp xếp theo</label>
                        <select name="sortBy">
                            <option value="PRICE_ASC" <%= "PRICE_ASC".equals(sortBy) ? "selected" : "" %>>Giá tăng dần</option>
                            <option value="PRICE_DESC" <%= "PRICE_DESC".equals(sortBy) ? "selected" : "" %>>Giá giảm dần</option>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label>Khoảng giá (VNĐ)</label>
                        <input type="number" name="minPrice" placeholder="Từ..." value="<%= minPrice != null ? minPrice : "" %>" style="margin-bottom:10px;">
                        <input type="number" name="maxPrice" placeholder="Đến..." value="<%= maxPrice != null ? maxPrice : "" %>">
                    </div>
                    
                    <div class="filter-group">
                        <label>Loại phòng</label>
                        <label class="checkbox-label"><input type="checkbox" name="roomType" value="1" <%= selectedRoomTypes.contains(1L) ? "checked" : "" %>> Standard</label>
                        <label class="checkbox-label"><input type="checkbox" name="roomType" value="2" <%= selectedRoomTypes.contains(2L) ? "checked" : "" %>> Deluxe</label>
                        <label class="checkbox-label"><input type="checkbox" name="roomType" value="3" <%= selectedRoomTypes.contains(3L) ? "checked" : "" %>> Twin</label>
                        <label class="checkbox-label"><input type="checkbox" name="roomType" value="4" <%= selectedRoomTypes.contains(4L) ? "checked" : "" %>> Suite</label>
                        <label class="checkbox-label"><input type="checkbox" name="roomType" value="5" <%= selectedRoomTypes.contains(5L) ? "checked" : "" %>> Family</label>
                    </div>
                    
                    <button type="submit" class="btn-apply">Áp dụng bộ lọc</button>
                </form>
            </aside>
            
            <section class="results">
                <div class="header-results">
                    <h2>Kết quả tìm kiếm</h2>
                    <p>Tìm thấy <strong><%= availableRooms != null ? availableRooms.size() : 0 %></strong> loại phòng trống.</p>
                </div>
                
                <div class="room-list">
                    <% if (availableRooms != null && !availableRooms.isEmpty()) { 
                           for (RoomType rt : availableRooms) { %>
                            <div class="room-card">
                                <div class="room-info">
                                    <h3><%= rt.getName() %></h3>
                                    <p><%= rt.getDescription() %></p>
                                    <p style="color:#666; font-size:0.9em;">Số khách tối đa: <%= rt.getCapacity() %> | Còn trống: <%= rt.getAvailableCount() %> phòng</p>
                                </div>
                                <div class="room-action" style="text-align: right;">
                                    <div class="room-price">
                                        <%= currencyFormat.format(rt.getBasePrice()) %> / đêm
                                    </div>
                                    <a href="<%= request.getContextPath() %>/room-detail?id=<%= rt.getId() %>&checkIn=<%= checkIn %>&checkOut=<%= checkOut %>" class="btn btn-primary" style="padding: 10px 20px; text-decoration: none; display: inline-block;">Xem chi tiết</a>
                                </div>
                            </div>
                    <%     }
                       } else { %>
                        <div style="text-align:center; padding: 40px; background: white; border-radius: 8px; border: 1px solid #ddd;">
                            <h3>Không tìm thấy phòng phù hợp</h3>
                            <p>Vui lòng thử điều chỉnh lại ngày hoặc bộ lọc của bạn.</p>
                        </div>
                    <% } %>
                </div>
            </section>
        </div>
    </main>
</body>
</html>
