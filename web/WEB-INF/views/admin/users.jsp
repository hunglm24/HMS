<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="model.User" %>
<%@ page import="model.Role" %>
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
    private String enc(Object value) {
        if (value == null) return "";
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
%>
<%
    List<User> users = (List<User>) request.getAttribute("users");
    List<Role> roles = (List<Role>) request.getAttribute("roles");
    String q = (String) request.getAttribute("q");
    String selectedRole = (String) request.getAttribute("selectedRole");
    String selectedStatus = (String) request.getAttribute("selectedStatus");
    int currentPage = request.getAttribute("page") == null ? 1 : (Integer) request.getAttribute("page");
    int pageSize = request.getAttribute("pageSize") == null ? 5 : (Integer) request.getAttribute("pageSize");
    int totalPages = request.getAttribute("totalPages") == null ? 1 : (Integer) request.getAttribute("totalPages");
    int totalItems = request.getAttribute("totalItems") == null ? 0 : (Integer) request.getAttribute("totalItems");
    String pageQuery = "&q=" + enc(q) + "&role=" + enc(selectedRole) + "&status=" + enc(selectedStatus);
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
    <title>User Management - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <style>
        .admin-tabs { display:flex; gap:10px; margin-bottom:20px; border-bottom:1px solid var(--color-border); padding-bottom:10px; }
        .admin-tabs a { padding:8px 14px; border-radius:6px; text-decoration:none; font-weight:700; color:#344054; }
        .admin-tabs a.active { background:var(--color-primary-100); color:var(--color-primary-600); }
        .toolbar { display:flex; justify-content:space-between; gap:12px; align-items:end; flex-wrap:wrap; margin-bottom:16px; }
        .filters { display:flex; gap:10px; flex-wrap:wrap; align-items:end; }
        .filters .filter-field { width:190px; }
        .filters .search-field { width:210px; }
        .data-table { width:100%; border-collapse:collapse; background:#fff; border:1px solid var(--color-border); border-radius:8px; overflow:hidden; }
        .data-table th,.data-table td { padding:11px 12px; border-bottom:1px solid var(--color-border); text-align:left; vertical-align:top; }
        .data-table th { background:var(--color-bg-surface); color:var(--color-text-secondary); font-size:13px; }
        .badge { display:inline-flex; padding:3px 8px; border-radius:999px; font-size:12px; font-weight:700; }
        .badge-active { background:#dcfce7; color:#166534; }
        .badge-inactive { background:#f3f4f6; color:#374151; }
        .badge-blocked { background:#fee2e2; color:#991b1b; }
        .row-actions { display:grid; grid-template-columns:repeat(4, 78px); gap:6px; align-items:center; }
        .inline-form { display:inline; margin:0; }
        .small-button { width:78px; min-height:34px; padding:6px 8px; font-size:12px; border-radius:8px; }
        .admin-panel { margin-top:20px; padding:18px; background:#fff; border:1px solid var(--color-border); border-radius:8px; }
        .form-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:12px; }
        .message { margin-bottom:14px; padding:10px 12px; border-radius:8px; border:1px solid var(--color-border); background:#fff; }
        .message.success { border-color:#86efac; background:#f0fdf4; color:#166534; }
        .message.error { border-color:#fecaca; background:#fef2f2; color:#991b1b; }
        .pagination-bar { display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap; margin-top:14px; }
        .pagination-links { display:flex; gap:6px; align-items:center; flex-wrap:wrap; }
        .page-link { min-width:38px; min-height:36px; display:inline-flex; align-items:center; justify-content:center; padding:7px 11px; border:1px solid var(--color-border); border-radius:8px; background:#fff; color:var(--color-text-primary); font-weight:700; text-decoration:none; }
        .page-link.active { border-color:var(--color-primary-600); background:var(--color-primary-600); color:#fff; }
        .page-link.disabled { opacity:.45; pointer-events:none; }
        @media (max-width: 900px) { .row-actions { grid-template-columns:repeat(2, 78px); } }
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

    <h1>User & Roles Management</h1>
    <p>CRUD user accounts, assign role and control account status.</p>

    <% if (toastMessage != null) { %>
        <div class="message <%= h(toastType) %>"><%= h(toastMessage) %></div>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <div class="message error"><%= h(request.getAttribute("error")) %></div>
    <% } %>

    <div class="toolbar">
        <form class="filters" method="get" action="${pageContext.request.contextPath}/admin/users">
            <div class="search-field">
                <label class="form-label" for="q">Search</label>
                <input class="form-control" id="q" name="q" value="<%= h(q) %>" placeholder="Name, email, phone">
            </div>
            <div class="filter-field">
                <label class="form-label" for="role">Role</label>
                <select class="form-control" id="role" name="role">
                    <option value="">All roles</option>
                    <% if (roles != null) for (Role role : roles) { %>
                        <option value="<%= h(role.getName()) %>" <%= role.getName().equals(selectedRole) ? "selected" : "" %>><%= h(role.getName()) %></option>
                    <% } %>
                </select>
            </div>
            <div class="filter-field">
                <label class="form-label" for="status">Status</label>
                <select class="form-control" id="status" name="status">
                    <option value="">All statuses</option>
                    <option value="ACTIVE" <%= "ACTIVE".equals(selectedStatus) ? "selected" : "" %>>ACTIVE</option>
                    <option value="INACTIVE" <%= "INACTIVE".equals(selectedStatus) ? "selected" : "" %>>INACTIVE</option>
                    <option value="BLOCKED" <%= "BLOCKED".equals(selectedStatus) ? "selected" : "" %>>BLOCKED</option>
                </select>
            </div>
            <button class="button button-secondary" type="submit">Filter</button>
        </form>
    </div>

    <table class="data-table">
        <thead>
        <tr><th>ID</th><th>User</th><th>Contact</th><th>Role</th><th>Status</th><th>Created</th><th>Actions</th></tr>
        </thead>
        <tbody>
        <% if (users != null && !users.isEmpty()) for (User user : users) { %>
            <tr>
                <td><%= user.getUserId() %></td>
                <td><strong><%= h(user.getFullName()) %></strong><br><small><%= h(user.getEmail()) %></small></td>
                <td><%= h(user.getPhone()) %></td>
                <td><%= h(user.getRoleName()) %></td>
                <td><span class="badge badge-<%= h(user.getStatus().toLowerCase()) %>"><%= h(user.getStatus()) %></span></td>
                <td><%= h(user.getCreatedAt()) %></td>
                <td>
                    <div class="row-actions">
                        <button class="button button-secondary small-button" type="button"
                                onclick="fillUser('<%= user.getUserId() %>','<%= h(js(user.getFullName())) %>','<%= h(js(user.getEmail())) %>','<%= h(js(user.getPhone())) %>','<%= user.getRoleId() %>','<%= h(js(user.getStatus())) %>')">Edit</button>
                        <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/users/status">
                            <input type="hidden" name="id" value="<%= user.getUserId() %>">
                            <input type="hidden" name="status" value="<%= "BLOCKED".equals(user.getStatus()) ? "ACTIVE" : "BLOCKED" %>">
                            <button class="button button-secondary small-button" type="submit"><%= "BLOCKED".equals(user.getStatus()) ? "Unblock" : "Block" %></button>
                        </form>
                        <button class="button button-secondary small-button" type="button" onclick="fillPassword('<%= user.getUserId() %>')">Password</button>
                        <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/users/delete" onsubmit="return confirm('Delete this account?');">
                            <input type="hidden" name="id" value="<%= user.getUserId() %>">
                            <button class="button button-secondary small-button" type="submit">Delete</button>
                        </form>
                    </div>
                </td>
            </tr>
        <% } else { %>
            <tr><td colspan="7">No users found.</td></tr>
        <% } %>
        </tbody>
    </table>
    <div class="pagination-bar">
        <span>Showing <%= users == null ? 0 : users.size() %> of <%= totalItems %> users, 5 per page</span>
        <div class="pagination-links">
            <a class="page-link <%= currentPage <= 1 ? "disabled" : "" %>" href="${pageContext.request.contextPath}/admin/users?page=<%= currentPage - 1 %><%= pageQuery %>">Prev</a>
            <% for (int i = 1; i <= totalPages; i++) {
                if (i == 1 || i == totalPages || Math.abs(i - currentPage) <= 2) { %>
                    <a class="page-link <%= i == currentPage ? "active" : "" %>" href="${pageContext.request.contextPath}/admin/users?page=<%= i %><%= pageQuery %>"><%= i %></a>
                <% } else if (i == currentPage - 3 || i == currentPage + 3) { %>
                    <span class="page-link disabled">...</span>
                <% }
            } %>
            <a class="page-link <%= currentPage >= totalPages ? "disabled" : "" %>" href="${pageContext.request.contextPath}/admin/users?page=<%= currentPage + 1 %><%= pageQuery %>">Next</a>
        </div>
    </div>

    <section class="admin-panel">
        <h2 id="userFormTitle">Create user</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/users/save">
            <input type="hidden" id="userId" name="id">
            <div class="form-grid">
                <div><label class="form-label" for="fullName">Full name</label><input class="form-control" id="fullName" name="fullName" required></div>
                <div><label class="form-label" for="email">Email</label><input class="form-control" id="email" name="email" type="email" required></div>
                <div><label class="form-label" for="phone">Phone</label><input class="form-control" id="phone" name="phone"></div>
                <div>
                    <label class="form-label" for="roleId">Role</label>
                    <select class="form-control" id="roleId" name="roleId" required>
                        <% if (roles != null) for (Role role : roles) { %>
                            <option value="<%= role.getId() %>"><%= h(role.getName()) %></option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label class="form-label" for="userStatus">Status</label>
                    <select class="form-control" id="userStatus" name="status">
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                        <option value="BLOCKED">BLOCKED</option>
                    </select>
                </div>
                <div><label class="form-label" for="password">Password for new user</label><input class="form-control" id="password" name="password" type="password" minlength="8"></div>
            </div>
            <div class="form-actions">
                <button class="button button-secondary" type="button" onclick="resetUserForm()">New</button>
                <button class="button button-primary" type="submit">Save user</button>
            </div>
        </form>
    </section>

    <section class="admin-panel">
        <h2>Reset password</h2>
        <form class="filters" method="post" action="${pageContext.request.contextPath}/admin/users/password">
            <input type="hidden" id="passwordUserId" name="id">
            <input class="form-control" id="newPassword" name="password" type="password" minlength="8" placeholder="New password" required>
            <button class="button button-primary" type="submit">Reset password</button>
        </form>
    </section>
</main>
<script>
    function fillUser(id, fullName, email, phone, roleId, status) {
        document.getElementById('userFormTitle').textContent = 'Edit user #' + id;
        document.getElementById('userId').value = id;
        document.getElementById('fullName').value = fullName;
        document.getElementById('email').value = email;
        document.getElementById('phone').value = phone;
        document.getElementById('roleId').value = roleId;
        document.getElementById('userStatus').value = status;
        document.getElementById('password').value = '';
        document.getElementById('password').removeAttribute('required');
    }
    function resetUserForm() {
        document.getElementById('userFormTitle').textContent = 'Create user';
        document.getElementById('userId').value = '';
        document.getElementById('fullName').value = '';
        document.getElementById('email').value = '';
        document.getElementById('phone').value = '';
        document.getElementById('userStatus').value = 'ACTIVE';
        document.getElementById('password').setAttribute('required', 'required');
        document.getElementById('password').value = '';
    }
    function fillPassword(id) {
        document.getElementById('passwordUserId').value = id;
        document.getElementById('newPassword').focus();
    }
    resetUserForm();
</script>
</body>
</html>
