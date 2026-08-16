package controller.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/technician/equipment", "/technician/equipment-detail"})
public class EquipmentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = "/technician/equipment-detail".equals(request.getServletPath())
                ? "/WEB-INF/views/technician/equipment-detail.jsp"
                : "/WEB-INF/views/technician/equipment-list.jsp";
        request.getRequestDispatcher(view).forward(request, response);
    }
}
