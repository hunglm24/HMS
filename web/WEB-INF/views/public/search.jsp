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
            <div class="form-group">
                <label for="checkIn">Ngày nhận phòng</label>
                <input type="date" id="checkIn" name="checkIn" required>
            </div>
            <div class="form-group">
                <label for="checkOut">Ngày trả phòng</label>
                <input type="date" id="checkOut" name="checkOut" required>
            </div>
            <button type="submit" class="btn btn-primary">Tìm kiếm</button>
        </form>
    </main>
</body>
</html>
