<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="modal" id="recordPaymentModal" aria-hidden="true">
    <div class="modal-content"><div class="modal-header"><h3>Ghi nhận thanh toán</h3><button type="button" class="close-btn" data-close-modal>&times;</button></div>
        <form method="post" action="${pageContext.request.contextPath}/api/payments"><label>Số tiền<input type="number" name="amount" min="0" required></label><label>Phương thức<select name="method"><option>Tiền mặt</option><option>Thẻ</option><option>VNPay</option></select></label><button type="submit">Lưu thanh toán</button></form>
    </div>
</div>
