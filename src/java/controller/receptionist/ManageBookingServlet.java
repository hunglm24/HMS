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
                } else if ("REJECT".equals(action)) {
                    bookingDao.updateBookingStatus(bookingId, "CANCELLED");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/reception/bookings");
    }
}

