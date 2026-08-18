<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Kết quả thanh toán | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page">
    <section class="empty-card">
        <c:choose>
            <c:when test="${paymentStatus == 'SUCCESS'}">
                <p class="section-kicker">Thanh toán</p>
                <h1>Thanh toán thành công!</h1>
                <p>Cảm ơn bạn đã đặt phòng. Vui lòng kiểm tra email hoặc danh sách đặt phòng của bạn.</p>
                <a class="btn" href="${pageContext.request.contextPath}/my-bookings">Xem đặt phòng của tôi</a>
            </c:when>
            <c:when test="${paymentStatus == 'FAILED'}">
                <p class="section-kicker">Thanh toán</p>
                <h1>Thanh toán thất bại</h1>
                <p>Đã xảy ra lỗi trong quá trình thanh toán. Vui lòng thử lại.</p>
                <a class="btn" href="${pageContext.request.contextPath}/checkout">Quay lại checkout</a>
            </c:when>
            <c:otherwise>
                <p class="section-kicker">Thanh toán</p>
                <h1>Đang xử lý thanh toán...</h1>
                <p>Hệ thống đang kiểm tra giao dịch của bạn.</p>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
