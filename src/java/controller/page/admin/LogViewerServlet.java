package controller.page.admin;

import dao.AuditLogDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/admin/logs"})
public class LogViewerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AuditLogDao auditLogDao;

    @Override
    public void init() {
        auditLogDao = new AuditLogDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String keyword = request.getParameter("q");
            int limit = parseLimit(request.getParameter("limit"));
            request.setAttribute("logs", auditLogDao.findRecent(limit, keyword));
            request.setAttribute("q", keyword == null ? "" : keyword);
            request.setAttribute("limit", limit);
        } catch (SQLException ex) {
            getServletContext().log("Cannot load system logs", ex);
            request.setAttribute("error", "Cannot load logs. Check database connection.");
        }
        request.getRequestDispatcher("/WEB-INF/views/admin/logs.jsp").forward(request, response);
    }

    private int parseLimit(String value) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException ex) {
            return 100;
        }
    }
}

