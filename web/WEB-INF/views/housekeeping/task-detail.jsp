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
    boolean isManager = Boolean.TRUE.equals(request.getAttribute("isManager"));
    List<HousekeepingTask.WorkItem> workItems = (List<HousekeepingTask.WorkItem>) request.getAttribute("workItems");
    String inspectionMessage = (String) request.getAttribute("inspectionMessage");
    Map<String, String> cleaningChecklist =
            (Map<String, String>) request.getAttribute("cleaningChecklist");
    String backUrl = isManager ? contextPath + "/manager/housekeeping" : contextPath + "/housekeeping/tasks?view=" + (history ? "history" : "mine");
    String backLabel = isManager ? "Lịch sử & Vận hành phòng" : (history ? "Lịch sử dọn phòng" : "Công việc của tôi");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Phòng <%= HousekeepingTask.esc(task.getRoomNumber()) %> | <%= isManager ? "Chi tiết công việc phòng" : "Dọn phòng" %></title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/rooms.css?v=20260820-7">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css?v=20260825-1">
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />
<main class="page-container hk-page">
    <a class="hk-back" href="<%= backUrl %>">← <%= backLabel %></a>
    <section class="hk-detail-heading">
        <div>
            <p class="hk-eyebrow"><%= isManager ? "Quản lý khách sạn" : "Vận hành phòng" %> · Mã công việc #<%= task.getTaskId() %></p>
            <h1>Phòng <%= HousekeepingTask.esc(task.getRoomNumber()) %></h1>
            <p>
                <%= HousekeepingTask.esc(task.getRoomTypeName()) %> · Tầng <%= task.getFloorNumber() == null ? "--" : task.getFloorNumber() %> · Người phụ trách: <strong><%= task.getAssignedStaffName() != null ? HousekeepingTask.esc(task.getAssignedStaffName()) : "Chưa phân công" %></strong>
                <% if (task.getCreatedAt() != null) { %>
                · Thời gian: <strong><%= task.getFormattedCreatedAt() %></strong>
                <% } %>
            </p>
        </div>
        <span class="hk-badge task-<%= task.getStatus().toLowerCase() %>"><%= task.getStatusLabel() %></span>
    </section>

    <% if (inspection && (history || isManager)) { %>
    <section class="hk-card hk-work-form">
        <div class="hk-section-heading">
            <div>
                <h2><%= history ? "Kết quả kiểm tra thiết bị" : "Danh mục thiết bị cần kiểm tra" %></h2>
                <p><%= history ? "Dữ liệu được lưu tại thời điểm hoàn tất inspection." : "Chế độ xem giám sát dành cho Quản lý (Chỉ đọc)." %></p>
            </div>
        </div>

        <% if (task.getNote() != null && !task.getNote().isBlank() && !"Kiểm tra phòng sau checkout".equalsIgnoreCase(task.getNote().trim()) && !"Kiểm tra phòng".equalsIgnoreCase(task.getNote().trim())) { %>
        <div class="hk-manager-note-box" style="margin: 0 0 20px 0; padding: 16px 20px; background: #fffbe6; border: 1.5px solid #ffe58f; border-left: 5px solid #faad14; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.05);">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
                <span style="font-size: 18px;">📌</span>
                <strong style="color: #d46b08; font-size: 15px; text-transform: uppercase; letter-spacing: 0.5px;">Ghi chú & Lời dặn:</strong>
            </div>
            <p style="margin: 0; color: #434343; font-size: 14.5px; font-weight: 500; line-height: 1.6; white-space: pre-wrap;"><%= HousekeepingTask.esc(task.getNote()) %></p>
        </div>
        <% } %>

        <div class="hk-history-equipment">
        <% if (equipment != null && !equipment.isEmpty()) {
            for (HousekeepingTask.EquipmentCheck item : equipment) { %>
            <article>
                <div>
                    <strong><%= HousekeepingTask.esc(item.getEquipmentName()) %></strong>
                    <small>Số lượng: <%= item.getQuantity() %></small>
                </div>
                <% String cond = item.getConditionStatus() != null ? item.getConditionStatus() : "NORMAL"; %>
                <span class="hk-badge condition-<%= cond.toLowerCase() %>">
                    <%= "NORMAL".equals(cond) ? "Bình thường" : "DAMAGED".equals(cond) ? "Hư hỏng" : "Thất lạc" %>
                </span>
                <div><small><%= HousekeepingTask.esc(item.getNote() != null ? item.getNote() : "") %></small></div>
            </article>
        <%  }
           } else { %>
            <p style="color: #64748b; padding: 10px;">Chưa có dữ liệu thiết bị.</p>
        <% } %>
        </div>

        <div class="hk-history-meta" style="margin-top: 20px;">
            <span>Bắt đầu: <strong><%= task.getStartedAt() == null ? "--" : task.getStartedAt() %></strong></span>
            <span>Hoàn thành: <strong><%= task.getCompletedAt() == null ? "--" : task.getCompletedAt() %></strong></span>
        </div>
        <div class="hk-form-actions" style="margin-top: 20px;">
            <a href="<%= backUrl %>">Quay lại danh sách</a>
        </div>
    </section>
    <% } else if (inspection) { %>
    <form id="inspection-form" class="hk-card hk-work-form" method="post"
          action="<%= contextPath %>/housekeeping/tasks/complete-inspection" novalidate>
        <input type="hidden" name="taskId" value="<%= task.getTaskId() %>">
        <div class="hk-section-heading"><div><h2>Kiểm tra thiết bị trong phòng</h2>
            <p>Kiểm tra lần lượt từng thiết bị trước khi xác nhận hoàn tất.</p></div>
            <span><%= equipment == null ? 0 : equipment.size() %> thiết bị</span></div>

        <% if (task.getNote() != null && !task.getNote().isBlank() && !"Kiểm tra phòng sau checkout".equalsIgnoreCase(task.getNote().trim()) && !"Kiểm tra phòng".equalsIgnoreCase(task.getNote().trim())) { %>
        <div class="hk-manager-note-box" style="margin: 0 0 20px 0; padding: 16px 20px; background: #fffbe6; border: 1.5px solid #ffe58f; border-left: 5px solid #faad14; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.05);">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
                <span style="font-size: 18px;">📌</span>
                <strong style="color: #d46b08; font-size: 15px; text-transform: uppercase; letter-spacing: 0.5px;">Ghi chú & Lời dặn từ Quản lý:</strong>
            </div>
            <p style="margin: 0; color: #434343; font-size: 14.5px; font-weight: 500; line-height: 1.6; white-space: pre-wrap;"><%= HousekeepingTask.esc(task.getNote()) %></p>
        </div>
        <% } %>

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
            <div style="margin-top: 16px;">
                <label class="hk-wide" style="display: flex; flex-direction: column; gap: 6px; font-weight: 600; color: #344054;">
                    <span>Đầu việc cần dọn thêm (tự nhập thủ công nếu có)</span>
                    <textarea name="customCleaningTasks" rows="3" maxlength="1500" placeholder="Nhập các việc phát sinh ngoài danh mục trên (mỗi việc 1 dòng)&#10;Ví dụ: Tẩy vết rượu trên thảm&#10;Xịt khử mùi thuốc lá ban công" style="width: 100%; border: 1.5px solid var(--hk-border); border-radius: var(--radius-md); padding: 10px 12px; font-family: inherit; font-size: 14px; resize: vertical;"></textarea>
                    <small style="color: #667085; font-weight: 400;">Mỗi dòng sẽ được đưa vào danh sách Checklist dọn phòng cho Housekeeper tích chọn.</small>
                </label>
            </div>
        </section>

        <div class="hk-equipment-list">
        <% if (equipment != null) for (HousekeepingTask.EquipmentCheck item : equipment) {
            String id = String.valueOf(item.getRoomEquipmentId()); %>
            <fieldset class="hk-equipment-item">
                <legend><span class="hk-equipment-icon" aria-hidden="true">⚙️</span>
                    <strong><%= HousekeepingTask.esc(item.getEquipmentName()) %></strong>
                    <small>Số lượng hiện có: <%= item.getQuantity() %> · Lúc check-in:
                        <%= item.getInitialStatus() == null ? "Không có snapshot" : HousekeepingTask.esc(item.getInitialStatus()) + " / SL " + item.getInitialQuantity() %></small></legend>
                <label><span class="hk-label-title">Tình trạng thực tế <span class="required">*</span></span>
                    <select class="condition-select" name="condition_<%= id %>" required>
                        <option value="NORMAL" selected>Bình thường</option>
                        <option value="DAMAGED">Hư hỏng</option>
                        <option value="MISSING">Thất lạc</option>
                    </select><small class="field-error">Vui lòng chọn tình trạng.</small>
                </label>
                <label class="hk-wide">Ghi chú sự cố
                    <input class="note-input" type="text" name="note_<%= id %>" maxlength="1000" placeholder="Mô tả vị trí, mức độ hư hỏng hoặc tình trạng thất lạc">
                    <small class="note-error" style="display: none; color: #d32f2f;">Vui lòng nhập ghi chú khi có sự cố</small>
                </label>
            </fieldset>
        <% } %>
        </div>
        <label class="hk-form-label">Ghi chú chung
            <textarea name="inspectionNote" maxlength="2000" rows="4" placeholder="Thông tin bổ sung sau khi kiểm tra phòng"></textarea>
            <small>Tối đa 2.000 ký tự.</small>
        </label>
        <div class="hk-form-actions">
            <a href="<%= backUrl %>">Quay lại</a>
            <button class="hk-primary" type="submit">Hoàn tất kiểm tra</button>
        </div>
    </form>
    <% } else { %>
    <section class="hk-cleaning-layout">
      <div class="hk-card hk-cleaning-main">
        <div class="hk-section-heading">
            <div>
                <h2>Những việc cần làm</h2>
                <p><%= history || isManager ? "Danh sách các đầu việc và tiến độ dọn phòng." : (pending ? "Danh sách các đầu việc cần thực hiện. Bấm 'Bắt đầu dọn phòng' để tiến hành ghi nhận." : "Đánh dấu lần lượt các đầu việc sau khi hoàn tất để tự động lưu tiến độ.") %></p>
            </div>
            <% if (!history && !pending && !isManager) { %>
                <span id="save-status" class="hk-save-status">✓ Tiến độ tự động lưu</span>
            <% } %>
        </div>

        <% if (!pending && workItems != null && !workItems.isEmpty()) { 
            int total = workItems.size();
            int done = 0;
            for (HousekeepingTask.WorkItem wi : workItems) if (wi.isCompleted()) done++;
            int percent = (int) Math.round((done * 100.0) / total);
        %>
            <div class="hk-progress-wrap">
                <div class="hk-progress-header">
                    <span>Tiến độ dọn phòng</span>
                    <strong id="progress-text"><%= done %> / <%= total %> việc (<%= percent %>%)</strong>
                </div>
                <div class="hk-progress-track">
                    <div id="progress-bar" class="hk-progress-fill" style="width: <%= percent %>%;"></div>
                </div>
            </div>
        <% } %>

        <% if (history || isManager) { %>
            <div class="hk-checklist-group">
                <% if (workItems != null && !workItems.isEmpty()) { 
                    for (HousekeepingTask.WorkItem item : workItems) { 
                        boolean done = item.isCompleted() || history;
                %>
                    <div class="hk-checklist-item <%= done ? "is-done" : "" %>" style="cursor: default;">
                        <span class="hk-check-done-tag"><%= done ? "✓ Đã dọn" : "⏳ Chưa dọn" %></span>
                        <span class="hk-check-text"><%= HousekeepingTask.esc(item.getName()) %></span>
                    </div>
                <%  } 
                   } else { %>
                    <div class="hk-checklist-item <%= history ? "is-done" : "" %>" style="cursor: default;">
                        <span class="hk-check-done-tag"><%= history ? "✓ Đã dọn" : "⏳ Chưa dọn" %></span>
                        <span class="hk-check-text">Dọn vệ sinh tổng quát và kiểm tra lại phòng</span>
                    </div>
                <% } %>
            </div>
        <% } else if (pending) { %>
            <div class="hk-checklist-group">
                <% if (workItems != null && !workItems.isEmpty()) { 
                    for (HousekeepingTask.WorkItem item : workItems) { 
                %>
                    <div class="hk-checklist-item" style="cursor: default;">
                        <input type="checkbox" disabled style="width: 18px; height: 18px; margin: 0; cursor: default;">
                        <span class="hk-check-text"><%= HousekeepingTask.esc(item.getName()) %></span>
                    </div>
                <%  } 
                   } else { %>
                    <div class="hk-checklist-item" style="cursor: default;">
                        <input type="checkbox" disabled style="width: 18px; height: 18px; margin: 0; cursor: default;">
                        <span class="hk-check-text">Dọn vệ sinh tổng quát và kiểm tra lại phòng</span>
                    </div>
                <% } %>
            </div>
        <% } else { %>
            <div class="hk-checklist-group" id="cleaning-checklist">
                <% if (workItems != null && !workItems.isEmpty()) { 
                    for (HousekeepingTask.WorkItem item : workItems) { 
                        boolean isDone = item.isCompleted();
                %>
                    <label class="hk-checklist-item <%= isDone ? "is-done" : "" %>">
                        <input type="checkbox" class="hk-check-input" name="completedItem" 
                               value="<%= HousekeepingTask.esc(item.getName()) %>" <%= isDone ? "checked" : "" %>>
                        <span class="hk-check-text"><%= HousekeepingTask.esc(item.getName()) %></span>
                        <% if (isDone) { %><span class="hk-check-done-tag">Đã xong</span><% } %>
                    </label>
                <%  } 
                   } else { %>
                    <label class="hk-checklist-item">
                        <input type="checkbox" class="hk-check-input" name="completedItem" value="Dọn vệ sinh tổng quát và kiểm tra lại phòng">
                        <span class="hk-check-text">Dọn vệ sinh tổng quát và kiểm tra lại phòng</span>
                    </label>
                <% } %>
            </div>
        <% } %>

        <% if (inspectionMessage != null && !inspectionMessage.isBlank()) { %>
        <div class="hk-note-box" style="margin-top: 16px;">
            <strong>Ghi chú từ người giao việc:</strong>
            <p><%= HousekeepingTask.esc(inspectionMessage) %></p>
        </div>
        <% } %>

        <% if (!history && !isManager) { %>
        <form method="post" id="cleaning-form" action="<%= contextPath %>/housekeeping/tasks/<%= pending ? "start-cleaning" : "complete-cleaning" %>">
            <input type="hidden" name="taskId" value="<%= task.getTaskId() %>">
            <div class="hk-form-actions">
                <a href="<%= backUrl %>">Quay lại</a>
                <button class="hk-primary" id="btn-submit-cleaning" type="submit"><%= pending ? "Bắt đầu dọn phòng" : "Hoàn tất dọn phòng" %></button>
            </div>
        </form>
        <% } else { %>
            <div class="hk-history-meta" style="margin-top: 20px;">
                <span>Bắt đầu: <strong><%= task.getFormattedStartedAt() %></strong></span>
                <span>Hoàn thành: <strong><%= task.getFormattedCompletedAt() %></strong></span>
            </div>
            <div class="hk-form-actions" style="margin-top: 20px;">
                <a href="<%= backUrl %>">Quay lại danh sách</a>
            </div>
        <% } %>
      </div>

      <aside class="hk-card hk-maintenance-check">
        <div class="hk-section-heading">
            <div>
                <h2>Kiểm tra thiết bị</h2>
                <p>Tình trạng thiết bị liên quan đến phòng này.</p>
            </div>
        </div>
        <% if (equipment == null || equipment.isEmpty()) { %>
            <div class="hk-all-clear"><span aria-hidden="true">✓</span><p>Không có thiết bị hỏng hoặc thất lạc được ghi nhận.</p></div>
            <% if (!history && !isManager) { %>
            <div style="margin-top: 14px;">
                <a href="<%= contextPath %>/housekeeping/issues/report?roomId=<%= task.getRoomId() %>" class="hk-primary" style="display:inline-block; padding: 10px 20px; text-decoration: none;">Báo cáo sự cố mới</a>
            </div>
            <% } %>
        <% } else { %>
            <div class="hk-maintenance-list">
            <% for (HousekeepingTask.EquipmentCheck item : equipment) {
                boolean repaired = "NORMAL".equals(item.getCurrentStatus()); %>
                <article>
                    <div>
                        <strong><%= HousekeepingTask.esc(item.getEquipmentName()) %></strong>
                        <small><%= HousekeepingTask.esc(item.getNote()) %></small>
                    </div>
                    <span class="hk-badge <%= repaired ? "condition-normal" : "condition-damaged" %>">
                        <%= repaired ? "Đã xử lý" : "Chưa xử lý" %>
                    </span>
                </article>
            <% } %>
            </div>
            <p class="hk-maintenance-help">Nếu thiết bị chưa được xử lý, phòng sẽ ở trạng thái NOT_READY hoặc MAINTENANCE.</p>
        <% } %>
      </aside>
    </section>
    <% } %>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

