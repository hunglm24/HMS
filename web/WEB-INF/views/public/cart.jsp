<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Giỏ phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .cart-grid { display: grid; grid-template-columns: 2fr 1fr; gap: var(--space-6); align-items: start; }
        .cart-items { display: flex; flex-direction: column; gap: var(--space-4); }
        .cart-card { display: flex; gap: var(--space-4); padding: var(--space-4); background: white; border-radius: var(--radius-lg); border: 1px solid var(--border-color); }
        .cart-card img { width: 120px; height: 120px; object-fit: cover; border-radius: var(--radius-md); }
        .cart-card-content { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
        .cart-card-header { display: flex; justify-content: space-between; align-items: flex-start; gap: var(--space-3); }
        .cart-card-header h3 { margin: 0; font-size: 1.125rem; }
        .cart-card-price { font-weight: 600; color: var(--primary); font-size: 1.125rem; white-space: nowrap; }
        .cart-card-meta { display: flex; flex-wrap: wrap; gap: var(--space-4); color: var(--text-muted); font-size: var(--text-sm); margin-top: var(--space-2); }
        .cart-card-actions { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-4); }
        .qty-control { display: inline-flex; align-items: center; gap: var(--space-2); background: var(--bg-color); border-radius: var(--radius-md); padding: 2px; border: 1px solid var(--border-color); }
        .qty-control button { width: 28px; height: 28px; border: none; background: white; border-radius: 4px; cursor: pointer; font-weight: bold; }
        .qty-control input { width: 30px; text-align: center; border: none; background: transparent; font-weight: 600; pointer-events: none; }
        .cart-summary { background: white; padding: var(--space-5); border-radius: var(--radius-lg); border: 1px solid var(--border-color); position: sticky; top: var(--space-6); }
        .summary-row { display: flex; justify-content: space-between; gap: var(--space-3); margin-bottom: var(--space-3); font-size: var(--text-sm); }
        .summary-total { display: flex; justify-content: space-between; gap: var(--space-3); margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--border-color); font-weight: 700; font-size: 1.25rem; color: var(--primary); }
        .voucher-input { display: flex; gap: var(--space-2); margin-bottom: var(--space-4); }
        .voucher-input input { flex: 1; min-width: 0; padding: var(--space-2); border: 1px solid var(--border-color); border-radius: var(--radius-md); text-transform: uppercase; }
        .timer-box { background: #fff3cd; color: #856404; padding: var(--space-3); border-radius: var(--radius-md); margin-bottom: var(--space-4); display: flex; align-items: center; justify-content: center; gap: var(--space-2); font-weight: 600; }
        @media (max-width: 768px) {
            .cart-grid { grid-template-columns: 1fr; }
            .cart-card { flex-direction: column; }
            .cart-card img { width: 100%; height: 200px; }
            .voucher-input { flex-direction: column; }
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="public-page">
        <section class="section-head">
            <div>
                <h1>Giỏ phòng</h1>
                <p>Kiểm tra phòng đã chọn, nhập mã giảm giá nếu có, rồi tiến hành đặt phòng.</p>
            </div>
        </section>

        <c:if test="${not empty sessionScope.error}">
            <div class="message error" role="alert" style="margin-bottom: var(--space-4);">
                <c:out value="${sessionScope.error}" />
            </div>
            <c:remove var="error" scope="session" />
        </c:if>

        <c:choose>
            <c:when test="${not empty sessionScope.cart}">
                <div class="cart-grid">
                    <div class="cart-items">
                        <div class="timer-box">
                            <span>Thời gian giữ phòng còn lại:</span>
                            <span id="countdown-timer" style="color: #dc3545; font-size: 1.25rem;">15:00</span>
                        </div>

                        <c:set var="total" value="0" />
                        <c:forEach var="item" items="${sessionScope.cart}" varStatus="status">
                            <div class="cart-card">
                                <img src="https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=300&q=80" alt="${item.roomType.name}">
                                <div class="cart-card-content">
                                    <div>
                                        <div class="cart-card-header">
                                            <h3>Phòng ${item.roomType.name}</h3>
                                            <span class="cart-card-price">
                                                <fmt:formatNumber value="${item.subtotal}" pattern="#,###" var="fmtSub" />${fn:replace(fmtSub, ',', ' ')} VND
                                            </span>
                                        </div>
                                        <div class="cart-card-meta">
                                            <span>${item.checkIn} đến ${item.checkOut} (${item.numberOfNights} đêm)</span>
                                            <span>${item.guests} khách</span>
                                            <span><fmt:formatNumber value="${item.averagePricePerNight}" pattern="#,###" var="fmtNightly" />${fn:replace(fmtNightly, ',', ' ')} VND/đêm x ${item.numberOfNights} đêm x ${item.quantity} phòng</span>
                                        </div>
                                    </div>
                                    <div class="cart-card-actions">
                                        <form method="post" action="${pageContext.request.contextPath}/cart" style="display:inline-block; margin:0;" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
                                            <input type="hidden" name="action" value="update">
                                            <input type="hidden" name="index" value="${status.index}">
                                            <div class="qty-control">
                                                <button type="button" onclick="if(this.form.dataset.submitted) return; this.form.quantity.value = Math.max(0, parseInt(this.form.quantity.value) - 1); this.form.submit();">-</button>
                                                <input type="number" name="quantity" value="${item.quantity}" readonly>
                                                <button type="button" onclick="if(this.form.dataset.submitted) return; this.form.quantity.value = parseInt(this.form.quantity.value) + 1; this.form.submit();">+</button>
                                            </div>
                                        </form>

                                        <form method="post" action="${pageContext.request.contextPath}/cart" style="display:inline-block; margin:0;" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
                                            <input type="hidden" name="action" value="remove">
                                            <input type="hidden" name="index" value="${status.index}">
                                            <button type="submit" style="color:var(--color-error-600); background:none; border:none; cursor:pointer; font-size: var(--text-sm); padding: 0;">Xóa</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                            <c:set var="total" value="${total + item.subtotal}" />
                        </c:forEach>
                    </div>

                    <div class="cart-summary">
                        <h3 style="margin-top: 0; margin-bottom: var(--space-4);">Tóm tắt chi phí</h3>
                        <form class="voucher-input" method="post" action="${pageContext.request.contextPath}/cart" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
                            <input type="hidden" name="action" value="applyVoucher">
                            <input type="text" name="promotionCode" placeholder="Nhập mã giảm giá" value="${sessionScope.appliedPromotion.code}" required>
                            <button type="submit" class="btn btn-secondary">Áp dụng</button>
                        </form>
                        <c:if test="${not empty sessionScope.appliedPromotion}">
                            <div class="summary-row" style="align-items:center;">
                                <span>Đang áp dụng: <strong><c:out value="${sessionScope.appliedPromotion.code}" /></strong></span>
                                <form method="post" action="${pageContext.request.contextPath}/cart" style="margin:0;" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
                                    <input type="hidden" name="action" value="removeVoucher">
                                    <button type="submit" style="border:0;background:none;color:#b42318;cursor:pointer;">Bỏ mã</button>
                                </form>
                            </div>
                        </c:if>
                        <div class="summary-row">
                            <span>Tạm tính tiền phòng theo số đêm:</span>
                            <span><fmt:formatNumber value="${total}" pattern="#,###" var="fmtTot" />${fn:replace(fmtTot, ',', ' ')} VND</span>
                        </div>
                        <div class="summary-row" style="color: #28a745;">
                            <span>Giảm giá:</span>
                            <span>- <fmt:formatNumber value="${sessionScope.discountAmount}" pattern="#,###" var="fmtDiscount" />${fn:replace(fmtDiscount, ',', ' ')} VND</span>
                        </div>
                        <div class="summary-total">
                            <span>Tổng thanh toán:</span>
                            <span><fmt:formatNumber value="${sessionScope.finalAmount}" pattern="#,###" var="fmtFinal" />${fn:replace(fmtFinal, ',', ' ')} VND</span>
                        </div>
                        <div style="margin-top: var(--space-5); display: flex; flex-direction: column; gap: var(--space-3);">
                            <a class="btn" style="width: 100%; text-align: center;" href="${pageContext.request.contextPath}/checkout">Tiến hành đặt phòng</a>
                            <a class="btn btn-secondary" style="width: 100%; text-align: center;" href="${pageContext.request.contextPath}/search?checkIn=${sessionScope.cart[0].checkIn}&checkOut=${sessionScope.cart[0].checkOut}&guests=${sessionScope.cart[0].guests}&numRooms=1">Tiếp tục tìm phòng</a>
                        </div>
                    </div>
                </div>

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
                            alert('Đã hết thời gian giữ phòng!');
                            window.location.reload();
                        }
                        time--;
                    }, 1000);
                </script>
            </c:when>
            <c:otherwise>
                <div style="text-align: center; padding: 4rem 2rem; background: white; border-radius: var(--radius-lg); border: 1px solid var(--border-color);">
                    <h2 style="margin-bottom: 0.5rem;">Giỏ hàng trống</h2>
                    <p style="color: var(--text-muted); margin-bottom: 1.5rem;">Bạn chưa chọn phòng nào. Hãy tìm và chọn phòng cho kỳ nghỉ của mình.</p>
                    <a class="btn" href="${pageContext.request.contextPath}/search">Bắt đầu tìm phòng</a>
                </div>
            </c:otherwise>
        </c:choose>
    </main>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>

