package controller.receptionist;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dao.RoomTypeDao;
import model.RoomType;
import java.util.List;

@WebServlet(name = "WalkInReservationServlet", urlPatterns = {"/receptionist/walk-in"})
public class WalkInReservationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RoomTypeDao roomTypeDao = new RoomTypeDao();

    private dao.RoomDao roomDao = new dao.RoomDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<RoomType> roomTypes = roomTypeDao.findAll();
        request.setAttribute("roomTypes", roomTypes);

        String checkInStr = request.getParameter("checkIn");
        String checkOutStr = request.getParameter("checkOut");
        String roomTypeIdStr = request.getParameter("roomTypeId");

        if (checkInStr != null && checkOutStr != null && !checkInStr.isBlank() && !checkOutStr.isBlank()) {
            try {
                java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInStr);
                java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutStr);
                Long roomTypeId = null;
                if (roomTypeIdStr != null && !roomTypeIdStr.isBlank()) {
                    roomTypeId = Long.parseLong(roomTypeIdStr);
                }
                List<model.Room> availablePhysicalRooms = roomDao.findAvailablePhysicalRooms(checkIn, checkOut, roomTypeId);
                request.setAttribute("availablePhysicalRooms", availablePhysicalRooms);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/reception/walk-in-booking.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Here we would ideally create a booking as CHECKED_IN directly or CONFIRMED
        // For now, redirect to manage booking list
        response.sendRedirect(request.getContextPath() + "/reception/bookings");
    }
}

