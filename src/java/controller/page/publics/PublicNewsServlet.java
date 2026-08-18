package controller.page.publics;

import dao.NewsDao;
import model.News;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "PublicNewsServlet", urlPatterns = {"/news", "/news/detail"})
public class PublicNewsServlet extends HttpServlet {

    private NewsDao newsDao;

    @Override
    public void init() throws ServletException {
        newsDao = new NewsDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.equals("/news/detail")) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    long id = Long.parseLong(idStr);
                    Optional<News> newsOpt = newsDao.getNewsById(id);
                    if (newsOpt.isPresent() && "PUBLISHED".equals(newsOpt.get().getStatus())) {
                        request.setAttribute("news", newsOpt.get());
                        
                        // Fetch some related news
                        List<News> latestNews = newsDao.getLatestNews(3);
                        latestNews.removeIf(n -> n.getId().equals(id));
                        request.setAttribute("relatedNews", latestNews);

                        request.getRequestDispatcher("/WEB-INF/views/public/news-detail.jsp").forward(request, response);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "News not found");
            return;
        }

        // List Published News
        int page = 1;
        int limit = 9; // 9 cards per page for grid
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int offset = (page - 1) * limit;

        List<News> newsList = newsDao.getPublishedNews(offset, limit);
        int totalRecords = newsDao.getTotalPublishedNewsCount();
        int totalPages = (int) Math.ceil((double) totalRecords / limit);

        request.setAttribute("newsList", newsList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/WEB-INF/views/public/news.jsp").forward(request, response);
    }
}
