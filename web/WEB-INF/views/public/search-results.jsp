<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.RoomType" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
    List<RoomType> availableRooms = (List<RoomType>) request.getAttribute("availableRooms");
    String checkIn = (String) request.getAttribute("checkIn");
    String checkOut = (String) request.getAttribute("checkOut");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kết quả tìm kiếm - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        .room-list { display: flex; flex-direction: column; gap: 20px; }
        .room-card { border: 1px solid #ccc; padding: 15px; border-radius: 5px; display: flex; justify-content: space-between; align-items: center; }
        .room-info h3 { margin: 0 0 10px 0; }
        .room-price { font-size: 1.2em; font-weight: bold; color: #d9534f; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1>Kết quả tìm kiếm</h1>
        <p>Từ: <strong><%= checkIn %></strong> - Đến: <strong><%= checkOut %></strong></p>
        
        <div class="room-list">
            <% if (availableRooms != null && !availableRooms.isEmpty()) { 
                   for (RoomType rt : availableRooms) { %>
                    <div class="room-card">
                        <div class="room-info">
                            <h3><%= rt.getName() %></h3>
                            <p><%= rt.getDescription() %></p>
                            <p>Số khách tối đa: <%= rt.getCapacity() %> | Còn trống: <%= rt.getAvailableCount() %> phòng</p>
                        </div>
                        <div class="room-action">
                            <p class="room-price">
                                <%= currencyFormat.format(rt.getBasePrice()) %> / đêm
                            </p>
                            <a href="<%= request.getContextPath() %>/room-detail?id=<%= rt.getId() %>&checkIn=<%= checkIn %>&checkOut=<%= checkOut %>" class="btn btn-primary">Xem chi tiết</a>
                        </div>
                    </div>
            <%     }
               } else { %>
                <p>Không tìm thấy phòng nào trống trong khoảng thời gian này. Vui lòng thử ngày khác.</p>
            <% } %>
        </div>
        
        <br>
        <a href="<%= request.getContextPath() %>/search">Quay lại tìm kiếm</a>
    </main>
</body>
</html>
