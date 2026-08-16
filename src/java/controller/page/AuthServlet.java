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
        String path = request.getServletPath();
        if ("/logout".equals(path)) {
            logout(request, response);
            return;
        }
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        if ("/register".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/public/register.jsp").forward(request, response);
            return;
        }
        if ("/forgot-password".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/public/forgot-password.jsp").forward(request, response);
            return;
        }
        if ("/reset-password".equals(path)) {
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
        String path = request.getServletPath();
        if ("/logout".equals(path)) {
            logout(request, response);
            return;
        }
        if ("/register".equals(path)) {
            register(request, response);
            return;
        }
        if ("/forgot-password".equals(path)) {
            requestPasswordReset(request, response);
            return;
        }
        if ("/reset-password".equals(path)) {
            resetPassword(request, response);
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        request.setAttribute("email", email == null ? "" : email.trim());

        try {
            Optional<User> authenticated = userService.authenticate(email, password);
            if (authenticated.isEmpty()) {
                request.setAttribute("error", "Email, mật khẩu không đúng hoặc tài khoản đã bị khóa.");
                request.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(request, response);
                return;
            }

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

            response.sendRedirect(returnUrl == null ? request.getContextPath() + "/" : returnUrl);
        } catch (SQLException ex) {
            getServletContext().log("Đăng nhập thất bại do lỗi cơ sở dữ liệu", ex);
            request.setAttribute("error", "Hệ thống đang bận. Vui lòng thử lại sau.");
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
            request.setAttribute("success", "Nếu email tồn tại, hệ thống đã gửi liên kết đặt lại mật khẩu.");
            request.getRequestDispatcher("/WEB-INF/views/public/forgot-password.jsp").forward(request, response);
        } catch (SQLException | IllegalStateException | IOException ex) {
            getServletContext().log("Gửi email đặt lại mật khẩu thất bại", ex);
            request.setAttribute("error", "Hệ thống đang bận. Vui lòng thử lại sau.");
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
            getServletContext().log("Đặt lại mật khẩu thất bại", ex);
            request.setAttribute("error", "Hệ thống đang bận. Vui lòng thử lại sau.");
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
            getServletContext().log("Đăng ký thất bại do lỗi cơ sở dữ liệu", ex);
            request.setAttribute("error", "Không thể tạo tài khoản. Vui lòng thử lại sau.");
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
