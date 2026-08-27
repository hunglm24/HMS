<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    model.HotelConfig detailConfig = (model.HotelConfig) application.getAttribute("hotelConfig");
    String detailHotelName = detailConfig != null && detailConfig.getHotelName() != null && !detailConfig.getHotelName().isBlank()
            ? detailConfig.getHotelName()
            : "HMS Hotel";
    String detailAddress = detailConfig != null && detailConfig.getAddress() != null && !detailConfig.getAddress().isBlank()
            ? detailConfig.getAddress()
            : "Địa chỉ đang được cập nhật";
    String detailPhone = detailConfig != null && detailConfig.getPhone() != null && !detailConfig.getPhone().isBlank()
            ? detailConfig.getPhone()
            : "1900 1234";
    String detailEmail = detailConfig != null && detailConfig.getEmail() != null && !detailConfig.getEmail().isBlank()
            ? detailConfig.getEmail()
            : "support@hmshotel.com";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết booking | <%= escapeHtml(detailHotelName) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .detail-grid { display: grid; grid-template-columns: 2fr 1fr; gap: var(--space-6); align-items: start; }
        .detail-card { background: white; border-radius: var(--radius-lg); border: 1px solid var(--border-color); padding: var(--space-5); margin-bottom: var(--space-5); }
        .detail-card h3 { margin-top: 0; margin-bottom: var(--space-4); border-bottom: 1px solid var(--border-color); padding-bottom: var(--space-3); font-size: 1.25rem; }
        .info-row { display: flex; justify-content: space-between; margin-bottom: var(--space-3); font-size: 0.95rem; }
        .info-row strong { color: var(--text-color); }
        .info-label { color: var(--text-muted); }
        
        .qr-section { display: flex; align-items: center; gap: var(--space-5); margin-bottom: var(--space-4); }
        .qr-code { width: 100px; height: 100px; padding: 5px; border: 1px solid var(--border-color); border-radius: var(--radius-md); background: white; }
        .booking-status { font-size: 1.125rem; font-weight: 700; }
        
        .room-item { display: flex; gap: var(--space-4); margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px dashed var(--border-color); }
        .room-item img { width: 100px; height: 100px; object-fit: cover; border-radius: var(--radius-md); }
        .room-info h4 { margin: 0 0 var(--space-2) 0; font-size: 1.125rem; }
        .room-info p { margin: 0 0 4px 0; color: var(--text-muted); font-size: 0.9rem; }
        
        .badge-status { padding: 4px 10px; border-radius: 20px; font-size: 0.85rem; font-weight: bold; }
        .s-pending { background: #fff3cd; color: #856404; }
        .s-confirmed { background: #d1ecf1; color: #0c5460; }
        .s-checkedin { background: #cce5ff; color: #004085; }
        .s-checkedout { background: #d4edda; color: #155724; }
        .s-cancelled { background: #f8d7da; color: #721c24; }

        .hotel-contact { display: flex; flex-direction: column; gap: var(--space-3); }
        .hotel-contact p { margin: 0; font-size: 0.95rem; display: flex; align-items: center; gap: 8px; }
        .map-btn { display: inline-block; padding: 4px 12px; background: #e9ecef; color: #495057; border-radius: 15px; font-size: 0.8rem; text-decoration: none; font-weight: bold; }
        .map-btn:hover { background: #dee2e6; }

        @media (max-width: 768px) {
            .detail-grid { grid-template-columns: 1fr; }
            .qr-section { flex-direction: column; text-align: center; }
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="public-page">
        <section class="section-head" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
            <div>
                <p class="section-kicker">Chi tiết booking</p>
                <h1 style="margin-bottom: 0;">Mã: ${booking.bookingCode}</h1>
            </div>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/my-bookings">Quay lại danh sách</a>
        </section>

        <div class="detail-grid">
            <div class="main-content">
                <!-- THÔNG TIN MÃ & TRẠNG THÁI -->
                <div class="detail-card">
                    <div class="qr-section">
                        <img class="qr-code" src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${booking.bookingCode}" alt="QR Code">
                        <div>
                            <p style="margin: 0 0 8px 0; color: var(--text-muted);">Vui lòng đưa mã QR này cho lễ tân khi nhận phòng.</p>
                            <div class="booking-status">Trạng thái: 
                                <c:choose>
                                    <c:when test="${booking.status == 'PENDING_PAYMENT'}"><span class="badge-status s-pending">Chờ thanh toán</span></c:when>
                                    <c:when test="${booking.status == 'CONFIRMED'}"><span class="badge-status s-confirmed">Đã xác nhận</span></c:when>
                                    <c:when test="${booking.status == 'CHECKED_IN'}"><span class="badge-status s-checkedin">Đang lưu trú</span></c:when>
                                    <c:when test="${booking.status == 'CHECKED_OUT'}"><span class="badge-status s-checkedout">Đã hoàn thành</span></c:when>
                                    <c:when test="${booking.status == 'CANCELLATION_PENDING'}"><span class="badge-status s-pending">Đang chờ hủy</span></c:when>
                                    <c:when test="${booking.status == 'CANCELLED'}"><span class="badge-status s-cancelled">Đã hủy thành công</span></c:when>
                                    <c:otherwise><span class="badge-status">${booking.status}</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- THÔNG TIN NGƯỜI ĐẶT -->
                <div class="detail-card">
                    <h3>Thông tin người đặt</h3>
                    <div class="info-row"><span class="info-label">Họ tên:</span> <strong>${summary.guestName != null ? summary.guestName : booking.bookingCode}</strong></div>
                    <div class="info-row"><span class="info-label">Số điện thoại:</span> <strong>${summary.phone != null ? summary.phone : 'N/A'}</strong></div>
                    <div class="info-row"><span class="info-label">Email:</span> <strong>${summary.email != null ? summary.email : 'N/A'}</strong></div>
                    <div class="info-row"><span class="info-label">Ghi chú:</span> <strong>${booking.note != null && !booking.note.isEmpty() ? booking.note : 'Không có'}</strong></div>
                </div>

                <!-- THỜI GIAN LƯU TRÚ & PHÒNG -->
                <div class="detail-card">
                    <h3>Thời gian lưu trú & Phòng</h3>
                    <%
                        model.Booking b = (model.Booking) request.getAttribute("booking");
                        long diff = b.getCheckOutDate().getTime() - b.getCheckInDate().getTime();
                        long nights = diff / (1000 * 60 * 60 * 24);
                    %>
                    <div class="info-row"><span class="info-label">Nhận phòng:</span> <strong>14:00, <fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy"/></strong></div>
                    <div class="info-row"><span class="info-label">Trả phòng:</span> <strong>12:00, <fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy"/></strong></div>
                    <div class="info-row"><span class="info-label">Số đêm:</span> <strong><%= nights %> đêm</strong></div>
                    
                    <div class="room-item">
                        <img src="https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=300&q=80" alt="Room Image">
                        <div class="room-info">
                            <h4>${summary.roomTypes != null ? summary.roomTypes : 'Phòng tiêu chuẩn'}</h4>
                            <p>Số lượng: <strong>${summary.roomCount > 0 ? summary.roomCount : 1} phòng</strong></p>
                            <p>Loại phòng: <strong>${summary.roomTypes != null ? summary.roomTypes : 'Không rõ'}</strong></p>
                        </div>
                    </div>
                </div>
                
                <c:if test="${booking.status == 'CANCELLATION_PENDING'}">
                    <div class="detail-card" style="border-left: 4px solid var(--color-warning-600);">
                        <h3>Yêu cầu hủy đang chờ Manager</h3>
                        <div class="info-row"><span class="info-label">Thông tin:</span> <strong>${booking.cancellationReason}</strong></div>
                    </div>
                </c:if>
                <c:if test="${booking.status == 'CANCELLED'}">
                    <div class="detail-card" style="border-left: 4px solid var(--color-error-600);">
                        <h3 style="color: var(--color-error-700);">Đã hủy thành công</h3>
                        <div class="info-row"><span class="info-label">Lý do:</span> <strong>${booking.cancellationReason != null ? booking.cancellationReason : 'N/A'}</strong></div>
                        <div class="info-row"><span class="info-label">Ngày hủy:</span> <strong><fmt:formatDate value="${booking.cancelledAt}" pattern="dd/MM/yyyy HH:mm"/></strong></div>
                    </div>
                </c:if>
            </div>

            <div class="sidebar">
                <!-- GIÁ TIỀN -->
                <div class="detail-card">
                    <h3>Thanh toán</h3>
                    <div class="info-row">
                        <span class="info-label">Hình thức thanh toán:</span>
                        <strong>${booking.status == 'PENDING_PAYMENT' ? 'Thanh toán tại quầy' : 'Online (Đã trả)'}</strong>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--border-color); font-size: 1.125rem; font-weight: 700;">
                        <span>Tổng tiền phòng:</span>
                        <span style="color: var(--primary);">
                            <fmt:formatNumber value="${booking.totalAmount}" pattern="#,###" var="fmtTot" />${fn:replace(fmtTot, ',', ' ')} VND
                        </span>
                    </div>
                </div>

                <!-- THÔNG TIN LIÊN HỆ KHÁCH SẠN -->
                <div class="detail-card">
                    <h3>Liên hệ khách sạn</h3>
                    <div class="hotel-contact">
                        <p>🏨 <strong><%= escapeHtml(detailHotelName) %></strong></p>
                        <p>📍 <%= escapeHtml(detailAddress) %></p>
                        <a href="https://maps.app.goo.gl/19ttJVPZB76SSip66" target="_blank" class="map-btn">Mở Google Maps</a>
                        <p style="margin-top: 8px;">📞 Hotline: <strong><%= escapeHtml(detailPhone) %></strong></p>
                        <p>✉️ Email: <strong><%= escapeHtml(detailEmail) %></strong></p>
                    </div>
                </div>

                <!-- NÚT THAO TÁC -->
                <div class="detail-card" style="background: transparent; border: none; padding: 0;">
                    <c:if test="${booking.status == 'PENDING_PAYMENT' || booking.status == 'CONFIRMED'}">
                        <a href="${pageContext.request.contextPath}/user/cancel-booking?bookingId=${booking.id}" class="btn" style="width: 100%; background-color: white; color: var(--color-error-600); border: 1px solid var(--color-error-600);">Hủy phòng</a>
                    </c:if>
                    <c:if test="${booking.status == 'CHECKED_OUT'}">
                        <button class="btn" style="width: 100%;" onclick="alert('Tính năng đánh giá đang được cập nhật!')">Đánh giá khách sạn</button>
                    </c:if>
                </div>
            </div>
        </div>
    </main>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
