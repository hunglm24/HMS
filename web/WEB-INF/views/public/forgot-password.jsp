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
    model.HotelConfig forgotConfig = (model.HotelConfig) application.getAttribute("hotelConfig");
    String forgotHotelName = forgotConfig != null && forgotConfig.getHotelName() != null && !forgotConfig.getHotelName().isBlank()
            ? forgotConfig.getHotelName()
            : "HMS Hotel";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quên mật khẩu | <%= escapeAttr(forgotHotelName) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260827-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-center-page forgot-password-page">
    <div class="auth-center-container">
        <section class="auth-card-center" aria-labelledby="auth-title">
            <div class="auth-header-brand">
                <a class="auth-brand" href="${pageContext.request.contextPath}/"><%= escapeAttr(forgotHotelName) %></a>
                <p class="auth-subtitle">Hệ thống quản lý khách sạn</p>
            </div>

            <div class="auth-icon-badge" aria-hidden="true">
                <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
            </div>

            <h1 id="auth-title">Quên mật khẩu?</h1>
            <p class="auth-form-note">Nhập địa chỉ email liên kết với tài khoản của bạn để nhận liên kết đặt lại mật khẩu. Liên kết có hiệu lực trong 15 phút.</p>

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
            <% if (request.getAttribute("success") != null) { %>
                <div class="auth-message success" role="status">
                    <svg class="auth-message-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                    <span><%= escapeAttr(request.getAttribute("success")) %></span>
                </div>
            <% } %>

            <form class="auth-form" method="post" action="${pageContext.request.contextPath}/forgot-password">
                <div class="auth-field">
                    <label for="email">Email</label>
                    <div class="auth-input-wrapper">
                        <svg class="auth-input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                            <polyline points="22,6 12,13 2,6"></polyline>
                        </svg>
                        <input id="email" name="email" type="email" autocomplete="email" placeholder="example@email.com" required autofocus>
                    </div>
                </div>
                <button class="auth-submit" type="submit" data-loading-label="Đang gửi liên kết...">
                    <span>Gửi liên kết đặt lại</span>
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
