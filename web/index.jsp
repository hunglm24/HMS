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
    </main>
</body>
</html>
