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
        
        List<Booking> bookings = bookingDao.findByCustomerId(user.getId());
        request.setAttribute("bookings", bookings);
        
        request.getRequestDispatcher("/WEB-INF/views/public/my-bookings.jsp").forward(request, response);
    }
}
