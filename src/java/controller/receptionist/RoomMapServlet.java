package controller.receptionist;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "RoomMapServlet", urlPatterns = {"/receptionist/room-map"})
public class RoomMapServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xử lý hiển thị trang JSP
        request.getRequestDispatcher("/WEB-INF/jsp/receptionist/room-map.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xử lý logic nghiệp vụ và chuyển hướng/forward
    }
}
