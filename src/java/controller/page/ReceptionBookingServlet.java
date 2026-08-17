package controller.page;

import dao.BookingDao;
import model.Booking;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ReceptionBookingServlet", urlPatterns = {"/reception/bookings"})
public class ReceptionBookingServlet extends HttpServlet {

    private BookingDao bookingDao;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        if (status == null || status.isEmpty()) {
            status = "ALL";
        }
        
        String dateType = request.getParameter("dateType");
        
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
        
        String bookingSource = request.getParameter("bookingSource");
        String paymentStatus = request.getParameter("paymentStatus");

        List<Booking> bookings = bookingDao.searchBookingsForReception(keyword, status, dateType, fromDate, toDate, bookingSource, paymentStatus);
        
        request.setAttribute("bookings", bookings);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentStatus", status);
        request.setAttribute("dateType", dateType);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);
        request.setAttribute("bookingSource", bookingSource);
        request.setAttribute("paymentStatus", paymentStatus);
        
        request.getRequestDispatcher("/WEB-INF/views/reception/booking-list.jsp").forward(request, response);
    }
}
