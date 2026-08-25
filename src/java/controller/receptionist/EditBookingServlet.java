package controller.receptionist;

import dao.BookingDao;
import dao.RoomDao;
import dao.RoomTypeDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Booking;
import model.Room;
import model.RoomType;
import util.DBConnectionUtil;
import service.AuditLogService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@WebServlet(name = "EditBookingServlet", urlPatterns = {"/receptionist/edit-booking"})
public class EditBookingServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();
    private final RoomTypeDao roomTypeDao = new RoomTypeDao();
    private final RoomDao roomDao = new RoomDao();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/reception/bookings");
            return;
        }

        try {
            long bookingId = Long.parseLong(idStr);
            Booking booking = bookingDao.findById(bookingId).orElse(null);
            if (booking == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Khong tim thay booking.");
                return;
            }

            if ("CANCELLED".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
                request.getSession().setAttribute("error", "Khong the sua booking da huy hoac da tra phong.");
                response.sendRedirect(request.getContextPath() + "/reception/bookings");
                return;
            }

            String guestName = "";
            String phone = "";

            List<BookingRoomItemView> bookingRooms;
            try (Connection conn = DBConnectionUtil.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT full_name, phone FROM booking_guests WHERE booking_id = ? AND is_primary_guest = 1 LIMIT 1")) {
                    ps.setLong(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            guestName = rs.getString("full_name");
                            phone = rs.getString("phone");
                        }
                    }
                }

                bookingRooms = loadBookingRooms(conn, bookingId);
            }

            LocalDate filterCheckIn = parseDateOrDefault(request.getParameter("checkInDate"), booking.getCheckInDate().toLocalDate());
            LocalDate filterCheckOut = parseDateOrDefault(request.getParameter("checkOutDate"), booking.getCheckOutDate().toLocalDate());
            Long selectedRoomTypeId = parseNullableLong(request.getParameter("roomTypeId"));
            String activeBookingRoomKey = request.getParameter("activeBookingRoomKey");
            Map<Long, Long> selectedAssignments = parseSelectedRoomAssignments(request);
            Map<Long, Long> newSelectedAssignments = parseNewRoomAssignments(request);
            List<Long> newRoomSlots = parseNewRoomSlots(request);
            if (selectedAssignments.isEmpty()) {
                for (BookingRoomItemView room : bookingRooms) {
                    selectedAssignments.put(room.bookingRoomId, room.roomId);
                }
            } else {
                for (BookingRoomItemView room : bookingRooms) {
                    if (!selectedAssignments.containsKey(room.bookingRoomId)) {
                        selectedAssignments.put(room.bookingRoomId, room.roomId);
                    }
                }
            }

            try (Connection conn = DBConnectionUtil.getConnection()) {
                for (BookingRoomItemView room : bookingRooms) {
                    Long selectedRoomId = selectedAssignments.get(room.bookingRoomId);
                    if (selectedAssignments.containsKey(room.bookingRoomId) && selectedRoomId == null) {
                        room.selectedRoomId = null;
                        room.selectedRoomNumber = null;
                        room.selectedRoomTypeName = null;
                        room.selectedRoomTypeBasePrice = null;
                        continue;
                    }
                    RoomAvailabilityView selectedRoom = selectedRoomId != null ? loadRoomForEdit(conn, selectedRoomId) : null;
                    if (selectedRoom == null) {
                        selectedRoom = new RoomAvailabilityView();
                        selectedRoom.roomId = room.roomId;
                        selectedRoom.roomNumber = room.roomNumber;
                        selectedRoom.roomTypeId = room.roomTypeId;
                        selectedRoom.roomTypeName = room.roomTypeName;
                        selectedRoom.roomTypeBasePrice = room.roomTypeBasePrice;
                    }
                    room.selectedRoomId = selectedRoom.roomId;
                    room.selectedRoomNumber = selectedRoom.roomNumber;
                    room.selectedRoomTypeName = selectedRoom.roomTypeName;
                    room.selectedRoomTypeBasePrice = selectedRoom.roomTypeBasePrice;
                }
            }

            List<RoomType> roomTypes = roomTypeDao.findAll();
            List<Room> availablePhysicalRooms = roomDao.findAvailablePhysicalRooms(filterCheckIn, filterCheckOut, selectedRoomTypeId, bookingId);

            request.setAttribute("booking", booking);
            request.setAttribute("guestName", guestName);
            request.setAttribute("phone", phone);
            request.setAttribute("roomTypes", roomTypes);
            request.setAttribute("availablePhysicalRooms", availablePhysicalRooms);
            request.setAttribute("roomPickerCheckIn", filterCheckIn.toString());
            request.setAttribute("roomPickerCheckOut", filterCheckOut.toString());
            request.setAttribute("selectedRoomTypeId", selectedRoomTypeId);
            request.setAttribute("bookingRooms", bookingRooms);
            request.setAttribute("bookingRoomCount", bookingRooms.size());
            request.setAttribute("totalBookingRoomCount", bookingRooms.size() + newRoomSlots.size());
            request.setAttribute("selectedRoomAssignments", selectedAssignments);
            request.setAttribute("activeBookingRoomKey", activeBookingRoomKey);
            request.setAttribute("newRoomAssignments", newSelectedAssignments);
            request.setAttribute("newRoomSlots", newRoomSlots);

            request.getRequestDispatcher("/WEB-INF/views/reception/edit-booking.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String checkInStr = request.getParameter("checkInDate");
        String checkOutStr = request.getParameter("checkOutDate");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String reason = request.getParameter("reason");
        String roomIdStr = request.getParameter("roomId");
        String roomTypeIdStr = request.getParameter("roomTypeId");

        if (idStr == null || idStr.isBlank() || reason == null || reason.trim().isEmpty()) {
            request.getSession().setAttribute("error", "Thieu ID hoac ly do sua.");
            response.sendRedirect(request.getContextPath() + "/reception/bookings");
            return;
        }

        try {
            long bookingId = Long.parseLong(idStr);
            Booking booking = bookingDao.findById(bookingId).orElse(null);
            if (booking == null || "CANCELLED".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/reception/bookings");
                return;
            }

            LocalDate newCheckIn = LocalDate.parse(checkInStr);
            LocalDate newCheckOut = LocalDate.parse(checkOutStr);
            if (!newCheckIn.isBefore(newCheckOut)) {
                request.getSession().setAttribute("error", "Ngay tra phong phai sau ngay nhan phong.");
                response.sendRedirect(buildEditBookingRedirect(request, String.valueOf(bookingId), checkInStr, checkOutStr, roomIdStr, roomTypeIdStr));
                return;
            }

            Map<Long, Long> selectedAssignments = parseSelectedRoomAssignments(request);
            Map<Long, Long> newSelectedAssignments = parseNewRoomAssignments(request);

            try (Connection conn = DBConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    List<BookingRoomItemView> bookingRooms = loadBookingRooms(conn, bookingId);
                    if (bookingRooms.isEmpty()) {
                        throw new IllegalArgumentException("Booking chua co phong de cap nhat.");
                    }

                    for (BookingRoomItemView room : bookingRooms) {
                        if (!selectedAssignments.containsKey(room.bookingRoomId)) {
                            selectedAssignments.put(room.bookingRoomId, room.roomId);
                        }
                    }

                    if (fullName != null && !fullName.isBlank()) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE booking_guests SET full_name = ?, phone = ? WHERE booking_id = ? AND is_primary_guest = 1")) {
                            ps.setString(1, fullName);
                            ps.setString(2, phone);
                            ps.setLong(3, bookingId);
                            ps.executeUpdate();
                        }
                    }

                    List<Room> availableRooms = roomDao.findAvailablePhysicalRooms(newCheckIn, newCheckOut, null, bookingId);
                    Set<Long> availableRoomIds = new HashSet<>();
                    for (Room room : availableRooms) {
                        availableRoomIds.add(room.getId());
                    }

                    long nights = ChronoUnit.DAYS.between(newCheckIn, newCheckOut);
                    java.math.BigDecimal totalRoomAmount = java.math.BigDecimal.ZERO;
                    StringBuilder roomNoteBuilder = new StringBuilder();

                    Set<Long> selectedRoomIds = new HashSet<>();
                    for (BookingRoomItemView bookingRoom : bookingRooms) {
                        boolean hasExplicitAssignment = selectedAssignments.containsKey(bookingRoom.bookingRoomId);
                        Long targetRoomId = selectedAssignments.get(bookingRoom.bookingRoomId);
                        if (hasExplicitAssignment && targetRoomId == null) {
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "DELETE FROM booking_rooms WHERE id = ? AND booking_id = ?")) {
                                ps.setLong(1, bookingRoom.bookingRoomId);
                                ps.setLong(2, bookingId);
                                ps.executeUpdate();
                            }

                            roomNoteBuilder.append("\n[")
                                    .append(java.time.LocalDateTime.now())
                                    .append("] Xoa phong booking_room #")
                                    .append(bookingRoom.bookingRoomId)
                                    .append(": ")
                                    .append(bookingRoom.roomNumber)
                                    .append(". Ly do: ")
                                    .append(reason);
                            continue;
                        }
                        if (targetRoomId == null || targetRoomId <= 0) {
                            throw new IllegalArgumentException("Vui long chon phong cho tung dong booking.");
                        }
                        if (!selectedRoomIds.add(targetRoomId)) {
                            throw new IllegalArgumentException("Khong the gan trung phong vat ly cho nhieu dong trong cung booking.");
                        }
                        boolean isCurrentRoom = targetRoomId == bookingRoom.roomId;
                        if (!isCurrentRoom && !availableRoomIds.contains(targetRoomId)) {
                            throw new IllegalArgumentException("Phong duoc chon khong con trong trong khoang thoi gian nay.");
                        }

                        RoomAvailabilityView roomAvailability = loadRoomForEdit(conn, targetRoomId);
                        if (roomAvailability == null) {
                            throw new IllegalArgumentException("Phong duoc chon khong ton tai.");
                        }

                        java.math.BigDecimal basePrice = roomAvailability.roomTypeBasePrice != null ? roomAvailability.roomTypeBasePrice : java.math.BigDecimal.ZERO;
                        java.math.BigDecimal subtotal = basePrice.multiply(java.math.BigDecimal.valueOf(nights));
                        totalRoomAmount = totalRoomAmount.add(subtotal);

                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE booking_rooms SET room_id = ?, price_per_night = ?, number_of_nights = ?, subtotal = ? WHERE id = ? AND booking_id = ?")) {
                            ps.setLong(1, targetRoomId);
                            ps.setBigDecimal(2, basePrice);
                            ps.setLong(3, nights);
                            ps.setBigDecimal(4, subtotal);
                            ps.setLong(5, bookingRoom.bookingRoomId);
                            ps.setLong(6, bookingId);
                            ps.executeUpdate();
                        }

                        if (!isCurrentRoom) {
                            roomNoteBuilder.append("\n[")
                                    .append(java.time.LocalDateTime.now())
                                    .append("] Doi phong booking_room #")
                                    .append(bookingRoom.bookingRoomId)
                                    .append(": ")
                                    .append(bookingRoom.roomNumber)
                                    .append(" -> ")
                                    .append(roomAvailability.roomNumber)
                                    .append(". Ly do: ")
                                    .append(reason);
                        }
                    }

                    for (Map.Entry<Long, Long> entry : newSelectedAssignments.entrySet()) {
                        Long targetRoomId = entry.getValue();
                        if (targetRoomId == null || targetRoomId <= 0) {
                            throw new IllegalArgumentException("Vui long chon phong cho dong them moi.");
                        }
                        if (!selectedRoomIds.add(targetRoomId)) {
                            throw new IllegalArgumentException("Khong the gan trung phong vat ly cho nhieu dong trong cung booking.");
                        }
                        if (!availableRoomIds.contains(targetRoomId)) {
                            throw new IllegalArgumentException("Phong duoc chon khong con trong trong khoang thoi gian nay.");
                        }

                        RoomAvailabilityView roomAvailability = loadRoomForEdit(conn, targetRoomId);
                        if (roomAvailability == null) {
                            throw new IllegalArgumentException("Phong duoc chon khong ton tai.");
                        }

                        java.math.BigDecimal basePrice = roomAvailability.roomTypeBasePrice != null ? roomAvailability.roomTypeBasePrice : java.math.BigDecimal.ZERO;
                        java.math.BigDecimal subtotal = basePrice.multiply(java.math.BigDecimal.valueOf(nights));
                        totalRoomAmount = totalRoomAmount.add(subtotal);

                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO booking_rooms (booking_id, room_id, price_per_night, number_of_nights, subtotal) VALUES (?, ?, ?, ?, ?)")) {
                            ps.setLong(1, bookingId);
                            ps.setLong(2, targetRoomId);
                            ps.setBigDecimal(3, basePrice);
                            ps.setLong(4, nights);
                            ps.setBigDecimal(5, subtotal);
                            ps.executeUpdate();
                        }

                        roomNoteBuilder.append("\n[")
                                .append(java.time.LocalDateTime.now())
                                .append("] Them phong moi: ")
                                .append(roomAvailability.roomNumber)
                                .append(". Ly do: ")
                                .append(reason);
                    }

                    if (roomNoteBuilder.length() == 0) {
                        roomNoteBuilder.append("\n[")
                                .append(java.time.LocalDateTime.now())
                                .append("] Cap nhat thong tin booking. Ly do: ")
                                .append(reason);
                    }

                    String dateNote = !newCheckIn.equals(booking.getCheckInDate().toLocalDate()) || !newCheckOut.equals(booking.getCheckOutDate().toLocalDate())
                            ? "\n[" + java.time.LocalDateTime.now() + "] Doi ngay -> Cu: " + booking.getCheckInDate() + " den " + booking.getCheckOutDate() + ". Ly do: " + reason
                            : "";
                    String roomNote = roomNoteBuilder.toString();

                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE bookings SET check_in_date = ?, check_out_date = ?, check_in_datetime = ?, check_out_datetime = ?, total_room_amount = ?, total_amount = ?, note = CONCAT(IFNULL(note,''), ?, ?) WHERE id = ?")) {
                        ps.setDate(1, java.sql.Date.valueOf(newCheckIn));
                        ps.setDate(2, java.sql.Date.valueOf(newCheckOut));
                        ps.setTimestamp(3, java.sql.Timestamp.valueOf(newCheckIn.atStartOfDay()));
                        ps.setTimestamp(4, java.sql.Timestamp.valueOf(newCheckOut.atStartOfDay()));
                        ps.setBigDecimal(5, totalRoomAmount);
                        ps.setBigDecimal(6, totalRoomAmount);
                        ps.setString(7, dateNote);
                        ps.setString(8, roomNote);
                        ps.setLong(9, bookingId);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    auditLogService.log(request, "UPDATE_BOOKING", "BOOKING", bookingId,
                            "Edited booking " + bookingId + ". Reason: " + reason);
                    request.getSession().setAttribute("toastMessage", "Cap nhat Reservation thanh cong!");
                    request.getSession().setAttribute("toastType", "toast-success");
                    response.sendRedirect(request.getContextPath() + "/reception/bookings");
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "Loi du lieu: " + e.getMessage());
            response.sendRedirect(buildEditBookingRedirect(request, idStr, checkInStr, checkOutStr, roomIdStr, roomTypeIdStr));
        }
    }

    private BookingRoomView loadPrimaryBookingRoom(Connection conn, long bookingId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT br.id AS booking_room_id,
                       br.room_id,
                       r.room_number,
                       r.room_type_id,
                       rt.name AS room_type_name,
                       rt.base_price AS room_type_base_price
                FROM booking_rooms br
                JOIN rooms r ON r.id = br.room_id
                JOIN room_types rt ON rt.id = r.room_type_id
                WHERE br.booking_id = ?
                ORDER BY br.id ASC
                LIMIT 1
                """)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BookingRoomView view = new BookingRoomView();
                    view.bookingRoomId = rs.getLong("booking_room_id");
                    view.roomId = rs.getLong("room_id");
                    view.roomNumber = rs.getString("room_number");
                    view.roomTypeId = rs.getLong("room_type_id");
                    view.roomTypeName = rs.getString("room_type_name");
                    view.roomTypeBasePrice = rs.getBigDecimal("room_type_base_price");
                    return view;
                }
            }
        }
        return null;
    }

    private int countBookingRooms(Connection conn, long bookingId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM booking_rooms WHERE booking_id = ?")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    private List<BookingRoomItemView> loadBookingRooms(Connection conn, long bookingId) throws Exception {
        List<BookingRoomItemView> bookingRooms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT br.id AS booking_room_id,
                       br.room_id,
                       r.room_number,
                       r.room_type_id,
                       rt.name AS room_type_name,
                       rt.base_price AS room_type_base_price,
                       br.price_per_night,
                       br.number_of_nights,
                       br.subtotal
                FROM booking_rooms br
                JOIN rooms r ON br.room_id = r.id
                JOIN room_types rt ON rt.id = r.room_type_id
                WHERE br.booking_id = ?
                ORDER BY br.id ASC
                """)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingRoomItemView view = new BookingRoomItemView();
                    view.bookingRoomId = rs.getLong("booking_room_id");
                    view.roomId = rs.getLong("room_id");
                    view.roomNumber = rs.getString("room_number");
                    view.roomTypeId = rs.getLong("room_type_id");
                    view.roomTypeName = rs.getString("room_type_name");
                    view.roomTypeBasePrice = rs.getBigDecimal("room_type_base_price");
                    view.pricePerNight = rs.getBigDecimal("price_per_night");
                    view.numberOfNights = rs.getInt("number_of_nights");
                    view.subtotal = rs.getBigDecimal("subtotal");
                    bookingRooms.add(view);
                }
            }
        }
        return bookingRooms;
    }

    private List<java.util.Map<String, Object>> loadBookedRooms(Connection conn, long bookingId) throws Exception {
        List<java.util.Map<String, Object>> bookedRooms = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT r.room_number,
                       rt.name AS room_type_name,
                       br.price_per_night,
                       br.number_of_nights,
                       br.subtotal
                FROM booking_rooms br
                JOIN rooms r ON br.room_id = r.id
                JOIN room_types rt ON r.room_type_id = rt.id
                WHERE br.booking_id = ?
                ORDER BY br.id ASC
                """)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> roomMap = new java.util.HashMap<>();
                    roomMap.put("roomNumber", rs.getString("room_number"));
                    roomMap.put("roomTypeName", rs.getString("room_type_name"));
                    roomMap.put("pricePerNight", rs.getBigDecimal("price_per_night"));
                    roomMap.put("nights", rs.getInt("number_of_nights"));
                    roomMap.put("subtotal", rs.getBigDecimal("subtotal"));
                    bookedRooms.add(roomMap);
                }
            }
        }
        return bookedRooms;
    }

    private Map<Long, Long> parseSelectedRoomAssignments(HttpServletRequest request) {
        Map<Long, Long> assignments = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (key == null || !key.startsWith("assignedRoom_") || values == null || values.length == 0) {
                return;
            }
            try {
                long bookingRoomId = Long.parseLong(key.substring("assignedRoom_".length()));
                Long roomId = parseNullableLong(values[0]);
                assignments.put(bookingRoomId, roomId);
            } catch (NumberFormatException ignored) {
                // Ignore malformed assignment keys.
            }
        });
        return assignments;
    }

    private Map<Long, Long> parseNewRoomAssignments(HttpServletRequest request) {
        Map<Long, Long> assignments = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (key == null || !key.startsWith("newAssignedRoom_") || values == null || values.length == 0) {
                return;
            }
            Long roomId = parseNullableLong(values[0]);
            if (roomId != null) {
                try {
                    long slotId = Long.parseLong(key.substring("newAssignedRoom_".length()));
                    assignments.put(slotId, roomId);
                } catch (NumberFormatException ignored) {
                    // Ignore malformed temp slot ids.
                }
            }
        });
        return assignments;
    }

    private List<Long> parseNewRoomSlots(HttpServletRequest request) {
        java.util.Set<Long> slots = new java.util.TreeSet<>();
        request.getParameterMap().forEach((key, values) -> {
            if (key == null || !key.startsWith("newAssignedRoom_")) {
                return;
            }
            try {
                long slotId = Long.parseLong(key.substring("newAssignedRoom_".length()));
                slots.add(slotId);
            } catch (NumberFormatException ignored) {
                // Ignore malformed temp slot ids.
            }
        });
        return new ArrayList<>(slots);
    }

    private RoomAvailabilityView loadRoomForEdit(Connection conn, long roomId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT r.id,
                       r.room_number,
                       r.room_type_id,
                       rt.name AS room_type_name,
                       rt.base_price AS room_type_base_price
                FROM rooms r
                JOIN room_types rt ON rt.id = r.room_type_id
                WHERE r.id = ?
                LIMIT 1
                """)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RoomAvailabilityView view = new RoomAvailabilityView();
                    view.roomId = rs.getLong("id");
                    view.roomNumber = rs.getString("room_number");
                    view.roomTypeId = rs.getLong("room_type_id");
                    view.roomTypeName = rs.getString("room_type_name");
                    view.roomTypeBasePrice = rs.getBigDecimal("room_type_base_price");
                    return view;
                }
            }
        }
        return null;
    }

    private LocalDate parseDateOrDefault(String value, LocalDate fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return LocalDate.parse(value);
    }

    private Long parseNullableLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildEditBookingRedirect(HttpServletRequest request, String bookingId, String checkInStr,
                                            String checkOutStr, String roomIdStr, String roomTypeIdStr) {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/receptionist/edit-booking?id=").append(bookingId == null ? "" : bookingId);
        if (checkInStr != null && !checkInStr.isBlank()) {
            url.append("&checkInDate=").append(checkInStr);
        }
        if (checkOutStr != null && !checkOutStr.isBlank()) {
            url.append("&checkOutDate=").append(checkOutStr);
        }
        if (roomIdStr != null && !roomIdStr.isBlank()) {
            url.append("&roomId=").append(roomIdStr);
        }
        if (roomTypeIdStr != null && !roomTypeIdStr.isBlank()) {
            url.append("&roomTypeId=").append(roomTypeIdStr);
        }
        String activeBookingRoomKey = request.getParameter("activeBookingRoomKey");
        if (activeBookingRoomKey != null && !activeBookingRoomKey.isBlank()) {
            url.append("&activeBookingRoomKey=").append(activeBookingRoomKey);
        }
        request.getParameterMap().forEach((key, values) -> {
            if (key == null || values == null || values.length == 0) {
                return;
            }
            String value = values[0];
            if (key.startsWith("assignedRoom_")) {
                url.append("&").append(key).append("=").append(value);
            } else if (key.startsWith("newAssignedRoom_")) {
                url.append("&").append(key).append("=");
            }
        });
        return url.toString();
    }

    public static class BookingRoomView {
        long bookingRoomId;
        long roomId;
        String roomNumber;
        long roomTypeId;
        String roomTypeName;
        java.math.BigDecimal roomTypeBasePrice;

        public long getBookingRoomId() {
            return bookingRoomId;
        }

        public long getRoomId() {
            return roomId;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public long getRoomTypeId() {
            return roomTypeId;
        }

        public String getRoomTypeName() {
            return roomTypeName;
        }

        public java.math.BigDecimal getRoomTypeBasePrice() {
            return roomTypeBasePrice;
        }
    }

    public static class BookingRoomItemView {
        long bookingRoomId;
        long roomId;
        String roomNumber;
        long roomTypeId;
        String roomTypeName;
        java.math.BigDecimal roomTypeBasePrice;
        java.math.BigDecimal pricePerNight;
        int numberOfNights;
        java.math.BigDecimal subtotal;
        Long selectedRoomId;
        String selectedRoomNumber;
        String selectedRoomTypeName;
        java.math.BigDecimal selectedRoomTypeBasePrice;

        public long getBookingRoomId() {
            return bookingRoomId;
        }

        public long getRoomId() {
            return roomId;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public long getRoomTypeId() {
            return roomTypeId;
        }

        public String getRoomTypeName() {
            return roomTypeName;
        }

        public java.math.BigDecimal getRoomTypeBasePrice() {
            return roomTypeBasePrice;
        }

        public java.math.BigDecimal getPricePerNight() {
            return pricePerNight;
        }

        public int getNumberOfNights() {
            return numberOfNights;
        }

        public java.math.BigDecimal getSubtotal() {
            return subtotal;
        }

        public Long getSelectedRoomId() {
            return selectedRoomId;
        }

        public String getSelectedRoomNumber() {
            return selectedRoomNumber;
        }

        public String getSelectedRoomTypeName() {
            return selectedRoomTypeName;
        }

        public java.math.BigDecimal getSelectedRoomTypeBasePrice() {
            return selectedRoomTypeBasePrice;
        }
    }

    public static class RoomAvailabilityView {
        long roomId;
        String roomNumber;
        long roomTypeId;
        String roomTypeName;
        java.math.BigDecimal roomTypeBasePrice;

        public long getRoomId() {
            return roomId;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public long getRoomTypeId() {
            return roomTypeId;
        }

        public String getRoomTypeName() {
            return roomTypeName;
        }

        public java.math.BigDecimal getRoomTypeBasePrice() {
            return roomTypeBasePrice;
        }

        BookingRoomView asBookingRoomView() {
            BookingRoomView view = new BookingRoomView();
            view.roomId = roomId;
            view.roomNumber = roomNumber;
            view.roomTypeId = roomTypeId;
            view.roomTypeName = roomTypeName;
            view.roomTypeBasePrice = roomTypeBasePrice;
            return view;
        }
    }
}
