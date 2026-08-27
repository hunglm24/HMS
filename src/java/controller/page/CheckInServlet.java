package controller.page;


import model.CheckInBookingSummary;
import model.RoomType;
import service.CheckInService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/reception/check-in"})
public class CheckInServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CheckInService checkInService;

    @Override
    public void init() {
        checkInService = new CheckInService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int bookingId = parseInt(request.getParameter("bookingId"));
        String keyword = request.getParameter("q");
        String bookingStatus = request.getParameter("status");
        if (bookingStatus == null) bookingStatus = "Confirmed";
        Integer roomTypeId = parseNullableInt(request.getParameter("roomTypeId"));
        String scope = request.getParameter("scope");
        if (scope == null) scope = "today";
        String sort = request.getParameter("sort");
        String direction = request.getParameter("direction");
        int page = parseInt(request.getParameter("page"));

        try {
            CheckInService.CheckInPage result = checkInService.getCheckInPage(
                    keyword, bookingStatus, roomTypeId, scope, sort, direction, page);
            List<RoomType> roomTypes = checkInService.getRoomTypes();
            request.setAttribute("result", result);
            request.setAttribute("roomTypes", roomTypes);
            CheckInBookingSummary selectedBooking = findSelectedBooking(bookingId).orElse(null);
            request.setAttribute("selectedBooking", selectedBooking);
            
            if (selectedBooking != null) {
                java.util.List<java.util.Map<String, Object>> assignments = new java.util.ArrayList<>();
                try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                                // in ra tât ca cac phong
                                java.util.Map<Long, java.util.List<model.Room>> availableRoomsByTypeId = new java.util.HashMap<>();
                                
                                // Gọi chung hàm từ roomdao
                                dao.RoomDao roomDao = new dao.RoomDao();
                                java.time.LocalDate checkIn = new java.sql.Date(selectedBooking.getCheckInDate().getTime()).toLocalDate();
                                java.time.LocalDate checkOut = new java.sql.Date(selectedBooking.getCheckOutDate().getTime()).toLocalDate();
                                // 
                                java.util.List<model.Room> allAvailRooms = roomDao.findAvailablePhysicalRooms(checkIn, checkOut, null, (long)bookingId);
                                
                                for(model.Room r : allAvailRooms) {
                                    availableRoomsByTypeId.computeIfAbsent(r.getRoomTypeId(), k -> new java.util.ArrayList<>()).add(r);
                                }
                                request.setAttribute("availableRoomsByTypeId", availableRoomsByTypeId);

                                String sqlBR = "SELECT br.id as br_id, br.room_id, r.room_number, r.room_type_id, rt.name as room_type_name " +
                                               "FROM booking_rooms br " +
                                               "JOIN rooms r ON br.room_id = r.id " +
                                               "JOIN room_types rt ON r.room_type_id = rt.id " +
                                               "WHERE br.booking_id = ?";
                                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlBR)) {
                                    ps.setInt(1, bookingId);
                                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                                        while (rs.next()) {
                                            java.util.Map<String, Object> map = new java.util.HashMap<>();
                                            long brId = rs.getLong("br_id");
                                            long currentRoomId = rs.getLong("room_id");
                                            long roomTypeIdBR = rs.getLong("room_type_id");
                                            map.put("brId", brId);
                                            map.put("currentRoomId", currentRoomId);
                                            map.put("roomTypeName", rs.getString("room_type_name"));
                                            map.put("roomTypeId", roomTypeIdBR);
                                            map.put("currentRoomNumber", rs.getString("room_number"));
                                            assignments.add(map);
                                        }
                                    }
                                }
                }
                request.setAttribute("assignments", assignments);
            }
            
            if ("true".equals(request.getParameter("modal"))) {
                request.getRequestDispatcher("/WEB-INF/views/reception/check-in-modal.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/views/reception/check-in.jsp").forward(request, response);
            }
        } catch (SQLException ex) {
            getServletContext().log("Không thể tải danh sách booking check-in", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Optional<CheckInBookingSummary> findSelectedBooking(int bookingId) throws SQLException {
        return bookingId > 0 ? checkInService.findBookingById(bookingId) : Optional.empty();
    }

    private int parseInt(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Integer parseNullableInt(String value) {
        int parsed = parseInt(value);
        return parsed > 0 ? parsed : null;
    }
}

