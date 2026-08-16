package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/reception/bookings", "/reception/booking-detail", "/reception/walk-in", "/reception/check-out"})
public class ReceptionBookingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String view = switch (path) {
            case "/reception/booking-detail" -> "/WEB-INF/views/reception/booking-detail.jsp";
            case "/reception/walk-in" -> "/WEB-INF/views/reception/walk-in-booking.jsp";
            case "/reception/check-out" -> "/WEB-INF/views/reception/check-out.jsp";
            default -> "/WEB-INF/views/reception/booking-list.jsp";
        };
        request.getRequestDispatcher(view).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/reception/bookings");
    }
}
