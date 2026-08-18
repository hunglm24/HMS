package controller.customer;

import dao.BookingDao;
import dao.FeedbackDao;
import model.Booking;
import model.Feedback;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CustomerFeedbackServlet", urlPatterns = {"/customer/feedback"})
public class CustomerFeedbackServlet extends HttpServlet {

    private final BookingDao bookingDao = new BookingDao();
    private final FeedbackDao feedbackDao = new FeedbackDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null || !"CUSTOMER".equals(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String bookingIdStr = request.getParameter("bookingId");
        if (bookingIdStr == null || bookingIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/my-bookings");
            return;
        }

        try {
            long bookingId = Long.parseLong(bookingIdStr);
            Booking booking = bookingDao.findById(bookingId).orElse(null);

            if (booking == null || booking.getCustomerId() != user.getId() || !"CHECKED_OUT".equals(booking.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
            }

            if (feedbackDao.hasFeedback(bookingId, user.getId())) {
                request.setAttribute("errorMessage", "Bạn đã đánh giá đặt phòng này rồi.");
                request.getRequestDispatcher("/WEB-INF/views/public/my-bookings.jsp").forward(request, response);
                return;
            }

            request.setAttribute("booking", booking);
            request.getRequestDispatcher("/WEB-INF/views/public/feedback-form.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/my-bookings");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null || !"CUSTOMER".equals(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            long bookingId = Long.parseLong(request.getParameter("bookingId"));
            int rating = Integer.parseInt(request.getParameter("rating"));
            String comment = request.getParameter("comment");

            Booking booking = bookingDao.findById(bookingId).orElse(null);
            if (booking == null || booking.getCustomerId() != user.getId() || !"CHECKED_OUT".equals(booking.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
            }

            if (feedbackDao.hasFeedback(bookingId, user.getId())) {
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
            }

            Feedback feedback = new Feedback();
            feedback.setBookingId(bookingId);
            feedback.setCustomerId(user.getId());
            feedback.setRating(rating);
            feedback.setComment(comment);

            feedbackDao.insertFeedback(feedback);

            request.getSession().setAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá!");
            response.sendRedirect(request.getContextPath() + "/my-bookings");

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Có lỗi xảy ra, vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/my-bookings");
        }
    }
}
