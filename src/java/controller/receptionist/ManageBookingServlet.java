package controller.receptionist;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dao.BookingDao;
import model.CheckInBookingSummary;
import java.util.List;
import java.sql.SQLException;

@WebServlet(name = "ManageBookingServlet", urlPatterns = {"/reception/bookings", "/manager/bookings"})
public class ManageBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BookingDao bookingDao = new BookingDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean managerView = request.getServletPath().startsWith("/manager/");
        request.setAttribute("managerView", managerView);
        request.setAttribute("bookingBasePath", managerView ? "/manager/bookings" : "/reception/bookings");
        try {
            String keyword = request.getParameter("keyword");
            String status = request.getParameter("status");
            String fromDate = request.getParameter("fromDate");
            String toDate = request.getParameter("toDate");
            String source = request.getParameter("source");

            String pageStr = request.getParameter("page");
            int page = pageStr != null && !pageStr.isBlank() ? Integer.parseInt(pageStr) : 1;
            int limit = 10;
            int offset = (page - 1) * limit;

            List<CheckInBookingSummary> bookings = bookingDao.findCheckInBookings(keyword, status, null, null, null, null, offset, limit, fromDate, toDate, source);
            request.setAttribute("bookings", bookings);
            
            int totalListRecords = bookingDao.countCheckInBookings(keyword, status, null, null, fromDate, toDate, source);
            int totalPages = (int) Math.ceil((double) totalListRecords / limit);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            
            // Fetch metrics
            int allCount = bookingDao.countCheckInBookings(null, null, null, null, null, null, null);
            int pendingCount = bookingDao.countCheckInBookings(null, "PENDING_PAYMENT", null, null, null, null, null);
            int checkInTodayCount = bookingDao.countCheckInBookings(null, null, null, "today", null, null, null);
            int checkedInCount = bookingDao.countCheckInBookings(null, "CHECKED_IN", null, null, null, null, null);
            int checkoutPendingCount = bookingDao.countCheckInBookings(null, "CHECKOUT_PENDING", null, null, null, null, null);
            
            request.setAttribute("allCount", allCount);
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("checkInTodayCount", checkInTodayCount);
            request.setAttribute("checkedInCount", checkedInCount);
            request.setAttribute("checkoutPendingCount", checkoutPendingCount);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.getRequestDispatcher("/WEB-INF/views/reception/booking-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean managerView = request.getServletPath().startsWith("/manager/");
        String action = request.getParameter("action");
        String idStr = request.getParameter("id");
        if (idStr != null && action != null) {
            try {
                long bookingId = Long.parseLong(idStr);
                
                if ("CONFIRM".equals(action)) {
                    bookingDao.updateBookingStatus(bookingId, "CONFIRMED");
                    request.getSession().setAttribute("toastMessage", "Đã xác nhận đặt phòng.");
                    request.getSession().setAttribute("toastType", "toast-success");
                } else if ("REJECT".equals(action)) {
                    String reason = request.getParameter("reason");
                    if (reason == null || reason.trim().isEmpty()) {
                        reason = "Lễ tân từ chối không có lý do";
                    } else {
                        reason = "Lễ tân từ chối: " + reason;
                    }
                    bookingDao.cancelBooking(bookingId, reason);
                    request.getSession().setAttribute("toastMessage", "Đã hủy đặt phòng.");
                    request.getSession().setAttribute("toastType", "toast-success");
                } else if ("CHECK_IN".equals(action)) {
                    boolean canProceed = true;
                    String errorMessage = "";
                    
                    try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            // Check booking status and check_in_date
                            try (java.sql.PreparedStatement datePs = conn.prepareStatement("SELECT status, check_in_date FROM bookings WHERE id = ?")) {
                                datePs.setLong(1, bookingId);
                                try (java.sql.ResultSet dateRs = datePs.executeQuery()) {
                                    if (dateRs.next()) {
                                        String currentStatus = dateRs.getString("status");
                                        if (!"CONFIRMED".equals(currentStatus)) {
                                            canProceed = false;
                                            errorMessage = "Không thể Check-in! Đơn đặt phòng phải ở trạng thái Đã duyệt (CONFIRMED). Trạng thái hiện tại: " + currentStatus;
                                        } else {
                                            java.sql.Date checkInDate = dateRs.getDate("check_in_date");
                                            if (checkInDate != null) {
                                                java.time.LocalDate today = java.time.LocalDate.now();
                                                if (checkInDate.toLocalDate().isAfter(today)) {
                                                    canProceed = false;
                                                    errorMessage = "Không thể Check-in sớm! Đơn đặt phòng này có ngày nhận phòng là " + 
                                                                   checkInDate.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                                                   ". Nếu khách đến sớm, vui lòng thay đổi ngày nhận phòng trước.";
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (canProceed) {
                                // Process room assignment changes
                                java.util.Enumeration<String> paramNames = request.getParameterNames();
                                java.util.Set<Long> selectedRooms = new java.util.HashSet<>();
                                while (paramNames.hasMoreElements()) {
                                    String paramName = paramNames.nextElement();
                                    if (paramName.startsWith("assignedRoom_")) {
                                        long newRoomId = Long.parseLong(request.getParameter(paramName));
                                        if (!selectedRooms.add(newRoomId)) {
                                            canProceed = false;
                                            errorMessage = "Không thể gán một phòng vật lý cho nhiều phòng trong cùng booking!";
                                            break;
                                        }
                                        long brId = Long.parseLong(paramName.substring("assignedRoom_".length()));
                                        try (java.sql.PreparedStatement updateBrPs = conn.prepareStatement("UPDATE booking_rooms SET room_id = ? WHERE id = ? AND booking_id = ?")) {
                                            updateBrPs.setLong(1, newRoomId);
                                            updateBrPs.setLong(2, brId);
                                            updateBrPs.setLong(3, bookingId);
                                            updateBrPs.executeUpdate();
                                        }
                                    }
                                }
                            }
                            
                            if (canProceed) {
                                String sqlRooms = "SELECT r.id, r.status FROM booking_rooms br JOIN rooms r ON br.room_id = r.id WHERE br.booking_id = ?";
                                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlRooms)) {
                                    ps.setLong(1, bookingId);
                                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                                        while (rs.next()) {
                                            long roomId = rs.getLong("id");
                                            String physicalStatus = rs.getString("status");
                                            
                                            if (!"AVAILABLE".equals(physicalStatus)) {
                                                canProceed = false;
                                                errorMessage = "Không thể Check-in! Một số phòng chưa sẵn sàng (Trạng thái hiện tại: " + physicalStatus + ").";
                                                break;
                                            } else {
                                                // Update room physical status to OCCUPIED
                                                try (java.sql.PreparedStatement updatePs = conn.prepareStatement("UPDATE rooms SET status = 'OCCUPIED' WHERE id = ?")) {
                                                    updatePs.setLong(1, roomId);
                                                    updatePs.executeUpdate();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (canProceed) {
                                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = 'CHECKED_IN', updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                                    ps.setLong(1, bookingId);
                                    ps.executeUpdate();
                                }
                                conn.commit();
                                request.getSession().setAttribute("toastMessage", "Check-in thành công.");
                                request.getSession().setAttribute("toastType", "toast-success");
                            } else {
                                conn.rollback();
                                request.getSession().setAttribute("error", errorMessage);
                            }
                        } catch (Exception e) {
                            conn.rollback();
                            throw e;
                        }
                    }
                } else if ("REQUEST_CHECKOUT".equals(action) || "START_CHECKOUT".equals(action)) {
                    // Start checkout flow: set booking to CHECKOUT_PENDING, room to INSPECTION, and create CHECKOUT_INSPECTION task
                    try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            // Update booking status
                            try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = 'CHECKOUT_PENDING', updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                                ps.setLong(1, bookingId);
                                ps.executeUpdate();
                            }

                            // Update rooms and create inspection tasks
                            String sqlRooms = "SELECT br.id as br_id, br.room_id, r.floor_number FROM booking_rooms br JOIN rooms r ON br.room_id = r.id WHERE br.booking_id = ?";
                            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlRooms)) {
                                ps.setLong(1, bookingId);
                                try (java.sql.ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        long brId = rs.getLong("br_id");
                                        long roomId = rs.getLong("room_id");
                                        int floor = rs.getInt("floor_number");
                                        Long assignedHkId = getHousekeeperForFloor(conn, floor);

                                        try (java.sql.PreparedStatement updatePs = conn.prepareStatement("UPDATE rooms SET status = 'INSPECTION' WHERE id = ?")) {
                                            updatePs.setLong(1, roomId);
                                            updatePs.executeUpdate();
                                        }

                                        long taskId = 0;
                                        String checkTaskSql = "SELECT id FROM housekeeping_tasks WHERE booking_room_id = ? AND task_type = 'CHECKOUT_INSPECTION' AND status <> 'CANCELLED'";
                                        try (java.sql.PreparedStatement checkPs = conn.prepareStatement(checkTaskSql)) {
                                            checkPs.setLong(1, brId);
                                            try (java.sql.ResultSet taskRs = checkPs.executeQuery()) {
                                                if (taskRs.next()) {
                                                    taskId = taskRs.getLong("id");
                                                }
                                            }
                                        }

                                        if (taskId == 0) {
                                            String insertTaskSql = "INSERT INTO housekeeping_tasks (room_id, booking_room_id, assigned_to, task_type, priority, status, note, created_at) VALUES (?, ?, ?, 'CHECKOUT_INSPECTION', 'HIGH', 'PENDING', 'Checkout room inspection', CURRENT_TIMESTAMP)";
                                            try (java.sql.PreparedStatement insertTaskPs = conn.prepareStatement(insertTaskSql)) {
                                                insertTaskPs.setLong(1, roomId);
                                                insertTaskPs.setLong(2, brId);
                                                if (assignedHkId != null) {
                                                    insertTaskPs.setLong(3, assignedHkId);
                                                } else {
                                                    insertTaskPs.setNull(3, java.sql.Types.BIGINT);
                                                }
                                                insertTaskPs.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            }

                            conn.commit();
                            request.getSession().setAttribute("toastMessage", "Đã gửi yêu cầu kiểm tra phòng sang Nhân viên dọn dẹp. Phòng đang ở trạng thái Kiểm tra (Inspection).");
                            request.getSession().setAttribute("toastType", "toast-success");
                        } catch (Exception e) {
                            conn.rollback();
                            throw e;
                        }
                    }
                } else if ("COMPLETE_CHECKOUT".equals(action) || "CHECK_OUT".equals(action)) {
                    // Complete checkout flow: set booking to CHECKED_OUT, rooms to CLEANING, and create CLEANING task
                    model.User currentUser = (model.User) request.getSession().getAttribute("currentUser");
                    Long staffId = currentUser != null ? currentUser.getId() : null;
                    String surchargeStr = request.getParameter("surcharge");
                    java.math.BigDecimal surcharge = java.math.BigDecimal.ZERO;
                    if (surchargeStr != null && !surchargeStr.isBlank()) {
                        try {
                            surcharge = new java.math.BigDecimal(surchargeStr.trim());
                        } catch (Exception ignored) {}
                    }
                    String paymentMethod = request.getParameter("paymentMethod");
                    if (paymentMethod == null || paymentMethod.isBlank()) paymentMethod = "CASH";
                    if ("TRANSFER".equalsIgnoreCase(paymentMethod)) paymentMethod = "BANK_TRANSFER";
                    if ("CARD".equalsIgnoreCase(paymentMethod)) paymentMethod = "CREDIT_CARD";

                    try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            // Validate that all inspection tasks are completed
                            String checkIncompleteSql = """
                                SELECT 1
                                FROM booking_rooms br
                                LEFT JOIN housekeeping_tasks ht ON ht.booking_room_id = br.id AND ht.task_type = 'CHECKOUT_INSPECTION' AND ht.status <> 'CANCELLED'
                                LEFT JOIN room_inspections ri ON ri.housekeeping_task_id = ht.id OR ri.booking_room_id = br.id
                                WHERE br.booking_id = ?
                                  AND (ht.id IS NULL OR ht.status <> 'COMPLETED' OR ri.id IS NULL OR ri.status = 'PENDING')
                                LIMIT 1
                            """;
                            boolean hasIncomplete = false;
                            try (java.sql.PreparedStatement chkPs = conn.prepareStatement(checkIncompleteSql)) {
                                chkPs.setLong(1, bookingId);
                                try (java.sql.ResultSet chkRs = chkPs.executeQuery()) {
                                    if (chkRs.next()) {
                                        hasIncomplete = true;
                                    }
                                }
                            }
                            if (hasIncomplete) {
                                conn.rollback();
                                request.getSession().setAttribute("error", "Không thể hoàn tất Check-out! Nhân viên dọn dẹp chưa hoàn thành kiểm tra phòng. Vui lòng chờ nhân viên kiểm tra xong.");
                                String redirect = request.getParameter("redirect");
                                response.sendRedirect(request.getContextPath() + (redirect != null && !redirect.isBlank() ? redirect : "/reception/check-out?bookingId=" + bookingId));
                                return;
                            }

                            // Update booking status to CHECKED_OUT and record check out datetime
                            try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = 'CHECKED_OUT', check_out_datetime = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                                ps.setLong(1, bookingId);
                                ps.executeUpdate();
                            }

                            // Update rooms and create cleaning tasks
                            String sqlRooms = "SELECT br.id as br_id, br.room_id, r.floor_number FROM booking_rooms br JOIN rooms r ON br.room_id = r.id WHERE br.booking_id = ?";
                            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlRooms)) {
                                ps.setLong(1, bookingId);
                                try (java.sql.ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        long brId = rs.getLong("br_id");
                                        long roomId = rs.getLong("room_id");
                                        int floor = rs.getInt("floor_number");
                                        Long assignedHkId = getHousekeeperForFloor(conn, floor);

                                        boolean hasDamage = false;
                                        try (java.sql.PreparedStatement dmgPs = conn.prepareStatement("SELECT 1 FROM damage_reports dr JOIN room_equipment re ON dr.room_equipment_id = re.id WHERE re.room_id = ? AND dr.booking_id = ? LIMIT 1")) {
                                            dmgPs.setLong(1, roomId);
                                            dmgPs.setLong(2, bookingId);
                                            try (java.sql.ResultSet dmgRs = dmgPs.executeQuery()) {
                                                if (dmgRs.next()) hasDamage = true;
                                            }
                                        }

                                        String nextStatus = hasDamage ? "NOT_READY" : "CLEANING";
                                        try (java.sql.PreparedStatement updatePs = conn.prepareStatement("UPDATE rooms SET status = ? WHERE id = ?")) {
                                            updatePs.setString(1, nextStatus);
                                            updatePs.setLong(2, roomId);
                                            updatePs.executeUpdate();
                                        }

                                        // Retrieve the inspection checklist & note recorded by housekeeping
                                        String cleanNote = null;
                                        try (java.sql.PreparedStatement notePs = conn.prepareStatement(
                                                "SELECT note FROM room_inspections WHERE booking_room_id = ? ORDER BY id DESC LIMIT 1")) {
                                            notePs.setLong(1, brId);
                                            try (java.sql.ResultSet noteRs = notePs.executeQuery()) {
                                                if (noteRs.next()) {
                                                    cleanNote = noteRs.getString("note");
                                                }
                                            }
                                        }
                                        if (cleanNote == null || cleanNote.isBlank()) {
                                            cleanNote = "[===TASKS===]\n[ ] Dọn vệ sinh tổng quát và kiểm tra lại phòng\n[===END_TASKS===]\n[===NOTE===]\nDọn phòng sau checkout";
                                        }

                                        String insertCleanTaskSql = "INSERT INTO housekeeping_tasks (room_id, booking_room_id, assigned_to, task_type, priority, status, note, created_at) VALUES (?, ?, ?, 'CLEANING', 'NORMAL', 'PENDING', ?, CURRENT_TIMESTAMP)";
                                        try (java.sql.PreparedStatement cleanTaskPs = conn.prepareStatement(insertCleanTaskSql)) {
                                            cleanTaskPs.setLong(1, roomId);
                                            cleanTaskPs.setLong(2, brId);
                                            if (assignedHkId != null) {
                                                cleanTaskPs.setLong(3, assignedHkId);
                                            } else {
                                                cleanTaskPs.setNull(3, java.sql.Types.BIGINT);
                                            }
                                            cleanTaskPs.setString(4, cleanNote);
                                            cleanTaskPs.executeUpdate();
                                        }
                                    }
                                }
                            }

                            // Calculate damage compensation sum
                            java.math.BigDecimal damageAmount = java.math.BigDecimal.ZERO;
                            try (java.sql.PreparedStatement dmgSumPs = conn.prepareStatement("SELECT COALESCE(SUM(compensation_amount), 0) FROM damage_reports WHERE booking_id = ? AND charge_status != 'WAIVED'")) {
                                dmgSumPs.setLong(1, bookingId);
                                try (java.sql.ResultSet dmgSumRs = dmgSumPs.executeQuery()) {
                                    if (dmgSumRs.next()) damageAmount = dmgSumRs.getBigDecimal(1);
                                }
                            }

                            // Update charge_status to PAID
                            try (java.sql.PreparedStatement updateDmgPs = conn.prepareStatement("UPDATE damage_reports SET charge_status = 'PAID', updated_at = CURRENT_TIMESTAMP WHERE booking_id = ? AND charge_status = 'PENDING'")) {
                                updateDmgPs.setLong(1, bookingId);
                                updateDmgPs.executeUpdate();
                            }

                            java.math.BigDecimal totalExtra = surcharge.add(damageAmount);

                            // Record extra payment if applicable
                            if (totalExtra.compareTo(java.math.BigDecimal.ZERO) > 0) {
                                String insertPayment = "INSERT INTO payments (booking_id, amount, payment_method, payment_type, status, processed_by, paid_at, created_at) VALUES (?, ?, ?, 'FINAL_PAYMENT', 'SUCCESS', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                                try (java.sql.PreparedStatement payPs = conn.prepareStatement(insertPayment)) {
                                    payPs.setLong(1, bookingId);
                                    payPs.setBigDecimal(2, totalExtra);
                                    payPs.setString(3, paymentMethod);
                                    if (staffId != null) payPs.setLong(4, staffId); else payPs.setNull(4, java.sql.Types.BIGINT);
                                    payPs.executeUpdate();
                                }
                            }

                            conn.commit();
                            request.getSession().setAttribute("toastMessage", "Check-out hoàn tất thành công. Phòng đã chuyển sang trạng thái chờ dọn dẹp (Cleaning).");
                            request.getSession().setAttribute("toastType", "toast-success");
                        } catch (Exception e) {
                            conn.rollback();
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("error", "Lỗi xử lý hệ thống.");
            }
        }
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + redirect);
        } else {
            response.sendRedirect(request.getContextPath()
                    + (managerView ? "/manager/bookings" : "/reception/bookings"));
        }
    }

    private Long getHousekeeperForFloor(java.sql.Connection conn, int floor) {
        String targetEmail = (floor >= 3) ? "housekeeping2@hms.com" : "housekeeping1@hms.com";
        try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT id FROM accounts WHERE email = ? AND status = 'ACTIVE' LIMIT 1")) {
            ps.setString(1, targetEmail);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (Exception ignored) {}

        try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT a.id FROM accounts a JOIN roles r ON a.role_id = r.id WHERE r.name = 'HOUSEKEEPING' AND a.status = 'ACTIVE' ORDER BY a.id ASC")) {
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                java.util.List<Long> ids = new java.util.ArrayList<>();
                while (rs.next()) ids.add(rs.getLong("id"));
                if (!ids.isEmpty()) {
                    if (floor >= 3 && ids.size() > 1) {
                        return ids.get(1);
                    }
                    return ids.get(0);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}

