<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý loại phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/rooms.css">
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">Quản lý khách sạn</p>
          <h1>Quản lý loại phòng</h1>
          <p>Quản lý hạng phòng, giá cơ bản, sức chứa và trạng thái hoạt động của từng loại phòng.</p>
        </div>

        <div class="room-management-hero__actions">
          <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/rooms">
            Quản lý phòng
          </a>
          <button class="btn" type="button" data-room-mgmt-open="room-type" onclick="window.RoomManagement && window.RoomManagement.openRoomTypeModal()">
            + Thêm loại phòng
          </button>
        </div>
      </section>

      <c:if test="${not empty sessionScope.toastMessage}">
        <div class="toast ${sessionScope.toastType}">
          <c:out value="${sessionScope.toastMessage}" />
        </div>
        <c:remove var="toastMessage" scope="session" />
        <c:remove var="toastType" scope="session" />
      </c:if>

      <section class="room-management-content">
        <section class="room-management-panel panel">
            <div class="room-management-toolbar">
                <form class="room-management-filters" method="get" action="${pageContext.request.contextPath}/manager/room-types">
                    <div class="room-management-filters__search">
                        <input type="search" name="keyword" value="${pageData.keyword}" placeholder="Tìm loại phòng...">
                    </div>
                    <div class="room-management-filters__select">
                        <select name="roomTypeStatus">
                            <option value="" ${empty pageData.roomTypeStatus ? 'selected' : ''}>Trạng thái: Tất cả</option>
                            <option value="ACTIVE" ${pageData.roomTypeStatus eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                            <option value="INACTIVE" ${pageData.roomTypeStatus eq 'INACTIVE' ? 'selected' : ''}>Ngừng hoạt động</option>
                        </select>
                    </div>
                    <button class="btn btn-secondary" type="submit">Lọc</button>
                </form>
                <button class="btn" type="button" data-room-mgmt-open="room-type">+ Thêm loại phòng</button>
            </div>

            <div class="room-management-table-wrap" data-pagination-root data-pagination-key="room-types-table" data-pagination-size="5">
                <table class="room-management-table">
                    <thead>
                    <tr>
                        <th>Tên</th>
                        <th>Giá cơ bản</th>
                        <th>Sức chứa</th>
                        <th>Tổng số phòng</th>
                        <th>Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty roomTypes}">
                            <tr>
                                <td colspan="5">
                                    <div class="room-management-empty">
                                        <strong>Chưa có loại phòng nào</strong>
                                        <span>Hãy tạo loại phòng đầu tiên để bắt đầu quản lý.</span>
                                    </div>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <%-- Render the room type table rows. --%>
                            <c:forEach var="roomType" items="${roomTypes}">
                                <tr data-pagination-item>
                                    <td>
                                        <div class="room-management-primary">
                                            <strong><c:out value="${roomType.name}" /></strong>
                                            <span><c:out value="${roomType.description}" /></span>
                                        </div>
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${roomType.basePrice}" type="number" groupingUsed="true" maxFractionDigits="0" />
                                    </td>
                                    <td><c:out value="${roomType.capacity}" /></td>
                                    <td><c:out value="${roomType.totalQuantity}" /></td>
                                    <td>
                                        <div class="room-management-actions">
                                            <button
                                                type="button"
                                                class="btn btn-secondary btn-sm"
                                                data-room-mgmt-edit-room-type="true"
                                                data-room-type-id="${roomType.id}"
                                                data-room-type-name="${roomType.name}"
                                                data-room-type-description="${roomType.description}"
                                                data-room-type-capacity="${roomType.capacity}"
                                                data-room-type-base-price="${roomType.basePrice}"
                                                data-room-type-status="${roomType.status}">
                                                Sửa
                                            </button>
                                            <a class="btn btn-danger btn-sm"
                                               href="${pageContext.request.contextPath}/manager/room-types/deactivate-room-type?id=${roomType.id}"
                                               data-room-mgmt-confirm="true"
                                               data-room-mgmt-confirm-message="Bạn có muốn ngừng hoạt động loại phòng này không?">
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
                <div class="room-management-pagination" data-pagination-controls></div>
            </div>

            <div class="room-management-mobile-list" data-pagination-root data-pagination-key="room-types-mobile" data-pagination-size="5">
                <%-- Render the room type mobile cards. --%>
                <c:forEach var="roomType" items="${roomTypes}">
                    <article class="room-management-card" data-pagination-item>
                        <div class="room-management-card__head">
                            <div>
                                <h3><c:out value="${roomType.name}" /></h3>
                                <p><c:out value="${roomType.description}" /></p>
                            </div>
                            <span class="status-chip ${roomType.status eq 'ACTIVE' ? 'status-working' : 'status-cancelled'}">
                                <c:out value="${roomType.status eq 'ACTIVE' ? 'Hoạt động' : 'Ngừng hoạt động'}" />
                            </span>
                        </div>
                        <dl class="room-management-meta">
                            <div><dt>Giá cơ bản</dt><dd><fmt:formatNumber value="${roomType.basePrice}" type="number" groupingUsed="true" maxFractionDigits="0" /></dd></div>
                            <div><dt>Sức chứa</dt><dd><c:out value="${roomType.capacity}" /></dd></div>
                            <div><dt>Tổng số phòng</dt><dd><c:out value="${roomType.totalQuantity}" /></dd></div>
                        </dl>
                        <div class="room-management-actions">
                            <button
                                type="button"
                                class="btn btn-secondary btn-sm"
                                data-room-mgmt-edit-room-type="true"
                                data-room-type-id="${roomType.id}"
                                data-room-type-name="${roomType.name}"
                                data-room-type-description="${roomType.description}"
                                data-room-type-capacity="${roomType.capacity}"
                                data-room-type-base-price="${roomType.basePrice}"
                                data-room-type-status="${roomType.status}">
                                Sửa
                            </button>
                            <a class="btn btn-danger btn-sm"
                               href="${pageContext.request.contextPath}/manager/room-types/deactivate-room-type?id=${roomType.id}"
                               data-room-mgmt-confirm="true"
                               data-room-mgmt-confirm-message="Bạn có muốn ngừng hoạt động loại phòng này không?">
                                Ngừng hoạt động
                            </a>
                        </div>
                    </article>
                </c:forEach>
                <div class="room-management-pagination" data-pagination-controls></div>
            </div>
        </section>
      </section>
    </main>

    <jsp:include page="/WEB-INF/views/manager/modals/room-type-modal.jsp" />

    <script src="${pageContext.request.contextPath}/assets/js/rooms.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/pagination.js"></script>
  </body>
</html>
