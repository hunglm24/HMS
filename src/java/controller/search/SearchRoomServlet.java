package controller.search;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "SearchRoomServlet", urlPatterns = {"/search-room", "/search"})
public class SearchRoomServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private dao.RoomTypeDao roomTypeDao = new dao.RoomTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<model.RoomType> allRoomTypes = roomTypeDao.findAll();
        request.setAttribute("allRoomTypes", allRoomTypes);

        String error = validateDates(request);
        
        if (error == null) {
            String checkInStr = request.getParameter("checkIn");
            String checkOutStr = request.getParameter("checkOut");
            String guestsStr = request.getParameter("guests");
            String numRoomsStr = request.getParameter("numRooms");
            String minPriceStr = request.getParameter("minPrice");
            String maxPriceStr = request.getParameter("maxPrice");
            String sort = request.getParameter("sort");
            String roomTypeIdStr = request.getParameter("roomTypeId");
            
            if (checkInStr != null && checkOutStr != null && guestsStr != null) {
                LocalDate checkIn = LocalDate.parse(checkInStr);
                LocalDate checkOut = LocalDate.parse(checkOutStr);
                int guests = Integer.parseInt(guestsStr);
                int numRooms = numRoomsStr != null && !numRoomsStr.isBlank() ? Integer.parseInt(numRoomsStr) : 1;
                Double minPrice = minPriceStr != null && !minPriceStr.isBlank() ? Double.parseDouble(minPriceStr) : null;
                Double maxPrice = maxPriceStr != null && !maxPriceStr.isBlank() ? Double.parseDouble(maxPriceStr) : null;
                Long roomTypeId = roomTypeIdStr != null && !roomTypeIdStr.isBlank() ? Long.parseLong(roomTypeIdStr) : null;

                List<model.RoomType> availableRooms = roomTypeDao.findAvailableRoomTypes(checkIn, checkOut, guests, numRooms, minPrice, maxPrice, sort, roomTypeId);
                request.setAttribute("availableRooms", availableRooms);
            }
        }
        
        request.getRequestDispatcher("/WEB-INF/views/public/search-results.jsp").forward(request, response);
    }

    private String validateDates(HttpServletRequest request) {
        String checkIn = request.getParameter("checkIn");
        String checkOut = request.getParameter("checkOut");
        if (checkIn == null || checkOut == null || checkIn.isBlank() || checkOut.isBlank()) {
            return null; // Not searching yet
        }
        LocalDate today = LocalDate.now();
        try {
            LocalDate in = LocalDate.parse(checkIn);
            LocalDate out = LocalDate.parse(checkOut);
            if (in.isBefore(today)) {
                request.setAttribute("dateError", "Không được chọn ngày nhận phòng trong quá khứ.");
                return "error";
            } else if (out.isBefore(today)) {
                request.setAttribute("dateError", "Không được chọn ngày trả phòng trong quá khứ.");
                return "error";
            } else if (!out.isAfter(in)) {
                request.setAttribute("dateError", "Ngày trả phòng phải sau ngày nhận phòng.");
                return "error";
            }
        } catch (DateTimeParseException ex) {
            request.setAttribute("dateError", "Ngày không đúng định dạng.");
            return "error";
        }
        return null;
    }
}
