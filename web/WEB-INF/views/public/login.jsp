<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; min-height: 100vh; background: #f3f6fb; color: #172033; }
        .auth-content { min-height: calc(100vh - 71px); display: grid; place-items: center; padding: 24px; }
        .card { width: min(100%, 420px); background: #fff; border-radius: 16px; padding: 32px;
            box-shadow: 0 16px 45px rgba(20, 38, 70, .12); }
        h1 { margin: 0 0 8px; font-size: 28px; }
        .subtitle { margin: 0 0 24px; color: #687386; }
        label { display: block; margin: 16px 0 7px; font-weight: 600; }
        input { width: 100%; padding: 12px 14px; border: 1px solid #ccd3df; border-radius: 9px; font-size: 16px; }
        input:focus { outline: 3px solid #dce8ff; border-color: #316bd6; }
        .password-field { position: relative; }
        .password-field input { padding-right: 76px; }
        .password-toggle { position: absolute; right: 7px; top: 50%; transform: translateY(-50%); width: auto;
            margin: 0; padding: 7px 9px; background: transparent; color: #245ec7; font-size: 13px; }
        .password-toggle:hover { background: #edf4ff; }
        button { width: 100%; margin-top: 24px; padding: 13px; border: 0; border-radius: 9px;
            background: #245ec7; color: #fff; font-size: 16px; font-weight: 700; cursor: pointer; }
        button:hover { background: #194da9; }
        .secondary-action { display: block; margin-top: 10px; padding: 11px; border: 1px solid #ccd3df;
            border-radius: 9px; color: #245ec7; font-weight: 700; text-align: center; text-decoration: none; }
        .secondary-action:hover { background: #edf4ff; }
        .message { padding: 11px 13px; margin-bottom: 16px; border-radius: 8px; }
        .error { background: #fff0f0; color: #a51d27; }
        .success { background: #eaf8ef; color: #176438; }
    </style>
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
