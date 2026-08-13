<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Role" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return value.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    List<Role> roles = (List<Role>) request.getAttribute("roles");
    Role editRole = (Role) request.getAttribute("editRole");
    boolean editing = editRole != null && editRole.getId() != null && editRole.getId() > 0;
    String action = editing ? "update" : "create";
    if (request.getAttribute("formAction") != null) action = request.getAttribute("formAction").toString();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý vai trò - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main id="main-content" class="page-container">
    <h1>Quản lý vai trò</h1>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= h(request.getAttribute("error")) %></div>
    <% } %>
    <% if ("created".equals(request.getAttribute("success"))) { %>
        <div class="alert alert-success">Đã tạo vai trò.</div>
    <% } else if ("updated".equals(request.getAttribute("success"))) { %>
        <div class="alert alert-success">Đã cập nhật vai trò.</div>
    <% } %>

    <section class="panel">
        <h2><%= editing ? "Cập nhật vai trò" : "Tạo vai trò" %></h2>
        <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/roles">
            <input type="hidden" name="action" value="<%= h(action) %>">
            <% if (editing || "update".equals(action)) { %>
                <input type="hidden" name="id" value="<%= editRole == null || editRole.getId() == null ? 0 : editRole.getId() %>">
            <% } %>
            <label>Tên vai trò
                <input class="form-control" name="name" value="<%= h(editRole == null ? "" : editRole.getName()) %>" required maxlength="40" placeholder="ADMIN">
            </label>
            <label>Mô tả
                <input class="form-control" name="description" value="<%= h(editRole == null ? "" : editRole.getDescription()) %>" maxlength="255">
            </label>
            <div class="form-actions">
                <button class="button button-primary" type="submit"><%= editing ? "Lưu thay đổi" : "Tạo vai trò" %></button>
                <% if (editing) { %>
                    <a class="button button-secondary" href="${pageContext.request.contextPath}/admin/roles">Hủy</a>
                <% } %>
            </div>
        </form>
    </section>

    <section class="panel">
        <h2>Danh sách vai trò</h2>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên</th>
                    <th>Mô tả</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <% if (roles == null || roles.isEmpty()) { %>
                    <tr><td colspan="4">Không có vai trò.</td></tr>
                <% } else for (Role role : roles) { %>
                    <tr>
                        <td><%= role.getId() %></td>
                        <td><%= h(role.getName()) %></td>
                        <td><%= h(role.getDescription()) %></td>
                        <td><a href="${pageContext.request.contextPath}/admin/roles?edit=<%= role.getId() %>">Sửa</a></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
