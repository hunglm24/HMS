package controller.api;

import dao.RoomDao;
import model.Room;
import util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "RoomAssignmentApiServlet", urlPatterns = {"/api/available-rooms"})
public class RoomAssignmentApiServlet extends HttpServlet {

    private RoomDao roomDao;

    @Override
    public void init() throws ServletException {
        roomDao = new RoomDao();
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
            
            Long roomTypeId = null;
            try {
                if (request.getParameter("roomTypeId") != null && !request.getParameter("roomTypeId").isEmpty()) {
                    roomTypeId = Long.parseLong(request.getParameter("roomTypeId"));
                }
            } catch (NumberFormatException ignored) {}
            
            Integer guests = null;
            try {
                if (request.getParameter("guests") != null && !request.getParameter("guests").isEmpty()) {
                    guests = Integer.parseInt(request.getParameter("guests"));
                }
            } catch (NumberFormatException ignored) {}
            
            List<Room> availableRooms = roomDao.findAvailableRoomsForWalkIn(checkIn, checkOut, roomTypeId, guests);
            
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
