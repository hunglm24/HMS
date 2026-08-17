<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Giỏ phòng | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page"><section class="section-head"><div><h1>Giỏ phòng</h1><p>Kiểm tra phòng đã chọn trước khi tiến hành đặt phòng.</p></div></section>
<div class="placeholder-table"><table><thead><tr><th>Hạng phòng</th><th>Ngày ở</th><th>Số lượng</th><th>Tạm tính</th><th>Thao tác</th></tr></thead>
<tbody>
<c:set var="total" value="0" />
<c:forEach var="item" items="${sessionScope.cart}" varStatus="status">
    <tr>
        <td><c:out value="${item.roomType.name}" /></td>
        <td><c:out value="${item.numberOfNights}" /> đêm<br><small><c:out value="${item.checkIn}" /> - <c:out value="${item.checkOut}" /></small></td>
        <td><c:out value="${item.quantity}" /> phòng</td>
        <td><fmt:formatNumber value="${item.subtotal}" pattern="#,###" var="fmtSub" />${fn:replace(fmtSub, ',', ' ')} VND</td>
        <td>
            <form method="post" action="${pageContext.request.contextPath}/cart" style="display:inline;">
                <input type="hidden" name="action" value="remove">
                <input type="hidden" name="index" value="${status.index}">
                <button type="submit" style="color:red; background:none; border:none; cursor:pointer;">Xóa</button>
            </form>
        </td>
    </tr>
    <c:set var="total" value="${total + item.subtotal}" />
</c:forEach>
<c:if test="${empty sessionScope.cart}">
    <tr><td colspan="5" style="text-align:center;">Giỏ hàng trống</td></tr>
</c:if>
</tbody>
<c:if test="${not empty sessionScope.cart}">
    <tfoot><tr><td colspan="3" style="text-align:right;"><strong>Tổng tiền:</strong></td><td colspan="2"><strong><fmt:formatNumber value="${total}" pattern="#,###" var="fmtTot" />${fn:replace(fmtTot, ',', ' ')} VND</strong></td></tr></tfoot>
</c:if>
</table></div>
<div class="placeholder-actions">
    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/search">Chọn thêm phòng</a>
    <c:if test="${not empty sessionScope.cart}">
        <a class="btn" href="${pageContext.request.contextPath}/checkout">Tiến hành đặt phòng</a>
    </c:if>
</div>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
