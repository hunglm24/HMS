<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Booking cua toi | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page"><section class="section-head"><div><p class="section-kicker">Đặt phòng của tôi</p><h1>Booking của tôi</h1><p>Theo dõi trạng thái đặt phòng và thanh toán.</p></div><a class="btn" href="${pageContext.request.contextPath}/search">Đặt phòng mới</a></section>
<form class="toolbar-card" method="get" action="${pageContext.request.contextPath}/my-bookings">
    <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
        <label>Mã Booking<input type="text" name="bookingCode" value="${param.bookingCode}" placeholder="VD: BK-1234"></label>
        <label>Trạng thái<select name="status">
            <option value="">Tất cả</option>
            <option value="PENDING_PAYMENT" ${param.status == 'PENDING_PAYMENT' ? 'selected' : ''}>Chờ thanh toán</option>
            <option value="CONFIRMED" ${param.status == 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
            <option value="CHECKED_IN" ${param.status == 'CHECKED_IN' ? 'selected' : ''}>Đang ở</option>
            <option value="CHECKED_OUT" ${param.status == 'CHECKED_OUT' ? 'selected' : ''}>Đã hoàn thành</option>
            <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
        </select></label>
        <label>Từ ngày<input type="date" name="fromDate" value="${param.fromDate}"></label>
        <label>Đến ngày<input type="date" name="toDate" value="${param.toDate}"></label>
        <button type="submit" style="align-self: flex-end;">Lọc</button>
    </div>
</form>
<div class="placeholder-table"><table><thead><tr><th>Mã booking</th><th>Ngày ở</th><th>Tổng tiền</th><th>Trạng thái</th><th></th></tr></thead>
<tbody>
<c:choose>
    <c:when test="${not empty bookings}">
        <c:forEach var="b" items="${bookings}">
            <tr>
                <td><c:out value="${b.bookingCode}" /></td>
                <td><c:out value="${b.checkInDate}" /> - <c:out value="${b.checkOutDate}" /></td>
                <td><fmt:formatNumber value="${b.totalAmount}" pattern="#,###" var="fmtTot" />${fn:replace(fmtTot, ',', ' ')} VND</td>
                <td><span class="status-chip"><c:out value="${b.status}" /></span></td>
                <td><a class="btn btn-secondary" href="${pageContext.request.contextPath}/booking-detail?id=${b.id}">Chi tiết</a></td>
            </tr>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <tr><td colspan="5" style="text-align:center;">Bạn chưa có đặt phòng nào.</td></tr>
    </c:otherwise>
</c:choose>
</tbody></table></div>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
