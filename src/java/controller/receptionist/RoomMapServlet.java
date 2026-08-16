package controller.receptionist;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "RoomMapServlet", urlPatterns = {"/receptionist/room-map"})
public class RoomMapServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xá»­ lÃ½ hiá»ƒn thá»‹ trang JSP
        request.getRequestDispatcher("/WEB-INF/jsp/receptionist/room-map.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xá»­ lÃ½ logic nghiá»‡p vá»¥ vÃ  chuyá»ƒn hÆ°á»›ng/forward
    }
}

