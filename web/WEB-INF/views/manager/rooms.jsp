<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Room" %>
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

    private String statusLabel(String status) {
        if (status == null) return "Khong ro";
        switch (status) {
            case "AVAILABLE": return "Trống";
            case "OCCUPIED": return "Đang có khách";
            case "CLEANING": return "Đang dọn";
            case "MAINTENANCE": return "Bảo trì";
            case "NOT_READY": return "Chưa sẵn sàng";
            case "INSPECTION": return "Chờ kiểm tra";
            default: return status;
        }
    }

    private String statusClass(String status) {
        if (status == null) return "status-pending";
        switch (status) {
            case "AVAILABLE": return "status-available";
            case "OCCUPIED": return "status-occupied";
            case "CLEANING": return "status-cleaning";
            case "MAINTENANCE": return "status-maintenance";
            default: return "status-pending";
        }
    }
%>
<%
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
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
    <title>Quản lý phòng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">
    <nav class="tabs" aria-label="Room management">
        <a href="${pageContext.request.contextPath}/manager/room-types">Loại phòng</a>
        <a class="active" href="${pageContext.request.contextPath}/manager/rooms">Phòng vật lý</a>
    </nav>

    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>Quản lý phòng vật lý</h1>
            <p>Theo dõi số phòng, tầng, hạng phòng và trạng thái vận hành.</p>
        </div>
        <button type="button" onclick="openModal()">Thêm phòng</button>
    </section>

    <% if (toastMessage != null) { %>
        <div class="message <%= h(toastType) %>"><%= h(toastMessage) %></div>
    <% } %>

    <div class="placeholder-table">
        <table>
            <thead>
            <tr>
                <th>Số phòng</th>
                <th>Loại phòng</th>
                <th>Tầng</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (rooms != null && !rooms.isEmpty()) {
                for (Room r : rooms) { %>
                <tr>
                    <td><strong><%= h(r.getRoomNumber()) %></strong></td>
                    <td><%= h(r.getRoomTypeName()) %></td>
                    <td><%= r.getFloorNumber() == null ? "-" : "Tầng " + r.getFloorNumber() %></td>
                    <td><span class="status-chip <%= statusClass(r.getStatus()) %>"><%= statusLabel(r.getStatus()) %></span></td>
                    <td>
                        <div class="placeholder-actions">
                            <button type="button" class="btn btn-secondary"
                                    onclick="editRoom(<%= r.getId() %>, '<%= h(js(r.getRoomNumber())) %>', <%= r.getRoomTypeId() %>, '<%= r.getFloorNumber() == null ? "" : r.getFloorNumber() %>', '<%= h(js(r.getStatus())) %>', '<%= h(js(r.getDescription())) %>')">Sửa</button>
                            <a class="btn btn-link-danger" href="${pageContext.request.contextPath}/manager/rooms/delete?id=<%= r.getId() %>"
                               onclick="return confirm('Bạn có chắc muốn xóa phòng này?');">Xóa</a>
                        </div>
                    </td>
                </tr>
            <%  }
            } else { %>
                <tr><td colspan="5">Chưa có phòng nào.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</main>

<div id="roomModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="modalTitle">Thêm phòng</h3>
            <button class="close-btn" type="button" onclick="closeModal()">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/manager/rooms/save" method="post">
            <input type="hidden" id="rId" name="id" value="">
            <label for="rNumber">Số phòng</label>
            <input id="rNumber" name="roomNumber" required>

            <label for="rType">Loại phòng</label>
            <select id="rType" name="roomTypeId" required>
                <% if (roomTypes != null) {
                    for (RoomType rt : roomTypes) { %>
                    <option value="<%= rt.getId() %>"><%= h(rt.getName()) %></option>
                <%  }
                } %>
            </select>

            <label for="rFloor">Tầng</label>
            <input id="rFloor" name="floorNumber" type="number">

            <label for="rStatus">Trạng thái</label>
            <select id="rStatus" name="status">
                <option value="AVAILABLE">Trống</option>
                <option value="OCCUPIED">Đang có khách</option>
                <option value="CLEANING">Đang dọn</option>
                <option value="NOT_READY">Chưa sẵn sàng</option>
                <option value="INSPECTION">Chờ kiểm tra</option>
                <option value="MAINTENANCE">Bảo trì</option>
            </select>

            <label for="rDesc">Ghi chú</label>
            <textarea id="rDesc" name="description" rows="2"></textarea>

            <div class="placeholder-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Hủy</button>
                <button type="submit">Lưu</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openModal() {
        document.getElementById('rId').value = '';
        document.getElementById('rNumber').value = '';
        document.getElementById('rFloor').value = '';
        document.getElementById('rStatus').value = 'AVAILABLE';
        document.getElementById('rDesc').value = '';
        document.getElementById('modalTitle').innerText = 'Thêm phòng';
        document.getElementById('roomModal').classList.add('is-open');
    }

    function editRoom(id, number, typeId, floor, status, desc) {
        document.getElementById('rId').value = id;
        document.getElementById('rNumber').value = number;
        document.getElementById('rType').value = typeId;
        document.getElementById('rFloor').value = floor;
        document.getElementById('rStatus').value = status || 'AVAILABLE';
        document.getElementById('rDesc').value = desc;
        document.getElementById('modalTitle').innerText = 'Cập nhật phòng';
        document.getElementById('roomModal').classList.add('is-open');
    }

    function closeModal() {
        document.getElementById('roomModal').classList.remove('is-open');
    }
</script>
</body>
</html>
