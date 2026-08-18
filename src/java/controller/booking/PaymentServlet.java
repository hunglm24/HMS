package controller.booking;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/checkout"})
public class PaymentServlet extends HttpServlet {
    private service.VNPayService vnPayService = new service.VNPayService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = request.getSession();
        java.util.List<dto.CartItem> cart = (java.util.List<dto.CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (dto.CartItem item : cart) {
            total = total.add(item.getSubtotal());
        }
        request.setAttribute("totalAmount", total);
        
        request.getRequestDispatcher("/WEB-INF/views/public/payment-page.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = request.getSession();
        java.util.List<dto.CartItem> cart = (java.util.List<dto.CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (dto.CartItem item : cart) {
            total = total.add(item.getSubtotal());
        }
        
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String note = request.getParameter("note");

        try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert into bookings
                String bookingCode = "BK-" + System.currentTimeMillis();
                java.sql.Date checkIn = java.sql.Date.valueOf(cart.get(0).getCheckIn());
                java.sql.Date checkOut = java.sql.Date.valueOf(cart.get(0).getCheckOut());
                model.User user = (model.User) session.getAttribute("currentUser");
                String insertBooking = "INSERT INTO bookings (booking_code, booking_source, check_in_date, check_out_date, check_in_datetime, check_out_datetime, total_room_amount, total_amount, status, note, customer_id) VALUES (?, 'ONLINE', ?, ?, ?, ?, ?, ?, 'PENDING_PAYMENT', ?, ?)";
                long bookingId = 0;
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertBooking, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, bookingCode);
                    ps.setDate(2, checkIn);
                    ps.setDate(3, checkOut);
                    ps.setTimestamp(4, new java.sql.Timestamp(checkIn.getTime()));
                    ps.setTimestamp(5, new java.sql.Timestamp(checkOut.getTime()));
                    ps.setBigDecimal(6, total);
                    ps.setBigDecimal(7, total);
                    ps.setString(8, note);
                    if (user != null) {
                        ps.setLong(9, user.getId());
                    } else {
                        ps.setNull(9, java.sql.Types.BIGINT);
                    }
                    ps.executeUpdate();
                    try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) bookingId = rs.getLong(1);
                    }
                }

                // 2. Insert into booking_guests
                String insertGuest = "INSERT INTO booking_guests (booking_id, full_name, phone, is_primary_guest) VALUES (?, ?, ?, 1)";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertGuest)) {
                    ps.setLong(1, bookingId);
                    ps.setString(2, fullName);
                    ps.setString(3, phone);
                    ps.executeUpdate();
                }
                
                // 3. Find and assign physical rooms
                dao.RoomDao roomDao = new dao.RoomDao();
                String insertRoom = "INSERT INTO booking_rooms (booking_id, room_id, price_per_night, number_of_nights, subtotal) VALUES (?, ?, ?, ?, ?)";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertRoom)) {
                    for (dto.CartItem item : cart) {
                        java.util.List<model.Room> available = roomDao.findAvailablePhysicalRooms(item.getCheckIn(), item.getCheckOut(), item.getRoomType().getId());
                        if (available.size() < item.getQuantity()) {
                            throw new IllegalArgumentException("Không đủ phòng trống cho loại phòng: " + item.getRoomType().getName() + ". Vui lòng quay lại giỏ hàng để cập nhật.");
                        }
                        for (int i = 0; i < item.getQuantity(); i++) {
                            ps.setLong(1, bookingId);
                            ps.setLong(2, available.get(i).getId());
                            ps.setBigDecimal(3, item.getRoomType().getBasePrice());
                            ps.setLong(4, item.getNumberOfNights());
                            ps.setBigDecimal(5, item.getRoomType().getBasePrice().multiply(new java.math.BigDecimal(item.getNumberOfNights())));
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }

                conn.commit();
                session.removeAttribute("cart");
                
                // Set success message and redirect
                session.setAttribute("message", "Đặt phòng thành công! Mã booking của bạn là: " + bookingCode + ". Lễ tân sẽ sớm liên hệ với bạn để xác nhận.");
                response.sendRedirect(request.getContextPath() + "/");
            } catch (IllegalArgumentException e) {
                conn.rollback();
                session.setAttribute("error", e.getMessage());
                response.sendRedirect(request.getContextPath() + "/cart");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Lỗi khi đặt phòng: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }
}

