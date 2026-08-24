<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="toastMessage" value="${sessionScope.toastMessage}" />
<c:set var="toastType" value="${sessionScope.toastType}" />
<c:remove var="toastMessage" scope="session" />
<c:remove var="toastType" scope="session" />
<fmt:formatDate var="checkInTimeValue" value="${config.checkInTime}" pattern="HH:mm" />
<fmt:formatDate var="checkOutTimeValue" value="${config.checkOutTime}" pattern="HH:mm" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cấu hình | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${cp}/assets/css/system-config.css?v=20260824-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container system-config-page">
    <section class="config-hero">
        <div>
            <p class="section-kicker">Quản trị</p>
            <h1>Cấu hình hệ thống khách sạn</h1>
            <p>Chỉnh một bản ghi duy nhất cho thông tin khách sạn, giờ vận hành, hoàn tiền và phí mặc định.</p>
        </div>
        <div class="config-hero__chips">
            <span class="config-chip">Singleton record</span>
            <span class="config-chip config-chip--muted">Audit enabled</span>
        </div>
    </section>

    <c:if test="${not empty toastMessage}">
        <div class="config-toast ${toastType eq 'toast-success' ? 'is-success' : 'is-error'}">
            <c:out value="${toastMessage}" />
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="config-toast is-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <section class="config-layout">
        <article class="config-card config-card--form">
            <div class="config-card__head">
                <div>
                    <h2>Chỉnh cấu hình</h2>
                    <p>Chỉ sửa bản ghi hiện tại, không tạo thêm cấu hình mới.</p>
                </div>
                <div class="config-record-meta">
                    <span>ID</span>
                    <strong><c:out value="${empty config.id ? 'New' : config.id}" /></strong>
                    <span>Cập nhật</span>
                    <strong>
                        <c:choose>
                            <c:when test="${not empty config.updatedAt}">
                                <fmt:formatDate value="${config.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                            </c:when>
                            <c:otherwise>Chưa có</c:otherwise>
                        </c:choose>
                    </strong>
                </div>
            </div>

            <form id="hotelConfigForm" method="post" action="${cp}/admin/system-config" class="config-form">
                <input type="hidden" name="id" value="${config.id}">

                <div class="config-section">
                    <h3>Thông tin khách sạn</h3>
                    <div class="config-grid">
                        <label class="config-field">
                            <span>Tên khách sạn</span>
                            <input id="hotelName" name="hotelName" type="text" maxlength="150" value="<c:out value='${config.hotelName}' />" required>
                        </label>
                        <label class="config-field">
                            <span>Địa chỉ</span>
                            <input id="address" name="address" type="text" maxlength="255" value="<c:out value='${config.address}' />" required>
                        </label>
                        <label class="config-field">
                            <span>Số điện thoại</span>
                            <input id="phone" name="phone" type="text" maxlength="30" value="<c:out value='${config.phone}' />" pattern="[0-9+() .-]{8,20}" title="Nhập số điện thoại hợp lệ" required>
                        </label>
                        <label class="config-field">
                            <span>Email</span>
                            <input id="email" name="email" type="email" maxlength="150" value="<c:out value='${config.email}' />" title="Nhập email hợp lệ" required>
                        </label>
                    </div>
                </div>

                <div class="config-section">
                    <h3>Giờ vận hành</h3>
                    <div class="config-grid">
                        <label class="config-field">
                            <span>Giờ nhận phòng</span>
                            <input id="checkInTime" name="checkInTime" type="time" value="${checkInTimeValue}" required>
                        </label>
                        <label class="config-field">
                            <span>Giờ trả phòng</span>
                            <input id="checkOutTime" name="checkOutTime" type="time" value="${checkOutTimeValue}" required>
                        </label>
                    </div>
                    <p class="config-hint">Hai mốc giờ này sẽ được dùng ở luồng check-in/check-out và màn hình vận hành.</p>
                </div>

                <div class="config-section">
                    <h3>Chính sách hoàn tiền</h3>
                    <div class="config-grid">
                        <label class="config-field">
                            <span>Tỷ lệ hoàn tiền cùng ngày (%)</span>
                            <input id="sameDayRefundRate" name="sameDayRefundRate" type="number" min="0" max="100" step="0.01" value="<c:out value='${config.sameDayRefundRate}' />" required>
                        </label>
                        <label class="config-field">
                            <span>Tỷ lệ hoàn tiền trước ngày (%)</span>
                            <input id="beforeDayRefundRate" name="beforeDayRefundRate" type="number" min="0" max="100" step="0.01" value="<c:out value='${config.beforeDayRefundRate}' />" required>
                        </label>
                    </div>
                </div>

                <div class="config-section">
                    <h3>Phí tính vào hóa đơn</h3>
                    <div class="config-grid">
                        <label class="config-field">
                            <span>Thuế (%)</span>
                            <input id="taxRate" name="taxRate" type="number" min="0" max="100" step="0.01" value="<c:out value='${config.taxRate}' />" required>
                        </label>
                        <label class="config-field">
                            <span>Phí dịch vụ (%)</span>
                            <input id="serviceFeeRate" name="serviceFeeRate" type="number" min="0" max="100" step="0.01" value="<c:out value='${config.serviceFeeRate}' />" required>
                        </label>
                    </div>
                </div>

                <div class="config-actions">
                    <button class="btn" type="reset">Khôi phục</button>
                    <button class="btn btn-primary" type="submit">Lưu cấu hình</button>
                </div>
            </form>
        </article>

        <aside class="config-side">
            <section class="config-card config-card--summary">
                <div class="config-card__head">
                    <div>
                        <h2>Snapshot hiện tại</h2>
                        <p>Xem nhanh giá trị sẽ được dùng trên toàn hệ thống.</p>
                    </div>
                </div>
                <dl class="config-summary">
                    <div>
                        <dt>Tên khách sạn</dt>
                        <dd data-preview="hotelName"><c:out value="${config.hotelName}" /></dd>
                    </div>
                    <div>
                        <dt>Giờ nhận / trả phòng</dt>
                        <dd>
                            <span data-preview="checkInTime"><c:out value="${checkInTimeValue}" /></span>
                            <span class="config-summary__slash">/</span>
                            <span data-preview="checkOutTime"><c:out value="${checkOutTimeValue}" /></span>
                        </dd>
                    </div>
                    <div>
                        <dt>Hoàn tiền cùng ngày</dt>
                        <dd><span data-preview="sameDayRefundRate"><c:out value="${config.sameDayRefundRate}" /></span>%</dd>
                    </div>
                    <div>
                        <dt>Hoàn tiền trước ngày</dt>
                        <dd><span data-preview="beforeDayRefundRate"><c:out value="${config.beforeDayRefundRate}" /></span>%</dd>
                    </div>
                    <div>
                        <dt>Thuế / phí dịch vụ</dt>
                        <dd>
                            <span data-preview="taxRate"><c:out value="${config.taxRate}" /></span>% /
                            <span data-preview="serviceFeeRate"><c:out value="${config.serviceFeeRate}" /></span>%
                        </dd>
                    </div>
                </dl>
            </section>

            <section class="config-card config-card--history">
                <div class="config-card__head">
                    <div>
                        <h2>Lịch sử sửa gần đây</h2>
                        <p>Được lấy từ AuditLog để truy vết thay đổi.</p>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${not empty recentConfigLogs}">
                        <ul class="config-history">
                            <c:forEach var="log" items="${recentConfigLogs}">
                                <li class="config-history__item">
                                    <div class="config-history__meta">
                                        <strong><c:out value="${empty log.actorName ? 'System' : log.actorName}" /></strong>
                                        <span>
                                            <fmt:formatDate value="${log.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                        </span>
                                    </div>
                                    <p><c:out value="${log.detail}" /></p>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        <div class="config-empty">
                            Chưa có lịch sử thay đổi nào.
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </aside>
    </section>
</main>
<script src="${cp}/assets/js/system-config.js?v=20260824-1"></script>
</body>
</html>
