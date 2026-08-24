<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<<<<<<< Updated upstream
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đặt lại mật khẩu | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css"></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp"/><main id="main-content" class="auth-page"><section class="auth-card"><h1>Đặt mật khẩu mới</h1><p class="auth-subtitle">Mật khẩu phải có ít nhất 8 ký tự.</p>
<% if(request.getAttribute("error") != null){ %><div class="auth-message error" role="alert"><%=request.getAttribute("error")%></div><% } %>
<form class="auth-form" method="post" action="${pageContext.request.contextPath}/reset-password"><input type="hidden" name="token" value="${token}"><div class="auth-field"><label for="password">Mật khẩu mới</label><input id="password" name="password" type="password" minlength="8" required></div><div class="auth-field"><label for="confirmPassword">Xác nhận mật khẩu</label><input id="confirmPassword" name="confirmPassword" type="password" minlength="8" required></div><button class="auth-submit" type="submit" data-loading-label="Đang cập nhật...">Đặt lại mật khẩu</button></form></section></main><script src="${pageContext.request.contextPath}/assets/js/auth.js"></script></body></html>
=======
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Đặt lại mật khẩu | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260821-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-center-page reset-password-page">
    <section class="auth-card auth-card-center reset-password-card" aria-labelledby="auth-title">
        <a class="auth-brand" href="${pageContext.request.contextPath}/" aria-label="Về trang chủ HMS">HMS</a>
        <p class="auth-subtitle">Hệ thống quản lý khách sạn</p>
        <div class="reset-password-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false"><path d="M7 10V8a5 5 0 0 1 10 0v2m-11 0h12a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2Zm6 4v3" /></svg>
        </div>
        <h1 id="auth-title">Đặt mật khẩu mới</h1>
        <p class="auth-form-note">Tạo mật khẩu mới để tiếp tục sử dụng tài khoản HMS của bạn.</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="auth-message error" role="alert"><%=request.getAttribute("error")%></div>
        <% } %>

        <form class="auth-form reset-password-form" method="post" action="${pageContext.request.contextPath}/reset-password">
            <input type="hidden" name="token" value="${token}">
            <div class="auth-field">
                <label for="password">Mật khẩu mới</label>
                <div class="password-wrap">
                    <input id="password" name="password" type="password" minlength="8" autocomplete="new-password" placeholder="Nhập ít nhất 8 ký tự" required autofocus>
                    <button class="password-visibility" type="button" data-toggle-password="password" aria-label="Hiện mật khẩu">Hiện</button>
                </div>
            </div>
            <div class="auth-field">
                <label for="confirmPassword">Xác nhận mật khẩu</label>
                <div class="password-wrap">
                    <input id="confirmPassword" name="confirmPassword" type="password" minlength="8" autocomplete="new-password" placeholder="Nhập lại mật khẩu mới" required>
                    <button class="password-visibility" type="button" data-toggle-password="confirmPassword" aria-label="Hiện mật khẩu">Hiện</button>
                </div>
            </div>
            <div class="password-requirement"><span aria-hidden="true">✓</span>Mật khẩu phải có ít nhất 8 ký tự</div>
            <button class="auth-submit" type="submit" data-loading-label="Đang cập nhật...">Đặt lại mật khẩu</button>
        </form>
        <a class="auth-guest reset-back-link" href="${pageContext.request.contextPath}/login"><span aria-hidden="true">←</span> Quay lại đăng nhập</a>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
>>>>>>> Stashed changes
