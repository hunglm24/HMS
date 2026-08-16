<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-page">
    <section class="auth-card" aria-labelledby="auth-title">
        <nav class="auth-tabs" aria-label="Tai khoan">
            <a class="auth-tab" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            <span class="auth-tab active" aria-current="page">Đăng ký</span>
        </nav>
        <h1 id="auth-title">Tạo tài khoản</h1>
        <p class="auth-subtitle">Đăng ký để theo dõi và quản lý booking của bạn.</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="auth-message error" role="alert"><%= h(request.getAttribute("error")) %></div>
        <% } %>

        <form class="auth-form" method="post" action="${pageContext.request.contextPath}/register">
            <div class="auth-field">
                <label for="fullName">Họ và tên</label>
                <input id="fullName" name="fullName" maxlength="100" autocomplete="name"
                       value="<%= h(request.getAttribute("fullName")) %>" required>
            </div>
            <div class="auth-field">
                <label for="email">Email</label>
                <input id="email" name="email" type="email" maxlength="150" autocomplete="email"
                       value="<%= h(request.getAttribute("email")) %>" required>
            </div>
            <div class="auth-field">
                <label for="phone">Số điện thoại</label>
                <input id="phone" name="phone" type="tel" maxlength="20" autocomplete="tel"
                       value="<%= h(request.getAttribute("phone")) %>">
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
                <input id="confirmPassword" name="confirmPassword" type="password" minlength="8" maxlength="255"
                       autocomplete="new-password" required>
            </div>
            <button class="auth-submit" type="submit" data-loading-label="Đang tạo tài khoản...">Đăng ký</button>
        </form>
        <a class="auth-guest" href="${pageContext.request.contextPath}/search">Tiếp tục với tư cách khách</a>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
