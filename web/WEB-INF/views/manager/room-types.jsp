<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.RoomType" %>
<%
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Loại Phòng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .management-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .data-table { width: 100%; border-collapse: collapse; margin-top: 15px; background: #fff; box-shadow: 0 4px 6px rgba(0,0,0,0.05); border-radius: 8px; overflow: hidden; }
        .data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid var(--color-border); }
        .data-table th { background: var(--color-bg-base); font-weight: 600; color: var(--color-text-secondary); }
        .data-table tr:hover { background: var(--color-bg-surface); }
        .badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .badge-active { background: var(--color-success-100); color: #166534; }
        .badge-inactive { background: var(--color-error-100); color: #991b1b; }
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
            <a href="${pageContext.request.contextPath}/manager/room-types" class="active">Loại Phòng</a>
            <a href="${pageContext.request.contextPath}/manager/rooms">Phòng Vật Lý</a>
        </div>
        
        <div class="management-header">
            <h2>Quản lý Loại Phòng</h2>
            <button class="button button-primary" onclick="openModal()">+ Thêm loại phòng</button>
        </div>
        
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên Loại Phòng</th>
                    <th>Sức Chứa</th>
                    <th>Giá Cơ Bản</th>
                    <th>Trạng Thái</th>
                    <th>Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <% if (roomTypes != null) {
                    for (RoomType rt : roomTypes) { %>
                <tr>
                    <td><%= rt.getId() %></td>
                    <td><strong><%= rt.getName() %></strong></td>
                    <td><%= rt.getCapacity() %> người</td>
                    <td><%= String.format("%,.0f", rt.getBasePrice()) %> VNĐ</td>
                    <td>
                        <span class="badge <%= "ACTIVE".equals(rt.getStatus()) ? "badge-active" : "badge-inactive" %>">
                            <%= "ACTIVE".equals(rt.getStatus()) ? "Hoạt động" : "Ngừng hoạt động" %>
                        </span>
                    </td>
                    <td>
                        <button class="button button-secondary" style="padding: 5px 10px; min-height: unset; font-size: 12px;" 
                                onclick="editRoomType(<%= rt.getId() %>, '<%= rt.getName() %>', '<%= rt.getDescription().replace("\n", "\\n").replace("'", "\\'") %>', <%= rt.getCapacity() %>, <%= rt.getBasePrice() %>, '<%= rt.getStatus() %>')">
                            Sửa
                        </button>
                        <a href="${pageContext.request.contextPath}/manager/room-types/delete?id=<%= rt.getId() %>" 
                           class="button button-secondary" style="padding: 5px 10px; min-height: unset; font-size: 12px; color: #991b1b; border-color: #fca5a5;"
                           onclick="return confirm('Bạn có chắc muốn xóa loại phòng này?');">Xóa</a>
                    </td>
                </tr>
                <%  }
                } %>
            </tbody>
        </table>
    </main>

    <div id="roomTypeModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">Thêm Loại Phòng Mới</h3>
                <button class="close-btn" onclick="closeModal()">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/manager/room-types/save" method="post">
                <input type="hidden" id="rtId" name="id" value="">
                
                <div class="form-group">
                    <label class="form-label" for="rtName">Tên Loại Phòng</label>
                    <input type="text" class="form-control" id="rtName" name="name" required>
                </div>
                
                <div class="form-group">
                    <label class="form-label" for="rtCapacity">Sức Chứa (người)</label>
                    <input type="number" class="form-control" id="rtCapacity" name="capacity" min="1" required>
                </div>
                
                <div class="form-group">
                    <label class="form-label" for="rtPrice">Giá Cơ Bản (VNĐ)</label>
                    <input type="number" class="form-control" id="rtPrice" name="basePrice" min="0" step="1000" required>
                </div>
                
                <div class="form-group">
                    <label class="form-label" for="rtDesc">Mô Tả</label>
                    <textarea class="form-control" id="rtDesc" name="description" rows="3"></textarea>
                </div>
                
                <div class="form-group">
                    <label class="form-label" for="rtStatus">Trạng Thái</label>
                    <select class="form-control" id="rtStatus" name="status">
                        <option value="ACTIVE">Hoạt động</option>
                        <option value="INACTIVE">Ngừng hoạt động</option>
                    </select>
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
            document.getElementById('rtId').value = '';
            document.getElementById('rtName').value = '';
            document.getElementById('rtDesc').value = '';
            document.getElementById('rtCapacity').value = '2';
            document.getElementById('rtPrice').value = '1000000';
            document.getElementById('rtStatus').value = 'ACTIVE';
            
            document.getElementById('modalTitle').innerText = 'Thêm Loại Phòng Mới';
            document.getElementById('roomTypeModal').classList.add('is-open');
        }

        function editRoomType(id, name, desc, capacity, price, status) {
            document.getElementById('rtId').value = id;
            document.getElementById('rtName').value = name;
            document.getElementById('rtDesc').value = desc;
            document.getElementById('rtCapacity').value = capacity;
            document.getElementById('rtPrice').value = price;
            document.getElementById('rtStatus').value = status;
            
            document.getElementById('modalTitle').innerText = 'Cập Nhật Loại Phòng';
            document.getElementById('roomTypeModal').classList.add('is-open');
        }

        function closeModal() {
            document.getElementById('roomTypeModal').classList.remove('is-open');
        }
    </script>
</body>
</html>
