package controller.page;

import dao.HotelPolicyDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/hotel-policy"})
public class HotelPolicyGuestServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/public/hotel-policy.jsp";

    private final HotelPolicyDao policyDao = new HotelPolicyDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("policy", policyDao.findLatestActive().orElse(null));
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Cannot load guest hotel policy page", ex);
        }
    }
}
