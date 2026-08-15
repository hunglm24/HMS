<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.RoomType" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
    RoomType roomType = (RoomType) request.getAttribute("roomType");
    String checkIn = (String) request.getAttribute("checkIn");
    String checkOut = (String) request.getAttribute("checkOut");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết phòng - <%= roomType.getName() %> - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1><%= roomType.getName() %></h1>
        
        <div class="room-details">
            <p><strong>Mô tả:</strong> <%= roomType.getDescription() %></p>
            <p><strong>Sức chứa:</strong> <%= roomType.getCapacity() %> người</p>
            <p><strong>Giá mỗi đêm:</strong> <%= currencyFormat.format(roomType.getBasePrice()) %></p>
        </div>
        
        <hr>
        
        <% if (checkIn != null && !checkIn.isEmpty() && checkOut != null && !checkOut.isEmpty()) { %>
            <form action="<%= request.getContextPath() %>/cart" method="POST" class="form-container">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="roomTypeId" value="<%= roomType.getId() %>">
                <input type="hidden" name="checkIn" value="<%= checkIn %>">
                <input type="hidden" name="checkOut" value="<%= checkOut %>">
                
                <div class="form-group">
                    <label>Ngày đã chọn:</label>
                    <p><%= checkIn %> đến <%= checkOut %></p>
                </div>
                
                <div class="form-group">
                    <label for="quantity">Số lượng phòng:</label>
                    <input type="number" id="quantity" name="quantity" min="1" max="10" value="1" required>
                </div>
                
                <button type="submit" class="btn btn-success">Thêm vào giỏ hàng</button>
            </form>
        <% } else { %>
            <p>Vui lòng <a href="<%= request.getContextPath() %>/search">chọn ngày</a> để tiến hành đặt phòng.</p>
        <% } %>
        
    </main>
</body>
</html>
