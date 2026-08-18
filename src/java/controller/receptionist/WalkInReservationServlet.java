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
import java.util.ArrayList;

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
        String[] roomIdsStr = request.getParameterValues("roomIds");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String identityNumber = request.getParameter("identityNumber");
        String dobStr = request.getParameter("dateOfBirth");
        String guestsStr = request.getParameter("guests");
        String notes = request.getParameter("notes");
        String paymentStatus = request.getParameter("paymentStatus");
        String paymentMethod = request.getParameter("paymentMethod");
        String submitAction = request.getParameter("submitAction"); // CHECKIN or RESERVE
        String totalAmountStr = request.getParameter("totalAmount");

        try {
            java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInStr);
            java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutStr);
            int guests = Integer.parseInt(guestsStr);
            java.time.LocalDate today = java.time.LocalDate.now();
            java.math.BigDecimal totalAmount = new java.math.BigDecimal(totalAmountStr);

            if (roomIdsStr == null || roomIdsStr.length == 0) {
                request.getSession().setAttribute("error", "Vui lòng chọn ít nhất 1 phòng.");
                response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                return;
            }

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
            
            if ("CHECKIN".equals(submitAction) && !checkIn.equals(today)) {
                request.getSession().setAttribute("error", "Chỉ có thể check-in nếu ngày nhận phòng là hôm nay.");
                response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                return;
            }

            // 2. Validate Rooms
            List<model.Room> selectedRooms = new ArrayList<>();
            List<model.Room> availablePhysicalRooms = roomDao.findAvailablePhysicalRooms(checkIn, checkOut, null);
            int totalCapacity = 0;

            for (String ridStr : roomIdsStr) {
                long roomId = Long.parseLong(ridStr);
                
                // Realtime availability check
                model.Room room = null;
                for (model.Room r : availablePhysicalRooms) {
                    if (r.getId() == roomId) {
                        room = r;
                        break;
                    }
                }
                
                if (room == null || "INACTIVE".equals(room.getStatus()) || "MAINTENANCE".equals(room.getStatus())) {
                    request.getSession().setAttribute("error", "Phòng " + roomId + " đã bị đặt hoặc không hợp lệ.");
                    response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                    return;
                }
                
                // If CHECKIN, room must be clean
                if ("CHECKIN".equals(submitAction)) {
                    if ("CLEANING".equals(room.getStatus()) || "DIRTY".equals(room.getStatus()) || "NOT_READY".equals(room.getStatus())) {
                        request.getSession().setAttribute("error", "Không thể Check-in! Phòng " + room.getRoomNumber() + " chưa sẵn sàng.");
                        response.sendRedirect(request.getContextPath() + "/receptionist/walk-in?checkIn=" + checkInStr + "&checkOut=" + checkOutStr);
                        return;
                    }
                }
                
                RoomType rt = roomTypeDao.findById(room.getRoomTypeId()).orElse(null);
                if (rt != null) {
                    totalCapacity += rt.getCapacity();
                }
                selectedRooms.add(room);
            }

            if (guests > totalCapacity) {
                request.getSession().setAttribute("error", "Số khách vượt quá tổng sức chứa của các phòng đã chọn (" + totalCapacity + ").");
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
            String bookingCode = "WLK-" + System.currentTimeMillis();
            String bookingStatus = "CHECKIN".equals(submitAction) ? "CHECKED_IN" : "CONFIRMED";
            java.math.BigDecimal deposit = "PAID".equals(paymentStatus) ? totalAmount : java.math.BigDecimal.ZERO;

            try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Insert booking
                    String insertBooking = "INSERT INTO bookings (booking_code, booking_source, check_in_date, check_out_date, check_in_datetime, check_out_datetime, total_room_amount, total_amount, status, customer_id, note) VALUES (?, 'RECEPTION', ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    long bookingId = 0;
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(insertBooking, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, bookingCode);
                        ps.setDate(2, java.sql.Date.valueOf(checkIn));
                        ps.setDate(3, java.sql.Date.valueOf(checkOut));
                        ps.setTimestamp(4, new java.sql.Timestamp(java.sql.Date.valueOf(checkIn).getTime()));
                        ps.setTimestamp(5, new java.sql.Timestamp(java.sql.Date.valueOf(checkOut).getTime()));
                        ps.setBigDecimal(6, totalAmount);
                        ps.setBigDecimal(7, totalAmount);
                        ps.setString(8, bookingStatus);
                        if (customerId != null) {
                            ps.setLong(9, customerId);
                        } else {
                            ps.setNull(9, java.sql.Types.BIGINT);
                        }
                        ps.setString(10, notes);
                        ps.executeUpdate();
                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) bookingId = rs.getLong(1);
                        }
                    }

                    // Insert booking_guests
                    String insertGuest = "INSERT INTO booking_guests (booking_id, full_name, phone, email, identity_number, date_of_birth, is_primary_guest) VALUES (?, ?, ?, ?, ?, ?, 1)";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(insertGuest)) {
                        ps.setLong(1, bookingId);
                        ps.setString(2, fullName);
                        ps.setString(3, phone);
                        ps.setString(4, email);
                        ps.setString(5, identityNumber);
                        if (dobStr != null && !dobStr.isBlank()) {
                            ps.setDate(6, java.sql.Date.valueOf(dobStr));
                        } else {
                            ps.setNull(6, java.sql.Types.DATE);
                        }
                        ps.executeUpdate();
                    }

                    // Insert booking_rooms & update physical status
                    String insertRoom = "INSERT INTO booking_rooms (booking_id, room_id, price_per_night, number_of_nights, subtotal) VALUES (?, ?, ?, ?, ?)";
                    for (model.Room room : selectedRooms) {
                        RoomType rt = roomTypeDao.findById(room.getRoomTypeId()).orElse(null);
                        java.math.BigDecimal basePrice = rt != null ? rt.getBasePrice() : java.math.BigDecimal.ZERO;
                        java.math.BigDecimal subtotal = basePrice.multiply(new java.math.BigDecimal(nights));

                        try (java.sql.PreparedStatement ps = conn.prepareStatement(insertRoom)) {
                            ps.setLong(1, bookingId);
                            ps.setLong(2, room.getId());
                            ps.setBigDecimal(3, basePrice);
                            ps.setLong(4, nights);
                            ps.setBigDecimal(5, subtotal);
                            ps.executeUpdate();
                        }

                        if ("CHECKED_IN".equals(bookingStatus)) {
                            roomDao.updateStatus(conn, room.getId(), "OCCUPIED");
                        }
                    }

                    // Insert payment record if paid
                    if ("PAID".equals(paymentStatus)) {
                        String insertPayment = "INSERT INTO payments (booking_id, payment_type, payment_method, amount, status, paid_at) VALUES (?, 'PAYMENT', ?, ?, 'COMPLETED', CURRENT_TIMESTAMP)";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(insertPayment)) {
                            ps.setLong(1, bookingId);
                            ps.setString(2, paymentMethod != null ? paymentMethod : "CASH");
                            ps.setBigDecimal(3, totalAmount);
                            ps.executeUpdate();
                        }
                    }

                    conn.commit();
                    
                    // Gửi email xác nhận nếu có email
                    if (email != null && !email.isBlank()) {
                        dao.BookingDao bookingDao = new dao.BookingDao();
                        bookingDao.findById(bookingId).ifPresent(booking -> {
                            service.EmailService emailService = new service.EmailService();
                            emailService.sendBookingConfirmationAsync(booking, email, fullName);
                        });
                    }
                    
                    request.getSession().setAttribute("toastMessage", "Tạo booking thành công! Mã: " + bookingCode);
                    request.getSession().setAttribute("toastType", "toast-success");
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
