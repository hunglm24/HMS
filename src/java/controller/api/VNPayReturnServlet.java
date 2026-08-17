package controller.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import config.VNPayConfig;

@WebServlet(name = "VNPayReturnServlet", urlPatterns = {"/payment-return"})
public class VNPayReturnServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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

        // Ideally, we would verify the signature using VNPayConfig.hmacSHA512
        // and process the booking insertion to DB here if vnp_ResponseCode == "00"
        
        String responseCode = request.getParameter("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            HttpSession session = request.getSession();
            // Xóa giỏ hàng
            session.removeAttribute("cart");
            request.setAttribute("paymentStatus", "SUCCESS");
        } else {
            // Thanh toán thất bại
            request.setAttribute("paymentStatus", "FAILED");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/public/payment-redirect.jsp").forward(request, response);
    }
}
