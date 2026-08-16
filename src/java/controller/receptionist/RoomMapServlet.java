package controller.receptionist;

import dao.RoomDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RoomMapServlet", urlPatterns = {"/reception/room-map"})
public class RoomMapServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomDao roomDao;

    @Override
    public void init() throws ServletException {
        // Create the DAO once when the servlet is initialized.
        roomDao = new RoomDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Load room data with room type and booking information.
        List<Room> allRooms = roomDao.findAllWithRoomTypeNameAndBookingInfo();

        // Restore flash messages from the session if available.
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object flashMessage = session.getAttribute("flashMessage");
            Object flashType = session.getAttribute("flashType");
            if (flashMessage != null) {
                request.setAttribute("flashMessage", flashMessage);
                request.setAttribute("flashType", flashType == null ? "success" : flashType);
                session.removeAttribute("flashMessage");
                session.removeAttribute("flashType");
            }
        }

        // Read filter parameters from the request.
        String search = request.getParameter("search");
        String status = request.getParameter("status");
        Integer floor = parseIntSafely(request.getParameter("floor"));
        Integer roomTypeId = parseIntSafely(request.getParameter("roomTypeId"));

        // Normalize values for case-insensitive comparison.
        if (search != null) search = search.trim().toLowerCase();
        if (status != null) status = status.trim().toUpperCase();

        // Build summary counters and group rooms by floor in a single pass.
        long available = 0, occupied = 0, cleaning = 0, maintenance = 0;
        Map<Integer, List<Room>> roomsByFloor = new LinkedHashMap<>();

        for (Room room : allRooms) {
            // Count room statuses for the dashboard badges.
            String roomStatus = room.getStatus() != null ? room.getStatus().toUpperCase() : "";
            switch (roomStatus) {
                case "AVAILABLE":
                    available++;
                    break;
                case "OCCUPIED":
                    occupied++;
                    break;
                case "CLEANING":
                    cleaning++;
                    break;
                case "MAINTENANCE":
                    maintenance++;
                    break;
                default:
                    break;
            }

            // Apply filters from the UI.
            if (status != null && !status.isEmpty() && !"ALL".equals(status) && !status.equals(roomStatus)) {
                continue;
            }

            if (floor != null && (room.getFloorNumber() == null || !floor.equals(room.getFloorNumber()))) {
                continue;
            }

            if (roomTypeId != null && roomTypeId.intValue() != room.getRoomTypeId()) {
                continue;
            }

            if (search != null && !search.isEmpty()) {
                boolean matchRoomNum = room.getRoomNumber() != null && room.getRoomNumber().toLowerCase().contains(search);
                boolean matchTypeName = room.getRoomTypeName() != null && room.getRoomTypeName().toLowerCase().contains(search);

                if (!matchRoomNum && !matchTypeName) {
                    continue;
                }
            }

            // Group the room by floor if it passes all filters.
            int floorNum = room.getFloorNumber() != null ? room.getFloorNumber() : 0;
            roomsByFloor.computeIfAbsent(floorNum, key -> new ArrayList<>()).add(room);
        }

        // Expose data to the JSP.
        request.setAttribute("roomsByFloor", roomsByFloor);
        request.setAttribute("availableCount", available);
        request.setAttribute("occupiedCount", occupied);
        request.setAttribute("cleaningCount", cleaning);
        request.setAttribute("maintenanceCount", maintenance);
        request.setAttribute("totalCount", allRooms.size());
        request.setAttribute("currentSearch", request.getParameter("search"));
        request.setAttribute("currentStatus", (status == null || status.isEmpty()) ? "ALL" : status);
        request.setAttribute("currentFloor", request.getParameter("floor"));
        request.setAttribute("currentRoomTypeId", request.getParameter("roomTypeId"));

        // Forward to the room map view.
        request.getRequestDispatcher("/WEB-INF/views/reception/room-map.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private Integer parseIntSafely(String value) {
        // Return null when the value is missing or invalid.
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
