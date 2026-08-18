<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.AuditLog" %>
<%@ page import="model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    List<User> receptionists = (List<User>) request.getAttribute("receptionists");
    String bookingCode = (String) request.getAttribute("bookingCode");
    String fromDate = (String) request.getAttribute("fromDate");
    String toDate = (String) request.getAttribute("toDate");
    String receptionistId = (String) request.getAttribute("receptionistId");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch sử đổi phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/room-change-history.css?v=20260817-5">
</head>
<body class="room-change-history-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="page-container room-change-history-page">
    <section class="room-change-history-hero panel">
        <div>
            <p class="room-change-history-kicker">Lễ tân</p>
            <h1>Lịch sử đổi phòng</h1>
            <p>Theo dõi toàn bộ lượt đổi phòng được ghi vào hệ thống. Có thể tìm theo mã booking, khoảng ngày và người thực hiện.</p>
        </div>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/room-map">Về sơ đồ phòng</a>
    </section>

    <section class="room-change-history-toolbar panel">
        <form class="room-change-history-filter" method="get" action="${pageContext.request.contextPath}/reception/room-change-history">
            <div class="room-change-history-field">
                <label class="form-label" for="bookingCode">Mã booking</label>
                <input class="form-control" id="bookingCode" name="bookingCode" value="<%= bookingCode == null ? "" : bookingCode %>" placeholder="Nhập mã booking">
            </div>

            <div class="room-change-history-field">
                <label class="form-label" for="fromDate">Từ ngày</label>
                <input class="form-control" id="fromDate" name="fromDate" type="date" value="<%= fromDate == null ? "" : fromDate %>">
            </div>

            <div class="room-change-history-field">
                <label class="form-label" for="toDate">Đến ngày</label>
                <input class="form-control" id="toDate" name="toDate" type="date" value="<%= toDate == null ? "" : toDate %>">
            </div>

            <div class="room-change-history-field">
                <label class="form-label" for="receptionistId">Người thực hiện</label>
                <select class="form-control" id="receptionistId" name="receptionistId">
                    <option value="">Tất cả receptionist</option>
                    <%-- Render the receptionist filter options. --%>
                    <c:forEach var="receptionist" items="${receptionists}">
                        <option value="${receptionist.id}" <c:if test="${receptionistId eq receptionist.id}">selected="selected"</c:if>>
                            <c:out value="${receptionist.fullName}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="room-change-history-actions">
                <button class="btn btn-secondary" type="submit">Lọc</button>
            </div>
        </form>
    </section>

    <%-- Show the loading error only when the controller provides one. --%>
    <c:if test="${not empty error}">
        <section class="room-change-history-alert error">
            <c:out value="${error}" />
        </section>
    </c:if>

    <section class="room-change-history-card panel">
        <div class="room-change-history-table-wrap" data-pagination-root data-pagination-key="room-change-history" data-pagination-size="5">
            <table class="room-change-history-table">
                <thead>
                <tr>
                    <th>Thời gian</th>
                    <th>Người thực hiện</th>
                    <th>Booking</th>
                    <th>Nội dung</th>
                    <th>IP</th>
                </tr>
                </thead>
                <tbody>
                <%-- Render either the empty state or the history rows. --%>
                <c:choose>
                    <c:when test="${empty logs}">
                        <tr>
                            <td colspan="5">
                                <div class="room-change-history-empty">
                                    <strong>Chưa có lịch sử đổi phòng</strong>
                                    <span>Khi phát sinh thao tác đổi phòng, dữ liệu sẽ hiển thị tại đây.</span>
                                </div>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <%-- Render each room change history entry. --%>
                        <c:forEach var="log" items="${logs}">
                            <tr data-pagination-item>
                                <td><fmt:formatDate value="${log.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                <td>
                                    <%-- Prefer the actor name, then fall back to a system label. --%>
                                    <c:choose>
                                        <c:when test="${not empty log.actorName}">
                                            <c:out value="${log.actorName}" />
                                        </c:when>
                                        <c:otherwise>Hệ thống</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <%-- Show the booking code when available, otherwise use the target ID. --%>
                                    <c:choose>
                                        <c:when test="${not empty log.bookingCode}">
                                            <span class="room-change-history-code"><c:out value="${log.bookingCode}" /></span>
                                        </c:when>
                                        <c:when test="${log.targetId != null}">
                                            <span class="room-change-history-code">#<c:out value="${log.targetId}" /></span>
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td><c:out value="${log.detail}" /></td>
                                <td>
                                    <%-- Show the IP address only when it exists. --%>
                                    <c:choose>
                                        <c:when test="${not empty log.ipAddress}">
                                            <span class="room-change-history-code"><c:out value="${log.ipAddress}" /></span>
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
            <div class="room-management-pagination" data-pagination-controls></div>
        </div>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/pagination.js"></script>
</body>
</html>
