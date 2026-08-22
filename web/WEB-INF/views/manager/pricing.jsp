<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="toastMessage" value="${sessionScope.toastMessage}" />
<c:set var="toastType" value="${sessionScope.toastType}" />
<c:remove var="toastMessage" scope="session" />
<c:remove var="toastType" scope="session" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tạo mã giảm giá | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260821-1">
    <style>
        .manager-content { min-width: 0; background: #f6f8fb; }
        .manager-section { background: #fff; border: 1px solid #d9e0ea; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
        .section-title { margin-bottom: 16px; }
        .section-title h2 { margin: 0 0 6px; font-size: 1.25rem; }
        .section-title p { margin: 0; color: #526174; }
        .manager-form { display: grid; grid-template-columns: repeat(4, minmax(160px, 1fr)); gap: 12px; align-items: end; }
        .manager-form label { display: grid; gap: 6px; font-size: .9rem; font-weight: 700; color: #253246; }
        .manager-form input, .manager-form select { width: 100%; min-height: 40px; border: 1px solid #cfd8e3; border-radius: 6px; padding: 8px 10px; font: inherit; }
        .manager-form .span-2 { grid-column: span 2; }
        .manager-form .span-4 { grid-column: 1 / -1; }
        .manager-table { width: 100%; border-collapse: collapse; margin-top: 16px; background: #fff; }
        .manager-table th, .manager-table td { border-top: 1px solid #e2e8f0; padding: 10px; text-align: left; vertical-align: top; }
        .manager-table th { color: #526174; font-size: .82rem; text-transform: uppercase; }
        .inline-form { display: grid; grid-template-columns: repeat(4, minmax(120px, 1fr)); gap: 8px; align-items: end; }
        .inline-form input, .inline-form select { min-height: 36px; border: 1px solid #cfd8e3; border-radius: 6px; padding: 7px 8px; font: inherit; }
        .hint { color: #667085; font-size: .85rem; }
        .status-active { color: #067647; font-weight: 800; }
        .status-inactive { color: #b42318; font-weight: 800; }
        .toast-success, .toast-error { margin-bottom: 14px; padding: 10px 12px; border-radius: 8px; border: 1px solid; }
        .toast-success { background: #f0fdf4; border-color: #86efac; color: #166534; }
        .toast-error { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
        @media (max-width: 1100px) {
            .manager-form, .inline-form { grid-template-columns: 1fr 1fr; }
            .manager-form .span-4, .manager-form .span-2 { grid-column: 1 / -1; }
        }
        @media (max-width: 700px) {
            .manager-form, .inline-form { grid-template-columns: 1fr; }
            .manager-table { display: block; overflow-x: auto; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="manager-content">
    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>Tạo mã giảm giá</h1>
            <p>Quản lý mã giảm giá cho khách nhập khi đặt phòng.</p>
        </div>
    </section>

    <section class="manager-section">
        <c:if test="${not empty toastMessage}">
            <div class="${toastType}"><c:out value="${toastMessage}" /></div>
        </c:if>
        <div class="section-title">
            <h2>Mã giảm giá</h2>
            <p>Mức giảm theo phần trăm không vượt quá 100%. Mức giảm theo số tiền không được âm.</p>
        </div>
        <form class="manager-form" method="post" action="${cp}/manager/pricing/promotion/save">
            <label>Mã giảm giá<input name="code" maxlength="50" placeholder="VD: SUMMER20" required></label>
            <label>Tên mã<input name="name" maxlength="150" placeholder="Giảm giá mùa hè" required></label>
            <label>Loại giảm
                <select name="discountType" data-discount-type>
                    <option value="PERCENT">Phần trăm (%)</option>
                    <option value="FIXED_AMOUNT">Số tiền cố định</option>
                </select>
            </label>
            <label>Mức giảm<input name="discountValue" type="number" min="0" max="100" step="1" placeholder="20 hoặc 200000" data-discount-value required></label>
            <label>Đơn tối thiểu<input name="minBookingAmount" type="number" min="0" step="1" placeholder="VD: 1000000"></label>
            <label>Bắt đầu<input type="datetime-local" name="startDate" required></label>
            <label>Kết thúc<input type="datetime-local" name="endDate" required></label>
            <label>Số lượt dùng<input type="number" min="0" name="usageLimit" placeholder="Bỏ trống nếu không giới hạn"></label>
            <label>Trạng thái
                <select name="status"><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Tạm dừng</option></select>
            </label>
            <label class="span-2">Mô tả<input name="description" maxlength="500" placeholder="Điều kiện áp dụng ngắn gọn"></label>
            <button class="btn span-4" type="submit">Tạo mã giảm giá</button>
        </form>

        <table class="manager-table">
            <thead><tr><th>Mã</th><th>Thông tin</th><th>Hiệu lực</th><th>Đã dùng</th><th>Cập nhật</th></tr></thead>
            <tbody>
            <c:forEach var="promo" items="${promotions}">
                <fmt:formatDate value="${promo.startDate}" pattern="yyyy-MM-dd'T'HH:mm" var="promoStart" />
                <fmt:formatDate value="${promo.endDate}" pattern="yyyy-MM-dd'T'HH:mm" var="promoEnd" />
                <tr>
                    <td><strong><c:out value="${promo.code}" /></strong><br><span class="${promo.status eq 'ACTIVE' ? 'status-active' : 'status-inactive'}"><c:out value="${promo.status}" /></span></td>
                    <td>
                        <c:out value="${promo.name}" /><br>
                        <span class="hint"><c:out value="${promo.discountType}" />: <fmt:formatNumber value="${promo.discountValue}" maxFractionDigits="0" /></span>
                    </td>
                    <td><c:out value="${promo.startDate}" /><br><c:out value="${promo.endDate}" /></td>
                    <td><c:out value="${promo.usedCount}" /> / <c:out value="${empty promo.usageLimit ? 'Không giới hạn' : promo.usageLimit}" /></td>
                    <td>
                        <form class="inline-form" method="post" action="${cp}/manager/pricing/promotion/save">
                            <input type="hidden" name="id" value="${promo.id}">
                            <input name="code" value="${promo.code}" required>
                            <input name="name" value="${promo.name}" required>
                            <select name="discountType" data-discount-type><option value="PERCENT" ${promo.discountType eq 'PERCENT' ? 'selected' : ''}>%</option><option value="FIXED_AMOUNT" ${promo.discountType eq 'FIXED_AMOUNT' ? 'selected' : ''}>VND</option></select>
                            <input name="discountValue" type="number" min="0" step="1" value="${promo.discountValue}" data-discount-value required>
                            <input name="minBookingAmount" type="number" min="0" step="1" value="${promo.minBookingAmount}" placeholder="Tối thiểu">
                            <input type="datetime-local" name="startDate" value="${promoStart}" required>
                            <input type="datetime-local" name="endDate" value="${promoEnd}" required>
                            <input type="number" min="0" name="usageLimit" value="${promo.usageLimit}" placeholder="Lượt dùng">
                            <select name="status"><option value="ACTIVE" ${promo.status eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option><option value="INACTIVE" ${promo.status eq 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option></select>
                            <input name="description" value="${promo.description}" placeholder="Mô tả">
                        </form>
                        <form method="post" action="${cp}/manager/pricing/promotion/delete" style="margin-top:8px">
                            <input type="hidden" name="id" value="${promo.id}">
                            <button class="btn btn-secondary" type="submit" onclick="return confirm('Xóa mã giảm giá này?')">Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty promotions}"><tr><td colspan="5">Chưa có mã giảm giá.</td></tr></c:if>
            </tbody>
        </table>
    </section>
</main>
<script>
    document.querySelectorAll('form').forEach((form) => {
        const type = form.querySelector('[data-discount-type]');
        const value = form.querySelector('[data-discount-value]');
        if (!type || !value) return;
        const sync = () => {
            if (type.value === 'PERCENT') {
                value.max = '100';
            } else {
                value.removeAttribute('max');
            }
        };
        type.addEventListener('change', sync);
        sync();
    });
</script>
</body>
</html>
