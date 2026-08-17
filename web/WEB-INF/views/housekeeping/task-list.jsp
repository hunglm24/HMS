<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="model.HousekeepingTask" %>
<%@ page import="service.HousekeepingService" %>
<%!
    private String enc(Object value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
    private String query(HousekeepingService.TaskPage r, boolean includeSort) {
        StringBuilder q = new StringBuilder("view=").append(enc(r.view()));
        if (r.keyword() != null) q.append("&q=").append(enc(r.keyword()));
        if (r.floor() != null) q.append("&floor=").append(r.floor());
        if (r.taskType() != null) q.append("&taskType=").append(enc(r.taskType()));
        if (r.status() != null) q.append("&status=").append(enc(r.status()));
        if (includeSort) q.append("&sort=").append(enc(r.sort())).append("&direction=").append(enc(r.direction()));
        return q.toString();
    }
    private String sortUrl(HousekeepingService.TaskPage r, String column) {
        String next = column.equals(r.sort()) && "asc".equals(r.direction()) ? "desc" : "asc";
        return query(r, false) + "&sort=" + enc(column) + "&direction=" + next;
    }
    private String sortClass(HousekeepingService.TaskPage r, String column) {
        return column.equals(r.sort()) ? "sorted-" + r.direction() : "sortable";
    }
%>
<%
    HousekeepingService.TaskPage result = (HousekeepingService.TaskPage) request.getAttribute("result");
    String contextPath = request.getContextPath();
    boolean mine = "mine".equals(result.view());
    boolean history = "history".equals(result.view());
    boolean isManager = Boolean.TRUE.equals(request.getAttribute("isManager"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dọn phòng | HMS</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <section class="hk-hero">
        <div><p class="hk-eyebrow">Vận hành phòng</p><h1>Dọn phòng</h1>
            <p>Chọn công việc kiểm tra hoặc dọn phòng đang chờ nhận.</p></div>
        <div class="hk-total"><strong><%= result.totalItems() %></strong><span>kết quả</span></div>
    </section>
<!--
    <nav class="hk-tabs" aria-label="Nhóm công việc">
        <% if (!isManager) { %><a class="<%= !mine && !history ? "active" : "" %>" href="<%= contextPath %>/housekeeping/tasks?view=waiting">Task chờ nhận</a>
        <a class="<%= mine ? "active" : "" %>" href="<%= contextPath %>/housekeeping/tasks?view=mine">Task của tôi</a><% } %>
        <a class="<%= history ? "active" : "" %>" href="<%= contextPath %>/housekeeping/tasks?view=history">Lịch sử<%= isManager ? " toàn bộ" : " của tôi" %></a>
    </nav>-->

    <form class="hk-filters" method="get" action="<%= contextPath %>/housekeeping/tasks">
        <input type="hidden" name="view" value="<%= HousekeepingTask.esc(result.view()) %>">
        <label class="hk-search">Tìm kiếm
            <input type="search" name="q" maxlength="50" value="<%= HousekeepingTask.esc(result.keyword()) %>"
                   placeholder="Số phòng hoặc loại phòng">
        </label>
        <label>Tầng
            <input type="number" name="floor" min="0" max="999" value="<%= result.floor() == null ? "" : result.floor() %>" placeholder="Tất cả">
        </label>
        <% if (mine || history) { %>
        <label>Loại công việc
            <select name="taskType"><option value="">Tất cả</option>
                <option value="CHECKOUT_INSPECTION" <%= "CHECKOUT_INSPECTION".equals(result.taskType()) ? "selected" : "" %>>Kiểm tra phòng</option>
                <option value="CLEANING" <%= "CLEANING".equals(result.taskType()) ? "selected" : "" %>>Dọn phòng</option>
            </select>
        </label>
        <label>Trạng thái
            <select name="status"><option value="">Tất cả</option>
                <% if (!history) { %><option value="PENDING" <%= "PENDING".equals(result.status()) ? "selected" : "" %>>Chờ thực hiện</option>
                <option value="IN_PROGRESS" <%= "IN_PROGRESS".equals(result.status()) ? "selected" : "" %>>Đang thực hiện</option><% } else { %>
                <option value="COMPLETED" <%= "COMPLETED".equals(result.status()) ? "selected" : "" %>>Hoàn thành</option>
                <option value="CANCELLED" <%= "CANCELLED".equals(result.status()) ? "selected" : "" %>>Đã hủy</option><% } %>
            </select>
        </label>
        <% } %>
        <div class="hk-filter-actions"><button type="submit">Áp dụng</button>
            <a href="<%= contextPath %>/housekeeping/tasks?view=<%= enc(result.view()) %>">Đặt lại</a></div>
    </form>

    <% if (result.tasks().isEmpty()) { %>
    <section class="hk-empty"><span aria-hidden="true">✓</span><h2>Không có dữ liệu phù hợp</h2>
        <p>Hãy thử thay đổi từ khóa hoặc bộ lọc đang chọn.</p></section>
    <% } else { %>
    <div class="hk-table-wrap"><table class="hk-table">
        <thead><tr>
            <th class="<%= sortClass(result,"room") %>"><a href="?<%= sortUrl(result,"room") %>">Phòng</a></th>
            <th class="<%= sortClass(result,"roomType") %>"><a href="?<%= sortUrl(result,"roomType") %>">Loại phòng</a></th>
            <th class="<%= sortClass(result,"floor") %>"><a href="?<%= sortUrl(result,"floor") %>">Tầng</a></th>
            <th class="<%= sortClass(result,"taskType") %>"><a href="?<%= sortUrl(result,"taskType") %>">Công việc</a></th>
            <th class="<%= sortClass(result,"status") %>"><a href="?<%= sortUrl(result,"status") %>">Trạng thái</a></th>
            <th><span class="sr-only">Thao tác</span></th>
        </tr></thead>
        <tbody><% for (HousekeepingTask task : result.tasks()) { %><tr>
            <td data-label="Phòng"><span class="hk-room-number"><%= HousekeepingTask.esc(task.getRoomNumber()) %></span></td>
            <td data-label="Loại phòng"><%= HousekeepingTask.esc(task.getRoomTypeName()) %></td>
            <td data-label="Tầng"><%= task.getFloorNumber() == null ? "--" : task.getFloorNumber() %></td>
            <td data-label="Công việc"><%= task.getTaskTypeLabel() %></td>
            <td data-label="Trạng thái"><span class="hk-badge task-<%= task.getStatus().toLowerCase() %>"><%= task.getStatusLabel() %></span></td>
            <td class="hk-row-action"><% if (mine || history) { %>
                <a href="<%= contextPath %>/housekeeping/tasks/detail?id=<%= task.getTaskId() %>">Xem chi tiết</a>
            <% } else if ("CLEANING".equals(task.getTaskType())) { %>
                <form method="post" action="<%= contextPath %>/housekeeping/tasks/claim-cleaning">
                    <input type="hidden" name="taskId" value="<%= task.getTaskId() %>">
                    <button type="submit">Nhận dọn phòng</button>
                </form>
            <% } else { %><form method="post" action="<%= contextPath %>/housekeeping/tasks/claim">
                    <input type="hidden" name="bookingRoomId" value="<%= task.getBookingRoomId() %>">
                    <button type="submit">Nhận kiểm tra</button>
                </form><% } %></td>
        </tr><% } %></tbody>
    </table></div>

    <% if (result.totalPages() > 1) { %><nav class="hk-pagination" aria-label="Phân trang">
        <% if (result.page() > 1) { %><a href="?<%= query(result,true) %>&page=<%= result.page()-1 %>">‹ Trước</a><% } else { %><span>‹ Trước</span><% } %>
        <% for (int i = 1; i <= result.totalPages(); i++) { %>
            <% if (i == result.page()) { %><strong><%= i %></strong><% } else { %><a href="?<%= query(result,true) %>&page=<%= i %>"><%= i %></a><% } %>
        <% } %>
        <% if (result.page() < result.totalPages()) { %><a href="?<%= query(result,true) %>&page=<%= result.page()+1 %>">Sau ›</a><% } else { %><span>Sau ›</span><% } %>
    </nav><% } %>
    <% } %>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body></html>
