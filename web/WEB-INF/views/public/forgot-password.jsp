<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Quên mật khẩu | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-center-page forgot-password-page">
    <section class="auth-card auth-card-center" aria-labelledby="auth-title">
        <a class="auth-brand" href="${pageContext.request.contextPath}/">HMS</a>
        <p class="auth-subtitle">Hệ thống quản lý khách sạn</p>
        <h1 id="auth-title">Quên mật khẩu</h1>
        <p class="auth-form-note">Nhập email để nhận liên kết đặt lại mật khẩu. Liên kết có hiệu lực trong 15 phút.</p>

        <% if(request.getAttribute("error") != null){ %>
            <div class="auth-message error" role="alert"><%=request.getAttribute("error")%></div>
        <% } %>
        <% if(request.getAttribute("success") != null){ %>
            <div class="auth-message success" role="status"><%=request.getAttribute("success")%></div>
        <% } %>

        <form class="auth-form" method="post" action="${pageContext.request.contextPath}/forgot-password">
            <div class="auth-field">
                <label for="email">Email</label>
                <input id="email" name="email" type="email" autocomplete="email" required autofocus>
            </div>
            <button class="auth-submit" type="submit" data-loading-label="Đang gửi...">Gửi liên kết</button>
        </form>
        <a class="auth-guest" href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
