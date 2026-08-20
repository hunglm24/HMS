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

    private final dao.RoomTypeDao roomTypeDao = new dao.RoomTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<model.RoomType> allRoomTypes = roomTypeDao.findActive();
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

            if (hasSearchInput(checkInStr, checkOutStr, guestsStr)) {
                try {
                    LocalDate checkIn = LocalDate.parse(checkInStr);
                    LocalDate checkOut = LocalDate.parse(checkOutStr);
                    int guests = Integer.parseInt(guestsStr);
                    int numRooms = numRoomsStr != null && !numRoomsStr.isBlank() ? Integer.parseInt(numRoomsStr) : 1;
                    Double minPrice = minPriceStr != null && !minPriceStr.isBlank() ? Double.parseDouble(minPriceStr) : null;
                    Double maxPrice = maxPriceStr != null && !maxPriceStr.isBlank() ? Double.parseDouble(maxPriceStr) : null;
                    Long roomTypeId = roomTypeIdStr != null && !roomTypeIdStr.isBlank() ? Long.parseLong(roomTypeIdStr) : null;
                    
                    String pageStr = request.getParameter("page");
                    int page = pageStr != null && !pageStr.isBlank() ? Integer.parseInt(pageStr) : 1;
                    int limit = 10;
                    int offset = (page - 1) * limit;

                    List<model.RoomType> availableRooms = roomTypeDao.findAvailableRoomTypes(
                            checkIn, checkOut, guests, numRooms, minPrice, maxPrice, sort, roomTypeId, limit, offset);
                    
                    int totalRecords = roomTypeDao.countAvailableRoomTypes(checkIn, checkOut, guests, numRooms, minPrice, maxPrice, roomTypeId);
                    int totalPages = (int) Math.ceil((double) totalRecords / limit);
                    
                    request.setAttribute("availableRooms", availableRooms);
                    request.setAttribute("currentPage", page);
                    request.setAttribute("totalPages", totalPages);
                } catch (RuntimeException ex) {
                    request.setAttribute("dateError", "Dữ liệu tìm kiếm không hợp lệ. Vui lòng kiểm tra ngày, số khách, số phòng và giá.");
                }
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/public/search-results.jsp").forward(request, response);
    }

    private boolean hasSearchInput(String checkIn, String checkOut, String guests) {
        return checkIn != null && checkOut != null && guests != null
                && !checkIn.isBlank() && !checkOut.isBlank() && !guests.isBlank();
    }

    private String validateDates(HttpServletRequest request) {
        String checkIn = request.getParameter("checkIn");
        String checkOut = request.getParameter("checkOut");
        String guestsStr = request.getParameter("guests");
        String numRoomsStr = request.getParameter("numRooms");
        
        if (checkIn == null && checkOut == null && guestsStr == null) {
            return null; // initial load
        }
        
        if (checkIn == null || checkOut == null || checkIn.isBlank() || checkOut.isBlank()) {
            request.setAttribute("dateError", "Vui lòng nhập đầy đủ ngày nhận và trả phòng.");
            return "error";
        }
        
        try {
            if (guestsStr != null && !guestsStr.isBlank()) {
                if (Integer.parseInt(guestsStr) <= 0) {
                    request.setAttribute("dateError", "Số khách phải lớn hơn 0.");
                    return "error";
                }
            }
            if (numRoomsStr != null && !numRoomsStr.isBlank()) {
                if (Integer.parseInt(numRoomsStr) <= 0) {
                    request.setAttribute("dateError", "Số phòng phải lớn hơn 0.");
                    return "error";
                }
            }
        } catch (NumberFormatException e) {
            request.setAttribute("dateError", "Số lượng khách hoặc phòng không hợp lệ.");
            return "error";
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
