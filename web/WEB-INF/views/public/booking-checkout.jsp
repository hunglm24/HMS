<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dto.Cart" %>
<%@ page import="dto.CartItem" %>
<%@ page import="model.User" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
    Cart cart = (Cart) session.getAttribute("cart");
    User currentUser = (User) session.getAttribute("currentUser");
    String errorMsg = (String) request.getAttribute("error");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thanh toán - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        .checkout-container { display: flex; gap: 20px; }
        .checkout-form, .order-summary { flex: 1; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
        .order-summary h3 { margin-top: 0; }
        .summary-item { display: flex; justify-content: space-between; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 5px; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1>Thông tin Đặt phòng</h1>
        
        <% if (errorMsg != null) { %>
            <p style="color:red; font-weight:bold;"><%= errorMsg %></p>
        <% } %>
        
        <div class="checkout-container">
            <div class="checkout-form">
                <h3>Thông tin người đặt</h3>
                <form action="<%= request.getContextPath() %>/booking-checkout" method="POST" class="form-container">
                    <div class="form-group">
                        <label>Họ và Tên</label>
                        <input type="text" value="<%= currentUser != null ? currentUser.getFullName() : "" %>" disabled>
                    </div>
                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" value="<%= currentUser != null ? currentUser.getEmail() : "" %>" disabled>
                    </div>
                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="text" value="<%= currentUser != null ? currentUser.getPhoneNumber() : "" %>" disabled>
                    </div>
                    
                    <button type="submit" class="btn btn-success" style="width: 100%; margin-top: 20px;">Xác nhận Đặt phòng</button>
                </form>
            </div>
            
            <div class="order-summary">
                <h3>Tóm tắt đơn hàng</h3>
                <% if (cart != null && cart.getItems() != null) {
                    for (CartItem item : cart.getItems()) { %>
                    <div class="summary-item">
                        <span><%= item.getRoomType().getName() %> x <%= item.getQuantity() %> (<%= item.getNumberOfNights() %> đêm)</span>
                        <span><%= currencyFormat.format(item.getSubtotal()) %></span>
                    </div>
                <%  }
                   } %>
                
                <h2 style="text-align: right; margin-top: 20px;">
                    Tổng cộng: <%= cart != null ? currencyFormat.format(cart.getTotalAmount()) : "0" %>
                </h2>
            </div>
        </div>
    </main>
</body>
</html>
