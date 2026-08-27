package controller.page.manager;

import dao.NewsDao;
import model.Account;
import model.News;
import util.LocalFileUtil;
import util.MultipartUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import service.AuditLogService;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 5 * 1024 * 1024,         // 5 MB
        maxRequestSize = 10 * 1024 * 1024      // 10 MB
)
@WebServlet(name = "NewsManagementServlet", urlPatterns = {"/manager/news", "/manager/news/create", "/manager/news/edit", "/manager/news/delete"})
public class NewsManagementServlet extends HttpServlet {

    private static final String NEWS_IMAGE_DIR = "uploads/news";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private NewsDao newsDao;
    private AuditLogService auditLogService;

    @Override
    public void init() throws ServletException {
        newsDao = new NewsDao();
        auditLogService = new AuditLogService();
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
                        session.setAttribute("error", "Không tìm thấy bài viết.");
                        response.sendRedirect(request.getContextPath() + "/manager/news");
                        return;
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("error", "ID bài viết không hợp lệ.");
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
                    Optional<News> newsToDelete = newsDao.getNewsById(id);
                    boolean deleted = newsDao.deleteNews(id);
                    if (deleted) {
                        newsToDelete.ifPresent(n -> {
                            if (n.getThumbnailUrl() != null && n.getThumbnailUrl().startsWith("/uploads/")) {
                                LocalFileUtil.deleteByWebPath(getServletContext(), n.getThumbnailUrl());
                            }
                        });
                        auditLogService.log(request, "DELETE_NEWS", "NEWS", id, "Deleted news " + id);
                        session.setAttribute("message", "Đã xóa bài viết thành công.");
                    } else {
                        session.setAttribute("error", "Xóa bài viết thất bại.");
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("error", "ID không hợp lệ.");
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
            if (idStr != null && !idStr.isBlank()) {
                try { news.setId(Long.parseLong(idStr)); } catch (Exception ignored) {}
            }
            news.setTitle(title);
            news.setContent(content);
            news.setThumbnailUrl(thumbnailUrl);
            news.setStatus(status);
            request.setAttribute("news", news);
            request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
            return;
        }

        // Handle Local Image Upload
        Part thumbnailPart = null;
        try {
            thumbnailPart = request.getPart("thumbnailFile");
            if (thumbnailPart != null && thumbnailPart.getSize() > 0) {
                MultipartUtil.validateImagePart(
                        thumbnailPart,
                        MAX_IMAGE_SIZE,
                        ALLOWED_IMAGE_EXTENSIONS,
                        ALLOWED_IMAGE_CONTENT_TYPES,
                        "Ảnh đại diện bài viết"
                );
            }
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            News news = new News();
            if (idStr != null && !idStr.isBlank()) {
                try { news.setId(Long.parseLong(idStr)); } catch (Exception ignored) {}
            }
            news.setTitle(title);
            news.setContent(content);
            news.setThumbnailUrl(thumbnailUrl);
            news.setStatus(status);
            request.setAttribute("news", news);
            request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
            return;
        } catch (Exception ignored) {
        }

        String savedImagePath = null;
        if (thumbnailPart != null && thumbnailPart.getSize() > 0) {
            try {
                savedImagePath = LocalFileUtil.saveImagePart(
                        thumbnailPart,
                        getServletContext(),
                        NEWS_IMAGE_DIR,
                        "news"
                );
            } catch (IOException ex) {
                request.setAttribute("error", "Không thể lưu file ảnh tải lên: " + ex.getMessage());
                News news = new News();
                if (idStr != null && !idStr.isBlank()) {
                    try { news.setId(Long.parseLong(idStr)); } catch (Exception ignored) {}
                }
                news.setTitle(title);
                news.setContent(content);
                news.setThumbnailUrl(thumbnailUrl);
                news.setStatus(status);
                request.setAttribute("news", news);
                request.getRequestDispatcher("/WEB-INF/views/manager/news-form.jsp").forward(request, response);
                return;
            }
        }

        News news = new News();
        news.setTitle(title.trim());
        news.setContent(content != null ? content : "");
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
                    if (savedImagePath != null) {
                        news.setThumbnailUrl(savedImagePath);
                        if (existingNews.getThumbnailUrl() != null && existingNews.getThumbnailUrl().startsWith("/uploads/")) {
                            LocalFileUtil.deleteByWebPath(getServletContext(), existingNews.getThumbnailUrl());
                        }
                    } else if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
                        news.setThumbnailUrl(thumbnailUrl.trim());
                    } else {
                        news.setThumbnailUrl(existingNews.getThumbnailUrl());
                    }

                    if ("PUBLISHED".equals(status)) {
                        if (existingNews.getPublishedAt() != null) {
                            news.setPublishedAt(existingNews.getPublishedAt());
                        } else {
                            news.setPublishedAt(new Timestamp(System.currentTimeMillis()));
                        }
                    } else {
                        news.setPublishedAt(existingNews.getPublishedAt());
                    }
                } else {
                    if (savedImagePath != null) {
                        news.setThumbnailUrl(savedImagePath);
                    }
                    if ("PUBLISHED".equals(status)) {
                        news.setPublishedAt(new Timestamp(System.currentTimeMillis()));
                    }
                }

                success = newsDao.updateNews(news);
                if (success) {
                    auditLogService.log(request, "UPDATE_NEWS", "NEWS", news.getId(), "Updated news " + news.getTitle());
                    session.setAttribute("message", "Cập nhật bài viết thành công!");
                }
            } catch (NumberFormatException e) {
                // error
            }
        } else {
            // Create
            if (savedImagePath != null) {
                news.setThumbnailUrl(savedImagePath);
            } else if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
                news.setThumbnailUrl(thumbnailUrl.trim());
            }
            if ("PUBLISHED".equals(status)) {
                news.setPublishedAt(new Timestamp(System.currentTimeMillis()));
            }
            news = newsDao.insertNews(news);
            success = news.getId() != null && news.getId() > 0;
            if (success) {
                auditLogService.log(request, "CREATE_NEWS", "NEWS", news.getId(), "Created news " + news.getTitle());
                session.setAttribute("message", "Thêm bài viết mới thành công!");
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
