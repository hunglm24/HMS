<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hồ sơ cá nhân | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="page-container">
    <div class="profile-layout">
        <aside class="profile-summary" aria-label="Thông tin tài khoản">
            <div class="profile-avatar" aria-hidden="true">${sessionScope.currentUser.fullName.substring(0,1)}</div>
            <div><strong>${sessionScope.currentUser.fullName}</strong><span>${sessionScope.currentUser.email}</span></div>
        </aside>

        <section class="profile-card" aria-labelledby="profile-title">
            <h1 id="profile-title">Hồ sơ cá nhân</h1>
            <p class="profile-description">Cập nhật thông tin liên hệ của tài khoản HMS.</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error profile-message" role="alert"><%= request.getAttribute("error") %></div>
            <% } %>
            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success profile-message" role="status"><%= request.getAttribute("success") %></div>
            <% } %>

            <form method="post" action="${pageContext.request.contextPath}/profile">
                <div class="profile-field">
                    <label class="form-label" for="profileEmail">Email</label>
                    <input class="form-control" id="profileEmail" type="email" value="${sessionScope.currentUser.email}" readonly aria-describedby="emailHelper">
                    <p class="profile-helper" id="emailHelper">Email đăng nhập không thể thay đổi tại đây.</p>
                </div>
                <div class="profile-field">
                    <label class="form-label" for="fullName">Họ và tên</label>
                    <input class="form-control" id="fullName" name="fullName" value="${sessionScope.currentUser.fullName}" maxlength="100" autocomplete="name" required>
                </div>
                <div class="profile-field">
                    <label class="form-label" for="phone">Số điện thoại</label>
                    <input class="form-control" id="phone" name="phone" type="tel" value="${sessionScope.currentUser.phone}" maxlength="20" autocomplete="tel">
                </div>
                <div class="profile-actions">
                    <a class="button button-secondary" href="${pageContext.request.contextPath}/change-password">Đổi mật khẩu</a>
                    <button class="button button-primary" type="submit">Lưu thay đổi</button>
                </div>
            </form>
        </section>
    </div>
</main>
</body>
</html>
