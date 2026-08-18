<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Chi tiết booking | HMS Lễ tân</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
<style>
    .detail-section { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 20px; }
    .detail-section h2 { margin-top: 0; border-bottom: 2px solid #eee; padding-bottom: 10px; font-size: 1.25rem; }
    .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; }
    .info-item label { display: block; font-size: 0.85rem; color: #666; margin-bottom: 4px; }
    .info-item div { font-weight: 500; }
    .room-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
    .room-table th, .room-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
    .room-table th { background: #f9fafb; }
    .action-bar { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 20px; }
    .status-badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 0.85rem; font-weight: bold; background: #e5e7eb; }
    .status-CONFIRMED { background: #dcfce7; color: #166534; }
    .status-PENDING_PAYMENT { background: #fef08a; color: #854d0e; }
    .status-CHECKED_IN { background: #dbeafe; color: #1e40af; }
    .status-CHECKED_OUT { background: #f3f4f6; color: #374151; }
    .status-CANCELLED { background: #fee2e2; color: #991b1b; }
</style>
</head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">
    <section class="section-head">
        <div>
            <p class="section-kicker">Quản lý Lễ tân</p>
            <h1>Chi tiết Đặt phòng</h1>
        </div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/bookings">Quay lại danh sách</a>
    </section>
    
    <c:if test="${booking != null}">
        <!-- 1. Khối thông tin chung & Header -->
        <div class="detail-section">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 20px;">
                <div>
                    <h2 style="border: none; margin-bottom: 5px;">Mã Đặt Phòng: ${booking.bookingCode}</h2>
                    <p style="margin: 0; color: #666;">Kênh đặt: ${booking.bookingSource == 'ONLINE' ? 'OTA / Web' : 'Trực tiếp (Walk-in)'}</p>
                </div>
                <div>
                    <span class="status-badge status-${booking.status}">${booking.status}</span>
                </div>
            </div>
            <div class="info-grid" style="margin-top: 20px;">
                <div class="info-item">
                    <label>Ngày tạo đơn</label>
                    <div>${booking.createdAt}</div>
                </div>
                <div class="info-item">
                    <label>Người phụ trách</label>
                    <div>${booking.createdBy != null ? booking.createdBy : 'Hệ thống Online'}</div>
                </div>
            </div>
            
            <div class="action-bar">
                <c:if test="${booking.status == 'PENDING_PAYMENT'}">
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" style="display:inline;">
                        <input type="hidden" name="action" value="CONFIRM"><input type="hidden" name="id" value="${booking.id}">
                        <button class="btn btn-primary" type="submit">Xác nhận đơn</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" style="display:inline;" onsubmit="return confirm('Từ chối đơn này?');">
                        <input type="hidden" name="action" value="REJECT"><input type="hidden" name="id" value="${booking.id}">
                        <button class="btn btn-secondary" type="submit" style="color:var(--color-error-600);">Từ chối</button>
                    </form>
                </c:if>
                <c:if test="${booking.status == 'CONFIRMED'}">
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" style="display:inline;">
                        <input type="hidden" name="action" value="CHECK_IN"><input type="hidden" name="id" value="${booking.id}">
                        <button class="btn btn-primary" type="submit">Check-in Khách</button>
                    </form>
                </c:if>
                <c:if test="${booking.status == 'CHECKED_IN'}">
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" style="display:inline;">
                        <input type="hidden" name="action" value="CHECK_OUT"><input type="hidden" name="id" value="${booking.id}">
                        <button class="btn btn-primary" type="submit" style="background-color: var(--color-warning-600);">Check-out & Thanh toán</button>
                    </form>
                </c:if>
                
                <c:if test="${booking.status == 'PENDING_PAYMENT' || booking.status == 'CONFIRMED'}">
                    <form method="post" action="${pageContext.request.contextPath}/reception/bookings" style="display:inline;" onsubmit="return confirm('Hủy đặt phòng này?');">
                        <input type="hidden" name="action" value="REJECT"><input type="hidden" name="id" value="${booking.id}">
                        <button class="btn btn-secondary" type="submit">Hủy đơn</button>
                    </form>
                </c:if>

                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/receptionist/edit-booking?id=${booking.id}">Đổi phòng / Sửa lịch</a>
                <button class="btn btn-secondary" onclick="window.print()">In phiếu xác nhận</button>
            </div>
        </div>

        <!-- 2. Thông tin khách hàng & Thành viên lưu trú -->
        <div class="detail-section">
            <h2>Thông tin Khách hàng (Booker)</h2>
            <div class="info-grid">
                <div class="info-item">
                    <label>Họ và Tên</label>
                    <div>${guestName != null && guestName != '' ? guestName : 'Khách vãng lai'}</div>
                </div>
                <div class="info-item">
                    <label>Số điện thoại</label>
                    <div>${phone != null && phone != '' ? phone : 'Không có'}</div>
                </div>
                <div class="info-item">
                    <label>Email</label>
                    <div>${email != null && email != '' ? email : 'Không có'}</div>
                </div>
            </div>
        </div>

        <!-- 3. Thông tin phòng & Lưu trú -->
        <div class="detail-section">
            <h2>Thông tin Phòng & Lịch trình (Stay Details)</h2>
            <div class="info-grid" style="margin-bottom: 20px;">
                <div class="info-item">
                    <label>Check-in (Dự kiến)</label>
                    <div>${booking.checkInDate} 14:00</div>
                </div>
                <div class="info-item">
                    <label>Check-out (Dự kiến)</label>
                    <div>${booking.checkOutDate} 12:00</div>
                </div>
                <div class="info-item">
                    <label>Tổng số đêm</label>
                    <div>
                        <c:choose>
                            <c:when test="${not empty bookedRooms}">${bookedRooms[0].nights} đêm</c:when>
                            <c:otherwise>N/A</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            
            <table class="room-table">
                <thead>
                    <tr>
                        <th>Loại phòng (Hạng phòng)</th>
                        <th>Phòng gán (Room Number)</th>
                        <th>Giá mỗi đêm (Rate)</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="rm" items="${bookedRooms}">
                        <tr>
                            <td>${rm.roomType}</td>
                            <td><strong>${rm.roomNumber}</strong></td>
                            <td>${rm.pricePerNight} VND</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty bookedRooms}">
                        <tr><td colspan="3" style="text-align:center;">Chưa có phòng nào được gán</td></tr>
                    </c:if>
                </tbody>
            </table>
            
            <div style="margin-top: 15px; background: #fffbeb; padding: 10px; border-left: 4px solid #f59e0b;">
                <strong>Ghi chú / Yêu cầu đặc biệt (Note):</strong><br/>
                <span style="white-space: pre-line;">${booking.note != null && booking.note != '' ? booking.note : 'Không có yêu cầu đặc biệt.'}</span>
            </div>
        </div>

        <!-- 4. Bảng chi tiết thanh toán & Hóa đơn -->
        <div class="detail-section">
            <h2>Chi tiết Thanh toán (Billing)</h2>
            <table class="room-table">
                <thead>
                    <tr>
                        <th>Hạng mục</th>
                        <th>Số lượng (Đêm)</th>
                        <th style="text-align:right;">Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="rm" items="${bookedRooms}">
                        <tr>
                            <td>Tiền phòng: ${rm.roomType} (Phòng ${rm.roomNumber})</td>
                            <td>${rm.nights}</td>
                            <td style="text-align:right;">${rm.subtotal} VND</td>
                        </tr>
                    </c:forEach>
                    <tr>
                        <td colspan="2" style="text-align:right; font-weight:bold;">Tạm tính tiền phòng:</td>
                        <td style="text-align:right; font-weight:bold;">${booking.totalAmount} VND</td>
                    </tr>
                    <tr>
                        <td colspan="2" style="text-align:right;">Chiết khấu / Mã giảm giá:</td>
                        <td style="text-align:right; color: var(--color-error-600);">- 0 VND</td>
                    </tr>
                </tbody>
                <tfoot>
                    <tr>
                        <th colspan="2" style="text-align:right; font-size: 1.1rem;">Tổng tiền đơn (Grand Total):</th>
                        <th style="text-align:right; font-size: 1.1rem; color: var(--primary);">${booking.totalAmount} VND</th>
                    </tr>
                </tfoot>
            </table>
        </div>
        
        <c:if test="${booking.status == 'CANCELLED'}">
            <div class="detail-section" style="border-left: 4px solid var(--color-error-600);">
                <h2 style="color: var(--color-error-600);">Chi tiết Hủy Đơn</h2>
                <p><strong>Lý do:</strong> ${booking.cancellationReason != null ? booking.cancellationReason : 'N/A'}</p>
                <p><strong>Ngày hủy:</strong> ${booking.cancelledAt != null ? booking.cancelledAt : 'N/A'}</p>
            </div>
        </c:if>

    </c:if>
    <c:if test="${booking == null}">
        <div class="detail-section">
            <p>Không tìm thấy thông tin đặt phòng.</p>
        </div>
    </c:if>
</main>
</body></html>
