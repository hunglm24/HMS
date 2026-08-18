package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.AuditLogService;
import service.RoomChangeService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/reception/room-change"})
public class RoomChangeServlet extends HttpServlet {
    private RoomChangeService roomChangeService;
    private AuditLogService auditLogService;

    @Override
    public void init() {
        // Initialize the services once for the servlet lifecycle.
        roomChangeService = new RoomChangeService();
        auditLogService = new AuditLogService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Only logged-in staff can submit room changes.
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            // Reject anonymous requests before reading any form fields.
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Read form fields from the modal submission.
        long bookingId = parseLong(request.getParameter("bookingId"));
        long currentRoomId = parseLong(request.getParameter("currentRoomId"));
        long newRoomId = parseLong(request.getParameter("newRoomId"));
        String reason = request.getParameter("reason");
        String currentRoomNumber = request.getParameter("currentRoomNumber");
        String newRoomNumber = request.getParameter("newRoomNumber");

        try {
            // Delegate all business rules to the service layer.
            roomChangeService.changeRoom(bookingId, currentRoomId, newRoomId, reason);
            auditLogService.log(
                    request,
                    "ROOM_CHANGE",
                    "BOOKING",
                    bookingId,
                    buildDetail(currentRoomNumber, newRoomNumber, reason)
            );
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
            // Log the database failure and redirect with an error message.
            getServletContext().log("Không thể xử lý đổi phòng", ex);
            if (session != null) {
                session.setAttribute("flashMessage", "Hệ thống đang bận. Vui lòng thử lại sau.");
                session.setAttribute("flashType", "error");
            }
            response.sendRedirect(request.getContextPath() + "/reception/room-map");
        }
    }

    private long parseLong(String value) {
        // Parse the incoming ID safely and fall back to zero on invalid input.
        try {
            return value == null || value.isBlank() ? 0L : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private String buildDetail(String currentRoomNumber, String newRoomNumber, String reason) {
        // Build a human-readable audit log message for the room change event.
        String fromRoom = currentRoomNumber == null || currentRoomNumber.isBlank() ? "không rõ" : currentRoomNumber.trim();
        String toRoom = newRoomNumber == null || newRoomNumber.isBlank() ? "không rõ" : newRoomNumber.trim();
        String note = reason == null || reason.isBlank() ? "Không có lý do" : reason.trim();
        return "Đổi phòng từ " + fromRoom + " sang " + toRoom + ". Lý do: " + note;
    }
}
