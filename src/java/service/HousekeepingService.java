package service;

import dao.HousekeepingDao;
import model.HousekeepingTask;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

public class HousekeepingService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> CONDITIONS = Set.of("NORMAL", "DAMAGED", "MISSING");
    private static final Set<String> TASK_TYPES = Set.of("CHECKOUT_INSPECTION", "CLEANING");
    private static final Set<String> TASK_STATUSES = Set.of("PENDING", "IN_PROGRESS");
    private static final Map<String, String> SORTS = Map.of(
            "room", "rm.room_number", "roomType", "rt.name", "floor", "rm.floor_number",
            "taskType", "ht.task_type", "status", "ht.status", "created", "ht.created_at");
    private final HousekeepingDao dao;

    public HousekeepingService() { this(new HousekeepingDao()); }
    public HousekeepingService(HousekeepingDao dao) { this.dao = dao; }

    public TaskPage getTaskPage(long viewerId, String view, String keyword, Integer floor,
                                String taskType, String status, String sort,
                                String direction, int requestedPage) throws SQLException {
        String selectedView = "mine".equals(view) ? "mine" : "waiting";
        String normalizedKeyword = normalizeKeyword(keyword);
        Integer normalizedFloor = floor != null && floor >= 0 && floor <= 999 ? floor : null;
        String normalizedType = taskType != null && TASK_TYPES.contains(taskType) ? taskType : null;
        String normalizedStatus = status != null && TASK_STATUSES.contains(status) ? status : null;
        String normalizedSort = sort != null && SORTS.containsKey(sort) ? sort : "room";
        String normalizedDirection = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        String sortColumn = "waiting".equals(selectedView) && Set.of("taskType", "status", "created").contains(normalizedSort)
                ? "rm.room_number" : SORTS.get(normalizedSort);
        int totalItems = "mine".equals(selectedView)
                ? dao.countMyTasks(viewerId, normalizedKeyword, normalizedFloor, normalizedType, normalizedStatus)
                : dao.countPendingInspectionRooms(normalizedKeyword, normalizedFloor);
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        int offset = (page - 1) * PAGE_SIZE;
        List<HousekeepingTask> tasks = "mine".equals(selectedView)
                ? dao.findMyTasks(viewerId, normalizedKeyword, normalizedFloor, normalizedType,
                    normalizedStatus, sortColumn, normalizedDirection, offset, PAGE_SIZE)
                : dao.findPendingInspectionRooms(normalizedKeyword, normalizedFloor, sortColumn,
                    normalizedDirection, offset, PAGE_SIZE);
        return new TaskPage(tasks, page, totalPages, totalItems, selectedView,
                normalizedKeyword, normalizedFloor, normalizedType, normalizedStatus,
                normalizedSort, normalizedDirection.toLowerCase());
    }

    public Optional<HousekeepingTask> getTaskDetail(long taskId, long viewerId) throws SQLException {
        if (taskId <= 0) return Optional.empty();
        return dao.findById(taskId, viewerId);
    }

    public List<HousekeepingTask.EquipmentCheck> getEquipment(long roomId, Long bookingRoomId) throws SQLException {
        return dao.findEquipment(roomId, bookingRoomId);
    }

    public long claimInspection(long bookingRoomId, long staffId) throws SQLException {
        if (bookingRoomId <= 0) throw new IllegalArgumentException("Phòng cần kiểm tra không hợp lệ");
        return dao.claimInspection(bookingRoomId, staffId);
    }

    public void completeInspection(long taskId, long staffId,
                                   List<HousekeepingTask.EquipmentCheck> checks,
                                   String note) throws SQLException {
        if (checks == null) throw new IllegalArgumentException("Thiếu kết quả kiểm tra thiết bị");
        for (HousekeepingTask.EquipmentCheck check : checks) {
            if (check.getConditionStatus() == null || !CONDITIONS.contains(check.getConditionStatus())) {
                throw new IllegalArgumentException("Trạng thái thiết bị không hợp lệ");
            }
            if (check.getQuantity() <= 0) throw new IllegalArgumentException("Số lượng thiết bị không hợp lệ");
            BigDecimal fee = check.getDamageFee();
            if (fee == null || fee.signum() < 0) throw new IllegalArgumentException("Phí bồi thường không hợp lệ");
            if ("NORMAL".equals(check.getConditionStatus())) check.setDamageFee(BigDecimal.ZERO);
            check.setNote(trim(check.getNote(), 1000));
        }
        dao.completeInspection(taskId, staffId, checks, trim(note, 2000));
    }

    public void startCleaning(long taskId, long staffId) throws SQLException {
        dao.startCleaning(taskId, staffId);
    }

    public void completeCleaning(long taskId, long staffId) throws SQLException {
        dao.completeCleaning(taskId, staffId);
    }

    private String trim(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String result = value.trim();
        return result.length() > max ? result.substring(0, max) : result;
    }

    private String normalizeKeyword(String value) {
        if (value == null || value.isBlank()) return null;
        String result = value.trim();
        return result.length() > 50 ? result.substring(0, 50) : result;
    }

    public record TaskPage(List<HousekeepingTask> tasks, int page, int totalPages,
                           int totalItems, String view, String keyword, Integer floor,
                           String taskType, String status, String sort, String direction) { }
}
