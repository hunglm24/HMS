<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Check-in | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reception.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="page-container reception-page">
    <section class="reception-top">
        <div>
            <p class="reception-eyebrow">Reception</p>
            <h1>Search Booking</h1>
            <p>Tìm booking từ database trước khi thực hiện check-in khách.</p>
        </div>
        <div class="reception-counter">
            <strong>${result.totalItems}</strong>
            <span>bookings found</span>
        </div>
    </section>

    <nav class="breadcrumbs" aria-label="Breadcrumb">
        <a href="${pageContext.request.contextPath}/">Home</a>
        <span>›</span>
        <span>Check-in</span>
        <span>›</span>
        <strong>Search Booking</strong>
    </nav>

    <section class="search-panel">
        <div class="search-panel__head">
            <h2>Search</h2>
            <p>Tra cứu booking theo mã, tên khách, số điện thoại hoặc email.</p>
        </div>

        <form method="get" action="${pageContext.request.contextPath}/reception/check-in" class="booking-search-form">
            <label class="search-input">
                <span class="sr-only">Từ khóa</span>
                <input type="search" name="q" maxlength="50"
                       value="${fn:escapeXml(result.keyword)}"
                       placeholder="Booking Code / Name / Phone / Email">
            </label>

            <div class="search-toolbar">
                <button type="submit" class="btn btn-primary">Search</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/check-in">Reset</a>
            </div>

            <div class="search-filters">
                <label>
                    Booking Status
                    <select name="status">
                        <option value="">All</option>
                        <option value="Pending" ${result.bookingStatus == 'Pending' ? 'selected' : ''}>Pending</option>
                        <option value="Confirmed" ${result.bookingStatus == 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                        <option value="CheckedIn" ${result.bookingStatus == 'CheckedIn' ? 'selected' : ''}>Checked In</option>
                    </select>
                </label>

                <label>
                    Room Type
                    <select name="roomTypeId">
                        <option value="">All</option>
                        <c:forEach var="roomType" items="${roomTypes}">
                            <option value="${roomType.id}"
                                    ${result.roomTypeId != null && result.roomTypeId == roomType.id ? 'selected' : ''}>
                                ${fn:escapeXml(roomType.name)}
                            </option>
                        </c:forEach>
                    </select>
                </label>
            </div>

            <input type="hidden" name="scope" value="${result.scope}">
            <input type="hidden" name="sort" value="${result.sort}">
            <input type="hidden" name="direction" value="${result.direction}">
        </form>
    </section>

    <section class="scope-panel">
        <div class="scope-panel__label">Filters</div>
        <div class="scope-tabs">
            <c:url var="todayUrl" value="/reception/check-in">
                <c:param name="q" value="${result.keyword}" />
                <c:param name="status" value="${result.bookingStatus}" />
                <c:param name="roomTypeId" value="${result.roomTypeId}" />
                <c:param name="sort" value="${result.sort}" />
                <c:param name="direction" value="${result.direction}" />
                <c:param name="scope" value="today" />
            </c:url>
            <c:url var="upcomingUrl" value="/reception/check-in">
                <c:param name="q" value="${result.keyword}" />
                <c:param name="status" value="${result.bookingStatus}" />
                <c:param name="roomTypeId" value="${result.roomTypeId}" />
                <c:param name="sort" value="${result.sort}" />
                <c:param name="direction" value="${result.direction}" />
                <c:param name="scope" value="upcoming" />
            </c:url>
            <c:url var="overdueUrl" value="/reception/check-in">
                <c:param name="q" value="${result.keyword}" />
                <c:param name="status" value="${result.bookingStatus}" />
                <c:param name="roomTypeId" value="${result.roomTypeId}" />
                <c:param name="sort" value="${result.sort}" />
                <c:param name="direction" value="${result.direction}" />
                <c:param name="scope" value="overdue" />
            </c:url>
            <a class="scope-tab ${result.scope == 'today' ? 'active' : ''}" href="${todayUrl}">Check-in Today</a>
            <a class="scope-tab ${result.scope == 'upcoming' ? 'active' : ''}" href="${upcomingUrl}">Upcoming</a>
            <a class="scope-tab ${result.scope == 'overdue' ? 'active' : ''}" href="${overdueUrl}">Overdue</a>
        </div>
        <div class="sort-chip">
            <span>Sort:</span>
            <c:url var="sortUrl" value="/reception/check-in">
                <c:param name="q" value="${result.keyword}" />
                <c:param name="status" value="${result.bookingStatus}" />
                <c:param name="roomTypeId" value="${result.roomTypeId}" />
                <c:param name="scope" value="${result.scope}" />
                <c:param name="sort" value="${result.sort}" />
                <c:param name="direction" value="${result.direction == 'asc' ? 'desc' : 'asc'}" />
            </c:url>
            <a href="${sortUrl}">${result.sort == 'created' ? 'Newest First' : 'Sorted'} ${result.direction == 'asc' ? '↑' : '↓'}</a>
        </div>
    </section>

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
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" class="checkin-form">
                        <input type="hidden" name="action" value="CHECK_IN">
                        <input type="hidden" name="id" value="${selectedBooking.bookingId}">
                        <input type="hidden" name="redirect" value="/reception/check-in">
                        
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

    <section class="results-header" id="booking-results">
        <div>
            <h2>Bookings</h2>
            <p>${result.totalItems} bookings matched your filters.</p>
        </div>
        <div class="results-meta">
            <span>Page ${result.page} / ${result.totalPages}</span>
        </div>
    </section>

    <c:choose>
        <c:when test="${empty result.bookings}">
            <section class="empty-state">
                <h3>No bookings found</h3>
                <p>Thử đổi từ khóa hoặc bộ lọc hiện tại.</p>
            </section>
        </c:when>
        <c:otherwise>
            <div class="booking-list">
                <c:forEach var="booking" items="${result.bookings}" varStatus="loop">
                    <article class="booking-card">
                        <div class="booking-card__head">
                            <div class="booking-card__identity">
                                <span class="booking-index">#${loop.index + 1}</span>
                                <div>
                                    <h3>${fn:escapeXml(booking.bookingCode)}</h3>
                                    <p>${fn:escapeXml(booking.guestName)}</p>
                                </div>
                            </div>
                            <span class="status-badge status-${fn:toLowerCase(booking.status)}">${booking.status}</span>
                        </div>

                        <div class="booking-card__body">
                            <div class="booking-card__column">
                                <span>Check-in</span>
                                <strong><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></strong>
                            </div>
                            <div class="booking-card__column">
                                <span>Check-out</span>
                                <strong><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></strong>
                            </div>
                            <div class="booking-card__column">
                                <span>Room</span>
                                <strong>
                                    ${fn:escapeXml(booking.roomTypes)}
                                    <c:if test="${booking.roomCount > 1}">
                                        (${booking.roomCount})
                                    </c:if>
                                </strong>
                            </div>
                            <div class="booking-card__column">
                                <span>Source</span>
                                <strong>${fn:escapeXml(booking.bookingType)}</strong>
                            </div>
                            <div class="booking-card__column">
                                <span>Phone</span>
                                <strong>${fn:escapeXml(booking.phone)}</strong>
                            </div>
                            <div class="booking-card__column">
                                <span>Email</span>
                                <strong>${fn:escapeXml(booking.email)}</strong>
                            </div>
                        </div>

                        <div class="booking-card__footer">
                            <div class="booking-card__amounts">
                                <div><span>Total</span><strong><fmt:formatNumber value="${booking.totalAmount}" pattern="#,##0" /> đ</strong></div>
                                <div><span>Deposit</span><strong><fmt:formatNumber value="${booking.depositAmount}" pattern="#,##0" /> đ</strong></div>
                            </div>
                            <div class="booking-card__actions">
                                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/check-in?bookingId=${booking.bookingId}">View Detail</a>
                                <a class="btn btn-primary" href="${pageContext.request.contextPath}/reception/check-in?bookingId=${booking.bookingId}#selected-booking">Start Check-in</a>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>

            <c:if test="${result.totalPages > 1}">
                <nav class="pagination-bar" aria-label="Pagination">
                    <c:if test="${result.page > 1}">
                        <c:url var="prevUrl" value="/reception/check-in">
                            <c:param name="q" value="${result.keyword}" />
                            <c:param name="status" value="${result.bookingStatus}" />
                            <c:param name="roomTypeId" value="${result.roomTypeId}" />
                            <c:param name="scope" value="${result.scope}" />
                            <c:param name="sort" value="${result.sort}" />
                            <c:param name="direction" value="${result.direction}" />
                            <c:param name="page" value="${result.page - 1}" />
                        </c:url>
                        <a href="${prevUrl}">‹ Previous</a>
                    </c:if>
                    <span>Page ${result.page} of ${result.totalPages}</span>
                    <c:if test="${result.page < result.totalPages}">
                        <c:url var="nextUrl" value="/reception/check-in">
                            <c:param name="q" value="${result.keyword}" />
                            <c:param name="status" value="${result.bookingStatus}" />
                            <c:param name="roomTypeId" value="${result.roomTypeId}" />
                            <c:param name="scope" value="${result.scope}" />
                            <c:param name="sort" value="${result.sort}" />
                            <c:param name="direction" value="${result.direction}" />
                            <c:param name="page" value="${result.page + 1}" />
                        </c:url>
                        <a href="${nextUrl}">Next ›</a>
                    </c:if>
                </nav>
            </c:if>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
