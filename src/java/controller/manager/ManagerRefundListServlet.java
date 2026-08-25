package controller.manager;

import dao.BookingRefundDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ManagerRefundListServlet", urlPatterns = {"/manager/refunds"})
public class ManagerRefundListServlet extends HttpServlet {
    private final BookingRefundDao refundDao = new BookingRefundDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("refundRequests", refundDao.findAll(request.getParameter("status")));
            request.getRequestDispatcher("/WEB-INF/views/manager/refund-list.jsp").forward(request, response);
        } catch (Exception ex) {
            throw new ServletException("Không thể tải yêu cầu hoàn tiền.", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            long id = Long.parseLong(request.getParameter("id"));
            String status = request.getParameter("action");
            if (!refundDao.updateStatus(id, status)) throw new IllegalStateException("Yêu cầu đã được xử lý.");
            request.getSession().setAttribute("toastMessage",
                    "COMPLETED".equals(status) ? "Đã xác nhận hoàn tiền." : "Đã từ chối yêu cầu hoàn tiền.");
            request.getSession().setAttribute("toastType", "toast-success");
        } catch (Exception ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/manager/refunds");
    }
}
