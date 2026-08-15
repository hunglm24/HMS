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

    @Override
    public void init() throws ServletException {
        roomDao = new RoomDao();
        roomTypeDao = new RoomTypeDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/rooms".equals(path)) {
            List<Room> rooms = roomDao.findAllWithRoomTypeName();
            List<RoomType> roomTypes = roomTypeDao.findAll();
            req.setAttribute("rooms", rooms);
            req.setAttribute("roomTypes", roomTypes);
            req.getRequestDispatcher("/WEB-INF/views/manager/rooms.jsp").forward(req, resp);
        } else if ("/manager/rooms/delete".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomDao.delete(id);
                req.getSession().setAttribute("toastMessage", "Đã xóa phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            }
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/rooms/save".equals(path)) {
            String idStr = req.getParameter("id");
            String roomTypeIdStr = req.getParameter("roomTypeId");
            String roomNumber = req.getParameter("roomNumber");
            String floorNumberStr = req.getParameter("floorNumber");
            String status = req.getParameter("status");
            String description = req.getParameter("description");

            Room room = new Room();
            room.setRoomTypeId(Long.parseLong(roomTypeIdStr));
            room.setRoomNumber(roomNumber);
            if (floorNumberStr != null && !floorNumberStr.trim().isEmpty()) {
                room.setFloorNumber(Integer.parseInt(floorNumberStr));
            }
            room.setStatus(status);
            room.setDescription(description);

            boolean success = false;
            if (idStr != null && !idStr.trim().isEmpty()) {
                room.setId(Long.parseLong(idStr));
                success = roomDao.update(room);
            } else {
                success = roomDao.insert(room);
            }

            if (success) {
                req.getSession().setAttribute("toastMessage", "Lưu thông tin phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "Có lỗi xảy ra khi lưu phòng");
                req.getSession().setAttribute("toastType", "error");
            }
            
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
