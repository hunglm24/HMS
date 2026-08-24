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

@WebServlet(urlPatterns = {"/reception/check-out"})
public class CheckOutServlet extends HttpServlet {
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
        if (scope == null) scope = "checkout_today";
        String sort = request.getParameter("sort");
        String direction = request.getParameter("direction");
        int page = parseInt(request.getParameter("page"));

        try {
            CheckInService.CheckInPage result = checkInService.getCheckInPage(
                    keyword, bookingStatus, roomTypeId, scope, sort, direction, page);
            List<RoomType> roomTypes = checkInService.getRoomTypes();
            request.setAttribute("result", result);
            request.setAttribute("roomTypes", roomTypes);
            
            CheckInBookingSummary selectedBooking = findSelectedBooking(bookingId).orElse(null);
            request.setAttribute("selectedBooking", selectedBooking);

            if (selectedBooking != null) {
                dao.BookingDao bookingDao = new dao.BookingDao();
                java.util.List<java.util.Map<String, Object>> inspections = bookingDao.getInspectionSummary(bookingId);
                java.util.List<java.util.Map<String, Object>> damageReports = bookingDao.getDamageReports(bookingId);
                java.math.BigDecimal totalDamageAmount = bookingDao.getTotalDamageAmount(bookingId);

                boolean allInspectionsDone = !inspections.isEmpty();
                boolean hasPendingInspection = false;
                for (java.util.Map<String, Object> insp : inspections) {
                    String inspStatus = (String) insp.get("inspectionStatus");
                    if (inspStatus == null || "PENDING".equals(inspStatus)) {
                        hasPendingInspection = true;
                        allInspectionsDone = false;
                    }
                }

                request.setAttribute("inspections", inspections);
                request.setAttribute("damageReports", damageReports);
                request.setAttribute("totalDamageAmount", totalDamageAmount);
                request.setAttribute("allInspectionsDone", allInspectionsDone);
                request.setAttribute("hasPendingInspection", hasPendingInspection);
            }

            request.getRequestDispatcher("/WEB-INF/views/reception/check-out.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("Không thể tải danh sách booking check-out", ex);
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

