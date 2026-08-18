<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String sideBeanString(Object bean, String getterName) {
        if (bean == null) return "";
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
%>
<%
    Object sidebarUser = session.getAttribute("currentUser");
    String sidebarRole = sideBeanString(sidebarUser, "getRoleName");
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
    boolean isReception = "RECEPTIONIST".equalsIgnoreCase(sidebarRole);
    boolean isHousekeeping = "HOUSEKEEPING".equalsIgnoreCase(sidebarRole);
    boolean isManager = "HOTEL_MANAGER".equalsIgnoreCase(sidebarRole);
    boolean isAdmin = "ADMIN".equalsIgnoreCase(sidebarRole);
%>
<aside class="internal-sidebar" aria-label="Internal navigation">
    <div class="sidebar-section">
        <p class="sidebar-label">Vận hành</p>
        <% if (isReception) { %>
            <a class="<%= activePath(uri, "/reception/bookings") ? "active" : "" %>" href="<%= cp %>/reception/bookings"><span>BK</span>Danh sách booking</a>
            <a class="<%= activePath(uri, "/reception/walk-in") ? "active" : "" %>" href="<%= cp %>/reception/walk-in"><span>WI</span>Đặt tại quầy</a>
            <a class="<%= activePath(uri, "/reception/room-map") ? "active" : "" %>" href="<%= cp %>/reception/room-map"><span>RM</span>Sơ đồ phòng</a>
            <a class="<%= activePath(uri, "/reception/room-change-history") ? "active" : "" %>" href="<%= cp %>/reception/room-change-history"><span>RH</span>Lịch sử đổi phòng</a>
            <a class="<%= activePath(uri, "/reception/check-in") ? "active" : "" %>" href="<%= cp %>/reception/check-in"><span>CI</span>Check-in</a>
            <a class="<%= activePath(uri, "/reception/check-out") ? "active" : "" %>" href="<%= cp %>/reception/check-out"><span>CO</span>Check-out</a>
        <% } %>

        <% if (isHousekeeping) { %>
            <a class="<%= activePath(uri, "/housekeeping/tasks") ? "active" : "" %>" href="<%= cp %>/housekeeping/tasks"><span>HK</span>Task chờ nhận</a>
            <a href="<%= cp %>/housekeeping/tasks?view=mine"><span>MY</span>Task của tôi</a>
            <a href="<%= cp %>/housekeeping/tasks?view=history"><span>HS</span>Lịch sử</a>
            <a class="<%= activePath(uri, "/housekeeping/issues") ? "active" : "" %>" href="<%= cp %>/housekeeping/issues"><span>IS</span>Quản lý sự cố</a>
        <% } %>

        <% if (isManager) { %>
            <a class="<%= activePath(uri, "/manager/reports") ? "active" : "" %>" href="<%= cp %>/manager/reports"><span>RP</span>Báo cáo</a>
            <a class="<%= activePath(uri, "/manager/rooms") ? "active" : "" %>" href="<%= cp %>/manager/rooms"><span>RM</span>Phòng &amp; loại phòng</a>
            <a class="<%= activePath(uri, "/housekeeping/tasks") ? "active" : "" %>" href="<%= cp %>/housekeeping/tasks?view=history"><span>HK</span>Nhiệm vụ dọn phòng</a>
            <a class="<%= activePath(uri, "/manager/pricing") ? "active" : "" %>" href="<%= cp %>/manager/pricing"><span>PR</span>Giá và dịch vụ</a>
            <a class="<%= activePath(uri, "/manager/policies") ? "active" : "" %>" href="<%= cp %>/manager/policies"><span>PL</span>Chính sách</a>
            <a class="<%= activePath(uri, "/manager/staff") ? "active" : "" %>" href="<%= cp %>/manager/staff"><span>ST</span>Nhân sự</a>
        <% } %>

        <% if (isAdmin) { %>
            <a class="<%= activePath(uri, "/admin/users") ? "active" : "" %>" href="<%= cp %>/admin/users"><span>US</span>Người dùng</a>
            <a class="<%= activePath(uri, "/admin/roles") ? "active" : "" %>" href="<%= cp %>/admin/roles"><span>RL</span>Vai trò và quyền</a>
            <a class="<%= activePath(uri, "/admin/system-config") ? "active" : "" %>" href="<%= cp %>/admin/system-config"><span>CF</span>Cấu hình</a>
            <a class="<%= activePath(uri, "/admin/logs") ? "active" : "" %>" href="<%= cp %>/admin/logs"><span>LG</span>Nhật ký</a>
        <% } %>
    </div>
    <div class="sidebar-section sidebar-section--bottom">
        <p class="sidebar-label">Tài khoản</p>
        <a href="<%= cp %>/profile"><span>PF</span>Hồ sơ</a>
        <a href="<%= cp %>/change-password"><span>PW</span>Đổi mật khẩu</a>
    </div>
</aside>
