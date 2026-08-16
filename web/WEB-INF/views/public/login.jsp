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
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đăng nhập | HMS</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260816-4"></head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-split-page login-page">
    <section class="auth-hero" aria-label="Giới thiệu HMS">
        <div class="auth-hero-content">
            <a class="auth-hero-logo" href="${pageContext.request.contextPath}/">HMS</a>
            <p class="auth-hero-tagline">Hotel Management System</p>
            <div class="auth-preview-card auth-preview-main">
                <span class="auth-preview-title">Quản lý khách sạn</span>
                <div class="auth-preview-stats">
                    <span><strong>24</strong>Phòng</span>
                    <span><strong>18</strong>Đặt phòng</span>
                    <span><strong>96%</strong>Hài lòng</span>
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
        <div class="auth-card auth-card-login">
    <a class="auth-brand" href="${pageContext.request.contextPath}/">HMS</a>
    <p class="auth-subtitle">Hệ thống quản lý khách sạn</p>
    <h1 id="auth-title">Đăng nhập</h1>
    <% if (request.getAttribute("error") != null) { %><div class="auth-message error" role="alert"><%= request.getAttribute("error") %></div><% } %>
    <% if (request.getParameter("logout") != null) { %><div class="auth-message success" role="status">Bạn đã đăng xuất thành công.</div><% } %>
    <% if (request.getParameter("reset") != null) { %><div class="auth-message success" role="status">Đặt lại mật khẩu thành công.</div><% } %>
    <% if (request.getParameter("oauthError") != null) { %><div class="auth-message error" role="alert">Không thể đăng nhập bằng Google. Vui lòng thử lại.</div><% } %>
    <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
        <div class="auth-field"><label for="email">Email</label><input id="email" name="email" type="email" maxlength="150" autocomplete="username" value="<%= escapeAttr(request.getAttribute("email")) %>" required autofocus></div>
        <div class="auth-field"><label for="password">Mật khẩu</label><div class="password-wrap"><input id="password" name="password" type="password" minlength="8" autocomplete="current-password" required><button class="password-visibility" type="button" data-toggle-password="password" aria-label="Hiện mật khẩu">Hiện</button></div></div>
        <div class="auth-meta"><a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a></div>
        <button class="auth-submit" type="submit" data-loading-label="Đang đăng nhập...">Đăng nhập</button>
    </form>
    <div class="auth-divider"><span>hoặc</span></div>
    <a class="google-button" href="${pageContext.request.contextPath}/auth/google" aria-label="Đăng nhập bằng Google"><span class="google-mark" aria-hidden="true">G</span>Đăng nhập bằng Google</a>
    <p class="auth-switch">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
    <a class="auth-guest" href="${pageContext.request.contextPath}/search">Tiếp tục với tư cách khách</a>
        </div>
    </section>
</main><script src="${pageContext.request.contextPath}/assets/js/auth.js"></script></body></html>
