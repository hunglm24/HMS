package controller.page;

import dao.SearchDao;
import model.RoomType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
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
                
                int totalGuests = 2; // Default 2
                try {
                    totalGuests = Integer.parseInt(request.getParameter("guests"));
                } catch (NumberFormatException ignored) {}
                
                int roomsCount = 1; // Default 1
                try {
                    roomsCount = Integer.parseInt(request.getParameter("rooms"));
                } catch (NumberFormatException ignored) {}
                
                BigDecimal minPrice = null;
                try {
                    minPrice = new BigDecimal(request.getParameter("minPrice"));
                } catch (Exception ignored) {}
                
                BigDecimal maxPrice = null;
                try {
                    maxPrice = new BigDecimal(request.getParameter("maxPrice"));
                } catch (Exception ignored) {}
                
                List<Long> roomTypeIds = new ArrayList<>();
                String[] roomTypeParams = request.getParameterValues("roomType");
                if (roomTypeParams != null) {
                    for (String rtId : roomTypeParams) {
                        try {
                            roomTypeIds.add(Long.parseLong(rtId));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                
                String sortBy = request.getParameter("sortBy");
                if (sortBy == null || sortBy.isEmpty()) {
                    sortBy = "PRICE_ASC";
                }
                
                List<RoomType> availableRooms = searchDao.findAvailableRoomTypes(
                        checkIn, checkOut, totalGuests, roomsCount, minPrice, maxPrice, roomTypeIds, sortBy);
                
                request.setAttribute("availableRooms", availableRooms);
                request.setAttribute("checkIn", checkInStr);
                request.setAttribute("checkOut", checkOutStr);
                request.setAttribute("guests", totalGuests);
                request.setAttribute("rooms", roomsCount);
                request.setAttribute("minPrice", minPrice);
                request.setAttribute("maxPrice", maxPrice);
                request.setAttribute("selectedRoomTypes", roomTypeIds);
                request.setAttribute("sortBy", sortBy);
                
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
