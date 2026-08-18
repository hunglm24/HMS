package controller.page.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/manager/room-types", "/manager/room-types/delete"})
public class RoomTypeManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/room-types".equals(path)) {
            // Redirect the legacy route to the combined rooms screen.
            resp.sendRedirect(req.getContextPath() + "/manager/rooms?tab=room-types");
            return;
        }

        if ("/manager/room-types/delete".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/manager/rooms/deactivate-room-type?id=" + idParam);
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/manager/rooms?tab=room-types");
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
