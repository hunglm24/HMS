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
    private static final long serialVersionUID = 1L;

    private RoomTypeDao roomTypeDao;

    // HÃ m init cháº¡y má»™t láº§n khi Servlet Ä‘Æ°á»£c khá»Ÿi táº¡o
    @Override
    public void init() throws ServletException {
        roomTypeDao = new RoomTypeDao(); // Khá»Ÿi táº¡o DAO Ä‘á»ƒ gá»i cÃ¡c hÃ m tÆ°Æ¡ng tÃ¡c database
    }

    // Xá»­ lÃ½ cÃ¡c request dáº¡ng GET (vÃ­ dá»¥: gÃµ URL lÃªn trÃ¬nh duyá»‡t hoáº·c click link)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath(); // Láº¥y Ä‘Æ°á»ng dáº«n hiá»‡n táº¡i (vÃ­ dá»¥: "/manager/room-types")
        
        if ("/manager/room-types".equals(path)) {
            // Láº¥y toÃ n bá»™ danh sÃ¡ch loáº¡i phÃ²ng tá»« database
            List<RoomType> roomTypes = roomTypeDao.findAll();
            // Gá»­i dá»¯ liá»‡u nÃ y sang cho JSP hiá»ƒn thá»‹ thÃ´ng qua request attribute
            req.setAttribute("roomTypes", roomTypes);
            // Äiá»u hÆ°á»›ng (forward) tá»›i trang giao diá»‡n JSP
            req.getRequestDispatcher("/WEB-INF/views/manager/room-types.jsp").forward(req, resp);
            
        } else if ("/manager/room-types/delete".equals(path)) {
            // TÃ­nh nÄƒng xÃ³a loáº¡i phÃ²ng
            String idParam = req.getParameter("id"); // Láº¥y ID cáº§n xÃ³a tá»« URL (vÃ­ dá»¥: ?id=5)
            if (idParam != null && !idParam.trim().isEmpty()) {
                long id = Long.parseLong(idParam);
                roomTypeDao.delete(id);
                // LÆ°u cÃ¢u thÃ´ng bÃ¡o thÃ nh cÃ´ng vÃ o session Ä‘á»ƒ hiá»ƒn thá»‹ Toast
                req.getSession().setAttribute("toastMessage", "ÄÃ£ xÃ³a loáº¡i phÃ²ng thÃ nh cÃ´ng");
                req.getSession().setAttribute("toastType", "success");
            }
            // Chuyá»ƒn hÆ°á»›ng láº¡i trang danh sÃ¡ch sau khi xÃ³a xong
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");
            
        } else {
            // Náº¿u truy cáº­p sai URL thÃ¬ bÃ¡o lá»—i 404
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Xá»­ lÃ½ cÃ¡c request dáº¡ng POST (thÆ°á»ng Ä‘áº¿n tá»« Form submit)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        if ("/manager/room-types/save".equals(path)) {
            // Láº¥y táº¥t cáº£ thÃ´ng tin ngÆ°á»i dÃ¹ng nháº­p vÃ o tá»« form thÃªm/sá»­a
            String idStr = req.getParameter("id");
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String capacityStr = req.getParameter("capacity");
            String basePriceStr = req.getParameter("basePrice");
            String status = req.getParameter("status");

            // Táº¡o má»™t Ä‘á»‘i tÆ°á»£ng RoomType má»›i vÃ  nhÃ©t dá»¯ liá»‡u vÃ o
            RoomType rt = new RoomType();
            rt.setName(name);
            rt.setDescription(description);
            rt.setCapacity(Integer.parseInt(capacityStr)); // Chuyá»ƒn chuá»—i thÃ nh sá»‘ nguyÃªn
            rt.setBasePrice(new BigDecimal(basePriceStr)); // Chuyá»ƒn chuá»—i thÃ nh kiá»ƒu tiá»n tá»‡
            rt.setStatus(status);

            boolean success = false;
            // Náº¿u cÃ³ ID truyá»n lÃªn thÃ¬ nghÄ©a lÃ  mÃ¬nh Ä‘ang Sá»¬A, náº¿u khÃ´ng cÃ³ ID lÃ  THÃŠM Má»šI
            if (idStr != null && !idStr.trim().isEmpty()) {
                rt.setId(Long.parseLong(idStr));
                success = roomTypeDao.update(rt); // Cáº­p nháº­t
            } else {
                success = roomTypeDao.insert(rt); // ThÃªm má»›i
            }

            // Ghi nháº­n thÃ´ng bÃ¡o Ä‘á»ƒ hiá»ƒn thá»‹ trÃªn giao diá»‡n
            if (success) {
                req.getSession().setAttribute("toastMessage", "LÆ°u thÃ´ng tin loáº¡i phÃ²ng thÃ nh cÃ´ng");
                req.getSession().setAttribute("toastType", "success");
            } else {
                req.getSession().setAttribute("toastMessage", "CÃ³ lá»—i xáº£y ra khi lÆ°u loáº¡i phÃ²ng");
                req.getSession().setAttribute("toastType", "error");
            }
            
            // Xá»­ lÃ½ xong thÃ¬ redirect (chuyá»ƒn hÆ°á»›ng) vá» láº¡i danh sÃ¡ch
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");
            
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

