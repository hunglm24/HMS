<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<c:set var="toastMessage" value="${sessionScope.toastMessage}" />
<c:set var="toastType" value="${sessionScope.toastType}" />
<c:remove var="toastMessage" scope="session" />
<c:remove var="toastType" scope="session" />
<c:set var="isEdit" value="${not empty policy and not empty policy.id}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${isEdit ? 'Cập nhật policy' : 'Thiết lập policy'} | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260821-1">
    <style>
        .manager-content { min-width: 0; background: #f6f8fb; }
        .section-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; margin-bottom: 20px; }
        .section-head h1 { margin-bottom: 6px; }
        .section-head p { margin: 0; }
        .manager-section { background: #fff; border: 1px solid #d9e0ea; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
        .form-layout { display: grid; grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.7fr); gap: 20px; align-items: start; }
        .policy-form { display: grid; gap: 16px; }
        .field-group { display: grid; gap: 8px; }
        .field-group label { font-size: .9rem; font-weight: 700; color: #253246; }
        .field-group input, .field-group textarea, .field-group select {
            width: 100%; min-height: 42px; border: 1px solid #cfd8e3; border-radius: 6px; padding: 10px 12px; font: inherit;
        }
        .field-group textarea { min-height: 260px; resize: vertical; }
        .helper-card, .preview-card { background: #fbfcfe; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; }
        .preview-content { white-space: pre-line; line-height: 1.7; color: #344054; }
        .form-actions { display: flex; justify-content: flex-end; gap: 10px; flex-wrap: wrap; }
        .toast-success, .toast-error { margin-bottom: 14px; padding: 10px 12px; border-radius: 8px; border: 1px solid; }
        .toast-success { background: #f0fdf4; border-color: #86efac; color: #166534; }
        .toast-error { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
        @media (max-width: 900px) {
            .form-layout { grid-template-columns: 1fr; }
            .section-head { align-items: stretch; flex-direction: column; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="manager-content">
    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>${isEdit ? 'Cập nhật policy khách sạn' : 'Thiết lập policy khách sạn'}</h1>
            <p>Chỉ chỉnh sửa một policy duy nhất cho toàn khách sạn.</p>
        </div>
        <a class="btn btn-secondary" href="${cp}/manager/hotel-policy">Quay lại</a>
    </section>

    <c:if test="${not empty toastMessage}">
        <div class="${toastType}"><c:out value="${toastMessage}" /></div>
    </c:if>

    <section class="manager-section">
        <div class="form-layout">
            <form class="policy-form" method="post" action="${cp}/manager/hotel-policy/save">
                <c:if test="${not empty policy and not empty policy.id}">
                    <input type="hidden" name="id" value="${policy.id}">
                </c:if>

                <div class="field-group">
                    <label for="title">Tiêu đề</label>
                    <input id="title" name="title" maxlength="150" value="${not empty policy.title ? policy.title : 'Nội quy chung khách sạn'}" required>
                </div>

                <div class="field-group">
                    <label for="status">Trạng thái</label>
                    <select id="status" name="status" required>
                        <option value="ACTIVE" ${empty policy.status or policy.status eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                        <option value="INACTIVE" ${policy.status eq 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option>
                    </select>
                </div>

                <div class="field-group">
                    <label for="content">Nội dung</label>
                    <textarea id="content" name="content" maxlength="5000" required>${not empty policy.content ? policy.content : 'Giữ yên tĩnh sau 22:00.\nKhông hút thuốc tại khu vực cấm.\nKhông tự ý di chuyển tài sản khách sạn.\nGiữ vệ sinh khu vực chung và tôn trọng người khác.'}</textarea>
                </div>

                <div class="form-actions">
                    <a class="btn btn-secondary" href="${cp}/manager/hotel-policy">Hủy</a>
                    <button class="btn" type="submit">${isEdit ? 'Lưu thay đổi' : 'Lưu policy'}</button>
                </div>
            </form>

            <div class="preview-column">
                <div class="preview-card" style="margin-bottom: 16px;">
                    <h3 style="margin-top: 0;">Gợi ý</h3>
                    <p style="margin-bottom: 0; color: #667085;">Policy chỉ dùng cho nội quy chung. Các rule vận hành như check-in / check-out / refund không đặt ở đây.</p>
                </div>
                <div class="preview-card">
                    <h3 style="margin-top: 0;">Preview</h3>
                    <div class="preview-content">${not empty policy.content ? policy.content : 'Nội dung policy sẽ hiển thị ở đây sau khi nhập.'}</div>
                </div>
            </div>
        </div>
    </section>
</main>
</body>
</html>
