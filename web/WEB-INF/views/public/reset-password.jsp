<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String escapeAttr(Object value) {
        if (value == null) return "";
        return value.toString()
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
%>
<%
    model.HotelConfig resetConfig = (model.HotelConfig) application.getAttribute("hotelConfig");
    String resetHotelName = resetConfig != null && resetConfig.getHotelName() != null && !resetConfig.getHotelName().isBlank()
            ? resetConfig.getHotelName()
            : "HMS Hotel";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Đặt lại mật khẩu | <%= escapeAttr(resetHotelName) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260827-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<main id="main-content" class="auth-center-page forgot-password-page">
    <div class="auth-center-container">
        <section class="auth-card-center" aria-labelledby="auth-title">
            <div class="auth-header-brand">
                <a class="auth-brand" href="${pageContext.request.contextPath}/"><%= escapeAttr(resetHotelName) %></a>
                <p class="auth-subtitle">Hệ thống quản lý khách sạn</p>
            </div>

            <div class="auth-icon-badge" aria-hidden="true">
                <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 2l-2 2m-1-1l-3 3 2 2 3-3-2-2z"></path>
                    <path d="M3 21l10-10"></path>
                    <path d="M7 21l4-4"></path>
                    <circle cx="16.5" cy="7.5" r="4.5"></circle>
                </svg>
            </div>

            <h1 id="auth-title">Đặt lại mật khẩu mới</h1>
            <p class="auth-form-note">Vui lòng nhập mật khẩu mới với tối thiểu 8 ký tự để bảo vệ tài khoản của bạn.</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="auth-message error" role="alert">
                    <svg class="auth-message-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="8" x2="12" y2="12"></line>
                        <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <span><%= escapeAttr(request.getAttribute("error")) %></span>
                </div>
            <% } %>

            <form class="auth-form" method="post" action="${pageContext.request.contextPath}/reset-password">
                <input type="hidden" name="token" value="${token}">
                <div class="auth-field">
                    <label for="password">Mật khẩu mới</label>
                    <div class="password-wrap">
                        <input id="password" name="password" type="password" minlength="8" required autofocus placeholder="Nhập mật khẩu mới">
                        <button class="password-visibility" type="button" data-toggle-password="password" aria-label="Hiện mật khẩu">Hiện</button>
                    </div>
                </div>
                <div class="auth-field">
                    <label for="confirmPassword">Xác nhận mật khẩu</label>
                    <div class="password-wrap">
                        <input id="confirmPassword" name="confirmPassword" type="password" minlength="8" required placeholder="Nhập lại mật khẩu mới">
                        <button class="password-visibility" type="button" data-toggle-password="confirmPassword" aria-label="Hiện mật khẩu xác nhận">Hiện</button>
                    </div>
                </div>
                <button class="auth-submit" type="submit" data-loading-label="Đang cập nhật...">
                    <span>Đặt lại mật khẩu</span>
                    <svg class="auth-btn-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                        <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                </button>
            </form>

            <div class="auth-footer-actions">
                <a class="auth-back-link" href="${pageContext.request.contextPath}/login">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="19" y1="12" x2="5" y2="12"></line>
                        <polyline points="12 19 5 12 12 5"></polyline>
                    </svg>
                    <span>Quay lại đăng nhập</span>
                </a>
            </div>
        </section>
    </div>
</main>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
