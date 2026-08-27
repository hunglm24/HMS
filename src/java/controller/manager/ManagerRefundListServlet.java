package controller.manager;

import dao.BookingRefundDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import util.LocalFileUtil;
import util.MultipartUtil;

import java.io.IOException;
import java.util.Set;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 5 * 1024 * 1024,         // 5 MB
        maxRequestSize = 10 * 1024 * 1024      // 10 MB
)
@WebServlet(name = "ManagerRefundListServlet", urlPatterns = {"/manager/refunds"})
public class ManagerRefundListServlet extends HttpServlet {
    private static final String REFUND_BILL_DIR = "uploads/refunds";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

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
            if (status == null || (!"COMPLETED".equals(status) && !"REJECTED".equals(status))) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }

            String savedBillImage = null;
            if ("COMPLETED".equals(status)) {
                try {
                    Part billPart = request.getPart("billImage");
                    if (billPart != null && billPart.getSize() > 0) {
                        MultipartUtil.validateImagePart(
                                billPart,
                                MAX_IMAGE_SIZE,
                                ALLOWED_IMAGE_EXTENSIONS,
                                ALLOWED_IMAGE_CONTENT_TYPES,
                                "Ảnh bill hoàn tiền"
                        );
                        savedBillImage = LocalFileUtil.saveImagePart(
                                billPart,
                                getServletContext(),
                                REFUND_BILL_DIR,
                                "refund-bill-" + id
                        );
                    }
                } catch (IllegalArgumentException ex) {
                    request.getSession().setAttribute("error", ex.getMessage());
                    response.sendRedirect(request.getContextPath() + "/manager/refunds");
                    return;
                } catch (Exception ex) {
                    request.getSession().setAttribute("error", "Lỗi khi lưu ảnh bill: " + ex.getMessage());
                    response.sendRedirect(request.getContextPath() + "/manager/refunds");
                    return;
                }
            }

            if (!refundDao.updateStatus(id, status, savedBillImage)) {
                throw new IllegalStateException("Yêu cầu đã được xử lý hoặc không tồn tại.");
            }

            request.getSession().setAttribute("toastMessage",
                    "COMPLETED".equals(status)
                            ? (savedBillImage != null ? "Đã xác nhận hoàn tiền và lưu ảnh bill thành công." : "Đã xác nhận hoàn tiền.")
                            : "Đã từ chối yêu cầu hoàn tiền.");
            request.getSession().setAttribute("toastType", "toast-success");
        } catch (Exception ex) {
            request.getSession().setAttribute("error", ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/manager/refunds");
    }
}
