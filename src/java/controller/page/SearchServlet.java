package controller.page;

import dao.SearchDao;
import model.RoomType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {

    private SearchDao searchDao;

    @Override
    public void init() throws ServletException {
        searchDao = new SearchDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String checkInStr = request.getParameter("checkIn");
        String checkOutStr = request.getParameter("checkOut");
        
        if (checkInStr != null && !checkInStr.isEmpty() && checkOutStr != null && !checkOutStr.isEmpty()) {
            try {
                Date checkIn = Date.valueOf(checkInStr);
                Date checkOut = Date.valueOf(checkOutStr);
                
                List<RoomType> availableRooms = searchDao.findAvailableRoomTypes(checkIn, checkOut);
                
                request.setAttribute("availableRooms", availableRooms);
                request.setAttribute("checkIn", checkInStr);
                request.setAttribute("checkOut", checkOutStr);
                
                request.getRequestDispatcher("/WEB-INF/views/public/search-results.jsp").forward(request, response);
                return;
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", "Invalid date format");
            }
        }
        
        request.getRequestDispatcher("/WEB-INF/views/public/search.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
