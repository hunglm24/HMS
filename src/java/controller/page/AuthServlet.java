package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.UserService;
import util.MailUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/login", "/register", "/forgot-password", "/reset-password", "/logout"})
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getServletPath().equals("/logout")) {
            logout(request, response);
            return;
        }
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        if (request.getServletPath().equals("/register")) {
            request.getRequestDispatcher("/WEB-INF/views/public/register.jsp").forward(request, response);
            return;
        }
        if (request.getServletPath().equals("/forgot-password")) {
            request.getRequestDispatcher("/WEB-INF/views/public/forgot-password.jsp").forward(request, response);
            return;
        }
        if (request.getServletPath().equals("/reset-password")) {
            request.setAttribute("token", request.getParameter("token"));
            request.getRequestDispatcher("/WEB-INF/views/public/reset-password.jsp").forward(request, response);
            return;
        }
        String returnUrl = validReturnUrl(request, request.getParameter("returnUrl"));
        if (returnUrl != null) {
            request.getSession(true).setAttribute("loginReturnUrl", returnUrl);
        }
        request.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getServletPath().equals("/logout")) {
            logout(request, response);
            return;
        }
        if (request.getServletPath().equals("/register")) {
            register(request, response);
            return;
        }
        if (request.getServletPath().equals("/forgot-password")) {
            requestPasswordReset(request, response);
            return;
        }
        if (request.getServletPath().equals("/reset-password")) {
            resetPassword(request, response);
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        request.setAttribute("email", email == null ? "" : email.trim());

        try {
            Optional<User> authenticated = userService.authenticate(email, password);
            if (authenticated.isEmpty()) {
                request.setAttribute("error", "Email, máº­t kháº©u khÃ´ng Ä‘Ãºng hoáº·c tÃ i khoáº£n Ä‘Ã£ bá»‹ khÃ³a.");
                request.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(request, response);
                return;
            }

            // Chá»‘ng session fixation: bá» session cÅ© trÆ°á»›c khi táº¡o session Ä‘Äƒng nháº­p.
            HttpSession oldSession = request.getSession(false);
            String returnUrl = oldSession == null ? null
                    : (String) oldSession.getAttribute("loginReturnUrl");
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            User currentUser = authenticated.get();
            currentUser.setPasswordHash(null);
            session.setAttribute("currentUser", currentUser);
            session.setMaxInactiveInterval(30 * 60);

            String defaultUrl = request.getContextPath() + "/";
            response.sendRedirect(returnUrl == null ? defaultUrl : returnUrl);
        } catch (SQLException ex) {
            getServletContext().log("ÄÄƒng nháº­p tháº¥t báº¡i do lá»—i cÆ¡ sá»Ÿ dá»¯ liá»‡u", ex);
            request.setAttribute("error", "Há»‡ thá»‘ng Ä‘ang báº­n. Vui lÃ²ng thá»­ láº¡i sau.");
            request.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(request, response);
        }
    }

    private void requestPasswordReset(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String email = request.getParameter("email");
            Optional<String> token = userService.createPasswordResetToken(email);
            if (token.isPresent()) {
                String resetUrl = request.getScheme() + "://" + request.getServerName()
                        + ((request.getServerPort() == 80 || request.getServerPort() == 443)
                        ? "" : ":" + request.getServerPort())
                        + request.getContextPath() + "/reset-password?token="
                        + java.net.URLEncoder.encode(token.get(), java.nio.charset.StandardCharsets.UTF_8);
                MailUtil.sendPasswordReset(email.trim(), resetUrl);
            }
            request.setAttribute("success", "Náº¿u email tá»“n táº¡i, há»‡ thá»‘ng Ä‘Ã£ gá»­i liÃªn káº¿t Ä‘áº·t láº¡i máº­t kháº©u.");
            request.getRequestDispatcher("/WEB-INF/views/public/forgot-password.jsp").forward(request, response);
        } catch (SQLException | IllegalStateException | IOException ex) {
            getServletContext().log("Gá»­i email Ä‘áº·t láº¡i máº­t kháº©u tháº¥t báº¡i", ex);
            request.setAttribute("error", "Há»‡ thá»‘ng Ä‘ang báº­n. Vui lÃ²ng thá»­ láº¡i sau.");
            request.getRequestDispatcher("/WEB-INF/views/public/forgot-password.jsp").forward(request, response);
        }
    }

    private void resetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            userService.resetPasswordWithToken(request.getParameter("token"),
                    request.getParameter("password"), request.getParameter("confirmPassword"));
            response.sendRedirect(request.getContextPath() + "/login?reset=1");
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            request.setAttribute("token", request.getParameter("token"));
            request.getRequestDispatcher("/WEB-INF/views/public/reset-password.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("Äáº·t láº¡i máº­t kháº©u tháº¥t báº¡i", ex);
            request.setAttribute("error", "Há»‡ thá»‘ng Ä‘ang báº­n. Vui lÃ²ng thá»­ láº¡i sau.");
            request.getRequestDispatcher("/WEB-INF/views/public/reset-password.jsp").forward(request, response);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        request.setAttribute("fullName", fullName == null ? "" : fullName.trim());
        request.setAttribute("email", email == null ? "" : email.trim());
        request.setAttribute("phone", phone == null ? "" : phone.trim());

        try {
            User user = userService.register(fullName, email, phone,
                    request.getParameter("password"), request.getParameter("confirmPassword"));
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            user.setPasswordHash(null);
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", user);
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/");
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/public/register.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("ÄÄƒng kÃ½ tháº¥t báº¡i do lá»—i cÆ¡ sá»Ÿ dá»¯ liá»‡u", ex);
            request.setAttribute("error", "KhÃ´ng thá»ƒ táº¡o tÃ i khoáº£n. Vui lÃ²ng thá»­ láº¡i sau.");
            request.getRequestDispatcher("/WEB-INF/views/public/register.jsp").forward(request, response);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private String validReturnUrl(HttpServletRequest request, String returnUrl) {
        String contextPath = request.getContextPath();
        if (returnUrl != null && returnUrl.startsWith(contextPath + "/")
                && !returnUrl.startsWith("//") && !returnUrl.contains("\\")) {
            return returnUrl;
        }
        return null;
    }
}

