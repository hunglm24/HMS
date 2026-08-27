<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
            case "HOUSEKEEPING": return "Nhân viên vệ sinh";
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
    String brandHref;
    if (internal) {
        brandHref = contextPath + "/dashboard";
    } else {
        brandHref = contextPath + "/";
    }
    String currentPath = request.getServletPath();
    String fullName = escapeHtml(beanString(headerUser, "getFullName"));
    String avatar = fullName.isEmpty() ? "U" : fullName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    model.HotelConfig headerConfig = (model.HotelConfig) application.getAttribute("hotelConfig");
    String hotelName = headerConfig != null && headerConfig.getHotelName() != null && !headerConfig.getHotelName().isBlank()
            ? headerConfig.getHotelName()
            : "HMS Hotel";
    String hotelTagline = "Quản lý khách sạn";
%>
<header class="site-header <%= internal ? "site-header--internal" : "" %>">
    <div class="header-container">
        <a class="brand" href="<%= brandHref %>" aria-label="<%= escapeHtml(hotelName) %> home">
            <span class="brand-mark" aria-hidden="true"><%= escapeHtml(hotelName.substring(0, 1).toUpperCase(java.util.Locale.ROOT)) %></span>
            <span class="brand-copy"><strong><%= escapeHtml(hotelName) %></strong><small><%= escapeHtml(hotelTagline) %></small></span>
        </a>

        <button class="nav-toggle" type="button" aria-expanded="false"
                aria-controls="main-navigation" aria-label="Mở menu">☰</button>

        <nav id="main-navigation" class="main-nav" aria-label="Điều hướng chính">
            <% if (!signedIn || customer) { %>
                <a href="<%= contextPath %>/search">Tìm phòng</a>
                <a href="<%= contextPath %>/news">Tin tức</a>
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

                <a href="<%= contextPath %>/manager/rooms">Phòng</a>
                <a href="<%= contextPath %>/manager/room-types">Loại phòng</a>
                <a href="<%= contextPath %>/manager/housekeeping">Lịch sử dọn phòng</a>
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
<div class="toast-container" id="toastContainer" style="position: fixed; bottom: 24px; right: 24px; display: flex; flex-direction: column; gap: 12px; z-index: 99999; max-width: 380px;">
    <c:set var="reqSuccess" value="${not empty requestScope.message ? requestScope.message : (not empty requestScope.successMessage ? requestScope.successMessage : (not empty requestScope.toastMessage and requestScope.toastType ne 'error' and requestScope.toastType ne 'toast-error' ? requestScope.toastMessage : ''))}" />
    <c:set var="unifiedSuccessMsg" value="${not empty sessionScope.message ? sessionScope.message : (not empty sessionScope.successMessage ? sessionScope.successMessage : (not empty sessionScope.toastMessage and sessionScope.toastType ne 'error' and sessionScope.toastType ne 'toast-error' ? sessionScope.toastMessage : (not empty reqSuccess ? reqSuccess : '')))}" />

    <c:set var="reqError" value="${not empty requestScope.error ? requestScope.error : (not empty requestScope.errorMessage ? requestScope.errorMessage : (not empty requestScope.toastMessage and (requestScope.toastType eq 'error' or requestScope.toastType eq 'toast-error') ? requestScope.toastMessage : ''))}" />
    <c:if test="${empty reqError and not empty requestScope.errors}">
        <c:forEach items="${requestScope.errors}" var="err" varStatus="loop">
            <c:if test="${loop.first}">
                <c:set var="reqError" value="${err.value}" />
            </c:if>
        </c:forEach>
    </c:if>
    <c:set var="unifiedErrorMsg" value="${not empty sessionScope.error ? sessionScope.error : (not empty sessionScope.errorMessage ? sessionScope.errorMessage : (not empty sessionScope.toastMessage and (sessionScope.toastType eq 'error' or sessionScope.toastType eq 'toast-error') ? sessionScope.toastMessage : (not empty reqError ? reqError : '')))}" />

    <c:if test="${not empty unifiedSuccessMsg}">
        <div class="toast toast-success" style="display: flex; align-items: center; gap: 12px; background: #10b981; color: #ffffff; padding: 14px 18px; border-radius: 10px; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.2), 0 8px 10px -6px rgba(0, 0, 0, 0.1); font-size: 14px; font-weight: 600; cursor: pointer; animation: slideInToast 0.3s ease-out;" onclick="this.remove();">
            <span style="font-size: 18px; line-height: 1;">✓</span>
            <span><c:out value="${unifiedSuccessMsg}" /></span>
        </div>
        <c:remove var="message" scope="session" />
        <c:remove var="successMessage" scope="session" />
        <c:remove var="toastMessage" scope="session" />
        <c:remove var="toastType" scope="session" />
    </c:if>

    <c:if test="${not empty unifiedErrorMsg}">
        <div class="toast toast-error" style="display: flex; align-items: center; gap: 12px; background: #ef4444; color: #ffffff; padding: 14px 18px; border-radius: 10px; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.2), 0 8px 10px -6px rgba(0, 0, 0, 0.1); font-size: 14px; font-weight: 600; cursor: pointer; animation: slideInToast 0.3s ease-out;" onclick="this.remove();">
            <span style="font-size: 18px; line-height: 1;">⚠</span>
            <span><c:out value="${unifiedErrorMsg}" /></span>
        </div>
        <c:remove var="error" scope="session" />
        <c:remove var="errorMessage" scope="session" />
        <c:remove var="toastMessage" scope="session" />
        <c:remove var="toastType" scope="session" />
    </c:if>
</div>
<style>
@keyframes slideInToast {
    from { opacity: 0; transform: translateY(20px) scale(0.95); }
    to { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
<script>
    setTimeout(function() {
        document.querySelectorAll('#toastContainer .toast').forEach(function(el) {
            el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            el.style.opacity = '0';
            el.style.transform = 'translateY(10px)';
            setTimeout(function() { el.remove(); }, 500);
        });
    }, 4000);
</script>
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
