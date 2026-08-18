<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý giá và mã giảm giá | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260819-2">
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
            <h1>Giá và mã giảm giá</h1>
            <p>Thiết lập giai đoạn giá theo mùa/ngày lễ và tạo mã giảm giá cho khách nhập khi đặt phòng.</p>
        </div>
    </section>

    <section class="manager-section">
        <div class="section-title">
            <h2>Mã giảm giá</h2>
            <p>Manager tạo mã. Khách biết mã có thể nhập ở giỏ phòng để được giảm theo mức đã cấu hình.</p>
        </div>
        <form class="manager-form" method="post" action="${cp}/manager/pricing/promotion/save">
            <label>Mã giảm giá<input name="code" maxlength="50" placeholder="VD: SUMMER20" required></label>
            <label>Tên mã<input name="name" maxlength="150" placeholder="Giảm giá mùa hè" required></label>
            <label>Loại giảm
                <select name="discountType">
                    <option value="PERCENT">Phần trăm (%)</option>
                    <option value="FIXED_AMOUNT">Số tiền cố định</option>
                </select>
            </label>
            <label>Mức giảm<input name="discountValue" inputmode="numeric" placeholder="20 hoặc 200000" required></label>
            <label>Đơn tối thiểu<input name="minBookingAmount" inputmode="numeric" placeholder="VD: 1000000"></label>
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
                            <select name="discountType"><option value="PERCENT" ${promo.discountType eq 'PERCENT' ? 'selected' : ''}>%</option><option value="FIXED_AMOUNT" ${promo.discountType eq 'FIXED_AMOUNT' ? 'selected' : ''}>VND</option></select>
                            <input name="discountValue" value="${promo.discountValue}" required>
                            <input name="minBookingAmount" value="${promo.minBookingAmount}" placeholder="Tối thiểu">
                            <input type="datetime-local" name="startDate" value="${promoStart}" required>
                            <input type="datetime-local" name="endDate" value="${promoEnd}" required>
                            <input type="number" min="0" name="usageLimit" value="${promo.usageLimit}" placeholder="Lượt dùng">
                            <select name="status"><option value="ACTIVE" ${promo.status eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option><option value="INACTIVE" ${promo.status eq 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option></select>
                            <input name="description" value="${promo.description}" placeholder="Mô tả">
                            <button class="btn btn-secondary" type="submit">Lưu</button>
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

    <section class="manager-section">
        <div class="section-title">
            <h2>Bảng giá theo mùa/ngày lễ</h2>
            <p>Loại phòng được lấy trực tiếp từ database. Dùng phần này để khai báo giai đoạn mùa/ngày lễ cho từng loại phòng.</p>
        </div>
        <form class="manager-form" method="post" action="${cp}/manager/pricing/rule/save">
            <label>Loại phòng
                <select name="roomTypeId" required>
                    <c:if test="${empty roomTypes}">
                        <option value="">Không có loại phòng active trong DB</option>
                    </c:if>
                    <c:forEach var="roomType" items="${roomTypes}">
                        <option value="${roomType.id}"><c:out value="${roomType.name}" /></option>
                    </c:forEach>
                </select>
            </label>
            <label>Tên bảng giá<input name="ruleName" placeholder="Tết, hè, lễ 2/9..." required></label>
            <label>Loại
                <select name="ruleType"><option value="SEASON">Mùa</option><option value="HOLIDAY">Ngày lễ</option></select>
            </label>
            <label>Trạng thái
                <select name="status"><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Tạm dừng</option></select>
            </label>
            <label>Bắt đầu<input type="date" name="startDate" required></label>
            <label>Kết thúc<input type="date" name="endDate" required></label>
            <button class="btn span-4" type="submit">Thêm bảng giá</button>
        </form>

        <table class="manager-table">
            <thead><tr><th>Loại phòng</th><th>Bảng giá</th><th>Thời gian</th><th>Trạng thái</th><th>Cập nhật</th></tr></thead>
            <tbody>
            <c:forEach var="rule" items="${priceRules}">
                <tr>
                    <td><c:out value="${rule.roomTypeName}" /></td>
                    <td><strong><c:out value="${rule.ruleName}" /></strong><br><span class="hint"><c:out value="${rule.ruleType}" /></span></td>
                    <td><c:out value="${rule.startDate}" /> đến <c:out value="${rule.endDate}" /></td>
                    <td><span class="${rule.status eq 'ACTIVE' ? 'status-active' : 'status-inactive'}"><c:out value="${rule.status}" /></span></td>
                    <td>
                        <form class="inline-form" method="post" action="${cp}/manager/pricing/rule/save">
                            <input type="hidden" name="id" value="${rule.id}">
                            <select name="roomTypeId">
                                <c:if test="${empty roomTypes}">
                                    <option value="">Không có loại phòng active trong DB</option>
                                </c:if>
                                <c:forEach var="roomType" items="${roomTypes}">
                                    <option value="${roomType.id}" ${roomType.id eq rule.roomTypeId ? 'selected' : ''}><c:out value="${roomType.name}" /></option>
                                </c:forEach>
                            </select>
                            <input name="ruleName" value="${rule.ruleName}" required>
                            <select name="ruleType"><option value="SEASON" ${rule.ruleType eq 'SEASON' ? 'selected' : ''}>Mùa</option><option value="HOLIDAY" ${rule.ruleType eq 'HOLIDAY' ? 'selected' : ''}>Ngày lễ</option></select>
                            <input type="date" name="startDate" value="${rule.startDate}" required>
                            <input type="date" name="endDate" value="${rule.endDate}" required>
                            <select name="status"><option value="ACTIVE" ${rule.status eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option><option value="INACTIVE" ${rule.status eq 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option></select>
                            <button class="btn btn-secondary" type="submit">Lưu</button>
                        </form>
                        <form method="post" action="${cp}/manager/pricing/rule/delete" style="margin-top:8px">
                            <input type="hidden" name="id" value="${rule.id}">
                            <button class="btn btn-secondary" type="submit" onclick="return confirm('Xóa bảng giá này?')">Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty priceRules}"><tr><td colspan="5">Chưa có bảng giá mùa/ngày lễ.</td></tr></c:if>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>
