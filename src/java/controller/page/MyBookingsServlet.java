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
        String view = "/booking-detail".equals(request.getServletPath())
                ? "/WEB-INF/views/public/booking-detail-guest.jsp"
                : "/WEB-INF/views/public/my-bookings.jsp";
        
        if (!"/booking-detail".equals(request.getServletPath())) {
            // Fetch bookings for the logged-in user
            model.User user = (model.User) request.getSession().getAttribute("loggedInUser");
            if (user != null) {
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
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }
        
        request.getRequestDispatcher(view).forward(request, response);
    }
}
