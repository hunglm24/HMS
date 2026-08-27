<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="room-management-modal" id="taskModal" aria-hidden="true">
    <div class="room-management-modal__backdrop" data-task-mgmt-close="true"></div>
    <div class="room-management-modal__dialog">
        <header class="room-management-modal__header">
            <div>
                <p class="room-management-kicker">Phân công công việc</p>
                <h2 id="taskModalTitle">Giao việc phòng</h2>
            </div>
            <button class="icon-button" type="button" data-task-mgmt-close="true" aria-label="Đóng hộp thoại">&times;</button>
        </header>

        <form class="room-management-form" method="post" action="${pageContext.request.contextPath}/manager/rooms/create-task">
            <input type="hidden" name="roomId" id="taskRoomId">

            <div class="form-group">
                <label for="taskRoomNumber">Phòng</label>
                <input id="taskRoomNumber" type="text" readonly disabled class="form-control-readonly">
            </div>

            <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-3);">
                <div class="form-group">
                    <label for="taskTypeSelect">Loại công việc *</label>
                    <select id="taskTypeSelect" name="taskType" required onchange="document.getElementById('cleaningTasksGroup').style.display = (this.value === 'CHECKOUT_INSPECTION' ? 'none' : 'block');">
                        <option value="CHECKOUT_INSPECTION" selected>Kiểm tra phòng (Kiểm tra thiết bị)</option>
                        <option value="CLEANING">Dọn phòng (Theo Checklist việc cần làm)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="taskPriority">Mức độ ưu tiên *</label>
                    <select id="taskPriority" name="priority" required>
                        <option value="NORMAL">Bình thường</option>
                        <option value="HIGH">Ưu tiên cao (Gấp)</option>
                    </select>
                </div>
            </div>

            <!-- Khung thông tin phân công Housekeeping -->
            <div class="task-assignment-section">
                <div class="task-assignment-header">
                    <label for="taskAssigneeSelect">Phân công nhân sự Housekeeping</label>
                </div>

                <!-- Thẻ thông tin người kiểm tra mặc định theo tầng -->
                <c:set var="firstHk" value="${not empty housekeeperWorkloads ? housekeeperWorkloads[0] : null}" />
                <div class="task-default-hk-card" id="taskDefaultHkCard">
                    <div class="task-default-hk-info">
                        <div class="task-default-hk-title">
                            <span class="hk-icon">👤</span>
                            <span class="hk-label">Người kiểm tra theo tầng:</span>
                            <strong id="taskDefaultHkName">
                                <c:choose>
                                    <c:when test="${not empty firstHk}">
                                        <c:out value="${firstHk.fullName}" /> (Phụ trách Tầng 1 - 2)
                                    </c:when>
                                    <c:otherwise>Chưa có nhân viên Housekeeping</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                        <div id="taskDefaultHkBadgeWrap">
                            <span class="hk-status-badge ${not empty firstHk ? firstHk.statusBadgeClass : 'badge-pending'}" id="taskDefaultHkBadge">
                                <c:choose>
                                    <c:when test="${not empty firstHk}">
                                        <c:out value="${firstHk.statusBadgeText}" />
                                    </c:when>
                                    <c:otherwise>Chưa phân công</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                    <div class="task-default-hk-sub" id="taskDefaultHkSub">
                        <span>Hôm nay: <strong>${not empty firstHk ? firstHk.completedToday : 0}</strong> phòng đã hoàn thành</span>
                    </div>
                </div>

                <!-- Dropdown chỉ định nhân viên -->
                <div class="task-assignee-select-wrap">
                    <select id="taskAssigneeSelect" name="assignedTo">
                        <option value="" selected>-- Mặc định: Tự động giao cho HK theo tầng --</option>
                        <c:forEach var="hk" items="${housekeeperWorkloads}">
                            <option value="${hk.userId}"
                                    data-user-id="${hk.userId}"
                                    data-full-name="${hk.fullName}"
                                    data-in-progress="${hk.inProgressCount}"
                                    data-pending="${hk.pendingCount}"
                                    data-current-room="${hk.currentRoomNumber}"
                                    data-completed-today="${hk.completedToday}"
                                    data-badge-text="${hk.statusBadgeText}"
                                    data-badge-class="${hk.statusBadgeClass}">
                                <c:out value="${hk.fullName}" /> &mdash; [${hk.statusBadgeText}] (Xong: ${hk.completedToday})
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Cảnh báo thời gian thực khi chọn người đang bận -->
                <div id="taskQueueNotice" class="task-queue-notice" style="display: none;">
                    <span class="notice-icon">ℹ️</span>
                    <span id="taskQueueNoticeText">Nhân viên này đang bận. Công việc sẽ được xếp vào hàng đợi chờ xử lý.</span>
                </div>
            </div>

            <div class="form-group" id="cleaningTasksGroup" style="display: none;">
                <label for="taskCleaningTasks">Danh sách việc cần làm (Checklist)</label>
                <textarea id="taskCleaningTasks" name="cleaningTasks" rows="3" maxlength="1500" placeholder="Dọn vệ sinh WC&#10;Thay ga gối phòng ngủ&#10;Hút bụi và lau sàn"></textarea>
                <small style="display:block; margin-top:5px; color:var(--color-gray-500); font-size:12px;">Mỗi dòng sẽ trở thành một mục trong Checklist để Housekeeper tích chọn khi dọn xong.</small>
            </div>

            <div class="form-group">
                <label for="taskNote">Ghi chú / Lời dặn dò</label>
                <textarea id="taskNote" name="note" rows="2" maxlength="1000" placeholder="Ví dụ: Khách VIP nhận phòng sớm 14h, lưu ý dọn ưu tiên phòng này..."></textarea>
                <small style="display:block; margin-top:5px; color:var(--color-gray-500); font-size:12px;">Nội dung này sẽ hiển thị ở ô 'Ghi chú từ người giao việc' trong Task Detail.</small>
            </div>

            <div class="room-management-form__actions">
                <button class="btn btn-secondary" type="button" data-task-mgmt-close="true">Hủy</button>
                <button class="btn" type="submit">Giao việc</button>
            </div>
        </form>
    </div>
</div>

<%-- Dữ liệu JSON Workload của đội ngũ Housekeeping để JS xử lý realtime --%>
<script type="application/json" id="housekeeperWorkloadData">
[
<c:forEach var="hk" items="${housekeeperWorkloads}" varStatus="st">
  {
    "userId": ${hk.userId},
    "fullName": "<c:out value="${hk.fullName}" />",
    "phone": "<c:out value="${hk.phone}" />",
    "inProgressCount": ${hk.inProgressCount},
    "pendingCount": ${hk.pendingCount},
    "completedToday": ${hk.completedToday},
    "currentRoomNumber": "<c:out value="${hk.currentRoomNumber}" />",
    "currentFloor": ${empty hk.currentFloor ? "null" : hk.currentFloor},
    "badgeText": "<c:out value="${hk.statusBadgeText}" />",
    "badgeClass": "<c:out value="${hk.statusBadgeClass}" />",
    "isBusy": ${hk.busy}
  }<c:if test="${!st.last}">,</c:if>
</c:forEach>
]
</script>