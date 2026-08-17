<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.RoomType" %>
<%@ page import="java.util.List" %>
<%
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tạo Đơn tại quầy (Walk-in) - HMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css">
    <style>
        .walkin-container { display: flex; gap: 30px; margin-top: 20px; }
        .walkin-form { flex: 0 0 350px; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background: #f9f9f9; }
        .walkin-results { flex: 1; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input, .form-group select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        .room-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px; }
        .room-card { border: 2px solid #28a745; padding: 15px; border-radius: 8px; text-align: center; cursor: pointer; transition: all 0.2s; background: white; }
        .room-card:hover { transform: translateY(-3px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .room-card h3 { margin: 0 0 10px 0; color: #28a745; }
        .room-card .type { font-weight: bold; color: #555; }
        .room-card .capacity { font-size: 0.9em; color: #888; margin-bottom: 10px; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1>Tạo Đơn tại quầy (Walk-in)</h1>
        
        <div class="walkin-container">
            <aside class="walkin-form">
                <h3>Kiểm tra phòng trống</h3>
                <form id="checkAvailabilityForm">
                    <div class="form-group">
                        <label>Ngày nhận phòng</label>
                        <input type="date" id="checkIn" name="checkIn" required>
                    </div>
                    <div class="form-group">
                        <label>Ngày trả phòng</label>
                        <input type="date" id="checkOut" name="checkOut" required>
                    </div>
                    <div class="form-group">
                        <label>Loại phòng</label>
                        <select id="roomTypeId" name="roomTypeId">
                            <option value="">Tất cả loại phòng</option>
                            <% if (roomTypes != null) { 
                                for (RoomType rt : roomTypes) { %>
                                    <option value="<%= rt.getId() %>"><%= rt.getName() %> (Tối đa <%= rt.getCapacity() %> khách)</option>
                            <%  } 
                               } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Số lượng khách</label>
                        <input type="number" id="guests" name="guests" min="1" value="1" required>
                        <small style="color:#d9534f; display:none;" id="guestWarning">Lưu ý: Không được vượt quá sức chứa mặc định của phòng.</small>
                    </div>
                    
                    <button type="button" class="btn btn-primary" style="width:100%; padding:10px;" onclick="searchAvailableRooms()">Tìm phòng</button>
                </form>
            </aside>
            
            <section class="walkin-results">
                <h2>Kết quả</h2>
                <div id="loadingMsg" style="display:none;">Đang tìm phòng...</div>
                <div id="errorMsg" style="color:red; display:none;"></div>
                <div id="resultsContainer" class="room-grid">
                    <!-- Results will be loaded here via AJAX -->
                    <p style="color:#666;">Vui lòng nhập thông tin và bấm Tìm phòng.</p>
                </div>
            </section>
        </div>
    </main>
    
    <script>
        // Default dates to today and tomorrow
        document.getElementById('checkIn').valueAsDate = new Date();
        let tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('checkOut').valueAsDate = tomorrow;

        function searchAvailableRooms() {
            const checkIn = document.getElementById('checkIn').value;
            const checkOut = document.getElementById('checkOut').value;
            const roomTypeId = document.getElementById('roomTypeId').value;
            const guests = document.getElementById('guests').value;
            
            if (!checkIn || !checkOut) {
                alert("Vui lòng chọn ngày nhận và ngày trả phòng.");
                return;
            }
            
            if (new Date(checkIn) >= new Date(checkOut)) {
                alert("Ngày trả phòng phải sau ngày nhận phòng.");
                return;
            }

            document.getElementById('loadingMsg').style.display = 'block';
            document.getElementById('errorMsg').style.display = 'none';
            document.getElementById('resultsContainer').innerHTML = '';
            
            let url = '<%= request.getContextPath() %>/api/available-rooms?checkIn=' + checkIn + '&checkOut=' + checkOut + '&guests=' + guests;
            if (roomTypeId) {
                url += '&roomTypeId=' + roomTypeId;
            }
            
            fetch(url)
                .then(response => {
                    if (!response.ok) throw new Error("API response error");
                    return response.json();
                })
                .then(data => {
                    document.getElementById('loadingMsg').style.display = 'none';
                    const container = document.getElementById('resultsContainer');
                    
                    if (data.length === 0) {
                        container.innerHTML = '<p style="color:red;">Không có phòng nào trống phù hợp với yêu cầu (Lưu ý: Số khách không được vượt quá sức chứa phòng).</p>';
                        return;
                    }
                    
                    data.forEach(room => {
                        const div = document.createElement('div');
                        div.className = 'room-card';
                        div.innerHTML = `
                            <h3>Phòng ` + room.roomNumber + `</h3>
                            <div class="type">` + room.roomTypeName + `</div>
                            <div class="capacity">` + room.description + `</div>
                            <button class="btn btn-sm btn-primary" onclick="selectRoom(` + room.id + `, '` + room.roomNumber + `')">Chọn & Tạo đơn</button>
                        `;
                        container.appendChild(div);
                    });
                })
                .catch(error => {
                    document.getElementById('loadingMsg').style.display = 'none';
                    document.getElementById('errorMsg').style.display = 'block';
                    document.getElementById('errorMsg').innerText = "Đã xảy ra lỗi khi tìm kiếm phòng: " + error.message;
                });
        }
        
        function selectRoom(roomId, roomNumber) {
            // Forward to booking creation or checkout process
            // Passing the selected roomId and dates
            const checkIn = document.getElementById('checkIn').value;
            const checkOut = document.getElementById('checkOut').value;
            // Since this is a simple implementation, we might redirect to ReceptionBookingServlet 
            // to fill in guest details. For now, we alert.
            alert("Đã chọn phòng: " + roomNumber + ". Chuyển sang trang tạo đơn...");
            window.location.href = '<%= request.getContextPath() %>/reception/booking-checkout?roomId=' + roomId + '&checkIn=' + checkIn + '&checkOut=' + checkOut;
        }
    </script>
</body>
</html>
