<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Đang chuyển thanh toán | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" /><main class="public-page"><section class="empty-card"><p class="section-kicker">Thanh toán</p><h1>Đang chuyển đến cổng thanh toán</h1><p>Nếu trình duyệt không tự chuyển, hãy quay lại checkout và thử lại.</p><a class="btn" href="${pageContext.request.contextPath}/checkout">Quay lại checkout</a></section></main><jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
