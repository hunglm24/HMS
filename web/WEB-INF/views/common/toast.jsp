<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String toastMessage = (String) request.getAttribute("toastMessage"); %>
<% if (toastMessage != null && !toastMessage.trim().isEmpty()) { %>
    <div class="toast" role="status"><%= toastMessage %></div>
<% } %>
