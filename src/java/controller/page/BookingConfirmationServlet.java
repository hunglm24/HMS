package controller.page;

import dao.BookingDao;
import model.Booking;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "BookingConfirmationServlet", urlPatterns = {"/booking-confirmation"})
public class BookingConfirmationServlet extends HttpServlet {

    private BookingDao bookingDao;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                long id = Long.parseLong(idStr);
                Optional<Booking> bookingOpt = bookingDao.findById(id);
                if (bookingOpt.isPresent()) {
                    request.setAttribute("booking", bookingOpt.get());
                    request.getRequestDispatcher("/WEB-INF/views/public/booking-confirmation.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        response.sendRedirect(request.getContextPath() + "/search");
    }
}
