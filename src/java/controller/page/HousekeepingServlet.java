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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/housekeeping/tasks", "/housekeeping/tasks/detail",
        "/housekeeping/tasks/claim", "/housekeeping/tasks/complete-inspection",
        "/housekeeping/tasks/start-cleaning", "/housekeeping/tasks/complete-cleaning"})
public class HousekeepingServlet extends HttpServlet {
    private static final int ROLE_HOUSEKEEPING = 4;
    private HousekeepingService service;

    @Override public void init() { service = new HousekeepingService(); }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentStaff(request, response);
        if (user == null) return;
        try {
            if (request.getServletPath().endsWith("/detail")) showDetail(request, response, user);
            else showList(request, response, user);
        } catch (SQLException ex) {
            getServletContext().log("Không thể tải dữ liệu Dọn Phòng", ex);
            response.sendError(500, "Không thể tải dữ liệu Dọn Phòng.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentStaff(request, response);
        if (user == null) return;
        try {
            String path = request.getServletPath();
            long taskId;
            if (path.endsWith("/claim")) {
                taskId = service.claimInspection(parseLong(request.getParameter("bookingRoomId")), user.getUserId());
                response.sendRedirect(request.getContextPath() + "/housekeeping/tasks/detail?id=" + taskId);
                return;
            }
            taskId = parseLong(request.getParameter("taskId"));
            if (path.endsWith("/complete-inspection")) {
                HousekeepingTask task = service.getTaskDetail(taskId, user.getUserId()).orElseThrow();
                List<HousekeepingTask.EquipmentCheck> checks = parseChecks(request, service.getEquipment(task.getRoomId()));
                service.completeInspection(taskId, user.getUserId(), checks, request.getParameter("inspectionNote"));
            } else if (path.endsWith("/start-cleaning")) {
                service.startCleaning(taskId, user.getUserId());
            } else if (path.endsWith("/complete-cleaning")) {
                service.completeCleaning(taskId, user.getUserId());
            } else {
                response.sendError(404);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/housekeeping/tasks?view=mine");
        } catch (IllegalArgumentException | java.util.NoSuchElementException ex) {
            response.sendError(400, ex.getMessage());
        } catch (SQLException ex) {
            getServletContext().log("Không thể cập nhật công việc Dọn Phòng", ex);
            response.sendError(409, ex.getMessage());
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {
        HousekeepingService.TaskPage result = service.getTaskPage(user.getUserId(),
                request.getParameter("view"), request.getParameter("q"),
                parseNullableInt(request.getParameter("floor")), request.getParameter("taskType"),
                request.getParameter("status"), request.getParameter("sort"),
                request.getParameter("direction"), parseInt(request.getParameter("page"), 1));
        request.setAttribute("result", result);
        request.getRequestDispatcher("/WEB-INF/views/housekeeping/task-list.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {
        long taskId = parseLong(request.getParameter("id"));
        Optional<HousekeepingTask> task = service.getTaskDetail(taskId, user.getUserId());
        if (task.isEmpty()) { response.sendError(404, "Không tìm thấy công việc."); return; }
        request.setAttribute("task", task.get());
        if ("CHECKOUT_INSPECTION".equals(task.get().getTaskType())) {
            request.setAttribute("equipment", service.getEquipment(task.get().getRoomId()));
        }
        request.getRequestDispatcher("/WEB-INF/views/housekeeping/task-detail.jsp").forward(request, response);
    }

    private List<HousekeepingTask.EquipmentCheck> parseChecks(HttpServletRequest request,
                                                               List<HousekeepingTask.EquipmentCheck> equipment) {
        List<HousekeepingTask.EquipmentCheck> checks = new ArrayList<>();
        for (HousekeepingTask.EquipmentCheck source : equipment) {
            String suffix = String.valueOf(source.getRoomEquipmentId());
            HousekeepingTask.EquipmentCheck check = new HousekeepingTask.EquipmentCheck();
            check.setRoomEquipmentId(source.getRoomEquipmentId());
            check.setQuantity(source.getQuantity());
            check.setConditionStatus(request.getParameter("condition_" + suffix));
            String fee = request.getParameter("fee_" + suffix);
            try { check.setDamageFee(fee == null || fee.isBlank() ? BigDecimal.ZERO : new BigDecimal(fee)); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Phí bồi thường không hợp lệ"); }
            check.setNote(request.getParameter("note_" + suffix));
            checks.add(check);
        }
        return checks;
    }

    private User currentStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession(false).getAttribute("currentUser");
        if (user.getRoleId() != ROLE_HOUSEKEEPING) {
            response.sendError(403, "Bạn không có quyền truy cập chức năng Dọn Phòng.");
            return null;
        }
        return user;
    }

    private long parseLong(String value) {
        try { long result = Long.parseLong(value); if (result <= 0) throw new NumberFormatException(); return result; }
        catch (RuntimeException ex) { throw new IllegalArgumentException("ID không hợp lệ"); }
    }

    private int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (NumberFormatException ex) { return fallback; }
    }

    private Integer parseNullableInt(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Integer.valueOf(value); }
        catch (NumberFormatException ex) { return null; }
    }
}
