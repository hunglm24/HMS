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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MaintenanceVerifyServlet", urlPatterns = {"/housekeeping/issues/verify"})
public class MaintenanceVerifyServlet extends HttpServlet {
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
            long roomId = Long.parseLong(request.getParameter("roomId"));
            long taskId = Long.parseLong(request.getParameter("taskId"));
            
            List<HousekeepingTask.EquipmentCheck> equipments = maintenanceService.findDamagedEquipments(roomId);
            
            request.setAttribute("equipments", equipments);
            request.setAttribute("roomId", roomId);
            request.setAttribute("taskId", taskId);
            request.getRequestDispatcher("/WEB-INF/views/housekeeping/maintenance-verify.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("errorMessage", "Lỗi tải thiết bị cần xác nhận: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/housekeeping/issues");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("currentUser");
        if (account == null || (!"HOUSEKEEPING".equals(account.getRoleName()) && !"HOTEL_MANAGER".equals(account.getRoleName()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

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

            maintenanceService.verifyMaintenance(taskId, account.getId(), equipmentIds, note);
            session.setAttribute("successMessage", "Xác nhận thiết bị sửa chữa thành công.");
            response.sendRedirect(request.getContextPath() + "/housekeeping/issues");
        } catch (Exception ex) {
            session.setAttribute("errorMessage", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/housekeeping/issues");
        }
    }
}
