package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/profile", "/change-password"})
public class AccountServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (currentUser(request, response) == null) return;
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request, response);
        if (user == null) return;
        try {
            if ("/profile".equals(request.getServletPath())) {
                userService.updateProfile(user, request.getParameter("fullName"), request.getParameter("phone"));
                request.setAttribute("success", "Cáº­p nháº­t há»“ sÆ¡ thÃ nh cÃ´ng.");
            } else {
                userService.changePassword(user, request.getParameter("currentPassword"),
                        request.getParameter("password"), request.getParameter("confirmPassword"));
                request.setAttribute("success", "Äá»•i máº­t kháº©u thÃ nh cÃ´ng.");
            }
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        } catch (SQLException ex) {
            getServletContext().log("Cáº­p nháº­t tÃ i khoáº£n tháº¥t báº¡i", ex);
            request.setAttribute("error", "Há»‡ thá»‘ng Ä‘ang báº­n. Vui lÃ²ng thá»­ láº¡i sau.");
        }
        forward(request, response);
    }

    private User currentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = "/profile".equals(request.getServletPath()) ? "profile.jsp" : "change-password.jsp";
        request.getRequestDispatcher("/WEB-INF/views/public/" + view).forward(request, response);
    }
}

