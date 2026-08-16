<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đăng nhập | HMS</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-page"><section class="auth-card" aria-labelledby="auth-title">
    <nav class="auth-tabs" aria-label="Tài khoản">
        <a class="auth-tab active" href="${pageContext.request.contextPath}/login" aria-current="page">Đăng nhập</a>
        <a class="auth-tab" href="${pageContext.request.contextPath}/register">Đăng ký</a>
    </nav>
    <h1 id="auth-title">Chào mừng trở lại</h1>
    <p class="auth-subtitle">Đăng nhập để quản lý các đặt phòng của bạn.</p>
    <% if (request.getAttribute("error") != null) { %><div class="auth-message error" role="alert"><%= request.getAttribute("error") %></div><% } %>
    <% if (request.getParameter("reset") != null) { %><div class="auth-message success" role="status">Đặt lại mật khẩu thành công.</div><% } %>
    <% if (request.getParameter("oauthError") != null) { %><div class="auth-message error" role="alert">Không thể đăng nhập bằng Google. Vui lòng thử lại.</div><% } %>
    <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
        <div class="auth-field"><label for="email">Email</label><input id="email" name="email" type="email" maxlength="150" autocomplete="username" required></div>
        <div class="auth-field"><label for="password">Mật khẩu</label><div class="password-wrap"><input id="password" name="password" type="password" minlength="8" autocomplete="current-password" required><button class="password-visibility" type="button" data-toggle-password="password" aria-label="Hiện mật khẩu">Hiện</button></div></div>
        <div class="auth-meta"><a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a></div>
        <button class="auth-submit" type="submit" data-loading-label="Đang đăng nhập...">Đăng nhập</button>
    </form>
    <div class="auth-divider"><span>hoặc</span></div>
    <a class="google-button" href="${pageContext.request.contextPath}/auth/google" aria-label="Đăng nhập bằng Google"><span class="google-mark" aria-hidden="true">G</span>Đăng nhập bằng Google</a>
    <a class="auth-guest" href="${pageContext.request.contextPath}/search">Tiếp tục với tư cách khách</a>
</section></main><script src="${pageContext.request.contextPath}/assets/js/auth.js"></script></body></html>
