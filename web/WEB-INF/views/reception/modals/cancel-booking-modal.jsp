<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="modal" id="cancelBookingModal" aria-hidden="true">
    <div class="modal-content"><div class="modal-header"><h3>Hủy booking</h3><button type="button" class="close-btn" data-close-modal>&times;</button></div>
        <form method="post" action="${pageContext.request.contextPath}/reception/bookings/cancel"><label>Lý do hủy<textarea name="reason" required></textarea></label><button class="btn-danger" type="submit">Xác nhận hủy</button></form>
    </div>
</div>
