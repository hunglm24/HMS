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
        String requiredRole = path.startsWith("/reception/") ? "RECEPTIONIST"
                : path.startsWith("/housekeeping/") ? "HOUSEKEEPING"
                : path.startsWith("/technician/") ? "HOUSEKEEPING"
                : path.startsWith("/manager/") ? "HOTEL_MANAGER"
                : path.startsWith("/admin/") ? "ADMIN" : null;

        if (requiredRole != null && !requiredRole.equalsIgnoreCase(user.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng này.");
            return;
        }
        chain.doFilter(request, response);
    }
}
