<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<section class="room-management-panel panel">
    <div class="room-management-toolbar">
        <form class="room-management-filters room-management-filters--rooms" method="get" action="${pageContext.request.contextPath}/manager/rooms">
            <input type="hidden" name="tab" value="rooms">
            <input type="hidden" name="roomTypeStatus" value="${pageData.roomTypeStatus}">
            <div class="room-management-filters__search">
                <input type="search" name="keyword" value="${pageData.keyword}" placeholder="Tìm phòng...">
            </div>
            <div class="room-management-filters__select">
                <input type="number" name="floor" value="${pageData.floor}" min="0" placeholder="Tầng: Tất cả">
            </div>
            <div class="room-management-filters__select">
                <select name="roomTypeId">
                    <option value="" ${empty pageData.roomTypeId ? 'selected' : ''}>Loại phòng: Tất cả</option>
                    <c:forEach var="option" items="${roomTypeOptions}">
                        <option value="${option.id}" ${pageData.roomTypeId eq option.id ? 'selected' : ''}>
                            <c:out value="${option.name}" />
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="room-management-filters__select">
                <select name="roomStatus">
                    <option value="" ${empty pageData.roomStatus ? 'selected' : ''}>Trạng thái: Tất cả</option>
                    <option value="AVAILABLE" ${pageData.roomStatus eq 'AVAILABLE' ? 'selected' : ''}>Trống</option>
                    <option value="OCCUPIED" ${pageData.roomStatus eq 'OCCUPIED' ? 'selected' : ''}>Đang có khách</option>
                    <option value="CLEANING" ${pageData.roomStatus eq 'CLEANING' ? 'selected' : ''}>Đang dọn</option>
                    <option value="MAINTENANCE" ${pageData.roomStatus eq 'MAINTENANCE' ? 'selected' : ''}>Bảo trì</option>
                    <option value="NOT_READY" ${pageData.roomStatus eq 'NOT_READY' ? 'selected' : ''}>Chưa sẵn sàng</option>
                    <option value="INSPECTION" ${pageData.roomStatus eq 'INSPECTION' ? 'selected' : ''}>Chờ kiểm tra</option>
                </select>
            </div>
            <button class="btn btn-secondary" type="submit">Lọc</button>
        </form>
        <button class="btn" type="button" data-room-mgmt-open="room">+ Thêm phòng</button>
    </div>

    <div class="room-management-table-wrap" data-pagination-root data-pagination-key="rooms-table" data-pagination-size="5">
        <table class="room-management-table">
            <thead>
            <tr>
                <th>Số phòng</th>
                <th>Tầng</th>
                <th>Loại phòng</th>
                <th>Trạng thái hiện tại</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty rooms}">
                    <tr>
                        <td colspan="5">
                            <div class="room-management-empty">
                                <strong>Chưa có phòng nào</strong>
                                <span>Hãy thêm phòng và gán vào một loại phòng.</span>
                            </div>
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="room" items="${rooms}">
                        <tr data-pagination-item>
                            <td>
                                <div class="room-number-pill">
                                    <c:out value="${room.roomNumber}" />
                                </div>
                            </td>
                            <td><c:out value="${empty room.floorNumber ? '-' : room.floorNumber}" /></td>
                            <td><c:out value="${room.roomTypeName}" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${room.status eq 'AVAILABLE'}">
                                        <span class="status-chip status-available">Trống</span>
                                    </c:when>
                                    <c:when test="${room.status eq 'OCCUPIED'}">
                                        <span class="status-chip status-occupied">Đang có khách</span>
                                    </c:when>
                                    <c:when test="${room.status eq 'CLEANING'}">
                                        <span class="status-chip status-cleaning">Đang dọn</span>
                                    </c:when>
                                    <c:when test="${room.status eq 'MAINTENANCE'}">
                                        <span class="status-chip status-maintenance">Bảo trì</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-chip status-pending"><c:out value="${room.status}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="room-management-actions">
                                    <button
                                        type="button"
                                        class="btn btn-secondary btn-sm"
                                        data-room-mgmt-edit-room="true"
                                        data-room-id="${room.id}"
                                        data-room-number="${room.roomNumber}"
                                        data-room-type-id="${room.roomTypeId}"
                                        data-room-floor="${room.floorNumber}"
                                        data-room-status="${room.status}"
                                        data-room-description="${room.description}">
                                        Sửa
                                    </button>
                                    <a class="btn btn-danger btn-sm"
                                       href="${pageContext.request.contextPath}/manager/rooms/deactivate-room?id=${room.id}"
                                       data-room-mgmt-confirm="true"
                                       data-room-mgmt-confirm-message="Bạn có muốn ngưng hoạt động phòng này không?">
                                        Ngưng hoạt động
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
        <div class="room-management-pagination" data-pagination-controls></div>
    </div>

    <div class="room-management-mobile-list" data-pagination-root data-pagination-key="rooms-mobile" data-pagination-size="5">
        <c:forEach var="room" items="${rooms}">
            <article class="room-management-card" data-pagination-item>
                <div class="room-management-card__head">
                    <div>
                        <h3>Phòng <c:out value="${room.roomNumber}" /></h3>
                        <p><c:out value="${room.roomTypeName}" /> · Tầng <c:out value="${empty room.floorNumber ? '-' : room.floorNumber}" /></p>
                    </div>
                    <c:choose>
                        <c:when test="${room.status eq 'AVAILABLE'}">
                            <span class="status-chip status-available">Trống</span>
                        </c:when>
                        <c:when test="${room.status eq 'OCCUPIED'}">
                            <span class="status-chip status-occupied">Đang có khách</span>
                        </c:when>
                        <c:when test="${room.status eq 'CLEANING'}">
                            <span class="status-chip status-cleaning">Đang dọn</span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-chip status-maintenance"><c:out value="${room.status}" /></span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <dl class="room-management-meta">
                    <div><dt>Loại phòng</dt><dd><c:out value="${room.roomTypeName}" /></dd></div>
                    <div><dt>Tầng</dt><dd><c:out value="${empty room.floorNumber ? '-' : room.floorNumber}" /></dd></div>
                    <div><dt>Trạng thái</dt><dd><c:out value="${room.status}" /></dd></div>
                </dl>
                <div class="room-management-actions">
                    <button
                        type="button"
                        class="btn btn-secondary btn-sm"
                        data-room-mgmt-edit-room="true"
                        data-room-id="${room.id}"
                        data-room-number="${room.roomNumber}"
                        data-room-type-id="${room.roomTypeId}"
                        data-room-floor="${room.floorNumber}"
                        data-room-status="${room.status}"
                        data-room-description="${room.description}">
                        Sửa
                    </button>
                    <a class="btn btn-danger btn-sm"
                       href="${pageContext.request.contextPath}/manager/rooms/deactivate-room?id=${room.id}"
                       data-room-mgmt-confirm="true"
                       data-room-mgmt-confirm-message="Bạn có muốn ngưng hoạt động phòng này không?">
                        Ngưng hoạt động
                    </a>
                </div>
            </article>
        </c:forEach>
        <div class="room-management-pagination" data-pagination-controls></div>
    </div>
</section>
