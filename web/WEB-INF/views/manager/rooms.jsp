<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý phòng | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260824-2" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260824-2" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">Quản lý phòng</p>
          <h1>Quản lý phòng</h1>
          <p>Danh sách phòng, trạng thái, và thiết bị gắn theo phòng</p>
        </div>

        <div class="room-management-hero__actions" style="display:flex; gap:5px; align-items:center;">
          <a class="btn btn-secondary" href="${cp}/manager/room-map">Sơ đồ phòng</a>
          <a class="btn" href="${cp}/manager/rooms/new">+ Thêm phòng</a>
        </div>
      </section>

      <c:if test="${not empty sessionScope.toastMessage}">
        <div class="toast ${sessionScope.toastType}">
          <c:out value="${sessionScope.toastMessage}" />
        </div>
        <c:remove var="toastMessage" scope="session" />
        <c:remove var="toastType" scope="session" />
      </c:if>

      <section class="room-management-stats-grid">
        <article class="room-management-stat-card">
          <div class="room-management-stat-card__icon room-management-stat-card__icon--total">PH</div>
          <div class="room-management-stat-card__body">
            <span class="room-management-stat-card__label">Tổng phòng</span>
            <strong class="room-management-stat-card__value">${roomCount}</strong>
            <small>Đã tạo trong hệ thống</small>
          </div>
        </article>
        <article class="room-management-stat-card">
          <div class="room-management-stat-card__icon room-management-stat-card__icon--available">TR</div>
          <div class="room-management-stat-card__body">
            <span class="room-management-stat-card__label">Phòng trống</span>
            <strong class="room-management-stat-card__value">${availableRoomCount}</strong>
            <small>Sẵn sàng nhận khách</small>
          </div>
        </article>
        <article class="room-management-stat-card">
          <div class="room-management-stat-card__icon room-management-stat-card__icon--occupied">OC</div>
          <div class="room-management-stat-card__body">
            <span class="room-management-stat-card__label">Đang có khách</span>
            <strong class="room-management-stat-card__value">${occupiedRoomCount}</strong>
            <small>Đang được sử dụng</small>
          </div>
        </article>
        <article class="room-management-stat-card">
          <div class="room-management-stat-card__icon room-management-stat-card__icon--maintenance">BT</div>
          <div class="room-management-stat-card__body">
            <span class="room-management-stat-card__label">Bảo trì / chưa sẵn sàng</span>
            <strong class="room-management-stat-card__value">${maintenanceRoomCount}</strong>
            <small>Cần xử lý trước khi bán</small>
          </div>
        </article>
      </section>

      <section class="room-management-content">
        <section class="room-management-panel panel">
          <div class="room-management-toolbar">
            <form class="room-management-filters room-management-filters--rooms" method="get" action="${cp}/manager/rooms">
              <div class="room-management-filters__search">
                <input type="search" name="keyword" value="${pageData.keyword}" placeholder="Tìm phòng..." />
              </div>
              <div class="room-management-filters__select">
                <select name="floor">
                  <option value="" ${empty pageData.floor ? 'selected' : ''}>Tầng: Tất cả</option>
                  <c:forEach var="f" items="${floorOptions}">
                    <option value="${f}" ${pageData.floor eq f ? 'selected' : ''}>Tầng ${f}</option>
                  </c:forEach>
                </select>
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
              <div class="room-management-filters__select">
                <select name="equipmentFilter">
                  <option value="ALL" ${empty pageData.equipmentFilter or pageData.equipmentFilter eq 'ALL' ? 'selected' : ''}>Thiết bị: Tất cả</option>
                  <option value="YES" ${pageData.equipmentFilter eq 'YES' ? 'selected' : ''}>Có thiết bị</option>
                  <option value="NO" ${pageData.equipmentFilter eq 'NO' ? 'selected' : ''}>Không có thiết bị</option>
                </select>
              </div>
              <button class="btn btn-secondary" type="submit">Lọc</button>
              <a class="btn btn-secondary" href="${cp}/manager/rooms">Đặt lại</a>
            </form>
          </div>

          <div class="room-management-table-wrap" data-pagination-root data-pagination-key="rooms" data-pagination-size="5">
            <table class="room-management-table">
              <thead>
                <tr>
                  <th>Phòng</th>
                  <th>Tầng</th>
                  <th>Loại phòng</th>
                  <th>Thiết bị</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <c:choose>
                  <c:when test="${empty rooms}">
                    <tr>
                      <td colspan="6">
                        <div class="room-management-empty">
                          <strong>Chưa có phòng nào.</strong>
                          <span>Hãy thêm phòng đầu tiên để bắt đầu gán thiết bị.</span>
                        </div>
                      </td>
                    </tr>
                  </c:when>
                  <c:otherwise>
                    <c:forEach var="room" items="${rooms}">
                      <c:set var="roomEquipmentSummary" value="${roomEquipmentSummaries[room.id]}" />
                      <tr data-pagination-item>
                        <td>
                          <div class="room-number-pill">
                            <c:out value="${room.roomNumber}" />
                          </div>
                        </td>
                        <td><c:out value="${empty room.floorNumber ? '-' : room.floorNumber}" /></td>
                        <td>
                          <div class="room-management-primary">
                            <strong><c:out value="${room.roomTypeName}" /></strong>
                            <span>#<c:out value="${room.roomTypeId}" /></span>
                          </div>
                        </td>
                        <td>
                          <div
                            class="room-management-equipment"
                            title="<c:out value='${roomEquipmentSummary}' />">
                            <c:out value="${roomEquipmentSummary}" />
                          </div>
                        </td>
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
                            <a class="btn btn-secondary btn-sm" href="${cp}/manager/rooms/edit?id=${room.id}">Sửa</a>
                            <button
                              type="button"
                              class="btn btn-secondary btn-sm"
                              onclick="window.RoomManagement && window.RoomManagement.openTaskModal(${room.id}, '${room.roomNumber}', ${empty room.floorNumber ? 0 : room.floorNumber})">
                              Giao việc
                            </button>
                            <a
                              class="btn btn-danger btn-sm"
                              href="${cp}/manager/rooms/deactivate-room?id=${room.id}"
                              data-room-mgmt-confirm="true"
                              data-room-mgmt-confirm-message="Bạn có muốn ngừng hoạt động phòng này không?">
                              Ngừng hoạt động
                            </a>
                          </div>
                        </td>
                      </tr>
                    </c:forEach>
                  </c:otherwise>
                </c:choose>
              </tbody>
            </table>
          </div>

          <div class="room-management-mobile-list" data-pagination-root data-pagination-key="rooms" data-pagination-size="5">
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
                  <div><dt>Thiết bị</dt><dd class="room-management-equipment"><c:out value="${roomEquipmentSummary}" /></dd></div>
                  <div><dt>Trạng thái</dt><dd><c:out value="${room.status}" /></dd></div>
                </dl>

                <div class="room-management-actions">
                  <a class="btn btn-secondary btn-sm" href="${cp}/manager/rooms/edit?id=${room.id}">Sửa</a>
                  <button
                    type="button"
                    class="btn btn-secondary btn-sm"
                    onclick="window.RoomManagement && window.RoomManagement.openTaskModal(${room.id}, '${room.roomNumber}', ${empty room.floorNumber ? 0 : room.floorNumber})">
                    Giao việc
                  </button>
                  <a
                    class="btn btn-danger btn-sm"
                    href="${cp}/manager/rooms/deactivate-room?id=${room.id}"
                    data-room-mgmt-confirm="true"
                    data-room-mgmt-confirm-message="Bạn có muốn ngừng hoạt động phòng này không?">
                    Ngừng hoạt động
                  </a>
                </div>
              </article>
            </c:forEach>
          </div>

          <div class="room-management-pagination" data-pagination-controls data-pagination-target="rooms"></div>
        </section>
      </section>
    </main>

    <jsp:include page="/WEB-INF/views/manager/modals/task-modal.jsp" />
    <script src="${cp}/assets/js/rooms.js?v=20260824-2"></script>
    <script src="${cp}/assets/js/pagination.js?v=20260824-2"></script>
  </body>
</html>
