<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.HousekeepingTask" %>

<%
    HousekeepingTask task = (HousekeepingTask) request.getAttribute("task");
    List<HousekeepingTask.EquipmentCheck> equipment =
            (List<HousekeepingTask.EquipmentCheck>) request.getAttribute("equipment");
    String contextPath = request.getContextPath();
    boolean inspection = "CHECKOUT_INSPECTION".equals(task.getTaskType());
    boolean pending = "PENDING".equals(task.getStatus());
    boolean history = Boolean.TRUE.equals(request.getAttribute("history"));
    List<String> workItems = (List<String>) request.getAttribute("workItems");
    String inspectionMessage = (String) request.getAttribute("inspectionMessage");
    Map<String, String> cleaningChecklist =
            (Map<String, String>) request.getAttribute("cleaningChecklist");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Phòng <%= HousekeepingTask.esc(task.getRoomNumber()) %> | Dọn phòng</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css?v=20260816-4">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <a class="hk-back" href="<%= contextPath %>/housekeeping/tasks?view=<%= history ? "history" : "mine" %>">← <%= history ? "Lịch sử" : "Công việc của tôi" %></a>
    <section class="hk-detail-heading">
        <div><p class="hk-eyebrow">Dọn phòng · Mã công việc #<%= task.getTaskId() %></p>
            <h1>Phòng <%= HousekeepingTask.esc(task.getRoomNumber()) %></h1>
            <p><%= HousekeepingTask.esc(task.getRoomTypeName()) %> · Tầng <%= task.getFloorNumber() == null ? "--" : task.getFloorNumber() %></p></div>
        <span class="hk-badge task-<%= task.getStatus().toLowerCase() %>"><%= task.getStatusLabel() %></span>
    </section>

    <% if (inspection && history) { %>
    <section class="hk-card hk-work-form"><div class="hk-section-heading"><div><h2>Kết quả kiểm tra thiết bị</h2>
        <p>Dữ liệu được lưu tại thời điểm hoàn tất inspection.</p></div></div>
        <div class="hk-history-equipment"><% if (equipment != null) for (HousekeepingTask.EquipmentCheck item : equipment) { %>
            <article><div><strong><%= HousekeepingTask.esc(item.getEquipmentName()) %></strong><small>Số lượng: <%= item.getQuantity() %></small></div>
                <span class="hk-badge condition-<%= item.getConditionStatus().toLowerCase() %>"><%= "NORMAL".equals(item.getConditionStatus()) ? "Bình thường" : "DAMAGED".equals(item.getConditionStatus()) ? "Hư hỏng" : "Thất lạc" %></span>
                <div><strong><%= String.format("%,.0f", item.getDamageFee()).replace(",", " ").replace(".", " ") %> VND</strong><small><%= HousekeepingTask.esc(item.getNote()) %></small></div></article>
        <% } %></div>
        <div class="hk-history-meta"><span>Bắt đầu: <strong><%= task.getStartedAt() == null ? "--" : task.getStartedAt() %></strong></span>
            <span>Hoàn thành: <strong><%= task.getCompletedAt() == null ? "--" : task.getCompletedAt() %></strong></span></div>
    </section>
    <% } else if (inspection) { %>
    <form id="inspection-form" class="hk-card hk-work-form" method="post"
          action="<%= contextPath %>/housekeeping/tasks/complete-inspection" novalidate>
        <input type="hidden" name="taskId" value="<%= task.getTaskId() %>">
        <div class="hk-section-heading"><div><h2>Kiểm tra thiết bị trong phòng</h2>
            <p>Kiểm tra lần lượt từng thiết bị trước khi xác nhận hoàn tất.</p></div>
            <span><%= equipment == null ? 0 : equipment.size() %> thiết bị</span></div>

        <section class="hk-inspection-checklist" aria-labelledby="cleaning-checklist-title">
            <div><h3 id="cleaning-checklist-title">Khu vực cần dọn</h3>
                <p>Đánh dấu những khu vực housekeeper cần ưu tiên xử lý.</p></div>
            <div class="hk-check-grid">
                <% if (cleaningChecklist != null) for (Map.Entry<String, String> item : cleaningChecklist.entrySet()) { %>
                <label><input type="checkbox" name="cleaningItem" value="<%= HousekeepingTask.esc(item.getKey()) %>">
                    <span><strong><%= HousekeepingTask.esc(item.getValue()) %></strong><small>Thêm vào danh sách việc cần làm</small></span>
                </label>
                <% } %>
            </div>
        </section>

        <div class="hk-equipment-list">
        <% if (equipment != null) for (HousekeepingTask.EquipmentCheck item : equipment) {
            String id = String.valueOf(item.getRoomEquipmentId()); %>
            <fieldset class="hk-equipment-item">
                <legend><span class="hk-equipment-icon" aria-hidden="true">✓</span>
                    <strong><%= HousekeepingTask.esc(item.getEquipmentName()) %></strong>
                    <small>Số lượng hiện có: <%= item.getQuantity() %> · Lúc check-in:
                        <%= item.getInitialStatus() == null ? "Không có snapshot" : HousekeepingTask.esc(item.getInitialStatus()) + " / SL " + item.getInitialQuantity() %></small></legend>
                <label><span class="hk-label-title">Tình trạng thực tế <span class="required">*</span></span>
                    <select class="condition-select" name="condition_<%= id %>" required>
                        <option value="">-- Chọn tình trạng --</option>
                        <option value="NORMAL">Bình thường</option>
                        <option value="DAMAGED">Hư hỏng</option>
                        <option value="MISSING">Thất lạc</option>
                    </select><small class="field-error">Vui lòng chọn tình trạng.</small>
                </label>
                <label><span class="hk-label-title">Phí bồi thường dự kiến</span>
                    <input class="fee-input" type="number" name="fee_<%= id %>" min="0" max="15000000" step="1000" value="0" disabled>
                    <small>Tối đa: 15 000 000 VND</small>
                </label>
                <label class="hk-wide">Ghi chú sự cố
                    <input type="text" name="note_<%= id %>" maxlength="1000" placeholder="Mô tả vị trí, mức độ hư hỏng hoặc tình trạng thất lạc">
                    <small>Tối đa 1.000 ký tự.</small>
                </label>
            </fieldset>
        <% } %>
        </div>
        <label class="hk-form-label">Ghi chú chung
            <textarea name="inspectionNote" maxlength="2000" rows="4" placeholder="Thông tin bổ sung sau khi kiểm tra phòng"></textarea>
            <small>Tối đa 2.000 ký tự.</small>
        </label>
        <div class="hk-form-actions"><a href="<%= contextPath %>/housekeeping/tasks?view=mine">Quay lại</a>
            <button class="hk-primary" type="submit">Hoàn tất kiểm tra</button></div>
    </form>
    <% } else { %>
    <section class="hk-cleaning-layout">
      <div class="hk-card hk-cleaning-main">
        <div class="hk-section-heading"><div><h2>Những việc cần làm</h2>
            <p>Hoàn thành lần lượt các nội dung được ghi nhận khi inspection.</p></div></div>
        <ol class="hk-work-list">
            <% if (workItems != null && !workItems.isEmpty()) for (String item : workItems) { %>
                <li><span aria-hidden="true">✓</span><strong><%= HousekeepingTask.esc(item) %></strong></li>
            <% } else { %><li><span aria-hidden="true">✓</span><strong>Dọn vệ sinh tổng quát và kiểm tra lại phòng</strong></li><% } %>
        </ol>
        <% if (inspectionMessage != null) { %><div class="hk-note-box"><strong>Lời nhắn từ người inspection</strong>
            <p><%= HousekeepingTask.esc(inspectionMessage) %></p></div><% } %>
        <% if (!history) { %><form method="post" action="<%= contextPath %>/housekeeping/tasks/<%= pending ? "start-cleaning" : "complete-cleaning" %>">
            <input type="hidden" name="taskId" value="<%= task.getTaskId() %>">
            <div class="hk-form-actions"><a href="<%= contextPath %>/housekeeping/tasks?view=mine">Quay lại</a>
                <button class="hk-primary" type="submit"><%= pending ? "Bắt đầu dọn phòng" : "Hoàn tất dọn phòng" %></button></div>
        </form><% } else if (history) { %><div class="hk-history-meta"><span>Bắt đầu: <strong><%= task.getStartedAt() == null ? "--" : task.getStartedAt() %></strong></span><span>Hoàn thành: <strong><%= task.getCompletedAt() == null ? "--" : task.getCompletedAt() %></strong></span></div><% } %>
      </div>
      <aside class="hk-card hk-maintenance-check">
        <div class="hk-section-heading"><div><h2>Kiểm tra thiết bị</h2>
            <p>Housekeeper chỉ xác nhận tình trạng hiện tại, không thực hiện sửa chữa tại đây.</p></div></div>
        <% if (equipment == null || equipment.isEmpty()) { %>
            <div class="hk-all-clear"><span aria-hidden="true">✓</span><p>Không có thiết bị hỏng hoặc thất lạc được ghi nhận.</p></div>
        <% } else { %><div class="hk-maintenance-list">
            <% for (HousekeepingTask.EquipmentCheck item : equipment) {
                boolean repaired = "NORMAL".equals(item.getCurrentStatus()); %>
            <article><div><strong><%= HousekeepingTask.esc(item.getEquipmentName()) %></strong>
                <small><%= HousekeepingTask.esc(item.getNote()) %></small></div>
                <span class="hk-badge <%= repaired ? "condition-normal" : "condition-damaged" %>"><%= repaired ? "Đã xử lý" : "Chưa xử lý" %></span></article>
            <% } %>
        </div><p class="hk-maintenance-help">Nếu thiết bị chưa được xử lý, phòng vẫn có thể dọn xong nhưng sẽ ở trạng thái NOT_READY hoặc MAINTENANCE.</p><% } %>
      </aside>
    </section>
    <% } %>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
