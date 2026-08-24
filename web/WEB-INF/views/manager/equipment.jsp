<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý Thiết bị & Vật tư | HMS</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/equipment.css?v=20260824-1" />
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">QUẢN LÝ KHÁCH SẠN</p>
          <h1>Quản lý Thiết bị & Vật tư</h1>
          <p>Theo dõi danh mục thiết bị, phân loại khả năng bảo trì và cấu hình đơn giá đền bù phòng.</p>
        </div>
        <div class="room-management-hero__actions">
          <a class="btn" href="${cp}/manager/equipment/new">+ Thêm thiết bị</a>
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
            <form class="room-management-filters equipment-filters" method="get" action="${cp}/manager/equipment">
              <div class="room-management-filters__search">
                <input type="search" name="keyword" value="${keyword}" placeholder="Tìm thiết bị, mô tả..." />
              </div>
              <div class="room-management-filters__select">
                <select name="status">
                  <option value="ALL" ${empty status or status eq 'ALL' ? 'selected' : ''}>Trạng thái: Tất cả</option>
                  <option value="ACTIVE" ${status eq 'ACTIVE' ? 'selected' : ''}>Đang hoạt động</option>
                  <option value="INACTIVE" ${status eq 'INACTIVE' ? 'selected' : ''}>Ngừng hoạt động</option>
                </select>
              </div>
              <button class="btn btn-primary" type="submit">Lọc</button>
              <a class="btn btn-secondary equipment-reset-btn" href="${cp}/manager/equipment">Đặt lại</a>
            </form>
          </div>

          <div class="room-management-table-wrap" data-pagination-root data-pagination-key="equipment" data-pagination-size="5">
            <table class="room-management-table equipment-table">
              <thead>
                <tr>
                  <th class="equipment-table__th equipment-table__th--image">Ảnh</th>
                  <th>Tên thiết bị</th>
                  <th>Phân loại bảo trì</th>
                  <th>Mô tả / Danh mục</th>
                  <th>Đơn giá đền bù</th>
                  <th>Trạng thái</th>
                  <th class="equipment-table__th equipment-table__th--actions">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <c:choose>
                  <c:when test="${empty equipments}">
                    <tr>
                      <td colspan="7">
                        <div class="room-management-empty">
                          <strong>Chưa có thiết bị nào</strong>
                          <span>Hãy thêm thiết bị mới để gán vào các phòng trong khách sạn.</span>
                        </div>
                      </td>
                    </tr>
                  </c:when>
                  <c:otherwise>
                    <c:forEach var="equipment" items="${equipments}">
                      <tr data-pagination-item>
                        <td class="equipment-table__cell equipment-table__cell--image">
                          <c:choose>
                            <c:when test="${not empty equipment.imageUrl}">
                              <div class="equipment-thumb">
                                <img class="equipment-thumb__image" src="${cp}${equipment.imageUrl}" alt="${equipment.name}" />
                              </div>
                            </c:when>
                            <c:otherwise>
                              <div class="equipment-thumb equipment-thumb--fallback">TB</div>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td class="equipment-table__cell">
                          <div class="equipment-item-name">
                            <strong><c:out value="${equipment.name}" /></strong>
                            <small class="equipment-item-name__id">#<c:out value="${equipment.id}" /></small>
                          </div>
                        </td>
                        <td class="equipment-table__cell">
                          <c:choose>
                            <c:when test="${equipment.isMaintainable}">
                              <span class="status-chip status-available equipment-maintainable-chip">Có thể bảo trì</span>
                            </c:when>
                            <c:otherwise>
                              <span class="status-chip status-maintenance equipment-maintainable-chip">Chỉ thay thế</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td class="equipment-table__cell">
                          <span class="equipment-description">
                            <c:out value="${empty equipment.description ? '-' : equipment.description}" />
                          </span>
                        </td>
                        <td class="equipment-table__cell">
                          <strong class="equipment-price">
                            <fmt:formatNumber value="${equipment.defaultCompensationPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                          </strong>
                        </td>
                        <td class="equipment-table__cell">
                          <c:choose>
                            <c:when test="${equipment.status eq 'ACTIVE'}">
                              <span class="status-chip status-available">Đang hoạt động</span>
                            </c:when>
                            <c:otherwise>
                              <span class="status-chip status-maintenance">Ngừng hoạt động</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td class="equipment-table__cell equipment-table__cell--actions">
                          <div class="room-management-actions equipment-actions">
                            <a class="btn btn-secondary btn-sm" href="${cp}/manager/equipment/edit?id=${equipment.id}">Sửa</a>
                          </div>
                        </td>
                      </tr>
                    </c:forEach>
                  </c:otherwise>
                </c:choose>
              </tbody>
            </table>
          </div>

          <div class="room-management-mobile-list" data-pagination-root data-pagination-key="equipment" data-pagination-size="5">
            <c:forEach var="equipment" items="${equipments}">
              <article class="room-management-card equipment-card" data-pagination-item>
                <div class="room-management-card__head equipment-card__head">
                  <div class="equipment-card__head-left">
                    <c:choose>
                      <c:when test="${not empty equipment.imageUrl}">
                        <div class="equipment-thumb">
                          <img class="equipment-thumb__image" src="${cp}${equipment.imageUrl}" alt="${equipment.name}" />
                        </div>
                      </c:when>
                      <c:otherwise>
                        <div class="equipment-thumb equipment-thumb--fallback">TB</div>
                      </c:otherwise>
                    </c:choose>
                    <div class="equipment-item-name equipment-card__title">
                      <h3><c:out value="${equipment.name}" /></h3>
                      <small class="equipment-item-name__id">#<c:out value="${equipment.id}" /></small>
                    </div>
                  </div>
                  <c:choose>
                    <c:when test="${equipment.status eq 'ACTIVE'}">
                      <span class="status-chip status-available">Đang hoạt động</span>
                    </c:when>
                    <c:otherwise>
                      <span class="status-chip status-maintenance">Ngừng hoạt động</span>
                    </c:otherwise>
                  </c:choose>
                </div>

                <div class="equipment-card__badge-row">
                  <c:choose>
                    <c:when test="${equipment.isMaintainable}">
                      <span class="status-chip status-available equipment-maintainable-chip">Có thể bảo trì</span>
                    </c:when>
                    <c:otherwise>
                      <span class="status-chip status-maintenance equipment-maintainable-chip">Chỉ thay thế</span>
                    </c:otherwise>
                  </c:choose>
                </div>

                <dl class="room-management-meta equipment-card__meta">
                  <div class="equipment-card__meta-item">
                    <dt>Mô tả</dt>
                    <dd><c:out value="${empty equipment.description ? '-' : equipment.description}" /></dd>
                  </div>
                  <div class="equipment-card__meta-item">
                    <dt>Giá đền bù</dt>
                    <dd><strong class="equipment-price"><fmt:formatNumber value="${equipment.defaultCompensationPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ</strong></dd>
                  </div>
                </dl>

                <div class="room-management-actions equipment-actions">
                  <a class="btn btn-secondary btn-sm" href="${cp}/manager/equipment/edit?id=${equipment.id}">Sửa</a>
                </div>
              </article>
            </c:forEach>
          </div>

          <div class="room-management-pagination" data-pagination-controls data-pagination-target="equipment"></div>
        </section>
      </section>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${cp}/assets/js/pagination.js?v=20260820-7"></script>
  </body>
</html>
