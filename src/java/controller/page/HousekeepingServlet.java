package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.HousekeepingTask;
import model.User;
import service.HousekeepingService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {
        "/housekeeping/tasks", "/housekeeping/tasks/detail",
        "/manager/housekeeping", "/manager/housekeeping/detail",
        "/housekeeping/tasks/claim", "/housekeeping/tasks/claim-cleaning",
        "/housekeeping/tasks/complete-inspection",
        "/housekeeping/tasks/start-cleaning", "/housekeeping/tasks/complete-cleaning",
        "/housekeeping/tasks/save-progress"
})
public class HousekeepingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int ROLE_HOUSEKEEPING = 4;
    private static final int ROLE_MANAGER = 5;
    private HousekeepingService service;

    @Override
    public void init() {
        service = new HousekeepingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentStaff(request, response);
        if (user == null) return;
        try {
            service.syncDatabaseState();
            String path = request.getServletPath();
            boolean manager = user.getRoleId() == ROLE_MANAGER || path.startsWith("/manager/");

            if (manager && "/housekeeping/tasks".equals(path)) {
                String qs = request.getQueryString();
                response.sendRedirect(request.getContextPath() + "/manager/housekeeping" + (qs != null && !qs.isBlank() ? "?" + qs : ""));
                return;
            }
            if (manager && "/housekeeping/tasks/detail".equals(path)) {
                String qs = request.getQueryString();
                response.sendRedirect(request.getContextPath() + "/manager/housekeeping/detail" + (qs != null && !qs.isBlank() ? "?" + qs : ""));
                return;
            }

            if ("/housekeeping/tasks/detail".equals(path) || "/manager/housekeeping/detail".equals(path)) {
                showDetail(request, response, user);
            } else {
                showList(request, response, user);
            }
        } catch (SQLException ex) {
            getServletContext().log("Không thể tải dữ liệu dọn phòng", ex);
            response.sendError(500, "Không thể tải dữ liệu dọn phòng.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentStaff(request, response);
        if (user == null) return;
        if (user.getRoleId() == ROLE_MANAGER) {
            response.sendError(403, "Quản lý chỉ có quyền xem và giám sát công việc.");
            return;
        }

        try {
            service.syncDatabaseState();
            String path = request.getServletPath();
            long taskId;
            if (path.endsWith("/claim")) {
                taskId = service.claimInspection(parseLong(request.getParameter("bookingRoomId")), user.getUserId());
                response.sendRedirect(request.getContextPath() + "/housekeeping/tasks/detail?id=" + taskId);
                return;
            }
            if (path.endsWith("/claim-cleaning")) {
                taskId = service.claimCleaning(parseLong(request.getParameter("taskId")), user.getUserId());
                response.sendRedirect(request.getContextPath() + "/housekeeping/tasks/detail?id=" + taskId);
                return;
            }
            taskId = parseLong(request.getParameter("taskId"));
            if (path.endsWith("/save-progress")) {
                String[] completedItems = request.getParameterValues("completedItems");
                if (completedItems == null) {
                    completedItems = request.getParameterValues("completedItem");
                }
                List<String> completedList = completedItems != null ? List.of(completedItems) : List.of();
                service.saveCleaningProgress(taskId, user.getUserId(), completedList);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":true}");
                return;
            }
            if (path.endsWith("/complete-inspection")) {
                HousekeepingTask task = service.getTaskDetail(taskId, user.getUserId(), false).orElseThrow();
                List<HousekeepingTask.EquipmentCheck> checks = parseChecks(request,
                        service.getEquipment(task.getRoomId(), task.getBookingRoomId()));
                service.completeInspection(taskId, user.getUserId(), checks,
                        parameterValues(request, "cleaningItem"),
                        request.getParameter("customCleaningTasks"),
                        request.getParameter("inspectionNote"));
            } else if (path.endsWith("/start-cleaning")) {
                service.startCleaning(taskId, user.getUserId());
                response.sendRedirect(request.getContextPath() + "/housekeeping/tasks/detail?id=" + taskId);
                return;
            } else if (path.endsWith("/complete-cleaning")) {
                service.completeCleaning(taskId, user.getUserId());
            } else {
                response.sendError(400, "Hành động không hợp lệ.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/housekeeping/tasks?view=mine");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            response.sendError(409, ex.getMessage());
        } catch (SQLException ex) {
            getServletContext().log("Không thể xử lý công việc dọn phòng", ex);
            response.sendError(500, "Lỗi cơ sở dữ liệu khi cập nhật công việc.");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {
        boolean manager = user.getRoleId() == ROLE_MANAGER || request.getServletPath().startsWith("/manager/");
        String requestedView = manager ? "history" : request.getParameter("view");
        HousekeepingService.TaskPage result = service.getTaskPage(user.getUserId(), manager,
                requestedView, request.getParameter("q"),
                parseNullableInt(request.getParameter("floor")), request.getParameter("taskType"),
                request.getParameter("status"), request.getParameter("sort"),
                request.getParameter("direction"), parseInt(request.getParameter("page"), 1));
        request.setAttribute("result", result);
        request.setAttribute("isManager", manager);
        request.setAttribute("floorOptions", new dao.RoomDao().getDistinctFloors());
        request.setAttribute("maxFloor", new dao.RoomDao().getMaxFloor());
        request.getRequestDispatcher("/WEB-INF/views/housekeeping/task-list.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {
        long taskId = parseLong(request.getParameter("id"));
        boolean manager = user.getRoleId() == ROLE_MANAGER || request.getServletPath().startsWith("/manager/");
        Optional<HousekeepingTask> task = service.getTaskDetail(taskId, user.getUserId(), manager);
        if (task.isEmpty()) {
            response.sendError(404, "Không tìm thấy công việc.");
            return;
        }
        HousekeepingTask item = task.get();
        request.setAttribute("task", item);
        request.setAttribute("isManager", manager);
        request.setAttribute("workItems", service.getWorkItems(item.getNote()));
        request.setAttribute("inspectionMessage", service.getInspectionMessage(item.getNote()));
        boolean history = "COMPLETED".equals(item.getStatus()) || "CANCELLED".equals(item.getStatus());
        request.setAttribute("history", history);
        if ("CHECKOUT_INSPECTION".equals(item.getTaskType()) && (history || manager)) {
            List<HousekeepingTask.EquipmentCheck> results = service.getInspectionResults(taskId);
            if (results.isEmpty()) {
                results = service.getEquipment(item.getRoomId(), item.getBookingRoomId());
            }
            request.setAttribute("equipment", results);
        } else if ("CHECKOUT_INSPECTION".equals(item.getTaskType())) {
            request.setAttribute("equipment", service.getEquipment(
                    item.getRoomId(), item.getBookingRoomId()));
            request.setAttribute("cleaningChecklist", service.getCleaningChecklist());
        } else if ("CLEANING".equals(item.getTaskType())) {
            request.setAttribute("equipment", service.getCleaningEquipment(taskId));
        }
        request.getRequestDispatcher("/WEB-INF/views/housekeeping/task-detail.jsp").forward(request, response);
    }

    private List<String> parameterValues(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        return values == null ? List.of() : List.of(values);
    }

            private List<HousekeepingTask.EquipmentCheck> parseChecks(HttpServletRequest request,
                                                              List<HousekeepingTask.EquipmentCheck> equipment) {
        List<HousekeepingTask.EquipmentCheck> checks = new ArrayList<>();
        for (HousekeepingTask.EquipmentCheck source : equipment) {
            String suffix = String.valueOf(source.getRoomEquipmentId());
            String condition = request.getParameter("condition_" + suffix);
            if (condition == null || condition.isBlank()) {
                condition = "NORMAL";
            }
            String note = request.getParameter("note_" + suffix);
            BigDecimal fee = BigDecimal.ZERO;
            HousekeepingTask.EquipmentCheck check = new HousekeepingTask.EquipmentCheck();
            check.setRoomEquipmentId(source.getRoomEquipmentId());
            check.setEquipmentName(source.getEquipmentName());
            check.setQuantity(source.getQuantity() > 0 ? source.getQuantity() : 1);
            check.setConditionStatus(condition);
            check.setDamageFee(fee);
            check.setNote(note);
            checks.add(check);
        }
        return checks;
    }
private User currentStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            String target = request.getRequestURI()
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            response.sendRedirect(request.getContextPath() + "/login?returnUrl="
                    + URLEncoder.encode(target, StandardCharsets.UTF_8));
            return null;
        }
        if (user.getRoleId() != ROLE_HOUSEKEEPING && user.getRoleId() != ROLE_MANAGER) {
            response.sendError(403, "Bạn không có quyền truy cập chức năng dọn phòng.");
            return null;
        }
        return user;
    }

    private long parseLong(String value) {
        try {
            long result = Long.parseLong(value);
            if (result <= 0) throw new NumberFormatException();
            return result;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Mã công việc không hợp lệ.");
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            int result = Integer.parseInt(value);
            return result > 0 ? result : defaultValue;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private Integer parseNullableInt(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}