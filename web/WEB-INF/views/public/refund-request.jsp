<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Yêu cầu hủy và hoàn tiền | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp"/><main class="public-page">
<section class="section-head"><div><p class="section-kicker">Hoàn tiền</p><h1>Yêu cầu hủy booking</h1><p>Thông tin sẽ được gửi cho Manager xử lý hoàn tiền.</p></div>
<a class="btn btn-secondary" href="${pageContext.request.contextPath}/booking-detail?id=${booking.id}">Quay lại</a></section>
<div style="display:grid;grid-template-columns:minmax(0,1fr) minmax(280px,.55fr);gap:24px">
<form class="preview-card form-panel" method="post" action="${pageContext.request.contextPath}/user/cancel-booking" onsubmit="return confirm('Xác nhận gửi yêu cầu hủy và hoàn tiền?')">
<input type="hidden" name="bookingId" value="${booking.id}"><h2>Tài khoản nhận hoàn tiền</h2><div class="form-grid">
<label>Ngân hàng<select name="bankName" required><option value="">Chọn ngân hàng</option><option>Vietcombank</option><option>VietinBank</option><option>BIDV</option><option>Agribank</option><option>Techcombank</option><option>MB Bank</option><option>ACB</option><option>VPBank</option><option>TPBank</option><option>Khác</option></select></label>
<label>Chủ tài khoản<input name="accountHolder" maxlength="150" required placeholder="NGUYEN VAN A"></label>
<label>Số tài khoản<input name="accountNumber" inputmode="numeric" pattern="[0-9]{6,30}" maxlength="30" required></label>
<label style="grid-column:1/-1">Lý do hủy<textarea name="reason" maxlength="500" rows="4" required></textarea></label></div>
<button class="btn" style="background:#b91c1c" type="submit">Gửi yêu cầu cho Manager</button></form>
<aside class="preview-card"><h2>Thông tin dự kiến</h2><p>Booking: <strong>${booking.bookingCode}</strong></p>
<p>Tỷ lệ hoàn: <strong><fmt:formatNumber value="${refund.refundRate}" pattern="#0.##"/>%</strong></p>
<p>Số tiền hoàn: <strong><fmt:formatNumber value="${refund.refundAmount}" pattern="#,##0"/> ₫</strong></p>
<p>Phí hủy: <strong><fmt:formatNumber value="${refund.cancellationFee}" pattern="#,##0"/> ₫</strong></p></aside></div>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp"/></body></html>
