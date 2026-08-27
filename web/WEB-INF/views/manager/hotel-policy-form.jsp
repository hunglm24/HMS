<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${pageTitle}" default="Nội quy khách sạn | HMS" /></title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260821-1">
    <link rel="stylesheet" href="${cp}/assets/css/hotel-policy-form.css?v=20260827-1">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <script defer src="${cp}/assets/js/hotel-policy-form.js?v=20260827-1"></script>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="policy-management-page">
    <section class="policy-form-panel">
        <div class="policy-form__head">
            <div>
                <h1><c:out value="${pageHeading}" default="Thông tin policy" /></h1>
                <p><c:out value="${pageSubtitle}" default="Tạo hoặc cập nhật nội quy chung khách sạn." /></p>
            </div>
            <a class="btn btn-secondary" href="${cp}${backUrl}">Quay lại</a>
        </div>

        <c:if test="${not empty sessionScope.toastMessage}">
            <div class="${sessionScope.toastType}"><c:out value="${sessionScope.toastMessage}" /></div>
        </c:if>

        <form class="policy-form" method="post" action="${cp}/manager/hotel-policy/save">
            <c:if test="${isEditMode and not empty policy.id}">
                <input type="hidden" name="id" value="${policy.id}">
            </c:if>

            <div class="policy-form__grid">
                <label>
                    Tiêu đề
                    <input type="text"
                           name="title"
                           minlength="2"
                           maxlength="150"
                           value="${policy.title}"
                           placeholder="Nội quy chung khách sạn"
                           required>
                </label>
                <div class="policy-form-field policy-form-field--content">
                    <div class="policy-form-field__head">
                        <label for="policyContent">Nội dung</label>
                        <div class="policy-form-quick-actions">
                            <button type="button"
                                    class="btn btn-secondary btn-sm policy-number-toggle"
                                    data-policy-action="toggle-numbering"
                                    aria-label="Đánh số dòng"
                                    title="Đánh số dòng">
                                <i class="bi bi-list-ol" aria-hidden="true" data-policy-icon></i>
                            </button>
                        </div>
                    </div>
                    <textarea id="policyContent"
                              name="content"
                              minlength="10"
                              maxlength="5000"
                              placeholder="Nhập nội quy khách sạn..."
                              required>${policy.content}</textarea>
                </div>
            </div>

            <p class="policy-form__note">
                Hãy viết ngắn gọn, rõ ràng và phù hợp để khách đọc trên web/mobile. Nếu cần đổi quy trình check-in, check-out hoặc hoàn tiền, việc đó thuộc hotel config.
            </p>

            <div class="policy-form__actions">
                <a class="btn btn-secondary" href="${cp}${backUrl}">Hủy</a>
                <button class="btn" type="submit">
                    <c:out value="${submitLabel}" default="Sửa nội quy" />
                </button>
            </div>
        </form>
    </section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
