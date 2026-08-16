<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Chi tiết phòng | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page">
    <section class="section-head"><div><p class="section-kicker">Chi tiết phòng</p><h1>Executive Suite</h1><p>Phòng suite cho khách cần không gian riêng và dịch vụ đầy đủ.</p></div><a class="btn" href="${pageContext.request.contextPath}/cart">Thêm vào giỏ</a></section>
    <section class="room-card-grid">
        <article class="room-showcase-card"><img src="https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80" alt="Suite room"><div class="room-showcase-card__body"><h3>Tiện ích</h3><div class="room-meta"><span>48 m2</span><span>3 khách</span><span>Mini bar</span><span>Bữa sáng</span><span>City view</span></div><p>Giá từ 2.300.000 VND/đêm. Có thể đổi phòng tùy theo tình trạng thực tế.</p></div></article>
        <aside class="preview-card"><span>Đặt phòng</span><h3>Thông tin lưu trú</h3><form class="booking-form" method="get" action="${pageContext.request.contextPath}/checkout"><label>Check-in<input type="date" name="checkIn"></label><label>Check-out<input type="date" name="checkOut"></label><label>Khách<select name="guests"><option>1</option><option selected>2</option><option>3</option></select></label><button type="submit">Tiếp tục thanh toán</button></form></aside>
    </section>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
