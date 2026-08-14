<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="model.HousekeepingTask" %>
<%!
    private String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    private String label(String value) {
        if (value == null) return "—";
        switch (value) {
            case "Pending": return "Chờ xử lý";
            case "InProgress": return "Đang thực hiện";
            case "Blocked": return "Bị chặn";
            case "Completed": return "Hoàn thành";
            case "Cancelled": return "Đã hủy";
            case "Clean": return "Sạch";
            case "Dirty": return "Cần dọn";
            case "Cleaning": return "Đang dọn";
            case "Maintenance": return "Bảo trì";
            default: return value;
        }
    }
    private String date(java.util.Date value) {
        return value == null ? "—" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(value);
    }
%>
<%
    HousekeepingTask task = (HousekeepingTask) request.getAttribute("task");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Task phòng <%= esc(task.getRoomNumber()) %> | HMS</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page hk-detail-page">
    <a class="hk-back" href="<%= contextPath %>/housekeeping/tasks">← Quay lại danh sách</a>
    <div class="hk-detail-heading">
        <div><p class="hk-eyebrow">Housekeeping task #<%= task.getTaskId() %></p>
            <h1>Phòng <%= esc(task.getRoomNumber()) %></h1>
            <p><%= esc(task.getRoomTypeName()) %> · Tầng <%= task.getFloor() %></p>
        </div>
        <div class="hk-heading-badges">
            <span class="hk-badge room-<%= task.getRoomHousekeepingStatus().toLowerCase() %>"><%= label(task.getRoomHousekeepingStatus()) %></span>
            <span class="hk-badge task-<%= task.getStatus().toLowerCase() %>"><%= label(task.getStatus()) %></span>
        </div>
    </div>

    <section class="hk-detail-grid">
        <article class="hk-card">
            <h2>Thông tin công việc</h2>
            <dl>
                <div><dt>Task ID</dt><dd>#<%= task.getTaskId() %></dd></div>
                <div><dt>Tiến độ</dt><dd><%= label(task.getStatus()) %></dd></div>
                <div><dt>Nhân viên phụ trách</dt><dd><%= esc(task.getAssignedStaffName() == null ? "--" : task.getAssignedStaffName()) %></dd></div>
                <div><dt>Người hoàn thành</dt><dd><%= esc(task.getCompletedStaffName() == null ? "—" : task.getCompletedStaffName()) %></dd></div>
            </dl>
        </article>
        <article class="hk-card">
            <h2>Thông tin phòng</h2>
            <dl>
                <div><dt>Số phòng</dt><dd><%= esc(task.getRoomNumber()) %></dd></div>
                <div><dt>Loại phòng</dt><dd><%= esc(task.getRoomTypeName()) %></dd></div>
                <div><dt>Tầng</dt><dd><%= task.getFloor() %></dd></div>
                <div><dt>Housekeeping status</dt><dd><%= label(task.getRoomHousekeepingStatus()) %></dd></div>
            </dl>
        </article>
        <article class="hk-card hk-card-wide">
            <h2>Lịch sử thời gian</h2>
            <div class="hk-timeline">
                <div><span></span><strong>Tạo task</strong><time><%= date(task.getCreatedAt()) %></time></div>
                <div><span></span><strong>Bắt đầu</strong><time><%= date(task.getStartedAt()) %></time></div>
                <div><span></span><strong>Hoàn thành</strong><time><%= date(task.getCompletedAt()) %></time></div>
                <div><span></span><strong>Cập nhật gần nhất</strong><time><%= date(task.getUpdatedAt()) %></time></div>
            </div>
        </article>
        <article class="hk-card hk-card-wide">
            <h2>Ghi chú hoàn thành</h2>
            <p class="hk-note"><%= task.getCompletionNote() == null || task.getCompletionNote().isBlank()
                    ? "Chưa có ghi chú." : esc(task.getCompletionNote()) %></p>
        </article>
    </section>
    <p class="hk-readonly-note">Màn hình hiện ở chế độ chỉ xem. Chức năng cập nhật trạng thái chưa được triển khai.</p>
</main>
</body>
</html>
