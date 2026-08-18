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
    <title>Equipment Management | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260816-4" />
    <link rel="stylesheet" href="${cp}/assets/css/equipment.css" />
  </head>
  <body class="equipment-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container equipment-management-page">
      <section class="equipment-toolbar panel">
        <div class="equipment-toolbar__search">
          <span class="equipment-toolbar__icon" aria-hidden="true">&#128269;</span>
          <form class="equipment-search-form" method="get" action="${cp}/manager/equipment">
            <input
              type="search"
              name="keyword"
              value="${keyword}"
              placeholder="Search equipment, notes, etc"
              aria-label="Search equipment"
            />
            <c:if test="${not empty status and status ne 'ALL'}">
              <input type="hidden" name="status" value="${status}" />
            </c:if>
          </form>
        </div>

        <form class="equipment-toolbar__filter" method="get" action="${cp}/manager/equipment">
          <c:if test="${not empty keyword}">
            <input type="hidden" name="keyword" value="${keyword}" />
          </c:if>
          <div class="equipment-select-pill">
            <select name="status" onchange="this.form.submit()">
              <option value="ALL" ${status eq 'ALL' ? 'selected' : ''}>All Status</option>
              <option value="ACTIVE" ${status eq 'ACTIVE' ? 'selected' : ''}>Active</option>
              <option value="INACTIVE" ${status eq 'INACTIVE' ? 'selected' : ''}>Inactive</option>
            </select>
            <span class="equipment-select-pill__icon" aria-hidden="true">&#8964;</span>
          </div>
        </form>

        <a class="btn btn-warning equipment-add-btn" href="${cp}/manager/equipment/new">Add Equipment</a>
      </section>

      <section class="equipment-list panel">
        <div class="equipment-list__head">
          <div>
            <h1>Equipment</h1>
            <p>
              Showing <c:out value="${equipmentCount}" /> item(s)
            </p>
          </div>
        </div>

        <c:choose>
          <c:when test="${not empty equipments}">
            <div class="equipment-table-wrap">
              <table class="equipment-table">
                <thead>
                  <tr>
                    <th>Image</th>
                    <th>Item</th>
                    <th>Category</th>
                    <th>Status</th>
                    <th>Compensation</th>
                    <th>Updated</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach items="${equipments}" var="equipment">
                    <tr>
                      <td data-label="Image">
                        <div class="equipment-thumb">
                          <c:choose>
                            <c:when test="${not empty equipment.imageUrl}">
                              <img src="${cp}${equipment.imageUrl}" alt="${equipment.name}" />
                            </c:when>
                            <c:otherwise>
                              <span>EQ</span>
                            </c:otherwise>
                          </c:choose>
                        </div>
                      </td>
                      <td data-label="Item">
                        <div class="equipment-item-name">
                          <strong><c:out value="${equipment.name}" /></strong>
                          <span>#<c:out value="${equipment.id}" /></span>
                        </div>
                      </td>
                      <td data-label="Category">
                        <span class="equipment-category">
                          <c:choose>
                            <c:when test="${not empty equipment.description}">
                              <c:out value="${equipment.description}" />
                            </c:when>
                            <c:otherwise>General equipment</c:otherwise>
                          </c:choose>
                        </span>
                      </td>
                      <td data-label="Status">
                        <c:choose>
                          <c:when test="${equipment.status eq 'ACTIVE'}">
                            <span class="status-chip status-chip--success">ACTIVE</span>
                          </c:when>
                          <c:otherwise>
                            <span class="status-chip status-chip--neutral">INACTIVE</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td data-label="Compensation">
                        <div class="equipment-stock">
                          <strong>
                            <fmt:formatNumber value="${equipment.defaultCompensationPrice}" type="number" groupingUsed="true" maxFractionDigits="0" />
                          </strong>
                          <small>VND</small>
                        </div>
                      </td>
                      <td data-label="Updated">
                        <span class="equipment-updated">
                          <c:choose>
                            <c:when test="${not empty equipment.updatedAt}">
                              <fmt:formatDate value="${equipment.updatedAt}" pattern="dd/MM/yyyy" />
                            </c:when>
                            <c:otherwise>--</c:otherwise>
                          </c:choose>
                        </span>
                      </td>
                      <td data-label="Action">
                        <a class="equipment-action-btn" href="${cp}/manager/equipment/edit?id=${equipment.id}" title="Edit equipment" aria-label="Edit equipment">
                          &#9998;
                        </a>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </c:when>
          <c:otherwise>
            <div class="equipment-empty">
              <h3>No equipment found</h3>
              <p>Add the first equipment record to start managing your inventory.</p>
            </div>
          </c:otherwise>
        </c:choose>
      </section>
    </main>
  </body>
</html>
