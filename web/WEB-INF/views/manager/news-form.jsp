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
    <title>${not empty news.id ? 'Chỉnh sửa bài viết' : 'Thêm bài viết mới'} | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" />
    <!-- TinyMCE (Free CDNJS, no API key warning) -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/tinymce/6.8.3/tinymce.min.js"></script>
    <style>
      .news-form-layout {
        display: grid;
        grid-template-columns: minmax(0, 1fr) 340px;
        gap: 24px;
        align-items: start;
        width: 100%;
        margin-top: 24px;
      }
      @media (max-width: 980px) {
        .news-form-layout {
          grid-template-columns: 1fr;
        }
      }
      .news-form-main,
      .news-form-side {
        min-width: 0;
      }
      .news-form-card {
        padding: 24px;
        background: #ffffff;
        border: 1px solid var(--color-border);
        border-radius: var(--radius-lg);
        box-shadow: var(--elevation-1);
        margin-bottom: 24px;
      }
      .news-form-card__head {
        margin-bottom: 18px;
        padding-bottom: 12px;
        border-bottom: 1px solid var(--color-border);
      }
      .news-form-card__head h2,
      .news-form-card__head h3 {
        font-size: 17px;
        font-weight: 700;
        color: #1e293b;
        margin: 0;
      }
      .news-form-field {
        display: block;
        margin-bottom: 20px;
      }
      .news-form-field > span {
        display: block;
        font-size: 14px;
        font-weight: 600;
        color: #334155;
        margin-bottom: 8px;
      }
      .news-form-field input[type="text"],
      .news-form-field select {
        width: 100%;
        padding: 10px 14px;
        border: 1px solid var(--color-border);
        border-radius: 8px;
        font-size: 14px;
        box-sizing: border-box;
      }
      .news-form-field input[type="text"]:focus,
      .news-form-field select:focus {
        border-color: var(--color-primary-600);
        outline: none;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
      }
      .news-thumb-box {
        text-align: center;
      }
      .news-thumb-preview {
        width: 100%;
        height: 180px;
        border-radius: 8px;
        object-fit: cover;
        border: 1px solid #e2e8f0;
        background: #f8fafc;
        margin-bottom: 14px;
        display: block;
      }
      /* Reset global CSS button styles inside TinyMCE */
      .tox-tinymce,
      .tox {
        border-radius: 8px !important;
        border: 1px solid var(--color-border) !important;
        box-sizing: border-box !important;
      }
      .tox .tox-toolbar,
      .tox .tox-toolbar__primary,
      .tox .tox-toolbar__overflow {
        background: #f8fafc !important;
        padding: 4px 6px !important;
      }
      .tox button,
      .tox .tox-tbtn,
      .tox .tox-tbtn--bespoke,
      .tox .tox-tbtn--select {
        min-height: unset !important;
        padding: 0 6px !important;
        border-radius: 4px !important;
        box-shadow: none !important;
        box-sizing: border-box !important;
        font-family: inherit !important;
      }
      .tox .tox-tbtn {
        height: 32px !important;
        min-height: 32px !important;
      }
      .tox .tox-tbtn--select {
        width: auto !important;
        min-width: 100px !important;
        height: 32px !important;
      }
    </style>
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-management-page">
      <form action="${cp}${not empty news.id ? '/manager/news/edit' : '/manager/news/create'}" method="post" id="newsForm" enctype="multipart/form-data">
        <c:if test="${not empty news.id}">
          <input type="hidden" name="id" value="${news.id}" />
        </c:if>

        <section class="room-management-hero panel">
          <div class="room-management-hero__copy">
            <a class="btn btn-secondary btn-sm" href="${cp}/manager/news" style="display: inline-block; margin-bottom: 8px; text-decoration: none;">
              &larr; Quay lại danh sách
            </a>
            <h1>${not empty news.id ? 'Chỉnh sửa bài viết' : 'Thêm bài viết mới'}</h1>
            <p>Điền tiêu đề, nội dung chi tiết và tải ảnh đại diện bài viết.</p>
          </div>
        </section>

        <section class="news-form-layout">
          <!-- Main content column (Left) -->
          <div class="news-form-main">
            <article class="news-form-card">
              <div class="news-form-card__head">
                <h2>Nội dung bài viết</h2>
              </div>

              <label class="news-form-field">
                <span>Tiêu đề bài viết <strong style="color: #dc2626;">*</strong></span>
                <input type="text" name="title" id="title" value="<c:out value='${news.title}' />" placeholder="Nhập tiêu đề bài viết..." required maxlength="255" />
              </label>

              <div class="news-form-field" style="margin-bottom: 0;">
                <span>Nội dung chi tiết <strong style="color: #dc2626;">*</strong></span>
                <textarea id="newsContent" name="content">${news.content}</textarea>
              </div>
            </article>
          </div>

          <!-- Sidebar options column (Right) -->
          <div class="news-form-side">
            <article class="news-form-card">
              <div class="news-form-card__head">
                <h3>Trạng thái xuất bản</h3>
              </div>

              <label class="news-form-field">
                <span>Trạng thái</span>
                <select name="status" id="status">
                  <option value="PUBLISHED" ${news.status eq 'PUBLISHED' ? 'selected' : ''}>Đã xuất bản (PUBLISHED)</option>
                  <option value="DRAFT" ${empty news.status or news.status eq 'DRAFT' ? 'selected' : ''}>Bản nháp (DRAFT)</option>
                  <option value="HIDDEN" ${news.status eq 'HIDDEN' ? 'selected' : ''}>Đã ẩn (HIDDEN)</option>
                </select>
              </label>

              <button class="btn btn-primary" type="submit" style="width: 100%; justify-content: center;">
                Lưu bài viết
              </button>
            </article>

            <article class="news-form-card">
              <div class="news-form-card__head">
                <h3>Ảnh đại diện (Thumbnail)</h3>
              </div>

              <div class="news-thumb-box">
                <c:set var="currentThumb" value="${not empty news.thumbnailUrl ? news.thumbnailUrl : ''}" />
                <c:choose>
                  <c:when test="${not empty currentThumb}">
                    <c:set var="thumbSrc" value="${fn:startsWith(currentThumb, '/') ? cp.concat(currentThumb) : currentThumb}" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="thumbSrc" value="https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80" />
                  </c:otherwise>
                </c:choose>
                <img id="thumbnailPreview" src="${thumbSrc}" alt="Preview" class="news-thumb-preview" />
                
                <input type="file" id="thumbnailFile" name="thumbnailFile" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" style="display: none;" onchange="previewSelectedImage(this)" />
                <input type="hidden" name="thumbnailUrl" id="thumbnailUrl" value="<c:out value='${news.thumbnailUrl}' />" />

                <button type="button" class="btn btn-secondary" style="width: 100%; margin-bottom: 8px; justify-content: center;" onclick="document.getElementById('thumbnailFile').click();">
                  <i class="fa-solid fa-cloud-arrow-up" style="margin-right: 6px;"></i> Chọn ảnh từ máy tính
                </button>
                <small id="selectedFileName" style="display: block; color: #64748b; font-size: 12px; margin-bottom: 6px; word-break: break-all;">Chấp nhận JPG, PNG, WEBP (Tối đa 5MB)</small>
              </div>
            </article>
          </div>
        </section>
      </form>
    </main>

    <script>
      document.addEventListener("DOMContentLoaded", function() {
        tinymce.init({
          selector: '#newsContent',
          height: 460,
          menubar: false,
          toolbar_mode: 'wrap',
          plugins: 'lists link image table code wordcount',
          toolbar: 'undo redo | blocks | bold italic underline | bullist numlist | link image table | removeformat code',
          branding: false,
          promotion: false,
          statusbar: false,
          content_style: 'body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 14px; line-height: 1.6; color: #334155; }'
        });
      });

      function previewSelectedImage(input) {
        if (input.files && input.files[0]) {
          var file = input.files[0];
          var fileNameSpan = document.getElementById("selectedFileName");
          if (fileNameSpan) {
            fileNameSpan.textContent = "Đã chọn: " + file.name + " (" + (file.size / (1024 * 1024)).toFixed(2) + " MB)";
            fileNameSpan.style.color = "#15803d";
            fileNameSpan.style.fontWeight = "600";
          }
          var reader = new FileReader();
          reader.onload = function(e) {
            document.getElementById("thumbnailPreview").src = e.target.result;
          };
          reader.readAsDataURL(file);
        }
      }
    </script>
  </body>
</html>
