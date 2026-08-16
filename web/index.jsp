<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String indexBeanString(Object bean, String getterName) {
        if (bean == null) return "";
        try {
            Object value = bean.getClass().getMethod(getterName).invoke(bean);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }
%>
<%
    Object currentUser = session.getAttribute("currentUser");
    String role = indexBeanString(currentUser, "getRoleName");
    boolean internal = currentUser != null && !"CUSTOMER".equalsIgnoreCase(role);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<% if (!internal) { %>
<main class="public-page">
    <section class="hero-section">
        <div class="hero-content">
            <p class="hero-kicker">Hệ thống quản lý khách sạn</p>
            <h1>Đặt phòng nhanh, trải nghiệm khách sạn gọn gàng.</h1>
            <p>Tìm phòng, xem hạng phòng và quản lý booking cá nhân trong một giao diện đơn giản.</p>
            <form class="booking-strip" method="get" action="${pageContext.request.contextPath}/search">
                <label>Check-in<input type="date" name="checkIn"></label>
                <label>Check-out<input type="date" name="checkOut"></label>
                <label>Khách<select name="guests"><option>1</option><option selected>2</option><option>3</option><option>4</option></select></label>
                <label>Loại phòng<select name="roomType"><option value="">Tất cả</option><option>Deluxe</option><option>Suite</option></select></label>
                <button type="submit">Tìm phòng</button>
            </form>
        </div>
    </section>
    <section class="section-head">
        <div><p class="section-kicker">Nổi bật</p><h2>Phòng nổi bật</h2></div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/search">Xem tất cả</a>
    </section>
    <section class="room-card-grid">
        <article class="room-showcase-card">
            <img src="https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=900&q=80" alt="Deluxe room">
            <div class="room-showcase-card__body"><h3>Deluxe City View</h3><p>Phòng sáng, đầy đủ tiện nghi, phù hợp cho kỳ nghỉ ngắn ngày.</p><div class="room-meta"><span>2 khách</span><span>32 m2</span><span>Bữa sáng</span></div><a class="btn" href="${pageContext.request.contextPath}/room-detail">Xem chi tiết</a></div>
        </article>
        <article class="room-showcase-card">
            <img src="https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80" alt="Suite room">
            <div class="room-showcase-card__body"><h3>Executive Suite</h3><p>Không gian rộng, có khu tiếp khách và view thành phố.</p><div class="room-meta"><span>3 khách</span><span>48 m2</span><span>Mini bar</span></div><a class="btn" href="${pageContext.request.contextPath}/room-detail">Xem chi tiết</a></div>
        </article>
    </section>
</main>
<% } else { %>
<main class="page-container">
    <section class="section-head">
        <div><p class="section-kicker">Dashboard</p><h1>Xin chào, ${sessionScope.currentUser.fullName}</h1><p>Chọn nghiệp vụ để tiếp tục vận hành khách sạn.</p></div>
    </section>

    <% if ("HOTEL_MANAGER".equalsIgnoreCase(role)) { %>
        <section class="kpi-grid">
            <div class="metric-card"><span>Công suất phòng</span><strong>76%</strong></div>
            <div class="metric-card"><span>Khách đến hôm nay</span><strong>8</strong></div>
            <div class="metric-card"><span>Nhiệm vụ dọn phòng</span><strong>16</strong></div>
        </section>
    <% } %>

    <section class="dashboard-grid">
        <% if ("RECEPTIONIST".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/bookings"><span>Lễ tân</span><h3>Booking</h3><p>Quản lý đặt phòng, check-in và check-out.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/room-map"><span>Phòng</span><h3>Sơ đồ phòng</h3><p>Xem nhanh tình trạng phòng theo tầng.</p></a>
        <% } else if ("HOUSEKEEPING".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/tasks"><span>Buồng phòng</span><h3>Công việc phòng</h3><p>Nhận việc dọn phòng và kiểm tra checkout.</p></a>
        <% } else if ("HOTEL_MANAGER".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/reports"><span>Báo cáo</span><h3>Báo cáo</h3><p>Theo dõi doanh thu, công suất phòng và nhân sự.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/rooms"><span>Phòng</span><h3>Quản lý phòng</h3><p>Quản lý phòng vật lý và hạng phòng.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/tasks?view=history"><span>Buồng phòng</span><h3>Nhiệm vụ dọn phòng</h3><p>Theo dõi lịch sử và tiến độ dọn phòng.</p></a>
        <% } else if ("ADMIN".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/admin/users"><span>Admin</span><h3>Người dùng</h3><p>Quản lý tài khoản và phân quyền.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/admin/system-config"><span>Cấu hình</span><h3>Cấu hình</h3><p>Thiết lập hệ thống và tích hợp.</p></a>
        <% } %>
    </section>
</main>
<% } %>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
