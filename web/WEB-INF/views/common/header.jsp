<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    User headerUser = (User) session.getAttribute("currentUser");
    String contextPath = request.getContextPath();
%>
<header class="site-header">
    <div class="header-container">
        <a class="brand" href="<%= contextPath %>/" aria-label="Trang chủ HMS">
            <span class="brand-mark" aria-hidden="true">H</span>
            <span class="brand-copy">
                <strong>HMS</strong>
                <small>Hotel Management</small>
            </span>
        </a>

        <button class="nav-toggle" type="button" aria-expanded="false"
                aria-controls="main-navigation" aria-label="Mở menu">☰</button>

        <nav id="main-navigation" class="main-nav" aria-label="Điều hướng chính">
            <a href="<%= contextPath %>/">Trang chủ</a>
            <% if (headerUser == null) { %>
                <a href="<%= contextPath %>/login">Đăng nhập</a>
                <a class="nav-primary" href="<%= contextPath %>/register">Đăng ký</a>
            <% } else { %>
                <% if (headerUser.getRoleId() == 2 || headerUser.getRoleId() == 4) { %>
                    <a href="<%= contextPath %>/housekeeping/tasks">Housekeeping</a>
                <% } %>
                <span class="user-chip" title="Tài khoản đang đăng nhập">
                    <span class="user-avatar" aria-hidden="true">●</span>
                    <span>
                        <strong><%= escapeHtml(headerUser.getFullName()) %></strong>
                        <small><%= headerUser.getRoleId() == 0 ? "Khách hàng" : "Nhân viên" %></small>
                    </span>
                </span>
                <form class="logout-form" method="post" action="<%= contextPath %>/logout">
                    <button type="submit">Đăng xuất</button>
                </form>
            <% } %>
        </nav>
    </div>
</header>
<script>
    (() => {
        const header = document.currentScript.previousElementSibling;
        const toggle = header.querySelector('.nav-toggle');
        const nav = header.querySelector('.main-nav');
        toggle.addEventListener('click', () => {
            const open = nav.classList.toggle('is-open');
            toggle.setAttribute('aria-expanded', String(open));
            toggle.setAttribute('aria-label', open ? 'Đóng menu' : 'Mở menu');
        });
    })();
</script>
