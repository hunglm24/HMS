<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    boolean googleConfigured = System.getenv("HMS_GOOGLE_CLIENT_ID") != null
            && !System.getenv("HMS_GOOGLE_CLIENT_ID").trim().isEmpty()
            && System.getenv("HMS_GOOGLE_CLIENT_SECRET") != null
            && !System.getenv("HMS_GOOGLE_CLIENT_SECRET").trim().isEmpty();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="auth-page">
    <section class="auth-card" aria-labelledby="auth-title">
        <nav class="auth-tabs" aria-label="Tai khoan">
            <span class="auth-tab active" aria-current="page">Đăng nhập</span>
            <a class="auth-tab" href="${pageContext.request.contextPath}/register">Đăng ký</a>
        </nav>
        <h1 id="auth-title">Chào mừng trở lại</h1>
        <p class="auth-subtitle">Đăng nhập để quản lý booking và nghiệp vụ khách sạn.</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="auth-message error" role="alert"><%= h(request.getAttribute("error")) %></div>
        <% } %>
        <% if (request.getParameter("reset") != null) { %>
            <div class="auth-message success" role="status">Đặt lại mật khẩu thành công.</div>
        <% } %>
        <% if (request.getParameter("oauthError") != null) { %>
            <div class="auth-message error" role="alert">Không thể đăng nhập bằng Google. Vui lòng thử lại.</div>
        <% } %>
        <% if (request.getParameter("googleConfig") != null) { %>
            <div class="auth-message error" role="alert">Google Sign-In chưa được cấu hình. Vui lòng thiết lập HMS_GOOGLE_CLIENT_ID và HMS_GOOGLE_CLIENT_SECRET trong cấu hình chạy Tomcat.</div>
        <% } %>

        <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
            <div class="auth-field">
                <label for="email">Email</label>
                <input id="email" name="email" type="email" maxlength="150" autocomplete="username"
                       value="<%= h(request.getAttribute("email")) %>" required>
            </div>
            <div class="auth-field">
                <label for="password">Mật khẩu</label>
                <div class="password-wrap">
                    <input id="password" name="password" type="password" minlength="8" autocomplete="current-password" required>
                    <button class="password-visibility" type="button" data-toggle-password="password" aria-label="Hiện mật khẩu">Hiện</button>
                </div>
            </div>
            <div class="auth-meta"><a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a></div>
            <button class="auth-submit" type="submit" data-loading-label="Đang đăng nhập...">Đăng nhập</button>
        </form>

        <div class="auth-divider"><span>hoặc</span></div>
        <a class="google-button <%= googleConfigured ? "" : "is-disabled" %>"
           href="<%= googleConfigured ? request.getContextPath() + "/auth/google" : "#" %>"
           aria-label="Đăng nhập bằng Google"
           aria-disabled="<%= googleConfigured ? "false" : "true" %>"
           title="<%= googleConfigured ? "Đăng nhập bằng Google" : "Google Sign-In chưa được cấu hình" %>">
            <span class="google-mark" aria-hidden="true">G</span>Đăng nhập bằng Google
        </a>
        <a class="auth-guest" href="${pageContext.request.contextPath}/search">Tiếp tục với tư cách khách</a>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
