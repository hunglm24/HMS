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
    private static final int PAGE_SIZE = 5;
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
            int limit = PAGE_SIZE;
            int totalItems = auditLogDao.countRecent(keyword);
            int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) limit));
            int page = Math.min(parsePage(request.getParameter("page")), totalPages);
            request.setAttribute("logs", auditLogDao.findRecentPage(limit, (page - 1) * limit, keyword));
            request.setAttribute("q", keyword == null ? "" : keyword);
            request.setAttribute("limit", limit);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", totalItems);
        } catch (SQLException ex) {
            getServletContext().log("Cannot load system logs", ex);
            request.setAttribute("error", "Cannot load logs. Check database connection.");
        }
        request.getRequestDispatcher("/WEB-INF/views/admin/logs.jsp").forward(request, response);
    }

    private int parsePage(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (RuntimeException ex) {
            return 1;
        }
    }
}

