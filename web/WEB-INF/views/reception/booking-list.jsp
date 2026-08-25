<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Reception bookings | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
<style>
    a .metric-card { transition: all 0.2s ease; border: 2px solid transparent; }
    a:hover .metric-card { border-color: var(--primary); transform: translateY(-2px); }
    a .metric-card.active { border-color: var(--primary); background-color: #f0f7ff; }
</style>
</head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" /><main class="page-container"><section class="section-head"><div><p class="section-kicker">${managerView ? 'Manager' : 'Lễ tân'}</p><h1>Danh sách booking</h1><p>Theo dõi booking chờ xác nhận, sắp check-in và đang lưu trú.</p></div><c:if test="${!managerView}"><a class="btn" href="${pageContext.request.contextPath}/receptionist/walk-in">Đặt tại quầy</a></c:if></section>
<section class="kpi-grid">
    <a href="?status=" style="text-decoration: none; color: inherit; display: block;">
        <div class="metric-card ${empty param.status && empty param.scope ? 'active' : ''}">
            <span>Tất cả</span><strong>${allCount}</strong>
        </div>
    </a>
    <a href="?status=PENDING_PAYMENT" style="text-decoration: none; color: inherit; display: block;">
        <div class="metric-card ${param.status == 'PENDING_PAYMENT' ? 'active' : ''}">
            <span>Chờ xác nhận</span><strong>${pendingCount}</strong>
        </div>
    </a>
    <a href="?scope=today" style="text-decoration: none; color: inherit; display: block;">
        <div class="metric-card ${param.scope == 'today' ? 'active' : ''}">
            <span>Check-in hôm nay</span><strong>${checkInTodayCount}</strong>
        </div>
    </a>
    <a href="?status=CHECKED_IN" style="text-decoration: none; color: inherit; display: block;">
        <div class="metric-card ${param.status == 'CHECKED_IN' ? 'active' : ''}">
            <span>Đang lưu trú</span><strong>${checkedInCount}</strong>
        </div>
    </a>
    <a href="?status=CHECKOUT_PENDING" style="text-decoration: none; color: inherit; display: block;">
        <div class="metric-card ${param.status == 'CHECKOUT_PENDING' ? 'active' : ''}">
            <span>Chờ kiểm phòng</span><strong>${checkoutPendingCount}</strong>
        </div>
    </a>
</section>
<form class="toolbar-card" method="get" action="${pageContext.request.contextPath}${bookingBasePath}">
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1.5rem; align-items: end;">
        <label style="grid-column: 1 / -1;">Tìm kiếm<input type="text" name="keyword" value="${param.keyword}" placeholder="Mã booking, tên khách..."></label>
        
        <label>Trạng thái<select name="status">
            <option value="">Tất cả</option>
            <option value="PENDING_PAYMENT" ${param.status == 'PENDING_PAYMENT' ? 'selected' : ''}>Chờ duyệt/Cọc (Pending)</option>
            <option value="CONFIRMED" ${param.status == 'CONFIRMED' ? 'selected' : ''}>Đã duyệt (Confirmed)</option>
            <option value="CHECKED_IN" ${param.status == 'CHECKED_IN' ? 'selected' : ''}>Đang lưu trú (Checked-in)</option>
            <option value="CHECKOUT_PENDING" ${param.status == 'CHECKOUT_PENDING' ? 'selected' : ''}>Chờ kiểm phòng (Checkout Pending)</option>
            <option value="CHECKED_OUT" ${param.status == 'CHECKED_OUT' ? 'selected' : ''}>Đã trả phòng (Checked-out)</option>
            <option value="CANCELLATION_PENDING" ${param.status == 'CANCELLATION_PENDING' ? 'selected' : ''}>Đang chờ hủy</option>
            <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
        </select></label>
        
        <label>Nguồn<select name="source">
            <option value="">Tất cả</option>
            <option value="ONLINE" ${param.source == 'ONLINE' ? 'selected' : ''}>Online Web</option>
            <option value="RECEPTION" ${param.source == 'RECEPTION' ? 'selected' : ''}>Tại quầy (Walk-in)</option>
        </select></label>
        
        <label>Từ ngày<input type="date" name="fromDate" value="${param.fromDate}"></label>
        <label>Đến ngày<input type="date" name="toDate" value="${param.toDate}"></label>
        
        <button type="submit" style="height: 48px; width: 100%;">Lọc</button>
    </div>
</form>
<div class="placeholder-table"><table><thead><tr><th>Booking</th><th>Khách</th><th>Ngày ở</th><th>Phòng</th><th>Ghi chú</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
<tbody>
<c:forEach var="b" items="${bookings}">
    <tr>
        <td><c:out value="${b.bookingCode}" /></td>
        <td>
            <c:out value="${b.guestName}" /><br>
            <small><c:out value="${b.phone}" /></small><br>
            <small style="color: #666;"><c:out value="${b.email}" /></small>
        </td>
        <td><c:out value="${b.checkInDate}" /> - <c:out value="${b.checkOutDate}" /></td>
        <td><c:out value="${b.roomTypes}" /></td>
        <td>
            <c:choose>
                <c:when test="${empty b.note}">
                    <span style="color: #999; font-style: italic;">Không có</span>
                </c:when>
                <c:otherwise>
                    <c:out value="${b.note}" />
                </c:otherwise>
            </c:choose>
        </td>
        <td><span class="status-chip"><c:out value="${b.status}" /></span></td>
        <td>
            <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                <c:if test="${b.status == 'PENDING_PAYMENT'}">
                    <form method="post" action="${pageContext.request.contextPath}${bookingBasePath}" style="display:inline;" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
                        <input type="hidden" name="action" value="CONFIRM">
                        <input type="hidden" name="id" value="${b.bookingId}">
                        <button class="btn btn-secondary" type="submit">Duyệt</button>
                    </form>
                    <c:if test="${!managerView}"><button class="btn btn-secondary" type="button" style="color:var(--color-error-600);" onclick="openRejectModal('${b.bookingId}')">Từ chối</button></c:if>
                </c:if>
                <c:if test="${!managerView && b.status == 'CONFIRMED'}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/reception/check-in?bookingId=${b.bookingId}">Check-in</a>
                </c:if>
                <c:if test="${!managerView && b.status == 'CHECKED_IN'}">
                    <a class="btn btn-primary" style="background-color: var(--color-warning-600);" href="${pageContext.request.contextPath}/reception/check-out?bookingId=${b.bookingId}#selected-booking">Trả phòng</a>
                </c:if>
                <c:if test="${!managerView && b.status == 'CHECKOUT_PENDING'}">
                    <a class="btn btn-primary" style="background-color: #d97706;" href="${pageContext.request.contextPath}/reception/check-out?bookingId=${b.bookingId}#selected-booking">Hoàn tất Check-out</a>
                </c:if>
                <c:if test="${b.status != 'PENDING_PAYMENT'}">
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}${managerView ? '/manager/booking-detail' : '/reception/booking-detail'}?id=${b.bookingId}">Chi tiết</a>
                </c:if>
                <c:if test="${!managerView}"><a class="btn btn-secondary" href="${pageContext.request.contextPath}/receptionist/edit-booking?id=${b.bookingId}">Sửa</a></c:if>
                <c:if test="${managerView && (b.status == 'PENDING_PAYMENT' || b.status == 'CONFIRMED')}">
                    <a class="btn btn-secondary" style="color:var(--color-error-600)" href="${pageContext.request.contextPath}/manager/bookings/refund?id=${b.bookingId}">Hủy / Refund</a>
                </c:if>
            </div>
        </td>
    </tr>
