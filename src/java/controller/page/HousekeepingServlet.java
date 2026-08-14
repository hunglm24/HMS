package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HousekeepingTask;
import model.User;
import service.HousekeepingService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/housekeeping/tasks", "/housekeeping/tasks/detail"})
public class HousekeepingServlet extends HttpServlet {
    private static final int ROLE_HOUSEKEEPING = 2;
    private static final int ROLE_MANAGER = 4;
    private HousekeepingService housekeepingService;

    @Override
    public void init() {
        housekeepingService = new HousekeepingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = (User) request.getSession(false).getAttribute("currentUser");
        boolean manager = currentUser.getRoleId() == ROLE_MANAGER;
        if (!manager && currentUser.getRoleId() != ROLE_HOUSEKEEPING) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập chức năng Housekeeping.");
            return;
        }

        try {
            if (request.getServletPath().endsWith("/detail")) {
                showDetail(request, response, currentUser, manager);
            } else {
                showList(request, response, currentUser, manager);
            }
        } catch (SQLException ex) {
            getServletContext().log("Không thể tải dữ liệu Housekeeping", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải dữ liệu Housekeeping. Vui lòng thử lại.");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response,
                          User currentUser, boolean manager)
            throws SQLException, ServletException, IOException {
        HousekeepingService.TaskPage result = housekeepingService.getTaskPage(
                request.getParameter("q"), request.getParameter("taskStatus"),
                request.getParameter("roomStatus"), parseNullableInt(request.getParameter("assignedTo")),
                currentUser.getUserId(), manager, request.getParameter("sort"),
                request.getParameter("direction"), parsePositiveInt(request.getParameter("page"), 1));

        request.setAttribute("result", result);
        request.setAttribute("isManager", manager);
        request.setAttribute("filterQuery", buildFilterQuery(result));
        request.setAttribute("baseFilterQuery", buildBaseFilterQuery(result));
        if (manager) request.setAttribute("housekeepingStaff", housekeepingService.getHousekeepingStaff());
        request.getRequestDispatcher("/WEB-INF/views/housekeeping/task-list.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response,
                            User currentUser, boolean manager)
            throws SQLException, ServletException, IOException {
        int taskId = parsePositiveInt(request.getParameter("id"), -1);
        if (taskId < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID không hợp lệ.");
            return;
        }
        Optional<HousekeepingTask> task = housekeepingService.getTaskDetail(
                taskId, currentUser.getUserId(), manager);
        if (task.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy task hoặc bạn không có quyền xem task này.");
            return;
        }
        request.setAttribute("task", task.get());
        request.setAttribute("isManager", manager);
        request.getRequestDispatcher("/WEB-INF/views/housekeeping/task-detail.jsp").forward(request, response);
    }

    private Integer parseNullableInt(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Integer.valueOf(value); }
        catch (NumberFormatException ex) { return null; }
    }

    private int parsePositiveInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (NumberFormatException ex) { return fallback; }
    }

    private String buildFilterQuery(HousekeepingService.TaskPage result) {
        StringBuilder query = new StringBuilder(buildBaseFilterQuery(result));
        append(query, "sort", result.sort());
        append(query, "direction", result.direction());
        return query.toString();
    }

    private String buildBaseFilterQuery(HousekeepingService.TaskPage result) {
        StringBuilder query = new StringBuilder();
        append(query, "q", result.keyword());
        append(query, "taskStatus", result.taskStatus());
        append(query, "roomStatus", result.roomStatus());
        if (result.assignedTo() != null) append(query, "assignedTo", String.valueOf(result.assignedTo()));
        return query.toString();
    }

    private void append(StringBuilder query, String name, String value) {
        if (value == null) return;
        if (!query.isEmpty()) query.append('&');
        query.append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
