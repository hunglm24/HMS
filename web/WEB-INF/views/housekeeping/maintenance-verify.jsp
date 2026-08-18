<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.HousekeepingTask" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Xác nhận bảo trì - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .verify-panel { max-width: 800px; margin: 0 auto; }
        .equipment-list { margin-top: 20px; }
        .equipment-item { display: flex; align-items: center; padding: 12px; border: 1px solid var(--color-border); border-radius: 4px; margin-bottom: 8px; gap: 16px; }
        .equipment-item input[type="checkbox"] { width: 20px; height: 20px; cursor: pointer; }
        .equipment-info { flex: 1; }
        .equipment-status { font-weight: bold; }
        .status-damaged { color: #dc3545; }
        .status-missing { color: #dc3545; }
        .status-waiting_repair { color: #fd7e14; }
        .status-waiting_replacement { color: #fd7e14; }
        .status-maintenance { color: #0dcaf0; }
        .form-group { margin-top: 24px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
        .form-control { width: 100%; padding: 10px; border: 1px solid var(--color-border); border-radius: 4px; }
        .form-actions { margin-top: 24px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="app-shell">
    <jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />
    <main>
        <h1>Xác nhận thiết bị đã bảo trì</h1>
        <p>Phòng: <strong>P.${roomId}</strong> (Task #${taskId})</p>
        
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger">${sessionScope.errorMessage}</div>
            <c:remove var="errorMessage" scope="session"/>
        </c:if>

        <div class="verify-panel">
            <form action="${pageContext.request.contextPath}/housekeeping/issues/verify" method="post">
                <input type="hidden" name="taskId" value="${taskId}">
                
                <h3>Danh sách thiết bị hỏng/đang bảo trì</h3>
                <p>Vui lòng tích chọn những thiết bị đã được sửa chữa hoặc thay thế thành công (Trạng thái sẽ được cập nhật thành NORMAL).</p>
                
                <div class="equipment-list">
                    <c:forEach var="equip" items="${equipments}">
                        <div class="equipment-item">
                            <input type="checkbox" name="equipmentIds" value="${equip.roomEquipmentId}" id="equip_${equip.roomEquipmentId}">
                            <div class="equipment-info">
                                <label for="equip_${equip.roomEquipmentId}" style="cursor: pointer; display: block;">
                                    <strong>${HousekeepingTask.esc(equip.equipmentName)}</strong>
                                    (Mã TB: ${equip.roomEquipmentId})
                                </label>
                                <span class="equipment-status status-${equip.currentStatus.toLowerCase()}">
                                    Trạng thái hiện tại: ${equip.currentStatus}
                                </span>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty equipments}">
                        <div class="alert alert-info">Phòng này không có thiết bị nào đang ghi nhận hỏng hoặc cần bảo trì.</div>
                    </c:if>
                </div>
                
                <c:if test="${not empty equipments}">
                    <div class="form-group">
                        <label for="note">Ghi chú xác nhận (tùy chọn)</label>
                        <textarea name="note" id="note" rows="3" class="form-control" placeholder="Ví dụ: Đã kiểm tra thợ sửa xong điều hòa..."></textarea>
                    </div>
                    
                    <div class="form-actions">
                        <a href="${pageContext.request.contextPath}/housekeeping/issues" class="btn btn-secondary">Quay lại</a>
                        <button type="submit" class="btn btn-primary">Xác nhận đã sửa</button>
                    </div>
                </c:if>
                <c:if test="${empty equipments}">
                    <div class="form-actions">
                        <a href="${pageContext.request.contextPath}/housekeeping/issues" class="btn btn-secondary">Quay lại</a>
                    </div>
                </c:if>
            </form>
        </div>
    </main>
</div>
</body>
</html>
