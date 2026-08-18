<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Walk-in booking | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" /><main class="page-container"><section class="section-head"><div><p class="section-kicker">Lễ tân</p><h1>Đặt phòng tại quầy</h1><p>Tìm phòng trống thực tế và tạo booking.</p></div></section>
<form class="toolbar-card" method="get" action="${pageContext.request.contextPath}/receptionist/walk-in">
    <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
        <label>Check-in<input type="date" name="checkIn" value="${param.checkIn}" required></label>
        <label>Check-out<input type="date" name="checkOut" value="${param.checkOut}" required></label>
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
    <form class="preview-card form-panel" method="post" action="${pageContext.request.contextPath}/receptionist/walk-in">
        <h3>Kết quả: Chọn phòng để gán</h3>
        <div class="form-grid">
            <label style="grid-column: span 2;">Phòng trống thực tế
                <select name="roomId" required>
                    <c:forEach var="r" items="${availablePhysicalRooms}">
                        <option value="${r.id}">Phòng ${r.roomNumber} - ${r.roomTypeName}</option>
                    </c:forEach>
                </select>
            </label>
            <label>Họ tên<input name="fullName" required></label>
            <label>Điện thoại<input name="phone" required></label>
            <label>Email (Tùy chọn)<input type="email" name="email"></label>
            <label>CMND/Passport<input name="identityNumber" required></label>
            <label>Ngày sinh<input type="date" name="dateOfBirth" id="dobInput"></label>
            <label>Số khách<input type="number" name="guests" min="1" value="2" required></label>
            <input type="hidden" name="checkIn" value="${param.checkIn}">
            <input type="hidden" name="checkOut" value="${param.checkOut}">
        </div>
        <div class="placeholder-actions"><button type="submit">Tạo booking (Nhận phòng ngay)</button></div>
    </form>
</c:if>
<c:if test="${param.checkIn != null && empty availablePhysicalRooms}">
    <p>Không có phòng trống trong khoảng thời gian này.</p>
</c:if>
</main>
<script>
    const dobInput = document.getElementById('dobInput');
    if (dobInput) {
        const today = new Date();
        dobInput.max = today.toISOString().split('T')[0];
    }
</script>
</body></html>
