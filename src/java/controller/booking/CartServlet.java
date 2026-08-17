package controller.booking;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import dto.CartItem;
import model.RoomType;
import dao.RoomTypeDao;

@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RoomTypeDao roomTypeDao = new RoomTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
        request.getRequestDispatcher("/WEB-INF/views/public/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
        
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        String action = request.getParameter("action");
        if ("add".equals(action)) {
            try {
                long roomId = Long.parseLong(request.getParameter("roomId"));
                LocalDate checkIn = LocalDate.parse(request.getParameter("checkIn"));
                LocalDate checkOut = LocalDate.parse(request.getParameter("checkOut"));
                int guests = Integer.parseInt(request.getParameter("guests"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                
                RoomType rt = roomTypeDao.findById(roomId).orElse(null);
                if (rt != null) {
                    cart.add(new CartItem(rt, checkIn, checkOut, quantity, guests));
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        } else if ("remove".equals(action)) {
            try {
                int index = Integer.parseInt(request.getParameter("index"));
                if (index >= 0 && index < cart.size()) {
                    cart.remove(index);
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}

