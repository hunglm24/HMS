package controller.page.manager;

import dto.RoomManagementPageData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;
import model.RoomType;
import service.RoomService;
import service.RoomTypeService;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {
        "/manager/rooms",
        "/manager/rooms/save-room",
        "/manager/rooms/save-room-type",
        "/manager/rooms/deactivate-room",
        "/manager/rooms/deactivate-room-type",
        "/manager/rooms/delete"
})
public class RoomManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomService roomService;
    private RoomTypeService roomTypeService;

    @Override
    public void init() throws ServletException {
        roomService = new RoomService();
        roomTypeService = new RoomTypeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/rooms".equals(path)) {
            // Load both datasets for the combined room management screen.
            RoomManagementPageData pageData = buildPageData(req);
            req.setAttribute("pageData", pageData);
            req.setAttribute("roomTypes", pageData.getRoomTypes());
            req.setAttribute("roomTypeOptions", roomTypeService.getAllRoomTypes());
            req.setAttribute("rooms", pageData.getRooms());
            req.getRequestDispatcher("/WEB-INF/views/manager/rooms.jsp").forward(req, resp);
            return;
        }

        if ("/manager/rooms/delete".equals(path)) {
            handleDeactivateRoom(req, resp);
            return;
        }

        if ("/manager/rooms/deactivate-room".equals(path)) {
            handleDeactivateRoom(req, resp);
            return;
        }

        if ("/manager/rooms/deactivate-room-type".equals(path)) {
            handleDeactivateRoomType(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/rooms/save-room".equals(path)) {
            handleSaveRoom(req, resp);
            return;
        }

        if ("/manager/rooms/save-room-type".equals(path)) {
            handleSaveRoomType(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private RoomManagementPageData buildPageData(HttpServletRequest req) {
        RoomManagementPageData pageData = new RoomManagementPageData();
        String activeTab = ValidationUtil.isBlank(req.getParameter("tab"))
                ? "room-types" : req.getParameter("tab");
        String keyword = req.getParameter("keyword");
        String roomTypeStatus = req.getParameter("roomTypeStatus");
        if (!req.getParameterMap().containsKey("roomTypeStatus")) {
            roomTypeStatus = "ACTIVE";
        }
        String roomStatus = req.getParameter("roomStatus");
        Long roomTypeId = ValidationUtil.optionalPositiveLong(req.getParameter("roomTypeId"), "Room type");
        Integer floor = ValidationUtil.optionalPositiveInt(req.getParameter("floor"), "Floor");

        pageData.setActiveTab(activeTab);
        pageData.setKeyword(keyword);
        pageData.setRoomTypeStatus(roomTypeStatus);
        pageData.setRoomStatus(roomStatus);
        pageData.setRoomTypeId(roomTypeId);
        pageData.setFloor(floor);

        // Keep both lists available so the JSP can render either tab.
        List<RoomType> roomTypes = roomTypeService.findRoomTypes(keyword, roomTypeStatus);
        List<Room> rooms = roomService.findRooms(keyword, roomTypeId, floor, roomStatus);
        List<Room> allRooms = roomService.getAllRooms();
        Map<Long, Long> totalRoomCounts = allRooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomTypeId, Collectors.counting()));
        roomTypes.forEach(roomType -> roomType.setTotalQuantity(
                totalRoomCounts.getOrDefault(roomType.getId(), 0L).intValue()));
        pageData.setRoomTypes(roomTypes);
        pageData.setRooms(rooms);
        return pageData;
    }

    private void handleSaveRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Room room = new Room();
        String idStr = req.getParameter("id");
        String roomTypeIdStr = req.getParameter("roomTypeId");
        String roomNumber = req.getParameter("roomNumber");
        String floorNumberStr = req.getParameter("floorNumber");
        String status = req.getParameter("status");
        String description = req.getParameter("description");

        if (!ValidationUtil.isBlank(idStr)) {
            room.setId(ValidationUtil.requirePositiveLong(idStr, "Room"));
        }
        room.setRoomTypeId(ValidationUtil.requirePositiveLong(roomTypeIdStr, "Room type"));
        room.setRoomNumber(roomNumber);
        room.setFloorNumber(ValidationUtil.optionalPositiveInt(floorNumberStr, "Floor"));
        room.setStatus(status);
        room.setDescription(description);

        try {
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

        resp.sendRedirect(req.getContextPath() + "/manager/rooms?tab=rooms");
    }

    private void handleSaveRoomType(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoomType roomType = new RoomType();
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String capacityStr = req.getParameter("capacity");
        String basePriceStr = req.getParameter("basePrice");
        String status = req.getParameter("status");

        if (!ValidationUtil.isBlank(idStr)) {
            roomType.setId(ValidationUtil.requirePositiveLong(idStr, "Room type"));
        }
        roomType.setName(name);
        roomType.setDescription(description);
        roomType.setCapacity(ValidationUtil.requirePositiveInt(capacityStr, "Capacity"));
        roomType.setBasePrice(ValidationUtil.requirePositiveBigDecimal(basePriceStr, "Base price"));
        roomType.setStatus(status);

        try {
            roomTypeService.saveRoomType(roomType);
            req.getSession().setAttribute("toastMessage", "Room type saved successfully.");
            req.getSession().setAttribute("toastType", "success");
        } catch (IllegalArgumentException ex) {
            req.getSession().setAttribute("toastMessage", ex.getMessage());
            req.getSession().setAttribute("toastType", "error");
        } catch (SQLException ex) {
            req.getSession().setAttribute("toastMessage", "Failed to save the room type.");
            req.getSession().setAttribute("toastType", "error");
        }

        resp.sendRedirect(req.getContextPath() + "/manager/rooms?tab=room-types");
    }

    private void handleDeactivateRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (!ValidationUtil.isBlank(idParam)) {
            long id = ValidationUtil.requirePositiveLong(idParam, "Room");
            try {
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
        resp.sendRedirect(req.getContextPath() + "/manager/rooms?tab=rooms");
    }

    private void handleDeactivateRoomType(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (!ValidationUtil.isBlank(idParam)) {
            long id = ValidationUtil.requirePositiveLong(idParam, "Room type");
            try {
                roomTypeService.deactivateRoomType(id);
                req.getSession().setAttribute("toastMessage", "Room type deactivated successfully.");
                req.getSession().setAttribute("toastType", "success");
            } catch (IllegalArgumentException ex) {
                req.getSession().setAttribute("toastMessage", ex.getMessage());
                req.getSession().setAttribute("toastType", "error");
            } catch (SQLException ex) {
                req.getSession().setAttribute("toastMessage", "Failed to update the room type.");
                req.getSession().setAttribute("toastType", "error");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manager/rooms?tab=room-types");
    }
}
