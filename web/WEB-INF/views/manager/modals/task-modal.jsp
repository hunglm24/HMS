<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="room-management-modal" id="taskModal" aria-hidden="true">
    <div class="room-management-modal__backdrop" data-task-mgmt-close="true"></div>
    <div class="room-management-modal__dialog">
        <header class="room-management-modal__header">
            <div>
                <p class="room-management-kicker">Công việc</p>
                <h2 id="taskModalTitle">Giao việc</h2>
            </div>
            <button class="icon-button" type="button" data-task-mgmt-close="true" aria-label="Đóng hộp thoại">×</button>
        </header>

        <form class="room-management-form" method="post" action="${pageContext.request.contextPath}/manager/rooms/create-task">
            <input type="hidden" name="roomId" id="taskRoomId">

            <label for="taskRoomNumber">Phòng</label>
            <input id="taskRoomNumber" type="text" readonly disabled>

            <label for="taskTypeSelect">Loại công việc *</label>
            <select id="taskTypeSelect" name="taskType" required>
                <option value="PERIODIC_INSPECTION">Kiểm tra định kỳ</option>
                <option value="CLEANING">Dọn phòng</option>
            </select>
            


            <label for="taskPriority">Mức độ ưu tiên *</label>
            <select id="taskPriority" name="priority" required>
                <option value="NORMAL">Bình thường</option>
                <option value="HIGH">Cao</option>
            </select>

            <label for="taskNote">Ghi chú *</label>
            <textarea id="taskNote" name="note" rows="3" maxlength="2000" required placeholder="Nhập yêu cầu công việc..."></textarea>

            <div class="room-management-form__actions">
                <button class="btn btn-secondary" type="button" data-task-mgmt-close="true">Hủy</button>
                <button class="btn" type="submit">Giao việc</button>
            </div>
        </form>
    </div>
</div>
