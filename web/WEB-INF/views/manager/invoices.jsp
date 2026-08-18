<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Hóa đơn - HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <style>
        .toolbar { display:flex; justify-content:space-between; gap:12px; align-items:end; flex-wrap:wrap; margin-bottom:16px; }
        .filters { display:flex; gap:10px; flex-wrap:wrap; align-items:end; }
        .filters .filter-field { width:190px; }
        .filters .search-field { width:250px; }
        .data-table { width:100%; border-collapse:collapse; background:#fff; border:1px solid var(--color-border); border-radius:8px; overflow:hidden; }
        .data-table th,.data-table td { padding:11px 12px; border-bottom:1px solid var(--color-border); text-align:left; vertical-align:top; }
        .data-table th { background:var(--color-bg-surface); color:var(--color-text-secondary); font-size:13px; }
        .badge { display:inline-flex; padding:3px 8px; border-radius:999px; font-size:12px; font-weight:700; }
        .badge-PAID { background:#dcfce7; color:#166534; }
        .badge-PARTIALLY_PAID { background:#fef08a; color:#854d0e; }
        .badge-UNPAID { background:#fee2e2; color:#991b1b; }
        .badge-REFUNDED { background:#f3f4f6; color:#374151; }
        .pagination-bar { display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap; margin-top:14px; }
        .pagination-links { display:flex; gap:6px; align-items:center; flex-wrap:wrap; }
        .page-link { min-width:38px; min-height:36px; display:inline-flex; align-items:center; justify-content:center; padding:7px 11px; border:1px solid var(--color-border); border-radius:8px; background:#fff; color:var(--color-text-primary); font-weight:700; text-decoration:none; }
        .page-link.active { border-color:var(--color-primary-600); background:var(--color-primary-600); color:#fff; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container">

    <h1>Danh sách Hóa đơn</h1>
    <p>Quản lý các hóa đơn, theo dõi doanh thu và trạng thái thanh toán của khách hàng.</p>

    <div class="toolbar" style="margin-top: 20px;">
        <form class="filters" method="get" action="">
            <div class="search-field">
                <label class="form-label" for="keyword">Tìm kiếm</label>
                <input class="form-control" id="keyword" name="keyword" value="${keyword}" placeholder="Mã HĐ, mã Booking, SĐT, Tên KH...">
            </div>
            <div class="filter-field">
                <label class="form-label" for="status">Trạng thái</label>
                <select class="form-control" id="status" name="status">
                    <option value="">Tất cả trạng thái</option>
                    <option value="UNPAID" ${status eq 'UNPAID' ? 'selected' : ''}>Chưa thanh toán</option>
                    <option value="PARTIALLY_PAID" ${status eq 'PARTIALLY_PAID' ? 'selected' : ''}>Thanh toán 1 phần</option>
                    <option value="PAID" ${status eq 'PAID' ? 'selected' : ''}>Đã thanh toán (Hoàn tất)</option>
                    <option value="REFUNDED" ${status eq 'REFUNDED' ? 'selected' : ''}>Đã hoàn tiền</option>
                </select>
            </div>
            <button class="button button-secondary" type="submit">Lọc</button>
        </form>
    </div>

    <table class="data-table">
        <thead>
        <tr>
            <th>Mã Hóa Đơn</th>
            <th>Mã Booking</th>
            <th>Khách hàng</th>
            <th>Ngày cập nhật</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty invoices}">
                <tr>
                    <td colspan="6" style="text-align: center; padding: 40px;">
                        <strong>Không tìm thấy hóa đơn nào</strong><br>
                        <span style="color: gray;">Hãy thử điều chỉnh lại bộ lọc tìm kiếm.</span>
                    </td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="inv" items="${invoices}">
                    <tr>
                        <td><strong>${inv.invoiceCode}</strong></td>
                        <td>${inv.bookingCode}</td>
                        <td>${inv.guestName}</td>
                        <td><fmt:formatDate value="${inv.updatedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td style="font-weight: 600; color: #166534;">
                            <fmt:formatNumber value="${inv.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${inv.status eq 'PAID'}">
                                    <span class="badge badge-PAID">Đã thanh toán</span>
                                </c:when>
                                <c:when test="${inv.status eq 'PARTIALLY_PAID'}">
                                    <span class="badge badge-PARTIALLY_PAID">Thanh toán 1 phần</span>
                                </c:when>
                                <c:when test="${inv.status eq 'UNPAID'}">
                                    <span class="badge badge-UNPAID">Chưa thanh toán</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-REFUNDED">${inv.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>

    <c:if test="${totalPages > 1}">
        <div class="pagination-bar">
            <span style="color: var(--color-text-secondary); font-size: 13px;">Hiển thị trang ${currentPage} trên tổng số ${totalPages} trang</span>
            <div class="pagination-links">
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <a href="?keyword=${keyword}&status=${status}&page=${i}" 
                       class="page-link ${i == currentPage ? 'active' : ''}">
                        ${i}
                    </a>
                </c:forEach>
            </div>
        </div>
    </c:if>

</main>
</body>
</html>
