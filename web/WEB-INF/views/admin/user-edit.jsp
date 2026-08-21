<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="model.Role" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    private boolean selectableRole(Role role) {
        return role != null && !"ADMIN".equalsIgnoreCase(role.getName());
    }
%>
<%
    User editUser = (User) request.getAttribute("editUser");
    List<Role> roles = (List<Role>) request.getAttribute("roles");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit User - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .admin-tabs { display:flex; gap:10px; margin-bottom:20px; border-bottom:1px solid var(--color-border); padding-bottom:10px; }
        .admin-tabs a { padding:8px 14px; border-radius:6px; text-decoration:none; font-weight:700; color:#344054; }
        .admin-tabs a.active { background:var(--color-primary-100); color:var(--color-primary-600); }
        .admin-panel { max-width: 880px; padding:18px; background:#fff; border:1px solid var(--color-border); border-radius:8px; }
        .form-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:12px; }
        .form-actions { display:flex; gap:10px; justify-content:flex-end; margin-top:18px; }
        .readonly-value { min-height:40px; display:flex; align-items:center; padding:8px 10px; border:1px solid var(--color-border); border-radius:8px; background:var(--color-bg-surface); color:var(--color-text-secondary); }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">
    <nav class="admin-tabs">
        <a class="active" href="${pageContext.request.contextPath}/admin/users">Users</a>
        <a href="${pageContext.request.contextPath}/admin/roles">Roles & Permissions</a>
        <a href="${pageContext.request.contextPath}/admin/logs">System Logs</a>
    </nav>

    <h1>Edit user #<%= editUser.getUserId() %></h1>
    <p>Update account information, role and status.</p>

    <section class="admin-panel">
        <form method="post" action="${pageContext.request.contextPath}/admin/users/save">
            <input type="hidden" name="id" value="<%= editUser.getUserId() %>">
            <div class="form-grid">
                <div>
                    <label class="form-label" for="fullName">Full name</label>
                    <input class="form-control" id="fullName" name="fullName" value="<%= h(editUser.getFullName()) %>" required>
                </div>
                <div>
                    <label class="form-label" for="email">Email</label>
                    <input class="form-control" id="email" name="email" type="email" value="<%= h(editUser.getEmail()) %>" required>
                </div>
                <div>
                    <label class="form-label" for="phone">Phone</label>
                    <input class="form-control" id="phone" name="phone" value="<%= h(editUser.getPhone()) %>">
                </div>
                <div>
                    <label class="form-label" for="roleId">Role</label>
                    <% if ("ADMIN".equalsIgnoreCase(editUser.getRoleName())) { %>
                        <input type="hidden" name="roleId" value="<%= editUser.getRoleId() %>">
                        <div class="readonly-value"><%= h(editUser.getRoleName()) %></div>
                    <% } else { %>
                        <select class="form-control" id="roleId" name="roleId" required>
                            <% if (roles != null) for (Role role : roles) { %>
                                <% if (selectableRole(role)) { %>
                                    <option value="<%= role.getId() %>" <%= role.getId() == editUser.getRoleId() ? "selected" : "" %>><%= h(role.getName()) %></option>
                                <% } %>
                            <% } %>
                        </select>
                    <% } %>
                </div>
                <div>
                    <label class="form-label" for="userStatus">Status</label>
                    <select class="form-control" id="userStatus" name="status">
                        <option value="ACTIVE" <%= "ACTIVE".equals(editUser.getStatus()) ? "selected" : "" %>>ACTIVE</option>
                        <option value="INACTIVE" <%= "INACTIVE".equals(editUser.getStatus()) ? "selected" : "" %>>INACTIVE</option>
                        <option value="BLOCKED" <%= "BLOCKED".equals(editUser.getStatus()) ? "selected" : "" %>>BLOCKED</option>
                    </select>
                </div>
                <div>
                    <label class="form-label">Created</label>
                    <div class="readonly-value"><%= h(editUser.getCreatedAt()) %></div>
                </div>
            </div>
            <div class="form-actions">
                <a class="button button-secondary" href="${pageContext.request.contextPath}/admin/users">Cancel</a>
                <button class="button button-primary" type="submit">Save user</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
