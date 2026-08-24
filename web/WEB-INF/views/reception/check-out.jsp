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

        <form method="get" action="${pageContext.request.contextPath}/reception/check-out" class="booking-search-form">
            <label class="search-input">
                <span class="sr-only">Từ khóa</span>
                <input type="search" name="q" maxlength="50"
                       value="${fn:escapeXml(result.keyword)}"
                       placeholder="Booking Code / Name / Phone / Email">
            </label>

            <div class="search-toolbar">
                <button type="submit" class="btn btn-primary">Search</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/check-out">Reset</a>
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
            <c:url var="todayUrl" value="/reception/check-out">
                <c:param name="q" value="${result.keyword}" />
                <c:param name="status" value="${result.bookingStatus}" />
                <c:param name="roomTypeId" value="${result.roomTypeId}" />
                <c:param name="sort" value="${result.sort}" />
                <c:param name="direction" value="${result.direction}" />
                <c:param name="scope" value="today" />
            </c:url>
            <c:url var="upcomingUrl" value="/reception/check-out">
                <c:param name="q" value="${result.keyword}" />
                <c:param name="status" value="${result.bookingStatus}" />
                <c:param name="roomTypeId" value="${result.roomTypeId}" />
                <c:param name="sort" value="${result.sort}" />
                <c:param name="direction" value="${result.direction}" />
                <c:param name="scope" value="upcoming" />
            </c:url>
            <c:url var="overdueUrl" value="/reception/check-out">
                <c:param name="q" value="${result.keyword}" />
                <c:param name="status" value="${result.bookingStatus}" />
                <c:param name="roomTypeId" value="${result.roomTypeId}" />
                <c:param name="sort" value="${result.sort}" />
                <c:param name="direction" value="${result.direction}" />
                <c:param name="scope" value="overdue" />
            </c:url>
            <a class="scope-tab ${result.scope == 'today' ? 'active' : ''}" href="${todayUrl}">Check-out Today</a>
            <a class="scope-tab ${result.scope == 'upcoming' ? 'active' : ''}" href="${upcomingUrl}">Upcoming</a>
            <a class="scope-tab ${result.scope == 'overdue' ? 'active' : ''}" href="${overdueUrl}">Overdue</a>
        </div>
        <div class="sort-chip">
            <span>Sort:</span>
            <c:url var="sortUrl" value="/reception/check-out">
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
                    <p class="reception-eyebrow" style="color: var(--color-primary-600); font-weight: 600; text-transform: uppercase; font-size: 0.875rem; margin-bottom: 4px;">Quy trình Check-out</p>
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
                <div><span style="display:block; color:var(--color-gray-500); font-size:0.875rem;">Số phòng phân bổ</span><strong style="font-size:1.125rem; color: var(--color-primary-600);">${fn:escapeXml(selectedBooking.roomNumbers)}</strong></div>
            </div>

            <c:choose>
                <%-- GIAI ĐOẠN 1: Khách đang lưu trú -> Yêu cầu trả phòng & Gửi kiểm tra --%>
                <c:when test="${selectedBooking.status == 'CHECKED_IN'}">
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" class="checkin-form">
                        <input type="hidden" name="action" value="REQUEST_CHECKOUT">
                        <input type="hidden" name="id" value="${selectedBooking.bookingId}">
                        <input type="hidden" name="redirect" value="/reception/check-out?bookingId=${selectedBooking.bookingId}#selected-booking">
                        
                        <div style="background: #eef2ff; border: 1px solid #c7d2fe; padding: 20px; border-radius: 8px; margin-bottom: 24px;">
                            <h3 style="margin-top: 0; margin-bottom: 8px; font-size: 1.125rem; color: #3730a3;">Bước 1: Bắt đầu Trả phòng & Gửi Kiểm phòng</h3>
                            <p style="margin: 0; color: #4338ca; font-size: 0.95rem;">
                                Khi bấm <strong>"Bắt đầu trả phòng & Yêu cầu kiểm tra"</strong>, trạng thái phòng sẽ tự động chuyển sang <strong>INSPECTION (Đang kiểm tra)</strong> trên sơ đồ phòng và tự động tạo công việc kiểm tra phòng cho <strong>Nhân viên dọn dẹp</strong>.
                            </p>
                        </div>

                        <div style="display: flex; justify-content: flex-end; gap: 12px;">
                            <button type="submit" class="btn btn-primary" style="padding: 12px 24px; font-size: 1.125rem; background-color: var(--color-warning-600); border-color: var(--color-warning-600);">
                                Bắt đầu trả phòng & Yêu cầu kiểm tra
                            </button>
                        </div>
                    </form>
                </c:when>

                <%-- GIAI ĐOẠN 2: Đang chờ kiểm phòng -> Hiển thị kết quả kiểm tra & Hoàn tất Check-out --%>
                <c:when test="${selectedBooking.status == 'CHECKOUT_PENDING'}">
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" class="checkin-form">
                        <input type="hidden" name="action" value="COMPLETE_CHECKOUT">
                        <input type="hidden" name="id" value="${selectedBooking.bookingId}">
                        <input type="hidden" name="redirect" value="/reception/check-out">

                        <%-- Khối tiến độ kiểm tra của Buồng phòng --%>
                        <div style="background: var(--color-gray-50); padding: 20px; border-radius: 8px; margin-bottom: 24px; border: 1px solid var(--color-gray-200);">
                            <h3 style="margin-top: 0; margin-bottom: 16px; font-size: 1.125rem; display: flex; align-items: center; justify-content: space-between;">
                                <span>Tiến độ Kiểm phòng của Nhân viên dọn dẹp</span>
                                <c:choose>
                                    <c:when test="${allInspectionsDone}">
                                        <span style="color: var(--color-success-600); font-size: 0.9rem; font-weight: normal; background: #ecfdf5; padding: 4px 12px; border-radius: 20px; border: 1px solid #a7f3d0;">
                                            ✔ Nhân viên dọn dẹp đã kiểm tra xong
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: #b45309; font-size: 0.9rem; font-weight: normal; background: #fef3c7; padding: 4px 12px; border-radius: 20px; border: 1px solid #fde68a;">
                                            ⏳ Đang chờ nhân viên dọn dẹp kiểm tra
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </h3>

                            <c:forEach var="insp" items="${inspections}">
                                <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; background: white; border-radius: 6px; margin-bottom: 8px; border: 1px solid var(--color-gray-200);">
                                    <div>
                                        <strong>Phòng ${insp.roomNumber}</strong> (${insp.roomTypeName})
                                        <span style="margin-left: 12px; color: var(--color-gray-500); font-size: 0.875rem;">Phụ trách: ${insp.staffName}</span>
                                    </div>
                                    <div>
                                        <c:choose>
                                            <c:when test="${insp.inspectionStatus == 'PASSED'}">
                                                <span style="color: #059669; font-weight: 600; background: #d1fae5; padding: 4px 10px; border-radius: 4px; font-size: 0.85rem;">Đạt chuẩn (Passed)</span>
                                            </c:when>
                                            <c:when test="${insp.inspectionStatus == 'DAMAGE_FOUND'}">
                                                <span style="color: #dc2626; font-weight: 600; background: #fee2e2; padding: 4px 10px; border-radius: 4px; font-size: 0.85rem;">Có hư hại / Thiếu đồ</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: #d97706; font-weight: 600; background: #fef3c7; padding: 4px 10px; border-radius: 4px; font-size: 0.85rem;">Chờ kiểm tra</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:forEach>

                            <%-- Bảng biên bản hư hại nếu có --%>
                            <c:if test="${not empty damageReports}">
                                <div style="margin-top: 16px; background: #fff5f5; border: 1px solid #fed7d7; border-radius: 6px; padding: 14px;">
                                    <h4 style="margin-top: 0; margin-bottom: 10px; color: #c53030;">Biên bản ghi nhận hư hại / Mất đồ:</h4>
                                    <table style="width: 100%; border-collapse: collapse; font-size: 0.9rem;">
                                        <thead>
                                            <tr style="border-bottom: 1px solid #feb2b2; text-align: left;">
                                                <th style="padding: 6px;">Phòng</th>
                                                <th style="padding: 6px;">Thiết bị</th>
                                                <th style="padding: 6px;">Tình trạng</th>
                                                <th style="padding: 6px;">Ghi chú</th>
                                                <th style="padding: 6px; text-align: right;">Tiền bồi thường</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="dr" items="${damageReports}">
                                                <tr style="border-bottom: 1px solid #fee2e2;">
                                                    <td style="padding: 6px;">Phòng ${dr.roomNumber}</td>
                                                    <td style="padding: 6px; font-weight: 600;">${dr.equipmentName}</td>
                                                    <td style="padding: 6px; color: #c53030;">${dr.damageType == 'DAMAGED' ? 'Hư hỏng' : 'Mất / Thiếu'}</td>
                                                    <td style="padding: 6px; color: #666;">${dr.note}</td>
                                                    <td style="padding: 6px; text-align: right; font-weight: 600; color: #c53030;">
                                                        <fmt:formatNumber value="${dr.compensationAmount}" pattern="#,##0" /> đ
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:if>
                        </div>

                        <%-- Khối thanh toán tổng kết --%>
                        <c:set var="roomRemaining" value="${selectedBooking.totalAmount - selectedBooking.depositAmount}" />
                        <c:set var="damageFee" value="${not empty totalDamageAmount ? totalDamageAmount : 0}" />
                        
                        <div style="background: var(--color-gray-50); padding: 20px; border-radius: 8px; margin-bottom: 24px; border: 1px solid var(--color-gray-200);">
                            <h3 style="margin-top: 0; margin-bottom: 16px; font-size: 1.125rem;">Tổng kết Thanh toán Check-out</h3>
                            
                            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                <span>Tiền phòng còn thiếu:</span>
                                <strong><fmt:formatNumber value="${roomRemaining > 0 ? roomRemaining : 0}" pattern="#,##0" /> đ</strong>
                            </div>

                            <c:if test="${damageFee > 0}">
                                <div style="display: flex; justify-content: space-between; margin-bottom: 8px; color: #c53030;">
                                    <span>Phí bồi thường hư hại thiết bị:</span>
                                    <strong>+ <fmt:formatNumber value="${damageFee}" pattern="#,##0" /> đ</strong>
                                </div>
                            </c:if>

                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; margin-top: 8px; padding-top: 8px; border-top: 1px dashed var(--color-gray-300);">
                                <span>Phụ thu Minibar / Dịch vụ khác (nếu có):</span>
                                <div style="display: flex; gap: 8px; align-items: center;">
                                    <input type="number" name="surcharge" id="surchargeInput" value="0" min="0" placeholder="0" style="padding: 6px 10px; border: 1px solid #ccc; border-radius: 4px; text-align: right; width: 140px;" oninput="updateTotalDue()">
                                    <span>đ</span>
                                </div>
                            </div>

                            <div style="margin-bottom: 16px;">
                                <label style="display: block; font-weight: 600; margin-bottom: 6px; font-size: 0.9rem;">Ghi chú Check-out:</label>
                                <input type="text" name="checkoutNote" placeholder="Ví dụ: Khách gửi hành lý, trả phòng đúng giờ..." style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
                            </div>

                            <div style="display: flex; justify-content: space-between; margin-top: 16px; padding-top: 16px; border-top: 2px solid var(--color-gray-300); font-size: 1.25rem;">
                                <span><strong>Tổng tiền cần thu:</strong></span>
                                <strong style="color: var(--color-error-600);" id="finalTotalDisplay">
                                    <fmt:formatNumber value="${(roomRemaining > 0 ? roomRemaining : 0) + damageFee}" pattern="#,##0" /> đ
                                </strong>
                            </div>

                            <div style="margin-top: 16px;">
                                <label style="display: block; font-weight: 600; margin-bottom: 8px;">Phương thức thu tiền:</label>
                                <div style="display: flex; gap: 16px;">
                                    <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                        <input type="radio" name="paymentMethod" value="CASH" checked required> Tiền mặt
                                    </label>
                                    <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                        <input type="radio" name="paymentMethod" value="BANK_TRANSFER" required> Chuyển khoản
                                    </label>
                                    <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                        <input type="radio" name="paymentMethod" value="CREDIT_CARD" required> Quẹt thẻ (POS)
                                    </label>
                                </div>
                            </div>

                            <div style="margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--color-gray-300);">
                                <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 1.05rem;">
                                    <input type="checkbox" required style="width: 18px; height: 18px;">
                                    Khách đã trả đủ chìa khóa và xác nhận rời khách sạn
                                </label>
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${allInspectionsDone}">
                                <div style="display: flex; justify-content: flex-end; gap: 12px;">
                                    <button type="submit" class="btn btn-primary" style="padding: 12px 24px; font-size: 1.125rem; background-color: var(--color-success-600); border-color: var(--color-success-600);">
                                        Xác nhận thu tiền & Hoàn tất Check-out
                                    </button>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="display: flex; justify-content: space-between; align-items: center; background: #fffbeb; border: 1px solid #fde68a; padding: 14px 20px; border-radius: 8px;">
                                    <div style="color: #92400e; font-size: 0.95rem; display: flex; align-items: center; gap: 8px;">
                                        <span style="font-size: 1.25rem;">⏳</span>
                                        <span><strong>Chưa thể hoàn tất Check-out:</strong> Nhân viên dọn dẹp chưa hoàn thành kiểm tra phòng. Vui lòng chờ nhân viên kiểm phòng xong để hệ thống tổng hợp biên bản và tính phụ thu.</span>
                                    </div>
                                    <div style="display: flex; gap: 10px; align-items: center;">
                                        <a href="${pageContext.request.contextPath}/reception/check-out?bookingId=${selectedBooking.bookingId}#selected-booking" class="btn btn-secondary" style="padding: 8px 16px; font-size: 0.9rem;">
                                            🔄 Tải lại kết quả
                                        </a>
                                        <button type="button" disabled class="btn" style="padding: 10px 20px; font-size: 1rem; background-color: #cbd5e1; border-color: #cbd5e1; color: #64748b; cursor: not-allowed;">
                                            Chờ kiểm phòng...
                                        </button>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </form>
                </c:when>
                
                <c:otherwise>
                    <div style="padding: 16px; background: var(--color-gray-100); border-radius: 8px; color: var(--color-gray-600);">
                        Đơn đặt phòng này đang ở trạng thái <strong>${selectedBooking.status}</strong>. Không thể thực hiện thao tác check-out.
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
                                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/check-out?bookingId=${booking.bookingId}">View Detail</a>
                                <a class="btn btn-primary" href="${pageContext.request.contextPath}/reception/check-out?bookingId=${booking.bookingId}#selected-booking">Start Check-out</a>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>

            <c:if test="${result.totalPages > 1}">
                <nav class="pagination-bar" aria-label="Pagination">
                    <c:if test="${result.page > 1}">
                        <c:url var="prevUrl" value="/reception/check-out">
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
                        <c:url var="nextUrl" value="/reception/check-out">
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
<script>
function updateTotalDue() {
    var baseRemaining = ${selectedBooking != null && (selectedBooking.totalAmount - selectedBooking.depositAmount) > 0 ? (selectedBooking.totalAmount - selectedBooking.depositAmount) : 0};
    var damageFee = ${not empty totalDamageAmount ? totalDamageAmount : 0};
    var inputElem = document.getElementById('surchargeInput');
    var surchargeVal = inputElem ? (parseFloat(inputElem.value) || 0) : 0;
    var total = baseRemaining + damageFee + surchargeVal;
    var displayElem = document.getElementById('finalTotalDisplay');
    if (displayElem) {
        displayElem.textContent = new Intl.NumberFormat('vi-VN').format(total) + ' đ';
    }
}
</script>
</body>
</html>
