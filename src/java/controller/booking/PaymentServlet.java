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
    private dao.PromotionDao promotionDao = new dao.PromotionDao();
    private dao.SeasonalPriceRuleDao priceRuleDao = new dao.SeasonalPriceRuleDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = request.getSession();
        java.util.List<dto.CartItem> cart = (java.util.List<dto.CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        refreshCartPrices(cart);
        
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (dto.CartItem item : cart) {
            total = total.add(item.getSubtotal());
        }
        model.Promotion promotion = (model.Promotion) session.getAttribute("appliedPromotion");
        java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
        if (promotion != null) {
            try {
                promotion = promotionDao.findUsableByCode(promotion.getCode(), total).orElse(null);
                if (promotion != null) {
                    discountAmount = promotion.calculateDiscount(total);
                }
            } catch (Exception ignored) {
                promotion = null;
            }
        }
        java.math.BigDecimal finalAmount = total.subtract(discountAmount);
        request.setAttribute("roomAmount", total);
        request.setAttribute("discountAmount", discountAmount);
        request.setAttribute("totalAmount", finalAmount);
        if (promotion == null) {
            session.removeAttribute("appliedPromotion");
        }
        
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
        refreshCartPrices(cart);

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (dto.CartItem item : cart) {
            total = total.add(item.getSubtotal());
        }
        model.Promotion promotion = (model.Promotion) session.getAttribute("appliedPromotion");
        java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
        if (promotion != null) {
            try {
                promotion = promotionDao.findUsableByCode(promotion.getCode(), total).orElse(null);
                if (promotion == null) {
                    session.removeAttribute("appliedPromotion");
                    session.setAttribute("error", "Mã giảm giá đã không còn hợp lệ. Vui lòng kiểm tra lại giỏ hàng.");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                discountAmount = promotion.calculateDiscount(total);
            } catch (Exception ex) {
                session.setAttribute("error", "Không thể kiểm tra mã giảm giá. Vui lòng thử lại.");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
        }
        java.math.BigDecimal finalAmount = total.subtract(discountAmount);
        
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String note = request.getParameter("note");

        if (!config.VNPayConfig.isConfigured()) {
            session.setAttribute("error", "VNPay chưa được cấu hình. Vui lòng thêm mã TMN và Hash Secret sandbox.");
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        try (java.sql.Connection conn = util.DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert into bookings
                String bookingCode = "BK-" + System.currentTimeMillis();
                java.sql.Date checkIn = java.sql.Date.valueOf(cart.get(0).getCheckIn());
                java.sql.Date checkOut = java.sql.Date.valueOf(cart.get(0).getCheckOut());
                model.User user = (model.User) session.getAttribute("currentUser");
                String insertBooking = "INSERT INTO bookings (booking_code, booking_source, check_in_date, check_out_date, check_in_datetime, check_out_datetime, total_room_amount, discount_amount, total_amount, promotion_id, status, note, customer_id) VALUES (?, 'ONLINE', ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING_PAYMENT', ?, ?)";
                long bookingId = 0;
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertBooking, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, bookingCode);
                    ps.setDate(2, checkIn);
                    ps.setDate(3, checkOut);
                    ps.setTimestamp(4, new java.sql.Timestamp(checkIn.getTime()));
                    ps.setTimestamp(5, new java.sql.Timestamp(checkOut.getTime()));
                    ps.setBigDecimal(6, total);
                    ps.setBigDecimal(7, discountAmount);
                    ps.setBigDecimal(8, finalAmount);
                    if (promotion == null) {
                        ps.setNull(9, java.sql.Types.BIGINT);
                    } else {
                        ps.setLong(9, promotion.getId());
                    }
                    ps.setString(10, note);
                    if (user != null) {
                        ps.setLong(11, user.getId());
                    } else {
                        ps.setNull(11, java.sql.Types.BIGINT);
                    }
                    ps.executeUpdate();
                    try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) bookingId = rs.getLong(1);
                    }
                }

                // 2. Insert into booking_guests
                String insertGuest = "INSERT INTO booking_guests (booking_id, full_name, phone, email, is_primary_guest) VALUES (?, ?, ?, ?, 1)";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertGuest)) {
                    ps.setLong(1, bookingId);
                    ps.setString(2, fullName);
                    ps.setString(3, phone);
                    ps.setString(4, email);
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
                            java.math.BigDecimal pricePerRoom = item.getSubtotal().divide(
                                    java.math.BigDecimal.valueOf(item.getQuantity()),
                                    0,
                                    java.math.RoundingMode.HALF_UP);
                            ps.setLong(1, bookingId);
                            ps.setLong(2, available.get(i).getId());
                            ps.setBigDecimal(3, pricePerRoom.divide(
                                    java.math.BigDecimal.valueOf(item.getNumberOfNights()),
                                    0,
                                    java.math.RoundingMode.HALF_UP));
                            ps.setLong(4, item.getNumberOfNights());
                            ps.setBigDecimal(5, pricePerRoom);
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }

                if (promotion != null) {
                    promotionDao.incrementUsedCount(conn, promotion.getId());
                }

                conn.commit();
                session.setAttribute("pendingBookingId", bookingId);
                session.setAttribute("pendingBookingCode", bookingCode);
                session.setAttribute("pendingPaymentAmount", finalAmount);

                String returnUrl = request.getScheme() + "://" + request.getServerName()
                        + ((request.getServerPort() == 80 || request.getServerPort() == 443)
                        ? "" : ":" + request.getServerPort())
                        + request.getContextPath() + "/payment-return";
                String paymentUrl = vnPayService.createPaymentUrl(finalAmount.longValueExact(),
                        "Thanh toan dat phong " + bookingCode, bookingCode,
                        request.getRemoteAddr(), returnUrl);
                response.sendRedirect(paymentUrl);
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

    private void refreshCartPrices(java.util.List<dto.CartItem> cart) {
        for (dto.CartItem item : cart) {
            try {
                java.math.BigDecimal subtotal = priceRuleDao.calculateSubtotal(
                        item.getRoomType().getId(),
                        item.getRoomType().getBasePrice(),
                        item.getCheckIn(),
                        item.getCheckOut(),
                        item.getQuantity());
                item.setSubtotalOverride(subtotal);
            } catch (Exception ex) {
                item.setSubtotalOverride(null);
            }
        }
    }
}

