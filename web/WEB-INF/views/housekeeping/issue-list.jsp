<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="model.HousekeepingTask" %>
<%!
    private String enc(Object value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(String.valueOf(value), java.nio.charset.StandardCharsets.UTF_8);
    }
    private String query(String search, String floor, String taskType, String status, boolean includeSort, String sort, String direction) {
        StringBuilder q = new StringBuilder();
        if (search != null && !search.isEmpty()) q.append("search=").append(enc(search)).append("&");
        if (floor != null && !floor.isEmpty()) q.append("floor=").append(enc(floor)).append("&");
        if (taskType != null && !taskType.isEmpty()) q.append("taskType=").append(enc(taskType)).append("&");
        if (status != null && !status.isEmpty()) q.append("status=").append(enc(status)).append("&");
        if (includeSort) q.append("sort=").append(enc(sort)).append("&direction=").append(enc(direction));
        else if (q.length() > 0) q.setLength(q.length() - 1);
        return q.toString();
    }
    private String sortUrl(String search, String floor, String taskType, String status, String currentSort, String currentDir, String column, String baseUrl) {
        String next = column.equals(currentSort) && "asc".equals(currentDir) ? "desc" : "asc";
        String base = query(search, floor, taskType, status, false, "", "");
        return baseUrl + (base.isEmpty() ? "?" : "?" + base + "&") + "sort=" + enc(column) + "&direction=" + next;
    }
    private String sortClass(String currentSort, String currentDir, String column) {
        return column.equals(currentSort) ? "sorted-" + currentDir : "sortable";
    }
