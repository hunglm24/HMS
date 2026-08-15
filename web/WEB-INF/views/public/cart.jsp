<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dto.Cart" %>
<%@ page import="dto.CartItem" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
    Cart cart = (Cart) session.getAttribute("cart");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Giỏ hàng - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .cart-summary { text-align: right; margin-top: 20px; font-size: 1.2em; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1>Giỏ hàng của bạn</h1>
        
        <% if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) { %>
            <table>
                <thead>
                    <tr>
                        <th>Loại phòng</th>
                        <th>Ngày nhận - Ngày trả</th>
                        <th>Giá / Đêm</th>
                        <th>Số lượng</th>
                        <th>Thành tiền</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (CartItem item : cart.getItems()) { %>
                        <tr>
                            <td><%= item.getRoomType().getName() %></td>
                            <td><%= item.getCheckInDate() %> đến <%= item.getCheckOutDate() %> (<%= item.getNumberOfNights() %> đêm)</td>
                            <td><%= currencyFormat.format(item.getPricePerNight()) %></td>
                            <td>
                                <form action="<%= request.getContextPath() %>/cart" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="roomTypeId" value="<%= item.getRoomType().getId() %>">
                                    <input type="number" name="quantity" value="<%= item.getQuantity() %>" min="1" style="width: 50px;">
                                    <button type="submit">Cập nhật</button>
                                </form>
                            </td>
                            <td><%= currencyFormat.format(item.getSubtotal()) %></td>
                            <td>
                                <form action="<%= request.getContextPath() %>/cart" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="roomTypeId" value="<%= item.getRoomType().getId() %>">
                                    <button type="submit" style="color:red;">Xóa</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
            
            <div class="cart-summary">
                <p><strong>Tổng cộng: <%= currencyFormat.format(cart.getTotalAmount()) %></strong></p>
                <a href="<%= request.getContextPath() %>/booking-checkout" class="btn btn-primary">Tiến hành Thanh toán</a>
            </div>
        <% } else { %>
            <p>Giỏ hàng của bạn đang trống.</p>
            <a href="<%= request.getContextPath() %>/search" class="btn btn-primary">Tìm phòng ngay</a>
        <% } %>
    </main>
</body>
</html>
