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
    private dao.RoomTypeAmenityDao roomTypeAmenityDao = new dao.RoomTypeAmenityDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                long id = Long.parseLong(idStr);
                java.util.Optional<model.RoomType> optRt = roomTypeDao.findById(id);
                if (optRt.isPresent() && "ACTIVE".equals(optRt.get().getStatus())) {
                    model.RoomType rt = optRt.get();
                    
                    // Check availability if dates are provided
                    String checkInStr = request.getParameter("checkIn");
                    String checkOutStr = request.getParameter("checkOut");
                    boolean isAvailable = true;
                    if (checkInStr != null && checkOutStr != null && !checkInStr.isBlank() && !checkOutStr.isBlank()) {
                        try {
                            java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInStr);
                            java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutStr);
                            java.util.List<model.RoomType> availableRooms = roomTypeDao.findAvailableRoomTypes(
                                    checkIn, checkOut, 1, 1, null, null, null, id);
                            isAvailable = !availableRooms.isEmpty() && availableRooms.get(0).getAvailableQuantity() > 0;
                            rt.setAvailableQuantity(availableRooms.isEmpty() ? 0 : availableRooms.get(0).getAvailableQuantity());
                        } catch (Exception e) {
                            // ignore parse errors
                        }
                    }
                    
                    request.setAttribute("room", rt);
                    request.setAttribute("roomTypeAmenities", roomTypeAmenityDao.findAmenitiesByRoomTypeId(rt.getId()));
                    request.setAttribute("isAvailable", isAvailable);
                    request.getRequestDispatcher("/WEB-INF/views/public/room-detail.jsp").forward(request, response);
                    return;
                }
            } catch (NumberFormatException e) {
                // Invalid ID
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy phòng hoặc phòng đã ngưng hoạt động");
    }
}

