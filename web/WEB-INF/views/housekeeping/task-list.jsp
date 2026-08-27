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
    private String sortUrl(HousekeepingService.TaskPage r, String column, String baseUrl) {
        String next = column.equals(r.sort()) && "asc".equals(r.direction()) ? "desc" : "asc";
        return baseUrl + "?" + query(r, false) + "&sort=" + enc(column) + "&direction=" + next;
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
    String pageTitle = isManager || history ? "Lịch sử dọn phòng" : "Task dọn phòng của tôi";
    String pageSubtitle = isManager ? "Theo dõi tiến độ và lịch sử công việc kiểm tra, dọn dẹp phòng." : history ? "Theo dõi lịch sử kiểm tra và dọn phòng đã hoàn tất." : "Danh sách công việc kiểm tra và dọn phòng được phân công.";
    String baseUrl = isManager ? contextPath + "/manager/housekeeping" : contextPath + "/housekeeping/tasks";
    
    boolean hasFilter = (result.keyword() != null && !result.keyword().isBlank()) 
                     || result.floor() != null 
                     || (result.taskType() != null && !result.taskType().isBlank())
                     || (result.status() != null && !result.status().isBlank());
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= pageTitle %> | HMS</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css?v=20260821-1">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/rooms.css?v=20260821-1">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css?v=20260825-1">
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container hk-page">
    <section class="hk-hero">
        <div>
            <p class="hk-eyebrow"><%= isManager ? "Quản lý khách sạn" : "Vận hành phòng" %></p>
            <h1><%= pageTitle %></h1>
            <p><%= pageSubtitle %></p>
        </div>
        <div class="hk-total"><strong><%= result.totalItems() %></strong><span>kết quả</span></div>
    </section>

    <form class="hk-filters" method="get" action="<%= baseUrl %>">
        <input type="hidden" name="view" value="<%= HousekeepingTask.esc(result.view()) %>">
        <label class="hk-search">Tìm kiếm
            <input type="search" name="q" maxlength="50" value="<%= HousekeepingTask.esc(result.keyword()) %>"
                   placeholder="Số phòng hoặc loại phòng">
        </label>
        <label>Tầng
            <select name="floor">
                <option value="" <%= result.floor() == null ? "selected" : "" %>>Tất cả tầng</option>
                <% 
                    java.util.List<Integer> floorOpts = (java.util.List<Integer>) request.getAttribute("floorOptions");
                    if (floorOpts == null || floorOpts.isEmpty()) floorOpts = java.util.List.of(1, 2, 3);
                    for (Integer f : floorOpts) { 
                %>
                    <option value="<%= f %>" <%= result.floor() != null && result.floor().equals(f) ? "selected" : "" %>>Tầng <%= f %></option>
                <% } %>
            </select>
        </label>
        <label>Loại công việc
            <select name="taskType"><option value="">Tất cả loại công việc</option>
                <option value="CHECKOUT_INSPECTION" <%= "CHECKOUT_INSPECTION".equals(result.taskType()) ? "selected" : "" %>>Kiểm tra phòng</option>
                <option value="CLEANING" <%= "CLEANING".equals(result.taskType()) ? "selected" : "" %>>Dọn phòng</option>
            </select>
        </label>
        <label>Trạng thái
            <select name="status"><option value="">Tất cả trạng thái</option>
                <% if (isManager) { %>
                <option value="PENDING" <%= "PENDING".equals(result.status()) ? "selected" : "" %>>Chờ thực hiện</option>
                <option value="IN_PROGRESS" <%= "IN_PROGRESS".equals(result.status()) ? "selected" : "" %>>Đang thực hiện</option>
                <option value="COMPLETED" <%= "COMPLETED".equals(result.status()) ? "selected" : "" %>>Hoàn thành</option>
                <% } else if (mine) { %>
                <option value="PENDING" <%= "PENDING".equals(result.status()) ? "selected" : "" %>>Chờ thực hiện</option>
                <option value="IN_PROGRESS" <%= "IN_PROGRESS".equals(result.status()) ? "selected" : "" %>>Đang thực hiện</option>
                <% } else { %>
                <option value="COMPLETED" <%= "COMPLETED".equals(result.status()) ? "selected" : "" %>>Hoàn thành</option>
                <% } %>
            </select>
        </label>
        <div class="hk-filter-actions">
            <button type="submit">Lọc</button>
            <a href="<%= baseUrl %><%= isManager ? "" : "?view=" + enc(result.view()) %>">Đặt lại</a>
        </div>
    </form>

    <% if (result.tasks().isEmpty()) { %>
    <section class="hk-empty">
        <span aria-hidden="true">🧹</span>
        <% if (hasFilter) { %>
            <h2>Không tìm thấy kết quả phù hợp</h2>
            <p>Không có công việc nào khớp với bộ lọc hoặc từ khóa tìm kiếm của bạn.</p>
        <% } else if (mine) { %>
            <h2>Hiện tại bạn không có công việc nào</h2>
            <p>Bạn đã hoàn thành hết các nhiệm vụ hoặc chưa có công việc mới được phân công.</p>
        <% } else { %>
            <h2>Chưa có công việc nào trong danh sách</h2>
            <p>Hiện tại chưa có công việc dọn dẹp hoặc kiểm tra phòng nào được ghi nhận.</p>
        <% } %>
    </section>
    <% } else { %>
    <div class="hk-table-wrap" data-pagination-root data-pagination-key="task-list-table" data-pagination-size="5">
        <table class="hk-table">
            <thead>
                <tr>
                    <th class="<%= sortClass(result,"room") %>"><a href="<%= sortUrl(result,"room", baseUrl) %>">Phòng</a></th>
                    <th class="<%= sortClass(result,"roomType") %>"><a href="<%= sortUrl(result,"roomType", baseUrl) %>">Loại phòng</a></th>
                    <th class="<%= sortClass(result,"floor") %>"><a href="<%= sortUrl(result,"floor", baseUrl) %>">Tầng</a></th>
                    <th class="<%= sortClass(result,"taskType") %>"><a href="<%= sortUrl(result,"taskType", baseUrl) %>">Công việc</a></th>
                    <th class="<%= sortClass(result,"time") %>"><a href="<%= sortUrl(result,"time", baseUrl) %>">Thời gian</a></th>
                    <% if (isManager) { %>
                    <th class="<%= sortClass(result,"assigned_to") %>"><a href="<%= sortUrl(result,"assigned_to", baseUrl) %>">Người kiểm tra</a></th>
                    <% } %>
                    <th class="<%= sortClass(result,"status") %>"><a href="<%= sortUrl(result,"status", baseUrl) %>">Trạng thái</a></th>
                    <th><span class="sr-only">Thao tác</span></th>
                </tr>
            </thead>
            <tbody>
                <% for (HousekeepingTask task : result.tasks()) { %>
                <tr data-pagination-item>
                    <td data-label="Phòng"><span class="hk-room-number"><%= HousekeepingTask.esc(task.getRoomNumber()) %></span></td>
                    <td data-label="Loại phòng"><%= HousekeepingTask.esc(task.getRoomTypeName()) %></td>
                    <td data-label="Tầng"><%= task.getFloorNumber() == null ? "--" : task.getFloorNumber() %></td>
                    <td data-label="Công việc">
                        <strong><%= task.getTaskTypeLabel() %></strong>
                        <% if (task.getNote() != null && !task.getNote().isBlank()) { %>
                        <br><small style="color: #64748b; font-size: 12px;"><%= HousekeepingTask.esc(task.getNote().length() > 60 ? task.getNote().substring(0, 57) + "..." : task.getNote()) %></small>
                        <% } %>
                    </td>
                    <td data-label="Thời gian">
                        <span title="Tạo lúc: <%= task.getFormattedCreatedAt() %><%= task.getCompletedAt() != null ? " | Hoàn tất: " + task.getFormattedCompletedAt() : "" %>">
                            <%= task.getFormattedDate() %>
                        </span>
                    </td>
                    <% if (isManager) { %>
                    <td data-label="Người kiểm tra">
                        <%= task.getAssignedStaffName() != null ? HousekeepingTask.esc(task.getAssignedStaffName()) : "<span style='color:#94a3b8; font-style:italic;'>Chưa phân công</span>" %>
                    </td>
                    <% } %>
                    <td data-label="Trạng thái"><span class="hk-badge task-<%= task.getStatus().toLowerCase() %>"><%= task.getStatusLabel() %></span></td>
                    <td class="hk-row-action">
                        <a href="<%= isManager ? contextPath + "/manager/housekeeping/detail" : contextPath + "/housekeeping/tasks/detail" %>?id=<%= task.getTaskId() %>">Xem chi tiết</a>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
        <div class="room-management-pagination" data-pagination-controls data-pagination-target="task-list-table"></div>
    </div>
    <% } %>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
<script src="<%= contextPath %>/assets/js/pagination.js?v=20260820-7"></script>
</body>
</html>
