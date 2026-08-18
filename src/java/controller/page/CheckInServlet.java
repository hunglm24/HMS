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
        Integer roomTypeId = parseNullableInt(request.getParameter("roomTypeId"));
        String scope = request.getParameter("scope");
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
                                map.put("currentRoomNumber", rs.getString("room_number"));
                                
                                // Fetch available rooms of this type
                                java.util.List<model.Room> availRooms = new java.util.ArrayList<>();
                                String sqlAvail = "SELECT r.* FROM rooms r WHERE r.room_type_id = ? AND r.status != 'MAINTENANCE' " +
                                                  "AND r.id NOT IN (SELECT br2.room_id FROM booking_rooms br2 JOIN bookings b ON br2.booking_id = b.id " +
                                                  "WHERE b.status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CHECKED_IN') " +
                                                  "AND b.check_in_date < ? AND b.check_out_date > ? AND b.id != ?)";
                                try (java.sql.PreparedStatement psAvail = conn.prepareStatement(sqlAvail)) {
                                    psAvail.setLong(1, roomTypeIdBR);
                                    psAvail.setDate(2, new java.sql.Date(selectedBooking.getCheckOutDate().getTime()));
                                    psAvail.setDate(3, new java.sql.Date(selectedBooking.getCheckInDate().getTime()));
                                    psAvail.setInt(4, bookingId);
                                    try (java.sql.ResultSet rsAvail = psAvail.executeQuery()) {
                                        while (rsAvail.next()) {
                                            model.Room r = new model.Room();
                                            r.setId(rsAvail.getLong("id"));
                                            r.setRoomNumber(rsAvail.getString("room_number"));
                                            availRooms.add(r);
                                        }
                                    }
                                }
                                map.put("availableRooms", availRooms);
                                assignments.add(map);
                            }
                        }
                    }
                }
                request.setAttribute("assignments", assignments);
            }
            
            request.getRequestDispatcher("/WEB-INF/views/reception/check-in.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ£i danh sÃƒÂ¡ch booking check-in", ex);
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

