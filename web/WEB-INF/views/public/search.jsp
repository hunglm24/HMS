<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tìm kiếm phòng - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1>Tìm kiếm phòng trống</h1>
        <form action="${pageContext.request.contextPath}/search" method="GET" class="form-container">
            <div class="form-group" style="display:flex; gap: 20px;">
                <div style="flex:1;">
                    <label for="checkIn">Ngày nhận phòng</label>
                    <input type="date" id="checkIn" name="checkIn" required style="width:100%;">
                </div>
                <div style="flex:1;">
                    <label for="checkOut">Ngày trả phòng</label>
                    <input type="date" id="checkOut" name="checkOut" required style="width:100%;">
                </div>
            </div>
            <div class="form-group" style="display:flex; gap: 20px; margin-top: 15px;">
                <div style="flex:1;">
                    <label for="guests">Số khách</label>
                    <input type="number" id="guests" name="guests" value="2" min="1" required style="width:100%;">
                </div>
                <div style="flex:1;">
                    <label for="rooms">Số phòng</label>
                    <input type="number" id="rooms" name="rooms" value="1" min="1" required style="width:100%;">
                </div>
            </div>
            <button type="submit" class="btn btn-primary" style="margin-top: 20px; width: 100%;">Tìm kiếm</button>
        </form>
    </main>
</body>
</html>
