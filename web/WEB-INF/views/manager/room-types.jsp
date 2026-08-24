<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý loại phòng | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260819-1" />
    <link rel="stylesheet" href="${cp}/assets/css/room-types.css?v=20260819-2" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
  </head>
  <body class="room-management-body room-types-preview-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-types-preview-page">
      <c:url var="popularSortUrl" value="/manager/room-types">
        <c:if test="${not empty pageData.keyword}">
          <c:param name="keyword" value="${pageData.keyword}" />
        </c:if>
        <c:if test="${not empty pageData.roomTypeStatus}">
          <c:param name="roomTypeStatus" value="${pageData.roomTypeStatus}" />
        </c:if>
        <c:if test="${not empty selectedRoomTypeId}">
          <c:param name="selectedRoomTypeId" value="${selectedRoomTypeId}" />
        </c:if>
        <c:param name="sort" value="popular" />
        <c:param name="direction" value="${roomTypeNextDirection}" />
      </c:url>
      <section class="room-types-shell">
        <section class="room-types-list panel">
          <div class="room-types-topbar">
            <div class="room-types-topbar__title">
              <h1>Loại phòng</h1>
            </div>

            <div class="room-types-topbar__actions">
              <span class="room-types-sort-label">Sắp xếp theo:</span>
              <div class="room-types-toolbar">
                <a class="room-types-pill room-types-pill--dropdown ${roomTypeDirection eq 'ASC' ? 'is-up' : 'is-down'}" href="${popularSortUrl}">
                  <span>Phổ biến</span>
                </a>
                <form class="room-types-status-form" action="${cp}/manager/room-types" method="get">
                  <c:if test="${not empty pageData.keyword}">
                    <input type="hidden" name="keyword" value="${pageData.keyword}" />
                  </c:if>
                  <c:if test="${not empty selectedRoomTypeId}">
                    <input type="hidden" name="selectedRoomTypeId" value="${selectedRoomTypeId}" />
                  </c:if>
                  <input type="hidden" name="sort" value="${roomTypeSort}" />
                  <input type="hidden" name="direction" value="${roomTypeDirection}" />
                  <div class="room-types-select-pill">
                    <select name="roomTypeStatus" onchange="this.form.submit()">
                      <option value="ACTIVE" ${pageData.roomTypeStatus eq 'ACTIVE' ? 'selected' : ''}>Đang hoạt động</option>
                      <option value="INACTIVE" ${pageData.roomTypeStatus eq 'INACTIVE' ? 'selected' : ''}>Ngừng hoạt động</option>
                      <option value="ALL" ${pageData.roomTypeStatus eq 'ALL' ? 'selected' : ''}>Tất cả</option>
                    </select>
                    <span class="room-types-select-pill__icon" aria-hidden="true">⌄</span>
                  </div>
                </form>
              </div>
              <a class="btn btn-warning room-types-add-btn" href="${cp}/manager/room-types/new">+ Thêm loại phòng</a>
              <button class="room-types-icon-btn" type="button" aria-label="Lọc loại phòng">⟲</button>
            </div>
          </div>

          <c:choose>
            <c:when test="${not empty roomTypes}">
              <div class="room-types-cards" id="roomTypesCards">
                <c:forEach items="${roomTypes}" var="roomType" varStatus="loop">
                  <c:url var="roomTypeDetailUrl" value="/manager/room-types">
                    <c:if test="${not empty pageData.keyword}">
                      <c:param name="keyword" value="${pageData.keyword}" />
                    </c:if>
                    <c:if test="${not empty pageData.roomTypeStatus}">
                      <c:param name="roomTypeStatus" value="${pageData.roomTypeStatus}" />
                    </c:if>
                    <c:param name="sort" value="${roomTypeSort}" />
                    <c:param name="direction" value="${roomTypeDirection}" />
                    <c:param name="selectedRoomTypeId" value="${roomType.id}" />
                  </c:url>

                  <a
                    class="room-types-card room-types-card--link${roomType.id == selectedRoomTypeId ? ' is-selected' : ''}${loop.first && empty selectedRoomTypeId ? ' room-types-card--featured' : ''}"
                    href="${roomTypeDetailUrl}"
                  >
                    <div class="room-types-card__media">
                      <c:choose>
                        <c:when test="${not empty roomType.imageUrl}">
                          <div class="room-types-card__image-frame">
                            <img src="${cp}${roomType.imageUrl}" alt="${roomType.name}" />
                          </div>
                        </c:when>
                        <c:otherwise>
                          <div class="image-holder image-holder--large">
                            <span>Hình ảnh</span>
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div class="room-types-card__body">
                      <div class="room-types-card__head">
                        <div>
                          <h3><c:out value="${roomType.name}" /></h3>
                          <p><c:out value="${roomType.description}" /></p>
                        </div>

                        <div class="room-types-status">
                          <c:choose>
                            <c:when test="${roomType.status eq 'ACTIVE'}">
                              <span class="status-chip status-available">Đang hoạt động</span>
                            </c:when>
                            <c:when test="${roomType.status eq 'INACTIVE'}">
                              <span class="status-chip status-cancelled">Ngừng hoạt động</span>
                            </c:when>
                            <c:otherwise>
                              <span class="status-chip"><c:out value="${roomType.status}" /></span>
                            </c:otherwise>
                          </c:choose>
                        </div>
                      </div>

                      <div class="room-types-card__price">
                        <c:choose>
                          <c:when test="${not empty roomType.basePrice}">
                            <fmt:formatNumber value="${roomType.basePrice}" type="number" groupingUsed="true" maxFractionDigits="0" />
                            <small>VND / đêm</small>
                          </c:when>
                          <c:otherwise>
                            <small>Chưa cập nhật giá</small>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="room-types-card__meta room-meta">
                        <span class="room-meta__item">
                          <i class="bi bi-aspect-ratio room-meta__icon" aria-hidden="true"></i>
                          <span>
                            <c:choose>
                              <c:when test="${not empty roomType.sizeM2}">
                                <c:out value="${roomType.sizeM2}" /> m&sup2;
                              </c:when>
                              <c:otherwise>-</c:otherwise>
                            </c:choose>
                          </span>
                        </span>
                        <span class="room-meta__item">
                          <i class="bi bi-house-door room-meta__icon" aria-hidden="true"></i>
                          <span>
                            <c:choose>
                              <c:when test="${not empty roomType.bedType}">
                                <c:out value="${roomType.bedType}" />
                              </c:when>
                              <c:otherwise>-</c:otherwise>
                            </c:choose>
                          </span>
                        </span>
                        <span class="room-meta__item">
                          <i class="bi bi-people room-meta__icon" aria-hidden="true"></i>
                          <span>
                            <c:choose>
                              <c:when test="${not empty roomType.capacity}">
                                <c:out value="${roomType.capacity}" /> khách
                              </c:when>
                              <c:otherwise>-</c:otherwise>
                            </c:choose>
                          </span>
                        </span>
                      </div>
                    </div>
                  </a>
                </c:forEach>
              </div>
              <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
            </c:when>
            <c:otherwise>
              <div class="panel room-types-empty-state">
                <h3>Chưa có loại phòng nào</h3>
                <p>Hãy tạo loại phòng đầu tiên để bắt đầu quản lý danh mục.</p>
              </div>
            </c:otherwise>
          </c:choose>
        </section>

        <aside class="room-types-detail panel">
          <c:choose>
            <c:when test="${not empty selectedRoomType}">
              <div class="room-types-detail__head">
                <div>
                  <h2><c:out value="${selectedRoomType.name}" /></h2>
                  <p><c:out value="${selectedRoomType.status}" /></p>
                </div>
                <div class="room-types-detail__actions">
                  <a class="btn btn-secondary room-types-action-btn" href="${cp}/manager/room-types/edit?id=${selectedRoomType.id}">Sửa</a>
                  <form class="room-types-status-form room-types-status-form--toggle" action="${cp}/manager/room-types/toggle-status" method="post">
                    <input type="hidden" name="id" value="${selectedRoomType.id}" />
                    <c:choose>
                      <c:when test="${selectedRoomType.status eq 'ACTIVE'}">
                        <button class="room-status-switch room-status-switch--active" type="submit" aria-label="Ngừng hoạt động loại phòng" title="Ngừng hoạt động loại phòng">
                          <span class="room-status-switch__knob" aria-hidden="true"></span>
                        </button>
                      </c:when>
                      <c:otherwise>
                        <button class="room-status-switch room-status-switch--inactive" type="submit" aria-label="Kích hoạt loại phòng" title="Kích hoạt loại phòng">
                          <span class="room-status-switch__knob" aria-hidden="true"></span>
                        </button>
                      </c:otherwise>
                    </c:choose>
                  </form>
                </div>
              </div>

              <div class="room-types-detail__hero">
                <c:choose>
                  <c:when test="${not empty selectedRoomType.imageUrl}">
                    <div class="room-types-detail__image-frame">
                      <img src="${cp}${selectedRoomType.imageUrl}" alt="${selectedRoomType.name}" />
                    </div>
                  </c:when>
                  <c:otherwise>
                    <div class="image-holder image-holder--hero">
                      <span>Hình ảnh lớn</span>
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>

              <div class="room-types-facts room-meta">
                <span class="room-meta__item">
                  <i class="bi bi-aspect-ratio room-meta__icon" aria-hidden="true"></i>
                  <span>
                    <c:choose>
                      <c:when test="${not empty selectedRoomType.sizeM2}">
                        <c:out value="${selectedRoomType.sizeM2}" /> m&sup2;
                      </c:when>
                      <c:otherwise>-</c:otherwise>
                    </c:choose>
                  </span>
                </span>
                <span class="room-meta__item">
                  <i class="bi bi-house-door room-meta__icon" aria-hidden="true"></i>
                  <span>
                    <c:choose>
                      <c:when test="${not empty selectedRoomType.bedType}">
                        <c:out value="${selectedRoomType.bedType}" />
                      </c:when>
                      <c:otherwise>-</c:otherwise>
                    </c:choose>
                  </span>
                </span>
                <span class="room-meta__item">
                  <i class="bi bi-people room-meta__icon" aria-hidden="true"></i>
                  <span>
                    <c:choose>
                      <c:when test="${not empty selectedRoomType.capacity}">
                        <c:out value="${selectedRoomType.capacity}" /> khách
                      </c:when>
                      <c:otherwise>-</c:otherwise>
                    </c:choose>
                  </span>
                </span>
              </div>

              <p class="room-types-description">
                <c:out value="${selectedRoomType.description}" />
              </p>

              <section class="room-types-section">
                <h3>Tiện ích</h3>
                <c:choose>
                  <c:when test="${not empty selectedRoomTypeAmenities}">
                    <div class="room-types-bullets room-types-bullets--two-col">
                      <c:forEach items="${selectedRoomTypeAmenities}" var="amenity">
                        <span><c:out value="${amenity.name}" /></span>
                      </c:forEach>
                    </div>
                  </c:when>
                  <c:otherwise>
                    <p class="room-types-empty-inline">Chưa có tiện ích nào cho loại phòng này.</p>
                  </c:otherwise>
                </c:choose>
              </section>

              <section class="room-types-section">
                <h3>Chi tiết quản lý</h3>
                <div class="room-types-facts room-meta room-types-facts--compact">
                  <span class="room-meta__item">
                    <strong>Giá cơ bản</strong>
                    <span>
                      <c:choose>
                        <c:when test="${not empty selectedRoomType.basePrice}">
                          <fmt:formatNumber value="${selectedRoomType.basePrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> VND
                        </c:when>
                        <c:otherwise>-</c:otherwise>
                      </c:choose>
                    </span>
                  </span>
                  <span class="room-meta__item">
                    <strong>Trạng thái</strong>
                    <span><c:out value="${selectedRoomType.status}" /></span>
                  </span>
                  <span class="room-meta__item">
                    <strong>Mã loại phòng</strong>
                    <span>#<c:out value="${selectedRoomType.id}" /></span>
                  </span>
                </div>
              </section>
            </c:when>
            <c:otherwise>
              <div class="room-types-empty-state room-types-empty-state--detail">
                <h3>Chọn một loại phòng</h3>
                <p>Chi tiết của loại phòng sẽ hiển thị ở đây.</p>
              </div>
            </c:otherwise>
          </c:choose>
        </aside>
      </section>
    </main>
  </body>
</html>
