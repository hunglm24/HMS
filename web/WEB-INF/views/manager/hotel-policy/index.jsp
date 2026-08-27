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
    <title>Hotel Policy | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260821-1">
    <style>
        .manager-content { min-width: 0; background: #f6f8fb; }
        .page-shell { display: grid; grid-template-columns: minmax(0, 1fr) 0; gap: 20px; align-items: start; position: relative; }
        .section-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; margin-bottom: 20px; }
        .section-head h1 { margin-bottom: 6px; }
        .section-head p { margin: 0; }
        .manager-card { background: #fff; border: 1px solid #d9e0ea; border-radius: 8px; padding: 20px; }
        .policy-summary { display: grid; gap: 14px; }
        .policy-title-row { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; flex-wrap: wrap; }
        .policy-meta { color: #667085; font-size: .9rem; display: grid; gap: 4px; }
        .policy-content { white-space: pre-line; color: #344054; line-height: 1.7; }
        .policy-actions { display: flex; flex-wrap: wrap; gap: 10px; }
        .badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 999px; font-size: .8rem; font-weight: 700; border: 1px solid; }
        .badge-active { background: #f0fdf4; color: #166534; border-color: #86efac; }
        .badge-inactive { background: #fef2f2; color: #991b1b; border-color: #fecaca; }
        .policy-drawer-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.18); opacity: 0; pointer-events: none; transition: opacity .2s ease; z-index: 40; }
        .policy-drawer-overlay.is-open { opacity: 1; pointer-events: auto; }
        .policy-drawer { position: fixed; top: 0; right: 0; width: min(640px, 92vw); height: 100vh; background: #fff; border-left: 1px solid #d9e0ea; box-shadow: -20px 0 40px rgba(15, 23, 42, 0.12); transform: translateX(100%); transition: transform .25s ease; z-index: 50; display: flex; flex-direction: column; }
        .policy-drawer.is-open { transform: translateX(0); }
        .policy-drawer__head { padding: 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
        .policy-drawer__body { padding: 20px; overflow: auto; display: grid; gap: 20px; }
        .policy-drawer__section { border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; }
        .policy-drawer__section h3 { margin: 0 0 12px; font-size: 1.05rem; }
        .drawer-actions { display: flex; gap: 10px; flex-wrap: wrap; }
        .empty-state { padding: 28px 12px; color: #667085; text-align: center; border: 1px dashed #d9e0ea; border-radius: 8px; background: #fbfcfe; }
        .toast-success, .toast-error { margin-bottom: 14px; padding: 10px 12px; border-radius: 8px; border: 1px solid; }
        .toast-success { background: #f0fdf4; border-color: #86efac; color: #166534; }
        .toast-error { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
        @media (max-width: 900px) {
            .page-shell { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="manager-content">
    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>Hotel Policy</h1>
            <p>Chỉ quản lý một policy duy nhất cho khách sạn.</p>
        </div>
        <div class="policy-actions">
            <a class="btn btn-secondary" href="${cp}/manager/hotel-policy/create">Thiết lập / cập nhật</a>
        </div>
    </section>

    <c:if test="${not empty toastMessage}">
        <div class="${toastType}"><c:out value="${toastMessage}" /></div>
    </c:if>

    <div class="page-shell">
        <section class="manager-card policy-summary">
            <c:choose>
                <c:when test="${not empty policy}">
                    <div class="policy-title-row">
                        <div>
                            <div class="policy-meta">POLICY HIỆN TẠI</div>
                            <h2 style="margin: 6px 0 10px;"><c:out value="${policy.title}" /></h2>
                            <div class="policy-meta">
                                <span>
                                    Cập nhật gần nhất:
                                    <c:choose>
                                        <c:when test="${not empty policy.updatedAt}">
                                            <fmt:formatDate value="${policy.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                                        </c:when>
                                        <c:otherwise>N/A</c:otherwise>
                                    </c:choose>
                                </span>
                                <span>
                                    Khởi tạo:
                                    <c:choose>
                                        <c:when test="${not empty policy.createdAt}">
                                            <fmt:formatDate value="${policy.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                        </c:when>
                                        <c:otherwise>N/A</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>
                        <span class="badge ${policy.status eq 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">
                            <c:out value="${policy.status eq 'ACTIVE' ? 'ACTIVE' : 'INACTIVE'}" />
                        </span>
                    </div>

                    <div class="policy-content"><c:out value="${policy.content}" /></div>

                    <div class="policy-actions">
                        <button class="btn btn-secondary" type="button" data-open-drawer>Xem chi tiết</button>
                        <a class="btn btn-secondary" href="${cp}/manager/hotel-policy/edit?id=${policy.id}">Sửa</a>
                        <form method="post" action="${cp}/manager/hotel-policy/toggle-status" style="display:inline;">
                            <input type="hidden" name="id" value="${policy.id}">
                            <input type="hidden" name="status" value="${policy.status eq 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
                            <button class="btn btn-secondary" type="submit">
                                <c:choose>
                                    <c:when test="${policy.status eq 'ACTIVE'}">Tạm dừng</c:when>
                                    <c:otherwise>Kích hoạt</c:otherwise>
                                </c:choose>
                            </button>
                        </form>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        Chưa có policy khách sạn nào. Hãy thiết lập policy đầu tiên.
                        <div style="margin-top: 12px;">
                            <a class="btn" href="${cp}/manager/hotel-policy/create">Thiết lập policy</a>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>

<div class="policy-drawer-overlay" data-drawer-overlay></div>
<aside class="policy-drawer" data-drawer>
    <div class="policy-drawer__head">
        <div>
            <div class="policy-meta">DRAWER CHI TIẾT</div>
            <h2 style="margin: 6px 0 0;"><c:out value="${empty policy ? 'Nội quy chung khách sạn' : policy.title}" /></h2>
        </div>
        <button class="btn btn-secondary" type="button" data-close-drawer>Đóng</button>
    </div>
    <div class="policy-drawer__body">
        <div class="policy-drawer__section">
            <h3>Nội dung</h3>
            <div class="policy-content"><c:out value="${empty policy ? 'Chưa có policy để hiển thị.' : policy.content}" /></div>
        </div>
        <div class="policy-drawer__section">
            <h3>Thông tin</h3>
            <div class="policy-meta">
                <span>Trạng thái: <c:out value="${empty policy ? 'N/A' : policy.status}" /></span>
                <span>Phiên bản: <c:out value="${empty policy ? 'N/A' : policy.id}" /></span>
            </div>
        </div>
        <div class="policy-drawer__section">
            <h3>Hành động</h3>
            <div class="drawer-actions">
                <a class="btn btn-secondary" href="${cp}/manager/hotel-policy/create">Thiết lập / cập nhật</a>
                <c:if test="${not empty policy}">
                    <a class="btn btn-secondary" href="${cp}/manager/hotel-policy/edit?id=${policy.id}">Sửa nhanh</a>
                </c:if>
            </div>
        </div>
    </div>
</aside>

<script>
    (() => {
        const drawer = document.querySelector('[data-drawer]');
        const overlay = document.querySelector('[data-drawer-overlay]');
        const openBtn = document.querySelector('[data-open-drawer]');
        const closeBtn = document.querySelector('[data-close-drawer]');
        if (!drawer || !overlay) return;
        const openDrawer = () => {
            drawer.classList.add('is-open');
            overlay.classList.add('is-open');
        };
        const closeDrawer = () => {
            drawer.classList.remove('is-open');
            overlay.classList.remove('is-open');
        };
        if (openBtn) openBtn.addEventListener('click', openDrawer);
        if (closeBtn) closeBtn.addEventListener('click', closeDrawer);
        overlay.addEventListener('click', closeDrawer);
    })();
</script>
</body>
</html>
