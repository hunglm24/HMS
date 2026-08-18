<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Room" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    Map<Integer, List<Room>> roomsByFloor = (Map<Integer, List<Room>>) request.getAttribute("roomsByFloor");
    Long availableCount = (Long) request.getAttribute("availableCount");
    Long occupiedCount = (Long) request.getAttribute("occupiedCount");
    Long cleaningCount = (Long) request.getAttribute("cleaningCount");
    Long maintenanceCount = (Long) request.getAttribute("maintenanceCount");
    Integer totalCount = (Integer) request.getAttribute("totalCount");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sơ đồ phòng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/room-map.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/room-change-modal.css?v=20260816-4">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-map-page">
        <div class="map-header">
            <div>
                <h2>Sơ đồ phòng</h2>
                <div class="stats-row">
                    <div class="stat-badge stat-available">Trống: ${availableCount}</div>
                    <div class="stat-badge stat-occupied">Đang có khách: ${occupiedCount}</div>
                    <div class="stat-badge stat-reserved" style="background:#ffc107; color:#000;">Đã đặt trước: ${reservedCount}</div>
                    <div class="stat-badge stat-cleaning">Đang dọn: ${cleaningCount}</div>
                    <div class="stat-badge stat-maintenance">Bảo trì: ${maintenanceCount}</div>
                    <div class="stat-badge">Tổng: ${totalCount}</div>
                </div>
            </div>
        </div>

        <details class="room-map-filter-accordion">
            <summary class="room-map-filter-toggle">
                <span class="room-map-filter-toggle-text">
                    <span class="room-map-filter-label">Bộ lọc phòng</span>
                    <span class="room-map-filter-hint">Nhấn để mở / đóng</span>
                </span>
            </summary>
            <form class="room-map-filter-form" method="get" action="${pageContext.request.contextPath}/reception/room-map">
                <input type="text" name="search" value="${param.search}" placeholder="Số phòng, loại phòng...">
                <select name="status">
                    <option value="">Tất cả trạng thái</option>
                    <option value="AVAILABLE" ${param.status == 'AVAILABLE' ? 'selected' : ''}>Trống</option>
                    <option value="OCCUPIED" ${param.status == 'OCCUPIED' ? 'selected' : ''}>Đang có khách</option>
                    <option value="CLEANING" ${param.status == 'CLEANING' ? 'selected' : ''}>Đang dọn</option>
                    <option value="MAINTENANCE" ${param.status == 'MAINTENANCE' ? 'selected' : ''}>Bảo trì</option>
                </select>
                <select name="floor">
                    <option value="">Tất cả tầng</option>
                    <option value="1" ${param.floor == '1' ? 'selected' : ''}>Tầng 1</option>
                    <option value="2" ${param.floor == '2' ? 'selected' : ''}>Tầng 2</option>
                    <option value="3" ${param.floor == '3' ? 'selected' : ''}>Tầng 3</option>
                    <option value="4" ${param.floor == '4' ? 'selected' : ''}>Tầng 4</option>
                </select>
                <button type="submit">Lọc</button>
            </form>
        </details>

        <c:if test="${not empty flashMessage}">
            <div class="message ${empty flashType ? 'success' : flashType}">
                <c:out value="${flashMessage}" />
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty roomsByFloor}">
                <div class="floor-columns">
                    <%-- Render rooms grouped by floor. --%>
                    <c:forEach items="${roomsByFloor}" var="entry">
                        <div class="floor-section">
                            <h3 class="floor-title">
                                <c:choose>
                                    <c:when test="${roomStatus eq 'AVAILABLE'}">
                                        <c:set var="statusLabel" value="Trống" />
                                    </c:when>
                                    <c:when test="${roomStatus eq 'OCCUPIED'}">
                                        <c:set var="statusLabel" value="Có khách" />
                                    </c:when>
                                    <c:when test="${roomStatus eq 'RESERVED'}">
                                        <c:set var="statusLabel" value="Đã đặt trước" />
                                    </c:when>
                                    <c:when test="${roomStatus eq 'CLEANING'}">
                                        <c:set var="statusLabel" value="Đang dọn" />
                                    </c:when>
                                    <c:when test="${roomStatus eq 'MAINTENANCE'}">
                                        <c:set var="statusLabel" value="Bảo trì" />
                                    </c:when>
                                    <c:when test="${roomStatus eq 'NOT_READY'}">
                                        <c:set var="statusLabel" value="Chưa sẵn sàng" />
                                    </c:when>
                                    <c:when test="${roomStatus eq 'INSPECTION'}">
                                        <c:set var="statusLabel" value="Chờ kiểm tra" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="statusLabel" value="Không rõ" />
                                    </c:otherwise>
                                </c:choose>
                            </h3>
                            <div class="room-grid">
                                <%-- Render each room card inside the floor group. --%>
                                <c:forEach items="${entry.value}" var="room">
                                    <c:set var="roomStatus" value="${empty room.status ? '' : fn:toUpperCase(room.status)}" />
                                    <c:choose>
                                        <c:when test="${roomStatus eq 'AVAILABLE'}">
                                            <c:set var="statusLabel" value="Trống" />
                                        </c:when>
                                        <c:when test="${roomStatus eq 'OCCUPIED'}">
                                            <c:set var="statusLabel" value="Có khách" />
                                        </c:when>
                                        <c:when test="${roomStatus eq 'CLEANING'}">
                                            <c:set var="statusLabel" value="Đang dọn" />
                                        </c:when>
                                        <c:when test="${roomStatus eq 'MAINTENANCE'}">
                                            <c:set var="statusLabel" value="Bảo trì" />
                                        </c:when>
                                        <c:when test="${roomStatus eq 'NOT_READY'}">
                                            <c:set var="statusLabel" value="Chưa sẵn sàng" />
                                        </c:when>
                                        <c:when test="${roomStatus eq 'INSPECTION'}">
                                            <c:set var="statusLabel" value="Chờ kiểm tra" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="statusLabel" value="Không rõ" />
                                        </c:otherwise>
                                    </c:choose>

                                    <div class="room-card js-room-card ${roomStatus}"
                                         tabindex="0"
                                         role="button"
                                         aria-label="Xem chi tiết phòng ${room.roomNumber}"
                                         data-room-id="${room.id}"
                                         data-room-number="${room.roomNumber}"
                                         data-room-type="${room.roomTypeName}"
                                         data-room-status="${roomStatus}"
                                         data-room-status-label="${statusLabel}"
                                         data-room-floor="${entry.key == 0 ? 'Không rõ' : entry.key}"
                                         data-room-description="${room.description}"
                                         data-booking-id="${room.currentBookingId}"
                                         data-booking-code="${room.currentBookingCode}"
                                         data-guest-name="${room.currentGuestName}"
                                         data-booking-status="${room.currentBookingStatus}">
                                        <div class="room-number"><c:out value="${room.roomNumber}" /></div>
                                        <div class="room-type">
                                            <c:choose>
                                                <c:when test="${empty room.roomTypeName}">--</c:when>
                                                <c:otherwise><c:out value="${room.roomTypeName}" /></c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="room-status"><c:out value="${statusLabel}" /></div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p>Không có phòng nào trong hệ thống.</p>
            </c:otherwise>
        </c:choose>
    </main>

    <div id="drawerBackdrop" class="drawer-backdrop"></div>
    <aside id="roomDrawer" class="room-drawer" aria-hidden="true" aria-label="Chi tiết phòng">
        <div class="drawer-header">
            <div>
                <p class="drawer-note drawer-section-title">Chi tiết phòng</p>
                <h3 id="drawerRoomNumber" class="drawer-title">--</h3>
            </div>
            <button type="button" id="drawerCloseBtn" class="drawer-close" aria-label="Đóng drawer">×</button>
        </div>
        <div class="drawer-body">
            <div id="drawerStatusChip" class="drawer-chip neutral">Không rõ</div>

            <div class="drawer-card">
                <div class="drawer-meta">
                    <div class="meta-row">
                        <div class="meta-label">Loại phòng</div>
                        <div id="drawerRoomType" class="meta-value">--</div>
                    </div>
                    <div class="meta-row">
                        <div class="meta-label">Tầng</div>
                        <div id="drawerRoomFloor" class="meta-value">--</div>
                    </div>
                    <div class="meta-row">
                        <div class="meta-label">Trạng thái</div>
                        <div id="drawerRoomStatus" class="meta-value">--</div>
                    </div>
                    <div class="meta-row">
                        <div class="meta-label">Mô tả</div>
                        <div id="drawerRoomDescription" class="meta-value">--</div>
                    </div>
                </div>
            </div>

            <div class="drawer-card">
                <h4 class="drawer-section-title">Hành động nhanh</h4>
                <div class="drawer-actions">
                    <button type="button" id="changeRoomBtn" class="btn btn-secondary">Change Room</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/reception/check-in">Go to Check-in</a>
                </div>
                <p class="drawer-note drawer-note-spaced">
                    Drawer này đang hiển thị thông tin từ trạng thái phòng. Nếu phòng đang có khách, lễ tân có thể mở modal đổi phòng để xử lý theo đúng luồng reception.
                </p>
            </div>
        </div>
    </aside>

    <jsp:include page="/WEB-INF/views/reception/modals/room-change-modal.jsp" />

    <script src="${pageContext.request.contextPath}/assets/js/room-map.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/room-change-modal.js"></script>
</body>
</html>
