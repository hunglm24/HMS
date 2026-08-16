<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Giỏ phòng | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page"><section class="section-head"><div><p class="section-kicker">Giỏ phòng</p><h1>Giỏ phòng</h1><p>Kiểm tra phòng đã chọn trước khi thanh toán.</p></div></section>
<div class="placeholder-table"><table><thead><tr><th>Hạng phòng</th><th>Ngày ở</th><th>Số lượng</th><th>Tạm tính</th></tr></thead><tbody><tr><td>Executive Suite</td><td>2 đêm</td><td>1 phòng</td><td>4.600.000 VND</td></tr></tbody></table></div>
<div class="placeholder-actions"><a class="btn btn-secondary" href="${pageContext.request.contextPath}/search">Chọn thêm phòng</a><a class="btn" href="${pageContext.request.contextPath}/checkout">Thanh toán</a></div>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
