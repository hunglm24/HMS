package controller.page;

import model.CheckInBookingSummary;
import model.RoomType;
import service.CheckInService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/reception/check-in"})
public class CheckInServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CheckInService checkInService;

    @Override
    public void init() {
        checkInService = new CheckInService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int bookingId = parseInt(request.getParameter("bookingId"));
        String keyword = request.getParameter("q");
        String bookingStatus = request.getParameter("status");
        Integer roomTypeId = parseNullableInt(request.getParameter("roomTypeId"));
        String scope = request.getParameter("scope");
        String sort = request.getParameter("sort");
        String direction = request.getParameter("direction");
        int page = parseInt(request.getParameter("page"));

        try {
            CheckInService.CheckInPage result = checkInService.getCheckInPage(
                    keyword, bookingStatus, roomTypeId, scope, sort, direction, page);
            List<RoomType> roomTypes = checkInService.getRoomTypes();
            request.setAttribute("result", result);
            request.setAttribute("roomTypes", roomTypes);
            request.setAttribute("selectedBooking", findSelectedBooking(bookingId).orElse(null));
            request.getRequestDispatcher("/WEB-INF/views/reception/check-in.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ£i danh sÃƒÂ¡ch booking check-in", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Optional<CheckInBookingSummary> findSelectedBooking(int bookingId) throws SQLException {
        return bookingId > 0 ? checkInService.findBookingById(bookingId) : Optional.empty();
    }

    private int parseInt(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Integer parseNullableInt(String value) {
        int parsed = parseInt(value);
        return parsed > 0 ? parsed : null;
    }
}

