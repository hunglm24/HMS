package controller.search;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet(name = "SearchRoomServlet", urlPatterns = {"/search-room", "/search"})
public class SearchRoomServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        validateDates(request);
        request.getRequestDispatcher("/WEB-INF/views/public/search-results.jsp").forward(request, response);
    }

    private void validateDates(HttpServletRequest request) {
        String checkIn = request.getParameter("checkIn");
        String checkOut = request.getParameter("checkOut");
        LocalDate today = LocalDate.now();
        try {
            LocalDate in = checkIn == null || checkIn.isBlank() ? null : LocalDate.parse(checkIn);
            LocalDate out = checkOut == null || checkOut.isBlank() ? null : LocalDate.parse(checkOut);
            if (in != null && in.isBefore(today)) {
                request.setAttribute("dateError", "Không được chọn ngày nhận phòng trong quá khứ.");
            } else if (out != null && out.isBefore(today)) {
                request.setAttribute("dateError", "Không được chọn ngày trả phòng trong quá khứ.");
            } else if (in != null && out != null && !out.isAfter(in)) {
                request.setAttribute("dateError", "Ngày trả phòng phải sau ngày nhận phòng.");
            }
        } catch (DateTimeParseException ex) {
            request.setAttribute("dateError", "Ngày không đúng định dạng.");
        }
    }
}
