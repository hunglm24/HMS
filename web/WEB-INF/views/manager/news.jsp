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
    <title>Quản lý Tin tức & Khuyến mãi | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7" />
    <style>
      .news-thumb-img {
        width: 60px;
        height: 40px;
        border-radius: 6px;
        object-fit: cover;
        border: 1px solid #e2e8f0;
        background: #f8fafc;
        display: block;
      }
      .news-thumb-fallback {
        width: 60px;
        height: 40px;
        border-radius: 6px;
        background: #eff6ff;
        color: #2563eb;
        font-weight: 700;
        font-size: 11px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border: 1px solid #dbeafe;
      }
    </style>
  </head>
  <body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">QUẢN LÝ KHÁCH SẠN</p>
          <h1>Quản lý Tin tức & Sự kiện</h1>
          <p>
            Quản lý các bài viết tin tức, sự kiện và chương trình khuyến mãi hiển thị trên trang chủ và cổng thông tin.
          </p>
        </div>
        <div class="room-management-hero__actions">
          <a class="btn" href="${cp}/manager/news/create">
            + Thêm bài viết
          </a>
        </div>
      </section>

      <section class="room-management-content">
        <section class="room-management-panel panel">
          <div class="room-management-toolbar">
            <form class="room-management-filters" method="get" action="${cp}/manager/news" style="grid-template-columns: minmax(260px, 2fr) minmax(180px, 1fr) auto auto; gap: var(--space-3); align-items: end;">
              <div class="room-management-filters__search">
                <input type="search" name="search" value="${fn:escapeXml(search)}" placeholder="Tìm theo tiêu đề bài viết..." />
              </div>
              <div class="room-management-filters__select">
                <select name="status">
                  <option value="ALL" ${empty status or status eq 'ALL' ? 'selected' : ''}>Trạng thái: Tất cả</option>
                  <option value="PUBLISHED" ${status eq 'PUBLISHED' ? 'selected' : ''}>Đã xuất bản</option>
                  <option value="DRAFT" ${status eq 'DRAFT' ? 'selected' : ''}>Bản nháp</option>
                  <option value="HIDDEN" ${status eq 'HIDDEN' ? 'selected' : ''}>Đã ẩn</option>
                </select>
              </div>
              <button class="btn btn-primary" type="submit">Lọc</button>
              <a class="btn btn-secondary" href="${cp}/manager/news" style="text-decoration: none; text-align: center;">Đặt lại</a>
            </form>
          </div>

          <div class="room-management-table-wrap">
            <table class="room-management-table">
              <thead>
                <tr>
                  <th style="width: 75px; text-align: center;">Ảnh</th>
                  <th>Tiêu đề bài viết</th>
                  <th>Trạng thái</th>
                  <th>Tác giả</th>
                  <th>Ngày xuất bản</th>
                  <th style="text-align: right;">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <c:choose>
                  <c:when test="${empty newsList}">
                    <tr>
                      <td colspan="6">
                        <div class="room-management-empty">
                          <strong>Chưa có bài viết nào</strong>
                          <span>Nhấn vào nút "+ Thêm bài viết" để tạo tin tức mới.</span>
                        </div>
                      </td>
                    </tr>
                  </c:when>
                  <c:otherwise>
                    <c:forEach var="n" items="${newsList}">
                      <tr>
                        <td style="text-align: center;">
                          <c:choose>
                            <c:when test="${not empty n.thumbnailUrl}">
                              <img src="${fn:startsWith(n.thumbnailUrl, '/') ? cp.concat(n.thumbnailUrl) : n.thumbnailUrl}" class="news-thumb-img" alt="Thumbnail">
                            </c:when>
                            <c:otherwise>
                              <div class="news-thumb-fallback">No Pic</div>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <strong style="color: #1e293b; font-size: 15px;"><c:out value="${n.title}" /></strong>
                        </td>
                        <td>
                          <c:choose>
                            <c:when test="${n.status eq 'PUBLISHED'}">
                              <span class="status-chip status-available">Đã xuất bản</span>
                            </c:when>
                            <c:when test="${n.status eq 'DRAFT'}">
                              <span class="status-chip status-pending">Bản nháp</span>
                            </c:when>
                            <c:otherwise>
                              <span class="status-chip status-maintenance">Đã ẩn</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                        <td><c:out value="${empty n.creatorName ? '-' : n.creatorName}" /></td>
                        <td>
                          <c:choose>
                            <c:when test="${not empty n.publishedAt}">
                              <fmt:formatDate value="${n.publishedAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                          </c:choose>
                        </td>
                        <td>
                          <div class="room-management-actions" style="justify-content: flex-end; gap: 8px;">
                            <a class="btn btn-secondary btn-sm" href="${cp}/manager/news/edit?id=${n.id}">Sửa</a>
                            <form action="${cp}/manager/news/delete" method="post" style="margin: 0; display: inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa bài viết này không?');">
                              <input type="hidden" name="id" value="${n.id}">
                              <button type="submit" class="btn btn-secondary btn-sm" style="color: #dc2626; border-color: #fecaca; background: #fff5f5;">Xóa</button>
                            </form>
                          </div>
                        </td>
                      </tr>
                    </c:forEach>
                  </c:otherwise>
                </c:choose>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <c:if test="${totalPages > 1}">
            <div class="room-management-pagination">
              <c:forEach begin="1" end="${totalPages}" var="p">
                <a class="room-management-pagination__page ${p == currentPage ? 'is-active' : ''}" href="?page=${p}&search=${fn:escapeXml(search)}&status=${status}" style="text-decoration: none;">${p}</a>
              </c:forEach>
            </div>
          </c:if>
        </section>
      </section>
    </main>
  </body>
</html>
