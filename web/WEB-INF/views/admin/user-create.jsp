<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
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
    private boolean hasPermission(Object permissions, String code) {
        return permissions instanceof java.util.Set && ((java.util.Set<?>) permissions).contains(code);
    }
    private String beanString(Object bean, String getterName) {
        if (bean == null) return "";
        try {
            Object value = bean.getClass().getMethod(getterName).invoke(bean);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }
%>
<%
    List<Role> roles = (List<Role>) request.getAttribute("roles");
    String currentRole = beanString(session.getAttribute("currentUser"), "getRoleName");
    Object permissionCodes = session.getAttribute("permissionCodes");
    boolean admin = "ADMIN".equalsIgnoreCase(currentRole);
    boolean canAdminRoles = admin || hasPermission(permissionCodes, "ADMIN_ROLES");
    boolean canAdminLogs = admin || hasPermission(permissionCodes, "ADMIN_LOGS");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Create User - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .admin-tabs { display:flex; gap:10px; margin-bottom:20px; border-bottom:1px solid var(--color-border); padding-bottom:10px; }
        .admin-tabs a { padding:8px 14px; border-radius:6px; text-decoration:none; font-weight:700; color:#344054; }
        .admin-tabs a.active { background:var(--color-primary-100); color:var(--color-primary-600); }
        .admin-panel { max-width: 980px; padding:18px; background:#fff; border:1px solid var(--color-border); border-radius:8px; }
        .form-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:12px; }
        .form-actions { display:flex; gap:10px; justify-content:flex-end; margin-top:18px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">
    <nav class="admin-tabs">
        <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
        <a class="active" href="${pageContext.request.contextPath}/admin/users">Users</a>
        <% if (canAdminRoles) { %><a href="${pageContext.request.contextPath}/admin/roles">Roles & Permissions</a><% } %>
        <% if (canAdminLogs) { %><a href="${pageContext.request.contextPath}/admin/logs">System Logs</a><% } %>
    </nav>

    <h1>Create user</h1>
    <p>Create an internal or customer account. ADMIN role is intentionally not assignable here.</p>

    <section class="admin-panel">
        <form method="post" action="${pageContext.request.contextPath}/admin/users/save" autocomplete="off">
            <div class="form-grid">
                <div>
                    <label class="form-label" for="fullName">Full name</label>
                    <input class="form-control" id="fullName" name="fullName" required>
                </div>
                <div>
                    <label class="form-label" for="email">Email</label>
                    <input class="form-control" id="email" name="email" type="email" required>
                </div>
                <div>
                    <label class="form-label" for="phone">Phone</label>
                    <input class="form-control" id="phone" name="phone">
                </div>
                <div>
                    <label class="form-label" for="roleId">Role</label>
                    <select class="form-control" id="roleId" name="roleId" required>
                        <option value="">Select role</option>
                        <% if (roles != null) for (Role role : roles) { %>
                            <% if (selectableRole(role)) { %>
                                <option value="<%= role.getId() %>"><%= h(role.getName()) %></option>
                            <% } %>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label class="form-label" for="status">Status</label>
                    <select class="form-control" id="status" name="status" required>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                        <option value="BLOCKED">BLOCKED</option>
                    </select>
                </div>
                <div>
                    <label class="form-label" for="password">Password</label>
                    <input class="form-control" id="password" name="password" type="password" minlength="8" required autocomplete="new-password">
                </div>
                <div>
                    <label class="form-label" for="confirmPassword">Confirm password</label>
                    <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" minlength="8" required autocomplete="new-password">
                </div>
            </div>
            <div class="form-actions">
                <a class="button button-secondary" href="${pageContext.request.contextPath}/admin/users">Cancel</a>
                <button class="button button-primary" type="submit">Create user</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
