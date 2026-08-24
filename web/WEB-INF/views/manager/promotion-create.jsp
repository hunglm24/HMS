<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="toastMessage" value="${sessionScope.toastMessage}" />
<c:set var="toastType" value="${sessionScope.toastType}" />
<c:remove var="toastMessage" scope="session" />
<c:remove var="toastType" scope="session" />
<c:set var="isEdit" value="${not empty promotion}" />
<fmt:formatDate value="${promotion.startDate}" pattern="yyyy-MM-dd'T'HH:mm" var="promoStart" />
<fmt:formatDate value="${promotion.endDate}" pattern="yyyy-MM-dd'T'HH:mm" var="promoEnd" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${isEdit ? 'Sửa mã giảm giá' : 'Tạo mã giảm giá'} | HMS</title>
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
        .manager-form input, .manager-form select { width: 100%; min-height: 40px; border: 1px solid #cfd8e3; border-radius: 6px; padding: 8px 10px; font: inherit; }
        .manager-form .span-2 { grid-column: span 2; }
        .manager-form .span-4 { grid-column: 1 / -1; }
        .form-actions { display: flex; justify-content: flex-end; gap: 10px; }
        .toast-success, .toast-error { margin-bottom: 14px; padding: 10px 12px; border-radius: 8px; border: 1px solid; }
        .toast-success { background: #f0fdf4; border-color: #86efac; color: #166534; }
        .toast-error { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
        @media (max-width: 1100px) {
            .manager-form { grid-template-columns: 1fr 1fr; }
            .manager-form .span-4, .manager-form .span-2 { grid-column: 1 / -1; }
        }
        @media (max-width: 700px) {
            .section-head { align-items: stretch; flex-direction: column; }
            .manager-form { grid-template-columns: 1fr; }
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
            <h1>${isEdit ? 'Sửa mã giảm giá' : 'Tạo mã giảm giá'}</h1>
            <p>${isEdit ? 'Cập nhật thông tin mã giảm giá đang áp dụng.' : 'Nhập thông tin mã giảm giá mới cho khách sử dụng khi đặt phòng.'}</p>
        </div>
        <a class="btn btn-secondary" href="${cp}/manager/pricing">Quay lại danh sách</a>
    </section>

    <section class="manager-section">
        <c:if test="${not empty toastMessage}">
            <div class="${toastType}"><c:out value="${toastMessage}" /></div>
        </c:if>

        <div class="section-title">
            <h2>Thông tin mã giảm giá</h2>
            <p>Mức giảm theo phần trăm không vượt quá 100%. Mức giảm theo số tiền không được âm.</p>
        </div>

        <form class="manager-form" method="post" action="${cp}/manager/pricing/promotion/save">
            <c:if test="${isEdit}">
                <input type="hidden" name="id" value="${promotion.id}">
            </c:if>
            <label>Mã giảm giá<input name="code" maxlength="50" placeholder="VD: SUMMER20" value="${promotion.code}" required></label>
            <label>Tên mã<input name="name" maxlength="150" placeholder="Giảm giá mùa hè" value="${promotion.name}" required></label>
            <label>Loại giảm
                <select name="discountType" data-discount-type>
                    <option value="PERCENT" ${empty promotion.discountType or promotion.discountType eq 'PERCENT' ? 'selected' : ''}>Phần trăm (%)</option>
                    <option value="FIXED_AMOUNT" ${promotion.discountType eq 'FIXED_AMOUNT' ? 'selected' : ''}>Số tiền cố định</option>
                </select>
            </label>
            <label>Mức giảm<input name="discountValue" type="number" min="0" max="100" step="1" placeholder="20 hoặc 200000" value="${promotion.discountValue}" data-discount-value required></label>
            <label>Đơn tối thiểu<input name="minBookingAmount" type="number" min="0" step="1" placeholder="VD: 1000000" value="${promotion.minBookingAmount}"></label>
            <label>Bắt đầu<input type="datetime-local" name="startDate" value="${promoStart}" required></label>
            <label>Kết thúc<input type="datetime-local" name="endDate" value="${promoEnd}" required></label>
            <label>Số lượt dùng<input type="number" min="0" name="usageLimit" placeholder="Bỏ trống nếu không giới hạn" value="${promotion.usageLimit}"></label>
            <label>Trạng thái
                <select name="status">
                    <option value="ACTIVE" ${empty promotion.status or promotion.status eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                    <option value="INACTIVE" ${promotion.status eq 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option>
                </select>
            </label>
            <label class="span-2">Mô tả<input name="description" maxlength="500" placeholder="Điều kiện áp dụng ngắn gọn" value="${promotion.description}"></label>
            <div class="form-actions span-4">
                <a class="btn btn-secondary" href="${cp}/manager/pricing">Hủy</a>
                <button class="btn" type="submit">${isEdit ? 'Lưu thay đổi' : 'Tạo mã giảm giá'}</button>
            </div>
        </form>
    </section>
</main>
<script>
    const type = document.querySelector('[data-discount-type]');
    const value = document.querySelector('[data-discount-value]');
    const syncDiscountLimit = () => {
        if (type.value === 'PERCENT') {
            value.max = '100';
        } else {
            value.removeAttribute('max');
        }
    };
    type.addEventListener('change', syncDiscountLimit);
    syncDiscountLimit();
</script>
</body>
</html>
