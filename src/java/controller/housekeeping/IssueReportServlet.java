package controller.housekeeping;

import model.Account;
import service.MaintenanceService;
import service.RoomService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Room;

@WebServlet(name = "IssueReportServlet", urlPatterns = {"/housekeeping/issues/report", "/manager/issues/report"})
public class IssueReportServlet extends HttpServlet {
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final RoomService roomService = new RoomService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("currentUser");
        if (account == null || (!"HOUSEKEEPING".equals(account.getRoleName()) && !"HOTEL_MANAGER".equals(account.getRoleName()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }
        String action = request.getParameter("action");
        if ("getEquipments".equals(action)) {
            try {
                long roomId = Long.parseLong(request.getParameter("roomId"));
                java.util.List<model.HousekeepingTask.EquipmentCheck> equips = maintenanceService.findAllEquipmentsInRoom(roomId);
                
                java.util.Map<String, String> reportableStatuses = new java.util.LinkedHashMap<>();
                reportableStatuses.put("NORMAL", "Bình thường");
                reportableStatuses.put("DAMAGED", "Hư hỏng");
                reportableStatuses.put("MISSING", "Thất lạc");
                reportableStatuses.put("MAINTENANCE", "Bảo trì định kỳ");
                
                request.setAttribute("reportableStatuses", reportableStatuses);
                request.setAttribute("equips", equips);
                request.getRequestDispatcher("/WEB-INF/views/housekeeping/fragments/equipment-list.jsp").forward(request, response);
            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            return;
        }

        request.setAttribute("rooms", roomService.getAllRooms());
        String preselectedRoomIdStr = request.getParameter("roomId");
        if (preselectedRoomIdStr != null && !preselectedRoomIdStr.isBlank()) {
            try { request.setAttribute("preselectedRoomId", Long.parseLong(preselectedRoomIdStr)); }
            catch (NumberFormatException ignored) {}
        }
        request.getRequestDispatcher("/WEB-INF/views/housekeeping/issue-report.jsp").forward(request, response);
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
            long roomId = Long.parseLong(request.getParameter("roomId"));
            String note = request.getParameter("note");
            String[] equipmentParams = request.getParameterValues("roomEquipmentIds");
            boolean hasEquipmentIssue = false;

            if (equipmentParams != null && equipmentParams.length > 0) {
                for (String param : equipmentParams) {
                    if (param != null && !param.trim().isEmpty()) {
                        long equipId = Long.parseLong(param);
                        String currentStatus = request.getParameter("currentStatus_" + equipId);
                        String newStatus = request.getParameter("status_" + equipId);
                        
                        if (newStatus != null && !newStatus.equals("NORMAL") && !newStatus.equals(currentStatus)) {
                            maintenanceService.reportIssue(roomId, equipId, newStatus, note);
                            hasEquipmentIssue = true;
                        }
                    }
                }
            } 
            
            if (!hasEquipmentIssue) {
                maintenanceService.reportIssue(roomId, null, null, note);
            }

            session.setAttribute("successMessage", "Báo cáo sự cố thành công.");
                        boolean isMgr = "HOTEL_MANAGER".equals(account.getRoleName()) || request.getServletPath().startsWith("/manager/");
            response.sendRedirect(request.getContextPath() + (isMgr ? "/manager/issues" : "/housekeeping/issues"));
        } catch (Exception ex) {
            session.setAttribute("errorMessage", ex.getMessage());
            Long roomId = parsePositiveLong(request.getParameter("roomId"));
            response.sendRedirect(request.getContextPath() + "/housekeeping/issues/report"
                    + (roomId == null ? "" : "?roomId=" + roomId));
        }
    }

    private Long parsePositiveLong(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
