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
    <title>Chi tiết & Lịch sử sửa chữa thiết bị - HMS</title>
    <link rel="stylesheet" href="<%= cp %>/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="<%= cp %>/assets/css/housekeeping.css?v=20260820-7">
    <style>
        .verify-wrap { max-width: 900px; margin: 0 auto; }
        .verify-card { background: #fff; border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 24px; margin-bottom: 24px; box-shadow: var(--shadow-sm); }
        .verify-card h2 { margin: 0 0 16px 0; font-size: 1.25rem; color: #1e293b; display: flex; align-items: center; gap: 8px; }
        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 16px; }
        .info-box { background: #f8fafc; padding: 12px 16px; border-radius: 8px; border: 1px solid #e2e8f0; }
        .info-box span { display: block; font-size: 12px; color: #64748b; font-weight: 500; text-transform: uppercase; margin-bottom: 4px; }
        .info-box strong { font-size: 15px; color: #0f172a; }
        
        .equipment-list { margin-top: 16px; }
        .equipment-item { display: flex; align-items: center; padding: 14px 16px; border: 1px solid var(--color-border); border-radius: 8px; margin-bottom: 10px; gap: 16px; background: #fff; transition: background 0.2s; }
        .equipment-item:hover { background: #f8fafc; }
        .equipment-item input[type="checkbox"] { width: 22px; height: 22px; cursor: pointer; accent-color: #2563eb; }
        .equipment-info { flex: 1; }
        .equipment-status { display: inline-block; font-size: 12px; font-weight: 600; padding: 3px 8px; border-radius: 4px; margin-top: 4px; }
        .status-damaged { background: #fee2e2; color: #dc2626; }
        .status-missing { background: #fee2e2; color: #dc2626; }
        .status-waiting_repair { background: #ffedd5; color: #ea580c; }
        .status-waiting_replacement { background: #ffedd5; color: #ea580c; }
        .status-maintenance { background: #e0f2fe; color: #0284c7; }
        .status-normal { background: #dcfce7; color: #16a34a; }

        .log-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
        .log-table th, .log-table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        .log-table th { background: #f8fafc; color: #475569; font-weight: 600; }
        
        .form-group { margin-top: 20px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: #334155; }
        .form-control { width: 100%; padding: 12px; border: 1.5px solid var(--color-border); border-radius: 8px; font-family: inherit; font-size: 14px; resize: vertical; box-sizing: border-box; }
        .form-actions { display: flex; gap: 12px; margin-top: 24px; align-items: center; }
        .btn { padding: 10px 20px; border-radius: 8px; font-weight: 600; text-decoration: none; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; font-size: 14px; border: none; }
        .btn-primary { background: #2563eb; color: #ffffff; }
        .btn-primary:hover { background: #1d4ed8; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <div class="verify-wrap">
        <a class="hk-back" href="<%= backUrl %>" style="margin-bottom: 16px; display: inline-block;">← <%= isManager ? "Quản lý sự cố" : "Danh sách sự cố" %></a>
        
        <section class="hk-detail-heading" style="margin-bottom: 24px;">
            <div>
                <p class="hk-eyebrow"><%= isManager ? "Quản lý khách sạn" : "Vận hành phòng" %> · Sự cố bảo trì #${taskId}</p>
                <h1>Phòng ${task != null ? task.roomNumber : roomId}</h1>
                <p>${task != null ? task.roomTypeName : ""} · Tầng ${task != null ? task.floorNumber : "--"}</p>
            </div>
            <c:if test="${task != null}">
                <span class="hk-badge task-${task.status.toLowerCase()}">${task.statusLabel}</span>
            </c:if>
        </section>

        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger" style="margin-bottom: 20px;">${sessionScope.errorMessage}</div>
            <c:remove var="errorMessage" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success" style="margin-bottom: 20px;">${sessionScope.successMessage}</div>
            <c:remove var="successMessage" scope="session"/>
        </c:if>

        <!-- THÔNG TIN BÁO CÁO BAN ĐẦU -->
        <div class="verify-card">
            <h2>📋 Thông tin báo cáo sự cố</h2>
            <div class="info-grid">
                <div class="info-box">
                    <span>Mã công việc</span>
                    <strong>#${taskId}</strong>
                </div>
                <div class="info-box">
                    <span>Loại sự cố</span>
                    <strong>${task != null ? task.taskTypeLabel : "Bảo trì thiết bị"}</strong>
                </div>
                <div class="info-box">
                    <span>Thời gian báo cáo</span>
                    <strong><fmt:formatDate value="${task.createdAt}" pattern="dd/MM/yyyy HH:mm" /></strong>
                </div>
                <div class="info-box">
                    <span>Trạng thái</span>
                    <strong>${task != null ? task.statusLabel : "--"}</strong>
                </div>
            </div>
            <div class="info-box" style="margin-top: 12px; background: #fffbe6; border-color: #ffe58f;">
                <span style="color: #d46b08;">Mô tả chi tiết từ người báo cáo</span>
                <p style="margin: 4px 0 0 0; color: #1e293b; font-size: 14.5px; line-height: 1.5; white-space: pre-wrap;">${task != null && task.note != null && !task.note.isBlank() ? task.note : "Không có mô tả chi tiết."}</p>
            </div>
        </div>

        <!-- LỊCH SỬ SỬA CHỮA / NGHIỆM THU TỪ LOGS -->
        <c:if test="${not empty logs}">
        <div class="verify-card">
            <h2>🛠️ Lịch sử sửa chữa & Nghiệm thu thiết bị</h2>
            <p style="color: #64748b; font-size: 13.5px; margin: 0 0 12px 0;">Chi tiết các thiết bị đã được kỹ thuật viên sửa chữa và nhân viên xác nhận hoàn tất.</p>
            <div style="overflow-x: auto;">
                <table class="log-table">
                    <thead>
                        <tr>
                            <th>Thiết bị</th>
                            <th>Trạng thái trước</th>
                            <th>Kết quả</th>
                            <th>Ghi chú sửa chữa / nghiệm thu</th>
                            <th>Người xác nhận</th>
                            <th>Thời gian hoàn thành</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td><strong>${HousekeepingTask.esc(log.equipmentName)}</strong></td>
                                <td><span class="equipment-status status-${log.previousStatus.toLowerCase()}">${log.previousStatus}</span></td>
                                <td><span class="equipment-status status-normal">✓ Hoàn tất (NORMAL)</span></td>
                                <td>${HousekeepingTask.esc(log.note != null && !log.note.isBlank() ? log.note : "Đã sửa xong")}</td>
                                <td><strong>${HousekeepingTask.esc(log.confirmedByName != null ? log.confirmedByName : "Nhân viên")}</strong></td>
                                <td><fmt:formatDate value="${log.confirmedAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
        </c:if>

        <!-- DANH SÁCH THIẾT BỊ ĐÃ XỬ LÝ (KHI TASK HOÀN THÀNH NHƯNG CHƯA CÓ LOGS CŨ) -->
        <c:if test="${empty logs && task != null && task.status eq 'COMPLETED'}">
        <div class="verify-card">
            <h2>🛠️ Danh sách thiết bị trong phòng đã hoàn tất bảo trì</h2>
            <p style="color: #16a34a; font-size: 13.5px; font-weight: 600; margin: 0 0 12px 0;">✓ Công việc bảo trì đã hoàn tất, toàn bộ thiết bị trong phòng hoạt động bình thường.</p>
            <div class="equipment-list">
                <c:forEach var="equip" items="${allRoomEquipments}">
                    <c:if test="${empty task.roomEquipmentId or task.roomEquipmentId eq equip.roomEquipmentId}">
                    <div class="equipment-item">
                        <div class="equipment-info">
                            <strong>${HousekeepingTask.esc(equip.equipmentName)}</strong> (Mã TB: ${equip.roomEquipmentId})
                            <span class="equipment-status status-normal">✓ Hoạt động bình thường (NORMAL)</span>
                        </div>
                    </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>
        </c:if>

        <!-- FORM XÁC NHẬN SỬA CHỮA (NẾU TASK CHƯA HOÀN THÀNH VÀ CÒN THIẾT BỊ HỎNG) -->
        <c:if test="${not empty equipments}">
        <div class="verify-card">
            <h2>🔧 Xác nhận thiết bị đã được sửa xong</h2>
            <p style="color: #64748b; font-size: 13.5px; margin: 0 0 16px 0;">Tích chọn những thiết bị đã được sửa chữa hoặc thay thế thành công để khôi phục trạng thái hoạt động bình thường.</p>
            
            <form action="<%= cp %><%= isManager ? "/manager/issues/verify" : "/housekeeping/issues/verify" %>" method="post">
                <input type="hidden" name="taskId" value="${taskId}">
                
                <div class="equipment-list">
                    <c:forEach var="equip" items="${equipments}">
                        <div class="equipment-item">
                            <input type="checkbox" name="equipmentIds" value="${equip.roomEquipmentId}" id="equip_${equip.roomEquipmentId}">
                            <div class="equipment-info">
                                <label for="equip_${equip.roomEquipmentId}" style="cursor: pointer; display: block; font-weight: 600; color: #1e293b;">
                                    ${HousekeepingTask.esc(equip.equipmentName)}
                                </label>
                                <span class="equipment-status status-${equip.currentStatus.toLowerCase()}">
                                    Tình trạng: ${equip.currentStatus}
                                </span>
                                <c:if test="${not empty equip.note}">
                                    <small style="display: block; color: #64748b; margin-top: 2px;">Ghi chú hỏng: ${HousekeepingTask.esc(equip.note)}</small>
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