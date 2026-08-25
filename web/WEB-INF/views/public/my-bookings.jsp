<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Booking của tôi | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .tabs { display: flex; gap: var(--space-4); border-bottom: 1px solid var(--border-color); margin-bottom: var(--space-6); overflow-x: auto; }
        .tab-item { padding: var(--space-3) var(--space-4); color: var(--text-muted); font-weight: 600; text-decoration: none; white-space: nowrap; border-bottom: 2px solid transparent; }
        .tab-item.active { color: var(--primary); border-bottom-color: var(--primary); }
        .tab-item:hover:not(.active) { color: var(--text-color); border-bottom-color: var(--border-color); }
        
        .booking-list { display: flex; flex-direction: column; gap: var(--space-5); }
        .booking-card { background: white; border-radius: var(--radius-lg); border: 1px solid var(--border-color); padding: var(--space-5); display: flex; flex-direction: column; gap: var(--space-4); }
        .booking-header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 1px solid var(--border-color); padding-bottom: var(--space-3); }
        .booking-id { font-size: 1.125rem; font-weight: 700; margin: 0; }
        .booking-date { font-size: var(--text-sm); color: var(--text-muted); margin-top: 4px; }
        
        .booking-body { display: flex; justify-content: space-between; gap: var(--space-4); flex-wrap: wrap; }
        .booking-info { flex: 1; min-width: 250px; }
        .booking-price { text-align: right; min-width: 150px; }
        .price-label { font-size: var(--text-sm); color: var(--text-muted); }
        .price-amount { font-size: 1.25rem; font-weight: 700; color: var(--primary); margin-top: 4px; }
        
        .booking-actions { display: flex; justify-content: flex-end; gap: var(--space-3); flex-wrap: wrap; margin-top: var(--space-2); }
        .btn-sm { padding: var(--space-2) var(--space-4); font-size: var(--text-sm); }
        
        .badge-pending { background: #fff3cd; color: #856404; }
        .badge-confirmed { background: #d1ecf1; color: #0c5460; }
        .badge-checkedin { background: #cce5ff; color: #004085; }
        .badge-checkedout { background: #d4edda; color: #155724; }
        .badge-cancelled { background: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="public-page">
        <section class="section-head">
            <div>
                <p class="section-kicker">Đặt phòng của tôi</p>
                <c:if test="${not empty errorMessage}">
                    <div style="padding: 15px; margin-bottom: 20px; border: 1px solid transparent; border-radius: 4px; color: #721c24; background-color: #f8d7da; border-color: #f5c6cb;">
                        ${errorMessage}
                    </div>
                </c:if>
                <h1 class="section-title">Lịch sử giao dịch</h1>
                <p>Theo dõi trạng thái đặt phòng và thanh toán.</p>
            </div>
            <a class="btn" href="${pageContext.request.contextPath}/search">Đặt phòng mới</a>
        </section>

        <div class="tabs">
            <a href="?status=" class="tab-item ${empty param.status ? 'active' : ''}">Tất cả</a>
            <a href="?status=UPCOMING" class="tab-item ${param.status == 'UPCOMING' ? 'active' : ''}">Sắp tới</a>
            <a href="?status=CHECKED_IN" class="tab-item ${param.status == 'CHECKED_IN' ? 'active' : ''}">Đang ở</a>
            <a href="?status=CHECKED_OUT" class="tab-item ${param.status == 'CHECKED_OUT' ? 'active' : ''}">Đã hoàn thành</a>
            <a href="?status=CANCELLATION_PENDING" class="tab-item ${param.status == 'CANCELLATION_PENDING' ? 'active' : ''}">Đang chờ hủy</a>
            <a href="?status=CANCELLED" class="tab-item ${param.status == 'CANCELLED' ? 'active' : ''}">Đã hủy</a>
        </div>

        <div class="booking-list">
            <c:choose>
                <c:when test="${not empty bookings}">
                    <c:forEach var="b" items="${bookings}">
                        <div class="booking-card">
                            <div class="booking-header">
                                <div>
                                    <h3 class="booking-id">Mã: ${b.bookingCode}</h3>
                                    <div class="booking-date">Đặt lúc: <fmt:formatDate value="${b.createdAt}" pattern="dd/MM/yyyy HH:mm"/></div>
                                </div>
                                
                                <c:choose>
                                    <c:when test="${b.status == 'PENDING_PAYMENT'}"><span class="badge badge-pending">Chờ thanh toán</span></c:when>
                                    <c:when test="${b.status == 'CONFIRMED'}"><span class="badge badge-confirmed">Đã xác nhận</span></c:when>
                                    <c:when test="${b.status == 'CHECKED_IN'}"><span class="badge badge-checkedin">Đang ở</span></c:when>
                                    <c:when test="${b.status == 'CHECKED_OUT'}"><span class="badge badge-checkedout">Đã hoàn thành</span></c:when>
                                    <c:when test="${b.status == 'CANCELLATION_PENDING'}"><span class="badge badge-pending">Đang chờ hủy</span></c:when>
                                    <c:when test="${b.status == 'CANCELLED'}"><span class="badge badge-cancelled">Đã hủy thành công</span></c:when>
                                    <c:otherwise><span class="badge">${b.status}</span></c:otherwise>
                                </c:choose>
                            </div>
                            
                            <div class="booking-body">
                                <div class="booking-info">
                                    <p style="margin:0 0 8px 0;"><strong>Thời gian ở:</strong> ${b.checkInDate} đến ${b.checkOutDate}</p>
                                    <p style="margin:0; color: var(--text-muted);">Bạn có thể xem chi tiết phòng bằng cách nhấn nút "Xem chi tiết".</p>
                                </div>
                                <div class="booking-price">
                                    <div class="price-label">Tổng thanh toán</div>
                                    <div class="price-amount">
                                        <fmt:formatNumber value="${b.totalAmount}" pattern="#,###" var="fmtTot" />${fn:replace(fmtTot, ',', ' ')} VND
                                    </div>
                                </div>
                            </div>
                            
                            <div class="booking-actions">
                                <a href="${pageContext.request.contextPath}/booking-detail?id=${b.id}" class="btn btn-secondary btn-sm">Xem chi tiết</a>
                                
                                <c:if test="${b.status == 'PENDING_PAYMENT' || b.status == 'CONFIRMED'}">
                                    <button class="btn btn-secondary btn-sm" onclick="alert('Tính năng đang được phát triển!')">Yêu cầu đổi ngày</button>
                                    <a class="btn btn-sm" style="background: var(--color-error-100); color: var(--color-error-700); border: 1px solid var(--color-error-200);" href="${pageContext.request.contextPath}/user/cancel-booking?bookingId=${b.id}">Hủy phòng</a>
                                </c:if>
                                
                                <c:if test="${b.status == 'CHECKED_OUT'}">
                                    <c:choose>
                                        <c:when test="${hasFeedbackMap[b.id]}">
                                            <button class="btn btn-secondary btn-sm" disabled style="opacity: 0.6; cursor: not-allowed;">Đã đánh giá</button>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/customer/feedback?bookingId=${b.id}" class="btn btn-secondary btn-sm">Đánh giá</a>
                                        </c:otherwise>
                                    </c:choose>
                                    <a href="${pageContext.request.contextPath}/search" class="btn btn-sm">Đặt lại</a>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; padding: 4rem 2rem; background: white; border-radius: var(--radius-lg); border: 1px solid var(--border-color);">
                        <div style="font-size: 3rem; margin-bottom: 1rem;">📅</div>
                        <h2 style="margin-bottom: 0.5rem;">Không tìm thấy đặt phòng</h2>
                        <p style="color: var(--text-muted);">Bạn chưa có đơn đặt phòng nào trong trạng thái này.</p>
                    </div>
                </c:otherwise>
            </c:choose>
            <c:if test="${totalPages > 1}">
                <div style="display: flex; justify-content: center; gap: 8px; margin-top: 24px;">
                    <c:if test="${currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/my-bookings?bookingCode=${param.bookingCode}&status=${param.status}&fromDate=${param.fromDate}&toDate=${param.toDate}&page=${currentPage - 1}" class="btn btn-secondary">&laquo; Trước</a>
                    </c:if>
                    
                    <c:forEach begin="1" end="${totalPages}" var="p">
                        <a href="${pageContext.request.contextPath}/my-bookings?bookingCode=${param.bookingCode}&status=${param.status}&fromDate=${param.fromDate}&toDate=${param.toDate}&page=${p}" class="btn ${p == currentPage ? 'btn-primary' : 'btn-secondary'}">${p}</a>
                    </c:forEach>
                    
                    <c:if test="${currentPage < totalPages}">
                        <a href="${pageContext.request.contextPath}/my-bookings?bookingCode=${param.bookingCode}&status=${param.status}&fromDate=${param.fromDate}&toDate=${param.toDate}&page=${currentPage + 1}" class="btn btn-secondary">Sau &raquo;</a>
                    </c:if>
                </div>
            </c:if>
        </div>
    </main>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
