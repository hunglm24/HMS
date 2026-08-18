<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Báo cáo sự cố | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/housekeeping.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <a class="hk-back" href="${pageContext.request.contextPath}/housekeeping/issues">← Quản lý sự cố</a>
    <section class="hk-detail-heading">
        <div><p class="hk-eyebrow">Vận hành phòng</p>
            <h1>Báo cáo sự cố mới</h1>
            <p>Ghi nhận thiết bị hỏng hóc hoặc sự cố cần bảo trì.</p></div>
    </section>
        
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger">${sessionScope.errorMessage}</div>
            <c:remove var="errorMessage" scope="session"/>
        </c:if>

        <form id="issue-report-form" class="hk-card hk-work-form" action="${pageContext.request.contextPath}/housekeeping/issues/report" method="post">
            <div class="hk-section-heading"><div><h2>Thông tin sự cố</h2>
                <p>Cung cấp đầy đủ thông tin để bộ phận Kỹ thuật xử lý.</p></div></div>

            <label class="hk-form-label"><span class="hk-label-title">Phòng gặp sự cố <span class="required">*</span></span>
                <select name="roomId" id="roomId" required>
                    <option value="">-- Chọn phòng --</option>
                    <c:forEach var="room" items="${rooms}">
                        <option value="${room.id}">P.${room.roomNumber} - ${room.roomTypeName}</option>
                    </c:forEach>
                </select>
            </label>
            
            <section class="hk-inspection-checklist">
                <div><h3>Báo cáo tình trạng thiết bị</h3>
                    <p>Lưu ý: Chỉ chọn trạng thái khác "Bình thường" đối với thiết bị bị hỏng hoặc thất lạc. Nếu là sự cố chung của phòng, hãy để trống trạng thái thiết bị và mô tả ở ô Ghi chú bên dưới.</p></div>
                <div id="equipmentListContainer">
                    <span style="color: var(--color-text-secondary); padding: 8px; display: block;">-- Chọn phòng để hiển thị thiết bị --</span>
                </div>
            </section>

            <label class="hk-form-label"><span class="hk-label-title">Mô tả chi tiết <span class="required">*</span></span>
                <textarea name="note" id="note" rows="5" required placeholder="Nhập mô tả sự cố (ví dụ: hỏng điều hòa, nước rò rỉ...)"></textarea>
            </label>
            
            <div class="hk-form-actions">
                <a href="${pageContext.request.contextPath}/housekeeping/issues">Quay lại</a>
                <button type="submit" class="hk-primary">Gửi báo cáo</button>
            </div>
        </form>
    </main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

<script>
document.getElementById('roomId').addEventListener('change', function() {
    var roomId = this.value;
    var container = document.getElementById('equipmentListContainer');
    if (!roomId) {
        container.innerHTML = '<span style="color: var(--color-text-secondary);">-- Chọn phòng trước --</span>';
        return;
    }
    
    container.innerHTML = '<span style="color: var(--color-text-secondary);">Đang tải thiết bị...</span>';
    
    fetch('${pageContext.request.contextPath}/housekeeping/issues/report?action=getEquipments&roomId=' + roomId)
        .then(response => response.text())
        .then(html => {
            container.innerHTML = html;
        })
        .catch(err => {
            container.innerHTML = '<span style="color: var(--color-error-600);">Lỗi tải thiết bị</span>';
        });
});
</script>
</body>
</html>
