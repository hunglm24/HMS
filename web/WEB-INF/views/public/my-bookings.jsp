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
        
        .booking-actions { display: flex; justify-content: flex-end; align-items: center; gap: var(--space-3); flex-wrap: wrap; margin-top: var(--space-2); }
        .btn-sm { padding: var(--space-2) var(--space-4); font-size: var(--text-sm); }
        
        .badge-pending { background: #fff3cd; color: #856404; }
        .badge-confirmed { background: #d1ecf1; color: #0c5460; }
        .badge-checkedin { background: #cce5ff; color: #004085; }
        .badge-checkedout { background: #d4edda; color: #155724; }
        .badge-cancelled { background: #f8d7da; color: #721c24; }

        .refund-notice {
            background: #f8fafc;
            border-left: 4px solid #0284c7;
            padding: 10px 14px;
            border-radius: 4px;
            font-size: 0.9rem;
            margin-top: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 8px;
        }

        .modal-backdrop {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(15, 23, 42, 0.65);
            z-index: 1000;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .modal-backdrop.active {
            display: flex;
        }
        .modal-image-view {
            max-width: 90vw;
            max-height: 80vh;
            object-fit: contain;
            border-radius: 8px;
            background: white;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
        }
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
                <c:if test="${not empty sessionScope.message}">
                    <div style="padding: 15px; margin-bottom: 20px; border: 1px solid transparent; border-radius: 4px; color: #155724; background-color: #d4edda; border-color: #c3e6cb;">
                        ${sessionScope.message}
                    </div>
                    <c:remove var="message" scope="session"/>
                </c:if>
                <h1 class="section-title">Lịch sử giao dịch</h1>
                <p>Theo dõi trạng thái đặt phòng, hoàn tiền và hóa đơn.</p>
            </div>
            <a class="btn" href="${pageContext.request.contextPath}/search">Đặt phòng mới</a>
        </section>

        <div class="tabs">
            <a href="?status=" class="tab-item ${empty param.status ? 'active' : ''}">Tất cả</a>
            <a href="?status=UPCOMING" class="tab-item ${param.status == 'UPCOMING' ? 'active' : ''}">Sắp tới</a>
            <a href="?status=CHECKED_IN" class="tab-item ${param.status == 'CHECKED_IN' ? 'active' : ''}">Đang ở</a>
            <a href="?status=CHECKED_OUT" class="tab-item ${param.status == 'CHECKED_OUT' ? 'active' : ''}">Đã hoàn thành</a>
            <a href="?status=CANCELLATION_PENDING" class="tab-item ${param.status == 'CANCELLATION_PENDING' ? 'active' : ''}">Đang chờ hủy / hoàn tiền</a>
            <a href="?status=CANCELLED" class="tab-item ${param.status == 'CANCELLED' ? 'active' : ''}">Đã hủy</a>
        </div>

        <div class="booking-list">
            <c:choose>
                <c:when test="${not empty bookings}">
                    <c:forEach var="b" items="${bookings}">
                        <c:set var="rf" value="${refundMap[b.id]}" />
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
                                    <c:when test="${b.status == 'CANCELLATION_PENDING'}"><span class="badge badge-pending">Đang chờ hoàn tiền</span></c:when>
                                    <c:when test="${b.status == 'CANCELLED'}"><span class="badge badge-cancelled">Đã hủy</span></c:when>
                                    <c:otherwise><span class="badge">${b.status}</span></c:otherwise>
                                </c:choose>
                            </div>
                            
                            <div class="booking-body">
                                <div class="booking-info">
                                    <p style="margin:0 0 8px 0;"><strong>Thời gian ở:</strong> ${b.checkInDate} đến ${b.checkOutDate}</p>
                                    
                                    <c:if test="${not empty rf}">
                                        <div class="refund-notice" style="border-left-color: ${rf.status == 'COMPLETED' ? '#16a34a' : (rf.status == 'REJECTED' ? '#dc2626' : '#d97706')};">
                                            <div>
                                                <strong>Hoàn tiền:</strong>
                                                <c:choose>
                                                    <c:when test="${rf.status == 'COMPLETED'}">
                                                        <span style="color:#16a34a; font-weight:600;">Đã hoàn tiền (<fmt:formatNumber value="${rf.refundAmount}" pattern="#,##0"/> ₫)</span>
                                                    </c:when>
                                                    <c:when test="${rf.status == 'PENDING'}">
                                                        <span style="color:#d97706; font-weight:600;">Đang chờ Manager chuyển khoản (<fmt:formatNumber value="${rf.refundAmount}" pattern="#,##0"/> ₫)</span>
                                                    </c:when>
                                                    <c:when test="${rf.status == 'REJECTED'}">
                                                        <span style="color:#dc2626; font-weight:600;">Yêu cầu hoàn tiền bị từ chối</span>
                                                    </c:when>
                                                </c:choose>
                                            </div>
                                            <c:if test="${not empty rf.billImage}">
                                                <button type="button" class="btn btn-secondary btn-sm" onclick="openBillModal('${pageContext.request.contextPath}${rf.billImage}')" style="background:#eff6ff; color:#1d4ed8; border-color:#bfdbfe; cursor:pointer;">
                                                    📷 Xem ảnh bill hoàn tiền
                                                </button>
                                            </c:if>
                                        </div>
                                    </c:if>
                                </div>
                                <div class="booking-price">
                                    <div class="price-label">Tổng thanh toán</div>
                                    <div class="price-amount">
                                        <fmt:formatNumber value="${b.totalAmount}" pattern="#,###" var="fmtTot" />${fn:replace(fmtTot, ',', ' ')} VND
                                    </div>
                                </div>
                            </div>
                            
                            <div class="booking-actions">
                                <c:if test="${not empty rf && not empty rf.billImage}">
                                    <button type="button" class="btn btn-secondary btn-sm" onclick="openBillModal('${pageContext.request.contextPath}${rf.billImage}')" style="background:#eff6ff; color:#1d4ed8; border-color:#bfdbfe;">
                                        📷 Xem bill hoàn tiền
                                    </button>
                                </c:if>

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

    <!-- Modal Phóng To Bill Image -->
    <div class="modal-backdrop" id="billModal" onclick="closeBillModal()">
        <div style="position:relative; text-align:center;" onclick="event.stopPropagation();">
            <h4 style="color:white; margin-bottom:10px;">Ảnh biên lai / Bill hoàn tiền</h4>
            <img id="billModalImg" src="" alt="Biên lai hoàn tiền" class="modal-image-view">
            <div style="margin-top:14px;">
                <button type="button" class="btn btn-secondary btn-sm" onclick="closeBillModal()" style="background:white;">Đóng lại</button>
                <a id="billDownloadLink" href="" target="_blank" class="btn btn-sm" style="background:white; color:var(--primary); margin-left:8px;">Mở ảnh gốc</a>
            </div>
        </div>
    </div>

    <script>
        function openBillModal(imgUrl) {
            document.getElementById('billModalImg').src = imgUrl;
            document.getElementById('billDownloadLink').href = imgUrl;
            document.getElementById('billModal').classList.add('active');
        }
        function closeBillModal() {
            document.getElementById('billModal').classList.remove('active');
        }
    </script>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
