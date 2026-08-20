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
    <style>
      .equipment-thumb-img {
        width: 44px;
        height: 44px;
        border-radius: 8px;
        object-fit: cover;
        border: 1px solid #e2e8f0;
        background: #f8fafc;
        display: block;
      }
      .equipment-thumb-fallback {
        width: 44px;
        height: 44px;
        border-radius: 8px;
        background: #eff6ff;
        color: #2563eb;
        font-weight: 700;
        font-size: 13px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border: 1px solid #dbeafe;
      }
    </style>
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">QUẢN LÝ KHÁCH SẠN</p>
          <h1>Quản lý Thiết bị & Vật tư</h1>
          <p>
            Theo dõi danh mục thiết bị, phân loại khả năng bảo trì và cấu hình đơn giá đền bù phòng.
          </p>
        </div>
        <div class="room-management-hero__actions">
          <a class="btn" href="${cp}/manager/equipment/new">
            + Thêm thiết bị
          </a>
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
            <form class="room-management-filters" method="get" action="${cp}/manager/equipment" style="grid-template-columns: minmax(260px, 2fr) minmax(180px, 1fr) auto auto; gap: var(--space-3); align-items: end;">
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
              <a class="btn btn-secondary" href="${cp}/manager/equipment" style="text-decoration: none; text-align: center;">Đặt lại</a>
            </form>
          </div>

          <div class="room-management-table-wrap" data-pagination-root data-pagination-key="equipment-table" data-pagination-size="5">
            <table class="room-management-table">
              <thead>
                <tr>
                  <th style="width: 60px; text-align: center;">Ảnh</th>
                  <th>Tên thiết bị</th>
                  <th>Phân loại bảo trì</th>
                  <th>Mô tả / Danh mục</th>
                  <th>Đơn giá đền bù</th>
                  <th>Trạng thái</th>
                  <th style="text-align: right;">Thao tác</th>
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
                        <td style="text-align: center;">
                          <c:choose>
                            <c:when test="${not empty equipment.imageUrl}">
                              <img class="equipment-thumb-img" src="${cp}${equipment.imageUrl}" alt="${equipment.name}" />
                            </c:when>
                            <c:otherwise>
                              <div class="equipment-thumb-fallback">TB</div>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <div class="room-number-pill" style="display: inline-block;">
                            <strong><c:out value="${equipment.name}" /></strong>
                          </div>
                          <small style="color: #64748b; margin-left: 6px;">#<c:out value="${equipment.id}" /></small>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${equipment.isMaintainable}">
                              <span style="font-size: 11.5px; font-weight: 600; color: #15803d; background: #dcfce7; border: 1px solid #bbf7d0; padding: 3px 8px; border-radius: 6px; display: inline-flex; align-items: center; gap: 4px;">
                                🟢 Có thể bảo trì
                              </span>
                            </c:when>
                            <c:otherwise>
                              <span style="font-size: 11.5px; font-weight: 600; color: #b45309; background: #fef3c7; border: 1px solid #fde68a; padding: 3px 8px; border-radius: 6px; display: inline-flex; align-items: center; gap: 4px;">
                                🟡 Chỉ thay thế
                              </span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <span style="color: #475569; font-size: 13.5px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                            <c:out value="${empty equipment.description ? '-' : equipment.description}" />
                          </span>
                        </td>
                        <td>
                          <strong style="color: #0f172a; font-size: 14px;">
                            <fmt:formatNumber value="${equipment.defaultCompensationPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                          </strong>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${equipment.status eq 'ACTIVE'}">
                              <span class="status-chip status-available">Đang hoạt động</span>
                            </c:when>
                            <c:otherwise>
                              <span class="status-chip status-maintenance">Ngừng hoạt động</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td style="text-align: right;">
                          <div class="room-management-actions" style="justify-content: flex-end;">
                            <a class="btn btn-secondary btn-sm" href="${cp}/manager/equipment/edit?id=${equipment.id}">
                              Sửa
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

          <div class="room-management-mobile-list" data-pagination-root data-pagination-key="equipment-mobile" data-pagination-size="5">
            <c:forEach var="equipment" items="${equipments}">
              <article class="room-management-card" data-pagination-item>
                <div class="room-management-card__head">
                  <div style="display: flex; gap: 12px; align-items: center;">
                    <c:choose>
                      <c:when test="${not empty equipment.imageUrl}">
                        <img class="equipment-thumb-img" src="${cp}${equipment.imageUrl}" alt="${equipment.name}" />
                      </c:when>
                      <c:otherwise>
                        <div class="equipment-thumb-fallback">TB</div>
                      </c:otherwise>
                    </c:choose>
                    <div>
                      <h3 style="margin: 0;"><c:out value="${equipment.name}" /></h3>
                      <small style="color: #64748b;">#<c:out value="${equipment.id}" /></small>
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
                <div style="margin: 8px 0;">
                  <c:choose>
                    <c:when test="${equipment.isMaintainable}">
                      <span style="font-size: 11.5px; font-weight: 600; color: #15803d; background: #dcfce7; border: 1px solid #bbf7d0; padding: 2px 8px; border-radius: 6px;">
                        🟢 Có thể bảo trì
                      </span>
                    </c:when>
                    <c:otherwise>
                      <span style="font-size: 11.5px; font-weight: 600; color: #b45309; background: #fef3c7; border: 1px solid #fde68a; padding: 2px 8px; border-radius: 6px;">
                        🟡 Chỉ thay thế
                      </span>
                    </c:otherwise>
                  </c:choose>
                </div>
                <dl class="room-management-meta">
                  <div><dt>Mô tả</dt><dd><c:out value="${empty equipment.description ? '-' : equipment.description}" /></dd></div>
                  <div>
                    <dt>Giá đền bù</dt>
                    <dd><strong><fmt:formatNumber value="${equipment.defaultCompensationPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ</strong></dd>
                  </div>
                </dl>
                <div class="room-management-actions">
                  <a class="btn btn-secondary btn-sm" href="${cp}/manager/equipment/edit?id=${equipment.id}">
                    Sửa
                  </a>
                </div>
              </article>
            </c:forEach>
            <div class="room-management-pagination" data-pagination-controls></div>
          </div>
        </section>
      </section>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${cp}/assets/js/pagination.js?v=20260820-7"></script>
  </body>
</html>