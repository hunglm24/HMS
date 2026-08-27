<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.NumberFormat,java.text.SimpleDateFormat,java.util.Locale,model.DashboardStats" %>
<%!
    private String h(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String statusLabel(String status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case "PENDING_PAYMENT":
                return "Chờ thanh toán";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "CHECKED_IN":
                return "Đang ở";
            case "CHECKOUT_PENDING":
                return "Chờ trả phòng";
            case "CHECKED_OUT":
                return "Đã trả phòng";
            case "CANCELLED":
                return "Đã hủy";
            case "PENDING":
                return "Chờ xử lý";
            case "IN_PROGRESS":
                return "Đang làm";
            case "COMPLETED":
                return "Hoàn thành";
            default:
                return status;
        }
    }

    private String taskLabel(String taskType) {
        if (taskType == null) return "Công việc";
        switch (taskType) {
            case "CHECKOUT_INSPECTION":
                return "Kiểm tra checkout";
            case "CLEANING":
                return "Dọn phòng";
            case "EQUIPMENT_REPAIR":
                return "Sửa thiết bị";
            case "MAINTENANCE_CHECK":
                return "Kiểm tra bảo trì";
            case "EQUIPMENT_REPLACEMENT":
                return "Thay thiết bị";
            default:
                return taskType;
        }
    }

    private String beanString(Object bean, String getterName) {
        if (bean == null) return "";
        try {
            Object value = bean.getClass().getMethod(getterName).invoke(bean);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }
%>
<%
    DashboardStats stats = (DashboardStats) request.getAttribute("dashboardStats");
    if (stats == null) stats = new DashboardStats();
    Object currentUser = session.getAttribute("currentUser");
    Object permissions = session.getAttribute("permissionCodes");
    String role = beanString(currentUser, "getRoleName");
    String fullName = beanString(currentUser, "getFullName");
    boolean reception = "RECEPTIONIST".equalsIgnoreCase(role);
    boolean housekeeping = "HOUSEKEEPING".equalsIgnoreCase(role);
    boolean manager = "HOTEL_MANAGER".equalsIgnoreCase(role);
    boolean admin = "ADMIN".equalsIgnoreCase(role);
    boolean canAdminUsers = admin || (permissions instanceof java.util.Set && ((java.util.Set<?>) permissions).contains("ADMIN_USERS"));
    boolean canAdminRoles = admin || (permissions instanceof java.util.Set && ((java.util.Set<?>) permissions).contains("ADMIN_ROLES"));
    boolean canAdminLogs = admin || (permissions instanceof java.util.Set && ((java.util.Set<?>) permissions).contains("ADMIN_LOGS"));
    NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    int occupancyRate = stats.getOccupancyRatePercent();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260825-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container dashboard-page">
    <section class="section-head">
        <div>
            <p class="section-kicker">Dashboard</p>
            <h1>Xin chào, <%= h(fullName == null || fullName.trim().isEmpty() ? "nhân viên" : fullName) %></h1>
            <p>Theo dõi nhanh các việc cần xử lý trong ngày và truy cập đúng nghiệp vụ theo vai trò.</p>
        </div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/dashboard">Làm mới</a>
    </section>

    <% if (request.getAttribute("dashboardError") != null) { %>
        <div class="message error" role="alert"><%= h(String.valueOf(request.getAttribute("dashboardError"))) %></div>
    <% } %>

    <section class="kpi-grid dashboard-kpis">
        <% if (reception) { %>
            <div class="metric-card"><span>Khách đến hôm nay</span><strong><%= stats.getArrivalsToday() %></strong></div>
            <div class="metric-card"><span>Khách trả phòng</span><strong><%= stats.getDeparturesToday() %></strong></div>
            <div class="metric-card"><span>Chờ thanh toán</span><strong><%= stats.getPendingPayments() %></strong></div>
            <div class="metric-card"><span>Phòng trống</span><strong><%= stats.getAvailableRooms() %>/<%= stats.getTotalRooms() %></strong></div>
        <% } else if (housekeeping) { %>
            <div class="metric-card"><span>Task của tôi</span><strong><%= stats.getMyOpenTasks() %></strong></div>
            <div class="metric-card"><span>Đang dọn/kiểm tra</span><strong><%= stats.getCleaningRooms() + stats.getInspectionRooms() %></strong></div>
            <div class="metric-card"><span>Sự cố thiết bị</span><strong><%= stats.getOpenIssueTasks() %></strong></div>
            <div class="metric-card"><span>Phòng sẵn sàng</span><strong><%= stats.getAvailableRooms() %></strong></div>
        <% } else if (manager) { %>
            <div class="metric-card"><span>Doanh thu hôm nay</span><strong><%= h(money.format(stats.getRevenueToday())) %></strong></div>
            <div class="metric-card"><span>Doanh thu tháng</span><strong><%= h(money.format(stats.getRevenueThisMonth())) %></strong></div>
            <div class="metric-card dashboard-occupancy-card">
                <span>Phòng đang sử dụng hôm nay</span>
                <strong><%= stats.getTodayOccupiedRooms() %>/<%= stats.getTotalRooms() %></strong>
                <div class="dashboard-progress" aria-hidden="true"><i style="width:<%= occupancyRate %>%"></i></div>
                <small>Tỷ lệ sử dụng: <%= occupancyRate %>%</small>
            </div>
            <div class="metric-card"><span>Task đang mở</span><strong><%= stats.getOpenHousekeepingTasks() + stats.getOpenIssueTasks() %></strong></div>
        <% } else { %>
            <div class="metric-card"><span>Nhân viên active</span><strong><%= stats.getActiveStaff() %></strong></div>
            <div class="metric-card"><span>Khách hàng active</span><strong><%= stats.getActiveCustomers() %></strong></div>
            <div class="metric-card"><span>Booking chờ thanh toán</span><strong><%= stats.getPendingPayments() %></strong></div>
            <div class="metric-card"><span>Phòng vận hành</span><strong><%= stats.getTotalRooms() %></strong></div>
        <% } %>
    </section>

    <section class="dashboard-grid dashboard-actions" aria-label="Chức năng nhanh">
        <% if (reception) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/bookings"><span>Lễ tân</span><h3>Danh sách booking</h3><p>Xác nhận đặt phòng, check-in, check-out và xử lý thanh toán.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/walk-in"><span>Đặt phòng</span><h3>Đặt tại quầy</h3><p>Tạo booking trực tiếp cho khách vãng lai.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/reception/room-map"><span>Phòng</span><h3>Sơ đồ phòng</h3><p>Xem trạng thái phòng theo tầng để điều phối nhanh.</p></a>
        <% } %>
        <% if (housekeeping) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/tasks?view=mine"><span>Nhiệm vụ</span><h3>Task của tôi</h3><p>Nhận và cập nhật các việc dọn phòng, kiểm tra checkout.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/tasks?view=history"><span>Lịch sử</span><h3>Lịch sử dọn phòng</h3><p>Xem lại các task đã hoàn thành hoặc đã hủy.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/housekeeping/issues"><span>Sự cố</span><h3>Sự cố thiết bị</h3><p>Báo cáo hỏng, mất hoặc cần bảo trì thiết bị.</p></a>
        <% } %>
        <% if (manager) { %>
            <a class="preview-card" href="${pageContext.request.contextPath}/dashboard"><span>Báo cáo</span><h3>Báo cáo vận hành</h3><p>Theo dõi kết quả vận hành và hiệu suất phòng.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/rooms"><span>Phòng</span><h3>Quản lý phòng</h3><p>Quản lý phòng vật lý, tầng và trạng thái phòng.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/housekeeping"><span>Dọn phòng</span><h3>Lịch sử dọn phòng</h3><p>Theo dõi tiến độ kiểm tra và công việc dọn dẹp phòng.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/issues"><span>Sự cố</span><h3>Sự cố thiết bị</h3><p>Theo dõi và nghiệm thu các thiết bị cần sửa chữa.</p></a>
            <a class="preview-card" href="${pageContext.request.contextPath}/manager/feedbacks"><span>Đánh giá</span><h3>Đánh giá khách hàng</h3><p>Xem phản hồi khách hàng, quản lý hiển thị và chuyển tiếp sự cố.</p></a>
        <% } %>
        <% if (canAdminUsers) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/users"><span>Admin</span><h3>Người dùng</h3><p>Quản lý tài khoản nội bộ, role và trạng thái hoạt động.</p></a>
        <% } %>
        <% if (canAdminRoles) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/roles"><span>Phân quyền</span><h3>Vai trò và quyền</h3><p>Cập nhật quyền truy cập cho các role nội bộ.</p></a>
        <% } %>
        <% if (canAdminLogs) { %>
            <a class="preview-card admin-action-card" href="${pageContext.request.contextPath}/admin/logs"><span>Kiểm tra</span><h3>Nhật ký hệ thống</h3><p>Theo dõi các thao tác quản trị quan trọng.</p></a>
        <% } %>
    </section>

    <% if (reception || manager) { %>
        <section class="dashboard-two-column">
            <article class="dashboard-panel">
                <div class="dashboard-panel__head"><h2>Booking gần đây</h2><a href="${pageContext.request.contextPath}<%= reception ? "/reception/bookings" : "/dashboard" %>">Xem tất cả</a></div>
                <% if (stats.getRecentBookings().isEmpty()) { %>
                    <p>Chưa có booking nào.</p>
                <% } else { %>
                    <div class="dashboard-list">
                        <% for (DashboardStats.RecentBooking booking : stats.getRecentBookings()) { %>
                            <div class="dashboard-list__row">
                                <div><strong><%= h(booking.getBookingCode()) %></strong><span><%= h(booking.getGuestName()) %></span></div>
                                <div><span><%= booking.getCheckInDate() == null ? "-" : h(dateFormat.format(booking.getCheckInDate())) %></span><span><%= h(statusLabel(booking.getStatus())) %></span></div>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            </article>
            <article class="dashboard-panel">
                <div class="dashboard-panel__head"><h2>Phòng cần chú ý</h2><a href="${pageContext.request.contextPath}<%= reception ? "/reception/room-map" : "/manager/room-map" %>">Sơ đồ phòng</a></div>
                <div class="dashboard-room-status">
                    <span>Đang sử dụng hôm nay <strong><%= stats.getTodayOccupiedRooms() %></strong></span>
                    <span>Đang dọn <strong><%= stats.getCleaningRooms() %></strong></span>
                    <span>Kiểm tra <strong><%= stats.getInspectionRooms() %></strong></span>
                    <span>Bảo trì <strong><%= stats.getMaintenanceRooms() + stats.getNotReadyRooms() %></strong></span>
                </div>
            </article>
        </section>
    <% } %>

    <% if (housekeeping || manager) { %>
        <section class="dashboard-panel" style="<%= (reception || manager) ? "margin-top: 24px;" : "" %>">
            <div class="dashboard-panel__head">
                <h2>Task ưu tiên</h2>
                <a href="${pageContext.request.contextPath}<%= housekeeping ? "/housekeeping/tasks?view=mine" : "/manager/housekeeping" %>"><%= housekeeping ? "Xem tất cả" : "Xem task" %></a>
            </div>
            <% if (stats.getUrgentTasks().isEmpty()) { %>
                <p style="color: var(--color-text-secondary); margin: 16px 0;">Không có task nào cần xử lý gấp.</p>
            <% } else { %>
                <div class="dashboard-list">
                    <% for (DashboardStats.UrgentTask task : stats.getUrgentTasks()) { %>
                        <div class="dashboard-list__row">
                            <div>
                                <strong>Phòng <%= h(task.getRoomNumber()) %></strong>
                                <span><%= h(taskLabel(task.getTaskType())) %> · <%= h(task.getStaffName() != null && !task.getStaffName().isBlank() ? task.getStaffName() : "Chưa phân công") %></span>
                            </div>
                            <div>
                                <span style="font-weight: 700; color: <%= "URGENT".equalsIgnoreCase(task.getPriority()) ? "#dc2626" : ("HIGH".equalsIgnoreCase(task.getPriority()) ? "#ea580c" : "#2563eb") %>;">
                                    <%= "URGENT".equalsIgnoreCase(task.getPriority()) ? "🔥 Khẩn cấp" : ("HIGH".equalsIgnoreCase(task.getPriority()) ? "⚡ Ưu tiên cao" : "● Bình thường") %>
                                </span>
                                <span><%= h(statusLabel(task.getStatus())) %></span>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } %>
        </section>
    <% } %>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
