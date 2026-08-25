package controller.housekeeping;

import dao.DamageReportDao;
import dao.RoomDao;
import dto.DamageReportDto;
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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@WebServlet(name = "IssueListServlet", urlPatterns = {"/housekeeping/issues", "/manager/issues"})
public class IssueListServlet extends HttpServlet {
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final DamageReportDao damageReportDao = new DamageReportDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = session == null ? null : (Account) session.getAttribute("currentUser");
        if (account == null || (!"HOUSEKEEPING".equals(account.getRoleName()) && !"HOTEL_MANAGER".equals(account.getRoleName()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return;
        }

        boolean isManager = "HOTEL_MANAGER".equals(account.getRoleName()) || request.getServletPath().startsWith("/manager/");
        if (isManager && "/housekeeping/issues".equals(request.getServletPath())) {
            String qs = request.getQueryString();
            response.sendRedirect(request.getContextPath() + "/manager/issues" + (qs != null && !qs.isBlank() ? "?" + qs : ""));
            return;
        }

        try {
            String activeTab = request.getParameter("tab");
            if (activeTab == null || activeTab.isBlank()) {
                activeTab = isManager ? "damage" : "maintenance";
            }

            String keyword = request.getParameter("search");
            if (keyword != null && keyword.isBlank()) keyword = null;

            String floorParam = request.getParameter("floor");
            Integer floor = null;
            if (floorParam != null && !floorParam.trim().isEmpty()) {
                floor = Integer.parseInt(floorParam);
            }

            String taskType = request.getParameter("taskType");
            if (taskType != null && taskType.isBlank()) taskType = null;

            String status = request.getParameter("status");
            if (status != null && status.isBlank()) status = null;

            String damageStatus = request.getParameter("damageStatus");
            if (damageStatus != null && damageStatus.isBlank()) damageStatus = null;

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

            List<HousekeepingTask> tasks = maintenanceService.findIssueTasks(keyword, floor, taskType, status, sortColumn, sortDirection, page, pageSize);
            int total = maintenanceService.countIssueTasks(keyword, floor, taskType, status);
            int totalPages = (int) Math.ceil((double) total / pageSize);

            int pendingDamageCount = 0;
            List<DamageReportDto> damageReports = List.of();
            if (isManager) {
                pendingDamageCount = damageReportDao.countPendingReports();
                damageReports = damageReportDao.findDamageReports(keyword, damageStatus, 0, 1000);
            }

            request.setAttribute("activeTab", activeTab);
            request.setAttribute("tasks", tasks);
            request.setAttribute("damageReports", damageReports);
            request.setAttribute("pendingDamageCount", pendingDamageCount);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("search", keyword);
            request.setAttribute("floor", floorParam);
            request.setAttribute("floorOptions", new RoomDao().getDistinctFloors());
            request.setAttribute("maxFloor", new RoomDao().getMaxFloor());
            request.setAttribute("taskType", taskType);
            request.setAttribute("status", status);
            request.setAttribute("damageStatus", damageStatus);
            request.setAttribute("currentSort", currentSort);
            request.setAttribute("currentDir", currentDir);
            request.setAttribute("isManager", isManager);

            request.getRequestDispatcher("/WEB-INF/views/housekeeping/issue-list.jsp").forward(request, response);
        } catch (Exception ex) {
            getServletContext().log("Lỗi tải danh sách sự cố", ex);
            response.sendError(500, "Lỗi tải danh sách sự cố: " + ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = session == null ? null : (Account) session.getAttribute("currentUser");
        if (account == null || !"HOTEL_MANAGER".equals(account.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Quản lý mới có quyền duyệt đền bù");
            return;
        }

        String action = request.getParameter("action");
        String reportIdStr = request.getParameter("reportId");
        String compensationAmountStr = request.getParameter("compensationAmount");
        String note = request.getParameter("note");

        try {
            if (reportIdStr == null || reportIdStr.isBlank()) {
                throw new IllegalArgumentException("Mã báo cáo không hợp lệ.");
            }
            long reportId = Long.parseLong(reportIdStr);

            BigDecimal finalAmount = BigDecimal.ZERO;
            if (compensationAmountStr != null && !compensationAmountStr.isBlank()) {
                // Remove commas/dots if formatted
                String cleanAmount = compensationAmountStr.replace(",", "").replace(".", "").trim();
                finalAmount = new BigDecimal(cleanAmount);
            }

            if ("CHARGE".equalsIgnoreCase(action)) {
                damageReportDao.processDamageReport(reportId, "CHARGE", finalAmount, note, account.getId());
                NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                session.setAttribute("toastMessage", "Đã duyệt phạt đền bù " + nf.format(finalAmount) + " đ và tự động cập nhật vào Hóa đơn check-out của khách.");
                session.setAttribute("toastType", "toast-success");
            } else if ("WAIVE".equalsIgnoreCase(action)) {
                damageReportDao.processDamageReport(reportId, "WAIVE", BigDecimal.ZERO, note, account.getId());
                session.setAttribute("toastMessage", "Đã xác nhận miễn phạt (Waive) cho sự cố này.");
                session.setAttribute("toastType", "toast-success");
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }

            response.sendRedirect(request.getContextPath() + "/manager/issues?tab=damage");
        } catch (Exception ex) {
            getServletContext().log("Lỗi xử lý duyệt đền bù", ex);
            session.setAttribute("toastMessage", "Lỗi: " + ex.getMessage());
            session.setAttribute("toastType", "toast-error");
            response.sendRedirect(request.getContextPath() + "/manager/issues?tab=damage");
        }
    }
}