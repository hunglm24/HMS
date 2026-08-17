<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Room" %>
<%@ page import="model.RoomType" %>
<%@ page import="java.util.List" %>
<%
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");
    
    Long statAvailable = (Long) request.getAttribute("statAvailable");
    Long statOccupied = (Long) request.getAttribute("statOccupied");
    Long statCleaning = (Long) request.getAttribute("statCleaning");
    Long statMaintenance = (Long) request.getAttribute("statMaintenance");
    Integer statTotal = (Integer) request.getAttribute("statTotal");
    
    String currentFloor = (String) request.getAttribute("currentFloor");
    String currentRoomType = (String) request.getAttribute("currentRoomType");
    String currentStatus = (String) request.getAttribute("currentStatus");
    if (currentStatus == null) currentStatus = "ALL";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sơ đồ Phòng - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        .stats-container { display: flex; gap: 15px; margin-bottom: 20px; }
        .stat-card { flex: 1; padding: 15px; border-radius: 8px; text-align: center; color: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .stat-card h3 { margin: 0; font-size: 2em; }
        .stat-card p { margin: 5px 0 0 0; font-weight: bold; }
        .stat-available { background: #28a745; }
        .stat-occupied { background: #dc3545; }
        .stat-cleaning { background: #ffc107; color: #333 !important; }
        .stat-maintenance { background: #6c757d; }
        .stat-total { background: #007bff; }
        
        .filters { background: #f9f9f9; padding: 15px; border-radius: 8px; border: 1px solid #ddd; margin-bottom: 20px; display: flex; gap: 15px; align-items: flex-end; }
        .filter-group { display: flex; flex-direction: column; flex: 1; }
        .filter-group label { margin-bottom: 5px; font-weight: bold; }
        .filter-group select { padding: 8px; border-radius: 4px; border: 1px solid #ccc; }
        
        .room-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 15px; }
        .room-box { border-radius: 8px; padding: 20px 10px; text-align: center; color: white; cursor: pointer; transition: transform 0.2s; position: relative; }
        .room-box:hover { transform: scale(1.05); }
        .room-box h2 { margin: 0; font-size: 1.5em; }
        .room-box .type { font-size: 0.85em; margin-top: 5px; opacity: 0.9; }
        .room-box.AVAILABLE { background: #28a745; }
        .room-box.OCCUPIED { background: #dc3545; }
        .room-box.CLEANING { background: #ffc107; color: #333; }
        .room-box.MAINTENANCE { background: #6c757d; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container" style="max-width: 1400px;">
        <h1>Sơ đồ Phòng</h1>
        
        <div class="stats-container">
            <div class="stat-card stat-total">
                <h3><%= statTotal %></h3>
                <p>Tổng số phòng</p>
            </div>
            <div class="stat-card stat-available">
                <h3><%= statAvailable %></h3>
                <p>Trống (AVAILABLE)</p>
            </div>
            <div class="stat-card stat-occupied">
                <h3><%= statOccupied %></h3>
                <p>Có khách (OCCUPIED)</p>
            </div>
            <div class="stat-card stat-cleaning">
                <h3><%= statCleaning %></h3>
                <p>Đang dọn (CLEANING)</p>
            </div>
            <div class="stat-card stat-maintenance">
                <h3><%= statMaintenance %></h3>
                <p>Bảo trì (MAINTENANCE)</p>
            </div>
        </div>
        
        <form class="filters" action="<%= request.getContextPath() %>/reception/room-map" method="GET">
            <div class="filter-group">
                <label>Trạng thái</label>
                <select name="status" onchange="this.form.submit()">
                    <option value="ALL">Tất cả</option>
                    <option value="AVAILABLE" <%= "AVAILABLE".equals(currentStatus) ? "selected" : "" %>>Trống</option>
                    <option value="OCCUPIED" <%= "OCCUPIED".equals(currentStatus) ? "selected" : "" %>>Có khách</option>
                    <option value="CLEANING" <%= "CLEANING".equals(currentStatus) ? "selected" : "" %>>Đang dọn</option>
                    <option value="MAINTENANCE" <%= "MAINTENANCE".equals(currentStatus) ? "selected" : "" %>>Bảo trì</option>
                </select>
            </div>
            <div class="filter-group">
                <label>Tầng</label>
                <select name="floor" onchange="this.form.submit()">
                    <option value="">Tất cả các tầng</option>
                    <% for (int i = 1; i <= 10; i++) { %>
                        <option value="<%= i %>" <%= String.valueOf(i).equals(currentFloor) ? "selected" : "" %>>Tầng <%= i %></option>
                    <% } %>
                </select>
            </div>
            <div class="filter-group">
                <label>Loại phòng</label>
                <select name="roomType" onchange="this.form.submit()">
                    <option value="">Tất cả loại phòng</option>
                    <% if (roomTypes != null) { 
                        for (RoomType rt : roomTypes) { %>
                            <option value="<%= rt.getId() %>" <%= String.valueOf(rt.getId()).equals(currentRoomType) ? "selected" : "" %>><%= rt.getName() %></option>
                    <%  } 
                       } %>
                </select>
            </div>
            <div class="filter-group" style="flex: 0 0 auto;">
                <button type="submit" class="btn btn-primary" style="height: 38px;">Lọc</button>
            </div>
        </form>
        
        <div class="room-grid">
            <% if (rooms != null && !rooms.isEmpty()) { 
                   for (Room r : rooms) { %>
                    <div class="room-box <%= r.getStatus() %>" title="<%= r.getDescription() %>">
                        <h2><%= r.getRoomNumber() %></h2>
                        <div class="type"><%= r.getRoomTypeName() %></div>
                    </div>
            <%     }
               } else { %>
                <div style="grid-column: 1 / -1; text-align: center; padding: 40px; background: #f9f9f9; border-radius: 8px;">
                    <p>Không có phòng nào phù hợp với bộ lọc.</p>
                </div>
            <% } %>
        </div>
        
    </main>
</body>
</html>
