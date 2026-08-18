<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Role" %>
<%@ page import="model.Permission" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    private String js(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("\\", "\\\\").replace("'", "\\'")
                .replace("\r", "").replace("\n", "\\n");
    }
%>
<%
    List<Role> roles = (List<Role>) request.getAttribute("roles");
    List<Permission> permissions = (List<Permission>) request.getAttribute("permissions");
    long selectedRoleId = request.getAttribute("selectedRoleId") == null ? 0L : (Long) request.getAttribute("selectedRoleId");
    String toastMessage = (String) session.getAttribute("toastMessage");
    String toastType = (String) session.getAttribute("toastType");
    session.removeAttribute("toastMessage");
    session.removeAttribute("toastType");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Roles & Permissions - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <style>
        .admin-tabs { display:flex; gap:10px; margin-bottom:20px; border-bottom:1px solid var(--color-border); padding-bottom:10px; }
        .admin-tabs a { padding:8px 14px; border-radius:6px; text-decoration:none; font-weight:700; color:#344054; }
        .admin-tabs a.active { background:var(--color-primary-100); color:var(--color-primary-600); }
        .layout { display:grid; grid-template-columns:minmax(280px, 380px) 1fr; gap:20px; align-items:start; }
        .panel { padding:18px; background:#fff; border:1px solid var(--color-border); border-radius:8px; }
        .data-table { width:100%; border-collapse:collapse; }
        .data-table th,.data-table td { padding:10px; border-bottom:1px solid var(--color-border); text-align:left; }
        .role-link { font-weight:700; text-decoration:none; }
        .role-link.active { color:var(--color-primary-600); }
        .inline-form { display:inline; margin:0; }
        .small-button { min-height:0; padding:6px 9px; font-size:12px; }
        .permission-list { display:grid; gap:10px; margin:14px 0; }
        .permission-item { display:grid; grid-template-columns:24px 1fr; gap:8px; padding:10px; border:1px solid var(--color-border); border-radius:8px; }
        .message { margin-bottom:14px; padding:10px 12px; border-radius:8px; border:1px solid var(--color-border); background:#fff; }
        .message.success { border-color:#86efac; background:#f0fdf4; color:#166534; }
        .message.error { border-color:#fecaca; background:#fef2f2; color:#991b1b; }
        @media (max-width: 900px) { .layout { grid-template-columns:1fr; } }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">
    <nav class="admin-tabs">
        <a href="${pageContext.request.contextPath}/admin/users">Users</a>
        <a class="active" href="${pageContext.request.contextPath}/admin/roles">Roles & Permissions</a>
        <a href="${pageContext.request.contextPath}/admin/logs">System Logs</a>
    </nav>

    <h1>Assign Role / Permission</h1>
    <p>Create roles and assign the permissions used by the administration module.</p>

    <% if (toastMessage != null) { %>
        <div class="message <%= h(toastType) %>"><%= h(toastMessage) %></div>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <div class="message error"><%= h(request.getAttribute("error")) %></div>
    <% } %>

    <div class="layout">
        <section class="panel">
            <h2>Roles</h2>
            <table class="data-table">
                <thead><tr><th>Name</th><th>Actions</th></tr></thead>
                <tbody>
                <% if (roles != null) for (Role role : roles) { %>
                    <tr>
                        <td>
                            <a class="role-link <%= role.getId() == selectedRoleId ? "active" : "" %>"
                               href="${pageContext.request.contextPath}/admin/roles?roleId=<%= role.getId() %>"><%= h(role.getName()) %></a>
                            <br><small><%= h(role.getDescription()) %></small>
                        </td>
                        <td>
                            <button class="button button-secondary small-button" type="button"
                                    onclick="fillRole('<%= role.getId() %>','<%= h(js(role.getName())) %>','<%= h(js(role.getDescription())) %>')">Edit</button>
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/roles/delete" onsubmit="return confirm('Delete this role?');">
                                <input type="hidden" name="id" value="<%= role.getId() %>">
                                <button class="button button-secondary small-button" type="submit">Delete</button>
                            </form>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>

            <h2 id="roleFormTitle">Create role</h2>
            <form method="post" action="${pageContext.request.contextPath}/admin/roles/save">
                <input type="hidden" id="roleId" name="id">
                <label class="form-label" for="roleName">Role name</label>
                <input class="form-control" id="roleName" name="name" required>
                <label class="form-label" for="description">Description</label>
                <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                <div class="form-actions">
                    <button class="button button-secondary" type="button" onclick="resetRoleForm()">New</button>
                    <button class="button button-primary" type="submit">Save role</button>
                </div>
            </form>
        </section>

        <section class="panel">
            <h2>Permissions</h2>
            <% if (selectedRoleId == 0) { %>
                <p>No role selected.</p>
            <% } else { %>
                <form method="post" action="${pageContext.request.contextPath}/admin/roles/permissions">
                    <input type="hidden" name="roleId" value="<%= selectedRoleId %>">
                    <div class="permission-list">
                        <% if (permissions != null) for (Permission permission : permissions) { %>
                            <label class="permission-item">
                                <input type="checkbox" name="permissionId" value="<%= permission.getId() %>" <%= permission.isAssigned() ? "checked" : "" %>>
                                <span><strong><%= h(permission.getCode()) %></strong><br><%= h(permission.getName()) %><br><small><%= h(permission.getDescription()) %></small></span>
                            </label>
                        <% } %>
                    </div>
                    <button class="button button-primary" type="submit">Save permissions</button>
                </form>
            <% } %>
        </section>
    </div>
</main>
<script>
    function fillRole(id, name, description) {
        document.getElementById('roleFormTitle').textContent = 'Edit role #' + id;
        document.getElementById('roleId').value = id;
        document.getElementById('roleName').value = name;
        document.getElementById('description').value = description;
    }
    function resetRoleForm() {
        document.getElementById('roleFormTitle').textContent = 'Create role';
        document.getElementById('roleId').value = '';
        document.getElementById('roleName').value = '';
        document.getElementById('description').value = '';
    }
</script>
</body>
</html>
