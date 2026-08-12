<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.HousekeepingTask" %>
<%@ page import="model.User" %>
<%@ page import="service.HousekeepingService" %>
<%!
    private String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    private String label(String value) {
        if (value == null) return "Chưa phân công";
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
    private String nextDirection(HousekeepingService.TaskPage result, String column) {
        return column.equals(result.sort()) && "asc".equals(result.direction()) ? "desc" : "asc";
    }
    private String sortClass(HousekeepingService.TaskPage result, String column) {
        if (!column.equals(result.sort())) return "sortable";
        return "sortable sorted-" + result.direction();
    }
%>
<%
    HousekeepingService.TaskPage result = (HousekeepingService.TaskPage) request.getAttribute("result");
    boolean isManager = Boolean.TRUE.equals(request.getAttribute("isManager"));
    List<User> staff = (List<User>) request.getAttribute("housekeepingStaff");
    String filterQuery = (String) request.getAttribute("filterQuery");
    String baseFilterQuery = (String) request.getAttribute("baseFilterQuery");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh sách Housekeeping | HMS</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <div class="hk-heading">
        <div>
            <p class="hk-eyebrow">Housekeeping</p>
            <h1>Danh sách công việc</h1>
            <p>Các phòng chờ xử lý hoặc đang được nhân viên phụ trách.</p>
        </div>
        <div class="hk-total"><strong><%= result.totalItems() %></strong><span>công việc</span></div>
    </div>

    <form class="hk-filters" method="get" action="<%= contextPath %>/housekeeping/tasks">
        <label class="hk-search">Tìm kiếm
            <input type="search" name="q" maxlength="50" value="<%= esc(result.keyword()) %>"
                   placeholder="Số phòng hoặc loại phòng">
        </label>
        <label>Tiến độ
            <select name="taskStatus">
                <option value="">Tất cả</option>
                <% for (String status : new String[]{"Pending", "InProgress", "Blocked"}) { %>
                    <option value="<%= status %>" <%= status.equals(result.taskStatus()) ? "selected" : "" %>><%= label(status) %></option>
                <% } %>
            </select>
        </label>
        <label>Trạng thái phòng
            <select name="roomStatus">
                <option value="">Tất cả</option>
                <% for (String status : new String[]{"Clean", "Dirty", "Cleaning", "Maintenance"}) { %>
                    <option value="<%= status %>" <%= status.equals(result.roomStatus()) ? "selected" : "" %>><%= label(status) %></option>
                <% } %>
            </select>
        </label>
        <% if (isManager) { %>
        <label>Nhân viên
            <select name="assignedTo">
                <option value="">Tất cả</option>
                <% if (staff != null) for (User user : staff) { %>
                    <option value="<%= user.getUserId() %>"
                        <%= result.assignedTo() != null && result.assignedTo() == user.getUserId() ? "selected" : "" %>><%= esc(user.getFullName()) %></option>
                <% } %>
            </select>
        </label>
        <% } %>
        <label>Sắp xếp
            <select name="sort">
                <option value="created" <%= "created".equals(result.sort()) ? "selected" : "" %>>Ngày tạo</option>
                <option value="updated" <%= "updated".equals(result.sort()) ? "selected" : "" %>>Cập nhật gần nhất</option>
            </select>
        </label>
        <input type="hidden" name="direction" value="<%= esc(result.direction()) %>">
        <div class="hk-filter-actions">
            <button type="submit">Áp dụng</button>
            <a href="<%= contextPath %>/housekeeping/tasks">Đặt lại</a>
        </div>
    </form>

    <% if (result.tasks().isEmpty()) { %>
        <section class="hk-empty">
            <span aria-hidden="true">✓</span>
            <h2>Không tìm thấy công việc</h2>
            <p>Thử thay đổi từ khóa hoặc bộ lọc đang chọn.</p>
        </section>
    <% } else { %>
        <div class="hk-table-wrap">
            <table class="hk-table">
                <thead><tr>
                    <th class="<%= sortClass(result, "room") %>"><a href="<%= contextPath %>/housekeeping/tasks?<%= esc(baseFilterQuery) %>&sort=room&direction=<%= nextDirection(result, "room") %>">Phòng</a></th>
                    <th class="<%= sortClass(result, "roomType") %>"><a href="<%= contextPath %>/housekeeping/tasks?<%= esc(baseFilterQuery) %>&sort=roomType&direction=<%= nextDirection(result, "roomType") %>">Loại phòng</a></th>
                    <th class="<%= sortClass(result, "floor") %>"><a href="<%= contextPath %>/housekeeping/tasks?<%= esc(baseFilterQuery) %>&sort=floor&direction=<%= nextDirection(result, "floor") %>">Tầng</a></th>
                    <th class="<%= sortClass(result, "roomStatus") %>"><a href="<%= contextPath %>/housekeeping/tasks?<%= esc(baseFilterQuery) %>&sort=roomStatus&direction=<%= nextDirection(result, "roomStatus") %>">Trạng thái phòng</a></th>
                    <th class="<%= sortClass(result, "taskStatus") %>"><a href="<%= contextPath %>/housekeeping/tasks?<%= esc(baseFilterQuery) %>&sort=taskStatus&direction=<%= nextDirection(result, "taskStatus") %>">Tiến độ</a></th>
                    <th class="<%= sortClass(result, "staff") %>"><a href="<%= contextPath %>/housekeeping/tasks?<%= esc(baseFilterQuery) %>&sort=staff&direction=<%= nextDirection(result, "staff") %>">Nhân viên</a></th>
                    <th></th>
                </tr></thead>
                <tbody>
                <% for (HousekeepingTask task : result.tasks()) { %>
                    <tr>
                        <td data-label="Phòng"><strong><%= esc(task.getRoomNumber()) %></strong></td>
                        <td data-label="Loại phòng"><%= esc(task.getRoomTypeName()) %></td>
                        <td data-label="Tầng"><%= task.getFloor() %></td>
                        <td data-label="Trạng thái phòng"><span class="hk-badge room-<%= task.getRoomHousekeepingStatus().toLowerCase() %>"><%= label(task.getRoomHousekeepingStatus()) %></span></td>
                        <td data-label="Tiến độ"><span class="hk-badge task-<%= task.getStatus().toLowerCase() %>"><%= label(task.getStatus()) %></span></td>
                        <td data-label="Nhân viên"><%= esc(task.getAssignedStaffName() == null ? "--" : task.getAssignedStaffName()) %></td>
                        <td class="hk-row-action"><a href="<%= contextPath %>/housekeeping/tasks/detail?id=<%= task.getTaskId() %>">Xem chi tiết</a></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>

        <% if (result.totalPages() > 1) { %>
        <nav class="hk-pagination" aria-label="Phân trang">
            <% if (result.page() > 1) { %>
                <a href="<%= contextPath %>/housekeeping/tasks?<%= esc(filterQuery) %>&page=<%= result.page() - 1 %>">‹ Trước</a>
            <% } else { %><span>‹ Trước</span><% } %>
            <strong>Trang <%= result.page() %> / <%= result.totalPages() %></strong>
            <% if (result.page() < result.totalPages()) { %>
                <a href="<%= contextPath %>/housekeeping/tasks?<%= esc(filterQuery) %>&page=<%= result.page() + 1 %>">Sau ›</a>
            <% } else { %><span>Sau ›</span><% } %>
        </nav>
        <% } %>
    <% } %>
</main>
</body>
</html>
