<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="model.Role" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return value.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private boolean selected(Object a, Object b) {
        return a != null && b != null && a.toString().equalsIgnoreCase(b.toString());
    }
%>
<%
    List<User> users = (List<User>) request.getAttribute("users");
    List<Role> roles = (List<Role>) request.getAttribute("roles");
    User editUser = (User) request.getAttribute("editUser");
    boolean editing = editUser != null && editUser.getUserId() > 0;
    String action = editing ? "update" : "create";
    if (request.getAttribute("formAction") != null) action = request.getAttribute("formAction").toString();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý người dùng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="page-container">
    <h1>Quản lý người dùng</h1>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= h(request.getAttribute("error")) %></div>
    <% } %>
    <% if ("created".equals(request.getAttribute("success"))) { %>
        <div class="alert alert-success">Đã tạo người dùng.</div>
    <% } else if ("updated".equals(request.getAttribute("success"))) { %>
        <div class="alert alert-success">Đã cập nhật người dùng.</div>
    <% } %>

    <section class="panel">
        <h2><%= editing ? "Cập nhật người dùng" : "Tạo người dùng" %></h2>
        <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/users">
            <input type="hidden" name="action" value="<%= h(action) %>">
            <% if (editing || "update".equals(action)) { %>
                <input type="hidden" name="id" value="<%= editUser == null ? 0 : editUser.getUserId() %>">
            <% } %>

            <label>Họ tên
                <input class="form-control" name="fullName" value="<%= h(editUser == null ? "" : editUser.getFullName()) %>" required maxlength="100">
            </label>

            <label>Email
                <input class="form-control" type="email" name="email" value="<%= h(editUser == null ? "" : editUser.getEmail()) %>" <%= editing ? "readonly" : "required" %>>
            </label>

            <% if (!editing && !"update".equals(action)) { %>
                <label>Mật khẩu
                    <input class="form-control" type="password" name="password" minlength="8" required>
                </label>
            <% } %>

            <label>Số điện thoại
                <input class="form-control" name="phone" value="<%= h(editUser == null ? "" : editUser.getPhone()) %>" maxlength="20">
            </label>

            <label>Vai trò
                <select class="form-control" name="roleId" required>
                    <option value="">Chọn vai trò</option>
                    <% if (roles != null) for (Role role : roles) { %>
                        <option value="<%= role.getId() %>" <%= editUser != null && editUser.getRoleId() == role.getId() ? "selected" : "" %>><%= h(role.getName()) %></option>
                    <% } %>
                </select>
            </label>

            <label>Trạng thái
                <select class="form-control" name="status" required>
                    <% String status = editUser == null || editUser.getStatus() == null ? "ACTIVE" : editUser.getStatus(); %>
                    <option value="ACTIVE" <%= selected(status, "ACTIVE") ? "selected" : "" %>>ACTIVE</option>
                    <option value="INACTIVE" <%= selected(status, "INACTIVE") ? "selected" : "" %>>INACTIVE</option>
                    <option value="LOCKED" <%= selected(status, "LOCKED") ? "selected" : "" %>>LOCKED</option>
                </select>
            </label>

            <div class="form-actions">
                <button class="button button-primary" type="submit"><%= editing ? "Lưu thay đổi" : "Tạo người dùng" %></button>
                <% if (editing) { %>
                    <a class="button button-secondary" href="${pageContext.request.contextPath}/admin/users">Hủy</a>
                <% } %>
            </div>
        </form>
    </section>

    <section class="panel">
        <div class="section-header">
            <h2>Danh sách người dùng</h2>
            <form class="search-form" method="get" action="${pageContext.request.contextPath}/admin/users">
                <input class="form-control" name="q" value="<%= h(request.getParameter("q")) %>" placeholder="Tìm theo tên hoặc email">
                <button class="button button-secondary" type="submit">Tìm</button>
            </form>
        </div>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Họ tên</th>
                    <th>Email</th>
                    <th>Điện thoại</th>
                    <th>Vai trò</th>
                    <th>Trạng thái</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <% if (users == null || users.isEmpty()) { %>
                    <tr><td colspan="7">Không có người dùng.</td></tr>
                <% } else for (User user : users) { %>
                    <tr>
                        <td><%= user.getUserId() %></td>
                        <td><%= h(user.getFullName()) %></td>
                        <td><%= h(user.getEmail()) %></td>
                        <td><%= h(user.getPhone()) %></td>
                        <td><%= h(user.getRoleName()) %></td>
                        <td><%= h(user.getStatus()) %></td>
                        <td><a href="${pageContext.request.contextPath}/admin/users?edit=<%= user.getUserId() %>">Sửa</a></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
