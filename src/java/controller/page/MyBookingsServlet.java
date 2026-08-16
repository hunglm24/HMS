package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/my-bookings", "/booking-detail"})
public class MyBookingsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = "/booking-detail".equals(request.getServletPath())
                ? "/WEB-INF/views/public/booking-detail-guest.jsp"
                : "/WEB-INF/views/public/my-bookings.jsp";
        request.getRequestDispatcher(view).forward(request, response);
    }
}
