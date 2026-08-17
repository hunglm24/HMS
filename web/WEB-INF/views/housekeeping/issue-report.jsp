<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Báo cáo sự cố - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .form-panel { max-width: 600px; margin: 0 auto; }
        .form-group { margin-bottom: 16px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
        .form-control { width: 100%; padding: 10px; border: 1px solid var(--color-border); border-radius: 4px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="app-shell">
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />
    <main>
        <h1>Báo cáo sự cố</h1>
        
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger">${sessionScope.errorMessage}</div>
            <c:remove var="errorMessage" scope="session"/>
        </c:if>

        <div class="form-panel">
            <form action="${pageContext.request.contextPath}/housekeeping/issues/report" method="post">
                <div class="form-group">
                    <label for="roomId">Phòng gặp sự cố <span style="color:red">*</span></label>
                    <select name="roomId" id="roomId" class="form-control" required>
                        <option value="">-- Chọn phòng --</option>
                        <c:forEach var="room" items="${rooms}">
                            <option value="${room.id}">P.${room.roomNumber} - ${room.roomTypeName}</option>
                        </c:forEach>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="roomEquipmentId">Thiết bị hỏng (Nếu có)</label>
                    <input type="number" name="roomEquipmentId" id="roomEquipmentId" class="form-control" placeholder="Mã thiết bị (tùy chọn)">
                    <small><i>Lưu ý: Nếu sự cố chung của phòng, có thể bỏ trống phần này.</i></small>
                </div>

                <div class="form-group">
                    <label for="note">Mô tả sự cố <span style="color:red">*</span></label>
                    <textarea name="note" id="note" rows="5" class="form-control" required placeholder="Nhập mô tả sự cố (ví dụ: hỏng điều hòa, nước rò rỉ...)"></textarea>
                </div>
                
                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/housekeeping/issues" class="btn btn-secondary">Hủy</a>
                    <button type="submit" class="btn btn-primary">Gửi báo cáo</button>
                </div>
            </form>
        </div>
    </main>
</div>
</body>
</html>
