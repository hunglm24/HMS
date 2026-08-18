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

            List<CheckInBookingSummary> bookings = bookingDao.findCheckInBookings(keyword, status, null, null, null, null, 0, 100, fromDate, toDate, source);
            request.setAttribute("bookings", bookings);
            
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
                    bookingDao.updateBookingStatus(bookingId, "CANCELLED");
                    request.getSession().setAttribute("toastMessage", "Đã hủy đặt phòng.");
                    request.getSession().setAttribute("toastType", "toast-success");
                } else if ("CHECK_IN".equals(action) || "CHECK_OUT".equals(action)) {
                    // Cần kiểm tra trạng thái phòng trước khi Check-in
                    boolean canProceed = true;
                    String errorMessage = "";
                    
                    try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
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
                                            // Update room physical status to DIRTY
                                            try (java.sql.PreparedStatement updatePs = conn.prepareStatement("UPDATE rooms SET status = 'DIRTY' WHERE id = ?")) {
                                                updatePs.setLong(1, roomId);
                                                updatePs.executeUpdate();
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
        response.sendRedirect(request.getContextPath() + "/reception/bookings");
    }
}

