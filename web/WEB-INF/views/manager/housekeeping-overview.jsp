<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="dao.HousekeepingDao" %>
<%@ page import="dao.HousekeepingDao.HousekeepingStats" %>
<%
    HousekeepingStats stats = (HousekeepingStats) request.getAttribute("stats");
    if (stats == null) {
        try {
            stats = new HousekeepingDao().getHousekeepingStats();
        } catch (Exception ex) {
            stats = new HousekeepingStats();
        }
    }
    String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tổng quan dọn phòng | HMS</title>
    <link rel="stylesheet" href="<%= cp %>/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/housekeeping.css?v=20260825-1">
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />
<main class="page-container room-management-page">
    <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
            <p class="room-management-kicker">QUẢN LÝ KHÁCH SẠN</p>
            <h1>Tổng quan dọn phòng</h1>
            <p>Theo dõi tiến độ dọn phòng, kiểm tra phòng sau checkout và khối lượng công việc của nhân viên vệ sinh.</p>
        </div>
        <div class="room-management-hero__actions">
            <a class="btn" href="<%= cp %>/manager/housekeeping">Danh sách công việc</a>
            <a class="btn btn-secondary" href="<%= cp %>/manager/issues">Sự cố thiết bị</a>
        </div>
    </section>

    <section class="kpi-grid">
        <div class="metric-card panel">
            <span>Chờ xử lý</span>
            <strong><%= stats.getPendingCount() %></strong>
            <small>Nhiệm vụ chưa có người làm</small>
        </div>
        <div class="metric-card panel">
            <span>Đang thực hiện</span>
            <strong><%= stats.getInProgressCount() %></strong>
            <small>Nhân viên đang dọn dẹp</small>
        </div>
        <div class="metric-card panel">
            <span>Hoàn thành hôm nay</span>
            <strong><%= stats.getCompletedTodayCount() %></strong>
            <small>Phòng sạch trong ngày</small>
        </div>
        <div class="metric-card panel">
            <span>Tổng hoàn tất</span>
            <strong><%= stats.getTotalCompletedCount() %></strong>
            <small>Toàn bộ lịch sử</small>
        </div>
    </section>

    <section class="room-management-panel panel">
        <h2>Điều hướng nhanh</h2>
        <p>Truy cập nhanh các chức năng nghiệp vụ dọn phòng của khách sạn.</p>
        <div class="flex-align-center">
            <a href="<%= cp %>/manager/housekeeping" class="btn btn-primary">📋 Quản lý lịch sử dọn phòng</a>
            <a href="<%= cp %>/manager/issues" class="btn btn-secondary">🛠️ Quản lý sự cố thiết bị</a>
            <a href="<%= cp %>/manager/rooms" class="btn btn-secondary">🏨 Sơ đồ &amp; Trạng thái phòng</a>
        </div>
    </section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>