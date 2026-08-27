package controller.page.publics;

import dao.HotelPolicyDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HotelPolicy;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "HotelPolicyServlet", urlPatterns = {"/hotel-policy"})
public class HotelPolicyServlet extends HttpServlet {
    private HotelPolicyDao policyDao;

    @Override
    public void init() throws ServletException {
        policyDao = new HotelPolicyDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Load the newest hotel-policy record so guests always see the same policy as managers.
            HotelPolicy policy = policyDao.findLatestHotelPolicy().orElse(null);
            request.setAttribute("policy", policy);
            request.getRequestDispatcher("/WEB-INF/views/public/hotel-policy.jsp").forward(request, response);
        } catch (SQLException ex) {
            getServletContext().log("Cannot load hotel policy page", ex);
            request.setAttribute("policy", null);
            request.getRequestDispatcher("/WEB-INF/views/public/hotel-policy.jsp").forward(request, response);
        }
    }
}
