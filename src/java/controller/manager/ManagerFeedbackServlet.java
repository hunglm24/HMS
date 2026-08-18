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
            java.util.List<FeedbackDao.FeedbackDto> feedbacks = feedbackDao.findAllFeedbacks();
            request.setAttribute("feedbacks", feedbacks);
            request.getRequestDispatcher("/WEB-INF/views/manager/feedback-list.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
