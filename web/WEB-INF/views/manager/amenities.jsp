<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý tiện nghi | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">Quản lý khách sạn</p>
          <h1>Quản lý tiện nghi</h1>
          <p>Quản lý danh mục tiện ích, biểu tượng và gán tiện nghi vào từng loại phòng.</p>
        </div>

        <div class="room-management-hero__actions">
          <a class="btn btn-secondary" href="${cp}/manager/room-types">
            Quản lý loại phòng
          </a>
          <a class="btn" href="${cp}/manager/amenity/new">
            + Thêm tiện nghi
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
            <form class="room-management-filters" method="get" action="${cp}/manager/amenities" style="grid-template-columns: minmax(260px, 2fr) minmax(180px, 1fr) auto auto; gap: var(--space-3); align-items: end;">
              <div class="room-management-filters__search">
                <input type="search" name="keyword" value="${keyword}" placeholder="Tìm tiện nghi, mô tả..." />
              </div>
              <div class="room-management-filters__select">
                <select name="status">
                  <option value="ALL" ${empty status or status eq 'ALL' ? 'selected' : ''}>Trạng thái: Tất cả</option>
                  <option value="ACTIVE" ${status eq 'ACTIVE' ? 'selected' : ''}>Đang hoạt động</option>
                  <option value="INACTIVE" ${status eq 'INACTIVE' ? 'selected' : ''}>Ngừng hoạt động</option>
                </select>
              </div>
              <button class="btn btn-primary" type="submit">Lọc</button>
              <a class="btn btn-secondary" href="${cp}/manager/amenities" style="text-decoration: none; text-align: center;">Đặt lại</a>
            </form>
          </div>

          <div class="room-management-table-wrap" data-pagination-root data-pagination-key="amenities" data-pagination-size="5">
            <table class="room-management-table">
              <thead>
                <tr>
                  <th style="width: 80px; text-align: center;">Icon</th>
                  <th>Tên tiện nghi</th>
                  <th>Mô tả</th>
                  <th>Trạng thái</th>
                  <th>Ngày cập nhật</th>
                  <th style="text-align: right;">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <c:choose>
                  <c:when test="${empty amenities}">
                    <tr>
                      <td colspan="6">
                        <div class="room-management-empty">
                          <strong>Chưa có tiện nghi nào</strong>
                          <span>Hãy thêm tiện nghi mới để gán vào các loại phòng.</span>
                        </div>
                      </td>
                    </tr>
                  </c:when>
                  <c:otherwise>
                    <c:forEach var="amenity" items="${amenities}">
                      <tr data-pagination-item>
                        <td style="text-align: center;">
                          <div style="width: 38px; height: 38px; border-radius: 8px; background: #eff6ff; color: #2563eb; font-size: 17px; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #dbeafe;">
                            <i class="<c:out value='${not empty amenity.icon ? amenity.icon : "fa-solid fa-star"}' />"></i>
                          </div>
                        </td>
                        <td>
                          <div class="room-number-pill" style="display: inline-block;">
                            <strong><c:out value="${amenity.name}" /></strong>
                          </div>
                          <small style="color: #64748b; margin-left: 6px;">#<c:out value="${amenity.id}" /></small>
                        </td>
                        <td>
                          <span style="color: #475569; font-size: 13.5px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                            <c:out value="${empty amenity.description ? '-' : amenity.description}" />
                          </span>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${amenity.status eq 'ACTIVE'}">
                              <span class="status-chip status-available">Đang hoạt động</span>
                            </c:when>
                            <c:otherwise>
                              <span class="status-chip status-maintenance">Ngừng hoạt động</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <fmt:formatDate value="${amenity.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                        </td>
                        <td style="text-align: right;">
                          <div class="room-management-actions" style="justify-content: flex-end;">
                            <a class="btn btn-secondary btn-sm" href="${cp}/manager/amenity/edit?id=${amenity.id}">
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
          </div>

          <div class="room-management-mobile-list" data-pagination-root data-pagination-key="amenities" data-pagination-size="5">
            <c:forEach var="amenity" items="${amenities}">
              <article class="room-management-card" data-pagination-item>
                <div class="room-management-card__head">
                  <div style="display: flex; gap: 12px; align-items: center;">
                    <div style="width: 38px; height: 38px; border-radius: 8px; background: #eff6ff; color: #2563eb; font-size: 17px; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #dbeafe; flex-shrink: 0;">
                      <i class="<c:out value='${not empty amenity.icon ? amenity.icon : "fa-solid fa-star"}' />"></i>
                    </div>
                    <div>
                      <h3 style="margin: 0;"><c:out value="${amenity.name}" /></h3>
                      <small style="color: #64748b;">#<c:out value="${amenity.id}" /></small>
                    </div>
                  </div>
                  <c:choose>
                    <c:when test="${amenity.status eq 'ACTIVE'}">
                      <span class="status-chip status-available">Đang hoạt động</span>
                    </c:when>
                    <c:otherwise>
                      <span class="status-chip status-maintenance">Ngừng hoạt động</span>
                    </c:otherwise>
                  </c:choose>
                </div>
                <dl class="room-management-meta">
                  <div><dt>Mô tả</dt><dd><c:out value="${empty amenity.description ? '-' : amenity.description}" /></dd></div>
                  <div><dt>Cập nhật</dt><dd><fmt:formatDate value="${amenity.updatedAt}" pattern="dd/MM/yyyy HH:mm" /></dd></div>
                </dl>
                <div class="room-management-actions">
                  <a class="btn btn-secondary btn-sm" href="${cp}/manager/amenity/edit?id=${amenity.id}">
                    Sửa
                  </a>
                </div>
              </article>
            </c:forEach>
          </div>

          <div class="room-management-pagination" data-pagination-controls data-pagination-target="amenities"></div>
        </section>
      </section>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${cp}/assets/js/pagination.js?v=20260820-7"></script>
  </body>
</html>
