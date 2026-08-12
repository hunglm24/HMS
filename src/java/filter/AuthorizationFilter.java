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

@WebFilter(urlPatterns = {"/reception/*", "/housekeeping/*", "/technician/*", "/manager/*", "/admin/*"})
public class AuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("currentUser") instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        String path = request.getServletPath();
        int requiredRole = path.startsWith("/reception/") ? 1
                : path.startsWith("/housekeeping/") ? 2
                : path.startsWith("/technician/") ? 3
                : path.startsWith("/manager/") ? 4
                : path.startsWith("/admin/") ? 5 : -1;

        if (requiredRole != -1 && user.getRoleId() != requiredRole) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng này.");
            return;
        }
        chain.doFilter(request, response);
    }
}
