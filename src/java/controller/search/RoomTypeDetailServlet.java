package controller.search;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "RoomTypeDetailServlet", urlPatterns = {"/room-type-detail", "/room-detail"})
public class RoomTypeDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private dao.RoomTypeDao roomTypeDao = new dao.RoomTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                long id = Long.parseLong(idStr);
                roomTypeDao.findById(id).ifPresent(rt -> request.setAttribute("room", rt));
            } catch (NumberFormatException e) {
                // Invalid ID
            }
        }
        request.getRequestDispatcher("/WEB-INF/views/public/room-detail.jsp").forward(request, response);
    }
}

