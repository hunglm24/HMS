package controller.user;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "CancelBookingServlet", urlPatterns = {"/user/cancel-booking"})
public class CancelBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xá»­ lÃ½ hiá»ƒn thá»‹ trang JSP
        request.getRequestDispatcher("/WEB-INF/views/public/booking-detail-guest.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xá»­ lÃ½ logic nghiá»‡p vá»¥ vÃ  chuyá»ƒn hÆ°á»›ng/forward
    }
}

