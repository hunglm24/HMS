<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; min-height: 100vh; background: #f3f6fb; color: #172033; }
        .auth-content { min-height: calc(100vh - 71px); display: grid; place-items: center; padding: 24px; }
        .card { width: min(100%, 460px); background: #fff; border-radius: 16px; padding: 32px;
            box-shadow: 0 16px 45px rgba(20, 38, 70, .12); }
        h1 { margin: 0 0 8px; font-size: 28px; }
        .subtitle { margin: 0 0 22px; color: #687386; }
        label { display: block; margin: 14px 0 7px; font-weight: 600; }
        input { width: 100%; padding: 12px 14px; border: 1px solid #ccd3df; border-radius: 9px; font-size: 16px; }
        input:focus { outline: 3px solid #dce8ff; border-color: #316bd6; }
        button { width: 100%; margin-top: 24px; padding: 13px; border: 0; border-radius: 9px;
            background: #245ec7; color: #fff; font-size: 16px; font-weight: 700; cursor: pointer; }
        button:hover { background: #194da9; }
        .form-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 24px; }
        .form-actions button { margin: 0; }
        .reset-button { background: #eef2f7; color: #344054; }
        .reset-button:hover { background: #dfe6ef; }
        .error { padding: 11px 13px; margin-bottom: 16px; border-radius: 8px; background: #fff0f0; color: #a51d27; }
        .link { text-align: center; margin: 18px 0 0; }
        .link a { color: #245ec7; text-decoration: none; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="auth-content">
<main class="card">
    <h1>Tạo tài khoản</h1>
    <p class="subtitle">Đăng ký tài khoản khách hàng HMS.</p>

    <% if (request.getAttribute("error") != null) { %>
        <div class="error" role="alert"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/register">
        <label for="fullName">Họ và tên</label>
        <input id="fullName" name="fullName" type="text" maxlength="100" autocomplete="name" required autofocus>

        <label for="email">Email</label>
        <input id="email" name="email" type="email" maxlength="150" autocomplete="email" required>

        <label for="phone">Số điện thoại (không bắt buộc)</label>
        <input id="phone" name="phone" type="tel" maxlength="20" autocomplete="tel">

        <label for="password">Mật khẩu</label>
        <input id="password" name="password" type="password" minlength="8" autocomplete="new-password" required>

        <label for="confirmPassword">Xác nhận mật khẩu</label>
        <input id="confirmPassword" name="confirmPassword" type="password" minlength="8" autocomplete="new-password" required>

        <div class="form-actions">
            <button class="reset-button" type="reset">Nhập lại</button>
            <button type="submit">Đăng ký</button>
        </div>
    </form>
    <p class="link">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
</main>
</div>
</body>
</html>
