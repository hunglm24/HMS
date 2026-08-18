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
                
                // Validation: Check-in < Check-out
                if (!checkIn.isBefore(checkOut)) {
                    session.setAttribute("error", "Ngày trả phòng phải sau ngày nhận phòng.");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                
                // Validation: All cart items must have the same dates
                if (!cart.isEmpty()) {
                    CartItem first = cart.get(0);
                    if (!checkIn.equals(first.getCheckIn()) || !checkOut.equals(first.getCheckOut())) {
                        session.setAttribute("error", "Chỉ có thể đặt phòng trong cùng một khoảng thời gian. Vui lòng thanh toán giỏ hàng hiện tại hoặc xóa các phòng cũ.");
                        response.sendRedirect(request.getContextPath() + "/cart");
                        return;
                    }
                }
                
                RoomType rt = roomTypeDao.findById(roomId).orElse(null);
                if (rt != null && "ACTIVE".equals(rt.getStatus())) {
                    // Validation: Capacity check
                    if (guests > rt.getCapacity() * quantity) {
                        session.setAttribute("error", "Số khách vượt quá sức chứa của số phòng được chọn.");
                        response.sendRedirect(request.getContextPath() + "/cart");
                        return;
                    }
                    
                    // Check existing quantity in cart for this room type
                    int existingQuantity = 0;
                    CartItem existingItem = null;
                    for (CartItem item : cart) {
                        if (item.getRoomType().getId() == roomId) {
                            existingQuantity = item.getQuantity();
                            existingItem = item;
                            break;
                        }
                    }
                    
                    int totalRequested = quantity + existingQuantity;
                    
                    // Re-check availability from database
                    java.util.List<RoomType> available = roomTypeDao.findAvailableRoomTypes(
                            checkIn, checkOut, 1, totalRequested, null, null, null, roomId);
                    
                    if (!available.isEmpty() && available.get(0).getAvailableQuantity() >= totalRequested) {
                        if (existingItem != null) {
                            existingItem.setQuantity(totalRequested);
                            existingItem.setGuests(existingItem.getGuests() + guests);
                        } else {
                            cart.add(new CartItem(rt, checkIn, checkOut, quantity, guests));
                        }
                        session.setAttribute("toastMessage", "Đã thêm phòng vào giỏ hàng");
                        session.setAttribute("toastType", "toast-success");
                    } else {
                        session.setAttribute("error", "Số lượng phòng trống không đủ (" + (available.isEmpty() ? 0 : available.get(0).getAvailableQuantity()) + " phòng).");
                    }
                } else {
                    session.setAttribute("error", "Loại phòng không hợp lệ hoặc đã ngừng hoạt động.");
                }
            } catch (Exception e) {
                session.setAttribute("error", "Dữ liệu không hợp lệ.");
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
        } else if ("update".equals(action)) {
            try {
                int index = Integer.parseInt(request.getParameter("index"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                if (index >= 0 && index < cart.size()) {
                    if (quantity <= 0) {
                        cart.remove(index);
                    } else {
                        CartItem item = cart.get(index);
                        if (item.getGuests() > item.getRoomType().getCapacity() * quantity) {
                            session.setAttribute("error", "Số lượng phòng quá ít so với số lượng khách.");
                        } else {
                            java.util.List<RoomType> available = roomTypeDao.findAvailableRoomTypes(
                                    item.getCheckIn(), item.getCheckOut(), 1, quantity, null, null, null, item.getRoomType().getId());
                            if (!available.isEmpty() && available.get(0).getAvailableQuantity() >= quantity) {
                                item.setQuantity(quantity);
                            } else {
                                session.setAttribute("error", "Không đủ số lượng phòng trống.");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}

