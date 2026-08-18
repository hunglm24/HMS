<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Xac nhan booking | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" /><main class="public-page"><section class="empty-card"><p class="section-kicker">Thành công</p><h1>Booking đã được ghi nhận</h1><p>Hệ thống đã lưu yêu cầu đặt phòng. Vui lòng theo dõi trạng thái trong mục Đặt phòng của tôi.</p><div class="placeholder-actions"><a class="btn" href="${pageContext.request.contextPath}/my-bookings">Xem booking</a><a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Về trang chủ</a></div></section></main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
