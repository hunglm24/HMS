package controller.page;

import dao.RoomTypeDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.RoomType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/manager/room-types", "/manager/room-types/save", "/manager/room-types/delete"})
public class RoomTypeManagementServlet extends HttpServlet {

    private RoomTypeDao roomTypeDao;

    // Hàm init chạy một lần khi Servlet được khởi tạo
    @Override
    public void init() throws ServletException {
        roomTypeDao = new RoomTypeDao(); // Khởi tạo DAO để gọi các hàm tương tác database
    }

    // Xử lý các request dạng GET (ví dụ: gõ URL lên trình duyệt hoặc click link)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath(); // Lấy đường dẫn hiện tại (ví dụ: "/manager/room-types")
        
        if ("/manager/room-types".equals(path)) {
            // Lấy toàn bộ danh sách loại phòng từ database
            List<RoomType> roomTypes = roomTypeDao.findAll();
            // Gửi dữ liệu này sang cho JSP hiển thị thông qua request attribute
            req.setAttribute("roomTypes", roomTypes);
            // Điều hướng (forward) tới trang giao diện JSP
            req.getRequestDispatcher("/WEB-INF/views/manager/room-types.jsp").forward(req, resp);
            
        } else if ("/manager/room-types/delete".equals(path)) {
            // Tính năng xóa loại phòng
            String idParam = req.getParameter("id"); // Lấy ID cần xóa từ URL (ví dụ: ?id=5)
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomTypeDao.delete(id);
                // Lưu câu thông báo thành công vào session để hiển thị Toast
                req.getSession().setAttribute("toastMessage", "Đã xóa loại phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            }
            // Chuyển hướng lại trang danh sách sau khi xóa xong
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");
            
        } else {
            // Nếu truy cập sai URL thì báo lỗi 404
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Xử lý các request dạng POST (thường đến từ Form submit)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/room-types/save".equals(path)) {
            // Lấy tất cả thông tin người dùng nhập vào từ form thêm/sửa
            String idStr = req.getParameter("id");
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String capacityStr = req.getParameter("capacity");
            String basePriceStr = req.getParameter("basePrice");
            String status = req.getParameter("status");

            // Tạo một đối tượng RoomType mới và nhét dữ liệu vào
            RoomType rt = new RoomType();
            rt.setName(name);
            rt.setDescription(description);
            rt.setCapacity(Integer.parseInt(capacityStr)); // Chuyển chuỗi thành số nguyên
            rt.setBasePrice(new BigDecimal(basePriceStr)); // Chuyển chuỗi thành kiểu tiền tệ
            rt.setStatus(status);

            boolean success = false;
            // Nếu có ID truyền lên thì nghĩa là mình đang SỬA, nếu không có ID là THÊM MỚI
            if (idStr != null && !idStr.trim().isEmpty()) {
                rt.setId(Long.parseLong(idStr));
                success = roomTypeDao.update(rt); // Cập nhật
            } else {
                success = roomTypeDao.insert(rt); // Thêm mới
            }

            // Ghi nhận thông báo để hiển thị trên giao diện
            if (success) {
                req.getSession().setAttribute("toastMessage", "Lưu thông tin loại phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "Có lỗi xảy ra khi lưu loại phòng");
                req.getSession().setAttribute("toastType", "error");
            }
            
            // Xử lý xong thì redirect (chuyển hướng) về lại danh sách
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");
            
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
