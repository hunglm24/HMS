package controller.receptionist;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dao.RoomTypeDao;
import model.RoomType;
import java.util.List;

@WebServlet(name = "WalkInReservationServlet", urlPatterns = {"/receptionist/walk-in"})
public class WalkInReservationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RoomTypeDao roomTypeDao = new RoomTypeDao();

    private dao.RoomDao roomDao = new dao.RoomDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<RoomType> roomTypes = roomTypeDao.findAll();
        request.setAttribute("roomTypes", roomTypes);

        String checkInStr = request.getParameter("checkIn");
        String checkOutStr = request.getParameter("checkOut");
        String roomTypeIdStr = request.getParameter("roomTypeId");

        if (checkInStr != null && checkOutStr != null && !checkInStr.isBlank() && !checkOutStr.isBlank()) {
            try {
                java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInStr);
                java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutStr);
                Long roomTypeId = null;
                if (roomTypeIdStr != null && !roomTypeIdStr.isBlank()) {
                    roomTypeId = Long.parseLong(roomTypeIdStr);
                }
                List<model.Room> availablePhysicalRooms = roomDao.findAvailablePhysicalRooms(checkIn, checkOut, roomTypeId);
                request.setAttribute("availablePhysicalRooms", availablePhysicalRooms);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/reception/walk-in-booking.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String checkInStr = request.getParameter("checkIn");
        String checkOutStr = request.getParameter("checkOut");
        String roomIdStr = request.getParameter("roomId");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String identityNumber = request.getParameter("identityNumber");
        String dobStr = request.getParameter("dateOfBirth");
        String guestsStr = request.getParameter("guests");

        try {
            java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInStr);
            java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutStr);
            long roomId = Long.parseLong(roomIdStr);
            int guests = Integer.parseInt(guestsStr);
            java.time.LocalDate today = java.time.LocalDate.now();

            // 1. Validate dates
            if (checkIn.isBefore(today)) {
                request.getSession().setAttribute("error", "Ngày nhận phòng không thể trong quá khứ.");
                response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                return;
            }
            if (!checkIn.isBefore(checkOut)) {
                request.getSession().setAttribute("error", "Ngày trả phòng phải sau ngày nhận phòng.");
                response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                return;
            }

            // 2. Validate Room & Capacity
            model.Room room = roomDao.findById(roomId).orElse(null);
            if (room == null || "INACTIVE".equals(room.getStatus()) || "MAINTENANCE".equals(room.getStatus())) {
                request.getSession().setAttribute("error", "Phòng không hợp lệ hoặc đang bảo trì.");
                response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                return;
            }
            
            RoomType rt = roomTypeDao.findById(room.getRoomTypeId()).orElse(null);
            if (rt == null || guests > rt.getCapacity()) {
                request.getSession().setAttribute("error", "Số khách vượt quá sức chứa của phòng.");
                response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                return;
            }

            // 3. Walk-in Check-in Validation: If checking in today, room must be clean
            String bookingStatus = checkIn.equals(today) ? "CHECKED_IN" : "CONFIRMED";
            if ("CHECKED_IN".equals(bookingStatus)) {
                if ("CLEANING".equals(room.getStatus()) || "DIRTY".equals(room.getStatus()) || "NOT_READY".equals(room.getStatus())) {
                    request.getSession().setAttribute("error", "Không thể Check-in! Phòng đang được dọn hoặc chưa sẵn sàng.");
                    response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                    return;
                }
            }

            // 4. Booking Conflict Validation (Realtime availability check)
            List<model.Room> availablePhysicalRooms = roomDao.findAvailablePhysicalRooms(checkIn, checkOut, null);
            boolean isAvailable = false;
            for (model.Room r : availablePhysicalRooms) {
                if (r.getId() == roomId) {
                    isAvailable = true;
                    break;
                }
            }
            if (!isAvailable) {
                request.getSession().setAttribute("error", "Phòng đã bị đặt trong khoảng thời gian này.");
                response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                return;
            }

            // 5. Customer Profile reuse
            dao.UserDao userDao = new dao.UserDao();
            Long customerId = null;
            if (email != null && !email.isBlank()) {
                java.util.Optional<model.User> existingUser = userDao.findByEmail(email);
                if (existingUser.isPresent()) {
                    customerId = existingUser.get().getId();
                } else {
                    model.User newUser = userDao.createCustomer(fullName, email, phone, null);
                    customerId = newUser.getId();
                }
            }

            // 6. Create booking transaction
            long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            java.math.BigDecimal total = rt.getBasePrice().multiply(new java.math.BigDecimal(nights));
            String bookingCode = "WLK-" + System.currentTimeMillis();

            try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Insert booking
                    String insertBooking = "INSERT INTO bookings (booking_code, booking_source, check_in_date, check_out_date, check_in_datetime, check_out_datetime, total_room_amount, total_amount, status, customer_id) VALUES (?, 'RECEPTION', ?, ?, ?, ?, ?, ?, ?, ?)";
                    long bookingId = 0;
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(insertBooking, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, bookingCode);
                        ps.setDate(2, java.sql.Date.valueOf(checkIn));
                        ps.setDate(3, java.sql.Date.valueOf(checkOut));
                        ps.setTimestamp(4, new java.sql.Timestamp(java.sql.Date.valueOf(checkIn).getTime()));
                        ps.setTimestamp(5, new java.sql.Timestamp(java.sql.Date.valueOf(checkOut).getTime()));
                        ps.setBigDecimal(6, total);
                        ps.setBigDecimal(7, total);
                        ps.setString(8, bookingStatus);
                        if (customerId != null) {
                            ps.setLong(9, customerId);
                        } else {
                            ps.setNull(9, java.sql.Types.BIGINT);
                        }
                        ps.executeUpdate();
                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) bookingId = rs.getLong(1);
                        }
                    }

                    // Insert booking_guests
                    String insertGuest = "INSERT INTO booking_guests (booking_id, full_name, phone, identity_number, date_of_birth, is_primary_guest) VALUES (?, ?, ?, ?, ?, 1)";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(insertGuest)) {
                        ps.setLong(1, bookingId);
                        ps.setString(2, fullName);
                        ps.setString(3, phone);
                        ps.setString(4, identityNumber);
                        if (dobStr != null && !dobStr.isBlank()) {
                            ps.setDate(5, java.sql.Date.valueOf(dobStr));
                        } else {
                            ps.setNull(5, java.sql.Types.DATE);
                        }
                        ps.executeUpdate();
                    }

                    // Insert booking_rooms
                    String insertRoom = "INSERT INTO booking_rooms (booking_id, room_id, price_per_night, number_of_nights, subtotal) VALUES (?, ?, ?, ?, ?)";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(insertRoom)) {
                        ps.setLong(1, bookingId);
                        ps.setLong(2, roomId);
                        ps.setBigDecimal(3, rt.getBasePrice());
                        ps.setLong(4, nights);
                        ps.setBigDecimal(5, total);
                        ps.executeUpdate();
                    }

                    // If checked in, update physical room status
                    if ("CHECKED_IN".equals(bookingStatus)) {
                        roomDao.updateStatus(conn, roomId, "OCCUPIED");
                    }

                    conn.commit();
                    request.getSession().setAttribute("message", "Tạo booking thành công! Mã: " + bookingCode);
                    response.sendRedirect(request.getContextPath() + "/reception/bookings");
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "Dữ liệu không hợp lệ: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
        }
    }
}

