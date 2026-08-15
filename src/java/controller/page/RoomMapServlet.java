package controller.page;

import dao.RoomDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/reception/room-map"})
public class RoomMapServlet extends HttpServlet {

    private RoomDao roomDao;

    @Override
    public void init() throws ServletException {
        roomDao = new RoomDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Room> allRooms = roomDao.findAllWithRoomTypeName();
        
        // Group rooms by floor
        Map<Integer, List<Room>> roomsByFloor = new LinkedHashMap<>();
        for (Room room : allRooms) {
            Integer floor = room.getFloorNumber();
            if (floor == null) floor = 0; // default for unknown floor
            roomsByFloor.computeIfAbsent(floor, k -> new ArrayList<>()).add(room);
        }
        
        req.setAttribute("roomsByFloor", roomsByFloor);
        
        // Count statuses for summary
        long availableCount = allRooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long occupiedCount = allRooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();
        long cleaningCount = allRooms.stream().filter(r -> "CLEANING".equals(r.getStatus())).count();
        long maintenanceCount = allRooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();
        
        req.setAttribute("availableCount", availableCount);
        req.setAttribute("occupiedCount", occupiedCount);
        req.setAttribute("cleaningCount", cleaningCount);
        req.setAttribute("maintenanceCount", maintenanceCount);
        req.setAttribute("totalCount", allRooms.size());

        req.getRequestDispatcher("/WEB-INF/views/reception/room-map.jsp").forward(req, resp);
    }
}
