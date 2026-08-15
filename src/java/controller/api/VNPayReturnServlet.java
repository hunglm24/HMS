package controller.api;

import config.VNPayConfig;
import dao.BookingDao;
import dao.PaymentDao;
import model.Booking;
import model.Payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "VNPayReturnServlet", urlPatterns = {"/api/vnpay-return"})
public class VNPayReturnServlet extends HttpServlet {

    private BookingDao bookingDao;
    private PaymentDao paymentDao;

    @Override
    public void init() throws ServletException {
        bookingDao = new BookingDao();
        paymentDao = new PaymentDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        
        String signValue = VNPayConfig.hashAllFields(fields);
        
        if (signValue.equals(vnp_SecureHash)) {
            String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // format: random_bookingId
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
            String vnp_Amount = request.getParameter("vnp_Amount");
            
            String[] parts = vnp_TxnRef.split("_");
            if (parts.length == 2) {
                try {
                    long bookingId = Long.parseLong(parts[1]);
                    Optional<Booking> bookingOpt = bookingDao.findById(bookingId);
                    
                    if (bookingOpt.isPresent()) {
                        if ("00".equals(vnp_ResponseCode)) {
                            // Payment success
                            bookingDao.updateStatus(bookingId, "CONFIRMED");
                            
                            // Insert payment record
                            Payment payment = new Payment();
                            payment.setBookingId(bookingId);
                            payment.setAmount(new BigDecimal(vnp_Amount).divide(new BigDecimal(100)));
                            payment.setPaymentMethod("VNPAY");
                            payment.setPaymentType("FULL");
                            payment.setTransactionCode(vnp_TransactionNo);
                            payment.setStatus("COMPLETED");
                            
                            paymentDao.insertPayment(payment);
                            
                            response.sendRedirect(request.getContextPath() + "/booking-confirmation?id=" + bookingId + "&payment=success");
                        } else {
                            // Payment failed
                            response.sendRedirect(request.getContextPath() + "/booking-confirmation?id=" + bookingId + "&payment=failed");
                        }
                        return;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/search?payment=invalid");
    }
}
