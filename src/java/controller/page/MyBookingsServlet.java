package controller.page;

import dao.BookingDao;
import model.Booking;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "MyBookingsServlet", urlPatterns = {"/my-bookings"})
public class MyBookingsServlet extends HttpServlet {

    private BookingDao bookingDao;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        
        if (user == null) {
            session.setAttribute("redirectUrl", request.getContextPath() + "/my-bookings");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String status = request.getParameter("status");
        if (status == null || status.isEmpty()) {
            status = "ALL";
        }
        String bookingCode = request.getParameter("bookingCode");
        
        java.sql.Date fromDate = null;
        try {
            if (request.getParameter("fromDate") != null && !request.getParameter("fromDate").isEmpty()) {
                fromDate = java.sql.Date.valueOf(request.getParameter("fromDate"));
            }
        } catch (IllegalArgumentException ignored) {}
        
        java.sql.Date toDate = null;
        try {
            if (request.getParameter("toDate") != null && !request.getParameter("toDate").isEmpty()) {
                toDate = java.sql.Date.valueOf(request.getParameter("toDate"));
            }
        } catch (IllegalArgumentException ignored) {}
        
        List<Booking> bookings = bookingDao.findByCustomerIdWithFilters(user.getId(), status, bookingCode, fromDate, toDate);
        request.setAttribute("bookings", bookings);
        request.setAttribute("currentStatus", status);
        request.setAttribute("bookingCode", bookingCode);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);
        
        request.getRequestDispatcher("/WEB-INF/views/public/my-bookings.jsp").forward(request, response);
    }
}
