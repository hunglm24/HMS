<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Tìm phòng | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page">
    <section class="section-head">
        <div><p class="section-kicker">Booking</p><h1>Tìm phòng trống</h1><p>Lọc theo ngày ở, số khách và hạng phòng.</p></div>
    </section>
    <form class="toolbar-card" method="get" action="${pageContext.request.contextPath}/search">
        <label>Check-in<input type="date" name="checkIn"></label>
        <label>Check-out<input type="date" name="checkOut"></label>
        <label>Số khách<select name="guests"><option>1</option><option selected>2</option><option>3</option><option>4</option></select></label>
        <label>Hạng phòng<select name="roomType"><option value="">Tất cả</option><option>Deluxe</option><option>Suite</option><option>Family</option></select></label>
        <button type="submit">Áp dụng</button>
    </form>
    <% if (request.getAttribute("dateError") != null) { %>
        <div class="message error" role="alert"><%= request.getAttribute("dateError") %></div>
    <% } %>
    <section class="room-card-grid">
        <article class="room-showcase-card"><img src="https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=900&q=80" alt="Deluxe room"><div class="room-showcase-card__body"><h3>Deluxe City View</h3><p>Phòng tiện nghi cho 2 khách, còn phòng trống hôm nay.</p><div class="room-meta"><span>2 khách</span><span>1 giường king</span><span>1.200.000 VND</span></div><a class="btn" href="${pageContext.request.contextPath}/cart">Chọn phòng</a></div></article>
        <article class="room-showcase-card"><img src="https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80" alt="Suite room"><div class="room-showcase-card__body"><h3>Executive Suite</h3><p>Suite rộng với không gian làm việc và tiếp khách riêng.</p><div class="room-meta"><span>3 khách</span><span>48 m2</span><span>2.300.000 VND</span></div><a class="btn" href="${pageContext.request.contextPath}/cart">Chọn phòng</a></div></article>
    </section>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
