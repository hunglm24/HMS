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
    <title>Quản lý loại phòng | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260819-1" />
    <link rel="stylesheet" href="${cp}/assets/css/room-types.css" />
  </head>
  <body class="room-management-body room-types-preview-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-types-preview-page">
      <section class="room-types-shell">
        <section class="room-types-list panel">
          <div class="room-types-topbar">
            <div class="room-types-topbar__title">
              <h1>Room Category</h1>
            </div>

            <div class="room-types-topbar__actions">
              <span class="room-types-sort-label">Sort by:</span>
              <div class="room-types-toolbar">
                <button class="room-types-pill room-types-pill--dropdown" type="button">
                  <span>Popular</span>
                </button>
                <form class="room-types-status-form" action="${cp}/manager/room-types" method="get">
                  <c:if test="${not empty pageData.keyword}">
                    <input type="hidden" name="keyword" value="${pageData.keyword}" />
                  </c:if>
                  <div class="room-types-select-pill">
                    <select name="roomTypeStatus" onchange="this.form.submit()">
                      <option value="ACTIVE" ${pageData.roomTypeStatus eq 'ACTIVE' ? 'selected' : ''}>Active</option>
                      <option value="INACTIVE" ${pageData.roomTypeStatus eq 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                      <option value="ALL" ${pageData.roomTypeStatus eq 'ALL' ? 'selected' : ''}>All Status</option>
                    </select>
                    <span class="room-types-select-pill__icon" aria-hidden="true">⌄</span>
                  </div>
                </form>
              </div>
              <a class="btn btn-warning room-types-add-btn" href="${cp}/manager/room-types/new">Add Room</a>
              <button class="room-types-icon-btn" type="button" aria-label="Filter room types">⟲</button>
            </div>
          </div>

          <c:choose>
            <c:when test="${not empty roomTypes}">
              <div class="room-types-cards">
                <c:forEach items="${roomTypes}" var="roomType" varStatus="loop">
                  <c:url var="roomTypeDetailUrl" value="/manager/room-types">
                    <c:if test="${not empty pageData.keyword}">
                      <c:param name="keyword" value="${pageData.keyword}" />
                    </c:if>
                    <c:if test="${not empty pageData.roomTypeStatus}">
                      <c:param name="roomTypeStatus" value="${pageData.roomTypeStatus}" />
                    </c:if>
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
                            <span>Image Holder</span>
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
                              <span class="status-chip status-available"><c:out value="${roomType.status}" /></span>
                            </c:when>
                            <c:when test="${roomType.status eq 'INACTIVE'}">
                              <span class="status-chip status-cancelled"><c:out value="${roomType.status}" /></span>
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
                            <small>VND / night</small>
                          </c:when>
                          <c:otherwise>
                            <small>Price not set</small>
                          </c:otherwise>
                        </c:choose>
                      </div>

                      <div class="room-types-card__meta room-meta">
                        <span class="room-meta__item">
                          <svg class="room-meta__icon" viewBox="0 0 24 24" aria-hidden="true">
                            <rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.6" />
                            <path d="M8 8h8M8 12h8M8 16h4" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                          </svg>
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
                          <svg class="room-meta__icon" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M4 10h16v6H4z" fill="none" stroke="currentColor" stroke-width="1.6" />
                            <path d="M6 10V7h5v3M13 10V7h5v3" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                          </svg>
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
                          <svg class="room-meta__icon" viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="9" cy="8" r="3" fill="none" stroke="currentColor" stroke-width="1.6" />
                            <circle cx="16" cy="8.5" r="2.5" fill="none" stroke="currentColor" stroke-width="1.6" />
                            <path d="M4 18c0-2.8 2.5-5 5.5-5s5.5 2.2 5.5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                            <path d="M13 18c.2-1.8 1.5-3.2 3.5-4" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                          </svg>
                          <span>
                            <c:choose>
                              <c:when test="${not empty roomType.capacity}">
                                <c:out value="${roomType.capacity}" /> guests
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
            </c:when>
            <c:otherwise>
              <div class="panel room-types-empty-state">
                <h3>No room types found</h3>
                <p>Create the first room type to start managing your catalog.</p>
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
                  <a class="btn btn-secondary room-types-action-btn" href="${cp}/manager/room-types/edit?id=${selectedRoomType.id}">Edit</a>
                  <form class="room-types-status-form room-types-status-form--toggle" action="${cp}/manager/room-types/toggle-status" method="post">
                    <input type="hidden" name="id" value="${selectedRoomType.id}" />
                    <c:choose>
                      <c:when test="${selectedRoomType.status eq 'ACTIVE'}">
                        <button class="room-status-switch room-status-switch--active" type="submit" aria-label="Deactivate room type" title="Deactivate room type">
                          <span class="room-status-switch__knob" aria-hidden="true"></span>
                        </button>
                      </c:when>
                      <c:otherwise>
                        <button class="room-status-switch room-status-switch--inactive" type="submit" aria-label="Activate room type" title="Activate room type">
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
                      <span>Large Image Holder</span>
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>

              <div class="room-types-facts room-meta">
                <span class="room-meta__item">
                  <svg class="room-meta__icon" viewBox="0 0 24 24" aria-hidden="true">
                    <rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.6" />
                    <path d="M8 8h8M8 12h8M8 16h4" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                  </svg>
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
                  <svg class="room-meta__icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M4 10h16v6H4z" fill="none" stroke="currentColor" stroke-width="1.6" />
                    <path d="M6 10V7h5v3M13 10V7h5v3" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                  </svg>
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
                  <svg class="room-meta__icon" viewBox="0 0 24 24" aria-hidden="true">
                    <circle cx="9" cy="8" r="3" fill="none" stroke="currentColor" stroke-width="1.6" />
                    <circle cx="16" cy="8.5" r="2.5" fill="none" stroke="currentColor" stroke-width="1.6" />
                    <path d="M4 18c0-2.8 2.5-5 5.5-5s5.5 2.2 5.5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                    <path d="M13 18c.2-1.8 1.5-3.2 3.5-4" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                  </svg>
                  <span>
                    <c:choose>
                      <c:when test="${not empty selectedRoomType.capacity}">
                        <c:out value="${selectedRoomType.capacity}" /> guests
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
                <h3>Amenities</h3>
                <c:choose>
                  <c:when test="${not empty selectedRoomTypeAmenities}">
                    <div class="room-types-bullets room-types-bullets--two-col">
                      <c:forEach items="${selectedRoomTypeAmenities}" var="amenity">
                        <span><c:out value="${amenity.name}" /></span>
                      </c:forEach>
                    </div>
                  </c:when>
                  <c:otherwise>
                    <p class="room-types-empty-inline">No amenities configured for this room type.</p>
                  </c:otherwise>
                </c:choose>
              </section>
            </c:when>
            <c:otherwise>
              <div class="room-types-empty-detail">
                <h2>No room type selected</h2>
                <p>Pick a room type from the list to see its detail information.</p>
              </div>
            </c:otherwise>
          </c:choose>
        </aside>
      </section>
    </main>
  </body>
</html>
