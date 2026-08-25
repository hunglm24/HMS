<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hủy booking và hoàn tiền | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .refund-layout { display:grid; grid-template-columns:minmax(0,1fr) minmax(300px,.65fr); gap:24px; }
        .refund-summary { background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:20px; }
        .refund-row { display:flex; justify-content:space-between; gap:16px; padding:10px 0; border-bottom:1px solid #e2e8f0; }
        .refund-row:last-child { border-bottom:0; }
        .refund-amount { color:#166534; font-size:1.25rem; }
        @media (max-width:800px) { .refund-layout { grid-template-columns:1fr; } }
    </style>
</head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">
    <section class="section-head">
        <div><p class="section-kicker">Manager</p><h1>Hủy booking và hoàn tiền</h1>
            <p>Kiểm tra chính sách và nhập tài khoản nhận hoàn tiền.</p></div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/bookings">Quay lại</a>
    </section>
    <div class="refund-layout">
        <form class="preview-card form-panel" method="post" action="${pageContext.request.contextPath}/manager/bookings/refund"
              onsubmit="return confirm('Xác nhận hủy booking và tạo yêu cầu hoàn tiền?');">
            <input type="hidden" name="id" value="${booking.id}">
            <h2>Thông tin nhận tiền</h2>
            <div class="form-grid">
                <label>Ngân hàng
                    <select name="bankName" required>
                        <option value="">Chọn ngân hàng</option>
                        <option>Vietcombank</option><option>VietinBank</option><option>BIDV</option>
                        <option>Agribank</option><option>Techcombank</option><option>MB Bank</option>
                        <option>ACB</option><option>VPBank</option><option>TPBank</option><option>Khác</option>
                    </select>
                </label>
                <label>Chủ tài khoản<input name="accountHolder" maxlength="150" required placeholder="NGUYEN VAN A"></label>
                <label>Số tài khoản<input name="accountNumber" maxlength="30" inputmode="numeric" pattern="[0-9]{6,30}" required></label>
                <label style="grid-column:1/-1">Lý do hủy<textarea name="reason" maxlength="500" rows="4" required></textarea></label>
            </div>
            <button class="btn" style="background:#b91c1c" type="submit">Xác nhận hủy và tạo refund</button>
        </form>
        <aside class="refund-summary">
            <h2>Chi tiết hoàn tiền</h2>
            <div class="refund-row"><span>Booking</span><strong><c:out value="${booking.bookingCode}"/></strong></div>
            <div class="refund-row"><span>Tổng booking</span><strong><fmt:formatNumber value="${booking.totalAmount}" pattern="#,##0"/> ₫</strong></div>
            <div class="refund-row"><span>Tỷ lệ hoàn</span><strong><fmt:formatNumber value="${refund.refundRate}" pattern="#0.##"/>%</strong></div>
            <div class="refund-row"><span>Phí hủy</span><strong><fmt:formatNumber value="${refund.cancellationFee}" pattern="#,##0"/> ₫</strong></div>
            <div class="refund-row"><span>Số tiền hoàn dự kiến</span><strong class="refund-amount"><fmt:formatNumber value="${refund.refundAmount}" pattern="#,##0"/> ₫</strong></div>
            <p><small>Yêu cầu sẽ được lưu ở trạng thái <strong>PENDING</strong> để xử lý chuyển khoản.</small></p>
        </aside>
    </div>
</main></body></html>
