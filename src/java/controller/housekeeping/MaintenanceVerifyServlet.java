package controller.housekeeping;

import dao.HousekeepingDao;
import model.Account;
import model.HousekeepingTask;
import model.MaintenanceLog;
import service.MaintenanceService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "MaintenanceVerifyServlet", urlPatterns = {"/housekeeping/issues/verify", "/manager/issues/verify"})
public class MaintenanceVerifyServlet extends HttpServlet {
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final HousekeepingDao housekeepingDao = new HousekeepingDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = session == null ? null : (Account) session.getAttribute("currentUser");
        if (account == null || (!"HOUSEKEEPING".equals(account.getRoleName()) && !"HOTEL_MANAGER".equals(account.getRoleName()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

        try {
            boolean isManager = "HOTEL_MANAGER".equals(account.getRoleName()) || request.getServletPath().startsWith("/manager/");
            if (isManager && "/housekeeping/issues/verify".equals(request.getServletPath())) {
                String qs = request.getQueryString();
                response.sendRedirect(request.getContextPath() + "/manager/issues/verify" + (qs != null && !qs.isBlank() ? "?" + qs : ""));
                return;
            }

            long roomId = Long.parseLong(request.getParameter("roomId"));
            long taskId = Long.parseLong(request.getParameter("taskId"));
            
            Optional<HousekeepingTask> task = housekeepingDao.findById(taskId, account.getId(), true);

            List<HousekeepingTask.EquipmentCheck> damagedEquipments = new ArrayList<>();
            if (task.isPresent() && task.get().getRoomEquipmentId() != null) {
                damagedEquipments = maintenanceService.findDamagedEquipmentById(task.get().getRoomEquipmentId());
                if (damagedEquipments.isEmpty() && !"COMPLETED".equals(task.get().getStatus())) {
                    damagedEquipments = maintenanceService.findDamagedEquipments(roomId);
                }
            } else {
                damagedEquipments = maintenanceService.findDamagedEquipments(roomId);
            }

            List<HousekeepingTask.EquipmentCheck> allRoomEquipments = maintenanceService.findAllEquipmentsInRoom(roomId);
            List<MaintenanceLog> logs = maintenanceService.findLogsByTaskId(taskId);

            request.setAttribute("equipments", damagedEquipments);
            request.setAttribute("allRoomEquipments", allRoomEquipments);
            request.setAttribute("logs", logs);
            task.ifPresent(t -> request.setAttribute("task", t));
            request.setAttribute("roomId", roomId);
            request.setAttribute("taskId", taskId);
            request.setAttribute("isManager", isManager);
            request.getRequestDispatcher("/WEB-INF/views/housekeeping/maintenance-verify.jsp").forward(request, response);
        } catch (Exception ex) {
            boolean isMgr = "HOTEL_MANAGER".equals(account.getRoleName()) || request.getServletPath().startsWith("/manager/");
            session.setAttribute("error", "Lỗi tải thiết bị: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + (isMgr ? "/manager/issues" : "/housekeeping/issues"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = session == null ? null : (Account) session.getAttribute("currentUser");
        if (account == null || (!"HOUSEKEEPING".equals(account.getRoleName()) && !"HOTEL_MANAGER".equals(account.getRoleName()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

        boolean isManager = "HOTEL_MANAGER".equals(account.getRoleName()) || request.getServletPath().startsWith("/manager/");
        String targetUrl = request.getContextPath() + (isManager ? "/manager/issues" : "/housekeeping/issues");

        try {
            long taskId = Long.parseLong(request.getParameter("taskId"));
            String note = request.getParameter("note");
            String[] equipmentParams = request.getParameterValues("equipmentIds");
            
            List<Long> equipmentIds = new ArrayList<>();
            if (equipmentParams != null) {
                for (String param : equipmentParams) {
                    equipmentIds.add(Long.parseLong(param));
                }
            }

            if (equipmentIds.isEmpty()) {
                Optional<HousekeepingTask> task = housekeepingDao.findById(taskId, account.getId(), true);
                if (task.isPresent() && task.get().getRoomEquipmentId() != null) {
                    equipmentIds.add(task.get().getRoomEquipmentId());
                }
            }

            maintenanceService.verifyMaintenance(taskId, account.getId(), equipmentIds, note);
            session.setAttribute("message", "Xác nhận thiết bị sửa chữa thành công.");
            response.sendRedirect(targetUrl);
        } catch (Exception ex) {
            session.setAttribute("error", ex.getMessage());
            response.sendRedirect(targetUrl);
        }
    }
}