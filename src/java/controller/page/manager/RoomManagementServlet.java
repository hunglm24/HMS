package controller.page.manager;

import dao.RoomTypeDao;
import dto.RoomManagementPageData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;
import model.RoomEquipment;
import model.RoomType;
import service.HousekeepingService;
import service.RoomEquipmentService;
import service.RoomService;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import util.DBConnectionUtil;

@WebServlet(urlPatterns = {
        "/manager/rooms",
        "/manager/rooms/new",
        "/manager/rooms/edit",
        "/manager/rooms/save-room",
        "/manager/rooms/deactivate-room",
        "/manager/rooms/delete",
        "/manager/rooms/create-task"
})
public class RoomManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomService roomService;
    private RoomEquipmentService roomEquipmentService;
    private HousekeepingService housekeepingService;
    private RoomTypeDao roomTypeDao;

    @Override
    public void init() throws ServletException {
        roomService = new RoomService();
        roomEquipmentService = new RoomEquipmentService();
        housekeepingService = new HousekeepingService();
        roomTypeDao = new RoomTypeDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/rooms".equals(path)) {
            prepareRoomListPage(req);
            req.getRequestDispatcher("/WEB-INF/views/manager/rooms.jsp").forward(req, resp);
            return;
        }

        if ("/manager/rooms/new".equals(path)) {
            prepareRoomFormLookup(req, null);
            prepareRoomFormView(req, new Room(), false, List.of());
            req.getRequestDispatcher("/WEB-INF/views/manager/room-form.jsp").forward(req, resp);
            return;
        }

        if ("/manager/rooms/edit".equals(path)) {
            handleEditRoom(req, resp);
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

    private void prepareRoomListPage(HttpServletRequest req) {
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

        List<Room> rooms = roomService.findRooms(
                pageData.getKeyword(),
                pageData.getRoomTypeId(),
                pageData.getFloor(),
                pageData.getRoomStatus());
        req.setAttribute("rooms", rooms);
        req.setAttribute("roomEquipmentSummaries", roomEquipmentService.findRoomEquipmentSummaries(rooms));
    }

    private RoomManagementPageData buildPageData(HttpServletRequest req) {
        RoomManagementPageData pageData = new RoomManagementPageData();
        pageData.setKeyword(req.getParameter("keyword"));
        pageData.setRoomStatus(req.getParameter("roomStatus"));
        pageData.setRoomTypeId(ValidationUtil.optionalPositiveLong(req.getParameter("roomTypeId"), "Room type"));
        pageData.setFloor(ValidationUtil.optionalPositiveInt(req.getParameter("floor"), "Floor"));
        return pageData;
    }

    private void prepareRoomFormLookup(HttpServletRequest req, Room existingRoom) {
        List<RoomType> roomTypes = new ArrayList<>(roomService.getRoomTypeOptions());
        if (existingRoom != null && existingRoom.getRoomTypeId() > 0) {
            boolean hasCurrentType = roomTypes.stream()
                    .anyMatch(roomType -> roomType != null
                            && roomType.getId() != null
                            && roomType.getId().longValue() == existingRoom.getRoomTypeId());
            if (!hasCurrentType) {
                roomTypeDao.findById(existingRoom.getRoomTypeId()).ifPresent(roomTypes::add);
            }
        }

        req.setAttribute("roomTypeOptions", roomTypes);
        req.setAttribute("roomOptions", roomService.getAllRooms().stream()
                .filter(room -> existingRoom == null || existingRoom.getId() == null || room.getId() != existingRoom.getId())
                .toList());
        req.setAttribute("roomEquipmentStatuses", roomEquipmentService.findStatuses());
        req.setAttribute("equipmentCatalog", roomEquipmentService.findAssignableEquipments());
    }

    private void prepareRoomFormView(HttpServletRequest req, Room form, boolean updating, List<RoomEquipment> roomEquipments) {
        req.setAttribute("form", form);
        req.setAttribute("isEditMode", updating);
        req.setAttribute("roomId", form.getId());
        req.setAttribute("roomFormAction", "/manager/rooms/save-room");
        req.setAttribute("roomPageTitle", updating ? "Edit Room | HMS" : "New Room | HMS");
        req.setAttribute("roomPageHeading", updating ? "Edit Room" : "New Room");
        req.setAttribute(
                "roomPageSubtitle",
                updating
                        ? "Update room information and equipment in one place."
                        : "Create a room and assign equipment before saving."
        );
        req.setAttribute("roomBackUrl", "/manager/rooms");
        req.setAttribute("roomSubmitLabel", updating ? "Update Room" : "Save Room");
        req.setAttribute("roomEquipments", roomEquipments == null ? List.of() : roomEquipments);
        req.setAttribute("roomEquipmentCount", roomEquipments == null ? 0 : roomEquipments.size());
    }

    private void handleEditRoom(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long roomId = ValidationUtil.optionalPositiveLong(req.getParameter("id"), "Room");
        if (roomId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Room room = roomService.getRoomById(roomId).orElse(null);
        if (room == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        prepareRoomFormLookup(req, room);
        prepareRoomFormView(req, room, true, roomEquipmentService.findRoomEquipments(roomId));
        req.getRequestDispatcher("/WEB-INF/views/manager/room-form.jsp").forward(req, resp);
    }

    private void handleSaveRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Map<String, String> errors = new LinkedHashMap<>();
        Room room = new Room();
        boolean updating = false;

        String idStr = req.getParameter("id");
        if (!ValidationUtil.isBlank(idStr)) {
            try {
                room.setId(ValidationUtil.requirePositiveLong(idStr, "Room"));
                updating = true;
            } catch (IllegalArgumentException ex) {
                errors.put("general", ex.getMessage());
            }
        }

        try {
            room.setRoomTypeId(ValidationUtil.requirePositiveLong(req.getParameter("roomTypeId"), "Room type"));
        } catch (IllegalArgumentException ex) {
            errors.put("roomTypeId", ex.getMessage());
        }

        try {
            room.setRoomNumber(ValidationUtil.requireDigitsText(req.getParameter("roomNumber"), "Room number", 1, 20));
        } catch (IllegalArgumentException ex) {
            errors.put("roomNumber", ex.getMessage());
        }

        try {
            room.setFloorNumber(ValidationUtil.optionalPositiveInt(req.getParameter("floorNumber"), "Floor"));
        } catch (IllegalArgumentException ex) {
            errors.put("floorNumber", ex.getMessage());
        }

        try {
            room.setStatus(ValidationUtil.requireStatus(req.getParameter("status"), "Status", java.util.Set.of(
                    "AVAILABLE", "OCCUPIED", "CLEANING", "MAINTENANCE", "NOT_READY", "INSPECTION")));
        } catch (IllegalArgumentException ex) {
            errors.put("status", ex.getMessage());
        }

        try {
            room.setDescription(ValidationUtil.optionalText(req.getParameter("description"), 500));
        } catch (IllegalArgumentException ex) {
            errors.put("description", ex.getMessage());
        }

        List<RoomEquipment> equipmentDrafts = readEquipmentDrafts(req);
        List<RoomEquipment> normalizedEquipments = List.of();
        try {
            normalizedEquipments = roomEquipmentService.normalizeAssignments(equipmentDrafts);
        } catch (IllegalArgumentException ex) {
            errors.put("roomEquipments", ex.getMessage());
        }

        if (!errors.isEmpty()) {
            prepareRoomFormLookup(req, room);
            req.setAttribute("errors", errors);
            prepareRoomFormView(req, room, updating, equipmentDrafts.isEmpty() ? normalizedEquipments : equipmentDrafts);
            req.getRequestDispatcher("/WEB-INF/views/manager/room-form.jsp").forward(req, resp);
            return;
        }

        try {
            try (Connection conn = DBConnectionUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    boolean saved = roomService.saveRoom(conn, room);
                    ValidationUtil.requireTrue(saved, updating ? "Failed to update the room." : "Failed to create the room.");
                    roomEquipmentService.replaceRoomEquipments(conn, room.getId(), normalizedEquipments, null);
                    conn.commit();
                } catch (SQLException ex) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        ex.addSuppressed(rollbackEx);
                    }
                    throw new RuntimeException(ex);
                }
            }

            req.getSession().setAttribute("toastMessage", updating ? "Room updated successfully." : "Room created successfully.");
            req.getSession().setAttribute("toastType", "success");
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");
        } catch (IllegalArgumentException ex) {
            errors.put("general", ex.getMessage());
            prepareRoomFormLookup(req, room);
            req.setAttribute("errors", errors);
            prepareRoomFormView(req, room, updating, normalizedEquipments);
            req.getRequestDispatcher("/WEB-INF/views/manager/room-form.jsp").forward(req, resp);
        } catch (SQLException ex) {
            errors.put("general", updating ? "Failed to update the room." : "Failed to create the room.");
            prepareRoomFormLookup(req, room);
            req.setAttribute("errors", errors);
            prepareRoomFormView(req, room, updating, normalizedEquipments);
            req.getRequestDispatcher("/WEB-INF/views/manager/room-form.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            errors.put("general", updating ? "Failed to update the room." : "Failed to create the room.");
            prepareRoomFormLookup(req, room);
            req.setAttribute("errors", errors);
            prepareRoomFormView(req, room, updating, normalizedEquipments);
            req.getRequestDispatcher("/WEB-INF/views/manager/room-form.jsp").forward(req, resp);
        }
    }

    private List<RoomEquipment> readEquipmentDrafts(HttpServletRequest req) {
        String[] equipmentIds = req.getParameterValues("equipmentId");
        String[] quantities = req.getParameterValues("equipmentQuantity");
        String[] statuses = req.getParameterValues("equipmentStatus");
        String[] notes = req.getParameterValues("equipmentNote");
        List<RoomEquipment> drafts = new ArrayList<>();
        int total = Math.max(Math.max(equipmentIds == null ? 0 : equipmentIds.length, quantities == null ? 0 : quantities.length),
                Math.max(statuses == null ? 0 : statuses.length, notes == null ? 0 : notes.length));

        for (int i = 0; i < total; i++) {
            String equipmentIdRaw = valueAt(equipmentIds, i);
            String quantityRaw = valueAt(quantities, i);
            String statusRaw = valueAt(statuses, i);
            String noteRaw = valueAt(notes, i);

            if (ValidationUtil.isBlank(equipmentIdRaw)
                    && ValidationUtil.isBlank(quantityRaw)
                    && ValidationUtil.isBlank(statusRaw)
                    && ValidationUtil.isBlank(noteRaw)) {
                continue;
            }

            RoomEquipment draft = new RoomEquipment();
            draft.setEquipmentName("Equipment");
            try {
                draft.setEquipmentId(ValidationUtil.requirePositiveLong(equipmentIdRaw, "Equipment"));
                draft.setEquipmentName("Equipment #" + draft.getEquipmentId());
            } catch (IllegalArgumentException ignored) {
            }
            try {
                draft.setQuantity(ValidationUtil.requirePositiveInt(quantityRaw, "Quantity"));
            } catch (IllegalArgumentException ignored) {
            }
            draft.setStatus(statusRaw);
            draft.setNote(noteRaw);
            drafts.add(draft);
        }

        return drafts;
    }

    private String valueAt(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length) {
            return null;
        }
        return values[index];
    }

    private void handleDeactivateRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (!ValidationUtil.isBlank(idParam)) {
            long id = ValidationUtil.requirePositiveLong(idParam, "Room");
            try {
                boolean deactivated = roomService.deactivateRoom(id);
                ValidationUtil.requireTrue(deactivated, "Failed to update the room.");
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
            req.getSession().setAttribute("toastMessage", "Created task successfully.");
            req.getSession().setAttribute("toastType", "success");
        } catch (IllegalArgumentException ex) {
            req.getSession().setAttribute("toastMessage", ex.getMessage());
            req.getSession().setAttribute("toastType", "error");
        } catch (SQLException ex) {
            req.getSession().setAttribute("toastMessage", "System error while creating task.");
            req.getSession().setAttribute("toastType", "error");
        }

        resp.sendRedirect(req.getContextPath() + "/manager/rooms");
    }
}
