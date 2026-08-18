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
        String role = user.getRoleName();
        String path = request.getServletPath();

        if (!isAllowed(path, role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowed(String path, String role) {
        if (role == null) return false;
        if (path.startsWith("/admin/")) return "ADMIN".equalsIgnoreCase(role);
        if (path.startsWith("/manager/")) return "HOTEL_MANAGER".equalsIgnoreCase(role);
        if (path.startsWith("/reception/")) return "RECEPTIONIST".equalsIgnoreCase(role);
        if (path.startsWith("/technician/")) return "HOUSEKEEPING".equalsIgnoreCase(role)
                || "HOTEL_MANAGER".equalsIgnoreCase(role);
        if (path.startsWith("/housekeeping/")) return "HOUSEKEEPING".equalsIgnoreCase(role)
                || "HOTEL_MANAGER".equalsIgnoreCase(role);
        return true;
    }
}
