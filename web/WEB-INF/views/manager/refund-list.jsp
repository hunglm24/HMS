<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Yêu cầu hoàn tiền | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260824-2">
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260824-2">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        .bill-thumb {
            width: 42px;
            height: 42px;
            object-fit: cover;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            cursor: pointer;
            transition: transform 0.15s ease, border-color 0.15s ease;
        }
        .bill-thumb:hover {
            transform: scale(1.08);
            border-color: var(--color-primary-600);
        }
        .bill-badge-btn {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            padding: 5px 10px;
            background: #eff6ff;
            color: #1e4fd8;
            border: 1px solid #bfdbfe;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.15s ease;
        }
        .bill-badge-btn:hover {
            background: #dbeafe;
        }
        .modal-backdrop {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(15, 23, 42, 0.52);
            backdrop-filter: blur(3px);
            z-index: 3000;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .modal-backdrop.active {
            display: flex;
        }
        .modal-box {
            background: #ffffff;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-lg);
            max-width: 520px;
            width: 100%;
            padding: 24px;
            box-shadow: 0 24px 60px rgba(16, 24, 40, 0.24);
        }
        .modal-box h3 {
            margin-top: 0;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 1px solid var(--color-border);
            font-size: 1.25rem;
        }
        .modal-image-view {
            max-width: 90vw;
            max-height: 80vh;
            object-fit: contain;
            border-radius: 8px;
            background: white;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
        }
        .preview-box {
            margin-top: 10px;
            text-align: center;
            display: none;
        }
        .preview-box img {
            max-height: 160px;
            max-width: 100%;
            border-radius: 6px;
            border: 1px solid #e2e8f0;
        }
        .btn-action-group {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            justify-content: flex-end;
        }
        .btn-action-group form {
            margin: 0;
            display: inline-flex;
        }
    </style>
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="page-container room-management-page">
    <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
            <p class="room-management-kicker">Manager</p>
            <h1>Yêu cầu hoàn tiền</h1>
            <p>Tiếp nhận yêu cầu từ khách hàng, kiểm tra thông tin tài khoản, tải lên ảnh bill chuyển khoản và xác nhận hoàn tiền.</p>
        </div>
    </section>

    <!-- Thống kê nhanh -->
    <c:set var="countPending" value="0" />
    <c:set var="countCompleted" value="0" />
    <c:set var="countRejected" value="0" />
    <c:forEach var="item" items="${refundRequests}">
        <c:choose>
            <c:when test="${item.status eq 'PENDING'}"><c:set var="countPending" value="${countPending + 1}" /></c:when>
            <c:when test="${item.status eq 'COMPLETED'}"><c:set var="countCompleted" value="${countCompleted + 1}" /></c:when>
            <c:when test="${item.status eq 'REJECTED'}"><c:set var="countRejected" value="${countRejected + 1}" /></c:when>
        </c:choose>
    </c:forEach>

    <section class="room-management-stats-grid">
        <article class="room-management-stat-card">
            <div class="room-management-stat-card__icon room-management-stat-card__icon--total">RF</div>
            <div class="room-management-stat-card__body">
                <span class="room-management-stat-card__label">Tổng yêu cầu</span>
                <strong class="room-management-stat-card__value">${refundRequests != null ? refundRequests.size() : 0}</strong>
                <small>Tất cả yêu cầu đã tạo</small>
            </div>
        </article>
        <article class="room-management-stat-card">
            <div class="room-management-stat-card__icon room-management-stat-card__icon--occupied">⏳</div>
            <div class="room-management-stat-card__body">
                <span class="room-management-stat-card__label">Chờ xử lý</span>
                <strong class="room-management-stat-card__value" style="color: #b45309;">${countPending}</strong>
                <small>Cần chuyển khoản &amp; gửi bill</small>
            </div>
        </article>
        <article class="room-management-stat-card">
            <div class="room-management-stat-card__icon room-management-stat-card__icon--available">✓</div>
            <div class="room-management-stat-card__body">
                <span class="room-management-stat-card__label">Đã hoàn tiền</span>
                <strong class="room-management-stat-card__value" style="color: #15803d;">${countCompleted}</strong>
                <small>Đã gửi bill thành công</small>
            </div>
        </article>
        <article class="room-management-stat-card">
            <div class="room-management-stat-card__icon room-management-stat-card__icon--maintenance">✕</div>
            <div class="room-management-stat-card__body">
                <span class="room-management-stat-card__label">Bị từ chối</span>
                <strong class="room-management-stat-card__value" style="color: #be123c;">${countRejected}</strong>
                <small>Yêu cầu không hợp lệ</small>
            </div>
        </article>
    </section>

    <!-- Bộ lọc & Bảng danh sách -->
    <section class="room-management-content">
        <section class="room-management-panel panel">
            <div class="room-management-toolbar">
                <form class="room-management-filters" method="get" action="${cp}/manager/refunds" style="grid-template-columns: minmax(220px, 1fr) auto auto; max-width: 450px;">
                    <div class="room-management-filters__select">
                        <select name="status">
                            <option value="">Trạng thái: Tất cả</option>
                            <option value="PENDING" ${param.status eq 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                            <option value="COMPLETED" ${param.status eq 'COMPLETED' ? 'selected' : ''}>Đã hoàn tiền</option>
                            <option value="REJECTED" ${param.status eq 'REJECTED' ? 'selected' : ''}>Bị từ chối</option>
                        </select>
                    </div>
                    <button class="btn btn-secondary" type="submit">Lọc</button>
                    <a class="btn btn-secondary" href="${cp}/manager/refunds">Đặt lại</a>
                </form>
            </div>

            <div class="room-management-table-wrap">
                <table class="room-management-table">
                    <thead>
                    <tr>
                        <th style="width: 130px;">Mã Booking</th>
                        <th style="width: 170px;">Khách hàng</th>
                        <th style="width: 130px;">Ngân hàng</th>
                        <th style="width: 190px;">Tài khoản nhận</th>
                        <th style="width: 140px;">Số tiền hoàn</th>
                        <th>Lý do</th>
                        <th style="width: 120px;">Trạng thái</th>
                        <th style="width: 150px;">Bill chuyển khoản</th>
                        <th style="width: 200px; text-align: right;">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty refundRequests}">
                            <tr>
                                <td colspan="9">
                                    <div class="room-management-empty">
                                        <strong>Chưa có yêu cầu hoàn tiền nào.</strong>
                                        <span>Các yêu cầu hủy booking hoàn tiền sẽ hiển thị tại đây.</span>
                                    </div>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="r" items="${refundRequests}">
                                <tr>
                                    <td>
                                        <a href="${cp}/manager/booking-detail?id=${r.bookingId}" class="room-number-pill" style="text-decoration: none;">
                                            <c:out value="${r.bookingCode}"/>
                                        </a>
                                    </td>
                                    <td>
                                        <div class="room-management-primary">
                                            <strong><c:out value="${r.guestName != null && r.guestName != '' ? r.guestName : 'Khách vãng lai'}"/></strong>
                                            <span>#<c:out value="${r.bookingId}"/></span>
                                        </div>
                                    </td>
                                    <td><strong><c:out value="${r.bankName}"/></strong></td>
                                    <td>
                                        <div class="room-management-primary">
                                            <strong><c:out value="${r.accountNumber}"/></strong>
                                            <span><c:out value="${r.accountHolder}"/></span>
                                        </div>
                                    </td>
                                    <td>
                                        <strong style="color: #15803d; font-size: 15px;">
                                            <fmt:formatNumber value="${r.refundAmount}" pattern="#,##0"/> ₫
                                        </strong>
                                    </td>
                                    <td>
                                        <div class="room-management-equipment" title="<c:out value='${r.reason}'/>">
                                            <c:out value="${r.reason}"/>
                                        </div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${r.status eq 'PENDING'}">
                                                <span class="status-chip status-occupied">Chờ xử lý</span>
                                            </c:when>
                                            <c:when test="${r.status eq 'COMPLETED'}">
                                                <span class="status-chip status-available">Đã hoàn</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-chip status-maintenance"><c:out value="${r.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty r.billImage}">
                                                <div style="display:flex; align-items:center; gap:8px;">
                                                    <img src="${cp}${r.billImage}" alt="Bill" class="bill-thumb"
                                                         onclick="viewBillModal('${cp}${r.billImage}')" title="Nhấn để phóng to">
                                                    <button type="button" class="bill-badge-btn" onclick="viewBillModal('${cp}${r.billImage}')">
                                                        <i class="bi bi-image"></i> Xem bill
                                                    </button>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color:#94a3b8; font-size:13px;">Chưa có</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <c:if test="${r.status eq 'PENDING'}">
                                            <div class="btn-action-group">
                                                <button type="button" class="btn btn-primary btn-sm"
                                                        onclick="openCompleteModal('${r.id}', '${r.bookingCode}', '${r.bankName}', '${r.accountNumber}', '${r.accountHolder}', '<fmt:formatNumber value="${r.refundAmount}" pattern="#,##0"/> ₫')">
                                                    Hoàn tiền &amp; Gửi bill
                                                </button>
                                                <form method="post" action="${cp}/manager/refunds" onsubmit="return confirm('Bạn có chắc muốn từ chối yêu cầu hoàn tiền này?');">
                                                    <input type="hidden" name="id" value="${r.id}">
                                                    <button class="btn btn-secondary btn-sm" name="action" value="REJECTED" type="submit" style="color: var(--color-error-600);">
                                                        Từ chối
                                                    </button>
                                                </form>
                                            </div>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </section>
</main>

<!-- Modal Hoàn Tiền & Tải Lên Bill -->
<div class="modal-backdrop" id="completeModal">
    <div class="modal-box">
        <h3>Xác nhận hoàn tiền &amp; Tải lên bill</h3>
        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:14px; margin-bottom:16px; font-size:14px;">
            <p style="margin:0 0 6px 0;"><strong>Mã Booking:</strong> <span id="mBookingCode"></span></p>
            <p style="margin:0 0 6px 0;"><strong>Tài khoản nhận:</strong> <span id="mBank"></span> - <span id="mAccount"></span> (<span id="mHolder"></span>)</p>
            <p style="margin:0;"><strong>Số tiền cần hoàn:</strong> <span id="mAmount" style="color:#15803d; font-weight:700;"></span></p>
        </div>
        <form method="post" action="${cp}/manager/refunds" enctype="multipart/form-data" class="room-management-form">
            <input type="hidden" name="id" id="mRefundId" value="">
            <input type="hidden" name="action" value="COMPLETED">
            
            <div>
                <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Ảnh biên lai / Bill chuyển khoản:</label>
                <input type="file" name="billImage" id="billImageInput" accept="image/png, image/jpeg, image/jpg, image/webp" class="form-control" onchange="previewBill(event)">
                <small class="room-management-form__hint" style="display:block; margin-top:4px;">Chấp nhận file ảnh JPG, PNG, WEBP tối đa 5MB.</small>
                <div class="preview-box" id="billPreviewContainer">
                    <img id="billPreviewImg" src="" alt="Bill Preview">
                </div>
            </div>

            <div class="room-management-form__actions" style="margin-top: 16px;">
                <button type="button" class="btn btn-secondary btn-sm" onclick="closeCompleteModal()">Hủy bỏ</button>
                <button type="submit" class="btn btn-primary btn-sm" style="background:#15803d !important;">Xác nhận đã hoàn tiền</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal Phóng To Bill Image -->
<div class="modal-backdrop" id="viewBillModal" onclick="closeViewBillModal()">
    <div style="position:relative; text-align:center;" onclick="event.stopPropagation();">
        <img id="fullBillImage" src="" alt="Biên lai hoàn tiền" class="modal-image-view">
        <div style="margin-top:14px;">
            <button type="button" class="btn btn-secondary btn-sm" onclick="closeViewBillModal()" style="background:white;">Đóng lại</button>
            <a id="billDownloadLink" href="" target="_blank" class="btn btn-primary btn-sm" style="margin-left:8px;">Mở ảnh gốc</a>
        </div>
    </div>
</div>

<script>
    function openCompleteModal(id, code, bank, account, holder, amount) {
        document.getElementById('mRefundId').value = id;
        document.getElementById('mBookingCode').textContent = code;
        document.getElementById('mBank').textContent = bank;
        document.getElementById('mAccount').textContent = account;
        document.getElementById('mHolder').textContent = holder;
        document.getElementById('mAmount').textContent = amount;
        document.getElementById('billImageInput').value = '';
        document.getElementById('billPreviewContainer').style.display = 'none';
        document.getElementById('completeModal').classList.add('active');
    }

    function closeCompleteModal() {
        document.getElementById('completeModal').classList.remove('active');
    }

    function previewBill(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                document.getElementById('billPreviewImg').src = e.target.result;
                document.getElementById('billPreviewContainer').style.display = 'block';
            };
            reader.readAsDataURL(file);
        } else {
            document.getElementById('billPreviewContainer').style.display = 'none';
        }
    }

    function viewBillModal(src) {
        document.getElementById('fullBillImage').src = src;
        document.getElementById('billDownloadLink').href = src;
        document.getElementById('viewBillModal').classList.add('active');
    }

    function closeViewBillModal() {
        document.getElementById('viewBillModal').classList.remove('active');
    }
</script>
</body>
</html>
