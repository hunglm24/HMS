<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    model.HotelConfig homeConfig = (model.HotelConfig) application.getAttribute("hotelConfig");
    String homeHotelName = homeConfig != null && homeConfig.getHotelName() != null && !homeConfig.getHotelName().isBlank()
            ? homeConfig.getHotelName()
            : "HMS Hotel";
    java.util.List<model.RoomType> publicRoomTypes = new dao.RoomTypeDao().findActive();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= escapeHtml(homeHotelName) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page">
    <section class="hero-section">
        <div class="hero-content">
            <p class="hero-kicker"><%= escapeHtml(homeHotelName) %></p>
            <h1>Đặt phòng nhanh, vận hành khách sạn gọn gàng.</h1>
            <p>Giao diện website khách sạn: tìm phòng, chọn ngày lưu trú, xem hạng phòng và theo dõi booking cá nhân.</p>
            <form class="booking-strip" method="get" action="${pageContext.request.contextPath}/search">
                <label>Check-in<input type="date" name="checkIn"></label>
                <label>Check-out<input type="date" name="checkOut"></label>
                <label>Khách<select name="guests"><option>1</option><option selected>2</option><option>3</option><option>4</option></select></label>
                <label>Loại phòng<select name="roomTypeId">
                    <option value="">Tất cả</option>
                    <% for (model.RoomType rt : publicRoomTypes) { %>
                        <option value="<%= rt.getId() %>"><%= rt.getName() %></option>
                    <% } %>
                </select></label>
                <button type="submit">Tìm phòng</button>
            </form>
        </div>
    </section>

    <section class="section-head">
        <div><p class="section-kicker">Nổi bật</p><h2>Hạng phòng phổ biến</h2></div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/search">Xem tất cả</a>
    </section>

    <section class="room-card-grid">
        <article class="room-showcase-card">
            <img src="https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=900&q=80" alt="Deluxe room">
            <div class="room-showcase-card__body">
                <h3>Deluxe City View</h3>
                <p>Phòng rộng, ánh sáng tốt, phù hợp cặp đôi và khách công tác.</p>
                <div class="room-meta"><span>2 khách</span><span>32 m2</span><span>Bữa sáng</span></div>
                <a class="btn" href="${pageContext.request.contextPath}/room-detail">Xem chi tiết</a>
            </div>
        </article>
        <article class="room-showcase-card">
            <img src="https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80" alt="Suite room">
            <div class="room-showcase-card__body">
                <h3>Executive Suite</h3>
                <p>Không gian tiếp khách riêng, view đẹp, ưu tiên cho kỳ nghỉ dài ngày.</p>
                <div class="room-meta"><span>3 khách</span><span>48 m2</span><span>Mini bar</span></div>
                <a class="btn" href="${pageContext.request.contextPath}/room-detail">Xem chi tiết</a>
            </div>
        </article>
        <article class="room-showcase-card">
            <img src="https://images.unsplash.com/photo-1595576508898-0ad5c879a061?auto=format&fit=crop&w=900&q=80" alt="Family room">
            <div class="room-showcase-card__body">
                <h3>Family Room</h3>
                <p>Bố trí linh hoạt cho gia đình, gần thang máy và khu tiện ích.</p>
                <div class="room-meta"><span>4 khách</span><span>55 m2</span><span>Gia đình</span></div>
                <a class="btn" href="${pageContext.request.contextPath}/room-detail">Xem chi tiết</a>
            </div>
        </article>
    </section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
