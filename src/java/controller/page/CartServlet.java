package controller.page;

import dao.RoomTypeDao;
import dto.Cart;
import dto.CartItem;
import model.RoomType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.Optional;

@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {

    private RoomTypeDao roomTypeDao;

    @Override
    public void init() throws ServletException {
        roomTypeDao = new RoomTypeDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/public/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        try {
            if ("add".equals(action)) {
                long roomTypeId = Long.parseLong(request.getParameter("roomTypeId"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                Date checkIn = Date.valueOf(request.getParameter("checkIn"));
                Date checkOut = Date.valueOf(request.getParameter("checkOut"));
                
                Optional<RoomType> rtOpt = roomTypeDao.findById(roomTypeId);
                if (rtOpt.isPresent()) {
                    RoomType rt = rtOpt.get();
                    CartItem item = new CartItem(rt, quantity, checkIn, checkOut, rt.getBasePrice());
                    cart.addItem(item);
                }
            } else if ("update".equals(action)) {
                long roomTypeId = Long.parseLong(request.getParameter("roomTypeId"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                cart.updateQuantity(roomTypeId, quantity);
            } else if ("remove".equals(action)) {
                long roomTypeId = Long.parseLong(request.getParameter("roomTypeId"));
                cart.removeItem(roomTypeId);
            } else if ("clear".equals(action)) {
                cart.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
