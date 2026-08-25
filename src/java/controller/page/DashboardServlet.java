//file:noinspection SpellCheckingInspection
package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.DashboardService;

import java.io.IOException;
import java.io.Serial;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/dashboard"})
@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class DashboardServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private DashboardService dashboardService;

    @Override
    public void init() {
        dashboardService = new DashboardService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = null;
        if (session != null) {
            Object currentUser = session.getAttribute("currentUser");
            if (currentUser instanceof User) {
                user = (User) currentUser;
            }
        }
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if ("CUSTOMER".equalsIgnoreCase(user.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            request.setAttribute("dashboardStats", dashboardService.loadStats(user.getUserId(), user.getRoleName()));
            request.getRequestDispatcher("/WEB-INF/views/common/dashboard.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("Cannot load dashboard", ex);
            request.setAttribute("dashboardError", "Không thể tải số liệu dashboard. Vui lòng kiểm tra kết nối CSDL.");
            request.getRequestDispatcher("/WEB-INF/views/common/dashboard.jsp").forward(request, response);
        }
    }
}
