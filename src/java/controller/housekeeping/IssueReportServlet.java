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

@WebServlet(name = "IssueReportServlet", urlPatterns = {"/housekeeping/issues/report"})
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
        
        request.setAttribute("rooms", roomService.getAllRooms());
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
            String equipmentParam = request.getParameter("roomEquipmentId");
            Long roomEquipmentId = null;
            if (equipmentParam != null && !equipmentParam.trim().isEmpty()) {
                roomEquipmentId = Long.parseLong(equipmentParam);
            }
            String note = request.getParameter("note");

            maintenanceService.reportIssue(roomId, roomEquipmentId, note);
            session.setAttribute("successMessage", "Báo cáo sự cố thành công.");
            response.sendRedirect(request.getContextPath() + "/housekeeping/issues");
        } catch (Exception ex) {
            session.setAttribute("errorMessage", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/housekeeping/issues/report");
        }
    }
}
