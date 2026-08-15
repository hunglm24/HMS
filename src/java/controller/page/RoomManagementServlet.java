package controller.page;

import dao.RoomDao;
import dao.RoomTypeDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;
import model.RoomType;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manager/rooms", "/manager/rooms/save", "/manager/rooms/delete"})
public class RoomManagementServlet extends HttpServlet {

    private RoomDao roomDao;
    private RoomTypeDao roomTypeDao;

    // Khởi tạo các DAO cần thiết để lấy dữ liệu
    @Override
    public void init() throws ServletException {
        roomDao = new RoomDao();
        roomTypeDao = new RoomTypeDao();
    }

    // Xử lý khi người dùng truy cập trang bằng URL (GET request)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/rooms".equals(path)) {
            // Cần lấy danh sách phòng để hiển thị ra bảng
            List<Room> rooms = roomDao.findAllWithRoomTypeName();
            // Cần lấy danh sách loại phòng để đổ vào thẻ <select> khi Thêm/Sửa phòng
            List<RoomType> roomTypes = roomTypeDao.findAll();
            
            // Gửi dữ liệu qua cho JSP
            req.setAttribute("rooms", rooms);
            req.setAttribute("roomTypes", roomTypes);
            req.getRequestDispatcher("/WEB-INF/views/manager/rooms.jsp").forward(req, resp);
            
        } else if ("/manager/rooms/delete".equals(path)) {
            // Lấy ID phòng cần xóa từ tham số URL
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomDao.delete(id); // Thực thi lệnh xóa
                req.getSession().setAttribute("toastMessage", "Đã xóa phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            }
            // Quay về lại trang danh sách phòng
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");
            
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Xử lý khi người dùng submit form thêm hoặc sửa phòng (POST request)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/rooms/save".equals(path)) {
            // Hứng toàn bộ dữ liệu từ các thẻ <input> và <select> trong JSP
            String idStr = req.getParameter("id");
            String roomTypeIdStr = req.getParameter("roomTypeId");
            String roomNumber = req.getParameter("roomNumber");
            String floorNumberStr = req.getParameter("floorNumber");
            String status = req.getParameter("status");
            String description = req.getParameter("description");

            // Đóng gói dữ liệu vào một đối tượng Room
            Room room = new Room();
            room.setRoomTypeId(Long.parseLong(roomTypeIdStr));
            room.setRoomNumber(roomNumber);
            
            // Lưu ý: Tầng có thể bị bỏ trống (null)
            if (floorNumberStr != null && !floorNumberStr.trim().isEmpty()) {
                room.setFloorNumber(Integer.parseInt(floorNumberStr));
            }
            room.setStatus(status);
            room.setDescription(description);

            boolean success = false;
            // Dựa vào ID để biết là người dùng đang cập nhật phòng cũ hay thêm phòng mới
            if (idStr != null && !idStr.trim().isEmpty()) {
                room.setId(Long.parseLong(idStr));
                success = roomDao.update(room);
            } else {
                success = roomDao.insert(room);
            }

            // Ghi nhận thông báo để JSP có thể hiện Popup (Toast) thông báo
            if (success) {
                req.getSession().setAttribute("toastMessage", "Lưu thông tin phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "Có lỗi xảy ra khi lưu phòng");
                req.getSession().setAttribute("toastType", "error");
            }
            
            // Cuối cùng, redirect về trang danh sách phòng
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");
            
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
