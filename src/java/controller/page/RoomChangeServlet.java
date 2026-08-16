package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.RoomChangeService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/reception/room-change"})
public class RoomChangeServlet extends HttpServlet {
    private RoomChangeService roomChangeService;

    @Override
    public void init() {
        roomChangeService = new RoomChangeService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Only logged-in staff can submit room changes.
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Read form fields from the modal submission.
        long bookingId = parseLong(request.getParameter("bookingId"));
        long currentRoomId = parseLong(request.getParameter("currentRoomId"));
        long newRoomId = parseLong(request.getParameter("newRoomId"));
        String reason = request.getParameter("reason");

        try {
            // Delegate all business rules to the service layer.
            roomChangeService.changeRoom(bookingId, currentRoomId, newRoomId, reason);
            if (session != null) {
                // Flash message is shown on the next room-map redirect.
                session.setAttribute("flashMessage", "Đổi phòng thành công.");
                session.setAttribute("flashType", "success");
            }
            response.sendRedirect(request.getContextPath() + "/reception/room-map");
        } catch (IllegalArgumentException ex) {
            if (session != null) {
                // Validation errors come back as a soft warning for the user.
                session.setAttribute("flashMessage", ex.getMessage());
                session.setAttribute("flashType", "warning");
            }
            response.sendRedirect(request.getContextPath() + "/reception/room-map");
        } catch (SQLException ex) {
            getServletContext().log("Không thể xử lý đổi phòng", ex);
            if (session != null) {
                session.setAttribute("flashMessage", "Hệ thống đang bận. Vui lòng thử lại sau.");
                session.setAttribute("flashType", "error");
            }
            response.sendRedirect(request.getContextPath() + "/reception/room-map");
        }
    }

    private long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? 0L : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
