<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Chi tiet booking | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" /><main class="public-page"><section class="section-head"><div><p class="section-kicker">Chi tiết booking</p><h1>${booking.bookingCode}</h1><p>Check-in: ${booking.checkInDate} | Check-out: ${booking.checkOutDate}</p></div></section>
<section class="kpi-grid">
    <div class="metric-card"><span>Trạng thái</span><strong>${booking.status}</strong></div>
    <div class="metric-card"><span>Tổng tiền</span><strong>${booking.totalAmount} VND</strong></div>
</section>
<div class="placeholder-actions">
    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/my-bookings">Quay lại</a>
    <c:if test="${booking.status == 'PENDING_PAYMENT' || booking.status == 'CONFIRMED'}">
        <form method="post" action="${pageContext.request.contextPath}/user/cancel-booking" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn hủy đặt phòng này? Chính sách hủy: Miễn phí trước 48h. Phạt 20% nếu hủy sát giờ.');">
            <input type="hidden" name="bookingId" value="${booking.id}">
            <input type="hidden" name="reason" value="Khách hàng tự hủy qua Portal">
            <button class="btn btn-primary" type="submit" style="background-color: var(--color-error-600);">Hủy phòng</button>
        </form>
    </c:if>
</div>
<c:if test="${booking.status == 'CANCELLED'}">
    <div class="metric-card" style="margin-top: 20px; border-left: 4px solid var(--color-error-600);">
        <strong>Chi tiết Hủy:</strong> <br/>
        Lý do: ${booking.cancellationReason != null ? booking.cancellationReason : 'N/A'} <br/>
        Ngày hủy: ${booking.cancelledAt != null ? booking.cancelledAt : 'N/A'}
    </div>
</c:if>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
