<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reception.css?v=20260816-4">
    <style>
        body { padding: 0; margin: 0; background: transparent; overflow-x: hidden; }
        .selected-booking { margin: 0 !important; border: none !important; border-radius: 0 !important; box-shadow: none !important; }
    </style>
</head>
<body>
    <c:if test="${not empty selectedBooking}">
        <section class="selected-booking card" id="selected-booking" style="background: var(--color-white); border-radius: 12px; padding: 24px; box-shadow: var(--shadow-sm); margin-bottom: 24px; border: 2px solid var(--color-primary-500);">
            <div class="selected-booking__head" style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;">
                <div>
                    <p class="reception-eyebrow" style="color: var(--color-primary-600); font-weight: 600; text-transform: uppercase; font-size: 0.875rem; margin-bottom: 4px;">Quy trình Check-in</p>
                    <h2 style="margin: 0; font-size: 1.5rem;">${fn:escapeXml(selectedBooking.bookingCode)}</h2>
                </div>
                <span class="status-badge status-${fn:toLowerCase(selectedBooking.status)}">${selectedBooking.status}</span>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; padding-bottom: 24px; border-bottom: 1px solid var(--color-gray-200);">
                <div><span style="display:block; color:var(--color-gray-500); font-size:0.875rem;">Khách hàng</span><strong style="font-size:1.125rem;">${fn:escapeXml(selectedBooking.guestName)}</strong></div>
                <div><span style="display:block; color:var(--color-gray-500); font-size:0.875rem;">Số điện thoại</span><strong style="font-size:1.125rem;">${fn:escapeXml(selectedBooking.phone)}</strong></div>
                <div><span style="display:block; color:var(--color-gray-500); font-size:0.875rem;">Nhận phòng</span><strong style="font-size:1.125rem;"><fmt:formatDate value="${selectedBooking.checkInDate}" pattern="dd/MM/yyyy" /></strong></div>
                <div><span style="display:block; color:var(--color-gray-500); font-size:0.875rem;">Trả phòng</span><strong style="font-size:1.125rem;"><fmt:formatDate value="${selectedBooking.checkOutDate}" pattern="dd/MM/yyyy" /></strong></div>
                <div><span style="display:block; color:var(--color-gray-500); font-size:0.875rem;">Số lượng phòng</span><strong style="font-size:1.125rem;">${selectedBooking.roomCount} phòng (${fn:escapeXml(selectedBooking.roomTypes)})</strong></div>
                <div>
                    <span style="display:block; color:var(--color-gray-500); font-size:0.875rem; margin-bottom: 8px;">Xếp phòng (Room Assignment)</span>
                    <c:forEach var="asgn" items="${assignments}">
                        <div style="margin-bottom: 12px; display: flex; align-items: center; gap: 12px;">
                            <select class="form-control" style="width: auto;" onchange="updateRoomOptions(this, '${asgn.brId}')">
                                <c:forEach var="rt" items="${roomTypes}">
                                    <option value="${rt.id}" ${rt.id == asgn.roomTypeId ? 'selected' : ''}>${fn:escapeXml(rt.name)}</option>
                                </c:forEach>
                            </select>
                            <select name="assignedRoom_${asgn.brId}" class="form-control" style="width: auto;" required>
                                <c:forEach var="room" items="${availableRoomsByTypeId[asgn.roomTypeId]}">
                                    <option value="${room.id}" ${room.id == asgn.currentRoomId ? 'selected' : ''}>Phòng ${room.roomNumber}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:forEach>
                </div>
                <script>
                    const availableRoomsMap = {
                        <c:forEach var="entry" items="${availableRoomsByTypeId}" varStatus="status">
                            "${entry.key}": [
                                <c:forEach var="room" items="${entry.value}" varStatus="rStatus">
                                    { id: "${room.id}", roomNumber: "${fn:escapeXml(room.roomNumber)}" }${!rStatus.last ? ',' : ''}
                                </c:forEach>
                            ]${!status.last ? ',' : ''}
                        </c:forEach>
                    };

                    function updateRoomOptions(selectElement, brId) {
                        const typeId = selectElement.value;
                        const roomSelect = document.querySelector('select[name="assignedRoom_' + brId + '"]');
                        roomSelect.innerHTML = '';
                        const rooms = availableRoomsMap[typeId] || [];
                        if (rooms.length === 0) {
                            const opt = document.createElement('option');
                            opt.value = '';
                            opt.textContent = 'Hết phòng trống';
                            roomSelect.appendChild(opt);
                        } else {
                            rooms.forEach(room => {
                                const opt = document.createElement('option');
                                opt.value = room.id;
                                opt.textContent = 'Phòng ' + room.roomNumber;
                                roomSelect.appendChild(opt);
                            });
                        }
                    }
                </script>
            </div>

            <c:choose>
                <c:when test="${selectedBooking.status == 'CONFIRMED'}">
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" class="checkin-form" target="_parent">
                        <input type="hidden" name="action" value="CHECK_IN">
                        <input type="hidden" name="id" value="${selectedBooking.bookingId}">
                        <input type="hidden" name="redirect" value="/reception/bookings">
                        
                        <c:set var="remaining" value="${selectedBooking.totalAmount - selectedBooking.depositAmount}" />
                        
                        <div style="background: var(--color-gray-50); padding: 20px; border-radius: 8px; margin-bottom: 24px;">
                            <h3 style="margin-top: 0; margin-bottom: 16px; font-size: 1.125rem;">Thông tin Thanh toán</h3>
                            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                <span>Tổng tiền phòng:</span>
                                <strong><fmt:formatNumber value="${selectedBooking.totalAmount}" pattern="#,##0" /> đ</strong>
                            </div>
                            <div style="display: flex; justify-content: space-between; margin-bottom: 8px; color: var(--color-success-600);">
                                <span>Đã thanh toán (Cọc):</span>
                                <strong>- <fmt:formatNumber value="${selectedBooking.depositAmount}" pattern="#,##0" /> đ</strong>
                            </div>
                            <div style="display: flex; justify-content: space-between; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--color-gray-300); font-size: 1.25rem;">
                                <span><strong>Cần thu thêm:</strong></span>
                                <strong style="color: var(--color-error-600);"><fmt:formatNumber value="${remaining}" pattern="#,##0" /> đ</strong>
                            </div>
                            
                            <c:if test="${remaining > 0}">
                                <div style="margin-top: 20px;">
                                    <label style="display: block; font-weight: 600; margin-bottom: 8px;">Phương thức thu tiền phần còn thiếu:</label>
                                    <div style="display: flex; gap: 16px;">
                                        <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                            <input type="radio" name="paymentMethod" value="CASH" checked required> Tiền mặt
                                        </label>
                                        <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                            <input type="radio" name="paymentMethod" value="TRANSFER" required> Chuyển khoản
                                        </label>
                                        <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                            <input type="radio" name="paymentMethod" value="CARD" required> Quẹt thẻ (POS)
                                        </label>
                                    </div>
                                </div>
                            </c:if>
                        </div>

                        <div style="display: flex; justify-content: flex-end; gap: 12px;">
                            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/receptionist/edit-booking?id=${selectedBooking.bookingId}">Chi tiết / Đổi phòng</a>
                            <button type="submit" class="btn btn-primary" style="padding: 12px 24px; font-size: 1.125rem;">Xác nhận Check-in</button>
                        </div>
                    </form>
                </c:when>
                <c:otherwise>
                    <div style="background: #f8fafc; border: 1px solid #e2e8f0; padding: 20px; border-radius: 8px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center;">
                        <div style="color: #475569; font-size: 1rem;">
                            Đơn đặt phòng này đang ở trạng thái <strong>${selectedBooking.status}</strong>. Chỉ những đơn ở trạng thái <strong>CONFIRMED (Đã duyệt)</strong> mới có thể thực hiện Check-in.
                        </div>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/booking-detail?id=${selectedBooking.bookingId}">Xem chi tiết đơn</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </c:if>

</body>
</html>
