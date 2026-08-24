<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="toastMessage" value="${sessionScope.toastMessage}" />
<c:set var="toastType" value="${sessionScope.toastType}" />
<c:remove var="toastMessage" scope="session" />
<c:remove var="toastType" scope="session" />
<c:set var="isEdit" value="${not empty policy}" />
<c:set var="rule" value="${cancellationRule}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${isEdit ? 'Sửa chính sách' : 'Tạo chính sách'} | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260821-1">
    <style>
        .manager-content { min-width: 0; background: #f6f8fb; }
        .manager-section { background: #fff; border: 1px solid #d9e0ea; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
        .section-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; }
        .section-head h1 { margin-bottom: 6px; }
        .section-head p { margin: 0; }
        .section-title { margin-bottom: 16px; }
        .section-title h2 { margin: 0 0 6px; font-size: 1.25rem; }
        .section-title p { margin: 0; color: #526174; }
        .manager-form { display: grid; grid-template-columns: repeat(4, minmax(160px, 1fr)); gap: 12px; align-items: end; }
        .manager-form label { display: grid; gap: 6px; font-size: .9rem; font-weight: 700; color: #253246; }
        .manager-form input, .manager-form select, .manager-form textarea { width: 100%; min-height: 40px; border: 1px solid #cfd8e3; border-radius: 6px; padding: 8px 10px; font: inherit; }
        .manager-form textarea { min-height: 140px; resize: vertical; }
        .manager-form .span-2 { grid-column: span 2; }
        .manager-form .span-4 { grid-column: 1 / -1; }
        .refund-rule { grid-column: 1 / -1; display: grid; grid-template-columns: repeat(3, minmax(180px, 1fr)); gap: 12px; padding: 14px; border: 1px solid #d9e0ea; border-radius: 8px; background: #f8fafc; }
        .refund-rule__title { grid-column: 1 / -1; margin: 0; font-weight: 800; color: #253246; }
        .form-actions { display: flex; justify-content: flex-end; gap: 10px; }
        .toast-success, .toast-error { margin-bottom: 14px; padding: 10px 12px; border-radius: 8px; border: 1px solid; }
        .toast-success { background: #f0fdf4; border-color: #86efac; color: #166534; }
        .toast-error { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
        @media (max-width: 1100px) {
            .manager-form { grid-template-columns: 1fr 1fr; }
            .manager-form .span-4, .manager-form .span-2, .refund-rule { grid-column: 1 / -1; }
            .refund-rule { grid-template-columns: 1fr; }
        }
        @media (max-width: 700px) {
            .section-head { align-items: stretch; flex-direction: column; }
            .manager-form { grid-template-columns: 1fr; }
            .refund-rule { grid-template-columns: 1fr; }
            .form-actions { flex-direction: column-reverse; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="manager-content">
    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>${isEdit ? 'Sửa chính sách' : 'Tạo chính sách'}</h1>
            <p>${isEdit ? 'Cập nhật nội dung chính sách đang áp dụng.' : 'Nhập chính sách mới do manager quản lý.'}</p>
        </div>
        <a class="btn btn-secondary" href="${cp}/manager/policies">Quay lại danh sách</a>
    </section>

    <section class="manager-section">
        <c:if test="${not empty toastMessage}">
            <div class="${toastType}"><c:out value="${toastMessage}" /></div>
        </c:if>

        <div class="section-title">
            <h2>Thông tin chính sách</h2>
            <p>Có thể tạo các chính sách như hủy phòng, hoàn tiền, check-in, check-out hoặc đặt cọc.</p>
        </div>

        <form class="manager-form" method="post" action="${cp}/manager/policies/save">
            <c:if test="${isEdit}">
                <input type="hidden" name="id" value="${policy.id}">
            </c:if>
            <label class="span-2">Tiêu đề<input name="title" maxlength="150" placeholder="VD: Chính sách hủy phòng và hoàn tiền" value="${empty policy.title ? 'Chính sách hủy phòng và hoàn tiền' : policy.title}" required></label>
            <label>Nhóm chính sách<input name="category" maxlength="80" placeholder="VD: Hủy phòng" value="${empty policy.category ? 'Hủy phòng' : policy.category}" required></label>
            <label>Trạng thái
                <select name="status">
                    <option value="ACTIVE" ${empty policy.status or policy.status eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                    <option value="INACTIVE" ${policy.status eq 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option>
                </select>
            </label>
            <div class="refund-rule">
                <p class="refund-rule__title">Mốc hoàn tiền khi khách hủy phòng</p>
                <label>Hoàn cao nhất từ trước check-in bao nhiêu ngày
                    <input name="fullRefundDays" type="number" min="1" max="365" value="${empty rule ? 3 : rule.fullRefundDays}" data-refund-rule required>
                </label>
                <label>Tỷ lệ hoàn cao nhất (%)
                    <input name="fullRefundRate" type="number" min="0" max="100" value="${empty rule ? 100 : rule.fullRefundRate}" data-refund-rule required>
                </label>
                <label>Hoàn một phần từ trước check-in bao nhiêu ngày
                    <input name="partialRefundDays" type="number" min="1" max="365" value="${empty rule ? 1 : rule.partialRefundDays}" data-refund-rule required>
                </label>
                <label>Tỷ lệ hoàn một phần (%)
                    <input name="partialRefundRate" type="number" min="0" max="100" value="${empty rule ? 50 : rule.partialRefundRate}" data-refund-rule required>
                </label>
                <label>Tỷ lệ hoàn trong ngày check-in (%)
                    <input name="sameDayRefundRate" type="number" min="0" max="100" value="${empty rule ? 0 : rule.sameDayRefundRate}" data-refund-rule required>
                </label>
            </div>
            <label class="span-4">Nội dung hiển thị<textarea name="content" maxlength="2000" readonly data-policy-preview>${policy.displayContent}</textarea></label>
            <div class="form-actions span-4">
                <a class="btn btn-secondary" href="${cp}/manager/policies">Hủy</a>
                <button class="btn" type="submit">${isEdit ? 'Lưu thay đổi' : 'Tạo chính sách'}</button>
            </div>
        </form>
    </section>
</main>
<script>
    (() => {
        const fields = document.querySelectorAll('[data-refund-rule]');
        const preview = document.querySelector('[data-policy-preview]');
        if (!fields.length || !preview) return;
        const value = (name) => document.querySelector(`[name="${name}"]`)?.value || '0';
        const syncPreview = () => {
            preview.value = 'Khách hủy phòng trước ngày check-in từ ' + value('fullRefundDays')
                + ' ngày trở lên sẽ được hoàn ' + value('fullRefundRate') + '% số tiền đã thanh toán.\n'
                + 'Khách hủy phòng trước ngày check-in từ ' + value('partialRefundDays')
                + ' ngày trở lên sẽ được hoàn ' + value('partialRefundRate') + '% số tiền đã thanh toán.\n'
                + 'Khách hủy phòng trong ngày check-in hoặc sau thời điểm check-in sẽ được hoàn '
                + value('sameDayRefundRate') + '% số tiền đã thanh toán.';
        };
        fields.forEach((field) => field.addEventListener('input', syncPreview));
        if (!preview.value.trim()) syncPreview();
    })();
</script>
</body>
</html>
