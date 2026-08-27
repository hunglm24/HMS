<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tìm phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page">
    <section class="section-head">
        <div><p class="section-kicker">Booking</p><h1>Tìm phòng trống</h1><p>Lọc theo ngày ở, số khách và hạng phòng.</p></div>
    </section>
    <form class="toolbar-card" method="get" action="${pageContext.request.contextPath}/search" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1.5rem; align-items: end;">
            <label>Check-in<input type="date" id="checkIn" name="checkIn" value="${param.checkIn}" required></label>
            <label>Check-out<input type="date" id="checkOut" name="checkOut" value="${param.checkOut}" required></label>
            <label>Số khách<input type="number" name="guests" value="${not empty param.guests ? param.guests : 2}" min="1" required></label>
            <label>Số phòng<input type="number" name="numRooms" value="${not empty param.numRooms ? param.numRooms : 1}" min="1" required></label>
            
            <label>Hạng phòng<select name="roomTypeId">
                <option value="">Tất cả</option>
                <c:forEach var="rt" items="${allRoomTypes}">
                    <option value="${rt.id}" ${param.roomTypeId == rt.id ? 'selected' : ''}>${rt.name}</option>
                </c:forEach>
            </select></label>
            
            <label>Giá từ<input type="number" name="minPrice" placeholder="VD: 500000" value="${param.minPrice}"></label>
            <label>Giá đến<input type="number" name="maxPrice" placeholder="VD: 2000000" value="${param.maxPrice}"></label>
            <label>Sắp xếp<select name="sort">
                <option value="PRICE_ASC" ${param.sort == 'PRICE_ASC' ? 'selected' : ''}>Giá tăng dần</option>
                <option value="PRICE_DESC" ${param.sort == 'PRICE_DESC' ? 'selected' : ''}>Giá giảm dần</option>
            </select></label>
            
            <button type="submit" style="height: 48px; width: 100%;">Áp dụng</button>
        </div>
    </form>
    <% if (request.getAttribute("dateError") != null) { %>
        <div class="message error" role="alert"><%= request.getAttribute("dateError") %></div>
    <% } %>
    <section class="room-card-grid">
        <c:choose>
            <c:when test="${not empty availableRooms}">
                <c:forEach var="room" items="${availableRooms}">
                    <article class="room-showcase-card">
                        <!-- Placeholder image, ideally from DB -->
                        <img src="https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=900&q=80" alt="Room image">
                        <div class="room-showcase-card__body">
                            <h3><c:out value="${room.name}" /></h3>
                            <p><c:out value="${room.description}" /></p>
                            <div class="room-meta">
                                <span><c:out value="${room.capacity}" /> khách</span>
                                <span>Còn <c:out value="${room.availableQuantity}" /> phòng</span>
                                <span><fmt:formatNumber value="${room.basePrice}" pattern="#,###" var="fmtPrice" />${fn:replace(fmtPrice, ',', ' ')} VND</span>
                            </div>
                            <a class="btn" href="${pageContext.request.contextPath}/room-detail?id=${room.id}&checkIn=${param.checkIn}&checkOut=${param.checkOut}&guests=${param.guests}">Chọn phòng</a>
                        </div>
                    </article>
                </c:forEach>
            </c:when>
            <c:when test="${param.checkIn != null}">
                <p>Không tìm thấy phòng trống phù hợp với yêu cầu của bạn.</p>
            </c:when>
        </c:choose>
        <c:if test="${totalPages > 1}">
            <div style="display: flex; justify-content: center; gap: 8px; margin-top: 24px; width: 100%; grid-column: 1 / -1;">
                <c:if test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/search?checkIn=${param.checkIn}&checkOut=${param.checkOut}&guests=${param.guests}&numRooms=${param.numRooms}&roomTypeId=${param.roomTypeId}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&sort=${param.sort}&page=${currentPage - 1}" class="btn btn-secondary">&laquo; Trước</a>
                </c:if>
                
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <a href="${pageContext.request.contextPath}/search?checkIn=${param.checkIn}&checkOut=${param.checkOut}&guests=${param.guests}&numRooms=${param.numRooms}&roomTypeId=${param.roomTypeId}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&sort=${param.sort}&page=${p}" class="btn ${p == currentPage ? 'btn-primary' : 'btn-secondary'}">${p}</a>
                </c:forEach>
                
                <c:if test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/search?checkIn=${param.checkIn}&checkOut=${param.checkOut}&guests=${param.guests}&numRooms=${param.numRooms}&roomTypeId=${param.roomTypeId}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&sort=${param.sort}&page=${currentPage + 1}" class="btn btn-secondary">Sau &raquo;</a>
                </c:if>
            </div>
        </c:if>
    </section>
</main>
<script>
    const checkInInput = document.getElementById('checkIn');
    const checkOutInput = document.getElementById('checkOut');
    
    // Set min date for checkIn to today
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const today = year'-'month'-'day;
    checkInInput.setAttribute('min', today);

    // When checkIn changes, update checkOut min date
    checkInInput.addEventListener('change', function() {
        if (this.value) {
            const nextDay = new Date(this.value);
            nextDay.setDate(nextDay.getDate() + 1);
            const minOut = nextDay.toISOString().split('T')[0];
            checkOutInput.setAttribute('min', minOut);
            if (checkOutInput.value && checkOutInput.value <= this.value) {
                checkOutInput.value = minOut;
            }
        }
    });
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" /></body></html>
