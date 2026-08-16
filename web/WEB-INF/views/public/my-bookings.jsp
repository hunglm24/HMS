<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Booking cua toi | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page"><section class="section-head"><div><p class="section-kicker">Đặt phòng của tôi</p><h1>Booking của tôi</h1><p>Theo dõi trạng thái đặt phòng và thanh toán.</p></div><a class="btn" href="${pageContext.request.contextPath}/search">Đặt phòng mới</a></section>
<div class="placeholder-table"><table><thead><tr><th>Mã booking</th><th>Ngày ở</th><th>Phòng</th><th>Trạng thái</th><th></th></tr></thead><tbody><tr><td>BK-1024</td><td>20/08 - 22/08</td><td>Executive Suite</td><td><span class="status-chip status-confirmed">Đã xác nhận</span></td><td><a class="btn btn-secondary" href="${pageContext.request.contextPath}/booking-detail">Chi tiết</a></td></tr></tbody></table></div>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
