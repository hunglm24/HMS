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
import java.util.Set;

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
        Set<String> permissionCodes = permissionCodes(session);

        if (!isAllowed(path, role, permissionCodes)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Set<String> permissionCodes(HttpSession session) {
        Object value = session.getAttribute("permissionCodes");
        return value instanceof Set ? (Set<String>) value : Set.of();
    }

    private boolean isAllowed(String path, String role, Set<String> permissionCodes) {
        if (role == null) return false;
        if (path.startsWith("/admin/users")) return "ADMIN".equalsIgnoreCase(role) || permissionCodes.contains("ADMIN_USERS");
        if (path.startsWith("/admin/roles")) return "ADMIN".equalsIgnoreCase(role) || permissionCodes.contains("ADMIN_ROLES");
        if (path.startsWith("/admin/logs")) return "ADMIN".equalsIgnoreCase(role) || permissionCodes.contains("ADMIN_LOGS");
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
