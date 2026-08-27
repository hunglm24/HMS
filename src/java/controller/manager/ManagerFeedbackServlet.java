package controller.manager;

import dao.FeedbackDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;
import java.io.IOException;

@WebServlet(name = "ManagerFeedbackServlet", urlPatterns = {"/manager/feedbacks"})
public class ManagerFeedbackServlet extends HttpServlet {
    private final FeedbackDao feedbackDao = new FeedbackDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Account account = (Account) request.getSession().getAttribute("currentUser");
        if (account == null || !"HOTEL_MANAGER".equals(account.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String keyword = request.getParameter("keyword");
            String ratingStr = request.getParameter("rating");
            String status = request.getParameter("status");

            Integer rating = null;
            if (ratingStr != null && !ratingStr.isBlank() && !"ALL".equalsIgnoreCase(ratingStr)) {
                try { rating = Integer.parseInt(ratingStr); } catch (NumberFormatException ignored) {}
            }

            java.util.List<FeedbackDao.FeedbackDto> feedbacks = feedbackDao.findFeedbacks(keyword, rating, status);
            request.setAttribute("feedbacks", feedbacks);
            request.setAttribute("keyword", keyword != null ? keyword.trim() : "");
            request.setAttribute("rating", rating != null ? String.valueOf(rating) : "ALL");
            request.setAttribute("status", status != null && !status.isBlank() ? status : "ALL");
            request.setAttribute("avgRating", feedbackDao.getAverageRating());
            request.setAttribute("totalFeedbacks", feedbackDao.countTotalFeedbacks());
            request.setAttribute("filteredCount", feedbacks.size());
            request.getRequestDispatcher("/WEB-INF/views/manager/feedback-list.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Account account = (Account) request.getSession().getAttribute("currentUser");
        if (account == null || !"HOTEL_MANAGER".equals(account.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String idStr = request.getParameter("id");
        if ("toggleStatus".equals(action) && idStr != null && !idStr.isBlank()) {
            try {
                long feedbackId = Long.parseLong(idStr);
                String newStatus = request.getParameter("status");
                if ("VISIBLE".equals(newStatus) || "HIDDEN".equals(newStatus)) {
                    feedbackDao.updateFeedbackStatus(feedbackId, newStatus);
                    request.getSession().setAttribute("message", "Đã cập nhật trạng thái phản hồi.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            }
        }
        response.sendRedirect(request.getContextPath() + "/manager/feedbacks");
    }
}
