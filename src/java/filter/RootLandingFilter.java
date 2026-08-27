package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.lang.reflect.Method;

@WebFilter(urlPatterns = {"/*"})
public class RootLandingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!isRootRequest(request)) {
            chain.doFilter(request, servletResponse);
            return;
        }

        Object currentUser = getCurrentUser(request);
        String roleName = getRoleName(currentUser);

        if (roleName == null || roleName.isBlank() || "CUSTOMER".equalsIgnoreCase(roleName)) {
            request.getRequestDispatcher("/WEB-INF/views/public/home.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No-op.
    }

    private boolean isRootRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        return (contextPath != null && uri != null)
                && (uri.equals(contextPath) || uri.equals(contextPath + "/"));
    }

    private Object getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : session.getAttribute("currentUser");
    }

    private String getRoleName(Object currentUser) {
        if (currentUser == null) {
            return null;
        }

        try {
            Method method = currentUser.getClass().getMethod("getRoleName");
            Object value = method.invoke(currentUser);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
}
