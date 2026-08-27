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
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/equipment.css?v=20260824-1" />
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel equipment-hero">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">QUẢN LÝ KHÁCH SẠN</p>
          <h1>Quản lý Thiết bị & Vật tư</h1>
          <p>Theo dõi danh mục thiết bị, phân loại khả năng bảo trì và cấu hình đơn giá đền bù phòng.</p>
        </div>
        <div class="room-management-hero__actions">
          <a class="btn" href="${cp}/manager/equipment/new">+ Thêm thiết bị</a>
        </div>
      </section>

      <section class="equipment-stats-grid">
        <article class="equipment-stat-card">
          <div class="equipment-stat-card__icon equipment-stat-card__icon--total">TB</div>
          <div class="equipment-stat-card__body">
            <span class="equipment-stat-card__label">Tổng thiết bị</span>
            <strong class="equipment-stat-card__value">${equipmentCount}</strong>
            <small>Toàn bộ danh mục trong hệ thống</small>
          </div>
        </article>
        <article class="equipment-stat-card">
          <div class="equipment-stat-card__icon equipment-stat-card__icon--active">HD</div>
          <div class="equipment-stat-card__body">
            <span class="equipment-stat-card__label">Đang hoạt động</span>
            <strong class="equipment-stat-card__value">${activeEquipmentCount}</strong>
            <small>Được phép gán vào phòng</small>
          </div>
        </article>
        <article class="equipment-stat-card">
          <div class="equipment-stat-card__icon equipment-stat-card__icon--maintainable">BT</div>
          <div class="equipment-stat-card__body">
            <span class="equipment-stat-card__label">Có thể bảo trì</span>
            <strong class="equipment-stat-card__value">${maintainableEquipmentCount}</strong>
            <small>Thiết bị có luồng xử lý bảo trì</small>
          </div>
        </article>
        <article class="equipment-stat-card">
          <div class="equipment-stat-card__icon equipment-stat-card__icon--inactive">NG</div>
          <div class="equipment-stat-card__body">
            <span class="equipment-stat-card__label">Ngừng hoạt động / Ẩn</span>
            <strong class="equipment-stat-card__value">${inactiveEquipmentCount}</strong>
            <small>Không dùng trong phòng mới</small>
          </div>
        </article>
      </section>

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
              <div class="room-management-filters__select">
                <select name="maintainable">
                  <option value="ALL" ${empty maintainable or maintainable eq 'ALL' ? 'selected' : ''}>Bảo trì: Tất cả</option>
                  <option value="YES" ${maintainable eq 'YES' ? 'selected' : ''}>Có thể bảo trì</option>
                  <option value="NO" ${maintainable eq 'NO' ? 'selected' : ''}>Chỉ thay thế</option>
                </select>
              </div>
              <div class="room-management-filters__select">
                <select name="hasImage">
                  <option value="ALL" ${empty hasImage or hasImage eq 'ALL' ? 'selected' : ''}>Ảnh: Tất cả</option>
                  <option value="YES" ${hasImage eq 'YES' ? 'selected' : ''}>Có ảnh</option>
                  <option value="NO" ${hasImage eq 'NO' ? 'selected' : ''}>Chưa có ảnh</option>
                </select>
              </div>
              <button class="btn btn-primary equipment-icon-btn" type="submit" aria-label="Lọc" title="Lọc">
                <i class="bi bi-funnel" aria-hidden="true"></i>
              </button>
              <a class="btn btn-secondary equipment-icon-btn equipment-reset-btn" href="${cp}/manager/equipment" aria-label="Đặt lại" title="Đặt lại">
                <i class="bi bi-arrow-counterclockwise" aria-hidden="true"></i>
              </a>
            </form>
          </div>

          <div class="room-management-table-wrap" data-pagination-root data-pagination-key="equipment" data-pagination-size="5">
            <table class="room-management-table equipment-table">
              <colgroup>
                <col class="equipment-table__col equipment-table__col--image" />
                <col class="equipment-table__col equipment-table__col--name" />
                <col class="equipment-table__col equipment-table__col--maintainable" />
                <col class="equipment-table__col equipment-table__col--description" />
                <col class="equipment-table__col equipment-table__col--price" />
                <col class="equipment-table__col equipment-table__col--status" />
                <col class="equipment-table__col equipment-table__col--actions" />
              </colgroup>
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
                            <a class="btn btn-secondary btn-sm equipment-icon-btn" href="${cp}/manager/equipment/edit?id=${equipment.id}" aria-label="Sửa" title="Sửa">
                              <i class="bi bi-pencil" aria-hidden="true"></i>
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
                  <a class="btn btn-secondary btn-sm equipment-icon-btn" href="${cp}/manager/equipment/edit?id=${equipment.id}" aria-label="Sửa" title="Sửa">
                    <i class="bi bi-pencil" aria-hidden="true"></i>
                  </a>
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
