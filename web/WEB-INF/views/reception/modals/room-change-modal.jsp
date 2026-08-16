<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Room" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="roomChangeBackdrop" class="room-change-backdrop" aria-hidden="true"></div>
<section id="roomChangeModal"
         class="room-change-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="roomChangeTitle"
         aria-hidden="true">
    <div class="room-change-modal__header">
        <div>
            <h3 id="roomChangeTitle">Đổi phòng</h3>
        </div>
        <button type="button" id="roomChangeCloseBtn" class="room-change-modal__close" aria-label="Đóng modal">×</button>
    </div>

    <form id="roomChangeForm" class="room-change-modal__body" action="${pageContext.request.contextPath}/reception/room-change" method="post">
        <%-- Hidden fields carry the selected booking context to the controller --%>
        <input type="hidden" name="bookingId" id="roomChangeBookingId">
        <input type="hidden" name="currentRoomId" id="roomChangeCurrentRoomId">
        <input type="hidden" name="currentRoomNumber" id="roomChangeCurrentRoomNumber">

        <div class="room-change-summary">
            <div class="booking-detail-card">
                <div class="booking-detail-card__top">
                    <div class="booking-detail-card__label-group">
                        <span class="booking-detail-card__label">Booking</span>
                        <strong id="roomChangeBookingCode" class="booking-detail-card__code">--</strong>
                    </div>
                    <span id="roomChangeCurrentStatus" class="status-badge status-checked-in booking-detail-card__status">--</span>
                </div>

                <div class="booking-detail-card__divider"></div>

                <div class="booking-detail-card__bottom">
                    <div class="booking-detail-card__field">
                        <span class="booking-detail-card__label">Guest</span>
                        <strong id="roomChangeGuestName" class="booking-detail-card__value">--</strong>
                    </div>
                    <div class="booking-detail-card__field booking-detail-card__field--right">
                        <span class="booking-detail-card__label">Current Room</span>
                        <strong id="roomChangeCurrentRoomLabel" class="booking-detail-card__value">--</strong>
                    </div>
                </div>
            </div>
        </div>

        <div class="room-change-note">
            <strong>Mode:</strong>
            <span>Post check-in → Swap room</span>
        </div>

        <div class="room-change-grid">
            <div class="form-field">
                <label for="roomChangeCurrentRoomSelect">Current selection</label>
                <input id="roomChangeCurrentRoomSelect" type="text" readonly>
            </div>

            <div class="form-field">
                <label for="roomChangeNewRoomId">New option</label>
                <select name="newRoomId" id="roomChangeNewRoomId" required>
                    <option value="">Select an available room...</option>
                    <%-- Reuse the roomsByFloor data that already powers the room map --%>
                    <c:forEach items="${roomsByFloor}" var="entry">
                        <c:forEach items="${entry.value}" var="room">
                            <c:if test="${fn:toUpperCase(room.status) eq 'AVAILABLE'}">
                                <option value="${room.id}"
                                        data-room-number="${room.roomNumber}"
                                        data-room-type="${room.roomTypeName}">
                                    ${room.roomNumber} - ${room.roomTypeName}
                                </option>
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                </select>
            </div>

            <div class="room-change-delta">
                <span>Chênh lệch giá</span>
                <strong id="roomChangePriceDiff">Sẽ tính tự động</strong>
            </div>

            <div class="form-field room-change-reason">
                <label for="roomChangeReason">Reason</label>
                <textarea name="reason" id="roomChangeReason" placeholder="Enter reason for room change..." maxlength="500" required></textarea>
            </div>
        </div>

        <p id="roomChangeHint" class="room-change-hint">
            Chọn một phòng trống phù hợp. Giá chênh lệch sẽ được xử lý theo quy trình receptionist.
        </p>

        <div class="room-change-modal__actions">
            <button type="button" id="roomChangeCancelBtn" class="btn btn-secondary">Cancel</button>
            <button type="submit" id="roomChangeConfirmBtn" class="btn btn-primary">Confirm Change</button>
        </div>
    </form>
</section>
