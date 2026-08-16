package controller.page;

import dao.RoomTypeDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.RoomType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/manager/room-types", "/manager/room-types/save", "/manager/room-types/delete"})
public class RoomTypeManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomTypeDao roomTypeDao;

    // This method runs once when the servlet is initialized.
    @Override
    public void init() throws ServletException {
        roomTypeDao = new RoomTypeDao(); // Initialize the DAO for database operations.
    }

    // Handle GET requests, such as opening the page in a browser or clicking a link.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath(); // Get the current request path.

        if ("/manager/room-types".equals(path)) {
            // Load the full list of room types from the database.
            List<RoomType> roomTypes = roomTypeDao.findAll();
            // Pass the data to the JSP through a request attribute.
            req.setAttribute("roomTypes", roomTypes);
            // Forward to the JSP view.
            req.getRequestDispatcher("/WEB-INF/views/manager/room-types.jsp").forward(req, resp);

        } else if ("/manager/room-types/delete".equals(path)) {
            // Delete room type functionality.
            String idParam = req.getParameter("id"); // Read the ID to delete from the URL.
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomTypeDao.delete(id);
                // Store a success message in the session for Toast display.
                req.getSession().setAttribute("toastMessage", "Đã xóa loại phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            }
            // Redirect back to the list page after deletion.
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");

        } else {
            // Return 404 if the URL is invalid.
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Handle POST requests, typically from form submissions.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/room-types/save".equals(path)) {
            // Read all values entered by the user in the add/edit form.
            String idStr = req.getParameter("id");
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String capacityStr = req.getParameter("capacity");
            String basePriceStr = req.getParameter("basePrice");
            String status = req.getParameter("status");

            // Create a new RoomType object and populate it.
            RoomType rt = new RoomType();
            rt.setName(name);
            rt.setDescription(description);
            rt.setCapacity(Integer.parseInt(capacityStr)); // Convert the value to integer.
            rt.setBasePrice(new BigDecimal(basePriceStr)); // Convert the value to decimal.
            rt.setStatus(status);

            boolean success = false;
            // If an ID is provided, update the record; otherwise insert a new one.
            if (idStr != null && !idStr.trim().isEmpty()) {
                rt.setId(Long.parseLong(idStr));
                success = roomTypeDao.update(rt); // Update.
            } else {
                success = roomTypeDao.insert(rt); // Insert.
            }

            // Store a message so the JSP can show a toast notification.
            if (success) {
                req.getSession().setAttribute("toastMessage", "Lưu thông tin loại phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "Có lỗi xảy ra khi lưu loại phòng");
                req.getSession().setAttribute("toastType", "error");
            }

            // Redirect back to the list page after processing.
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
