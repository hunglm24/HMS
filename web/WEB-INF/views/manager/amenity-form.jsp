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
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <style>
      .form-control {
        width: 100%;
        padding: 10px 14px;
        border: 1.5px solid var(--color-border);
        border-radius: var(--radius-md);
        font-family: inherit;
        font-size: 14.5px;
        box-sizing: border-box;
        transition: border-color 0.2s, box-shadow 0.2s;
      }
      .form-control:focus {
        border-color: var(--color-primary-600);
        outline: none;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
      }
      .hk-back {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 13.5px;
        font-weight: 600;
        color: var(--color-primary-600);
        text-decoration: none;
        transition: color 0.2s;
      }
      .hk-back:hover {
        color: var(--color-primary-800);
        text-decoration: underline;
      }
    </style>
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-management-page">
      <form class="amenity-form" action="${cp}${amenityFormAction}" method="post" novalidate>
        <c:if test="${isEditMode}">
          <input type="hidden" name="id" value="${amenityId}" />
        </c:if>

        <section class="room-management-hero panel">
          <div class="room-management-hero__copy">
            <a class="hk-back" href="${cp}/manager/amenities" style="margin-bottom: 12px; display: inline-block;">← Quay lại danh sách tiện nghi</a>
            <p class="room-management-kicker">Quản lý khách sạn</p>
            <h1><c:out value="${amenityPageHeading}" /></h1>
            <p><c:out value="${amenityPageSubtitle}" /></p>
          </div>


        </section>

        <c:if test="${not empty errors}">
          <div class="alert alert-danger" style="margin-bottom: 20px;">
            <strong>Vui lòng kiểm tra lại các trường thông tin:</strong>
            <ul style="margin: 8px 0 0 20px; padding: 0;">
              <c:forEach items="${errors}" var="entry">
                <li><c:out value="${entry.value}" /></li>
              </c:forEach>
            </ul>
          </div>
        </c:if>

        <section class="room-management-content" style="max-width: 860px; margin: 0 auto; width: 100%;">
          <section class="room-management-panel panel">
            <div style="display: grid; gap: 20px;">
              <div>
                <h2 style="font-size: 1.15rem; color: #1e293b; margin-bottom: 4px;">Thông tin tiện nghi</h2>
                <p style="color: #64748b; font-size: 13.5px; margin: 0;">Điền các thông tin của tiện nghi để hiển thị khi quản lý phòng và đặt phòng.</p>
              </div>

              <div class="form-group">
                <label style="display: block; font-weight: 600; margin-bottom: 6px; color: #334155;">
                  Tên tiện nghi <span style="color: #ef4444;">*</span>
                </label>
                <input type="text" name="name" class="form-control" value="<c:out value='${form.name}' />" maxlength="100" required placeholder="Ví dụ: Wi-Fi tốc độ cao, Smart TV 55 inch..." />
                <c:if test="${not empty errors.name}">
                  <small style="color: #ef4444; display: block; margin-top: 4px;"><c:out value="${errors.name}" /></small>
                </c:if>
              </div>

              <div class="form-group">
                <label style="display: block; font-weight: 600; margin-bottom: 6px; color: #334155;">
                  Mô tả chi tiết
                </label>
                <textarea name="description" rows="4" class="form-control" maxlength="500" placeholder="Mô tả công dụng hoặc thông số của tiện nghi..."><c:out value="${form.description}" /></textarea>
                <c:if test="${not empty errors.description}">
                  <small style="color: #ef4444; display: block; margin-top: 4px;"><c:out value="${errors.description}" /></small>
                </c:if>
              </div>

              <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px;">
                <div class="form-group">
                  <label style="display: block; font-weight: 600; margin-bottom: 6px; color: #334155;">
                    Mã icon FontAwesome <span style="color: #ef4444;">*</span>
                  </label>
                  <div style="display: flex; gap: 10px; align-items: center;">
                    <div id="iconPreview" style="width: 44px; height: 44px; border-radius: 8px; background: #eff6ff; color: #2563eb; font-size: 20px; display: flex; align-items: center; justify-content: center; border: 1px solid #dbeafe; flex-shrink: 0;">
                      <i id="previewIconEl" class="<c:out value='${not empty form.icon ? form.icon : "fa-solid fa-star"}' />"></i>
                    </div>
                    <input type="text" id="iconInput" name="icon" class="form-control" value="<c:out value='${form.icon}' />" maxlength="50" required placeholder="Ví dụ: fa-solid fa-wifi" oninput="updateIconPreview(this.value)" />
                  </div>
                  <small style="color: #64748b; display: block; margin-top: 4px;">Ví dụ: <code>fa-solid fa-wifi</code>, <code>fa-solid fa-tv</code>, <code>fa-solid fa-snowflake</code></small>
                  <c:if test="${not empty errors.icon}">
                    <small style="color: #ef4444; display: block; margin-top: 4px;"><c:out value="${errors.icon}" /></small>
                  </c:if>
                </div>

                <div class="form-group">
                  <label style="display: block; font-weight: 600; margin-bottom: 6px; color: #334155;">
                    Trạng thái <span style="color: #ef4444;">*</span>
                  </label>
                  <select name="status" class="form-control" style="height: 44px;">
                    <option value="ACTIVE" ${form.status eq 'ACTIVE' or empty form.status ? 'selected' : ''}>Đang hoạt động (ACTIVE)</option>
                    <option value="INACTIVE" ${form.status eq 'INACTIVE' ? 'selected' : ''}>Ngừng hoạt động (INACTIVE)</option>
                  </select>
                  <c:if test="${not empty errors.status}">
                    <small style="color: #ef4444; display: block; margin-top: 4px;"><c:out value="${errors.status}" /></small>
                  </c:if>
                </div>
              </div>

              <div style="display: flex; gap: 12px; margin-top: 12px; border-top: 1px solid #e2e8f0; padding-top: 20px;">
                <button class="btn" type="submit"><c:out value="${amenitySubmitLabel}" /></button>
                <a class="btn btn-secondary" href="${cp}/manager/amenities">Hủy bỏ</a>
              </div>
            </div>
          </section>
        </section>
      </form>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script>
      function updateIconPreview(val) {
        const el = document.getElementById('previewIconEl');
        if (el) {
          el.className = val && val.trim() ? val.trim() : 'fa-solid fa-star';
        }
      }
    </script>
  </body>
</html>