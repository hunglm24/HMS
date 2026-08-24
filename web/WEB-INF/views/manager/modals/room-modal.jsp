<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="room-management-modal" id="roomModal" aria-hidden="true">
  <div
    class="room-management-modal__backdrop"
    data-room-mgmt-close="true"
  ></div>
  <div class="room-management-modal__dialog">
    <header class="room-management-modal__header">
      <div>
        <p class="room-management-kicker">Phòng</p>
        <h2 id="roomModalTitle">Thêm phòng</h2>
      </div>
      <button
        class="icon-button"
        type="button"
        data-room-mgmt-close="true"
        aria-label="Đóng hộp thoại"
      >
        ×
      </button>
    </header>

    <form
      class="room-management-form"
      method="post"
      action="${pageContext.request.contextPath}/manager/rooms/save-room"
    >
      <input type="hidden" name="id" id="roomId" />

      <label for="roomNumber">Số phòng *</label>
      <input
        id="roomNumber"
        name="roomNumber"
        type="text"
        inputmode="numeric"
        pattern="[0-9]+"
        required
        maxlength="20"
      />

      <label for="roomFloor">Tầng</label>
      <input
        id="roomFloor"
        name="floorNumber"
        type="number"
        min="1"
        max="4"
        step="1"
      />

      <label for="roomTypeSelect">Loại phòng *</label>
      <select id="roomTypeSelect" name="roomTypeId" required>
        <option value="">Chọn loại phòng</option>
        <c:forEach var="option" items="${roomTypeOptions}">
          <option value="${option.id}">
            <c:out value="${option.name}" />
          </option>
        </c:forEach>
      </select>

      <label for="roomStatus">Trạng thái</label>
      <!-- <select id="roomStatus" name="status">
        <option value="AVAILABLE">Trống</option>
        <option value="OCCUPIED">Đang có khách</option>
        <option value="CLEANING">Đang dọn</option>
        <option value="MAINTENANCE">Bảo trì</option>
        <option value="NOT_READY">Chưa sẵn sàng</option>
        <option value="INSPECTION">Chờ kiểm tra</option>
      </select> -->
      <c:choose>
        <c:when
          test="${isEditMode and (form.status eq 'OCCUPIED' or form.status eq 'NOT_READY')}"
        >
          <input
            type="hidden"
            id="roomStatus"
            name="status"
            value="${form.status}"
          />
          <div class="room-status-readonly">
            <c:out value="${form.status}" />
          </div>
        </c:when>
        <c:otherwise>
          <select id="roomStatus" name="status">
            <option value="AVAILABLE">Trống</option>
            <option value="CLEANING">Đang dọn</option>
            <option value="MAINTENANCE">Bảo trì</option>
            <option value="INSPECTION">Chờ kiểm tra</option>
            <option value="OCCUPIED" disabled>Đang có khách</option>
            <option value="NOT_READY" disabled>Chưa sẵn sàng</option>
          </select>
        </c:otherwise>
      </c:choose>

      <label for="roomDescription">Mô tả</label>
      <textarea
        id="roomDescription"
        name="description"
        rows="3"
        maxlength="500"
      ></textarea>

      <div class="room-management-form__actions">
        <button
          class="btn btn-secondary"
          type="button"
          data-room-mgmt-close="true"
        >
          Hủy
        </button>
        <button class="btn" type="submit">Lưu</button>
      </div>
    </form>
  </div>
</div>
