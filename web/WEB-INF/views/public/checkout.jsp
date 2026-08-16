<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Thanh toán | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page"><section class="section-head"><div><p class="section-kicker">Thanh toán</p><h1>Thông tin đặt phòng</h1><p>Nhập thông tin khách và chọn phương thức thanh toán.</p></div></section>
<section class="dashboard-grid"><form class="preview-card" method="post" action="${pageContext.request.contextPath}/checkout"><span>Khách</span><h3>Thông tin liên hệ</h3><label>Họ tên<input name="fullName" required></label><label>Email<input type="email" name="email" required></label><label>Điện thoại<input name="phone" required></label><label>Ghi chú<textarea name="note"></textarea></label><button type="submit">Xác nhận booking</button></form><aside class="preview-card"><span>Tóm tắt</span><h3>Tạm tính</h3><p>Executive Suite, 2 đêm, 1 phòng.</p><h2>4.600.000 VND</h2><p>Chưa bao gồm phụ thu nếu có khi check-out.</p></aside></section>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
