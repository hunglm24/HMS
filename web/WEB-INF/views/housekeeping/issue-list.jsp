<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="model.HousekeepingTask" %>
<%!
    private String enc(Object value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(String.valueOf(value), java.nio.charset.StandardCharsets.UTF_8);
    }
    private String query(String search, String floor, boolean includeSort, String sort, String direction) {
        StringBuilder q = new StringBuilder();
        if (search != null && !search.isEmpty()) q.append("search=").append(enc(search)).append("&");
        if (floor != null && !floor.isEmpty()) q.append("floor=").append(enc(floor)).append("&");
        if (includeSort) q.append("sort=").append(enc(sort)).append("&direction=").append(enc(direction));
        else if (q.length() > 0) q.setLength(q.length() - 1);
        return q.toString();
    }
    private String sortUrl(String search, String floor, String currentSort, String currentDir, String column) {
        String next = column.equals(currentSort) && "asc".equals(currentDir) ? "desc" : "asc";
        String base = query(search, floor, false, "", "");
        return (base.isEmpty() ? "" : base + "&") + "sort=" + enc(column) + "&direction=" + next;
    }
    private String sortClass(String currentSort, String currentDir, String column) {
        return column.equals(currentSort) ? "sorted-" + currentDir : "sortable";
    }
%>
<%
    String searchStr = (String) request.getAttribute("search");
    String floorStr = (String) request.getAttribute("floor");
    String currentSort = (String) request.getAttribute("currentSort");
    String currentDir = (String) request.getAttribute("currentDir");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý sự cố | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/housekeeping.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <section class="hk-hero">
        <div><p class="hk-eyebrow">Vận hành phòng</p><h1>Quản lý sự cố</h1>
            <p>Theo dõi và cập nhật trạng thái thiết bị cần bảo trì.</p></div>
        <div>
            <a href="${pageContext.request.contextPath}/housekeeping/issues/report" class="hk-primary" style="display:inline-block; padding: 10px 20px; text-decoration: none;">Báo cáo sự cố mới</a>
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

        <form method="get" action="${pageContext.request.contextPath}/housekeeping/issues" class="hk-filters">
            <label class="hk-search">Tìm kiếm
                <input type="search" name="search" maxlength="50" value="<c:out value='${search}'/>" placeholder="Số phòng, thiết bị...">
            </label>
            <label>Tầng
                <input type="number" name="floor" min="0" max="999" value="<c:out value='${floor}'/>" placeholder="Tất cả">
            </label>
            <div class="hk-filter-actions">
                <button type="submit">Lọc</button>
                <a href="${pageContext.request.contextPath}/housekeeping/issues">Đặt lại</a>
            </div>
        </form>

        <div class="hk-table-wrap" data-pagination-root data-pagination-key="issue-list-table" data-pagination-size="5">
            <table class="hk-table">
                <thead>
                    <tr>
                        <th class="<%= sortClass(currentSort, currentDir, "id") %>"><a href="?<%= sortUrl(searchStr, floorStr, currentSort, currentDir, "id") %>">ID</a></th>
                        <th class="<%= sortClass(currentSort, currentDir, "room") %>"><a href="?<%= sortUrl(searchStr, floorStr, currentSort, currentDir, "room") %>">Phòng</a></th>
                        <th class="<%= sortClass(currentSort, currentDir, "type") %>"><a href="?<%= sortUrl(searchStr, floorStr, currentSort, currentDir, "type") %>">Loại Task</a></th>
                        <th>Mô tả</th>
                        <th class="<%= sortClass(currentSort, currentDir, "created_at") %>"><a href="?<%= sortUrl(searchStr, floorStr, currentSort, currentDir, "created_at") %>">Thời gian báo cáo</a></th>
                        <th class="<%= sortClass(currentSort, currentDir, "status") %>"><a href="?<%= sortUrl(searchStr, floorStr, currentSort, currentDir, "status") %>">Trạng thái</a></th>
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
                            <td data-label="Loại Task">${HousekeepingTask.esc(task.taskType)}</td>
                            <td data-label="Mô tả">${HousekeepingTask.esc(task.note)}</td>
                            <td data-label="Thời gian báo cáo"><fmt:formatDate value="${task.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                            <td data-label="Trạng thái">
                                <span class="hk-badge task-${task.status.toLowerCase()}">${task.getStatusLabel()}</span>
                            </td>
                            <td class="hk-row-action">
                                <c:if test="${task.status eq 'PENDING' or task.status eq 'IN_PROGRESS'}">
                                    <form method="get" action="${pageContext.request.contextPath}/housekeeping/issues/verify">
                                        <input type="hidden" name="taskId" value="${task.taskId}">
                                        <input type="hidden" name="roomId" value="${task.roomId}">
                                        <button type="submit">Kiểm tra bảo trì</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty tasks}">
                        <tr><td colspan="7" class="text-center">Không có sự cố nào.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <div class="room-management-pagination" data-pagination-controls></div>
    </main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
<script src="${pageContext.request.contextPath}/assets/js/pagination.js"></script>
</body>
</html>
