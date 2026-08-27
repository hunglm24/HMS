package controller.page;

import dao.BookingDao;
import dao.BookingRefundDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Booking;
import util.DBConnectionUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/reception/booking-detail", "/reception/walk-in", "/manager/booking-detail"})
public class ReceptionBookingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String view = "/WEB-INF/views/reception/booking-list.jsp";

        if ("/reception/booking-detail".equals(path) || "/manager/booking-detail".equals(path)) {
            boolean managerView = path.startsWith("/manager/");
            request.setAttribute("managerView", managerView);
            request.setAttribute("bookingBasePath", managerView ? "/manager/bookings" : "/reception/bookings");
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isBlank()) {
                try {
                    long bookingId = Long.parseLong(idStr);
                    BookingDao bookingDao = new BookingDao();
                    Booking booking = bookingDao.findById(bookingId).orElse(null);
                    if (booking != null) {
                        request.setAttribute("booking", booking);

                        // Lấy danh sách Guest & Email
                        String guestName = "";
                        String phone = "";
                        String email = "";
                        try (Connection conn = DBConnectionUtil.getConnection()) {
                            try (PreparedStatement ps = conn.prepareStatement("SELECT bg.full_name, bg.phone, a.email FROM booking_guests bg LEFT JOIN bookings b ON bg.booking_id = b.id LEFT JOIN accounts a ON b.customer_id = a.id WHERE bg.booking_id = ? AND bg.is_primary_guest = 1 LIMIT 1")) {
                                ps.setLong(1, bookingId);
                                try (ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        guestName = rs.getString("full_name");
                                        phone = rs.getString("phone");
                                        email = rs.getString("email");
                                    }
                                }
                            }

                            // Lấy danh sách phòng
                            List<Map<String, Object>> bookedRooms = new ArrayList<>();
                            try (PreparedStatement ps = conn.prepareStatement("SELECT r.room_number, rt.name as room_type, br.price_per_night, br.number_of_nights, br.subtotal FROM booking_rooms br JOIN rooms r ON br.room_id = r.id JOIN room_types rt ON r.room_type_id = rt.id WHERE br.booking_id = ?")) {
                                ps.setLong(1, bookingId);
                                try (ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        Map<String, Object> roomMap = new HashMap<>();
                                        roomMap.put("roomNumber", rs.getString("room_number"));
                                        roomMap.put("roomType", rs.getString("room_type"));
                                        roomMap.put("pricePerNight", rs.getBigDecimal("price_per_night"));
                                        roomMap.put("nights", rs.getInt("number_of_nights"));
                                        roomMap.put("subtotal", rs.getBigDecimal("subtotal"));
                                        bookedRooms.add(roomMap);
                                    }
                                }
                            }
                            request.setAttribute("bookedRooms", bookedRooms);
                        }
                        request.setAttribute("guestName", guestName);
                        request.setAttribute("phone", phone);
                        request.setAttribute("email", email);

                        // Lấy thông tin hoàn tiền nếu có
                        BookingRefundDao refundDao = new BookingRefundDao();
                        Map<String, Object> refundRequest = refundDao.findByBookingId(bookingId);
                        request.setAttribute("refundRequest", refundRequest);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            view = "/WEB-INF/views/reception/booking-detail.jsp";
        } else if ("/reception/walk-in".equals(path)) {
            view = "/WEB-INF/views/reception/walk-in-booking.jsp";
        }

        request.getRequestDispatcher(view).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/reception/bookings");
    }
}
