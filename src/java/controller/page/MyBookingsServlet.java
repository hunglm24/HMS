package controller.page;

import dao.BookingDao;
import dao.BookingRefundDao;
import dao.FeedbackDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Booking;
import model.CheckInBookingSummary;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/my-bookings", "/booking-detail"})
public class MyBookingsServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();
    private final BookingRefundDao refundDao = new BookingRefundDao();
    private final FeedbackDao feedbackDao = new FeedbackDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("/booking-detail".equals(request.getServletPath())) {
            String bookingIdStr = request.getParameter("id");
            if (bookingIdStr != null && !bookingIdStr.isBlank()) {
                try {
                    long bookingId = Long.parseLong(bookingIdStr);
                    Booking booking = bookingDao.findById(bookingId).orElse(null);

                    if (booking == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thông tin đặt phòng.");
                        return;
                    }

                    if (booking.getCustomerId() == null || booking.getCustomerId() != user.getId()) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập thông tin đặt phòng này.");
                        return;
                    }

                    CheckInBookingSummary summary = bookingDao.findCheckInBookingById((int) bookingId).orElse(null);
                    Map<String, Object> refundRequest = refundDao.findByBookingId(bookingId);

                    request.setAttribute("booking", booking);
                    request.setAttribute("summary", summary);
                    request.setAttribute("refundRequest", refundRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
            }
            request.getRequestDispatcher("/WEB-INF/views/public/booking-detail-guest.jsp").forward(request, response);
        } else {
            // Fetch list
            try {
                String bookingCode = request.getParameter("bookingCode");
                String status = request.getParameter("status");
                String fromDate = request.getParameter("fromDate");
                String toDate = request.getParameter("toDate");

                String pageStr = request.getParameter("page");
                int page = pageStr != null && !pageStr.isBlank() ? Integer.parseInt(pageStr) : 1;
                int limit = 10;
                int offset = (page - 1) * limit;

                List<Booking> bookings = bookingDao.findBookingsByCustomerId(user.getId(), bookingCode, status, fromDate, toDate, limit, offset);
                request.setAttribute("bookings", bookings);

                int totalRecords = bookingDao.countBookingsByCustomerId(user.getId(), bookingCode, status, fromDate, toDate);
                int totalPages = (int) Math.ceil((double) totalRecords / limit);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", totalPages);

                try {
                    Map<Long, Boolean> hasFeedbackMap = new HashMap<>();
                    List<Long> bookingIds = new ArrayList<>();
                    for (Booking b : bookings) {
                        bookingIds.add(b.getId());
                        if ("CHECKED_OUT".equals(b.getStatus())) {
                            hasFeedbackMap.put(b.getId(), feedbackDao.hasFeedback(b.getId(), user.getId()));
                        }
                    }
                    request.setAttribute("hasFeedbackMap", hasFeedbackMap);

                    Map<Long, Map<String, Object>> refundMap = refundDao.findByBookingIds(bookingIds);
                    request.setAttribute("refundMap", refundMap);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    request.setAttribute("errorMessage", "Error checking refund / feedback: " + ex.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Error loading bookings: " + e.getMessage());
            }
            request.getRequestDispatcher("/WEB-INF/views/public/my-bookings.jsp").forward(request, response);
        }
    }
}
