<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đánh Giá Khách Hàng | HMS</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="${cp}/assets/css/housekeeping.css?v=20260825-1">
    <link rel="stylesheet" href="${cp}/assets/css/feedback.css?v=20260824-1">
</head>
<body class="room-management-body">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

    <main class="page-container hk-page">
        <!-- Hero Section gọn gàng theo chuẩn Lịch sử dọn phòng -->
        <section class="hk-hero">
            <div>
                <p class="hk-eyebrow">CHẤT LƯỢNG DỊCH VỤ</p>
                <h1>Đánh Giá &amp; Phản Hồi Khách Hàng</h1>
                <p>Theo dõi trải nghiệm của khách lưu trú, quản lý hiển thị và chuyển tiếp sự cố cho bộ phận liên quan.</p>
            </div>
            <div style="display: flex; gap: 12px; align-items: center;">
                <div class="hk-total" style="min-width: 120px;">
                    <strong style="color: #f59e0b;"><fmt:formatNumber value="${avgRating}" maxFractionDigits="1" /> ★</strong>
                    <span>Điểm trung bình</span>
                </div>
                <div class="hk-total" style="min-width: 100px;">
                    <strong>${totalFeedbacks}</strong>
                    <span>Tổng đánh giá</span>
                </div>
            </div>
        </section>

        <c:if test="${not empty sessionScope.toastMessage}">
            <div class="toast ${sessionScope.toastType}">
                <c:out value="${sessionScope.toastMessage}" />
            </div>
            <c:remove var="toastMessage" scope="session" />
            <c:remove var="toastType" scope="session" />
        </c:if>

        <!-- Bộ lọc Form theo đúng cấu trúc Lịch sử dọn phòng (hk-filters feedback-filters) -->
        <form class="hk-filters feedback-filters" method="get" action="${cp}/manager/feedbacks">
            <label class="hk-search">Tìm kiếm
                <input type="search" name="keyword" maxlength="50" value="<c:out value='${keyword}'/>"
                       placeholder="Tên khách, mã booking, phòng, bình luận...">
            </label>
            <label>Đánh giá
                <select name="rating">
                    <option value="ALL" ${empty rating or rating eq 'ALL' ? 'selected' : ''}>Tất cả sao</option>
                    <option value="5" ${rating eq '5' ? 'selected' : ''}>5 sao (⭐⭐⭐⭐⭐)</option>
                    <option value="4" ${rating eq '4' ? 'selected' : ''}>4 sao (⭐⭐⭐⭐)</option>
                    <option value="3" ${rating eq '3' ? 'selected' : ''}>3 sao (⭐⭐⭐)</option>
                    <option value="2" ${rating eq '2' ? 'selected' : ''}>2 sao (⭐⭐)</option>
                    <option value="1" ${rating eq '1' ? 'selected' : ''}>1 sao (⭐)</option>
                </select>
            </label>
            <label>Trạng thái
                <select name="status">
                    <option value="ALL" ${empty status or status eq 'ALL' ? 'selected' : ''}>Tất cả trạng thái</option>
                    <option value="VISIBLE" ${status eq 'VISIBLE' ? 'selected' : ''}>Đang hiển thị</option>
                    <option value="HIDDEN" ${status eq 'HIDDEN' ? 'selected' : ''}>Đã ẩn</option>
                </select>
            </label>
            <div class="hk-filter-actions">
                <button type="submit">Lọc</button>
                <a href="${cp}/manager/feedbacks">Đặt lại</a>
            </div>
        </form>

        <!-- Bảng danh sách Feedback chuẩn hk-table kèm phân trang -->
        <div class="hk-table-wrap" data-pagination-root data-pagination-key="feedbacks" data-pagination-size="10">
            <table class="hk-table">
                <thead>
                    <tr>
                        <th>Mã Đặt Phòng</th>
                        <th>Phòng</th>
                        <th>Khách Hàng</th>
                        <th>Đánh Giá</th>
                        <th>Bình Luận</th>
                        <th>Thời Gian</th>
                        <th>Trạng Thái</th>
                        <th style="text-align: center; width: 280px;"><span class="sr-only">Thao tác</span></th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty feedbacks}">
                            <c:forEach var="f" items="${feedbacks}">
                                <tr data-pagination-item>
                                    <td><strong>${f.bookingCode}</strong></td>
                                    <td>
                                        <c:if test="${not empty f.roomNumbers}">
                                            <span class="hk-room-number">${f.roomNumbers}</span>
                                        </c:if>
                                        <c:if test="${not empty f.roomTypeNames}">
                                            <small style="display:block; color:#64748b; margin-top:2px;">${f.roomTypeNames}</small>
                                        </c:if>
                                    </td>
                                    <td><strong>${f.customerName}</strong></td>
                                    <td>
                                        <div class="feedback-star-display">
                                            <c:forEach begin="1" end="${f.rating}"><i class="fas fa-star"></i></c:forEach>
                                            <c:forEach begin="${f.rating + 1}" end="5"><i class="far fa-star feedback-star-empty"></i></c:forEach>
                                        </div>
                                    </td>
                                    <td style="max-width: 280px; font-size: 13.5px; line-height: 1.4;">
                                        <c:out value="${not empty f.comment ? f.comment : '<i>(Không có bình luận)</i>'}" escapeXml="false" />
                                    </td>
                                    <td style="white-space: nowrap; font-size: 13px; color: var(--hk-muted);">
                                        <fmt:formatDate value="${f.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${f.status == 'VISIBLE'}">
                                                <span class="hk-badge task-completed"><i class="fas fa-eye" style="margin-right: 4px;"></i> Hiển thị</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="hk-badge task-cancelled"><i class="fas fa-eye-slash" style="margin-right: 4px;"></i> Đã ẩn</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="hk-row-action feedback-actions-cell">
                                        <div class="feedback-actions-wrap">
                                            <!-- Form toggle Ẩn/Hiện -->
                                            <form action="${cp}/manager/feedbacks" method="post" style="margin: 0;">
                                                <input type="hidden" name="action" value="toggleStatus">
                                                <input type="hidden" name="id" value="${f.id}">
                                                <c:choose>
                                                    <c:when test="${f.status == 'VISIBLE'}">
                                                        <input type="hidden" name="status" value="HIDDEN">
                                                        <button type="submit" class="btn-fb-toggle btn-fb-toggle-hide" title="Ẩn khỏi trang chủ">Ẩn</button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <input type="hidden" name="status" value="VISIBLE">
                                                        <button type="submit" class="btn-fb-toggle btn-fb-toggle-show" title="Cho phép hiển thị">Hiện</button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </form>

                                            <!-- Nút Báo sự cố -->
                                            <c:url var="reportUrl" value="/manager/issues/report">
                                                <c:if test="${not empty f.primaryRoomId}">
                                                    <c:param name="roomId" value="${f.primaryRoomId}" />
                                                </c:if>
                                            </c:url>
                                            <a href="${reportUrl}" class="btn-fb-report" title="Báo sự cố thiết bị / dọn phòng cho phòng này">
                                                <i class="fas fa-wrench"></i> Báo sự cố
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" style="text-align: center; padding: 48px 16px; color: var(--hk-muted);">
                                    <i class="far fa-comment-dots" style="font-size: 32px; display: block; margin-bottom: 8px; color: #98a2b3;"></i>
                                    Không tìm thấy đánh giá nào phù hợp với bộ lọc.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
            <!-- Thanh điều khiển phân trang Pagination -->
            <div class="room-management-pagination" data-pagination-controls data-pagination-target="feedbacks"></div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${cp}/assets/js/pagination.js?v=20260824-2"></script>
    <script src="${cp}/assets/js/feedback.js?v=20260824-1"></script>
</body>
</html>
