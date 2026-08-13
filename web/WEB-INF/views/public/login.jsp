<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="auth-content">
<main class="card">
    <h1>Đăng nhập HMS</h1>
    <p class="subtitle">Nhập tài khoản của bạn để tiếp tục.</p>

    <% if (request.getAttribute("error") != null) { %>
        <div class="message error" role="alert"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if (request.getParameter("logout") != null) { %>
        <div class="message success" role="status">Bạn đã đăng xuất thành công.</div>
    <% } %>
    <% if (request.getParameter("reset") != null) { %>
        <div class="message success" role="status">Đặt lại mật khẩu thành công. Bạn có thể đăng nhập.</div>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" autocomplete="username" required autofocus>

        <label for="password">Mật khẩu</label>
        <div class="password-field">
            <input id="password" name="password" type="password" autocomplete="current-password" required>
            <button class="password-toggle" type="button" data-password-toggle="password">Hiện</button>
        </div>

        <button type="submit">Đăng nhập</button>
    </form>
    <a class="secondary-action" href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
    <a class="secondary-action" href="${pageContext.request.contextPath}/register">Tạo tài khoản mới</a>
</main>
</div>
<script>
    document.querySelectorAll('[data-password-toggle]').forEach(button => {
        button.addEventListener('click', () => {
            const input = document.getElementById(button.dataset.passwordToggle);
            const showing = input.type === 'text';
            input.type = showing ? 'password' : 'text';
            button.textContent = showing ? 'Hiện' : 'Ẩn';
        });
    });
</script>
</body>
</html>
