package controller.page;

import dao.RoomTypeDao;
import model.RoomType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "WalkInServlet", urlPatterns = {"/reception/walk-in"})
public class WalkInServlet extends HttpServlet {

    private RoomTypeDao roomTypeDao;

    @Override
    public void init() throws ServletException {
        roomTypeDao = new RoomTypeDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<RoomType> roomTypes = roomTypeDao.findAll();
        request.setAttribute("roomTypes", roomTypes);
        
        request.getRequestDispatcher("/WEB-INF/views/reception/walk-in-booking.jsp").forward(request, response);
    }
}
