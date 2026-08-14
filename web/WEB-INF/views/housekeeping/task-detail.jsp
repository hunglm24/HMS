<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.HousekeepingTask" %>
<%!
    private String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    HousekeepingTask task = (HousekeepingTask) request.getAttribute("task");
    List<HousekeepingTask.EquipmentCheck> equipment =
            (List<HousekeepingTask.EquipmentCheck>) request.getAttribute("equipment");
    String contextPath = request.getContextPath();
    boolean inspection = "CHECKOUT_INSPECTION".equals(task.getTaskType());
    boolean pending = "PENDING".equals(task.getStatus());
    boolean cleaningBlocked = !inspection && pending && !task.isActionReady();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Phòng <%= esc(task.getRoomNumber()) %> | Dọn phòng</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="hk-page">
    <a class="hk-back" href="<%= contextPath %>/housekeeping/tasks?view=mine">← Công việc của tôi</a>
    <section class="hk-detail-heading">
        <div><p class="hk-eyebrow">Dọn phòng · Mã công việc #<%= task.getTaskId() %></p>
            <h1>Phòng <%= esc(task.getRoomNumber()) %></h1>
            <p><%= esc(task.getRoomTypeName()) %> · Tầng <%= task.getFloorNumber() == null ? "--" : task.getFloorNumber() %></p></div>
        <span class="hk-badge task-<%= task.getStatus().toLowerCase() %>"><%= pending ? "Chờ thực hiện" : "Đang thực hiện" %></span>
    </section>

    <% if (inspection) { %>
    <form id="inspection-form" class="hk-card hk-work-form" method="post"
          action="<%= contextPath %>/housekeeping/tasks/complete-inspection" novalidate>
        <input type="hidden" name="taskId" value="<%= task.getTaskId() %>">
        <div class="hk-section-heading"><div><h2>Kiểm tra thiết bị trong phòng</h2>
            <p>Kiểm tra lần lượt từng thiết bị trước khi xác nhận hoàn tất.</p></div>
            <span><%= equipment == null ? 0 : equipment.size() %> thiết bị</span></div>

        <div class="hk-equipment-list">
        <% if (equipment != null) for (HousekeepingTask.EquipmentCheck item : equipment) {
            String id = String.valueOf(item.getRoomEquipmentId()); %>
            <fieldset class="hk-equipment-item">
                <legend><span class="hk-equipment-icon" aria-hidden="true">✓</span>
                    <strong><%= esc(item.getEquipmentName()) %></strong>
                    <small>Số lượng hiện có: <%= item.getQuantity() %> · Lúc check-in:
                        <%= item.getInitialStatus() == null ? "Không có snapshot" : esc(item.getInitialStatus()) + " / SL " + item.getInitialQuantity() %></small></legend>
                <label>Tình trạng thực tế <span class="required">*</span>
                    <select class="condition-select" name="condition_<%= id %>" required>
                        <option value="">-- Chọn tình trạng --</option>
                        <option value="NORMAL">Bình thường</option>
                        <option value="DAMAGED">Hư hỏng</option>
                        <option value="MISSING">Thất lạc</option>
                    </select><small class="field-error">Vui lòng chọn tình trạng.</small>
                </label>
                <label>Phí bồi thường dự kiến
                    <input type="number" name="fee_<%= id %>" min="0" max="9999999999999.99" step="1000" value="0">
                    <small>Tối đa 9.999.999.999.999,99</small>
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
    <section class="hk-card hk-cleaning-card">
        <div class="hk-section-heading"><div><h2>Dọn phòng sau checkout</h2>
            <p>Đảm bảo phòng sạch sẽ và sẵn sàng cho công việc vận hành tiếp theo.</p></div></div>
        <div class="hk-note-box"><strong>Ghi chú công việc</strong><p><%= esc(task.getNote()) %></p></div>
        <% if (cleaningBlocked) { %><div class="hk-warning-box"><strong>Chưa thể bắt đầu dọn phòng</strong>
            <p>Inspection đã hoàn tất nhưng booking vẫn đang xử lý checkout. Công việc sẽ được mở khi checkout hoàn tất.</p></div><% } %>
        <form method="post" action="<%= contextPath %>/housekeeping/tasks/<%= pending ? "start-cleaning" : "complete-cleaning" %>">
            <input type="hidden" name="taskId" value="<%= task.getTaskId() %>">
            <div class="hk-form-actions"><a href="<%= contextPath %>/housekeeping/tasks?view=mine">Quay lại</a>
                <button class="hk-primary" type="submit" <%= cleaningBlocked ? "disabled" : "" %>><%= cleaningBlocked ? "Đang chờ checkout" : pending ? "Bắt đầu dọn phòng" : "Hoàn tất dọn phòng" %></button></div>
        </form>
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
        if (!valid) {
            event.preventDefault();
            form.querySelector('.has-error select').focus();
        }
    });
    form.querySelectorAll('.condition-select').forEach(select => select.addEventListener('change', () => {
        select.closest('label').classList.toggle('has-error', !select.value);
    }));
})();
</script>
</body></html>
