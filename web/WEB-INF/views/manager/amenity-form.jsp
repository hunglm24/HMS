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
    <title><c:out value="${amenityPageTitle}" /> | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/amenities.css?v=20260825-1" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container room-management-page">
      <form class="amenity-form" action="${cp}${amenityFormAction}" method="post" novalidate>
        <c:if test="${isEditMode}">
          <input type="hidden" name="id" value="${amenityId}" />
        </c:if>

        <section class="room-management-hero panel">
          <div class="room-management-hero__copy">
            <a class="amenity-back-link" href="${cp}/manager/amenities">← Quay lại danh sách tiện nghi</a>
            <p class="room-management-kicker">Quản lý khách sạn</p>
            <h1><c:out value="${amenityPageHeading}" /></h1>
            <p><c:out value="${amenityPageSubtitle}" /></p>
          </div>
        </section>

        <c:if test="${not empty errors}">
          <div class="alert alert-danger">
            <strong>Vui lòng kiểm tra lại các trường thông tin:</strong>
            <ul>
              <c:forEach items="${errors}" var="entry">
                <li><c:out value="${entry.value}" /></li>
              </c:forEach>
            </ul>
          </div>
        </c:if>

        <section class="room-management-content amenity-form-card">
          <section class="room-management-panel panel">
            <div class="form-layout-stack">
              <div>
                <h2>Thông tin tiện nghi</h2>
                <p class="text-secondary">Điền các thông tin của tiện nghi để hiển thị khi quản lý phòng và đặt phòng.</p>
              </div>

              <div class="amenity-form-group">
                <label class="amenity-form-label">
                  Tên tiện nghi <span class="text-danger">*</span>
                </label>
                <input type="text" name="name" class="amenity-form-control" value="<c:out value='${form.name}' />" maxlength="100" required placeholder="Ví dụ: Wi-Fi tốc độ cao, Smart TV 55 inch..." />
                <c:if test="${not empty errors.name}">
                  <small class="text-danger"><c:out value="${errors.name}" /></small>
                </c:if>
              </div>

              <div class="amenity-form-group">
                <label class="amenity-form-label">
                  Mô tả chi tiết
                </label>
                <textarea name="description" rows="4" class="amenity-form-control" maxlength="500" placeholder="Mô tả công dụng hoặc thông số của tiện nghi..."><c:out value="${form.description}" /></textarea>
                <c:if test="${not empty errors.description}">
                  <small class="text-danger"><c:out value="${errors.description}" /></small>
                </c:if>
              </div>

              <div class="form-grid-columns">
                <div class="amenity-form-group">
                  <label class="amenity-form-label">
                    Mã icon FontAwesome <span class="text-danger">*</span>
                  </label>
                  <div class="flex-align-center">
                    <div id="iconPreview" class="amenity-icon-box">
                      <i id="previewIconEl" class="<c:out value='${not empty form.icon ? form.icon : "fa-solid fa-star"}' />"></i>
                    </div>
                    <input type="text" id="iconInput" name="icon" class="amenity-form-control" value="<c:out value='${form.icon}' />" maxlength="50" required placeholder="Ví dụ: fa-solid fa-wifi" />
                  </div>
                  <small class="text-muted">Ví dụ: <code>fa-solid fa-wifi</code>, <code>fa-solid fa-tv</code>, <code>fa-solid fa-snowflake</code></small>
                  <c:if test="${not empty errors.icon}">
                    <small class="text-danger"><c:out value="${errors.icon}" /></small>
                  </c:if>
                </div>

                <div class="amenity-form-group">
                  <label class="amenity-form-label">
                    Trạng thái <span class="text-danger">*</span>
                  </label>
                  <select name="status" class="amenity-form-control select-fixed-height">
                    <option value="ACTIVE" ${form.status eq 'ACTIVE' or empty form.status ? 'selected' : ''}>Đang hoạt động (ACTIVE)</option>
                    <option value="INACTIVE" ${form.status eq 'INACTIVE' ? 'selected' : ''}>Ngừng hoạt động (INACTIVE)</option>
                  </select>
                  <c:if test="${not empty errors.status}">
                    <small class="text-danger"><c:out value="${errors.status}" /></small>
                  </c:if>
                </div>
              </div>

              <div class="form-actions-bar">
                <button class="btn" type="submit"><c:out value="${amenitySubmitLabel}" /></button>
                <a class="btn btn-secondary" href="${cp}/manager/amenities">Hủy bỏ</a>
              </div>
            </div>
          </section>
        </section>
      </form>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${cp}/assets/js/amenity-form.js?v=20260825-1"></script>
  </body>
</html>