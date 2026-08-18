package controller.receptionist;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.RoomChangeHistoryService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "RoomChangeHistoryServlet", urlPatterns = {"/reception/room-change-history"})
public class RoomChangeHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomChangeHistoryService roomChangeHistoryService;

    @Override
    public void init() {
        // Initialize the history service once for the servlet lifecycle.
        roomChangeHistoryService = new RoomChangeHistoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Read and normalize the optional filter parameters.
            String bookingCode = trimToNull(request.getParameter("bookingCode"));
            LocalDate fromDate = parseDate(request.getParameter("fromDate"));
            LocalDate toDate = parseDate(request.getParameter("toDate"));
            // Swap the range when the user picks dates in reverse order.
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                LocalDate temp = fromDate;
                fromDate = toDate;
                toDate = temp;
            }
            Long receptionistId = parseLong(request.getParameter("receptionistId"));
            int limit = 100;

            List<model.User> receptionists = roomChangeHistoryService.getReceptionists();
            request.setAttribute("logs", roomChangeHistoryService.getRoomChangeHistory(
                    bookingCode, fromDate, toDate, receptionistId, limit));
            request.setAttribute("receptionists", receptionists);
            request.setAttribute("bookingCode", bookingCode == null ? "" : bookingCode);
            request.setAttribute("fromDate", fromDate == null ? "" : fromDate.toString());
            request.setAttribute("toDate", toDate == null ? "" : toDate.toString());
            request.setAttribute("receptionistId", receptionistId == null ? "" : String.valueOf(receptionistId));
        } catch (SQLException ex) {
            getServletContext().log("Cannot load room change history", ex);
            request.setAttribute("error", "Không thể tải lịch sử đổi phòng. Vui lòng kiểm tra kết nối cơ sở dữ liệu.");
        }
        request.getRequestDispatcher("/WEB-INF/views/reception/room-change-history.jsp").forward(request, response);
    }

    private Long parseLong(String value) {
        // Parse a nullable long value and fall back to null on invalid input.
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Long.parseLong(value.trim());
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String trimToNull(String value) {
        // Convert blank input into null so the filters stay consistent.
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate parseDate(String value) {
        // Parse ISO dates from the query string and ignore invalid values.
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
