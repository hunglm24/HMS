package controller.page;

import dao.RoomTypeDao;
import model.RoomType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "RoomDetailServlet", urlPatterns = {"/room-detail"})
public class RoomDetailServlet extends HttpServlet {

    private RoomTypeDao roomTypeDao;

    @Override
    public void init() throws ServletException {
        roomTypeDao = new RoomTypeDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                long id = Long.parseLong(idStr);
                Optional<RoomType> rtOpt = roomTypeDao.findById(id);
                if (rtOpt.isPresent()) {
                    request.setAttribute("roomType", rtOpt.get());
                    // Pass along search dates if they exist
                    request.setAttribute("checkIn", request.getParameter("checkIn"));
                    request.setAttribute("checkOut", request.getParameter("checkOut"));
                    
                    request.getRequestDispatcher("/WEB-INF/views/public/room-detail.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        response.sendRedirect(request.getContextPath() + "/search");
    }
}
