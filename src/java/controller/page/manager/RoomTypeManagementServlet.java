package controller.page.manager;

import dto.RoomManagementPageData;
import model.RoomType;
import service.RoomTypeService;
import util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {
        "/manager/room-types",
        "/manager/room-types/save-room-type",
        "/manager/room-types/deactivate-room-type",
        "/manager/room-types/delete"
})
public class RoomTypeManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomTypeService roomTypeService;

    @Override
    public void init() throws ServletException {
        // Initialize the service once for the servlet lifecycle.
        roomTypeService = new RoomTypeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Route GET requests based on the requested servlet path.
        String path = req.getServletPath();

        if ("/manager/room-types".equals(path)) {
            // Load the management page with the current filter state.
            RoomManagementPageData pageData = new RoomManagementPageData();
            String keyword = req.getParameter("keyword");
            String roomTypeStatus = req.getParameter("roomTypeStatus");
            if (!req.getParameterMap().containsKey("roomTypeStatus")) {
                // Default to active room types when the filter is missing.
                roomTypeStatus = "ACTIVE";
            }
            pageData.setKeyword(keyword);
            pageData.setRoomTypeStatus(roomTypeStatus);
            pageData.setRoomTypes(roomTypeService.findRoomTypes(keyword, roomTypeStatus));
            req.setAttribute("pageData", pageData);
            req.setAttribute("roomTypes", pageData.getRoomTypes());
            req.getRequestDispatcher("/WEB-INF/views/manager/room-types.jsp").forward(req, resp);
            return;
        }

        if ("/manager/room-types/delete".equals(path) || "/manager/room-types/deactivate-room-type".equals(path)) {
            handleDeactivateRoomType(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle only the room type save endpoint for POST requests.
        if ("/manager/room-types/save-room-type".equals(req.getServletPath())) {
            handleSaveRoomType(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleSaveRoomType(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Build a RoomType object from the submitted form values.
        RoomType roomType = new RoomType();
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String capacityStr = req.getParameter("capacity");
        String basePriceStr = req.getParameter("basePrice");
        String status = req.getParameter("status");

        if (!ValidationUtil.isBlank(idStr)) {
            // Keep the existing ID when this is an edit flow.
            roomType.setId(ValidationUtil.requirePositiveLong(idStr, "Room type"));
        }
        roomType.setName(name);
        roomType.setDescription(description);
        roomType.setCapacity(ValidationUtil.requirePositiveInt(capacityStr, "Capacity"));
        roomType.setBasePrice(ValidationUtil.requirePositiveBigDecimal(basePriceStr, "Base price"));
        roomType.setStatus(status);

        try {
            // Persist the room type and prepare a toast message for the redirect.
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

        resp.sendRedirect(req.getContextPath() + "/manager/room-types");
    }

    private void handleDeactivateRoomType(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Deactivate the selected room type when a valid ID is provided.
        String idParam = req.getParameter("id");
        if (!ValidationUtil.isBlank(idParam)) {
            // Validate the ID before calling the service layer.
            long id = ValidationUtil.requirePositiveLong(idParam, "Room type");
            try {
                // Update the status and report the result through a toast.
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
        resp.sendRedirect(req.getContextPath() + "/manager/room-types");
    }
}
