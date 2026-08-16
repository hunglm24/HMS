package controller.page;

import dao.RoomDao;
import dao.RoomTypeDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;
import model.RoomType;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manager/rooms", "/manager/rooms/save", "/manager/rooms/delete"})
public class RoomManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomDao roomDao;
    private RoomTypeDao roomTypeDao;

    // Initialize the required DAOs to load data.
    @Override
    public void init() throws ServletException {
        roomDao = new RoomDao();
        roomTypeDao = new RoomTypeDao();
    }

    // Handle GET requests when the user opens the page directly.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/rooms".equals(path)) {
            // Load the room list for rendering the table.
            List<Room> rooms = roomDao.findAllWithRoomTypeName();
            // Load room types for the <select> field in the add/edit form.
            List<RoomType> roomTypes = roomTypeDao.findAll();

            // Pass the data to the JSP.
            req.setAttribute("rooms", rooms);
            req.setAttribute("roomTypes", roomTypes);
            req.getRequestDispatcher("/WEB-INF/views/manager/rooms.jsp").forward(req, resp);

        } else if ("/manager/rooms/delete".equals(path)) {
            // Read the room ID from the URL parameter.
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomDao.delete(id); // Execute the delete statement.
                req.getSession().setAttribute("toastMessage", "Đã xóa phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            }
            // Return to the room list page.
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Handle form submission for adding or updating a room.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/rooms/save".equals(path)) {
            // Read all values submitted from the JSP form.
            String idStr = req.getParameter("id");
            String roomTypeIdStr = req.getParameter("roomTypeId");
            String roomNumber = req.getParameter("roomNumber");
            String floorNumberStr = req.getParameter("floorNumber");
            String status = req.getParameter("status");
            String description = req.getParameter("description");

            // Build a Room object from the form data.
            Room room = new Room();
            room.setRoomTypeId(Long.parseLong(roomTypeIdStr));
            room.setRoomNumber(roomNumber);

            // Floor number is optional and can be left blank.
            if (floorNumberStr != null && !floorNumberStr.trim().isEmpty()) {
                room.setFloorNumber(Integer.parseInt(floorNumberStr));
            }
            room.setStatus(status);
            room.setDescription(description);

            boolean success = false;
            // If an ID is present, update the existing room; otherwise insert a new one.
            if (idStr != null && !idStr.trim().isEmpty()) {
                room.setId(Long.parseLong(idStr));
                success = roomDao.update(room);
            } else {
                success = roomDao.insert(room);
            }

            // Store a toast message for the next page load.
            if (success) {
                req.getSession().setAttribute("toastMessage", "Lưu thông tin phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "Có lỗi xảy ra khi lưu phòng");
                req.getSession().setAttribute("toastType", "error");
            }

            // Redirect back to the room list page.
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
