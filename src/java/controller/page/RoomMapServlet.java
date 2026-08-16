package controller.page;

import dao.RoomDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/reception/room-map"})
public class RoomMapServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RoomDao roomDao;

    @Override
    public void init() throws ServletException {
        // Khá»Ÿi táº¡o DAO 1 láº§n duy nháº¥t khi Servlet Ä‘Æ°á»£c load vÃ o bá»™ nhá»›
        roomDao = new RoomDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // BÆ¯á»šC 1: Láº¤Y Táº¤T Cáº¢ Dá»® LIá»†U Tá»ª DATABASE
        List<Room> allRooms = roomDao.findAllWithRoomTypeName();

        // BÆ¯á»šC 2: Äá»ŒC VÃ€ CHUáº¨N HÃ“A CÃC THAM Sá» FILTER Tá»ª GIAO DIá»†N (URL / Form)
        String search = req.getParameter("search");           // Ã” tÃ¬m kiáº¿m tá»« khÃ³a
        String status = req.getParameter("status");           // Tráº¡ng thÃ¡i (AVAILABLE, OCCUPIED...)
        Integer floor = parseIntSafely(req.getParameter("floor"));             // Táº§ng (1, 2, 3...)
        Integer roomTypeId = parseIntSafely(req.getParameter("roomTypeId"));   // ID loáº¡i phÃ²ng

        // Chuáº©n hÃ³a chuá»—i Ä‘á»ƒ so sÃ¡nh khÃ´ng phÃ¢n biá»‡t hoa/thÆ°á»ng, cáº¯t khoáº£ng tráº¯ng thá»«a
        if (search != null) search = search.trim().toLowerCase();
        if (status != null) status = status.trim().toUpperCase();

        // BÆ¯á»šC 3: KHá»žI Táº O CÃC BIáº¾N CHá»¨A Káº¾T QUáº¢
        // Biáº¿n Ä‘áº¿m thá»‘ng kÃª cho cÃ¡c tháº» badge trÃªn cÃ¹ng
        long available = 0, occupied = 0, cleaning = 0, maintenance = 0;
        
        // Map gom nhÃ³m phÃ²ng theo táº§ng: Key = Sá»‘ táº§ng (int), Value = Danh sÃ¡ch phÃ²ng cá»§a táº§ng Ä‘Ã³
        // DÃ¹ng LinkedHashMap Ä‘á»ƒ giá»¯ Ä‘Ãºng thá»© tá»± táº§ng khi duyá»‡t
        Map<Integer, List<Room>> roomsByFloor = new LinkedHashMap<>();

        // BÆ¯á»šC 4: VÃ’NG Láº¶P DUY NHáº¤T (Vá»«a thá»‘ng kÃª tá»•ng thá»ƒ, vá»«a lá»c vÃ  gom nhÃ³m)
        for (Room r : allRooms) {
            
            // 4.1. Thá»‘ng kÃª sá»‘ lÆ°á»£ng (LuÃ´n Ä‘áº¿m trÃªn toÃ n bá»™ allRooms)
            String rStatus = (r.getStatus() != null) ? r.getStatus().toUpperCase() : "";
            switch (rStatus) {
                case "AVAILABLE":
        available++;
        break;
    case "OCCUPIED":
        occupied++;
        break;
    case "CLEANING":
        cleaning++;
        break;
    case "MAINTENANCE":
        maintenance++;
        break;
    default:
        // Xá»­ lÃ½ cÃ¡c tráº¡ng thÃ¡i láº¡ hoáº·c khÃ´ng xÃ¡c Ä‘á»‹nh
        break;
            }

            // 4.2. Kiá»ƒm tra cÃ¡c Ä‘iá»u kiá»‡n lá»c (Ai khÃ´ng Ä‘áº¡t tiÃªu chuáº©n thÃ¬ 'continue' bá» qua)
            
            // Lá»c theo tráº¡ng thÃ¡i
            if (status != null && !status.isEmpty() && !"ALL".equals(status) && !status.equals(rStatus)) {
                continue;
            }

            // Lá»c theo táº§ng
            if (floor != null && (r.getFloorNumber() == null || !floor.equals(r.getFloorNumber()))) {
                continue;
            }

            // Lá»c theo loáº¡i phÃ²ng (So sÃ¡nh an toÃ n cho cáº£ int vÃ  Integer)
            if (roomTypeId != null && roomTypeId.intValue() != r.getRoomTypeId()) {
                continue;
            }

            // Lá»c theo tá»« khÃ³a tÃ¬m kiáº¿m (Sá»‘ phÃ²ng hoáº·c TÃªn loáº¡i phÃ²ng)
            if (search != null && !search.isEmpty()) {
                boolean matchRoomNum = r.getRoomNumber() != null && r.getRoomNumber().toLowerCase().contains(search);
                boolean matchTypeName = r.getRoomTypeName() != null && r.getRoomTypeName().toLowerCase().contains(search);
                
                // Náº¿u cáº£ sá»‘ phÃ²ng vÃ  tÃªn loáº¡i phÃ²ng Ä‘á»u khÃ´ng khá»›p tá»« khÃ³a -> Bá» qua
                if (!matchRoomNum && !matchTypeName) {
                    continue;
                }
            }

            // 4.3. PhÃ²ng nÃ o sá»‘ng sÃ³t qua cÃ¡c bá»™ lá»c thÃ¬ gom vÃ o táº§ng tÆ°Æ¡ng á»©ng
            int floorNum = (r.getFloorNumber() != null) ? r.getFloorNumber() : 0;
            roomsByFloor.computeIfAbsent(floorNum, k -> new ArrayList<>()).add(r);
        }

        // BÆ¯á»šC 5: TRUYá»€N Dá»® LIá»†U SANG JSP
        // 5.1. Dá»¯ liá»‡u hiá»ƒn thá»‹ sÆ¡ Ä‘á»“ phÃ²ng
        req.setAttribute("roomsByFloor", roomsByFloor);

        // 5.2. Sá»‘ lÆ°á»£ng thá»‘ng kÃª tá»•ng quan
        req.setAttribute("availableCount", available);
        req.setAttribute("occupiedCount", occupied);
        req.setAttribute("cleaningCount", cleaning);
        req.setAttribute("maintenanceCount", maintenance);
        req.setAttribute("totalCount", allRooms.size());

        // 5.3. Giá»¯ láº¡i giÃ¡ trá»‹ vá»«a filter Ä‘á»ƒ hiá»ƒn thá»‹ láº¡i trÃªn Form (Selected / Value)
        req.setAttribute("currentSearch", req.getParameter("search"));
        req.setAttribute("currentStatus", (status == null || status.isEmpty()) ? "ALL" : status);
        req.setAttribute("currentFloor", req.getParameter("floor"));
        req.setAttribute("currentRoomTypeId", req.getParameter("roomTypeId"));

        // Chuyá»ƒn tiáº¿p tá»›i trang JSP giao diá»‡n
        req.getRequestDispatcher("/WEB-INF/views/reception/room-map.jsp").forward(req, resp);
    }

    /**
     * HÃ m phá»¥ trá»£ chuyá»ƒn Ä‘á»•i String sang Integer an toÃ n, trÃ¡nh vÄƒng lá»—i NumberFormatException
     */
    private Integer parseIntSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
