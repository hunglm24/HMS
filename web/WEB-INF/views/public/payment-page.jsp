<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Xác nhận đặt phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="public-page">
        <section class="section-head">
            <div>
                <p class="section-kicker">Đặt phòng</p>
                <h1>Thông tin đặt phòng</h1>
                <p>Nhập thông tin người đặt và xác nhận giữ phòng.</p>
            </div>
        </section>
        <section class="dashboard-grid">
            <form class="preview-card" method="post" action="${pageContext.request.contextPath}/checkout">
                <span>Booker</span>
                <h3>Thông tin liên hệ</h3>
                <label>Họ tên<input name="fullName" required></label>
                <label>Email<input type="email" name="email" required></label>
                <label>Điện thoại<input name="phone" required></label>
                <label>Ghi chú<textarea name="note"></textarea></label>
                
                <div style="margin-top: 1rem; padding: 1rem; background: #fff3cd; border: 1px solid #ffe69c; border-radius: 8px;">
                    <strong>Thời gian giữ giỏ hàng: </strong>
                    <span id="countdown-timer" style="font-size: 1.25rem; font-weight: bold; color: #dc3545;">15:00</span>
                </div>
                
                <button type="submit" class="btn" style="width: 100%; margin-top: 1rem;">Xác nhận đặt phòng</button>
            </form>
            
            <aside class="preview-card">
                <span>Tóm tắt</span>
                <h3>Giỏ hàng của bạn</h3>
                <ul style="list-style: none; padding: 0; margin: 1rem 0;">
                    <c:forEach var="item" items="${sessionScope.cart}">
                        <li style="margin-bottom: 0.5rem; padding-bottom: 0.5rem; border-bottom: 1px solid #eee;">
                            <strong>Phòng ${item.roomType.name}</strong> x ${item.quantity}<br>
                            <small>Từ ${item.checkIn} đến ${item.checkOut}</small><br>
                            <span style="color: #666;"><fmt:formatNumber value="${item.subtotal}" pattern="#,###" var="fmtSub" />${fn:replace(fmtSub, ',', ' ')} VND</span>
                        </li>
                    </c:forEach>
                </ul>
                <h2 style="color: var(--primary); margin-top: 1rem;">Tổng cộng: <fmt:formatNumber value="${totalAmount}" pattern="#,###" var="fmtTot" />${fn:replace(fmtTot, ',', ' ')} VND</h2>
            </aside>
        </section>
    </main>
    <script>
        let time = 15 * 60;
        const timerElement = document.getElementById('countdown-timer');
        const interval = setInterval(() => {
            let minutes = Math.floor(time / 60);
            let seconds = time % 60;
            minutes = minutes < 10 ? '0' + minutes : minutes;
            seconds = seconds < 10 ? '0' + seconds : seconds;
            timerElement.textContent = minutes + ':' + seconds;
            if (time <= 0) {
                clearInterval(interval);
                alert('Đã hết thời gian thanh toán. Vui lòng thử lại!');
                window.location.href = '${pageContext.request.contextPath}/cart';
            }
            time--;
        }, 1000);
    </script>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>