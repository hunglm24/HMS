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

    @Override
    public void init() throws ServletException {
        roomTypeDao = new RoomTypeDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/room-types".equals(path)) {
            List<RoomType> roomTypes = roomTypeDao.findAll();
            req.setAttribute("roomTypes", roomTypes);
            req.getRequestDispatcher("/WEB-INF/views/manager/room-types.jsp").forward(req, resp);
        } else if ("/manager/room-types/delete".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomTypeDao.delete(id);
                req.getSession().setAttribute("toastMessage", "Đã xóa loại phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            }
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/room-types/save".equals(path)) {
            String idStr = req.getParameter("id");
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String capacityStr = req.getParameter("capacity");
            String basePriceStr = req.getParameter("basePrice");
            String status = req.getParameter("status");

            RoomType rt = new RoomType();
            rt.setName(name);
            rt.setDescription(description);
            rt.setCapacity(Integer.parseInt(capacityStr));
            rt.setBasePrice(new BigDecimal(basePriceStr));
            rt.setStatus(status);

            boolean success = false;
            if (idStr != null && !idStr.trim().isEmpty()) {
                rt.setId(Long.parseLong(idStr));
                success = roomTypeDao.update(rt);
            } else {
                success = roomTypeDao.insert(rt);
            }

            if (success) {
                req.getSession().setAttribute("toastMessage", "Lưu thông tin loại phòng thành công");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "Có lỗi xảy ra khi lưu loại phòng");
                req.getSession().setAttribute("toastType", "error");
            }
            
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
