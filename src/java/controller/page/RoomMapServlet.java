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

    private RoomDao roomDao;

    @Override
    public void init() throws ServletException {
        // Khởi tạo DAO 1 lần duy nhất khi Servlet được load vào bộ nhớ
        roomDao = new RoomDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // BƯỚC 1: LẤY TẤT CẢ DỮ LIỆU TỪ DATABASE
        List<Room> allRooms = roomDao.findAllWithRoomTypeName();

        // BƯỚC 2: ĐỌC VÀ CHUẨN HÓA CÁC THAM SỐ FILTER TỪ GIAO DIỆN (URL / Form)
        String search = req.getParameter("search");           // Ô tìm kiếm từ khóa
        String status = req.getParameter("status");           // Trạng thái (AVAILABLE, OCCUPIED...)
        Integer floor = parseIntSafely(req.getParameter("floor"));             // Tầng (1, 2, 3...)
        Integer roomTypeId = parseIntSafely(req.getParameter("roomTypeId"));   // ID loại phòng

        // Chuẩn hóa chuỗi để so sánh không phân biệt hoa/thường, cắt khoảng trắng thừa
        if (search != null) search = search.trim().toLowerCase();
        if (status != null) status = status.trim().toUpperCase();

        // BƯỚC 3: KHỞI TẠO CÁC BIẾN CHỨA KẾT QUẢ
        // Biến đếm thống kê cho các thẻ badge trên cùng
        long available = 0, occupied = 0, cleaning = 0, maintenance = 0;
        
        // Map gom nhóm phòng theo tầng: Key = Số tầng (int), Value = Danh sách phòng của tầng đó
        // Dùng LinkedHashMap để giữ đúng thứ tự tầng khi duyệt
        Map<Integer, List<Room>> roomsByFloor = new LinkedHashMap<>();

        // BƯỚC 4: VÒNG LẶP DUY NHẤT (Vừa thống kê tổng thể, vừa lọc và gom nhóm)
        for (Room r : allRooms) {
            
            // 4.1. Thống kê số lượng (Luôn đếm trên toàn bộ allRooms)
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
        // Xử lý các trạng thái lạ hoặc không xác định
        break;
            }

            // 4.2. Kiểm tra các điều kiện lọc (Ai không đạt tiêu chuẩn thì 'continue' bỏ qua)
            
            // Lọc theo trạng thái
            if (status != null && !status.isEmpty() && !"ALL".equals(status) && !status.equals(rStatus)) {
                continue;
            }

            // Lọc theo tầng
            if (floor != null && (r.getFloorNumber() == null || !floor.equals(r.getFloorNumber()))) {
                continue;
            }

            // Lọc theo loại phòng (So sánh an toàn cho cả int và Integer)
            if (roomTypeId != null && roomTypeId.intValue() != r.getRoomTypeId()) {
                continue;
            }

            // Lọc theo từ khóa tìm kiếm (Số phòng hoặc Tên loại phòng)
            if (search != null && !search.isEmpty()) {
                boolean matchRoomNum = r.getRoomNumber() != null && r.getRoomNumber().toLowerCase().contains(search);
                boolean matchTypeName = r.getRoomTypeName() != null && r.getRoomTypeName().toLowerCase().contains(search);
                
                // Nếu cả số phòng và tên loại phòng đều không khớp từ khóa -> Bỏ qua
                if (!matchRoomNum && !matchTypeName) {
                    continue;
                }
            }

            // 4.3. Phòng nào sống sót qua các bộ lọc thì gom vào tầng tương ứng
            int floorNum = (r.getFloorNumber() != null) ? r.getFloorNumber() : 0;
            roomsByFloor.computeIfAbsent(floorNum, k -> new ArrayList<>()).add(r);
        }

        // BƯỚC 5: TRUYỀN DỮ LIỆU SANG JSP
        // 5.1. Dữ liệu hiển thị sơ đồ phòng
        req.setAttribute("roomsByFloor", roomsByFloor);

        // 5.2. Số lượng thống kê tổng quan
        req.setAttribute("availableCount", available);
        req.setAttribute("occupiedCount", occupied);
        req.setAttribute("cleaningCount", cleaning);
        req.setAttribute("maintenanceCount", maintenance);
        req.setAttribute("totalCount", allRooms.size());

        // 5.3. Giữ lại giá trị vừa filter để hiển thị lại trên Form (Selected / Value)
        req.setAttribute("currentSearch", req.getParameter("search"));
        req.setAttribute("currentStatus", (status == null || status.isEmpty()) ? "ALL" : status);
        req.setAttribute("currentFloor", req.getParameter("floor"));
        req.setAttribute("currentRoomTypeId", req.getParameter("roomTypeId"));

        // Chuyển tiếp tới trang JSP giao diện
        req.getRequestDispatcher("/WEB-INF/views/reception/room-map.jsp").forward(req, resp);
    }

    /**
     * Hàm phụ trợ chuyển đổi String sang Integer an toàn, tránh văng lỗi NumberFormatException
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