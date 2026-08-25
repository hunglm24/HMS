<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Yêu cầu hoàn tiền | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp"/><main class="page-container">
<section class="section-head"><div><p class="section-kicker">Manager</p><h1>Yêu cầu hoàn tiền</h1><p>Tiếp nhận yêu cầu từ khách và xác nhận sau khi đã chuyển khoản.</p></div></section>
<form class="toolbar-card" method="get"><label>Trạng thái<select name="status"><option value="">Tất cả</option><option value="PENDING" ${param.status=='PENDING'?'selected':''}>Chờ xử lý</option><option value="COMPLETED" ${param.status=='COMPLETED'?'selected':''}>Đã hoàn</option><option value="REJECTED" ${param.status=='REJECTED'?'selected':''}>Từ chối</option></select></label><button class="btn" type="submit">Lọc</button></form>
<div class="placeholder-table"><table><thead><tr><th>Booking / Khách</th><th>Ngân hàng</th><th>Tài khoản</th><th>Số tiền</th><th>Lý do</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach var="r" items="${refundRequests}"><tr><td><strong><c:out value="${r.bookingCode}"/></strong><br><c:out value="${r.guestName}"/></td><td><c:out value="${r.bankName}"/></td><td><c:out value="${r.accountHolder}"/><br><strong><c:out value="${r.accountNumber}"/></strong></td><td><fmt:formatNumber value="${r.refundAmount}" pattern="#,##0"/> ₫</td><td><c:out value="${r.reason}"/></td><td><span class="status-chip"><c:out value="${r.status}"/></span></td><td>
<c:if test="${r.status=='PENDING'}"><form method="post" style="display:inline"><input type="hidden" name="id" value="${r.id}"><button class="btn" name="action" value="COMPLETED" type="submit">Đã hoàn tiền</button><button class="btn btn-secondary" name="action" value="REJECTED" type="submit">Từ chối</button></form></c:if></td></tr></c:forEach>
<c:if test="${empty refundRequests}"><tr><td colspan="7">Chưa có yêu cầu hoàn tiền.</td></tr></c:if></tbody></table></div>
</main></body></html>
