package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.List;
import model.Booking;
import dao.BookingDao;

@WebServlet(urlPatterns = {"/my-bookings", "/booking-detail"})
public class MyBookingsServlet extends HttpServlet {
    private BookingDao bookingDao = new BookingDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        model.User user = (model.User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("/booking-detail".equals(request.getServletPath())) {
            String bookingIdStr = request.getParameter("id");
            if (bookingIdStr != null && !bookingIdStr.isBlank()) {
                try {
                    long bookingId = Long.parseLong(bookingIdStr);
                    Booking booking = bookingDao.findById(bookingId).orElse(null);
                    
                    if (booking == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thông tin đặt phòng.");
                        return;
                    }
                    
                    if (booking.getCustomerId() == null || booking.getCustomerId() != user.getId()) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập thông tin đặt phòng này.");
                        return;
                    }
                    
                    request.setAttribute("booking", booking);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
            }
            request.getRequestDispatcher("/WEB-INF/views/public/booking-detail-guest.jsp").forward(request, response);
        } else {
            // Fetch list
            try {
                String bookingCode = request.getParameter("bookingCode");
                String status = request.getParameter("status");
                String fromDate = request.getParameter("fromDate");
                String toDate = request.getParameter("toDate");

                List<Booking> bookings = bookingDao.findBookingsByCustomerId(user.getId(), bookingCode, status, fromDate, toDate);
                request.setAttribute("bookings", bookings);
            } catch (Exception e) {
                e.printStackTrace();
            }
            request.getRequestDispatcher("/WEB-INF/views/public/my-bookings.jsp").forward(request, response);
        }
    }
}
