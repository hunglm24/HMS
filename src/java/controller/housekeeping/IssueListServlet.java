package controller.housekeeping;

import model.Account;
import model.HousekeepingTask;
import service.MaintenanceService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "IssueListServlet", urlPatterns = {"/housekeeping/issues"})
public class IssueListServlet extends HttpServlet {
    private final MaintenanceService maintenanceService = new MaintenanceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("currentUser");
        if (account == null || (!"HOUSEKEEPING".equals(account.getRoleName()) && !"HOTEL_MANAGER".equals(account.getRoleName()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

        try {
            String keyword = request.getParameter("search");
            String floorParam = request.getParameter("floor");
            Integer floor = null;
            if (floorParam != null && !floorParam.trim().isEmpty()) {
                floor = Integer.parseInt(floorParam);
            }

            int page = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
            }

            int pageSize = 1000;
            
            String rawSort = request.getParameter("sort");
            String currentSort = "created_at";
            String sortColumn = "ht.created_at";
            if ("id".equals(rawSort)) { sortColumn = "ht.id"; currentSort = "id"; }
            else if ("room".equals(rawSort)) { sortColumn = "rm.room_number"; currentSort = "room"; }
            else if ("type".equals(rawSort)) { sortColumn = "ht.task_type"; currentSort = "type"; }
            else if ("status".equals(rawSort)) { sortColumn = "ht.status"; currentSort = "status"; }

            String direction = request.getParameter("direction");
            String sortDirection = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
            String currentDir = "ASC".equals(sortDirection) ? "asc" : "desc";

            List<HousekeepingTask> tasks = maintenanceService.findIssueTasks(keyword, floor, sortColumn, sortDirection, page, pageSize);
            int total = maintenanceService.countIssueTasks(keyword, floor);
            int totalPages = (int) Math.ceil((double) total / pageSize);

            request.setAttribute("tasks", tasks);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("search", keyword);
            request.setAttribute("floor", floorParam);
            request.setAttribute("currentSort", currentSort);
            request.setAttribute("currentDir", currentDir);

            request.getRequestDispatcher("/WEB-INF/views/housekeeping/issue-list.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("errorMessage", "Lỗi tải danh sách: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/housekeeping/tasks");
        }
    }
}
