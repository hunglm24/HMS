<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="room-management-modal" id="roomTypeModal" aria-hidden="true">
    <div class="room-management-modal__backdrop" data-room-mgmt-close="true"></div>
    <div class="room-management-modal__dialog">
        <header class="room-management-modal__header">
            <div>
                <p class="room-management-kicker">Loại phòng</p>
                <h2 id="roomTypeModalTitle">Thêm loại phòng</h2>
            </div>
            <button class="icon-button" type="button" data-room-mgmt-close="true" aria-label="Đóng hộp thoại">×</button>
        </header>

        <form class="room-management-form" method="post" action="${pageContext.request.contextPath}/manager/room-types/save-room-type">
            <input type="hidden" name="id" id="roomTypeIdField">

            <label for="roomTypeName">Tên loại phòng *</label>
            <input id="roomTypeName" name="name" type="text" required maxlength="100">

            <label for="roomTypePrice">Giá cơ bản *</label>
            <input id="roomTypePrice" name="basePrice" type="number" min="1" max="999999999999999" step="1" inputmode="numeric" autocomplete="off" required>
            <small class="room-management-form__hint" id="roomTypePriceHint">Nhập số nguyên dương, ví dụ 1800000. Tối đa 999999999999999.</small>

            <label for="roomTypeCapacity">Sức chứa *</label>
            <input id="roomTypeCapacity" name="capacity" type="number" min="1" step="1" required>

            <label for="roomTypeDescription">Mô tả</label>
            <textarea id="roomTypeDescription" name="description" rows="4" maxlength="500"></textarea>

            <label for="roomTypeStatus">Trạng thái</label>
            <select id="roomTypeStatus" name="status">
                <option value="ACTIVE">Hoạt động</option>
                <option value="INACTIVE">Ngừng hoạt động</option>
            </select>

            <div class="room-management-form__actions">
                <button class="btn btn-secondary" type="button" data-room-mgmt-close="true">Hủy</button>
                <button class="btn" type="submit">Lưu</button>
            </div>
        </form>
    </div>
</div>
