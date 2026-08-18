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
    <title><c:out value="${amenityPageTitle}" /></title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260816-4" />
    <link rel="stylesheet" href="${cp}/assets/css/amenities.css" />
  </head>
  <body class="amenity-management-body amenity-form-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container amenity-form-page">
      <form class="amenity-form" action="${cp}${amenityFormAction}" method="post" novalidate>
        <c:if test="${isEditMode}">
          <input type="hidden" name="id" value="${amenityId}" />
        </c:if>

        <section class="amenity-form-hero panel">
          <div class="amenity-form-hero__copy">
            <a class="amenity-form-back" href="${cp}${amenityBackUrl}">Back to Amenities</a>
            <h1><c:out value="${amenityPageHeading}" /></h1>
            <p><c:out value="${amenityPageSubtitle}" /></p>
          </div>

          <div class="amenity-form-hero__actions">
            <button class="btn btn-warning" type="submit"><c:out value="${amenitySubmitLabel}" /></button>
          </div>
        </section>

        <c:if test="${not empty errors}">
          <div class="amenity-form-alert" role="alert" aria-live="polite">
            <div class="amenity-form-alert__icon" aria-hidden="true">!</div>
            <div class="amenity-form-alert__content">
              <strong>Please check the highlighted fields.</strong>
              <c:if test="${not empty errors.general}">
                <p class="amenity-form-alert__lead"><c:out value="${errors.general}" /></p>
              </c:if>
              <c:if test="${not empty errors}">
                <ul class="amenity-form-alert__list">
                  <c:forEach items="${errors}" var="entry">
                    <c:if test="${entry.key ne 'general'}">
                      <li><c:out value="${entry.value}" /></li>
                    </c:if>
                  </c:forEach>
                </ul>
              </c:if>
            </div>
          </div>
        </c:if>

        <section class="amenity-form-layout" style="grid-template-columns: 1fr; max-width: 800px; margin: 0 auto;">
          <article class="panel amenity-form-card">
            <header class="amenity-form-card__head">
              <div>
                <h2>Basic Information</h2>
                <p>Core fields for each amenity item.</p>
              </div>
            </header>

            <label class="amenity-form-field${not empty errors.name ? ' is-error' : ''}">
              <span>Amenity Name <strong class="amenity-form-required">*</strong></span>
              <input name="name" type="text" value="<c:out value='${form.name}' />" maxlength="100" required placeholder="Enter amenity name" />
              <c:if test="${not empty errors.name}">
                <div class="amenity-form-field__error"><c:out value="${errors.name}" /></div>
              </c:if>
            </label>

            <label class="amenity-form-field${not empty errors.description ? ' is-error' : ''}">
              <span>Description</span>
              <textarea name="description" rows="6" maxlength="500" placeholder="Optional description"><c:out value="${form.description}" /></textarea>
              <c:if test="${not empty errors.description}">
                <div class="amenity-form-field__error"><c:out value="${errors.description}" /></div>
              </c:if>
            </label>

            <div class="amenity-form-grid-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
              <label class="amenity-form-field${not empty errors.icon ? ' is-error' : ''}">
                <span>Icon Class <strong class="amenity-form-required">*</strong></span>
                <input name="icon" type="text" value="<c:out value='${form.icon}' />" maxlength="50" required placeholder="e.g., fa fa-tv" />
                <c:if test="${not empty errors.icon}">
                  <div class="amenity-form-field__error"><c:out value="${errors.icon}" /></div>
                </c:if>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Use FontAwesome classes (e.g. fa-solid fa-wifi).</div>
              </label>

              <div class="amenity-form-field amenity-form-field--status${not empty errors.status ? ' is-error' : ''}">
                <span>Status <strong class="amenity-form-required">*</strong></span>
                <div class="amenity-form-radios" style="display: flex; gap: 16px; margin-top: 8px;">
                  <c:forEach var="status" items="${amenityStatuses}" varStatus="loop">
                    <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                      <input type="radio" name="status" value="${status}" ${form.status eq status or (empty form.status and loop.first) ? 'checked' : ''} />
                      <span>${status eq 'ACTIVE' ? 'Active' : 'Inactive'}</span>
                    </label>
                  </c:forEach>
                </div>
                <c:if test="${not empty errors.status}">
                  <div class="amenity-form-field__error"><c:out value="${errors.status}" /></div>
                </c:if>
              </div>
            </div>
          </article>
        </section>
      </form>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
  </body>
</html>