</c:forEach>
</tbody></table></div>

<c:if test="${totalPages > 1}">
    <div style="display: flex; justify-content: center; gap: 8px; margin-top: 24px;">
        <c:if test="${currentPage > 1}">
            <a href="${pageContext.request.contextPath}${bookingBasePath}?keyword=${param.keyword}&status=${param.status}&fromDate=${param.fromDate}&toDate=${param.toDate}&source=${param.source}&page=${currentPage - 1}" class="btn btn-secondary">&laquo; Trước</a>
        </c:if>
        
        <c:forEach begin="1" end="${totalPages}" var="p">
            <a href="${pageContext.request.contextPath}${bookingBasePath}?keyword=${param.keyword}&status=${param.status}&fromDate=${param.fromDate}&toDate=${param.toDate}&source=${param.source}&page=${p}" class="btn ${p == currentPage ? 'btn-primary' : 'btn-secondary'}">${p}</a>
        </c:forEach>
        
        <c:if test="${currentPage < totalPages}">
            <a href="${pageContext.request.contextPath}${bookingBasePath}?keyword=${param.keyword}&status=${param.status}&fromDate=${param.fromDate}&toDate=${param.toDate}&source=${param.source}&page=${currentPage + 1}" class="btn btn-secondary">Sau &raquo;</a>
        </c:if>
    </div>
</c:if>

<!-- Reject Modal -->
<div id="rejectModal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; align-items:center; justify-content:center;">
    <div style="background:white; padding:24px; border-radius:8px; width:400px; max-width:90%;">
        <h3 style="margin-top:0;">Từ chối Đặt phòng</h3>
        <form method="post" action="${pageContext.request.contextPath}${bookingBasePath}" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
            <input type="hidden" name="action" value="REJECT">
            <input type="hidden" id="rejectBookingId" name="id" value="">
            <div class="form-group" style="margin-bottom: 16px;">
                <label>Lý do từ chối (bắt buộc):</label>
                <textarea name="reason" rows="3" required style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px;" placeholder="Ví dụ: Hết phòng, sai thông tin..."></textarea>
            </div>
            <div style="display:flex; justify-content:flex-end; gap:12px;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('rejectModal').style.display='none'">Hủy</button>
                <button type="submit" class="btn" style="background-color:var(--color-error-600); color:white;">Xác nhận Từ chối</button>
            </div>
        </form>
    </div>
</div>
<script>
    function openRejectModal(bookingId) {
        document.getElementById('rejectBookingId').value = bookingId;
        document.getElementById('rejectModal').style.display = 'flex';
    }
</script>

</main></body></html>
