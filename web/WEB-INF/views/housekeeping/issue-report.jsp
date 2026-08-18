<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Báo cáo sự cố - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .form-panel { max-width: 600px; margin: 0 auto; }
        .form-group { margin-bottom: 20px; }
        .form-group > label { display: block; margin-bottom: 8px; font-weight: 500; }
        .form-control { width: 100%; padding: 10px; border: 1px solid var(--color-border); border-radius: var(--radius-md); font-family: inherit; }
        
        .equipment-list {
            padding: 8px;
            background: var(--color-bg-surface);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
        }
        
        .equipment-list:empty::after {
            content: "Không có dữ liệu";
            color: var(--color-text-secondary);
            padding: 8px;
            display: block;
        }
        
        .eq-info { flex: 1; }
        .eq-name { display: block; font-weight: 600; font-size: 14px; color: var(--color-text-primary); margin-bottom: 4px; }
        .eq-status { display: inline-block; font-size: 11px; padding: 2px 6px; border-radius: 4px; background: #f3f4f6; color: #4b5563; font-weight: 500; }
        .status-normal { background: #dcfce7; color: #166534; }
        .status-damaged { background: #fee2e2; color: #991b1b; }
        .status-missing { background: #fef9c3; color: #854d0e; }
        .status-waiting_repair { background: #ffedd5; color: #9a3412; }
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
                    <label>Báo cáo tình trạng thiết bị</label>
                    <div id="equipmentListContainer" class="equipment-list">
                        <span style="color: var(--color-text-secondary); padding: 8px; display: block;">-- Chọn phòng để hiển thị thiết bị --</span>
                    </div>
                    <small><i>Lưu ý: Chỉ chọn trạng thái khác "Bình thường" đối với thiết bị bị hỏng hoặc thất lạc. Nếu là sự cố chung của phòng, hãy mô tả ở bên dưới.</i></small>
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
