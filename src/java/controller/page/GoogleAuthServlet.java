package controller.page;

import dao.RoleDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.UserService;
import util.TokenUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/auth/google", "/auth/google/callback"})
public class GoogleAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String AUTHORIZE = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO = "https://openidconnect.googleapis.com/v1/userinfo";

    private final HttpClient http = HttpClient.newHttpClient();
    private UserService userService;
    private RoleDao roleDao;

    @Override
    public void init() {
        userService = new UserService();
        roleDao = new RoleDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if ("/auth/google".equals(request.getServletPath())) {
                start(request, response);
            } else {
                callback(request, response);
            }
        } catch (IllegalStateException ex) {
            getServletContext().log("Google Sign-In chưa được cấu hình", ex);
            response.sendRedirect(request.getContextPath() + "/login?googleConfig=missing");
        } catch (Exception ex) {
            getServletContext().log("Google Sign-In thất bại", ex);
            response.sendRedirect(request.getContextPath() + "/login?oauthError=1");
        }
    }

    private void start(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = TokenUtil.randomToken();
        request.getSession(true).setAttribute("googleOauthState", state);
        String url = AUTHORIZE + "?client_id=" + enc(required("HMS_GOOGLE_CLIENT_ID"))
                + "&redirect_uri=" + enc(callbackUrl(request))
                + "&response_type=code"
                + "&scope=" + enc("openid profile email")
                + "&state=" + enc(state)
                + "&prompt=select_account";
        response.sendRedirect(url);
    }

    private void callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute("googleOauthState");
        String actual = request.getParameter("state");
        if (expected == null || actual == null || !java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("OAuth state không hợp lệ");
        }
        session.removeAttribute("googleOauthState");

        String form = "code=" + enc(request.getParameter("code"))
                + "&client_id=" + enc(required("HMS_GOOGLE_CLIENT_ID"))
                + "&client_secret=" + enc(required("HMS_GOOGLE_CLIENT_SECRET"))
                + "&redirect_uri=" + enc(callbackUrl(request))
                + "&grant_type=authorization_code";
        HttpResponse<String> tokenResponse = http.send(HttpRequest.newBuilder(URI.create(TOKEN))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (tokenResponse.statusCode() != 200) {
            throw new IOException("Google token HTTP " + tokenResponse.statusCode());
        }

        String accessToken = jsonString(tokenResponse.body(), "access_token");
        HttpResponse<String> profileResponse = http.send(HttpRequest.newBuilder(URI.create(USER_INFO))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        if (profileResponse.statusCode() != 200) {
            throw new IOException("Google userinfo HTTP " + profileResponse.statusCode());
        }
        if (!jsonBoolean(profileResponse.body(), "email_verified")) {
            throw new SecurityException("Email Google chưa xác minh");
        }

        User user = userService.loginWithGoogle(
                jsonString(profileResponse.body(), "name"),
                jsonString(profileResponse.body(), "email"));
        user.setPasswordHash(null);
        session.invalidate();
        HttpSession authenticated = request.getSession(true);
        authenticated.setAttribute("currentUser", user);
        authenticated.setAttribute("permissionCodes", roleDao.findPermissionCodesForRole(user.getRoleId()));
        authenticated.setMaxInactiveInterval(30 * 60);
        response.sendRedirect(request.getContextPath() + "/");
    }

    private String callbackUrl(HttpServletRequest request) {
        int port = request.getServerPort();
        return request.getScheme() + "://" + request.getServerName()
                + ((port == 80 || port == 443) ? "" : ":" + port)
                + request.getContextPath() + "/auth/google/callback";
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Thiếu biến môi trường " + key);
        }
        return value;
    }

    private static String jsonString(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"")
                .matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Thiếu JSON field " + key);
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static boolean jsonBoolean(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false)").matcher(json);
        return matcher.find() && Boolean.parseBoolean(matcher.group(1));
    }
}
