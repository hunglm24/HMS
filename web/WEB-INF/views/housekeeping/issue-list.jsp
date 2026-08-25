<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="model.HousekeepingTask" %>
<%!
    private String enc(Object value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(String.valueOf(value), java.nio.charset.StandardCharsets.UTF_8);
    }
    private String query(String search, String floor, String taskType, String status, boolean includeSort, String sort, String direction) {
        StringBuilder q = new StringBuilder();
        if (search != null && !search.isEmpty()) q.append("search=").append(enc(search)).append("&");
        if (floor != null && !floor.isEmpty()) q.append("floor=").append(enc(floor)).append("&");
        if (taskType != null && !taskType.isEmpty()) q.append("taskType=").append(enc(taskType)).append("&");
        if (status != null && !status.isEmpty()) q.append("status=").append(enc(status)).append("&");
        if (includeSort) q.append("sort=").append(enc(sort)).append("&direction=").append(enc(direction));
        else if (q.length() > 0) q.setLength(q.length() - 1);
        return q.toString();
    }
    private String sortUrl(String search, String floor, String taskType, String status, String currentSort, String currentDir, String column, String baseUrl) {
        String next = column.equals(currentSort) && "asc".equals(currentDir) ? "desc" : "asc";
        String base = query(search, floor, taskType, status, false, "", "");
        return baseUrl + (base.isEmpty() ? "?" : "?" + base + "&") + "sort=" + enc(column) + "&direction=" + next;
    }
    private String sortClass(String currentSort, String currentDir, String column) {
        return column.equals(currentSort) ? "sorted-" + currentDir : "sortable";
    }
%>
<%
    String searchStr = (String) request.getAttribute("search");
    String floorStr = (String) request.getAttribute("floor");
    String taskTypeStr = (String) request.getAttribute("taskType");
    String statusStr = (String) request.getAttribute("status");
    String damageStatusStr = (String) request.getAttribute("damageStatus");
    String currentSort = (String) request.getAttribute("currentSort");
    String currentDir = (String) request.getAttribute("currentDir");
    String contextPath = request.getContextPath();
    Boolean isMgrAttr = (Boolean) request.getAttribute("isManager");
    boolean isManager = Boolean.TRUE.equals(isMgrAttr);
    String activeTab = (String) request.getAttribute("activeTab");
    if (activeTab == null || activeTab.isBlank()) activeTab = isManager ? "damage" : "maintenance";
    String baseUrl = contextPath + (isManager ? "/manager/issues" : "/housekeeping/issues");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sự cố &amp; Đền bù thiết bị | HMS</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/main.css?v=20260821-1">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/rooms.css?v=20260821-1">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/housekeeping.css?v=20260825-2">
</head>
<body class="room-management-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />

