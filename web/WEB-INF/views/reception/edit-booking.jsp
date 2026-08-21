<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sửa Đặt Phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/edit-booking.css?v=20260821-6">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container edit-booking-page">
        <section class="section-head edit-booking-header">
            <div>
                <p class="section-kicker">Lễ tân</p>
                <h1>Cập nhật đặt phòng: ${booking.bookingCode}</h1>
            </div>
            <a href="${pageContext.request.contextPath}/reception/bookings" class="btn btn-secondary">Quay lại</a>
        </section>

        <c:if test="${not empty error}">
            <div class="edit-booking-alert edit-booking-alert--top">
                ${error}
            </div>
            <c:remove var="error" scope="session" />
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/receptionist/edit-booking" id="editBookingForm" class="edit-booking-shell">
            <input type="hidden" name="id" value="${booking.id}">
            <input type="hidden" name="activeBookingRoomKey" id="activeBookingRoomKey" value="${activeBookingRoomKey}">

            <section class="edit-booking-panel">
                <div class="edit-booking-panel__header">
                    <h2>1. Thông tin booking</h2>
                    <p>Giữ bố cục đơn giản, tập trung vào dữ liệu chính cần sửa.</p>
                </div>

                <div class="edit-booking-info-grid">
                    <article class="edit-booking-field-card">
                        <label for="fullName">Tên khách hàng</label>
                        <input id="fullName" type="text" name="fullName" value="${guestName}" required>
                    </article>

                    <article class="edit-booking-field-card">
                        <label for="phone">Số điện thoại</label>
                        <input id="phone" type="text" name="phone" value="${phone}" required>
                    </article>

                    <article class="edit-booking-field-card">
                        <label for="checkInDate">Ngày nhận phòng</label>
                        <input id="checkInDate" type="date" name="checkInDate" value="${booking.checkInDate}" required>
                    </article>

                    <article class="edit-booking-field-card">
                        <label for="checkOutDate">Ngày trả phòng</label>
                        <input id="checkOutDate" type="date" name="checkOutDate" value="${booking.checkOutDate}" required>
                    </article>

                    <article class="edit-booking-field-card edit-booking-field-card--full">
                        <label for="reason">Lý do sửa đổi</label>
                        <input id="reason" type="text" name="reason" placeholder="Ví dụ: Khách yêu cầu đổi lịch / Nâng cấp phòng" required>
                    </article>
                </div>
            </section>

            <section class="edit-booking-panel">
                <div class="edit-booking-room-header">
                    <div>
                        <h2>2. Sửa phòng trong booking</h2>
                        <p>Chọn từng phòng bên trái, rồi gán phòng mới từ danh sách bên phải. Booking nhiều phòng vẫn sửa được bình thường.</p>
                    </div>
                    <div class="edit-booking-room-count">
                        <strong>${totalBookingRoomCount}</strong>
                        <span>phòng</span>
                    </div>
                </div>

                <div class="edit-booking-room-layout">
                    <div class="edit-booking-booking-list">
                        <c:forEach var="room" items="${bookingRooms}" varStatus="status">
                            <article class="edit-booking-booking-card js-booking-room-card ${activeBookingRoomKey == room.bookingRoomId || (empty activeBookingRoomKey && status.first) ? 'is-active' : ''}" data-booking-room-key="${room.bookingRoomId}">
                                <input type="hidden" name="assignedRoom_${room.bookingRoomId}" id="assignedRoom_${room.bookingRoomId}" value="${room.selectedRoomId}">

                                <div class="edit-booking-booking-card__top">
                                    <div class="edit-booking-booking-chip">Phòng ${status.count}</div>
                                    <div class="edit-booking-booking-main">
                                        <strong>Phòng ${room.roomNumber}</strong>
                                        <span>${room.roomTypeName}</span>
                                    </div>
                                    <div class="edit-booking-booking-status">Đang chọn</div>
                                </div>

                                <div class="edit-booking-booking-card__bottom">
                                    <div>
                                        <label>Phòng gán hiện tại</label>
                                        <strong class="js-assigned-room-label">
                                            ${empty room.selectedRoomId ? 'Chưa chọn' : 'Phòng '.concat(room.selectedRoomNumber)}
                                        </strong>
                                        <small class="js-assigned-room-meta">
                                            <c:choose>
                                                <c:when test="${empty room.selectedRoomId}">
                                                    Bấm phòng trống bên phải để gán
                                                </c:when>
                                                <c:otherwise>
                                                    ${room.selectedRoomTypeName}
                                                    - <fmt:formatNumber value="${room.selectedRoomTypeBasePrice}" pattern="#,##0" /> đ
                                                </c:otherwise>
                                            </c:choose>
                                        </small>
                                    </div>
                                    <div>
                                        <label>Tiền phòng</label>
                                        <strong><fmt:formatNumber value="${room.subtotal}" pattern="#,##0" /> đ</strong>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>

                        <c:forEach var="slotId" items="${newRoomSlots}">
                            <c:set var="newSlotKey" value="${'new-'.concat(slotId)}" />
                            <article class="edit-booking-booking-card edit-booking-booking-card--new js-booking-room-card ${activeBookingRoomKey eq newSlotKey ? 'is-active' : ''}" data-booking-room-key="${newSlotKey}">
                                <input type="hidden" name="newAssignedRoom_${slotId}" id="newAssignedRoom_${slotId}" value="${newRoomAssignments[slotId]}">

                                <div class="edit-booking-booking-card__top">
                                    <div class="edit-booking-booking-chip">Phòng mới</div>
                                    <div class="edit-booking-booking-main">
                                        <strong>Thêm phòng</strong>
                                        <span>Slot mới trong booking</span>
                                    </div>
                                    <div class="edit-booking-booking-status">Chưa gán</div>
                                </div>

                                <div class="edit-booking-booking-card__bottom">
                                    <div>
                                        <label>Phòng gán hiện tại</label>
                                        <strong class="js-assigned-room-label">Chưa chọn</strong>
                                        <small class="js-assigned-room-meta">Bấm phòng trống bên phải để gán</small>
                                    </div>
                                    <div>
                                        <label>Tiền phòng</label>
                                        <strong>0 đ</strong>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>

                        <button type="button" class="edit-booking-add-room js-add-room-card">
                            <span>+</span>
                            <strong>Thêm phòng</strong>
                            <small>Tạo một dòng phòng mới</small>
                        </button>
                    </div>

                    <aside class="edit-booking-picker">
                        <div class="edit-booking-picker__toolbar">
                            <div class="form-group edit-booking-picker__filter">
                                <label for="roomTypeId">Loại phòng</label>
                                <select id="roomTypeId" name="roomTypeId">
                                    <option value="">Tất cả loại phòng</option>
                                    <c:forEach var="rt" items="${roomTypes}">
                                        <option value="${rt.id}" ${selectedRoomTypeId == rt.id ? 'selected' : ''}>${rt.name}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <button type="button" class="btn btn-secondary" id="refreshRoomPickerBtn">Tìm phòng trống</button>
                        </div>

                        <div class="edit-booking-picker__hint">
                            <strong>Gợi ý thao tác</strong>
                            <ol>
                                <li>Chọn 1 card bên trái để xác định dòng cần đổi.</li>
                                <li>Click card phòng trống bên phải để gán.</li>
                            </ol>
                        </div>

                        <div class="edit-booking-picker__title">Danh sách phòng trống</div>

                        <c:choose>
                            <c:when test="${not empty availablePhysicalRooms}">
                                <div class="edit-booking-room-grid">
                                    <c:forEach var="room" items="${availablePhysicalRooms}">
                                        <button type="button"
                                                class="edit-booking-room-card js-room-picker-card"
                                                data-room-id="${room.id}"
                                                data-room-number="${room.roomNumber}"
                                                data-room-type-name="${room.roomTypeName}"
                                                data-room-price="${room.roomTypeBasePrice}">
                                            <strong>Phòng ${room.roomNumber}</strong>
                                            <span>${room.roomTypeName}</span>
                                            <em><fmt:formatNumber value="${room.roomTypeBasePrice}" pattern="#,##0" /> đ</em>
                                        </button>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="edit-booking-alert">
                                    Không có phòng trống nào phù hợp với bộ lọc hiện tại.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </aside>
                </div>

                <div class="edit-booking-footer">
                    <div class="edit-booking-footer__note">
                        <strong>Tổng quan</strong>
                        <p>Hệ thống sẽ tự chặn trùng phòng trong cùng booking khi lưu.</p>
                    </div>
                    <button type="submit" class="btn btn-primary" id="saveEditBookingBtn">Lưu thay đổi</button>
                </div>
            </section>
        </form>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/edit-booking.js?v=20260821-7"></script>
</body>
</html>
