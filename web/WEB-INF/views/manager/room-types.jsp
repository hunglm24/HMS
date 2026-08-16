<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.RoomType" %>
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
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");
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
    <title>Quản lý loại phòng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">
    <nav class="tabs" aria-label="Room management">
        <a class="active" href="${pageContext.request.contextPath}/manager/room-types">Loại phòng</a>
        <a href="${pageContext.request.contextPath}/manager/rooms">Phòng vật lý</a>
    </nav>

    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>Quản lý loại phòng</h1>
            <p>Quản lý tên hạng phòng, sức chứa, giá cơ bản và trạng thái kinh doanh.</p>
        </div>
        <button type="button" onclick="openModal()">Thêm loại phòng</button>
    </section>

    <% if (toastMessage != null) { %>
        <div class="message <%= h(toastType) %>"><%= h(toastMessage) %></div>
    <% } %>

    <div class="placeholder-table">
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Loại phòng</th>
                <th>Sức chứa</th>
                <th>Giá cơ bản</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (roomTypes != null && !roomTypes.isEmpty()) {
                for (RoomType rt : roomTypes) { %>
                <tr>
                    <td><%= rt.getId() %></td>
                    <td>
                        <strong><%= h(rt.getName()) %></strong><br>
                        <small><%= h(rt.getDescription()) %></small>
                    </td>
                    <td><%= rt.getCapacity() %> người</td>
                    <td><%= rt.getBasePrice() == null ? "0" : String.format("%,.0f", rt.getBasePrice()) %> VND</td>
                    <td>
                        <span class="status-chip <%= "ACTIVE".equals(rt.getStatus()) ? "status-paid" : "status-cancelled" %>">
                            <%= "ACTIVE".equals(rt.getStatus()) ? "Hoạt động" : "Ngừng hoạt động" %>
                        </span>
                    </td>
                    <td>
                        <div class="placeholder-actions">
                            <button type="button" class="btn btn-secondary"
                                    onclick="editRoomType(<%= rt.getId() %>, '<%= h(js(rt.getName())) %>', '<%= h(js(rt.getDescription())) %>', <%= rt.getCapacity() %>, <%= rt.getBasePrice() == null ? "0" : rt.getBasePrice().toPlainString() %>, '<%= h(js(rt.getStatus())) %>')">Sửa</button>
                            <a class="btn btn-link-danger" href="${pageContext.request.contextPath}/manager/room-types/delete?id=<%= rt.getId() %>"
                               onclick="return confirm('Bạn có chắc muốn xóa loại phòng này?');">Xóa</a>
                        </div>
                    </td>
                </tr>
            <%  }
            } else { %>
                <tr><td colspan="6">Chưa có loại phòng nào.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</main>

<div id="roomTypeModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="modalTitle">Thêm loại phòng</h3>
            <button class="close-btn" type="button" onclick="closeModal()">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/manager/room-types/save" method="post">
            <input type="hidden" id="rtId" name="id" value="">
            <label for="rtName">Tên loại phòng</label>
            <input id="rtName" name="name" required>

            <label for="rtCapacity">Sức chứa</label>
            <input id="rtCapacity" name="capacity" type="number" min="1" required>

            <label for="rtPrice">Giá cơ bản</label>
            <input id="rtPrice" name="basePrice" type="number" min="0" step="1000" required>

            <label for="rtDesc">Mô tả</label>
            <textarea id="rtDesc" name="description" rows="3"></textarea>

            <label for="rtStatus">Trạng thái</label>
            <select id="rtStatus" name="status">
                <option value="ACTIVE">Hoạt động</option>
                <option value="INACTIVE">Ngừng hoạt động</option>
            </select>

            <div class="placeholder-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Hủy</button>
                <button type="submit">Lưu</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openModal() {
        document.getElementById('rtId').value = '';
        document.getElementById('rtName').value = '';
        document.getElementById('rtDesc').value = '';
        document.getElementById('rtCapacity').value = '2';
        document.getElementById('rtPrice').value = '1000000';
        document.getElementById('rtStatus').value = 'ACTIVE';
        document.getElementById('modalTitle').innerText = 'Thêm loại phòng';
        document.getElementById('roomTypeModal').classList.add('is-open');
    }

    function editRoomType(id, name, desc, capacity, price, status) {
        document.getElementById('rtId').value = id;
        document.getElementById('rtName').value = name;
        document.getElementById('rtDesc').value = desc;
        document.getElementById('rtCapacity').value = capacity;
        document.getElementById('rtPrice').value = price;
        document.getElementById('rtStatus').value = status || 'ACTIVE';
        document.getElementById('modalTitle').innerText = 'Cập nhật loại phòng';
        document.getElementById('roomTypeModal').classList.add('is-open');
    }

    function closeModal() {
        document.getElementById('roomTypeModal').classList.remove('is-open');
    }
</script>
</body>
</html>
