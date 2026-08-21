package controller.page.manager;

import dto.RoomManagementPageData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;
import service.RoomService;
import service.HousekeepingService;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {
        "/manager/rooms",
        "/manager/rooms/save-room",
        "/manager/rooms/deactivate-room",
        "/manager/rooms/delete",
        "/manager/rooms/create-task"
})
public class RoomManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomService roomService;
    private HousekeepingService housekeepingService;

    @Override
    public void init() throws ServletException {
        // Initialize the service once for the servlet lifecycle.
        roomService = new RoomService();
        housekeepingService = new HousekeepingService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Route GET requests based on the requested servlet path.
        String path = req.getServletPath();
        if ("/manager/rooms".equals(path)) {
            // Switch to the room type tab when requested by the UI.
            if ("room-types".equalsIgnoreCase(req.getParameter("tab"))) {
                resp.sendRedirect(req.getContextPath() + "/manager/room-types");
                return;
            }

            // Build the page data and fetch the filtered room list.
            RoomManagementPageData pageData = buildPageData(req);
            req.setAttribute("pageData", pageData);
            req.setAttribute("roomTypeOptions", roomService.getRoomTypeOptions());
            req.setAttribute("floorOptions", roomService.getDistinctFloors());
            req.setAttribute("maxFloor", roomService.getMaxFloor());
            try {
                housekeepingService.syncDatabaseState();
                req.setAttribute("housekeepers", housekeepingService.getHousekeepers());
                req.setAttribute("housekeeperWorkloads", housekeepingService.getHousekeeperWorkloads());
            } catch (SQLException ignored) {
            }
            req.setAttribute("rooms", roomService.findRooms(
                    pageData.getKeyword(),
                    pageData.getRoomTypeId(),
                    pageData.getFloor(),
                    pageData.getRoomStatus()));
            req.getRequestDispatcher("/WEB-INF/views/manager/rooms.jsp").forward(req, resp);
            return;
        }

        if ("/manager/rooms/delete".equals(path) || "/manager/rooms/deactivate-room".equals(path)) {
            handleDeactivateRoom(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle only the room save endpoint for POST requests.
        if ("/manager/rooms/save-room".equals(req.getServletPath())) {
            handleSaveRoom(req, resp);
            return;
        }
        
        if ("/manager/rooms/create-task".equals(req.getServletPath())) {
            handleCreateTask(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private RoomManagementPageData buildPageData(HttpServletRequest req) {
        // Copy filter parameters into a single page-data object.
        RoomManagementPageData pageData = new RoomManagementPageData();
        pageData.setKeyword(req.getParameter("keyword"));
        pageData.setRoomStatus(req.getParameter("roomStatus"));
        pageData.setRoomTypeId(ValidationUtil.optionalPositiveLong(req.getParameter("roomTypeId"), "Room type"));
        pageData.setFloor(ValidationUtil.optionalPositiveInt(req.getParameter("floor"), "Floor"));
        return pageData;
    }

    private void handleSaveRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Build a Room object from the submitted form values.
        Room room = new Room();
        String idStr = req.getParameter("id");
        String roomTypeIdStr = req.getParameter("roomTypeId");
        String roomNumber = req.getParameter("roomNumber");
        String floorNumberStr = req.getParameter("floorNumber");
        String status = req.getParameter("status");
        String description = req.getParameter("description");

        if (!ValidationUtil.isBlank(idStr)) {
            // Keep the existing ID when this is an edit flow.
            room.setId(ValidationUtil.requirePositiveLong(idStr, "Room"));
        }
        room.setRoomTypeId(ValidationUtil.requirePositiveLong(roomTypeIdStr, "Room type"));
        room.setRoomNumber(roomNumber);
        room.setFloorNumber(ValidationUtil.optionalPositiveInt(floorNumberStr, "Floor"));
        room.setStatus(status);
        room.setDescription(description);

        try {
            // Persist the room and prepare a toast message for the redirect.
            roomService.saveRoom(room);
            req.getSession().setAttribute("toastMessage", "Room saved successfully.");
            req.getSession().setAttribute("toastType", "success");
        } catch (IllegalArgumentException ex) {
            req.getSession().setAttribute("toastMessage", ex.getMessage());
            req.getSession().setAttribute("toastType", "error");
        } catch (SQLException ex) {
            req.getSession().setAttribute("toastMessage", "Failed to save the room.");
            req.getSession().setAttribute("toastType", "error");
        }

        resp.sendRedirect(req.getContextPath() + "/manager/rooms");
    }

    private void handleDeactivateRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Deactivate the selected room when a valid ID is provided.
        String idParam = req.getParameter("id");
        if (!ValidationUtil.isBlank(idParam)) {
            // Validate the ID before calling the service layer.
            long id = ValidationUtil.requirePositiveLong(idParam, "Room");
            try {
                // Update the room status and report the result through a toast.
                roomService.deactivateRoom(id);
                req.getSession().setAttribute("toastMessage", "Room deactivated successfully.");
                req.getSession().setAttribute("toastType", "success");
            } catch (IllegalArgumentException ex) {
                req.getSession().setAttribute("toastMessage", ex.getMessage());
                req.getSession().setAttribute("toastType", "error");
            } catch (SQLException ex) {
                req.getSession().setAttribute("toastMessage", "Failed to update the room.");
                req.getSession().setAttribute("toastType", "error");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manager/rooms");
    }
    
    private void handleCreateTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long roomId = ValidationUtil.requirePositiveLong(req.getParameter("roomId"), "Room");
            String taskType = req.getParameter("taskType");
            Long assignedTo = ValidationUtil.optionalPositiveLong(req.getParameter("assignedTo"), "Assignee");
            String priority = req.getParameter("priority");
            String cleaningTasks = req.getParameter("cleaningTasks");
            String note = req.getParameter("note");
            
            housekeepingService.createManualTask(roomId, taskType, assignedTo, priority, cleaningTasks, note);
            req.getSession().setAttribute("toastMessage", "Đã tạo công việc thành công.");
            req.getSession().setAttribute("toastType", "success");
        } catch (IllegalArgumentException ex) {
            req.getSession().setAttribute("toastMessage", ex.getMessage());
            req.getSession().setAttribute("toastType", "error");
        } catch (SQLException ex) {
            req.getSession().setAttribute("toastMessage", "Lỗi hệ thống khi tạo công việc.");
            req.getSession().setAttribute("toastType", "error");
        }
        
        resp.sendRedirect(req.getContextPath() + "/manager/rooms");
    }
}
