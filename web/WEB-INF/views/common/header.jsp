<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String roleLabel(String roleName) {
        if (roleName == null) return "Khách hàng";
        switch (roleName.toUpperCase(java.util.Locale.ROOT)) {
            case "ADMIN": return "Quản trị hệ thống";
            case "CUSTOMER": return "Khách hàng";
            case "RECEPTIONIST": return "Lễ tân";
            case "HOUSEKEEPING": return "Buồng phòng";
            case "HOTEL_MANAGER": return "Quản lý khách sạn";
            default: return roleName;
        }
    }
%>
<%
    User headerUser = (User) session.getAttribute("currentUser");
    String contextPath = request.getContextPath();
    String roleName = headerUser == null ? "" : headerUser.getRoleName();
    boolean customer = "CUSTOMER".equalsIgnoreCase(roleName);
    boolean receptionist = "RECEPTIONIST".equalsIgnoreCase(roleName);
    boolean housekeeping = "HOUSEKEEPING".equalsIgnoreCase(roleName);
    boolean manager = "HOTEL_MANAGER".equalsIgnoreCase(roleName);
    boolean admin = "ADMIN".equalsIgnoreCase(roleName);
%>
<a class="skip-link" href="#main-content">Bỏ qua đến nội dung chính</a>
<header class="site-header">
    <div class="header-container">
        <a class="brand" href="<%= contextPath %>/" aria-label="Trang chủ HMS">
            <span class="brand-mark" aria-hidden="true">H</span>
            <span class="brand-copy"><strong>HMS</strong><small>Hotel Management System</small></span>
        </a>

        <button class="nav-toggle" type="button" aria-expanded="false"
                aria-controls="main-navigation" aria-label="Mở menu">☰</button>

        <nav id="main-navigation" class="main-nav" aria-label="Điều hướng chính">
            <a href="<%= contextPath %>/">Trang chủ</a>

            <% if (headerUser == null || customer) { %>
                <a href="<%= contextPath %>/search">Tìm phòng</a>
                <% if (headerUser != null) { %>
                    <a href="<%= contextPath %>/my-bookings">Đặt phòng của tôi</a>
                <% } %>
            <% } %>

            <% if (receptionist) { %>
                <div class="nav-dropdown">
                    <button type="button" class="dropdown-toggle">Đặt phòng <span>▾</span></button>
                    <div class="dropdown-menu">
                        <a href="<%= contextPath %>/reception/bookings">Danh sách đặt phòng</a>
                        <a href="<%= contextPath %>/reception/walk-in">Đặt phòng tại quầy</a>
                        <a href="<%= contextPath %>/reception/room-map">Sơ đồ phòng</a>
                    </div>
                </div>
                <a href="<%= contextPath %>/reception/check-in">Nhận phòng</a>
                <a href="<%= contextPath %>/reception/check-out">Trả phòng</a>
            <% } %>

            <% if (housekeeping) { %>
                <a href="<%= contextPath %>/housekeeping/tasks">Công việc buồng phòng</a>
                <a href="<%= contextPath %>/housekeeping/issues">Báo cáo sự cố</a>
            <% } %>

            <% if (manager) { %>
                <div class="nav-dropdown">
                    <button type="button" class="dropdown-toggle">Vận hành <span>▾</span></button>
                    <div class="dropdown-menu">
                        <a href="<%= contextPath %>/manager/room-types">Phòng và loại phòng</a>
                        <a href="<%= contextPath %>/manager/pricing">Giá và dịch vụ</a>
                        <a href="<%= contextPath %>/manager/staff">Quản lý nhân viên</a>
                    </div>
                </div>
                <a href="<%= contextPath %>/manager/reports">Báo cáo thống kê</a>
            <% } %>

            <% if (admin) { %>
                <div class="nav-dropdown">
                    <button type="button" class="dropdown-toggle">Quản trị <span>▾</span></button>
                    <div class="dropdown-menu">
                        <a href="<%= contextPath %>/admin/users">Người dùng</a>
                        <a href="<%= contextPath %>/admin/roles">Vai trò và quyền</a>
                        <a href="<%= contextPath %>/admin/system-config">Cấu hình hệ thống</a>
                        <a href="<%= contextPath %>/admin/backup">Sao lưu</a>
                    </div>
                </div>
                <a href="<%= contextPath %>/admin/logs">Nhật ký hệ thống</a>
            <% } %>

            <% if (headerUser == null) { %>
                <a href="<%= contextPath %>/login">Đăng nhập</a>
                <a class="nav-primary" href="<%= contextPath %>/register">Đăng ký</a>
            <% } else { %>
                <a class="user-chip" href="<%= contextPath %>/profile" title="Xem hồ sơ">
                    <span class="user-avatar" aria-hidden="true">●</span>
                    <span><strong><%= escapeHtml(headerUser.getFullName()) %></strong>
                        <small><%= roleLabel(headerUser.getRoleName()) %></small></span>
                </a>
                <a href="<%= contextPath %>/change-password">Đổi mật khẩu</a>
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
        header.querySelectorAll('.dropdown-toggle').forEach(button => {
            button.addEventListener('click', event => {
                event.stopPropagation();
                button.parentElement.classList.toggle('is-open');
            });
        });
        document.addEventListener('click', () => {
            header.querySelectorAll('.nav-dropdown.is-open').forEach(item => item.classList.remove('is-open'));
        });
    })();
</script>