%>
<%
    String searchStr = (String) request.getAttribute("search");
    String floorStr = (String) request.getAttribute("floor");
    String taskTypeStr = (String) request.getAttribute("taskType");
    String statusStr = (String) request.getAttribute("status");
    String currentSort = (String) request.getAttribute("currentSort");
    String currentDir = (String) request.getAttribute("currentDir");
    String contextPath = request.getContextPath();
    Boolean isMgrAttr = (Boolean) request.getAttribute("isManager");
    boolean isManager = Boolean.TRUE.equals(isMgrAttr);
    String baseUrl = contextPath + (isManager ? "/manager/issues" : "/housekeeping/issues");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sự cố thiết bị | HMS</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css?v=20260820-7">
    <style>
        .btn-verify-action { background: #2563eb; color: #fff; border: none; padding: 6px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; }
        .btn-verify-action:hover { background: #1d4ed8; }
        .btn-history-action { background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; padding: 6px 14px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; }
        .btn-history-action:hover { background: #e2e8f0; color: #0f172a; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <section class="hk-hero">
        <div>
            <p class="hk-eyebrow"><%= isManager ? "Quản lý khách sạn" : "Vận hành phòng" %></p>
            <h1>Sự cố thiết bị</h1>
            <p>Theo dõi và cập nhật trạng thái thiết bị cần bảo trì, sửa chữa.</p>
        </div>
        <div>
            <a href="<%= contextPath %><%= isManager ? "/manager/issues/report" : "/housekeeping/issues/report" %>" class="hk-primary" style="display:inline-block; padding: 10px 20px; text-decoration: none;">Báo cáo sự cố mới</a>
        </div>
    </section>
        
    <c:if test="${not empty sessionScope.successMessage}">
        <div class="alert alert-success">${sessionScope.successMessage}</div>
        <c:remove var="successMessage" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
        <div class="alert alert-danger">${sessionScope.errorMessage}</div>
        <c:remove var="errorMessage" scope="session"/>
    </c:if>

    <form method="get" action="<%= baseUrl %>" class="hk-filters">
        <label class="hk-search">Tìm kiếm
            <input type="search" name="search" maxlength="50" value="<c:out value='${search}'/>" placeholder="Số phòng, thiết bị..">
        </label>
        <label>Tầng
            <input type="number" name="floor" min="0" max="999" value="<c:out value='${floor}'/>" placeholder="Tất cả">
        </label>
        <label>Loại công việc
            <select name="taskType">
                <option value="">Tất cả loại sự cố</option>
                <option value="EQUIPMENT_REPAIR" ${taskType eq 'EQUIPMENT_REPAIR' ? 'selected' : ''}>Sửa chữa thiết bị</option>
                <option value="MAINTENANCE_CHECK" ${taskType eq 'MAINTENANCE_CHECK' ? 'selected' : ''}>Kiểm tra bảo trì</option>
                <option value="EQUIPMENT_REPLACEMENT" ${taskType eq 'EQUIPMENT_REPLACEMENT' ? 'selected' : ''}>Thay thế thiết bị</option>
            </select>
        </label>
        <label>Trạng thái
            <select name="status">
                <option value="">Tất cả trạng thái</option>
                <option value="PENDING" ${status eq 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                <option value="IN_PROGRESS" ${status eq 'IN_PROGRESS' ? 'selected' : ''}>Đang bảo trì</option>
                <option value="COMPLETED" ${status eq 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
            </select>
        </label>
        <div class="hk-filter-actions">
            <button type="submit">Lọc</button>
            <a href="<%= baseUrl %>">Đặt lại</a>
        </div>
    </form>

    <div class="hk-table-wrap" data-pagination-root data-pagination-key="issue-list-table" data-pagination-size="5">
        <table class="hk-table">
            <thead>
                <tr>
                    <th class="<%= sortClass(currentSort, currentDir, "id") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "id", baseUrl) %>">ID</a></th>
                    <th class="<%= sortClass(currentSort, currentDir, "room") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "room", baseUrl) %>">Phòng</a></th>
                    <th class="<%= sortClass(currentSort, currentDir, "type") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "type", baseUrl) %>">Loại Task</a></th>
                    <th>Mô tả</th>
                    <th class="<%= sortClass(currentSort, currentDir, "created_at") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "created_at", baseUrl) %>">Thời gian báo cáo</a></th>
                    <th class="<%= sortClass(currentSort, currentDir, "status") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "status", baseUrl) %>">Trạng thái</a></th>
                    <th><span class="sr-only">Thao tác</span></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="task" items="${tasks}">
                    <tr data-pagination-item>
                        <td data-label="ID">#${task.taskId}</td>
                        <td data-label="Phòng">
                            <span class="hk-room-number">${HousekeepingTask.esc(task.roomNumber)}</span><br>
                            <small>Tầng ${task.floorNumber}</small>
                        </td>
                        <td data-label="Loại Task"><strong>${HousekeepingTask.esc(task.getTaskTypeLabel())}</strong></td>
                        <td data-label="Mô tả">${HousekeepingTask.esc(task.note)}</td>
                        <td data-label="Thời gian báo cáo"><fmt:formatDate value="${task.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                        <td data-label="Trạng thái">
                            <span class="hk-badge task-${task.status.toLowerCase()}">${task.getStatusLabel()}</span>
                        </td>
                        <td class="hk-row-action">
                            <form method="get" action="<%= contextPath %><%= isManager ? "/manager/issues/verify" : "/housekeeping/issues/verify" %>">
                                <input type="hidden" name="taskId" value="${task.taskId}">
                                <input type="hidden" name="roomId" value="${task.roomId}">
                                <c:choose>
                                    <c:when test="${task.status eq 'PENDING' or task.status eq 'IN_PROGRESS'}">
                                        <button type="submit" class="btn-verify-action">Kiểm tra bảo trì</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="submit" class="btn-history-action">Xem lịch sử sửa</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty tasks}">
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px 16px; color: #64748b;">
                            <div style="font-size: 32px; margin-bottom: 8px;">🛠️</div>
                            <c:choose>
                                <c:when test="${not empty search or not empty floor or not empty taskType or not empty status}">
                                    <strong style="color: #1e293b; font-size: 15px; display: block; margin-bottom: 4px;">Không tìm thấy sự cố phù hợp</strong>
                                    <span>Không có sự cố thiết bị nào khớp với tiêu chí tìm kiếm hoặc bộ lọc đang chọn.</span>
                                </c:when>
                                <c:otherwise>
                                    <strong style="color: #1e293b; font-size: 15px; display: block; margin-bottom: 4px;">Không có sự cố thiết bị nào</strong>
                                    <span>Hiện tại toàn bộ thiết bị trong các phòng đều đang hoạt động bình thường.</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
        <div class="room-management-pagination" data-pagination-controls></div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
<script src="<%= contextPath %>/assets/js/pagination.js?v=20260820-7"></script>
</body>
</html>