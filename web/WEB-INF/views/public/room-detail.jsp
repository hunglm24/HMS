<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="public-page">
        <section class="section-head">
            <div>
                <p class="section-kicker">Chi tiết phòng</p>
                <h1><c:out value="${room.name}" /></h1>
                <p><c:out value="${room.description}" /></p>
            </div>
        </section>
        <section class="room-card-grid">
            <article class="room-showcase-card">
                <img src="https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80" alt="Room">
                <div class="room-showcase-card__body">
                    <h3>Tiện ích</h3>
                    <div class="room-meta">
                        <span><c:out value="${room.capacity}" /> khách</span>
                    </div>
                    <p>Giá từ <fmt:formatNumber value="${room.basePrice}" pattern="#,###" var="fmtPrice" />${fn:replace(fmtPrice, ',', ' ')} VND/đêm. Có thể đổi phòng tùy theo tình trạng thực tế.</p>
                </div>
            </article>
            <aside class="preview-card">
                <span>Đặt phòng</span>
                <h3>Thông tin lưu trú</h3>
                <form class="booking-form" method="post" action="${pageContext.request.contextPath}/cart">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="roomId" value="${room.id}">
                    <label>Check-in<input type="date" name="checkIn" id="checkIn" value="${param.checkIn}" required></label>
                    <label>Check-out<input type="date" name="checkOut" id="checkOut" value="${param.checkOut}" required></label>
                    <label>Số khách<input type="number" name="guests" value="${param.guests != null ? param.guests : room.capacity}" min="1" max="${room.capacity}" required></label>
                    <label>Số phòng<input type="number" name="quantity" value="1" min="1" required></label>
                    <button type="submit" class="btn" ${not requestScope.isAvailable ? 'disabled style="background:#ccc;"' : ''}>
                        ${requestScope.isAvailable ? 'Thêm vào giỏ' : 'Đã hết phòng'}
                    </button>
                    <c:if test="${not requestScope.isAvailable}">
                        <p style="color:red; font-size:0.9rem; margin-top:0.5rem;">Không còn phòng trống cho ngày đã chọn.</p>
                    </c:if>
                </form>
            </aside>
        </section>
    </main>
    <script>
        const checkInInput = document.getElementById('checkIn');
        const checkOutInput = document.getElementById('checkOut');
        
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const today = `${year}-${month}-${day}`;
        checkInInput.setAttribute('min', today);

        function updateCheckOutMin() {
            if (checkInInput.value) {
                const nextDay = new Date(checkInInput.value);
                nextDay.setDate(nextDay.getDate() + 1);
                const minOut = nextDay.toISOString().split('T')[0];
                checkOutInput.setAttribute('min', minOut);
                if (checkOutInput.value && checkOutInput.value <= checkInInput.value) {
                    checkOutInput.value = minOut;
                }
            }
        }
        
        checkInInput.addEventListener('change', updateCheckOutMin);
        updateCheckOutMin();
    </script>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
