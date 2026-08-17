package controller.api;

import dao.SearchDao;
import model.RoomType;
import util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AvailabilityApiServlet", urlPatterns = {"/api/availability"})
public class AvailabilityApiServlet extends HttpServlet {

    private SearchDao searchDao;

    @Override
    public void init() throws ServletException {
        searchDao = new SearchDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String checkInStr = request.getParameter("checkIn");
        String checkOutStr = request.getParameter("checkOut");
        
        if (checkInStr == null || checkInStr.isEmpty() || checkOutStr == null || checkOutStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Missing checkIn or checkOut dates\"}");
            return;
        }

        try {
            Date checkIn = Date.valueOf(checkInStr);
            Date checkOut = Date.valueOf(checkOutStr);
            
            int totalGuests = 2; // Default
            try {
                if (request.getParameter("guests") != null) {
                    totalGuests = Integer.parseInt(request.getParameter("guests"));
                }
            } catch (NumberFormatException ignored) {}
            
            int roomsCount = 1; // Default
            try {
                if (request.getParameter("rooms") != null) {
                    roomsCount = Integer.parseInt(request.getParameter("rooms"));
                }
            } catch (NumberFormatException ignored) {}
            
            BigDecimal minPrice = null;
            try {
                if (request.getParameter("minPrice") != null && !request.getParameter("minPrice").isEmpty()) {
                    minPrice = new BigDecimal(request.getParameter("minPrice"));
                }
            } catch (Exception ignored) {}
            
            BigDecimal maxPrice = null;
            try {
                if (request.getParameter("maxPrice") != null && !request.getParameter("maxPrice").isEmpty()) {
                    maxPrice = new BigDecimal(request.getParameter("maxPrice"));
                }
            } catch (Exception ignored) {}
            
            List<Long> roomTypeIds = new ArrayList<>();
            String roomTypeParam = request.getParameter("roomType"); // can be comma-separated in API
            if (roomTypeParam != null && !roomTypeParam.isEmpty()) {
                String[] rts = roomTypeParam.split(",");
                for (String rt : rts) {
                    try {
                        roomTypeIds.add(Long.parseLong(rt.trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
            
            String sortBy = request.getParameter("sortBy");
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "PRICE_ASC";
            }
            
            List<RoomType> availableRooms = searchDao.findAvailableRoomTypes(
                    checkIn, checkOut, totalGuests, roomsCount, minPrice, maxPrice, roomTypeIds, sortBy);
            
            out.print(JsonUtil.toJson(availableRooms));
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid date format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"An error occurred\"}");
        }
    }
}
