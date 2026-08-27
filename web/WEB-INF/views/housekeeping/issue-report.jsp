<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    Boolean isMgrAttr = (Boolean) request.getAttribute("isManager");
    boolean isManager = Boolean.TRUE.equals(isMgrAttr);
    String cp = request.getContextPath();
    String backUrl = isManager ? cp + "/manager/issues" : cp + "/housekeeping/issues";
    String formAction = isManager ? cp + "/manager/issues/report" : cp + "/housekeeping/issues/report";
    String fetchUrl = (isManager ? cp + "/manager/issues/report" : cp + "/housekeeping/issues/report") + "?action=getEquipments&roomId=";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Báo Cáo Sự Cố | HMS</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="<%= cp %>/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/housekeeping.css?v=20260825-1">
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="page-container hk-page">
    <a class="hk-back" href="<%= backUrl %>">← <%= isManager ? "Quản lý sự cố" : "Danh sách sự cố" %></a>
    <section class="hk-detail-heading">
        <div>
            <p class="hk-eyebrow"><%= isManager ? "Quản lý khách sạn" : "Vận hành phòng" %></p>
            <h1>Báo cáo sự cố mới</h1>
            <p>Ghi nhận thiết bị hỏng hóc hoặc sự cố cần bảo trì, sửa chữa.</p>
        </div>
    </section>

    <form id="issue-report-form" class="hk-card hk-work-form" action="<%= formAction %>" method="post">
        <div class="hk-section-heading">
            <div>
                <h2>Thông tin sự cố</h2>
                <p>Cung cấp đầy đủ thông tin để bộ phận Kỹ thuật / Dọn phòng xử lý.</p>
            </div>
        </div>

        <label class="hk-form-label">
            <span class="hk-label-title">Phòng gặp sự cố <span class="required">*</span></span>
            <select name="roomId" id="roomId" data-fetch-url="<%= fetchUrl %>" required>
                <option value="">-- Chọn phòng --</option>
                <c:forEach var="room" items="${rooms}">
                    <option value="${room.id}" ${room.id == preselectedRoomId ? 'selected' : ''}>P.${room.roomNumber} - ${room.roomTypeName}</option>
                </c:forEach>
            </select>
        </label>
        
        <section class="hk-inspection-checklist">
            <div>
                <h3>Báo cáo tình trạng thiết bị</h3>
                <p>Lưu ý: Chỉ chọn trạng thái khác "Bình thường" đối với thiết bị bị hỏng hoặc thất lạc. Nếu là sự cố chung của phòng, hãy để trống trạng thái thiết bị và mô tả ở ô Ghi chú bên dưới.</p>
            </div>
            <div id="equipmentListContainer">
                <span class="text-secondary">-- Chọn phòng để hiển thị thiết bị --</span>
            </div>
        </section>

        <label class="hk-form-label">
            <span class="hk-label-title">Mô tả chi tiết <span class="required">*</span></span>
            <textarea name="note" id="note" rows="5" required placeholder="Nhập mô tả sự cố (ví dụ: hỏng điều hòa, nước rò rỉ, bóng đèn chập...)"></textarea>
        </label>
        
        <div class="hk-form-actions">
            <a class="btn btn-secondary" href="<%= backUrl %>">Quay lại</a>
            <button type="submit" class="btn btn-primary">Gửi báo cáo sự cố</button>
        </div>
    </form>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
<script src="<%= cp %>/assets/js/issue-report.js?v=20260825-1"></script>
</body>
</html>
