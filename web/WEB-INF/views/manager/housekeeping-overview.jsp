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
    <title>Tổng quan buồng phòng | HMS</title>
    <link rel="stylesheet" href="<%= cp %>/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/housekeeping.css?v=20260820-7">
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container room-management-page">
    <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
            <p class="room-management-kicker">QUẢN LÝ KHÁCH SẠN</p>
            <h1>Tổng quan buồng phòng</h1>
            <p>Theo dõi tiến độ dọn phòng, kiểm tra phòng sau checkout và khối lượng công việc của nhân viên vệ sinh.</p>
        </div>
        <div class="room-management-hero__actions">
            <a class="btn" href="<%= cp %>/manager/housekeeping">Danh sách công việc</a>
            <a class="btn btn-secondary" href="<%= cp %>/manager/issues">Sự cố thiết bị</a>
        </div>
    </section>

    <section class="kpi-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px;">
        <div class="metric-card panel" style="padding: 20px; border-radius: 12px; background: #fff; border: 1px solid var(--color-border);">
            <span style="color: #64748b; font-size: 13px; font-weight: 600; text-transform: uppercase;">Chờ xử lý</span>
            <strong style="display: block; font-size: 2rem; color: #d97706; margin-top: 8px;"><%= stats.getPendingCount() %></strong>
            <small style="color: #94a3b8; font-size: 12px;">Nhiệm vụ chưa có người làm</small>
        </div>
        <div class="metric-card panel" style="padding: 20px; border-radius: 12px; background: #fff; border: 1px solid var(--color-border);">
            <span style="color: #64748b; font-size: 13px; font-weight: 600; text-transform: uppercase;">Đang thực hiện</span>
            <strong style="display: block; font-size: 2rem; color: #2563eb; margin-top: 8px;"><%= stats.getInProgressCount() %></strong>
            <small style="color: #94a3b8; font-size: 12px;">Nhân viên đang dọn dẹp</small>
        </div>
        <div class="metric-card panel" style="padding: 20px; border-radius: 12px; background: #fff; border: 1px solid var(--color-border);">
            <span style="color: #64748b; font-size: 13px; font-weight: 600; text-transform: uppercase;">Hoàn thành hôm nay</span>
            <strong style="display: block; font-size: 2rem; color: #16a34a; margin-top: 8px;"><%= stats.getCompletedTodayCount() %></strong>
            <small style="color: #94a3b8; font-size: 12px;">Phòng sạch trong ngày</small>
        </div>
        <div class="metric-card panel" style="padding: 20px; border-radius: 12px; background: #fff; border: 1px solid var(--color-border);">
            <span style="color: #64748b; font-size: 13px; font-weight: 600; text-transform: uppercase;">Tổng hoàn tất</span>
            <strong style="display: block; font-size: 2rem; color: #475569; margin-top: 8px;"><%= stats.getTotalCompletedCount() %></strong>
            <small style="color: #94a3b8; font-size: 12px;">Toàn bộ lịch sử</small>
        </div>
    </section>

    <section class="room-management-panel panel" style="padding: 24px;">
        <h2 style="margin: 0 0 8px 0; font-size: 1.25rem; color: #1e293b;">Điều hướng nhanh</h2>
        <p style="color: #64748b; margin: 0 0 20px 0;">Truy cập nhanh các chức năng nghiệp vụ buồng phòng của khách sạn.</p>
        <div style="display: flex; gap: 12px; flex-wrap: wrap;">
            <a href="<%= cp %>/manager/housekeeping" class="btn btn-primary" style="text-decoration: none;">📋 Quản lý lịch sử dọn phòng</a>
            <a href="<%= cp %>/manager/issues" class="btn btn-secondary" style="text-decoration: none;">🛠️ Quản lý sự cố thiết bị</a>
            <a href="<%= cp %>/manager/rooms" class="btn btn-secondary" style="text-decoration: none;">🏨 Sơ đồ & Trạng thái phòng</a>
        </div>
    </section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>