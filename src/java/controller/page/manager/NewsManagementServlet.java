package controller.page.manager;

import dao.NewsDao;
import model.News;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet(name = "NewsManagementServlet", urlPatterns = {"/manager/news", "/manager/news/create", "/manager/news/edit", "/manager/news/delete"})
public class NewsManagementServlet extends HttpServlet {

    private NewsDao newsDao;

    @Override
    public void init() throws ServletException {
        newsDao = new NewsDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login"); // Adjust if login route is different
            return;
        }
        model.Account currentUser = (model.Account) session.getAttribute("currentUser");
        if (!"HOTEL_MANAGER".equalsIgnoreCase(currentUser.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Only Hotel Managers can manage news.");
            return;
        }

        String path = request.getServletPath();

        if (path.equals("/manager/news/create")) {
            request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
            return;
        }

        if (path.equals("/manager/news/edit")) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    long id = Long.parseLong(idStr);
                    newsDao.getNewsById(id).ifPresent(news -> request.setAttribute("news", news));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
            return;
        }

        // List News
        int page = 1;
        int limit = 10;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        String search = request.getParameter("search");
        String status = request.getParameter("status");
        if (status == null) status = "ALL";

        int offset = (page - 1) * limit;

        List<News> newsList = newsDao.getAllNews(search, status, offset, limit);
        int totalRecords = newsDao.getTotalNewsCount(search, status);
        int totalPages = (int) Math.ceil((double) totalRecords / limit);

        request.setAttribute("newsList", newsList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("search", search);
        request.setAttribute("status", status);

        request.getRequestDispatcher("/WEB-INF/views/manager/news.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        model.Account currentUser = (model.Account) session.getAttribute("currentUser");
        if (!"HOTEL_MANAGER".equalsIgnoreCase(currentUser.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Only Hotel Managers can manage news.");
            return;
        }
        Long accountId = currentUser.getId();

        String path = request.getServletPath();

        if (path.equals("/manager/news/delete")) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    long id = Long.parseLong(idStr);
                    newsDao.deleteNews(id);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            response.sendRedirect(request.getContextPath() + "/manager/news");
            return;
        }

        // Handle Create or Update
        String idStr = request.getParameter("id");
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String thumbnailUrl = request.getParameter("thumbnailUrl");
        String status = request.getParameter("status"); // DRAFT, PUBLISHED, HIDDEN

        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setThumbnailUrl(thumbnailUrl);
        news.setStatus(status);
        news.setCreatedBy(accountId);

        if ("PUBLISHED".equals(status)) {
            news.setPublishedAt(new Timestamp(System.currentTimeMillis()));
        }

        boolean success = false;
        if (idStr != null && !idStr.isEmpty()) {
            // Update
            try {
                long id = Long.parseLong(idStr);
                news.setId(id);
                // Keep old publishedAt if it was already published and status is still PUBLISHED
                // This is simplified, ideally we check DB first.
                success = newsDao.updateNews(news);
            } catch (NumberFormatException e) {
                // error
            }
        } else {
            // Create
            news = newsDao.insertNews(news);
            success = news.getId() != null && news.getId() > 0;
        }

        if (success) {
            response.sendRedirect(request.getContextPath() + "/manager/news");
        } else {
            request.setAttribute("error", "Failed to save news.");
            request.setAttribute("news", news);
            request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
        }
    }
}