<script>
(() => {
    const form = document.getElementById('inspection-form');
    if (!form) return;
    form.addEventListener('submit', event => {
        let valid = true;
        form.querySelectorAll('.condition-select').forEach(select => {
            const group = select.closest('label');
            const missing = !select.value;
            group.classList.toggle('has-error', missing);
            if (missing) valid = false;
        });
        form.querySelectorAll('.fee-input:not(:disabled)').forEach(input => {
            const fee = Number(input.value);
            if (!Number.isFinite(fee) || fee < 0 || fee > 15000000) {
                input.setCustomValidity('Phí bồi thường phải từ 0 đến 15 000 000 VND.');
                input.reportValidity();
                valid = false;
            } else input.setCustomValidity('');
        });
        if (!valid) {
            event.preventDefault();
            form.querySelector('.has-error select').focus();
        }
    });
    form.querySelectorAll('.condition-select').forEach(select => {
        const syncFee = () => {
            select.closest('label').classList.toggle('has-error', !select.value);
            const fee = select.closest('fieldset').querySelector('.fee-input');
            const chargeable = select.value === 'DAMAGED' || select.value === 'MISSING';
            fee.disabled = !chargeable;
            if (!chargeable) fee.value = '0';
        };
        select.addEventListener('change', syncFee);
        syncFee();
    });
})();
</script>
</body></html>
