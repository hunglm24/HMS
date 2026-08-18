<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="model.HousekeepingTask" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý sự cố - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/housekeeping.css">
    <style>
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
        .issue-filter { display: flex; gap: 12px; margin-bottom: 24px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="app-shell">
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />
    <main>
        <div class="page-header">
            <h1>Quản lý sự cố</h1>
            <a href="${pageContext.request.contextPath}/housekeeping/issues/report" class="btn btn-primary">Báo cáo sự cố mới</a>
        </div>
        
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success">${sessionScope.successMessage}</div>
            <c:remove var="successMessage" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger">${sessionScope.errorMessage}</div>
            <c:remove var="errorMessage" scope="session"/>
        </c:if>

        <form method="get" class="issue-filter">
            <input type="text" name="search" placeholder="Số phòng, thiết bị..." value="<c:out value='${search}'/>">
            <input type="number" name="floor" placeholder="Tầng" value="<c:out value='${floor}'/>">
            <button type="submit" class="btn">Lọc</button>
        </form>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Phòng</th>
                        <th>Loại Task</th>
                        <th>Mô tả</th>
                        <th>Thời gian báo cáo</th>
                        <th>Trạng thái</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="task" items="${tasks}">
                        <tr>
                            <td>#${task.taskId}</td>
                            <td>
                                <strong>P.${HousekeepingTask.esc(task.roomNumber)}</strong><br>
                                <small>Tầng ${task.floorNumber}</small>
                            </td>
                            <td>${HousekeepingTask.esc(task.taskType)}</td>
                            <td>${HousekeepingTask.esc(task.note)}</td>
                            <td><fmt:formatDate value="${task.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                            <td>
                                <span class="status-badge status-${task.status.toLowerCase()}">${task.getStatusLabel()}</span>
                            </td>
                            <td>
                                <c:if test="${task.status eq 'PENDING' or task.status eq 'IN_PROGRESS'}">
                                    <a href="${pageContext.request.contextPath}/housekeeping/issues/verify?taskId=${task.taskId}&roomId=${task.roomId}" class="btn btn-sm btn-primary">Kiểm tra bảo trì</a>
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

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <a href="?page=${p}&search=<c:out value='${search}'/>&floor=<c:out value='${floor}'/>" class="${p == currentPage ? 'active' : ''}">${p}</a>
                </c:forEach>
            </div>
        </c:if>
    </main>
</div>
</body>
</html>
