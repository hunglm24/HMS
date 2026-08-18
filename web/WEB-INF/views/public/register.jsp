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
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-split-page register-page">
    <section class="auth-hero" aria-label="Giới thiệu HMS">
        <div class="auth-hero-content">
            <a class="auth-hero-logo" href="${pageContext.request.contextPath}/">HMS</a>
            <p class="auth-hero-tagline">Hotel Management System</p>
            <div class="auth-preview-card auth-preview-main">
                <span class="auth-preview-title">Theo dõi đặt phòng</span>
                <div class="auth-preview-stats">
                    <span><strong>3</strong>Bước</span>
                    <span><strong>24/7</strong>Hỗ trợ</span>
                    <span><strong>1</strong>Tài khoản</span>
                </div>
            </div>
            <div class="auth-preview-card auth-preview-phone">
                <span class="auth-preview-dot"></span>
                <span class="auth-preview-line"></span>
                <span class="auth-preview-line short"></span>
                <span class="auth-preview-button"></span>
            </div>
        </div>
    </section>
    <section class="auth-panel" aria-labelledby="auth-title">
        <div class="auth-card auth-card-register">
        <a class="auth-brand" href="${pageContext.request.contextPath}/">HMS</a>
        <p class="auth-subtitle">Hệ thống quản lý khách sạn</p>
        <h1 id="auth-title">Đăng ký</h1>
        <p class="auth-form-note">Tạo tài khoản để theo dõi và quản lý đặt phòng.</p>
        <% if (request.getAttribute("error") != null) { %>
            <div class="auth-message error" role="alert"><%= escapeAttr(request.getAttribute("error")) %></div>
        <% } %>

        <form class="auth-form" method="post" action="${pageContext.request.contextPath}/register">
            <div class="auth-field">
                <label for="fullName">Họ và tên</label>
                <input id="fullName" name="fullName" maxlength="100" autocomplete="name" value="<%= escapeAttr(request.getAttribute("fullName")) %>" required autofocus>
            </div>
            <div class="auth-field">
                <label for="email">Email</label>
                <input id="email" name="email" type="email" maxlength="150" autocomplete="email" value="<%= escapeAttr(request.getAttribute("email")) %>" required>
            </div>
            <div class="auth-field">
                <label for="phone">Số điện thoại</label>
                <input id="phone" name="phone" type="tel" maxlength="20" autocomplete="tel" value="<%= escapeAttr(request.getAttribute("phone")) %>">
            </div>
            <div class="auth-field">
                <label for="password">Mật khẩu</label>
                <div class="password-wrap">
                    <input id="password" name="password" type="password" minlength="8" maxlength="255"
                           autocomplete="new-password" required>
                    <button class="password-visibility" type="button" data-toggle-password="password" aria-label="Hiện mật khẩu">Hiện</button>
                </div>
            </div>
            <div class="auth-field">
                <label for="confirmPassword">Xác nhận mật khẩu</label>
                <div class="password-wrap">
                    <input id="confirmPassword" name="confirmPassword" type="password" minlength="8" maxlength="255"
                           autocomplete="new-password" required>
                    <button class="password-visibility" type="button" data-toggle-password="confirmPassword"
                            aria-label="Hiện mật khẩu xác nhận">Hiện</button>
                </div>
            </div>
            <button class="auth-submit" type="submit" data-loading-label="Đang tạo tài khoản...">Đăng ký</button>
        </form>
        <p class="auth-switch">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
        <a class="auth-guest" href="${pageContext.request.contextPath}/search">Tiếp tục với tư cách khách</a>
        </div>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