<% if (!isManager && !history) { %>
<script>
(() => {
    // Inspection form validation
    const form = document.getElementById('inspection-form');
    if (form) {
        form.addEventListener('submit', event => {
            let valid = true;
            form.querySelectorAll('.condition-select').forEach(select => {
                const group = select.closest('label');
                const fieldset = select.closest('fieldset');
                const error = group ? group.querySelector('.field-error') : null;
                const noteInput = fieldset ? fieldset.querySelector('.note-input') : null;
                const noteError = fieldset ? fieldset.querySelector('.note-error') : null;

                if (!select.value) {
                    valid = false;
                    if (error) error.style.display = 'block';
                } else if (error) {
                    error.style.display = 'none';
                }

                if (select.value === 'DAMAGED' || select.value === 'MISSING') {
                    if (noteInput && !noteInput.value.trim()) {
                        valid = false;
                        if (noteError) noteError.style.display = 'block';
                    } else if (noteError) {
                        noteError.style.display = 'none';
                    }
                } else if (noteError) {
                    noteError.style.display = 'none';
                }
            });
            if (!valid) event.preventDefault();
        });
    }

    // Cleaning checklist AJAX Auto-Save
    const list = document.getElementById('cleaning-checklist');
    const statusEl = document.getElementById('save-status');
    const progressBar = document.getElementById('progress-bar');
    const progressText = document.getElementById('progress-text');
    let timeoutId = null;

    if (list) {
        list.addEventListener('change', event => {
            if (!event.target.classList.contains('hk-check-input')) return;
            const label = event.target.closest('.hk-checklist-item');
            if (label) {
                label.classList.toggle('is-done', event.target.checked);
                let tag = label.querySelector('.hk-check-done-tag');
                if (event.target.checked) {
                    if (!tag) {
                        tag = document.createElement('span');
                        tag.className = 'hk-check-done-tag';
                        tag.textContent = 'Đã xong';
                        label.appendChild(tag);
                    }
                } else if (tag) {
                    tag.remove();
                }
            }

            const inputs = list.querySelectorAll('.hk-check-input');
            const total = inputs.length;
            let checked = 0;
            inputs.forEach(i => { if (i.checked) checked++; });
            const percent = total > 0 ? Math.round((checked * 100) / total) : 0;
            if (progressBar) progressBar.style.width = percent + '%';
            if (progressText) progressText.textContent = checked + ' / ' + total + ' việc (' + percent + '%)';

            if (statusEl) {
                statusEl.textContent = 'Đang lưu tiến độ...';
                statusEl.classList.add('saving');
            }

            if (timeoutId) clearTimeout(timeoutId);
            timeoutId = setTimeout(() => {
                const checkedItems = [];
                inputs.forEach(i => { if (i.checked) checkedItems.push(i.value); });
                const params = new URLSearchParams();
                params.append('taskId', '<%= task.getTaskId() %>');
                checkedItems.forEach(item => params.append('completedItem', item));

                fetch('<%= contextPath %>/housekeeping/tasks/save-progress', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                    body: params.toString()
                })
                .then(r => r.json())
                .then(data => {
                    if (statusEl) {
                        statusEl.classList.remove('saving');
                        if (data.success) {
                            statusEl.textContent = '✓ Đã tự động lưu';
                            statusEl.classList.add('saved');
                        } else {
                            statusEl.textContent = '⚠ Lưu thất bại';
                        }
                    }
                })
                .catch(() => {
                    if (statusEl) {
                        statusEl.classList.remove('saving');
                        statusEl.textContent = '⚠ Mất kết nối mạng';
                    }
                });
            }, 600);
        });
    }
})();
</script>
<% } %>
</body>
</html>