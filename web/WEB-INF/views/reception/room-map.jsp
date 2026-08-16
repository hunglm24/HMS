<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Room" %>
<%
    Map<Integer, List<Room>> roomsByFloor = (Map<Integer, List<Room>>) request.getAttribute("roomsByFloor");
    Long availableCount = (Long) request.getAttribute("availableCount");
    Long occupiedCount = (Long) request.getAttribute("occupiedCount");
    Long cleaningCount = (Long) request.getAttribute("cleaningCount");
    Long maintenanceCount = (Long) request.getAttribute("maintenanceCount");
    Integer totalCount = (Integer) request.getAttribute("totalCount");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sơ đồ phòng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .map-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
        .stats-row { display: flex; gap: 15px; margin-top: 15px; }
        .stat-badge { padding: 8px 16px; border-radius: 20px; font-weight: 600; font-size: 14px; border: 1px solid var(--color-border); }
        .stat-available { background: var(--color-success-100); color: #166534; border-color: #bbf7d0; }
        .stat-occupied { background: var(--color-primary-100); color: var(--color-primary-600); border-color: #bfdbfe; }
        .stat-cleaning { background: #fef3c7; color: #b45309; border-color: #fde68a; }
        .stat-maintenance { background: var(--color-error-100); color: #991b1b; border-color: #fca5a5; }
        
        .floor-section { margin-bottom: 30px; }
        .floor-title { margin-bottom: 15px; padding-bottom: 8px; border-bottom: 2px solid var(--color-border); color: var(--color-text-secondary); }
        
        .room-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
        .room-card { border-radius: 12px; padding: 16px; border: 2px solid transparent; display: flex; flex-direction: column; cursor: pointer; transition: transform 0.2s; }
        .room-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        
        .room-card.AVAILABLE { background: var(--color-success-100); border-color: #bbf7d0; }
        .room-card.OCCUPIED { background: var(--color-primary-100); border-color: #bfdbfe; }
        .room-card.CLEANING { background: #fef3c7; border-color: #fde68a; }
        .room-card.MAINTENANCE { background: var(--color-error-100); border-color: #fca5a5; }
        .room-card.NOT_READY, .room-card.INSPECTION { background: #f3f4f6; border-color: #e5e7eb; }
        
        .room-number { font-size: 24px; font-weight: 800; margin-bottom: 4px; color: var(--color-text-primary); }
        .room-type { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); margin-bottom: 12px; }
        
        .room-status { font-size: 12px; font-weight: 700; text-transform: uppercase; margin-top: auto; padding-top: 12px; border-top: 1px solid rgba(0,0,0,0.05); }
        .AVAILABLE .room-status { color: #166534; }
        .OCCUPIED .room-status { color: var(--color-primary-600); }
        .CLEANING .room-status { color: #b45309; }
        .MAINTENANCE .room-status { color: #991b1b; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        
        <div class="map-header">
            <div>
                <h2>Sơ đồ phòng</h2>
                <div class="stats-row">
                    <div class="stat-badge stat-available">Trống: <%= availableCount %></div>
                    <div class="stat-badge stat-occupied">Đang có khách: <%= occupiedCount %></div>
                    <div class="stat-badge stat-cleaning">Đang dọn: <%= cleaningCount %></div>
                    <div class="stat-badge stat-maintenance">Bảo trì: <%= maintenanceCount %></div>
                    <div class="stat-badge">Tổng: <%= totalCount %></div>
                </div>
            </div>
            <div>
                <!-- Add filters if needed in the future -->
            </div>
        </div>

        <% if (roomsByFloor != null && !roomsByFloor.isEmpty()) { 
            for (Map.Entry<Integer, List<Room>> entry : roomsByFloor.entrySet()) {
                Integer floor = entry.getKey();
                List<Room> floorRooms = entry.getValue();
        %>
            <div class="floor-section">
                <h3 class="floor-title"><%= floor == 0 ? "Không rõ tầng" : "Tầng " + floor %></h3>
                <div class="room-grid">
                    <% for (Room r : floorRooms) { 
                        String statusLabel = "Không rõ";
                        switch (r.getStatus()) {
                            case "AVAILABLE": statusLabel = "Trống"; break;
                            case "OCCUPIED": statusLabel = "Có khách"; break;
                            case "CLEANING": statusLabel = "Đang dọn"; break;
                            case "MAINTENANCE": statusLabel = "Bảo trì"; break;
                            case "NOT_READY": statusLabel = "Chưa sẵn sàng"; break;
                            case "INSPECTION": statusLabel = "Chờ kiểm tra"; break;
                        }
                    %>
                    <div class="room-card <%= r.getStatus() %>" onclick="alert('Phòng <%= r.getRoomNumber() %> - Trạng thái: <%= statusLabel %>')">
                        <div class="room-number"><%= r.getRoomNumber() %></div>
                        <div class="room-type"><%= r.getRoomTypeName() %></div>
                        <div class="room-status"><%= statusLabel %></div>
                    </div>
                    <% } %>
                </div>
            </div>
        <%  }
        } else { %>
            <p>Không có phòng nào trong hệ thống.</p>
        <% } %>
    </main>
</body>
</html>
