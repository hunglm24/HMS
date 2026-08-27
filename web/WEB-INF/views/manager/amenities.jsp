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
    <link rel="stylesheet" href="${cp}/assets/css/amenities.css?v=20260825-1" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
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

        <div class="room-management-hero__actions amenity-hero-actions">
          <a class="btn btn-secondary" href="${cp}/manager/room-types">
            Quản lý loại phòng
          </a>
          <a class="btn" href="${cp}/manager/amenity/new">
            + Thêm tiện nghi
          </a>
        </div>
      </section>

      <section class="room-management-content">
        <section class="room-management-panel panel">
          <div class="room-management-toolbar">
            <form class="room-management-filters amenity-filters" method="get" action="${cp}/manager/amenities">
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
              <div class="amenity-filter-actions">
                <button class="btn btn-primary" type="submit">Lọc</button>
                <a class="btn btn-secondary" href="${cp}/manager/amenities">Đặt lại</a>
              </div>
            </form>
          </div>

          <div class="room-management-table-wrap" data-pagination-root data-pagination-key="amenities" data-pagination-size="5">
            <table class="room-management-table">
              <thead>
                <tr>
                  <th class="table-col-icon">Icon</th>
                  <th>Tên tiện nghi</th>
                  <th>Mô tả</th>
                  <th>Trạng thái</th>
                  <th>Ngày cập nhật</th>
                  <th class="text-right">Thao tác</th>
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
                        <td class="text-center">
                          <div class="amenity-icon-box">
                            <i class="<c:out value='${not empty amenity.icon ? amenity.icon : "fa-solid fa-star"}' />"></i>
                          </div>
                        </td>
                        <td>
                          <div class="room-number-pill">
                            <strong><c:out value="${amenity.name}" /></strong>
                          </div>
                          <span class="amenity-id-tag">#<c:out value="${amenity.id}" /></span>
                        </td>
                        <td>
                          <span class="amenity-desc-clamp">
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
                        <td class="text-right">
                          <div class="room-management-actions">
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
                  <div class="flex-align-center">
                    <div class="amenity-icon-box">
                      <i class="<c:out value='${not empty amenity.icon ? amenity.icon : "fa-solid fa-star"}' />"></i>
                    </div>
                    <div>
                      <h3><c:out value="${amenity.name}" /></h3>
                      <span class="amenity-id-tag">#<c:out value="${amenity.id}" /></span>
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
