<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="toastMessage" value="${sessionScope.toastMessage}" />
<c:set var="toastType" value="${sessionScope.toastType}" />
<c:remove var="toastMessage" scope="session" />
<c:remove var="toastType" scope="session" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh sách chính sách | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260821-1">
    <style>
        .manager-content { min-width: 0; background: #f6f8fb; }
        .manager-section { background: #fff; border: 1px solid #d9e0ea; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
        .section-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; }
        .section-head h1 { margin-bottom: 6px; }
        .section-head p { margin: 0; }
        .manager-table { width: 100%; border-collapse: collapse; margin-top: 16px; background: #fff; }
        .manager-table th, .manager-table td { border-top: 1px solid #e2e8f0; padding: 12px 10px; text-align: left; vertical-align: top; }
        .manager-table th { color: #526174; font-size: .82rem; text-transform: uppercase; }
        .hint { color: #667085; font-size: .85rem; }
        .status-active { color: #067647; font-weight: 800; }
        .status-inactive { color: #b42318; font-weight: 800; }
        .empty-state { padding: 28px 12px; color: #667085; text-align: center; }
        .action-cell { text-align: right; }
        .policy-actions { display: flex; justify-content: flex-end; gap: 8px; align-items: center; flex-wrap: wrap; }
        .policy-actions .btn { min-height: 38px; }
        .delete-form { display: inline; margin: 0; }
        .toast-success, .toast-error { margin-bottom: 14px; padding: 10px 12px; border-radius: 8px; border: 1px solid; }
        .toast-success { background: #f0fdf4; border-color: #86efac; color: #166534; }
        .toast-error { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
        @media (max-width: 700px) {
            .section-head { align-items: stretch; flex-direction: column; }
            .manager-table { display: block; overflow-x: auto; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="manager-content">
    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>Danh sách chính sách</h1>
            <p>Quản lý các chính sách nội bộ do manager tạo, ví dụ chính sách hủy phòng và hoàn tiền.</p>
        </div>
        <a class="btn" href="${cp}/manager/policies/create">Tạo chính sách</a>
    </section>

    <section class="manager-section">
        <c:if test="${not empty toastMessage}">
            <div class="${toastType}"><c:out value="${toastMessage}" /></div>
        </c:if>

        <table class="manager-table">
            <thead>
            <tr>
                <th>Nhóm</th>
                <th>Chính sách</th>
                <th>Trạng thái</th>
                <th class="action-cell">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="policy" items="${policies}">
                <tr>
                    <td><c:out value="${policy.category}" /></td>
                    <td>
                        <strong><c:out value="${policy.title}" /></strong><br>
                        <span class="hint"><c:out value="${policy.displayContent}" /></span>
                    </td>
                    <td>
                        <span class="${policy.status eq 'ACTIVE' ? 'status-active' : 'status-inactive'}">
                            <c:out value="${policy.status}" />
                        </span>
                    </td>
                    <td class="action-cell">
                        <div class="policy-actions">
                            <a class="btn btn-secondary" href="${cp}/manager/policies/edit?id=${policy.id}">Sửa</a>
                            <form class="delete-form" method="post" action="${cp}/manager/policies/toggle-status">
                                <input type="hidden" name="id" value="${policy.id}">
                                <c:choose>
                                    <c:when test="${policy.status eq 'ACTIVE'}">
                                        <input type="hidden" name="status" value="INACTIVE">
                                        <button class="btn btn-secondary" type="submit">Tạm dừng</button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="status" value="ACTIVE">
                                        <button class="btn" type="submit">Kích hoạt</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                            <form class="delete-form" method="post" action="${cp}/manager/policies/delete">
                                <input type="hidden" name="id" value="${policy.id}">
                                <button class="btn btn-secondary" type="submit" onclick="return confirm('Xóa chính sách này?')">Xóa</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty policies}">
                <tr>
                    <td colspan="4">
                        <div class="empty-state">Chưa có chính sách.</div>
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>
