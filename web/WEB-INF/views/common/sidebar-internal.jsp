<<<<<<< Updated upstream
=======
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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

    private boolean activePath(String uri, String path) {
        return uri != null && uri.contains(path);
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
%>
<aside class="internal-sidebar" aria-label="Internal navigation">
    <div class="sidebar-section">
        <p class="sidebar-label">Vận hành</p>
        <% if (isReception) { %>
            <a class="<%= activePath(uri, "/reception/bookings") ? "active" : "" %>" href="<%= cp %>/reception/bookings"><span>BK</span>Danh sách booking</a>
            <a class="<%= activePath(uri, "/reception/walk-in") ? "active" : "" %>" href="<%= cp %>/reception/walk-in"><span>WI</span>Đặt tại quầy</a>
            <a class="<%= activePath(uri, "/reception/room-map") ? "active" : "" %>" href="<%= cp %>/reception/room-map"><span>RM</span>Sơ đồ phòng</a>
        <% } %>

        <% if (isHousekeeping) { %>
            <a class="<%= (activePath(uri, "/housekeeping/tasks") || activePath(uri, "/housekeeping/tasks/detail")) && !"history".equals(request.getParameter("view")) ? "active" : "" %>" href="<%= cp %>/housekeeping/tasks?view=mine"><span>MY</span>Task của tôi</a>
            <a class="<%= activePath(uri, "/housekeeping/tasks") && "history".equals(request.getParameter("view")) ? "active" : "" %>" href="<%= cp %>/housekeeping/tasks?view=history"><span>HS</span>Lịch sử dọn phòng</a>
            <a class="<%= activePath(uri, "/housekeeping/issues") ? "active" : "" %>" href="<%= cp %>/housekeeping/issues"><span>IS</span>Sự cố thiết bị</a>
        <% } %>

        <% if (isManager) { %>
            <a class="<%= activePath(uri, "/manager/bookings") || activePath(uri, "/manager/booking-detail") ? "active" : "" %>" href="<%= cp %>/manager/bookings"><span>BK</span>Quản lý booking</a>
            <a class="<%= activePath(uri, "/manager/refunds") ? "active" : "" %>" href="<%= cp %>/manager/refunds"><span>RF</span>Yêu cầu hoàn tiền</a>
            <a class="<%= activePath(uri, "/manager/reports") ? "active" : "" %>" href="<%= cp %>/manager/reports"><span>RP</span>Báo cáo</a>
            <a class="<%= activePath(uri, "/manager/rooms") ? "active" : "" %>" href="<%= cp %>/manager/rooms"><span>RM</span>Phòng</a>
            <a class="<%= activePath(uri, "/manager/room-types") ? "active" : "" %>" href="<%= cp %>/manager/room-types"><span>RT</span>Loại phòng</a>
            <a class="<%= activePath(uri, "/manager/equipment") ? "active" : "" %>" href="<%= cp %>/manager/equipment"><span>EQ</span>Thiết bị</a>
            <a class="<%= activePath(uri, "/manager/amenities") || activePath(uri, "/manager/amenity") ? "active" : "" %>" href="<%= cp %>/manager/amenities"><span>AM</span>Tiện nghi</a>
            <a class="<%= activePath(uri, "/manager/housekeeping") || (activePath(uri, "/housekeeping/tasks") && "history".equals(request.getParameter("view"))) ? "active" : "" %>" href="<%= cp %>/manager/housekeeping"><span>HK</span>Lịch sử dọn phòng</a>
            <a class="<%= activePath(uri, "/manager/issues") || activePath(uri, "/housekeeping/issues") ? "active" : "" %>" href="<%= cp %>/manager/issues"><span>IS</span>Sự cố thiết bị</a>
            <a class="<%= activePath(uri, "/manager/invoices") ? "active" : "" %>" href="<%= cp %>/manager/invoices"><span>IV</span>Hóa đơn</a>
            <a class="<%= activePath(uri, "/manager/pricing") ? "active" : "" %>" href="<%= cp %>/manager/pricing"><span>PR</span>Giá, mã giảm giá</a>
            <a class="<%= activePath(uri, "/manager/policies") ? "active" : "" %>" href="<%= cp %>/manager/policies"><span>PL</span>Chính sách</a>
            <a class="<%= activePath(uri, "/manager/news") ? "active" : "" %>" href="<%= cp %>/manager/news"><span>NW</span>Tin tức</a>
            <a class="<%= activePath(uri, "/manager/staff") ? "active" : "" %>" href="<%= cp %>/manager/staff"><span>ST</span>Nhân sự</a>
        <% } %>

        <% if (isAdmin || canAdminUsers || canAdminRoles || canAdminLogs) { %>
            <a class="<%= uri != null && (uri.endsWith(cp + "/") || uri.endsWith(cp)) ? "active" : "" %>" href="<%= cp %>/"><span>DB</span>Dashboard</a>
        <% } %>
        <% if (canAdminUsers) { %>
            <a class="<%= activePath(uri, "/admin/users") ? "active" : "" %>" href="<%= cp %>/admin/users"><span>US</span>Người dùng</a>
        <% } %>
        <% if (canAdminRoles) { %>
            <a class="<%= activePath(uri, "/admin/roles") ? "active" : "" %>" href="<%= cp %>/admin/roles"><span>RL</span>Vai trò và quyền</a>
        <% } %>
        <% if (canAdminLogs) { %>
            <a class="<%= activePath(uri, "/admin/logs") ? "active" : "" %>" href="<%= cp %>/admin/logs"><span>LG</span>Nhật ký</a>
        <% } %>
    </div>
    <div class="sidebar-section sidebar-section--bottom">
        <p class="sidebar-label">Tài khoản</p>
        <a href="<%= cp %>/profile"><span>PF</span>Hồ sơ</a>
        <a href="<%= cp %>/change-password"><span>PW</span>Đổi mật khẩu</a>
    </div>
</aside>
>>>>>>> Stashed changes
