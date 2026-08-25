<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="service.HousekeepingService" %>
<%!
    private String sideBeanString(Object bean, String getterName) {
        if (bean == null) {
            return "";
        }
        try {
            Object value = bean.getClass().getMethod(getterName).invoke(bean);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }

    private boolean isPathActive(HttpServletRequest req, String... patterns) {
        String forwardUri = (String) req.getAttribute("jakarta.servlet.forward.request_uri");
        if (forwardUri == null) forwardUri = (String) req.getAttribute("javax.servlet.forward.request_uri");
        String forwardServletPath = (String) req.getAttribute("jakarta.servlet.forward.servlet_path");
        if (forwardServletPath == null) forwardServletPath = (String) req.getAttribute("javax.servlet.forward.servlet_path");
        String uri = req.getRequestURI();
        String servletPath = req.getServletPath();

        for (String p : patterns) {
            if (p == null || p.isEmpty()) continue;
            if (forwardUri != null && forwardUri.contains(p)) return true;
            if (forwardServletPath != null && forwardServletPath.contains(p)) return true;
            if (uri != null && uri.contains(p)) return true;
            if (servletPath != null && servletPath.contains(p)) return true;
        }
        return false;
    }

    private boolean hasPermission(Object permissions, String code) {
        return permissions instanceof java.util.Set && ((java.util.Set<?>) permissions).contains(code);
    }
%>
<%
    Object sidebarUser = session.getAttribute("currentUser");
    Object sidebarPermissions = session.getAttribute("permissionCodes");
    String sidebarRole = sideBeanString(sidebarUser, "getRoleName");
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
    boolean isReception = "RECEPTIONIST".equalsIgnoreCase(sidebarRole);
    boolean isHousekeeping = "HOUSEKEEPING".equalsIgnoreCase(sidebarRole);
    boolean isManager = "HOTEL_MANAGER".equalsIgnoreCase(sidebarRole);
    boolean isAdmin = "ADMIN".equalsIgnoreCase(sidebarRole);
    boolean canAdminUsers = isAdmin || hasPermission(sidebarPermissions, "ADMIN_USERS");
    boolean canAdminRoles = isAdmin || hasPermission(sidebarPermissions, "ADMIN_ROLES");
    boolean canAdminLogs = isAdmin || hasPermission(sidebarPermissions, "ADMIN_LOGS");

    // Xử lý active tab cho Housekeeping & Manager
    String currentView = request.getParameter("view");
    Object resultObj = request.getAttribute("result");
    if (currentView == null && resultObj instanceof HousekeepingService.TaskPage) {
        currentView = ((HousekeepingService.TaskPage) resultObj).view();
    }
    boolean isHkHistory = "history".equals(currentView);
    boolean isHkTaskPage = isPathActive(request, "/housekeeping/tasks", "task-list", "task-detail") && !isPathActive(request, "/manager/housekeeping");
    boolean isHkIssuePage = isPathActive(request, "/housekeeping/issues", "issue-list", "issue-report", "maintenance-verify") && !isPathActive(request, "/manager/issues");
    boolean isInternal = isReception || isHousekeeping || isManager || isAdmin || canAdminUsers || canAdminRoles || canAdminLogs;
%>
<aside class="internal-sidebar" aria-label="Internal navigation">
    <div class="sidebar-section">
        <p class="sidebar-label">Vận hành</p>
        <% if (isInternal) { %>
            <a class="<%= isPathActive(request, "/dashboard") ? "active" : "" %>" href="<%= cp %>/dashboard"><span>DB</span>Dashboard</a>
        <% } %>
        <% if (isReception) { %>
            <a class="<%= isPathActive(request, "/reception/bookings", "/reception/booking-detail") ? "active" : "" %>" href="<%= cp %>/reception/bookings"><span>BK</span>Danh sách booking</a>
            <a class="<%= isPathActive(request, "/reception/walk-in", "/receptionist/walk-in") ? "active" : "" %>" href="<%= cp %>/reception/walk-in"><span>WI</span>Đặt tại quầy</a>
            <a class="<%= isPathActive(request, "/reception/room-map") ? "active" : "" %>" href="<%= cp %>/reception/room-map"><span>RM</span>Sơ đồ phòng</a>
        <% } %>

        <% if (isHousekeeping) { %>
            <a class="<%= isHkTaskPage && !isHkHistory ? "active" : "" %>" href="<%= cp %>/housekeeping/tasks?view=mine"><span>MY</span>Task của tôi</a>
            <a class="<%= isHkTaskPage && isHkHistory ? "active" : "" %>" href="<%= cp %>/housekeeping/tasks?view=history"><span>HS</span>Lịch sử dọn phòng</a>
            <a class="<%= isHkIssuePage ? "active" : "" %>" href="<%= cp %>/housekeeping/issues"><span>IS</span>Sự cố thiết bị</a>
        <% } %>

        <% if (isManager) { %>
            <a class="<%= isPathActive(request, "/manager/reports") ? "active" : "" %>" href="<%= cp %>/manager/reports"><span>RP</span>Báo cáo</a>
            <a class="<%= isPathActive(request, "/manager/bookings", "/manager/booking-detail") ? "active" : "" %>" href="<%= cp %>/manager/bookings"><span>BK</span>Quản lý booking</a>
            <a class="<%= isPathActive(request, "/manager/refunds") ? "active" : "" %>" href="<%= cp %>/manager/refunds"><span>RF</span>Yêu cầu hoàn tiền</a>
            <a class="<%= isPathActive(request, "/manager/rooms", "/manager/room-form") ? "active" : "" %>" href="<%= cp %>/manager/rooms"><span>RM</span>Phòng</a>
            <a class="<%= isPathActive(request, "/manager/room-map") ? "active" : "" %>" href="<%= cp %>/manager/room-map"><span>MP</span>Sơ đồ phòng</a>
            <a class="<%= isPathActive(request, "/manager/room-types", "/manager/room-type") ? "active" : "" %>" href="<%= cp %>/manager/room-types"><span>RT</span>Loại phòng</a>
            <a class="<%= isPathActive(request, "/manager/equipment") ? "active" : "" %>" href="<%= cp %>/manager/equipment"><span>EQ</span>Thiết bị</a>
            <a class="<%= isPathActive(request, "/manager/amenities", "/manager/amenity") ? "active" : "" %>" href="<%= cp %>/manager/amenities"><span>AM</span>Tiện nghi</a>
            <a class="<%= isPathActive(request, "/manager/housekeeping") || (isManager && isPathActive(request, "task-list", "task-detail")) ? "active" : "" %>" href="<%= cp %>/manager/housekeeping"><span>HK</span>Lịch sử dọn phòng</a>
            <a class="<%= isPathActive(request, "/manager/issues") || (isManager && isPathActive(request, "issue-list", "issue-report", "maintenance-verify")) ? "active" : "" %>" href="<%= cp %>/manager/issues"><span>IS</span>Sự cố thiết bị</a>
            <a class="<%= isPathActive(request, "/manager/pricing", "/manager/promotions") ? "active" : "" %>" href="<%= cp %>/manager/pricing"><span>PR</span>Giá, mã giảm giá</a>
            <a class="<%= isPathActive(request, "/manager/policies", "/manager/policy") ? "active" : "" %>" href="<%= cp %>/manager/policies"><span>PL</span>Chính sách</a>
            <a class="<%= isPathActive(request, "/manager/news") ? "active" : "" %>" href="<%= cp %>/manager/news"><span>NW</span>Tin tức</a>
            <a class="<%= isPathActive(request, "/manager/feedbacks") ? "active" : "" %>" href="<%= cp %>/manager/feedbacks"><span>FB</span>Đánh giá khách hàng</a>
        <% } %>

        <% if (canAdminUsers) { %>
            <a class="<%= isPathActive(request, "/admin/users", "/admin/user-form") ? "active" : "" %>" href="<%= cp %>/admin/users"><span>US</span>Người dùng</a>
        <% } %>
        <% if (canAdminRoles) { %>
            <a class="<%= isPathActive(request, "/admin/roles", "/admin/role-form") ? "active" : "" %>" href="<%= cp %>/admin/roles"><span>RL</span>Vai trò và quyền</a>
        <% } %>
        <% if (canAdminLogs) { %>
            <a class="<%= isPathActive(request, "/admin/logs") ? "active" : "" %>" href="<%= cp %>/admin/logs"><span>LG</span>Nhật ký</a>
        <% } %>
    </div>
    <div class="sidebar-section sidebar-section--bottom">
        <p class="sidebar-label">Tài khoản</p>
        <a class="<%= isPathActive(request, "/profile") ? "active" : "" %>" href="<%= cp %>/profile"><span>PF</span>Hồ sơ</a>
        <a class="<%= isPathActive(request, "/change-password") ? "active" : "" %>" href="<%= cp %>/change-password"><span>PW</span>Đổi mật khẩu</a>
    </div>
</aside>
