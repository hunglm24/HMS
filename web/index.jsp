<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (request != null) {
        request.getRequestDispatcher("/WEB-INF/views/public/home.jsp").forward(request, response);
        return;
    }
%>
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

    private String escapeHtml(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }

    private String resolveImageSrc(String contextPath, String imageUrl, String fallbackUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return fallbackUrl;
        }
        String trimmed = imageUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return contextPath + trimmed;
        }
        return contextPath + "/" + trimmed;
    }

    private boolean indexHasPermission(Object permissions, String code) {
        return permissions instanceof java.util.Set && ((java.util.Set<?>) permissions).contains(code);
    }
%>
<%
    model.HotelConfig indexConfig = (model.HotelConfig) application.getAttribute("hotelConfig");
    String indexHotelName = indexConfig != null && indexConfig.getHotelName() != null && !indexConfig.getHotelName().isBlank()
            ? indexConfig.getHotelName()
            : "HMS Hotel";
    Object currentUser = session.getAttribute("currentUser");
    Object permissionCodes = session.getAttribute("permissionCodes");
    String role = indexBeanString(currentUser, "getRoleName");
    boolean internal = currentUser != null && !"CUSTOMER".equalsIgnoreCase(role);
    boolean canAdminUsers = "ADMIN".equalsIgnoreCase(role) || indexHasPermission(permissionCodes, "ADMIN_USERS");
    boolean canAdminRoles = "ADMIN".equalsIgnoreCase(role) || indexHasPermission(permissionCodes, "ADMIN_ROLES");
    boolean canAdminLogs = "ADMIN".equalsIgnoreCase(role) || indexHasPermission(permissionCodes, "ADMIN_LOGS");

    java.util.List<model.RoomType> featuredRoomTypes = java.util.Collections.emptyList();
    java.util.List<model.News> latestNews = java.util.Collections.emptyList();
    java.util.List<dao.FeedbackDao.FeedbackDto> featuredFeedbacks = java.util.Collections.emptyList();
    if (internal) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
    if (!internal) {
        dao.RoomTypeDao roomTypeDao = new dao.RoomTypeDao();
        featuredRoomTypes = roomTypeDao.findFeaturedAvailable(4);
        try {
            dao.NewsDao newsDao = new dao.NewsDao();
            latestNews = newsDao.getLatestNews(3);
        } catch (Exception ex) {
            latestNews = java.util.Collections.emptyList();
        }
        try {
            dao.FeedbackDao feedbackDao = new dao.FeedbackDao();
            featuredFeedbacks = feedbackDao.findFeaturedFeedbacks(3);
        } catch (Exception ex) {
            featuredFeedbacks = java.util.Collections.emptyList();
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= escapeHtml(indexHotelName) %></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/feedback.css?v=20260824-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<% if (!internal) { %>
<main class="public-page">
    <section class="hero-section">
        <div class="hero-content">
            <h1>TÌM PHÒNG</h1>
            <p>Khám phá danh sách phòng hiện có và chọn hạng phòng phù hợp cho kỳ nghỉ của bạn.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/search">Tìm phòng</a>
        </div>
    </section>

    <section class="section-head">
        <div><p class="section-kicker">Gợi ý cho bạn</p><h2>Phòng nổi bật</h2></div>
    </section>

    <section class="room-card-grid">
        <% if (featuredRoomTypes.isEmpty()) { %>
            <p>Hiện chưa có phòng trống để giới thiệu.</p>
        <% } else { %>
            <% for (model.RoomType roomType : featuredRoomTypes) {
                String imageUrl = resolveImageSrc(
                        request.getContextPath(),
                        roomType.getImageUrl(),
                        "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80");
                String description = roomType.getDescription() != null && !roomType.getDescription().isBlank()
                        ? roomType.getDescription()
                        : "Thông tin phòng đang được cập nhật.";
                String bedType = roomType.getBedType() != null && !roomType.getBedType().isBlank()
                        ? roomType.getBedType()
                        : "Tiện ích";
                String sizeText = roomType.getSizeM2() != null
                        ? roomType.getSizeM2().stripTrailingZeros().toPlainString()
                        : "-";
            %>
                <article class="room-showcase-card">
                    <img src="<%= escapeHtml(imageUrl) %>" alt="<%= escapeHtml(roomType.getName()) %>">
                    <div class="room-showcase-card__body">
                        <h3><%= escapeHtml(roomType.getName()) %></h3>
                        <p><%= escapeHtml(description) %></p>
                        <div class="room-meta">
                            <span><%= roomType.getAvailableQuantity() %> phòng trống</span>
                            <span><%= roomType.getCapacity() %> khách</span>
                            <span><%= "-".equals(sizeText) ? "-" : sizeText %> m2</span>
                            <span><%= escapeHtml(bedType) %></span>
                        </div>
                        <a class="btn" href="${pageContext.request.contextPath}/room-detail?id=<%= roomType.getId() %>">Xem chi tiết</a>
                    </div>
                </article>
            <% } %>
        <% } %>
    </section>

    <section class="section-head" style="margin-top: 50px;">
        <div><p class="section-kicker">Trải nghiệm thực tế</p><h2>Khách hàng nói gì về chúng tôi</h2></div>
    </section>
    <% if (featuredFeedbacks.isEmpty()) { %>
        <div style="text-align: center; padding: 36px 20px; background: #ffffff; border-radius: 12px; border: 1px solid var(--border-color, #e5e7eb); color: #64748b; margin-bottom: 40px;">
            <i class="far fa-comment-dots" style="font-size: 32px; color: #f59e0b; margin-bottom: 8px; display: block;"></i>
            <p style="margin: 0; font-size: 15px;">Chưa có đánh giá nào được hiển thị. Hãy trải nghiệm dịch vụ và chia sẻ đánh giá đầu tiên của bạn!</p>
        </div>
    <% } else { %>
    <section class="testimonials-grid">
        <% for (dao.FeedbackDao.FeedbackDto fb : featuredFeedbacks) { %>
            <div class="testimonial-card">
                <div>
                    <div class="feedback-star-display" style="margin-bottom: 12px;">
                        <% for (int s = 1; s <= fb.getRating(); s++) { %><i class="fas fa-star"></i><% } %>
                        <% for (int s = fb.getRating() + 1; s <= 5; s++) { %><i class="far fa-star feedback-star-empty"></i><% } %>
                    </div>
                    <p class="testimonial-quote">
                        "<%= escapeHtml(fb.getComment() != null && !fb.getComment().isBlank() ? fb.getComment() : (fb.getRating() >= 4 ? "Trải nghiệm dịch vụ rất tuyệt vời, phòng ốc sạch sẽ và nhân viên chu đáo!" : "Dịch vụ và tiện nghi phòng tốt.")) %>"
                    </p>
                </div>
                <div class="testimonial-author-row">
                    <strong class="testimonial-author-name"><%= escapeHtml(fb.getCustomerName() != null ? fb.getCustomerName() : "Khách lưu trú") %></strong>
                    <% if (fb.getRoomTypeNames() != null && !fb.getRoomTypeNames().isBlank()) { %>
                        <span class="testimonial-room-tag"><%= escapeHtml(fb.getRoomTypeNames()) %></span>
                    <% } %>
                </div>
            </div>
        <% } %>
    </section>
    <% } %>

    <% if (!latestNews.isEmpty()) { %>
    <section class="section-head" style="margin-top: 40px;">
        <div><p class="section-kicker">Tin tức</p><h2>Khuyến mãi &amp; Sự kiện</h2></div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/news">Xem tất cả</a>
    </section>
    <section class="news-card-grid">
        <% for (model.News n : latestNews) { 
            String rawThumb = n.getThumbnailUrl();
            String newsThumb;
            if (rawThumb != null && !rawThumb.isBlank()) {
                newsThumb = rawThumb.startsWith("/") ? (request.getContextPath() + rawThumb) : rawThumb;
            } else {
                newsThumb = "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80";
            }
        %>
        <article class="room-showcase-card">
            <img src="<%= escapeHtml(newsThumb) %>" alt="<%= escapeHtml(n.getTitle()) %>">
            <div class="room-showcase-card__body">
                <h3 style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"><%= escapeHtml(n.getTitle()) %></h3>
                <div class="room-meta" style="margin-top: 8px; color: #666; font-size: 14px;">
                    <span><%= n.getPublishedAt() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(n.getPublishedAt()) : "" %></span>
                </div>
                <a class="btn" href="${pageContext.request.contextPath}/news/detail?id=<%= n.getId() %>" style="margin-top: 16px;">Xem chi tiết</a>
            </div>
        </article>
        <% } %>
    </section>
    <% } %>
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
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/walk-in"><span>Quầy</span><h3>Đặt tại quầy</h3><p>Tạo đặt phòng trực tiếp cho khách walk-in.</p></a>
        <% } else if ("HOUSEKEEPING".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/tasks?view=mine"><span>Nhiệm vụ</span><h3>Công việc của tôi</h3><p>Kiểm tra phòng sau checkout, dọn dẹp và chuẩn bị phòng sạch.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/tasks?view=history"><span>Nhật ký</span><h3>Lịch sử dọn phòng</h3><p>Xem lại các công việc kiểm tra và dọn dẹp đã hoàn tất.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/issues"><span>Bảo trì</span><h3>Sự cố thiết bị</h3><p>Báo cáo hư hỏng và kiểm tra bảo trì thiết bị trong phòng.</p></a>
        <% } else if ("HOTEL_MANAGER".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/reports"><span>Báo cáo</span><h3>Báo cáo vận hành</h3><p>Theo dõi doanh thu, công suất phòng và hiệu suất hoạt động.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/rooms"><span>Phòng</span><h3>Quản lý phòng</h3><p>Xem sơ đồ phòng, trạng thái phòng và thiết bị phòng.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/housekeeping"><span>Dọn phòng</span><h3>Lịch sử dọn phòng</h3><p>Theo dõi tiến độ kiểm tra và công việc dọn dẹp phòng.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/issues"><span>Sự cố</span><h3>Sự cố thiết bị</h3><p>Theo dõi và nghiệm thu các thiết bị cần sửa chữa.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/feedbacks"><span>Đánh giá</span><h3>Đánh giá khách hàng</h3><p>Xem phản hồi khách hàng, quản lý hiển thị và chuyển tiếp sự cố.</p></a>
        <% } %>
        <% if (canAdminUsers) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/users">
                <span>Admin</span>
                <h3>Người dùng</h3>
                <p>Quản lý tài khoản nội bộ, trạng thái hoạt động và phân quyền cho user.</p>
            </a>
        <% } %>
        <% if (canAdminRoles) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/roles">
                <span>Phân quyền</span>
                <h3>Vai trò và quyền</h3>
                <p>Quản lý các vai trò và phân quyền chức năng trong hệ thống.</p>
            </a>
        <% } %>
        <% if (canAdminLogs) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/logs">
                <span>Kiểm tra</span>
                <h3>Nhật ký hệ thống</h3>
                <p>Theo dõi các thao tác quản trị quan trọng trong hệ thống.</p>
            </a>
        <% } %>
    </section>
</main>
<% } %>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
