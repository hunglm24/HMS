<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    Object footerUser = session.getAttribute("currentUser");
    String footerRoleName = "";
    if (footerUser != null) {
        try {
            footerRoleName = String.valueOf(footerUser.getClass().getMethod("getRoleName").invoke(footerUser));
        } catch (ReflectiveOperationException ignore) {
            footerRoleName = "";
        }
    }
    boolean footerManager = "HOTEL_MANAGER".equalsIgnoreCase(footerRoleName);
    model.HotelConfig footerConfig = (model.HotelConfig) application.getAttribute("hotelConfig");
    String footerHotelName = footerConfig != null && footerConfig.getHotelName() != null && !footerConfig.getHotelName().isBlank()
            ? footerConfig.getHotelName()
            : "HMS Hotel";
    String footerMark = footerHotelName.isBlank() ? "H" : footerHotelName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    String footerAddress = footerConfig != null && footerConfig.getAddress() != null ? footerConfig.getAddress() : "";
    String footerPhone = footerConfig != null && footerConfig.getPhone() != null ? footerConfig.getPhone() : "";
%>
<footer class="site-footer">
    <div class="footer-inner">
        <div class="footer-brand">
            <span class="footer-brand-mark" aria-hidden="true"><%= footerMark %></span>
            <span><strong><%= escapeHtml(footerHotelName) %></strong><small>Hotel Management System</small></span>
        </div>
        <p class="footer-copy">
            Booking, reception, housekeeping and operations
            <% if (!footerAddress.isBlank()) { %><br><span><%= escapeHtml(footerAddress) %></span><% } %>
            <% if (!footerPhone.isBlank()) { %><br><span>Hotline: <%= escapeHtml(footerPhone) %></span><% } %>
        </p>
        <div class="footer-links">
            <a href="<%= request.getContextPath() %>/hotel-policy">Nội quy chung</a>
        </div>
    </div>
</footer>
