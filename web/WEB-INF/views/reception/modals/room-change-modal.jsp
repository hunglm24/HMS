<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="modal" id="roomChangeModal" aria-hidden="true">
    <div class="modal-content"><div class="modal-header"><h3>Đổi phòng</h3><button type="button" class="close-btn" data-close-modal>&times;</button></div>
        <form method="post" action="${pageContext.request.contextPath}/api/room-assignments"><label>Phòng mới<input name="roomNumber" required></label><label>Ghi chú<textarea name="note"></textarea></label><button type="submit">Cập nhật phòng</button></form>
    </div>
</div>
