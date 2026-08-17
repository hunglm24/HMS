package controller.page;

import dao.RoomDao;
import dao.RoomTypeDao;
import model.Room;
import model.RoomType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "RoomMapServlet", urlPatterns = {"/reception/room-map"})
public class RoomMapServlet extends HttpServlet {

    private RoomDao roomDao;
    private RoomTypeDao roomTypeDao;

    @Override
    public void init() throws ServletException {
        roomDao = new RoomDao();
        roomTypeDao = new RoomTypeDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Room> allRooms = roomDao.findAllWithRoomTypeName();
        List<RoomType> roomTypes = roomTypeDao.findAll();
        
        // Handle Filtering
        String floorParam = request.getParameter("floor");
        String roomTypeParam = request.getParameter("roomType");
        String statusParam = request.getParameter("status");
        
        List<Room> filteredRooms = allRooms.stream().filter(r -> {
            boolean match = true;
            if (floorParam != null && !floorParam.isEmpty()) {
                if (r.getFloorNumber() != null) {
                    match = match && r.getFloorNumber() == Integer.parseInt(floorParam);
                } else {
                    match = false;
                }
            }
            if (roomTypeParam != null && !roomTypeParam.isEmpty()) {
                match = match && r.getRoomTypeId() == Long.parseLong(roomTypeParam);
            }
            if (statusParam != null && !statusParam.isEmpty() && !statusParam.equals("ALL")) {
                match = match && statusParam.equals(r.getStatus());
            }
            return match;
        }).collect(Collectors.toList());
        
        // Calculate stats
        long available = allRooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long occupied = allRooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();
        long cleaning = allRooms.stream().filter(r -> "CLEANING".equals(r.getStatus())).count();
        long maintenance = allRooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();
        
        request.setAttribute("rooms", filteredRooms);
        request.setAttribute("roomTypes", roomTypes);
        
        request.setAttribute("statAvailable", available);
        request.setAttribute("statOccupied", occupied);
        request.setAttribute("statCleaning", cleaning);
        request.setAttribute("statMaintenance", maintenance);
        request.setAttribute("statTotal", allRooms.size());
        
        request.setAttribute("currentFloor", floorParam);
        request.setAttribute("currentRoomType", roomTypeParam);
        request.setAttribute("currentStatus", statusParam);

        request.getRequestDispatcher("/WEB-INF/views/reception/room-map.jsp").forward(request, response);
    }
}