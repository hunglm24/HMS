package controller.api;

import dao.BookingDao;
import model.Booking;
import service.VNPayService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet(name = "VNPayCreatePaymentServlet", urlPatterns = {"/api/vnpay-create"})
public class VNPayCreatePaymentServlet extends HttpServlet {

    private BookingDao bookingDao;
    private VNPayService vnPayService;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
        vnPayService = new VNPayService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingIdStr = request.getParameter("bookingId");
        
        if (bookingIdStr == null || bookingIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing booking ID");
            return;
        }
        
        try {
            long bookingId = Long.parseLong(bookingIdStr);
            Optional<Booking> bookingOpt = bookingDao.findById(bookingId);
            
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                if (!"PENDING_PAYMENT".equals(booking.getStatus())) {
                    response.sendRedirect(request.getContextPath() + "/booking-confirmation?id=" + bookingId);
                    return;
                }
                
                String orderInfo = "Thanh toan don dat phong " + booking.getBookingCode();
                String ipAddress = request.getRemoteAddr();
                if (ipAddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
                    ipAddress = "127.0.0.1";
                }
                
                // Assuming we require full payment or a fixed deposit. Here we use total amount for simplicity.
                BigDecimal amount = booking.getTotalAmount();
                
                String paymentUrl = vnPayService.createPaymentUrl(orderInfo, amount, ipAddress, String.valueOf(bookingId));
                
                response.sendRedirect(paymentUrl);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
