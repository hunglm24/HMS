<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Booking" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
    Booking booking = (Booking) request.getAttribute("booking");
    String paymentStatus = request.getParameter("payment");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Xác nhận Đặt phòng - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        .confirmation-box { text-align: center; margin-top: 50px; padding: 30px; border: 1px solid #ccc; border-radius: 5px; }
        .success-text { color: green; font-size: 1.5em; margin-bottom: 20px; }
        .fail-text { color: red; font-size: 1.2em; margin-bottom: 20px; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        
        <div class="confirmation-box">
            <% if ("success".equals(paymentStatus)) { %>
                <p class="success-text">Thanh toán thành công! Trạng thái đơn đặt phòng đã được cập nhật.</p>
            <% } else if ("failed".equals(paymentStatus)) { %>
                <p class="fail-text">Thanh toán thất bại hoặc bị hủy. Vui lòng thử lại.</p>
            <% } %>
        
            <% if (booking != null) { %>
                <h2>Đơn Đặt phòng: <%= booking.getBookingCode() %></h2>
                <p>Trạng thái: <strong><%= booking.getStatus() %></strong></p>
                <p>Nhận phòng: <%= booking.getCheckInDate() %> | Trả phòng: <%= booking.getCheckOutDate() %></p>
                <p>Tổng tiền: <strong><%= currencyFormat.format(booking.getTotalAmount()) %></strong></p>
                
                <br>
                
                <% if ("PENDING_PAYMENT".equals(booking.getStatus())) { %>
                    <p>Vui lòng thanh toán để xác nhận đơn đặt phòng của bạn.</p>
                    <form action="<%= request.getContextPath() %>/api/vnpay-create" method="POST">
                        <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                        <button type="submit" class="btn btn-primary" style="padding: 10px 30px; font-size: 1.2em;">Thanh toán bằng VNPay</button>
                    </form>
                <% } else { %>
                    <p>Đơn đặt phòng của bạn đã được xác nhận. Hẹn gặp bạn sớm!</p>
                <% } %>
            <% } else { %>
                <p>Không tìm thấy thông tin đơn đặt phòng.</p>
            <% } %>
            
            <br><br>
            <a href="<%= request.getContextPath() %>/my-bookings">Xem danh sách đặt phòng của tôi</a>
        </div>
    </main>
</body>
</html>
