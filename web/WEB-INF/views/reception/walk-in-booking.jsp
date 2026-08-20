<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Walk-in booking | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" /><main class="page-container"><section class="section-head"><div><p class="section-kicker">Lễ tân</p><h1>Đặt phòng tại quầy</h1><p>Tìm phòng trống thực tế và tạo booking.</p></div></section>
<c:if test="${not empty error}">
    <div style="padding: 15px; margin-bottom: 20px; border: 1px solid transparent; border-radius: 4px; color: #721c24; background-color: #f8d7da; border-color: #f5c6cb;">
        ${error}
    </div>
</c:if>
<form class="toolbar-card" method="get" action="${pageContext.request.contextPath}/receptionist/walk-in" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
    <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
        <label>Check-in<input type="date" name="checkIn" value="${param.checkIn}" id="topCheckIn" required></label>
        <label>Check-out<input type="date" name="checkOut" value="${param.checkOut}" id="topCheckOut" required></label>
        <label>Loại phòng<select name="roomTypeId">
            <option value="">Tất cả</option>
            <c:forEach var="rt" items="${roomTypes}">
                <option value="${rt.id}" ${param.roomTypeId == rt.id ? 'selected' : ''}>${rt.name}</option>
            </c:forEach>
        </select></label>
        <button type="submit" style="align-self: flex-end;">Tìm phòng trống</button>
    </div>
</form>

<c:if test="${not empty availablePhysicalRooms}">
    <form class="preview-card form-panel" method="post" action="${pageContext.request.contextPath}/receptionist/walk-in" id="walkinForm" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
        <h3>1. Chọn phòng</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; margin-bottom: 24px;">
            <c:forEach var="r" items="${availablePhysicalRooms}">
                <label style="display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; padding: 16px; border: 2px solid #ddd; border-radius: 8px; cursor: pointer; transition: all 0.2s;" class="room-checkbox">
                    <input type="checkbox" name="roomIds" value="${r.id}" data-price="${r.roomTypeBasePrice}" onchange="calculateTotal()" style="display: none;">
                    <div>
                        <strong>Phòng ${r.roomNumber}</strong><br>
                        <span style="font-size: 0.85em; color: #666;">${r.roomTypeName} - <fmt:formatNumber value="${r.roomTypeBasePrice}" pattern="#,##0" />đ</span>
                    </div>
                </label>
            </c:forEach>
        </div>

        <h3>2. Thông tin khách hàng</h3>
        <div class="form-grid">
            <label>Họ tên<input name="fullName" value="${param.fullName}" required></label>
            <label>Điện thoại<input name="phone" value="${param.phone}" required></label>
            <label>Email (Tùy chọn)<input type="email" name="email" value="${param.email}"></label>
            <label>CMND/Passport (Tùy chọn)<input name="identityNumber" value="${param.identityNumber}"></label>
            <label>Số khách<input type="number" name="guests" value="${not empty param.guests ? param.guests : '2'}" min="1" required></label>
            <label>Ghi chú<input name="notes" value="${param.notes}"></label>
            <input type="hidden" name="checkIn" id="checkInVal" value="${param.checkIn}">
            <input type="hidden" name="checkOut" id="checkOutVal" value="${param.checkOut}">
        </div>

        <h3 style="margin-top: 24px;">3. Thanh toán</h3>
        <div style="background: var(--color-gray-50); padding: 16px; border-radius: 8px; margin-bottom: 24px;">
            <div style="display: flex; justify-content: space-between; font-size: 1.25rem; margin-bottom: 16px;">
                <span>Tổng tiền:</span>
                <strong style="color: var(--color-primary-600);" id="totalDisplay">0 đ</strong>
                <input type="hidden" name="totalAmount" id="totalAmountVal" value="0">
            </div>
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <label>Trạng thái thanh toán
                    <select name="paymentStatus" id="paymentStatus" onchange="togglePaymentMethod()">
                        <option value="UNPAID" ${param.paymentStatus == 'UNPAID' ? 'selected' : ''}>Thanh toán khi trả phòng (Trả sau)</option>
                        <option value="PAID" ${param.paymentStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán đủ (Trả trước)</option>
                    </select>
                </label>
                <label id="paymentMethodLabel" style="opacity: 0.5; pointer-events: none;">Phương thức
                    <select name="paymentMethod" id="paymentMethod">
                        <option value="CASH">Tiền mặt</option>
                        <option value="TRANSFER">Chuyển khoản</option>
                        <option value="CARD">Quẹt thẻ (POS)</option>
                    </select>
                </label>
            </div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 12px; border-top: 1px solid #ddd; padding-top: 20px;">
            <a href="${pageContext.request.contextPath}/receptionist/walk-in" class="btn btn-secondary">Làm mới</a>
            <button type="submit" name="submitAction" value="RESERVE" class="btn btn-primary" style="background-color: var(--color-warning-600); border-color: var(--color-warning-600);">Lưu đặt trước (Giữ chỗ)</button>
            <button type="submit" name="submitAction" value="CHECKIN" class="btn btn-primary" id="btnCheckin">Tạo đơn & Check-in ngay</button>
        </div>
    </form>
</c:if>
<c:if test="${param.checkIn != null && empty availablePhysicalRooms}">
    <p style="color: var(--color-error-600); font-weight: bold;">Không có phòng trống trong khoảng thời gian này.</p>
</c:if>
</main>
<script>
    function calculateTotal() {
        const checkIn = new Date(document.getElementById('topCheckIn').value);
        const checkOut = new Date(document.getElementById('topCheckOut').value);
        let nights = 1;
        if (checkIn && checkOut && checkOut > checkIn) {
            nights = Math.ceil((checkOut - checkIn) / (1000 * 60 * 60 * 24));
        }

        let total = 0;
        document.querySelectorAll('input[name="roomIds"]:checked').forEach(cb => {
            const price = parseFloat(cb.getAttribute('data-price')) || 0;
            total += price * nights;
        });

        document.getElementById('totalAmountVal').value = total;
        document.getElementById('totalDisplay').textContent = new Intl.NumberFormat('vi-VN').format(total) + ' đ';
        
        // Highlight logic
        document.querySelectorAll('.room-checkbox').forEach(label => {
            if(label.querySelector('input').checked) {
                label.style.borderColor = 'var(--color-primary-600)';
                label.style.backgroundColor = 'var(--color-primary-50)';
            } else {
                label.style.borderColor = '#ddd';
                label.style.backgroundColor = 'transparent';
            }
        });
    }

    function togglePaymentMethod() {
        const status = document.getElementById('paymentStatus').value;
        const methodLabel = document.getElementById('paymentMethodLabel');
        if (status === 'PAID') {
            methodLabel.style.opacity = '1';
            methodLabel.style.pointerEvents = 'auto';
        } else {
            methodLabel.style.opacity = '0.5';
            methodLabel.style.pointerEvents = 'none';
        }
    }

    // Check-in logic: Disable "Check-in ngay" if checkIn date is not today
    const checkInDate = document.getElementById('topCheckIn').value;
    const todayStr = new Date().toISOString().split('T')[0];
    if (checkInDate && checkInDate !== todayStr) {
        const btn = document.getElementById('btnCheckin');
        if (btn) {
            btn.disabled = true;
            btn.title = "Chỉ có thể check-in nếu ngày nhận phòng là hôm nay.";
            btn.style.opacity = "0.5";
        }
    }
</script>
</body></html>
