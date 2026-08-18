<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String beanString(Object bean, String getterName) {
        if (bean == null) return "";
        try {
            Object value = bean.getClass().getMethod(getterName).invoke(bean);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String roleLabel(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) return "Khách hàng";
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
    Object headerUser = session.getAttribute("currentUser");
    String contextPath = request.getContextPath();
    String roleName = beanString(headerUser, "getRoleName");
    boolean signedIn = headerUser != null;
    boolean hideSidebar = Boolean.TRUE.equals(request.getAttribute("hideSidebar"));
    boolean customer = "CUSTOMER".equalsIgnoreCase(roleName);
    boolean housekeeping = "HOUSEKEEPING".equalsIgnoreCase(roleName);
    boolean manager = "HOTEL_MANAGER".equalsIgnoreCase(roleName);
    boolean internal = signedIn && !customer && !hideSidebar;
    String currentPath = request.getServletPath();
    String fullName = escapeHtml(beanString(headerUser, "getFullName"));
    String avatar = fullName.isEmpty() ? "U" : fullName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
%>
<header class="site-header <%= internal ? "site-header--internal" : "" %>">
    <div class="header-container">
        <a class="brand" href="<%= contextPath %>/" aria-label="HMS home">
            <span class="brand-mark" aria-hidden="true">H</span>
            <span class="brand-copy"><strong>HMS</strong><small>Quản lý khách sạn</small></span>
        </a>

        <button class="nav-toggle" type="button" aria-expanded="false"
                aria-controls="main-navigation" aria-label="Mở menu">☰</button>

        <nav id="main-navigation" class="main-nav" aria-label="Điều hướng chính">
            <% if (!signedIn || customer) { %>
                <a href="<%= contextPath %>/search">Tìm phòng</a>
                <% if (signedIn) { %>
                    <a href="<%= contextPath %>/my-bookings">Đặt phòng của tôi</a>
                    <%
                        java.util.List cartItems = (java.util.List) session.getAttribute("cart");
                        int cartCount = cartItems != null ? cartItems.size() : 0;
                    %>
                    <a href="<%= contextPath %>/cart">Giỏ phòng<% if (cartCount > 0) { %><span style="background: #dc3545; color: white; border-radius: 10px; padding: 2px 6px; font-size: 0.75rem; margin-left: 4px; font-weight: bold;">🛒 <%= cartCount %></span><% } %></a>
                <% } %>
            <% } %>

            <% if (manager) { %>
                <a href="<%= contextPath %>/manager/reports">Báo cáo</a>
                <a href="<%= contextPath %>/manager/rooms">Phòng &amp; Loại phòng</a>
                <a href="<%= contextPath %>/housekeeping/tasks?view=history">Nhiệm vụ dọn phòng</a>
            <% } %>

            <% if (!signedIn) { %>
                <% if ("/login".equals(currentPath)) { %>
                    <span class="nav-current">Đăng nhập</span>
                <% } else { %>
                    <a href="<%= contextPath %>/login">Đăng nhập</a>
                <% } %>
                <% if ("/register".equals(currentPath)) { %>
                    <span class="nav-current nav-primary">Đăng ký</span>
                <% } else { %>
                    <a class="nav-primary" href="<%= contextPath %>/register">Đăng ký</a>
                <% } %>
            <% } else { %>
                <a class="user-chip" href="<%= contextPath %>/profile" title="Hồ sơ">
                    <span class="user-avatar" aria-hidden="true"><%= avatar %></span>
                    <span><strong><%= fullName %></strong><small><%= roleLabel(roleName) %></small></span>
                </a>
                <form class="logout-form" method="post" action="<%= contextPath %>/logout">
                    <button type="submit">Đăng xuất</button>
                </form>
            <% } %>
        </nav>
    </div>
</header>
<div class="toast-container" style="position: fixed; bottom: 20px; right: 20px; display: flex; flex-direction: column; gap: 10px; z-index: 9999;">
    <c:if test="${not empty sessionScope.message}">
        <div class="toast toast-success" style="background: #28a745; color: white; padding: 15px 20px; border-radius: 5px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
            ${sessionScope.message}
        </div>
        <script>setTimeout(function() { let el = document.querySelector('.toast-success'); if(el) el.style.display = 'none'; }, 5000);</script>
        <c:remove var="message" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="toast toast-error" style="background: #dc3545; color: white; padding: 15px 20px; border-radius: 5px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
            ${sessionScope.error}
        </div>
        <script>setTimeout(function() { let el = document.querySelector('.toast-error'); if(el) el.style.display = 'none'; }, 5000);</script>
        <c:remove var="error" scope="session" />
    </c:if>
</div>
<% if (internal) { %>
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />
<% } %>
<script>
    (() => {
        const header = document.querySelector('.site-header');
        if (!header) return;
        const toggle = header.querySelector('.nav-toggle');
        const nav = header.querySelector('.main-nav');
        if (!toggle || !nav) return;
        toggle.addEventListener('click', () => {
            const open = nav.classList.toggle('is-open');
            toggle.setAttribute('aria-expanded', String(open));
            toggle.setAttribute('aria-label', open ? 'Đóng menu' : 'Mở menu');
        });
    })();
</script>
<script src="<%= contextPath %>/assets/js/booking-validation.js"></script>
