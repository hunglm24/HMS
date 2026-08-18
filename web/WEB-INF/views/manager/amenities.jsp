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
    <title>Amenity Management | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260816-4" />
    <link rel="stylesheet" href="${cp}/assets/css/amenities.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
  </head>
  <body class="amenity-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container amenity-management-page">
      <section class="amenity-toolbar panel">
        <div class="amenity-toolbar__search">
          <span class="amenity-toolbar__icon" aria-hidden="true">&#128269;</span>
          <form class="amenity-search-form" method="get" action="${cp}/manager/amenity">
            <input
              type="search"
              name="keyword"
              value="${keyword}"
              placeholder="Search amenity, descriptions..."
              aria-label="Search amenity"
            />
            <c:if test="${not empty status and status ne 'ALL'}">
              <input type="hidden" name="status" value="${status}" />
            </c:if>
          </form>
        </div>

        <form class="amenity-toolbar__filter" method="get" action="${cp}/manager/amenity">
          <c:if test="${not empty keyword}">
            <input type="hidden" name="keyword" value="${keyword}" />
          </c:if>
          <div class="amenity-select-pill">
            <select name="status" onchange="this.form.submit()">
              <option value="ALL" ${status eq 'ALL' ? 'selected' : ''}>All Status</option>
              <option value="ACTIVE" ${status eq 'ACTIVE' ? 'selected' : ''}>Active</option>
              <option value="INACTIVE" ${status eq 'INACTIVE' ? 'selected' : ''}>Inactive</option>
            </select>
            <span class="amenity-select-pill__icon" aria-hidden="true">&#8964;</span>
          </div>
        </form>

        <a class="btn btn-warning amenity-add-btn" href="${cp}/manager/amenity/new">Add Amenity</a>
      </section>

      <section class="amenity-list panel">
        <div class="amenity-list__head">
          <div>
            <h1>Amenities</h1>
            <p>
              Showing <c:out value="${amenityCount}" /> item(s)
            </p>
          </div>
        </div>

        <c:choose>
          <c:when test="${not empty amenities}">
            <div class="amenity-table-wrap" data-pagination-root data-pagination-key="amenities" data-pagination-size="10">
              <table class="amenity-table">
                <thead>
                  <tr>
                    <th>Icon</th>
                    <th>Item</th>
                    <th>Status</th>
                    <th>Updated</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach items="${amenities}" var="amenity">
                    <tr data-pagination-item>
                      <td data-label="Icon">
                        <div class="amenity-thumb" style="font-size: 24px; color: var(--color-primary); display: flex; align-items: center; justify-content: center;">
                          <c:choose>
                            <c:when test="${not empty amenity.icon}">
                              <i class="<c:out value='${amenity.icon}' />"></i>
                            </c:when>
                            <c:otherwise>
                              <span><i class="fa fa-star"></i></span>
                            </c:otherwise>
                          </c:choose>
                        </div>
                      </td>
                      <td data-label="Item">
                        <div class="amenity-item-name">
                          <strong><c:out value="${amenity.name}" /></strong>
                          <span>#<c:out value="${amenity.id}" /></span>
                        </div>
                        <div class="amenity-desc" style="color: #64748b; font-size: 13px; max-width: 300px; white-space: normal; margin-top: 4px;">
                            <c:out value="${amenity.description}" />
                        </div>
                      </td>
                      <td data-label="Status">
                        <span class="amenity-status amenity-status--${amenity.status.toLowerCase()}">
                          <c:out value="${amenity.status}" />
                        </span>
                      </td>
                      <td data-label="Updated">
                        <div class="amenity-date">
                          <fmt:formatDate value="${amenity.updatedAt}" pattern="MMM dd, yyyy" />
                        </div>
                      </td>
                      <td data-label="Action">
                        <a href="${cp}/manager/amenity/edit?id=${amenity.id}" class="amenity-action-btn" title="Edit">
                          <span aria-hidden="true">&#9998;</span> Edit
                        </a>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
            
            <div data-pagination-controls class="amenity-management-pagination"></div>
          </c:when>
          <c:otherwise>
            <div class="amenity-empty">
              <div class="amenity-empty__icon" aria-hidden="true">&#128230;</div>
              <p>No amenities found matching your criteria.</p>
              <a href="${cp}/manager/amenity" class="btn btn-secondary">Clear Filters</a>
            </div>
          </c:otherwise>
        </c:choose>
      </section>
    </main>

    <c:if test="${not empty sessionScope.toastMessage}">
      <div id="toastNotification" class="toast toast--${sessionScope.toastType}">
        <c:out value="${sessionScope.toastMessage}" />
      </div>
      <c:remove var="toastMessage" scope="session" />
      <c:remove var="toastType" scope="session" />
      <style>
        .toast {
            position: fixed;
            bottom: 24px;
            right: 24px;
            padding: 16px 24px;
            border-radius: 8px;
            color: #fff;
            font-weight: 500;
            z-index: 9999;
            animation: slideUpFade 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
        }
        .toast--success { background: #10b981; }
        .toast--error { background: #ef4444; }
        @keyframes slideUpFade {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
      </style>
      <script>
        setTimeout(() => {
            const toast = document.getElementById('toastNotification');
            if (toast) {
                toast.style.opacity = '0';
                toast.style.transition = 'opacity 0.3s ease';
                setTimeout(() => toast.remove(), 300);
            }
        }, 3000);
      </script>
    </c:if>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${cp}/assets/js/pagination.js"></script>
  </body>
</html>
