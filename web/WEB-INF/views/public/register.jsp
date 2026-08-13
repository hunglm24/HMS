<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
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
