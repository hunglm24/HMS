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
    private static final long serialVersionUID = 1L;

    private RoomDao roomDao;
    private RoomTypeDao roomTypeDao;

    // Khá»Ÿi táº¡o cÃ¡c DAO cáº§n thiáº¿t Ä‘á»ƒ láº¥y dá»¯ liá»‡u
    @Override
    public void init() throws ServletException {
        roomDao = new RoomDao();
        roomTypeDao = new RoomTypeDao();
    }

    // Xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng truy cáº­p trang báº±ng URL (GET request)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/rooms".equals(path)) {
            // Cáº§n láº¥y danh sÃ¡ch phÃ²ng Ä‘á»ƒ hiá»ƒn thá»‹ ra báº£ng
            List<Room> rooms = roomDao.findAllWithRoomTypeName();
            // Cáº§n láº¥y danh sÃ¡ch loáº¡i phÃ²ng Ä‘á»ƒ Ä‘á»• vÃ o tháº» <select> khi ThÃªm/Sá»­a phÃ²ng
            List<RoomType> roomTypes = roomTypeDao.findAll();
            
            // Gá»­i dá»¯ liá»‡u qua cho JSP
            req.setAttribute("rooms", rooms);
            req.setAttribute("roomTypes", roomTypes);
            req.getRequestDispatcher("/WEB-INF/views/manager/rooms.jsp").forward(req, resp);
            
        } else if ("/manager/rooms/delete".equals(path)) {
            // Láº¥y ID phÃ²ng cáº§n xÃ³a tá»« tham sá»‘ URL
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomDao.delete(id); // Thá»±c thi lá»‡nh xÃ³a
                req.getSession().setAttribute("toastMessage", "ÄÃ£ xÃ³a phÃ²ng thÃ nh cÃ´ng");
                req.getSession().setAttribute("toastType", "success");
            }
            // Quay vá» láº¡i trang danh sÃ¡ch phÃ²ng
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");
            
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng submit form thÃªm hoáº·c sá»­a phÃ²ng (POST request)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/rooms/save".equals(path)) {
            // Há»©ng toÃ n bá»™ dá»¯ liá»‡u tá»« cÃ¡c tháº» <input> vÃ  <select> trong JSP
            String idStr = req.getParameter("id");
            String roomTypeIdStr = req.getParameter("roomTypeId");
            String roomNumber = req.getParameter("roomNumber");
            String floorNumberStr = req.getParameter("floorNumber");
            String status = req.getParameter("status");
            String description = req.getParameter("description");

            // ÄÃ³ng gÃ³i dá»¯ liá»‡u vÃ o má»™t Ä‘á»‘i tÆ°á»£ng Room
            Room room = new Room();
            room.setRoomTypeId(Long.parseLong(roomTypeIdStr));
            room.setRoomNumber(roomNumber);
            
            // LÆ°u Ã½: Táº§ng cÃ³ thá»ƒ bá»‹ bá» trá»‘ng (null)
            if (floorNumberStr != null && !floorNumberStr.trim().isEmpty()) {
                room.setFloorNumber(Integer.parseInt(floorNumberStr));
            }
            room.setStatus(status);
            room.setDescription(description);

            boolean success = false;
            // Dá»±a vÃ o ID Ä‘á»ƒ biáº¿t lÃ  ngÆ°á»i dÃ¹ng Ä‘ang cáº­p nháº­t phÃ²ng cÅ© hay thÃªm phÃ²ng má»›i
            if (idStr != null && !idStr.trim().isEmpty()) {
                room.setId(Long.parseLong(idStr));
                success = roomDao.update(room);
            } else {
                success = roomDao.insert(room);
            }

            // Ghi nháº­n thÃ´ng bÃ¡o Ä‘á»ƒ JSP cÃ³ thá»ƒ hiá»‡n Popup (Toast) thÃ´ng bÃ¡o
            if (success) {
                req.getSession().setAttribute("toastMessage", "LÆ°u thÃ´ng tin phÃ²ng thÃ nh cÃ´ng");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "CÃ³ lá»—i xáº£y ra khi lÆ°u phÃ²ng");
                req.getSession().setAttribute("toastType", "error");
            }
            
            // Cuá»‘i cÃ¹ng, redirect vá» trang danh sÃ¡ch phÃ²ng
            resp.sendRedirect(req.getContextPath() + "/manager/rooms");
            
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

