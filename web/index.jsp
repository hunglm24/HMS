<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<% User dashboardUser = (User) session.getAttribute("currentUser");
   String dashboardRole = dashboardUser == null ? "" : dashboardUser.getRoleName();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main id="main-content" class="page-container">
        <% if (dashboardUser == null) { %>
            <h1>Chào mừng đến HMS</h1>
            <p>Tìm phòng phù hợp hoặc đăng nhập để quản lý các đặt phòng của bạn.</p>
        <% } else { %>
            <h1>Xin chào, ${sessionScope.currentUser.fullName}</h1>
            <p>Chọn một nghiệp vụ để bắt đầu làm việc.</p>
        <% } %>
        <section class="action-grid" aria-label="Thao tác nhanh">
            <% if (dashboardUser == null) { %>
                <a class="action-card" href="${pageContext.request.contextPath}/search"><strong>Tìm phòng</strong><span>Tìm kiếm theo ngày ở, số khách và loại phòng.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/login"><strong>Đăng nhập</strong><span>Truy cập tài khoản và các tính năng cá nhân.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/register"><strong>Đăng ký</strong><span>Tạo tài khoản khách hàng HMS.</span></a>
            <% } else if ("CUSTOMER".equalsIgnoreCase(dashboardRole)) { %>
                <a class="action-card" href="${pageContext.request.contextPath}/search"><strong>Tìm và đặt phòng</strong><span>Tìm phòng theo ngày, số khách và loại phòng.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/my-bookings"><strong>Đặt phòng của tôi</strong><span>Xem trạng thái, lịch sử hoặc hủy đặt phòng.</span></a>
            <% } else if ("RECEPTIONIST".equalsIgnoreCase(dashboardRole)) { %>
                <a class="action-card" href="${pageContext.request.contextPath}/reception/walk-in"><strong>Đặt phòng tại quầy</strong><span>Tạo booking cho khách đến trực tiếp.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/reception/check-in"><strong>Nhận phòng</strong><span>Xác minh khách và gán phòng thực tế.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/reception/check-out"><strong>Trả phòng</strong><span>Tổng hợp chi phí, thanh toán và xuất hóa đơn.</span></a>
            <% } else if ("HOUSEKEEPING".equalsIgnoreCase(dashboardRole)) { %>
                <a class="action-card" href="${pageContext.request.contextPath}/housekeeping/tasks"><strong>Phòng cần xử lý</strong><span>Nhận việc và cập nhật Dirty → Cleaning → Clean.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/housekeeping/issues"><strong>Báo cáo sự cố</strong><span>Ghi nhận vấn đề phòng hoặc thiết bị.</span></a>
            <% } else if ("HOTEL_MANAGER".equalsIgnoreCase(dashboardRole)) { %>
                <a class="action-card" href="${pageContext.request.contextPath}/manager/reports"><strong>Báo cáo vận hành</strong><span>Xem công suất phòng, doanh thu và thống kê.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/manager/pricing"><strong>Giá và chính sách</strong><span>Cấu hình giá, giảm giá và chính sách hủy.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/manager/staff"><strong>Quản lý nhân viên</strong><span>Xem và cập nhật thông tin nhân sự.</span></a>
            <% } else if ("ADMIN".equalsIgnoreCase(dashboardRole)) { %>
                <a class="action-card" href="${pageContext.request.contextPath}/admin/users"><strong>Quản lý người dùng</strong><span>Tạo, khóa và cập nhật tài khoản.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/admin/roles"><strong>Vai trò và quyền</strong><span>Phân quyền truy cập theo RBAC.</span></a>
                <a class="action-card" href="${pageContext.request.contextPath}/admin/logs"><strong>Nhật ký hệ thống</strong><span>Kiểm tra lịch sử thao tác và sự kiện.</span></a>
            <% } %>
        </section>
<<<<<<< Updated upstream
    </main>
=======
    <% } %>

    <section class="dashboard-grid">
        <% if ("RECEPTIONIST".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/bookings"><span>Lễ tân</span><h3>Booking</h3><p>Quản lý đặt phòng, check-in và check-out.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/room-map"><span>Phòng</span><h3>Sơ đồ phòng</h3><p>Xem nhanh tình trạng phòng theo tầng.</p></a>
        <% } else if ("HOUSEKEEPING".equalsIgnoreCase(role)) { %>
            <div style="display: flex; gap: 16px; flex-wrap: wrap; grid-column: 1 / -1;">
                <a class="btn btn-primary" style="text-decoration: none; padding: 12px 24px; font-size: 16px;" href="${pageContext.request.contextPath}/housekeeping/tasks">Nhận task phòng</a>
                <a class="btn btn-secondary" style="text-decoration: none; padding: 12px 24px; font-size: 16px;" href="${pageContext.request.contextPath}/housekeeping/issues">Báo cáo &amp; quản lý sự cố</a>
            </div>
        <% } else if ("HOTEL_MANAGER".equalsIgnoreCase(role)) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/bookings"><span>Booking</span><h3>Quản lý booking</h3><p>Theo dõi, xác nhận, hủy booking và tạo yêu cầu hoàn tiền.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/reports"><span>Báo cáo</span><h3>Báo cáo</h3><p>Theo dõi doanh thu, công suất phòng và nhân sự.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/rooms"><span>Phòng</span><h3>Quản lý phòng</h3><p>Quản lý phòng vật lý.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/room-types"><span>Loại phòng</span><h3>Quản lý loại phòng</h3><p>Quản lý hạng phòng, giá và sức chứa.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/tasks?view=history"><span>Buồng phòng</span><h3>Nhiệm vụ dọn phòng</h3><p>Theo dõi lịch sử và tiến độ dọn phòng.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/news"><span>Tin tức</span><h3>Quản lý tin tức</h3><p>Thêm, sửa, xóa các chương trình khuyến mãi.</p></a>
        <% } %>
        <% if (canAdminUsers) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/users">
                <span>Admin</span>
                <h3>Người dùng</h3>
                <p>Quản lý tài khoản nội bộ, trạng thái hoạt động và phân role cho user.</p>
            </a>
        <% } %>
        <% if (canAdminRoles) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/roles">
                <span>Phân quyền</span>
                <h3>Vai trò và quyền</h3>
                <p>Quản lý các role ngoài ADMIN và cập nhật quyền khi cần chỉnh sửa.</p>
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
>>>>>>> Stashed changes
</body>
</html>
