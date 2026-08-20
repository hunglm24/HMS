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

@WebServlet(name = "ManageBookingServlet", urlPatterns = {"/reception/bookings"})
public class ManageBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BookingDao bookingDao = new BookingDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
            
            request.setAttribute("allCount", allCount);
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("checkInTodayCount", checkInTodayCount);
            request.setAttribute("checkedInCount", checkedInCount);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.getRequestDispatcher("/WEB-INF/views/reception/booking-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
                } else if ("CHECK_IN".equals(action) || "CHECK_OUT".equals(action)) {
                    // Cần kiểm tra trạng thái phòng trước khi Check-in
                    boolean canProceed = true;
                    String errorMessage = "";
                    
                    try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            if ("CHECK_IN".equals(action)) {
                                // Check if the booking's check_in_date is in the future
                                try (java.sql.PreparedStatement datePs = conn.prepareStatement("SELECT check_in_date FROM bookings WHERE id = ?")) {
                                    datePs.setLong(1, bookingId);
                                    try (java.sql.ResultSet dateRs = datePs.executeQuery()) {
                                        if (dateRs.next()) {
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
                            }
                            
                            if (canProceed) {
                                String sqlRooms = "SELECT r.id, r.status FROM booking_rooms br JOIN rooms r ON br.room_id = r.id WHERE br.booking_id = ?";
                                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlRooms)) {
                                    ps.setLong(1, bookingId);
                                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                                        while (rs.next()) {
                                            long roomId = rs.getLong("id");
                                            String physicalStatus = rs.getString("status");
                                            
                                            if ("CHECK_IN".equals(action)) {
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
                                            } else if ("CHECK_OUT".equals(action)) {
                                                // Update room physical status to cleaning queue after checkout.
                                                try (java.sql.PreparedStatement updatePs = conn.prepareStatement("UPDATE rooms SET status = 'CLEANING' WHERE id = ?")) {
                                                    updatePs.setLong(1, roomId);
                                                    updatePs.executeUpdate();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (canProceed) {
                                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                                    ps.setString(1, "CHECK_IN".equals(action) ? "CHECKED_IN" : "CHECKED_OUT");
                                    ps.setLong(2, bookingId);
                                    ps.executeUpdate();
                                }
                                conn.commit();
                                request.getSession().setAttribute("toastMessage", "CHECK_IN".equals(action) ? "Check-in thành công." : "Check-out thành công. Phòng đã chuyển sang trạng thái chờ dọn dẹp.");
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
            response.sendRedirect(request.getContextPath() + "/reception/bookings");
        }
    }
}

