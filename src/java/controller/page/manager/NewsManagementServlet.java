package controller.page.manager;

import dao.NewsDao;
import model.Account;
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
import java.util.Optional;

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
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Account currentUser = (Account) session.getAttribute("currentUser");
        if (!"HOTEL_MANAGER".equalsIgnoreCase(currentUser.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Truy cập bị từ chối: Chỉ Quản lý khách sạn mới có quyền.");
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
                    Optional<News> newsOpt = newsDao.getNewsById(id);
                    if (newsOpt.isPresent()) {
                        request.setAttribute("news", newsOpt.get());
                    } else {
                        session.setAttribute("toastMessage", "Không tìm thấy bài viết.");
                        session.setAttribute("toastType", "toast-error");
                        response.sendRedirect(request.getContextPath() + "/manager/news");
                        return;
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("toastMessage", "ID bài viết không hợp lệ.");
                    session.setAttribute("toastType", "toast-error");
                    response.sendRedirect(request.getContextPath() + "/manager/news");
                    return;
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
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        String search = request.getParameter("search");
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            status = "ALL";
        }

        int offset = (page - 1) * limit;

        List<News> newsList = newsDao.getAllNews(search, status, offset, limit);
        int totalRecords = newsDao.getTotalNewsCount(search, status);
        int totalPages = (int) Math.ceil((double) totalRecords / limit);

        request.setAttribute("newsList", newsList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("search", search != null ? search : "");
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
        Account currentUser = (Account) session.getAttribute("currentUser");
        if (!"HOTEL_MANAGER".equalsIgnoreCase(currentUser.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Truy cập bị từ chối: Chỉ Quản lý khách sạn mới có quyền.");
            return;
        }
        Long accountId = currentUser.getId();

        String path = request.getServletPath();

        if (path.equals("/manager/news/delete")) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    long id = Long.parseLong(idStr);
                    boolean deleted = newsDao.deleteNews(id);
                    if (deleted) {
                        session.setAttribute("toastMessage", "Đã xóa bài viết thành công.");
                        session.setAttribute("toastType", "toast-success");
                    } else {
                        session.setAttribute("toastMessage", "Xóa bài viết thất bại.");
                        session.setAttribute("toastType", "toast-error");
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("toastMessage", "ID không hợp lệ.");
                    session.setAttribute("toastType", "toast-error");
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

        if (title == null || title.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập tiêu đề bài viết.");
            News news = new News();
            news.setTitle(title);
            news.setContent(content);
            news.setThumbnailUrl(thumbnailUrl);
            news.setStatus(status);
            request.setAttribute("news", news);
            request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
            return;
        }

        News news = new News();
        news.setTitle(title.trim());
        news.setContent(content != null ? content : "");
        news.setThumbnailUrl(thumbnailUrl != null ? thumbnailUrl.trim() : null);
        news.setStatus(status != null ? status : "DRAFT");
        news.setCreatedBy(accountId);

        boolean success = false;
        if (idStr != null && !idStr.trim().isEmpty()) {
            // Update
            try {
                long id = Long.parseLong(idStr);
                news.setId(id);

                Optional<News> existingNewsOpt = newsDao.getNewsById(id);
                if (existingNewsOpt.isPresent()) {
                    News existingNews = existingNewsOpt.get();
                    if ("PUBLISHED".equals(status)) {
                        if (existingNews.getPublishedAt() != null) {
                            news.setPublishedAt(existingNews.getPublishedAt());
                        } else {
                            news.setPublishedAt(new Timestamp(System.currentTimeMillis()));
                        }
                    } else {
                        news.setPublishedAt(existingNews.getPublishedAt());
                    }
                } else if ("PUBLISHED".equals(status)) {
                    news.setPublishedAt(new Timestamp(System.currentTimeMillis()));
                }

                success = newsDao.updateNews(news);
                if (success) {
                    session.setAttribute("toastMessage", "Cập nhật bài viết thành công!");
                    session.setAttribute("toastType", "toast-success");
                }
            } catch (NumberFormatException e) {
                // error
            }
        } else {
            // Create
            if ("PUBLISHED".equals(status)) {
                news.setPublishedAt(new Timestamp(System.currentTimeMillis()));
            }
            news = newsDao.insertNews(news);
            success = news.getId() != null && news.getId() > 0;
            if (success) {
                session.setAttribute("toastMessage", "Thêm bài viết mới thành công!");
                session.setAttribute("toastType", "toast-success");
            }
        }

        if (success) {
            response.sendRedirect(request.getContextPath() + "/manager/news");
        } else {
            request.setAttribute("error", "Lưu bài viết thất bại. Vui lòng kiểm tra lại dữ liệu.");
            request.setAttribute("news", news);
            request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
        }
    }
}