<main class="page-container hk-page">
    <section class="hk-hero">
        <div>
            <p class="hk-eyebrow"><%= isManager ? "Quản lý khách sạn" : "Vận hành phòng" %></p>
            <h1>Sự cố &amp; Đền bù thiết bị</h1>
            <p>Theo dõi sự cố phòng, phê duyệt phí bồi thường hư hại và điều phối bảo trì thiết bị.</p>
        </div>
        <div>
            <a href="<%= contextPath %><%= isManager ? "/manager/issues/report" : "/housekeeping/issues/report" %>" class="btn btn-primary">
                + Báo cáo sự cố mới
            </a>
        </div>
    </section>
        
    <c:if test="${not empty sessionScope.toastMessage}">
        <div class="alert ${sessionScope.toastType eq 'toast-error' ? 'alert-danger' : 'alert-success'}">
            ${sessionScope.toastMessage}
        </div>
        <c:remove var="toastMessage" scope="session"/>
        <c:remove var="toastType" scope="session"/>
    </c:if>

    <% if (isManager) { %>
    <!-- TAB CHUYỂN ĐỔI QUẢN LÝ -->
    <nav class="hk-main-tabs" aria-label="Phân loại sự cố">
        <a class="hk-main-tab <%= "damage".equals(activeTab) ? "active" : "" %>" href="<%= baseUrl %>?tab=damage">
            <span>💰 Đền bù thiết bị (Damage Reports)</span>
            <c:if test="${pendingDamageCount > 0}">
                <span class="hk-tab-badge badge-pending-red">${pendingDamageCount}</span>
            </c:if>
        </a>
        <a class="hk-main-tab <%= "maintenance".equals(activeTab) ? "active" : "" %>" href="<%= baseUrl %>?tab=maintenance">
            <span>🛠️ Sự cố &amp; Bảo trì phòng (Maintenance)</span>
        </a>
    </nav>
    <% } %>

    <c:choose>
        <%-- TAB 1: DANH SÁCH BÁO CÁO ĐỀN BÙ (DAMAGE REPORTS) --%>
        <c:when test="${activeTab eq 'damage' and isManager}">
            <form method="get" action="<%= baseUrl %>" class="hk-filters">
                <input type="hidden" name="tab" value="damage">
                <label class="hk-search">Tìm kiếm báo cáo
                    <input type="search" name="search" maxlength="50" value="<c:out value='${search}'/>" placeholder="Mã booking, phòng, thiết bị, khách..">
                </label>
                <label>Trạng thái xử lý
                    <select name="damageStatus">
                        <option value="" ${empty damageStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                        <option value="PENDING" ${damageStatus eq 'PENDING' ? 'selected' : ''}>🔴 Chờ duyệt phạt (Pending)</option>
                        <option value="CHARGED" ${damageStatus eq 'CHARGED' ? 'selected' : ''}>🟣 Đã duyệt phạt (Charged)</option>
                        <option value="WAIVED" ${damageStatus eq 'WAIVED' ? 'selected' : ''}>⚪ Miễn phạt (Waived)</option>
                        <option value="PAID" ${damageStatus eq 'PAID' ? 'selected' : ''}>🟢 Đã thanh toán (Paid)</option>
                    </select>
                </label>
                <div class="hk-filter-actions">
                    <button type="submit">Lọc</button>
                    <a href="<%= baseUrl %>?tab=damage">Đặt lại</a>
                </div>
            </form>

            <div class="hk-table-wrap" data-pagination-root data-pagination-key="damage-reports-table" data-pagination-size="10">
                <table class="hk-table">
                    <thead>
                        <tr>
                            <th>ID / Booking</th>
                            <th>Phòng &amp; Khách</th>
                            <th>Thiết bị</th>
                            <th>Tình trạng</th>
                            <th>Ghi chú hiện trường</th>
                            <th>Phí đền bù</th>
                            <th>Trạng thái</th>
                            <th style="text-align: right;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="dr" items="${damageReports}">
                            <tr data-pagination-item>
                                <td data-label="ID / Booking">
                                    <strong>#DR-${dr.id}</strong><br>
                                    <small style="color: #667085;">#${dr.bookingCode}</small>
                                </td>
                                <td data-label="Phòng &amp; Khách">
                                    <span class="hk-room-number">P.${HousekeepingTask.esc(dr.roomNumber)}</span><br>
                                    <small style="color: #475467;">${HousekeepingTask.esc(dr.customerName != null ? dr.customerName : 'Khách vãng lai')}</small>
                                </td>
                                <td data-label="Thiết bị">
                                    <strong>${HousekeepingTask.esc(dr.equipmentName)}</strong><br>
                                    <small style="color: #667085;">Giá gốc: <fmt:formatNumber value="${dr.defaultPrice}" pattern="#,##0"/> đ</small>
                                </td>
                                <td data-label="Tình trạng">
                                    <span class="equipment-status status-${dr.damageType != null ? dr.damageType.toLowerCase() : 'damaged'}">
                                        ${dr.damageTypeLabel}
                                    </span>
                                </td>
                                <td data-label="Ghi chú hiện trường">
                                    <span style="color: #344054; font-size: 13px;">${HousekeepingTask.esc(dr.housekeeperNote != null && !dr.housekeeperNote.isBlank() ? dr.housekeeperNote : dr.note)}</span>
                                </td>
                                <td data-label="Phí đền bù">
                                    <strong style="color: ${dr.chargeStatus eq 'WAIVED' ? '#475467' : '#b42318'}; font-size: 15px;">
                                        <fmt:formatNumber value="${dr.compensationAmount}" pattern="#,##0"/> đ
                                    </strong>
                                    <c:if test="${dr.chargeStatus eq 'PENDING'}">
                                        <br><small style="color: #667085;">(Đề xuất 30%: <fmt:formatNumber value="${dr.suggestedAmount}" pattern="#,##0"/> đ)</small>
                                    </c:if>
                                </td>
                                <td data-label="Trạng thái">
                                    <span class="hk-badge charge-status-${dr.chargeStatus != null ? dr.chargeStatus.toLowerCase() : 'pending'}">
                                        ${dr.chargeStatusLabel}
                                    </span>
                                </td>
                                <td class="hk-row-action" style="text-align: right;">
                                    <c:choose>
                                        <c:when test="${dr.chargeStatus eq 'PENDING'}">
                                            <button type="button" class="btn-review-action"
                                                    onclick="openDamageModal({
                                                        id: ${dr.id},
                                                        bookingCode: '${dr.bookingCode}',
                                                        customerName: '${HousekeepingTask.esc(dr.customerName)}',
                                                        roomNumber: '${dr.roomNumber}',
                                                        floorNumber: ${dr.floorNumber},
                                                        equipmentName: '${HousekeepingTask.esc(dr.equipmentName)}',
                                                        damageType: '${dr.damageTypeLabel}',
                                                        defaultPrice: ${dr.defaultPrice},
                                                        suggestedAmount: ${dr.suggestedAmount},
                                                        compensationAmount: ${dr.compensationAmount},
                                                        chargeStatus: '${dr.chargeStatus}',
                                                        note: '${HousekeepingTask.esc(dr.note)}',
                                                        housekeeperNote: '${HousekeepingTask.esc(dr.housekeeperNote)}',
                                                        inspectedByName: '${HousekeepingTask.esc(dr.inspectedByName)}',
                                                        createdAt: '<fmt:formatDate value="${dr.createdAt}" pattern="dd/MM/yyyy HH:mm"/>'
                                                    })">
                                                <i class="fa-solid fa-gavel"></i> Xử lý / Duyệt
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="button" class="btn-view-detail"
                                                    onclick="openDamageModal({
                                                        id: ${dr.id},
                                                        bookingCode: '${dr.bookingCode}',
                                                        customerName: '${HousekeepingTask.esc(dr.customerName)}',
                                                        roomNumber: '${dr.roomNumber}',
                                                        floorNumber: ${dr.floorNumber},
                                                        equipmentName: '${HousekeepingTask.esc(dr.equipmentName)}',
                                                        damageType: '${dr.damageTypeLabel}',
                                                        defaultPrice: ${dr.defaultPrice},
                                                        suggestedAmount: ${dr.suggestedAmount},
                                                        compensationAmount: ${dr.compensationAmount},
                                                        chargeStatus: '${dr.chargeStatus}',
                                                        note: '${HousekeepingTask.esc(dr.note)}',
                                                        housekeeperNote: '${HousekeepingTask.esc(dr.housekeeperNote)}',
                                                        inspectedByName: '${HousekeepingTask.esc(dr.inspectedByName)}',
                                                        createdAt: '<fmt:formatDate value="${dr.createdAt}" pattern="dd/MM/yyyy HH:mm"/>'
                                                    })">
                                                <i class="fa-solid fa-eye"></i> Xem lại
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty damageReports}">
                            <tr>
                                <td colspan="8" class="table-empty-notice">
                                    <div class="empty-icon-lg">💰</div>
                                    <strong class="empty-title">Không có báo cáo hư hại nào</strong>
                                    <span>Toàn bộ đồ dùng và thiết bị phòng của khách trả đều nguyên vẹn hoặc đã được giải quyết.</span>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
                <div class="room-management-pagination" data-pagination-controls data-pagination-target="damage-reports-table"></div>
            </div>
        </c:when>

        <%-- TAB 2: DANH SÁCH SỰ CỐ & BẢO TRÌ PHÒNG (MAINTENANCE TASKS) --%>
        <c:otherwise>
            <form method="get" action="<%= baseUrl %>" class="hk-filters">
                <c:if test="${isManager}">
                    <input type="hidden" name="tab" value="maintenance">
                </c:if>
                <label class="hk-search">Tìm kiếm sự cố
                    <input type="search" name="search" maxlength="50" value="<c:out value='${search}'/>" placeholder="Số phòng, thiết bị..">
                </label>
                <label>Tầng
                    <select name="floor">
                        <option value="" ${empty floor ? 'selected' : ''}>Tất cả tầng</option>
                        <c:forEach var="f" items="${floorOptions}">
                            <option value="${f}" ${floor == f ? 'selected' : ''}>Tầng ${f}</option>
                        </c:forEach>
                    </select>
                </label>
                <label>Loại công việc
                    <select name="taskType">
                        <option value="">Tất cả loại sự cố</option>
                        <option value="EQUIPMENT_REPAIR" ${taskType eq 'EQUIPMENT_REPAIR' ? 'selected' : ''}>Sửa chữa thiết bị</option>
                        <option value="MAINTENANCE_CHECK" ${taskType eq 'MAINTENANCE_CHECK' ? 'selected' : ''}>Kiểm tra bảo trì</option>
                        <option value="EQUIPMENT_REPLACEMENT" ${taskType eq 'EQUIPMENT_REPLACEMENT' ? 'selected' : ''}>Thay thế thiết bị</option>
                    </select>
                </label>
                <label>Trạng thái
                    <select name="status">
                        <option value="">Tất cả trạng thái</option>
                        <option value="PENDING" ${status eq 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                        <option value="IN_PROGRESS" ${status eq 'IN_PROGRESS' ? 'selected' : ''}>Đang bảo trì</option>
                        <option value="COMPLETED" ${status eq 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                    </select>
                </label>
                <div class="hk-filter-actions">
                    <button type="submit">Lọc</button>
                    <a href="<%= baseUrl %><%= isManager ? "?tab=maintenance" : "" %>">Đặt lại</a>
                </div>
            </form>

            <div class="hk-table-wrap" data-pagination-root data-pagination-key="issue-list-table" data-pagination-size="10">
                <table class="hk-table">
                    <thead>
                        <tr>
                            <th class="<%= sortClass(currentSort, currentDir, "id") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "id", baseUrl) %>">ID</a></th>
                            <th class="<%= sortClass(currentSort, currentDir, "room") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "room", baseUrl) %>">Phòng</a></th>
                            <th class="<%= sortClass(currentSort, currentDir, "type") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "type", baseUrl) %>">Loại Task</a></th>
                            <th>Mô tả</th>
                            <th class="<%= sortClass(currentSort, currentDir, "created_at") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "created_at", baseUrl) %>">Thời gian báo cáo</a></th>
                            <th class="<%= sortClass(currentSort, currentDir, "status") %>"><a href="<%= sortUrl(searchStr, floorStr, taskTypeStr, statusStr, currentSort, currentDir, "status", baseUrl) %>">Trạng thái</a></th>
                            <th><span class="sr-only">Thao tác</span></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="task" items="${tasks}">
                            <tr data-pagination-item>
                                <td data-label="ID">#${task.taskId}</td>
                                <td data-label="Phòng">
                                    <span class="hk-room-number">${HousekeepingTask.esc(task.roomNumber)}</span><br>
                                    <small>Tầng ${task.floorNumber}</small>
                                </td>
                                <td data-label="Loại Task"><strong>${HousekeepingTask.esc(task.getTaskTypeLabel())}</strong></td>
                                <td data-label="Mô tả">${HousekeepingTask.esc(task.note)}</td>
                                <td data-label="Thời gian báo cáo"><fmt:formatDate value="${task.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                <td data-label="Trạng thái">
                                    <span class="hk-badge task-${task.status.toLowerCase()}">${task.getStatusLabel()}</span>
                                </td>
                                <td class="hk-row-action">
                                    <form method="get" action="<%= contextPath %><%= isManager ? "/manager/issues/verify" : "/housekeeping/issues/verify" %>">
                                        <input type="hidden" name="taskId" value="${task.taskId}">
                                        <input type="hidden" name="roomId" value="${task.roomId}">
                                        <c:choose>
                                            <c:when test="${task.status eq 'PENDING' or task.status eq 'IN_PROGRESS'}">
                                                <button type="submit" class="btn-verify-action">Kiểm tra bảo trì</button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="submit" class="btn-history-action">Xem lịch sử sửa</button>
                                            </c:otherwise>
                                        </c:choose>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty tasks}">
                            <tr>
                                <td colspan="7" class="table-empty-notice">
                                    <div class="empty-icon-lg">🛠️</div>
                                    <c:choose>
                                        <c:when test="${not empty search or not empty floor or not empty taskType or not empty status}">
                                            <strong class="empty-title">Không tìm thấy sự cố phù hợp</strong>
                                            <span>Không có sự cố thiết bị nào khớp với tiêu chí tìm kiếm hoặc bộ lọc đang chọn.</span>
                                        </c:when>
                                        <c:otherwise>
                                            <strong class="empty-title">Không có sự cố thiết bị nào</strong>
                                            <span>Hiện tại toàn bộ thiết bị trong các phòng đều đang hoạt động bình thường.</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
                <div class="room-management-pagination" data-pagination-controls data-pagination-target="issue-list-table"></div>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<!-- ========================================================================= -->
<!-- MODAL DUYỆT ĐỀN BÙ THIẾT BỊ (DAMAGE REVIEW MODAL) -->
<!-- ========================================================================= -->
<div id="damageModalBackdrop" class="damage-modal-backdrop" onclick="handleBackdropClick(event)">
    <div class="damage-modal-dialog" onclick="event.stopPropagation()">
        <div class="damage-modal-header">
            <h3><i class="fa-solid fa-file-invoice-dollar" style="color: #175cd3;"></i> <span id="modalHeaderTitle">Xử lý đền bù thiết bị</span></h3>
            <button type="button" class="damage-modal-close" onclick="closeDamageModal()">&times;</button>
        </div>
        <form id="damageReviewForm" method="post" action="<%= contextPath %>/manager/issues">
            <input type="hidden" name="reportId" id="modalReportId">
            <input type="hidden" name="action" id="modalAction" value="CHARGE">
            <div class="damage-modal-body">
                <!-- THÔNG TIN PHÒNG & BOOKING -->
                <div class="damage-info-grid">
                    <div class="damage-info-item">
                        <span>Mã Đặt phòng (Booking)</span>
                        <strong id="modalBookingCode">--</strong>
                    </div>
                    <div class="damage-info-item">
                        <span>Khách hàng</span>
                        <strong id="modalCustomerName">--</strong>
                    </div>
                    <div class="damage-info-item">
                        <span>Phòng &amp; Vị trí</span>
                        <strong id="modalRoomInfo">--</strong>
                    </div>
                    <div class="damage-info-item">
                        <span>Thiết bị &amp; Tình trạng</span>
                        <strong id="modalEquipmentInfo">--</strong>
                    </div>
                </div>

                <!-- GHI CHÚ HIỆN TRƯỜNG CỦA HOUSEKEEPER -->
                <div class="damage-hk-box">
                    <span style="font-size: 12px; font-weight: 700; color: #b42318;">📋 Ghi chú hiện trường của Nhân viên buồng phòng:</span>
                    <p id="modalHousekeeperNote">Không có ghi chú thêm.</p>
                </div>

                <!-- KHỐI ĐỊNH GIÁ ĐỀN BÙ -->
                <div class="damage-pricing-card">
                    <div class="damage-calc-row">
                        <span>Giá niêm yết thiết bị:</span>
                        <strong id="modalDefaultPriceDisplay">0 đ</strong>
                    </div>
                    <div class="damage-calc-row" style="color: #b42318; border-bottom: 1px dashed #e4e7ec; padding-bottom: 10px;">
                        <span>Đề xuất hệ thống (30% hư hại / 100% mất):</span>
                        <strong id="modalSuggestedPriceDisplay">0 đ</strong>
                    </div>

                    <div class="damage-input-group">
                        <label for="modalCompensationAmount">Số tiền bồi thường chính thức (VNĐ) <span style="color: #d92d20;">*</span></label>
                        <input type="number" name="compensationAmount" id="modalCompensationAmount" min="0" step="1000" required placeholder="Nhập số tiền...">
                        <small style="color: #667085; display: block; margin-top: 4px;">Quản lý có thể điều chỉnh số tiền theo thực tế sửa chữa trước khi bấm Duyệt phạt.</small>
                    </div>

                    <div class="damage-input-group">
                        <label for="modalNote">Ghi chú phụ phí (Sẽ in trên Hóa đơn của khách)</label>
                        <input type="text" name="note" id="modalNote" maxlength="500" placeholder="Ví dụ: Bồi thường hư hỏng Tivi Samsung">
                    </div>
                </div>
            </div>
            <div class="damage-modal-footer">
                <button type="button" class="btn-cancel-modal" onclick="closeDamageModal()">Đóng</button>
                <div style="display: flex; gap: 10px;">
                    <button type="button" class="btn-waive-submit" id="btnWaiveAction" onclick="submitDamageAction('WAIVE')">
                        <i class="fa-solid fa-hand-holding-heart"></i> Miễn phạt (Waive)
                    </button>
                    <button type="button" class="btn-charge-submit" id="btnChargeAction" onclick="submitDamageAction('CHARGE')">
                        <i class="fa-solid fa-bolt"></i> Xác nhận Phạt (Charge)
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
<script src="<%= contextPath %>/assets/js/pagination.js?v=20260820-7"></script>
<script>
    function formatMoney(num) {
        return new Intl.NumberFormat('vi-VN').format(num || 0) + ' đ';
    }

    function openDamageModal(data) {
        document.getElementById('modalReportId').value = data.id;
        document.getElementById('modalHeaderTitle').innerText = 'Xử lý đền bù - Phòng ' + data.roomNumber;
        document.getElementById('modalBookingCode').innerText = '#' + (data.bookingCode || 'BK' + data.id);
        document.getElementById('modalCustomerName').innerText = data.customerName || 'Khách vãng lai';
        document.getElementById('modalRoomInfo').innerText = 'Phòng ' + data.roomNumber + ' (Tầng ' + data.floorNumber + ')';
        document.getElementById('modalEquipmentInfo').innerText = data.equipmentName + ' · ' + data.damageType;
        document.getElementById('modalHousekeeperNote').innerText = data.housekeeperNote || data.note || 'Không có ghi chú thêm.';
        
        document.getElementById('modalDefaultPriceDisplay').innerText = formatMoney(data.defaultPrice);
        document.getElementById('modalSuggestedPriceDisplay').innerText = formatMoney(data.suggestedAmount);
        
        var compInput = document.getElementById('modalCompensationAmount');
        compInput.value = data.compensationAmount !== undefined && data.compensationAmount !== null ? data.compensationAmount : data.suggestedAmount;
        
        var noteInput = document.getElementById('modalNote');
        noteInput.value = data.note || ('Bồi thường hư hại: ' + data.equipmentName);

        var backdrop = document.getElementById('damageModalBackdrop');
        backdrop.classList.add('open');
    }

    function closeDamageModal() {
        var backdrop = document.getElementById('damageModalBackdrop');
        backdrop.classList.remove('open');
    }

    function handleBackdropClick(event) {
        if (event.target.id === 'damageModalBackdrop') {
            closeDamageModal();
        }
    }

    function submitDamageAction(actionType) {
        var form = document.getElementById('damageReviewForm');
        document.getElementById('modalAction').value = actionType;
        
        if (actionType === 'CHARGE') {
            var amount = parseFloat(document.getElementById('modalCompensationAmount').value);
            if (isNaN(amount) || amount < 0) {
                alert('Vui lòng nhập số tiền bồi thường hợp lệ (>= 0 VNĐ).');
                return;
            }
            if (!confirm('Xác nhận phạt ' + formatMoney(amount) + ' và đẩy khoản phí này vào Hóa đơn check-out của khách?')) {
                return;
            }
        } else if (actionType === 'WAIVE') {
            if (!confirm('Xác nhận MIỄN PHẠT cho sự cố này? Khoản tiền phạt sẽ được đưa về 0đ và không tính vào hóa đơn của khách.')) {
                return;
            }
        }
        form.submit();
    }
</script>
</body>
</html>
