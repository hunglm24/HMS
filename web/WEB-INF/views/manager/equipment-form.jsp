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
    <title><c:out value="${equipmentPageTitle}" /></title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260816-4" />
    <link rel="stylesheet" href="${cp}/assets/css/equipment.css" />
  </head>
  <body class="equipment-management-body equipment-form-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container equipment-form-page">
      <form class="equipment-form" action="${cp}${equipmentFormAction}" method="post" enctype="multipart/form-data" novalidate>
        <c:if test="${isEditMode}">
          <input type="hidden" name="id" value="${equipmentId}" />
        </c:if>

        <section class="equipment-form-hero panel">
          <div class="equipment-form-hero__copy">
            <a class="equipment-form-back" href="${cp}${equipmentBackUrl}">Back to Equipment</a>
            <h1><c:out value="${equipmentPageHeading}" /></h1>
            <p><c:out value="${equipmentPageSubtitle}" /></p>
          </div>

          <div class="equipment-form-hero__actions">
            <button class="btn btn-secondary" type="button">Save Draft</button>
            <button class="btn btn-warning" type="submit"><c:out value="${equipmentSubmitLabel}" /></button>
          </div>
        </section>

        <section class="equipment-form-layout">
          <article class="panel equipment-form-card">
            <header class="equipment-form-card__head">
              <div>
                <h2>Basic Information</h2>
                <p>Core fields for each equipment item.</p>
              </div>
            </header>

            <label class="equipment-form-field${not empty errors.name ? ' is-error' : ''}">
              <span>Equipment Name <strong class="equipment-form-required">*</strong></span>
              <input name="name" type="text" value="<c:out value='${form.name}' />" maxlength="100" required placeholder="Enter equipment name" />
              <c:if test="${not empty errors.name}">
                <div class="equipment-form-field__error"><c:out value="${errors.name}" /></div>
              </c:if>
            </label>

            <label class="equipment-form-field${not empty errors.description ? ' is-error' : ''}">
              <span>Category / Description</span>
              <textarea name="description" rows="6" maxlength="500" placeholder="Optional description"><c:out value="${form.description}" /></textarea>
              <c:if test="${not empty errors.description}">
                <div class="equipment-form-field__error"><c:out value="${errors.description}" /></div>
              </c:if>
            </label>

            <div class="equipment-form-grid-2">
              <label class="equipment-form-field${not empty errors.defaultCompensationPrice ? ' is-error' : ''}">
                <span>Default Compensation <strong class="equipment-form-required">*</strong></span>
                <div class="equipment-form-money">
                  <input name="defaultCompensationPrice" type="text" value="<c:out value='${form.defaultCompensationPrice}' />" inputmode="numeric" autocomplete="off" required placeholder="0" />
                  <strong>VND</strong>
                </div>
                <c:if test="${not empty errors.defaultCompensationPrice}">
                  <div class="equipment-form-field__error"><c:out value="${errors.defaultCompensationPrice}" /></div>
                </c:if>
              </label>

              <div class="equipment-form-field equipment-form-field--status${not empty errors.status ? ' is-error' : ''}">
                <span>Status <strong class="equipment-form-required">*</strong></span>
                <div class="equipment-form-radios">
                  <c:forEach var="status" items="${equipmentStatuses}" varStatus="loop">
                    <label>
                      <input
                        type="radio"
                        name="status"
                        value="<c:out value='${status}' />"
                        <c:if test="${status eq form.status or (empty form.status and status eq equipmentStatuses[0])}">checked</c:if>
                        <c:if test="${loop.first}">required</c:if>
                      />
                      <span><c:out value="${status}" /></span>
                    </label>
                  </c:forEach>
                </div>
                <c:if test="${not empty errors.status}">
                  <div class="equipment-form-field__error"><c:out value="${errors.status}" /></div>
                </c:if>
              </div>
            </div>

            <div style="margin-top: 20px; padding: 14px 16px; background: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 8px;">
              <label style="display: flex; align-items: flex-start; gap: 10px; cursor: pointer; margin: 0;">
                <input type="checkbox" name="isMaintainable" value="true" ${form.maintainable or empty form.id ? 'checked' : ''} style="width: 20px; height: 20px; margin-top: 2px; accent-color: #2563eb;" />
                <div>
                  <strong style="color: #1e293b; font-size: 14.5px; display: block;">Thiết bị có thể bảo trì / sửa chữa (Maintainable)</strong>
                  <span style="color: #64748b; font-size: 13px; display: block; margin-top: 2px;">
                    Tích chọn nếu là thiết bị điện tử, máy móc (Điều hòa, Tivi, Tủ lạnh...). Bỏ chọn nếu là đồ vải/tiêu hao (Khăn tắm - Bath towel, Ga gối, Ly cốc...) chỉ có thể thay mới khi hỏng.
                  </span>
                </div>
              </label>
            </div>
          </article>

          <aside class="equipment-form-sidebar">
            <article class="panel equipment-form-card equipment-form-card--media">
              <header class="equipment-form-card__head">
                <div>
                  <h2>Media</h2>
                  <p>Store the image locally in <code>/uploads/equipment</code>.</p>
                </div>
              </header>

              <label class="equipment-form-field equipment-form-field--upload${not empty errors.imageFile ? ' is-error' : ''}">
                <span>Cover Image</span>
                <input name="imageFile" type="file" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" />
                <small>Recommended JPG, PNG, or WEBP, up to 5 MB.</small>
                <c:if test="${not empty errors.imageFile}">
                  <div class="equipment-form-field__error"><c:out value="${errors.imageFile}" /></div>
                </c:if>
              </label>

              <div class="equipment-form-preview">
                <span>Preview</span>
                <c:choose>
                  <c:when test="${not empty equipmentExistingImageUrl}">
                    <div class="equipment-form-preview__image">
                      <img
                        id="equipmentImagePreview"
                        src="${cp}${equipmentExistingImageUrl}"
                        data-original-src="${cp}${equipmentExistingImageUrl}"
                        alt="${form.name}"
                      />
                    </div>
                  </c:when>
                  <c:otherwise>
                    <div class="equipment-form-preview__image">
                      <img id="equipmentImagePreview" alt="Equipment image preview" hidden />
                      <div id="equipmentImagePlaceholder" class="equipment-form-preview__placeholder">Equipment image preview</div>
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>
            </article>
          </aside>
        </section>
      </form>
    </main>
    <script src="${cp}/assets/js/equipment-form.js"></script>
  </body>
</html>