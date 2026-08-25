<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="model.HousekeepingTask" %>
<%
    boolean isManager = Boolean.TRUE.equals(request.getAttribute("isManager"));
    String cp = request.getContextPath();
    String backUrl = isManager ? cp + "/manager/issues" : cp + "/housekeeping/issues";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi Tiết &amp; Lịch Sử Sửa Chữa Thiết Bị | HMS</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="<%= cp %>/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/housekeeping.css?v=20260825-1">
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

<main class="page-container hk-page">
    <div class="verify-wrap">
        <a class="hk-back" href="<%= backUrl %>">← <%= isManager ? "Quản lý sự cố" : "Danh sách sự cố" %></a>
        
        <section class="hk-detail-heading">
            <div>
                <p class="hk-eyebrow"><%= isManager ? "Quản lý khách sạn" : "Vận hành dọn phòng" %> · Task #<c:out value="${taskId}"/></p>
                <h1>Phòng <c:out value="${task.roomNumber}"/></h1>
                <p>
                    <c:out value="${task.roomTypeName}"/> · Tầng <c:out value="${task.floorNumber}"/> · 
                    Người phụ trách: <strong><c:out value="${task.assignedStaffName != null ? task.assignedStaffName : 'Chưa phân công'}"/></strong> · 
                    Thời gian: <strong><fmt:formatDate value="${task.createdAt}" pattern="dd/MM/yyyy HH:mm"/></strong>
                </p>
            </div>
            <div>
                <span class="hk-badge task-${task.status != null ? task.status.toLowerCase() : 'pending'}">${task != null ? task.getStatusLabel() : 'Chờ xử lý'}</span>
            </div>
        </section>

        <!-- THÔNG TIN SỰ CỐ GỐC -->
        <div class="verify-card">
            <h2>📋 Thông tin sự cố ban đầu</h2>
            <div class="info-grid">
                <div class="info-box">
                    <span>Loại công việc</span>
                    <strong>${task != null ? task.getTaskTypeLabel() : '--'}</strong>
                </div>
                <div class="info-box">
                    <span>Trạng thái hiện tại</span>
                    <strong>${task != null ? task.getStatusLabel() : '--'}</strong>
                </div>
                <div class="info-box">
                    <span>Người phụ trách / Kỹ thuật</span>
                    <strong>${task.assignedStaffName != null ? task.assignedStaffName : 'Chưa phân công'}</strong>
                </div>
            </div>
            <div class="info-box">
                <span>Mô tả sự cố &amp; Ghi chú ban đầu</span>
                <p style="margin: 0; color: #334155; font-size: 14.5px;">${task != null ? HousekeepingTask.esc(task.note) : '--'}</p>
            </div>
        </div>

        <!-- LỊCH SỬ BẢO TRÌ & THAY ĐỔI TRẠNG THÁI CỦA THIẾT BỊ NÀY -->
        <c:if test="${not empty logs}">
        <div class="verify-card">
            <h2>📜 Lịch sử xử lý &amp; Nhật ký bảo trì phòng này</h2>
            <div class="table-responsive">
                <table class="log-table">
                    <thead>
                        <tr>
                            <th>Thiết bị</th>
                            <th>Trạng thái cũ</th>
                            <th>Trạng thái mới</th>
                            <th>Ghi chú xử lý</th>
                            <th>Người thực hiện</th>
                            <th>Thời gian</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td><strong>${HousekeepingTask.esc(log.equipmentName)}</strong></td>
                                <td>
                                    <span class="equipment-status status-${log.previousStatus != null ? log.previousStatus.toLowerCase() : 'normal'}">${log.previousStatus}</span>
                                </td>
                                <td>
                                    <span class="equipment-status status-${log.newStatus != null ? log.newStatus.toLowerCase() : 'normal'}">${log.newStatus}</span>
                                </td>
                                <td>${HousekeepingTask.esc(log.note)}</td>
                                <td>${HousekeepingTask.esc(log.confirmedByName)}</td>
                                <td><fmt:formatDate value="${log.confirmedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
        </c:if>

        <!-- FORM XÁC NHẬN SỬA CHỮA (CHỈ HIỂN THỊ THIẾT BỊ LIÊN QUAN ĐẾN TASK NÀY) -->
        <c:if test="${not empty equipments}">
        <div class="verify-card">
            <h2>🔧 Xác nhận thiết bị đã được sửa xong</h2>
            <p class="text-secondary">Xác nhận thiết bị dưới đây đã được sửa chữa hoặc thay thế thành công để khôi phục trạng thái hoạt động bình thường.</p>
            
            <form action="<%= cp %><%= isManager ? "/manager/issues/verify" : "/housekeeping/issues/verify" %>" method="post">
                <input type="hidden" name="taskId" value="${taskId}">
                
                <div class="equipment-list">
                    <c:forEach var="equip" items="${equipments}">
                        <div class="equipment-item">
                            <input type="checkbox" name="equipmentIds" value="${equip.roomEquipmentId}" id="equip_${equip.roomEquipmentId}" checked>
                            <div class="equipment-info">
                                <label for="equip_${equip.roomEquipmentId}" class="clickable-bold-label">
                                    ${HousekeepingTask.esc(equip.equipmentName)}
                                </label>
                                <span class="equipment-status status-${equip.currentStatus.toLowerCase()}">
                                    Tình trạng: ${equip.currentStatusLabel}
                                </span>
                                <c:if test="${not empty equip.note}">
                                    <small class="text-muted">Ghi chú hỏng: ${HousekeepingTask.esc(equip.note)}</small>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                
                <div class="form-group">
                    <label for="note">Ghi chú xác nhận sửa chữa (tùy chọn)</label>
                    <textarea name="note" id="note" rows="3" class="form-control" placeholder="Ví dụ: Đã thay linh kiện mới, máy chạy êm..."></textarea>
                </div>
                
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Xác nhận hoàn tất sửa chữa</button>
                </div>
            </form>
        </div>
        </c:if>
    </div>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>