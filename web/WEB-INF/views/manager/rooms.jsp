<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Room" %>
<%@ page import="model.RoomType" %>
<%
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Phòng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .management-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .data-table { width: 100%; border-collapse: collapse; margin-top: 15px; background: #fff; box-shadow: 0 4px 6px rgba(0,0,0,0.05); border-radius: 8px; overflow: hidden; }
        .data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid var(--color-border); }
        .data-table th { background: var(--color-bg-base); font-weight: 600; color: var(--color-text-secondary); }
        .data-table tr:hover { background: var(--color-bg-surface); }
        .badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: 600; display: inline-block; }
        .badge-available { background: var(--color-success-100); color: #166534; }
        .badge-occupied { background: var(--color-primary-100); color: var(--color-primary-600); }
        .badge-cleaning { background: #fef3c7; color: #b45309; }
        .badge-maintenance { background: var(--color-error-100); color: #991b1b; }
        .badge-default { background: #e5e7eb; color: #374151; }
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center; }
        .modal.is-open { display: flex; }
        .modal-content { background: #fff; padding: 24px; border-radius: 8px; width: 500px; max-width: 90%; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        .modal-header h3 { margin: 0; }
        .close-btn { cursor: pointer; font-size: 20px; border: none; background: none; }
        .form-group { margin-bottom: 15px; }
        .actions { margin-top: 20px; display: flex; justify-content: flex-end; gap: 10px; }
        .nav-tabs { display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 1px solid var(--color-border); padding-bottom: 10px; }
        .nav-tabs a { padding: 8px 16px; text-decoration: none; color: var(--color-text-secondary); font-weight: 600; border-radius: 4px; }
        .nav-tabs a.active { background: var(--color-primary-100); color: var(--color-primary-600); }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <div class="nav-tabs">
            <a href="${pageContext.request.contextPath}/manager/room-types">Loại Phòng</a>
            <a href="${pageContext.request.contextPath}/manager/rooms" class="active">Phòng Vật Lý</a>
        </div>
        
        <div class="management-header">
            <h2>Quản lý Phòng Vật Lý</h2>
            <button class="button button-primary" onclick="openModal()">+ Thêm phòng</button>
        </div>
        
        <table class="data-table">
            <thead>
                <tr>
                    <th>Số Phòng</th>
                    <th>Loại Phòng</th>
                    <th>Tầng</th>
                    <th>Trạng Thái</th>
                    <th>Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <% if (rooms != null) {
                    for (Room r : rooms) {
                        String statusClass = "badge-default";
                        String statusText = r.getStatus();
                        switch (r.getStatus()) {
                            case "AVAILABLE": statusClass = "badge-available"; statusText = "Trống"; break;
                            case "OCCUPIED": statusClass = "badge-occupied"; statusText = "Đang có khách"; break;
                            case "CLEANING": statusClass = "badge-cleaning"; statusText = "Đang dọn"; break;
                            case "MAINTENANCE": statusClass = "badge-maintenance"; statusText = "Bảo trì"; break;
                            case "NOT_READY": statusClass = "badge-default"; statusText = "Chưa sẵn sàng"; break;
                            case "INSPECTION": statusClass = "badge-default"; statusText = "Chờ kiểm tra"; break;
                        }
                %>
                <tr>
                    <td><strong><%= r.getRoomNumber() %></strong></td>
                    <td><%= r.getRoomTypeName() %></td>
                    <td><%= r.getFloorNumber() != null ? "Tầng " + r.getFloorNumber() : "-" %></td>
                    <td><span class="badge <%= statusClass %>"><%= statusText %></span></td>
                    <td>
                        <button class="button button-secondary" style="padding: 5px 10px; min-height: unset; font-size: 12px;" 
                                onclick="editRoom(<%= r.getId() %>, '<%= r.getRoomNumber() %>', <%= r.getRoomTypeId() %>, '<%= r.getFloorNumber() != null ? r.getFloorNumber() : "" %>', '<%= r.getStatus() %>', '<%= r.getDescription() == null ? "" : r.getDescription().replace("\n", "\\n").replace("'", "\\'") %>')">
                            Sửa
                        </button>
                        <a href="${pageContext.request.contextPath}/manager/rooms/delete?id=<%= r.getId() %>" 
                           class="button button-secondary" style="padding: 5px 10px; min-height: unset; font-size: 12px; color: #991b1b; border-color: #fca5a5;"
                           onclick="return confirm('Bạn có chắc muốn xóa phòng này?');">Xóa</a>
                    </td>
                </tr>
                <%  }
                } %>
            </tbody>
        </table>
    </main>

    <div id="roomModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">Thêm Phòng Mới</h3>
                <button class="close-btn" onclick="closeModal()">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/manager/rooms/save" method="post">
                <input type="hidden" id="rId" name="id" value="">
                
                <div class="form-group">
                    <label class="form-label" for="rNumber">Số Phòng</label>
                    <input type="text" class="form-control" id="rNumber" name="roomNumber" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="rType">Loại Phòng</label>
                    <select class="form-control" id="rType" name="roomTypeId" required>
                        <% if (roomTypes != null) {
                            for (RoomType rt : roomTypes) { %>
                                <option value="<%= rt.getId() %>"><%= rt.getName() %></option>
                        <%  }
                        } %>
                    </select>
                </div>
                
                <div class="form-group">
                    <label class="form-label" for="rFloor">Tầng (tùy chọn)</label>
                    <input type="number" class="form-control" id="rFloor" name="floorNumber">
                </div>
                
                <div class="form-group">
                    <label class="form-label" for="rStatus">Trạng Thái</label>
                    <select class="form-control" id="rStatus" name="status">
                        <option value="AVAILABLE">Trống</option>
                        <option value="OCCUPIED">Đang có khách</option>
                        <option value="CLEANING">Đang dọn</option>
                        <option value="NOT_READY">Chưa sẵn sàng</option>
                        <option value="INSPECTION">Chờ kiểm tra</option>
                        <option value="MAINTENANCE">Bảo trì</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label" for="rDesc">Ghi chú</label>
                    <textarea class="form-control" id="rDesc" name="description" rows="2"></textarea>
                </div>
                
                <div class="actions">
                    <button type="button" class="button button-secondary" onclick="closeModal()">Hủy</button>
                    <button type="submit" class="button button-primary">Lưu</button>
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
            
            document.getElementById('modalTitle').innerText = 'Thêm Phòng Mới';
            document.getElementById('roomModal').classList.add('is-open');
        }

        function editRoom(id, number, typeId, floor, status, desc) {
            document.getElementById('rId').value = id;
            document.getElementById('rNumber').value = number;
            document.getElementById('rType').value = typeId;
            document.getElementById('rFloor').value = floor;
            document.getElementById('rStatus').value = status;
            document.getElementById('rDesc').value = desc;
            
            document.getElementById('modalTitle').innerText = 'Cập Nhật Phòng';
            document.getElementById('roomModal').classList.add('is-open');
        }

        function closeModal() {
            document.getElementById('roomModal').classList.remove('is-open');
        }
    </script>
</body>
</html>
