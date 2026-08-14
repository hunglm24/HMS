package controller.search;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SearchRoomServlet", urlPatterns = {"/search-room"})
public class SearchRoomServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xử lý hiển thị trang JSP
        request.getRequestDispatcher("/WEB-INF/jsp/search/search-room.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Xử lý logic nghiệp vụ và chuyển hướng/forward
    }
}
