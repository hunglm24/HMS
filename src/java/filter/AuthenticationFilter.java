package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebFilter(urlPatterns = {"/admin/*", "/manager/*", "/reception/*", "/housekeeping/*", "/technician/*"})
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (isPublicReceptionCheckIn(request)) {
            chain.doFilter(request, servletResponse);
            return;
        }
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("currentUser") == null) {
            String target = request.getRequestURI();
            if (request.getQueryString() != null) {
                target += "?" + request.getQueryString();
            }
            response.sendRedirect(request.getContextPath() + "/login?returnUrl="
                    + URLEncoder.encode(target, StandardCharsets.UTF_8));
            return;
        }
        User currentUser = (User) session.getAttribute("currentUser");
        if ("CUSTOMER".equalsIgnoreCase(currentUser.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản khách hàng không có quyền truy cập khu vực nội bộ.");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isPublicReceptionCheckIn(HttpServletRequest request) {
        return "/reception/check-in".equals(request.getServletPath());
    }
}
