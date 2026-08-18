package service;

import dao.HousekeepingDao;
import model.HousekeepingTask;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

public class HousekeepingService {
    public static final int PAGE_SIZE = 1000;
    private static final Set<String> CONDITIONS = Set.of("NORMAL", "DAMAGED", "MISSING");
    private static final Set<String> TASK_TYPES = Set.of("CHECKOUT_INSPECTION", "CLEANING");
    private static final Set<String> TASK_STATUSES = Set.of("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED");
    private static final BigDecimal MAX_DAMAGE_FEE = new BigDecimal("15000000");
    private static final String TASKS_START = "[CLEANING_TASKS]";
    private static final String TASKS_END = "[/CLEANING_TASKS]";
    private static final String NOTE_START = "[INSPECTION_NOTE]";
    private static final Map<String, String> SORTS = Map.of(
            "room", "rm.room_number", "roomType", "rt.name", "floor", "rm.floor_number",
            "taskType", "ht.task_type", "status", "ht.status", "created", "ht.created_at");
    private static final Map<String, String> CLEANING_CHECKLIST;
    static {
        Map<String, String> items = new LinkedHashMap<>();
        items.put("BED", "Dọn giường và thay ga gối");
        items.put("BATHROOM", "Vệ sinh nhà vệ sinh");
        items.put("BEDROOM", "Vệ sinh khu vực phòng ngủ");
        items.put("FLOOR", "Hút bụi và lau sàn");
        items.put("TRASH", "Thu gom và thay túi rác");
        items.put("AMENITIES", "Bổ sung khăn và đồ dùng phòng");
        CLEANING_CHECKLIST = Collections.unmodifiableMap(items);
    }
    private final HousekeepingDao dao;

    public HousekeepingService() { this(new HousekeepingDao()); }
    public HousekeepingService(HousekeepingDao dao) { this.dao = dao; }

    public TaskPage getTaskPage(long viewerId, boolean manager, String view, String keyword, Integer floor,
                                String taskType, String status, String sort,
                                String direction, int requestedPage) throws SQLException {
        String selectedView = "history".equals(view) ? "history" : "mine".equals(view) ? "mine" : "waiting";
        String normalizedKeyword = normalizeKeyword(keyword);
        Integer normalizedFloor = floor != null && floor >= 0 && floor <= 999 ? floor : null;
        String normalizedType = taskType != null && TASK_TYPES.contains(taskType) ? taskType : null;
        String normalizedStatus = status != null && TASK_STATUSES.contains(status) ? status : null;
        String normalizedSort = sort != null && SORTS.containsKey(sort) ? sort : "room";
        String normalizedDirection = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        String sortColumn = "waiting".equals(selectedView) && Set.of("taskType", "status", "created").contains(normalizedSort)
                ? "rm.room_number" : SORTS.get(normalizedSort);
        int totalItems = "history".equals(selectedView)
                ? dao.countHistory(viewerId, manager, normalizedKeyword, normalizedFloor, normalizedType, normalizedStatus)
                : "mine".equals(selectedView)
                    ? dao.countMyTasks(viewerId, normalizedKeyword, normalizedFloor, normalizedType,
                        normalizedStatus != null && !Set.of("COMPLETED","CANCELLED").contains(normalizedStatus) ? normalizedStatus : null)
                    : dao.countAvailableWork(normalizedKeyword, normalizedFloor);
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        int offset = (page - 1) * PAGE_SIZE;
        List<HousekeepingTask> tasks = "history".equals(selectedView)
                ? dao.findHistory(viewerId, manager, normalizedKeyword, normalizedFloor, normalizedType,
                    normalizedStatus, sortColumn, normalizedDirection, offset, PAGE_SIZE)
                : "mine".equals(selectedView)
                ? dao.findMyTasks(viewerId, normalizedKeyword, normalizedFloor, normalizedType,
                    normalizedStatus != null && Set.of("COMPLETED","CANCELLED").contains(normalizedStatus) ? null : normalizedStatus,
                    sortColumn, normalizedDirection, offset, PAGE_SIZE)
                : dao.findAvailableWork(normalizedKeyword, normalizedFloor, waitingSort(normalizedSort),
                    normalizedDirection, offset, PAGE_SIZE);
        return new TaskPage(tasks, page, totalPages, totalItems, selectedView,
                normalizedKeyword, normalizedFloor, normalizedType, normalizedStatus,
                normalizedSort, normalizedDirection.toLowerCase());
    }

    public Optional<HousekeepingTask> getTaskDetail(long taskId, long viewerId, boolean manager) throws SQLException {
        if (taskId <= 0) return Optional.empty();
        return dao.findById(taskId, viewerId, manager);
    }

    public List<HousekeepingTask.EquipmentCheck> getInspectionResults(long taskId) throws SQLException {
        return dao.findInspectionResults(taskId);
    }

    public List<HousekeepingTask.EquipmentCheck> getEquipment(long roomId, Long bookingRoomId) throws SQLException {
        return dao.findEquipment(roomId, bookingRoomId);
    }

    public long claimInspection(long bookingRoomId, long staffId) throws SQLException {
        if (bookingRoomId <= 0) throw new IllegalArgumentException("Phòng cần kiểm tra không hợp lệ");
        return dao.claimInspection(bookingRoomId, staffId);
    }

    public long claimCleaning(long taskId, long staffId) throws SQLException {
        if (taskId <= 0) throw new IllegalArgumentException("Công việc dọn phòng không hợp lệ");
        return dao.claimCleaning(taskId, staffId);
    }

    public void completeInspection(long taskId, long staffId,
                                   List<HousekeepingTask.EquipmentCheck> checks,
                                   List<String> selectedCleaningItems, String note) throws SQLException {
        if (checks == null) throw new IllegalArgumentException("Thiếu kết quả kiểm tra thiết bị");
        for (HousekeepingTask.EquipmentCheck check : checks) {
            if (check.getConditionStatus() == null || !CONDITIONS.contains(check.getConditionStatus())) {
                throw new IllegalArgumentException("Trạng thái thiết bị không hợp lệ");
            }
            if (check.getQuantity() <= 0) throw new IllegalArgumentException("Số lượng thiết bị không hợp lệ");
            BigDecimal fee = check.getDamageFee();
            if (fee == null || fee.signum() < 0 || fee.compareTo(MAX_DAMAGE_FEE) > 0)
                throw new IllegalArgumentException("Phí bồi thường phải từ 0 đến 15.000.000 VND");
            if ("NORMAL".equals(check.getConditionStatus())) check.setDamageFee(BigDecimal.ZERO);
            check.setNote(trim(check.getNote(), 1000));
        }
        dao.completeInspection(taskId, staffId, checks,
                buildCleaningNote(selectedCleaningItems, trim(note, 2000)));
    }

    public void startCleaning(long taskId, long staffId) throws SQLException {
        dao.startCleaning(taskId, staffId);
    }

    public void completeCleaning(long taskId, long staffId) throws SQLException {
        dao.completeCleaning(taskId, staffId);
    }

    public List<HousekeepingTask.EquipmentCheck> getCleaningEquipment(long taskId) throws SQLException {
        return dao.findCleaningEquipment(taskId);
    }

    public Map<String, String> getCleaningChecklist() { return CLEANING_CHECKLIST; }

    public List<String> getWorkItems(String note) {
        if (note == null) return List.of();
        int start = note.indexOf(TASKS_START);
        int end = note.indexOf(TASKS_END);
        if (start < 0 || end <= start) return List.of();
        String block = note.substring(start + TASKS_START.length(), end).trim();
        if (block.isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        for (String line : block.split("\\R")) {
            String item = line.trim();
            if (item.startsWith("- ")) item = item.substring(2).trim();
            if (!item.isEmpty()) result.add(item);
        }
        return result;
    }

    public String getInspectionMessage(String note) {
        if (note == null) return null;
        int marker = note.indexOf(NOTE_START);
        if (marker < 0) return note.isBlank() ? null : note;
        String message = note.substring(marker + NOTE_START.length()).trim();
        return message.isEmpty() ? null : message;
    }

    private String buildCleaningNote(List<String> selectedItems, String message) {
        List<String> labels = new ArrayList<>();
        if (selectedItems != null) {
            for (String key : selectedItems) {
                String label = CLEANING_CHECKLIST.get(key);
                if (label != null && !labels.contains(label)) labels.add(label);
            }
        }
        if (labels.isEmpty()) labels.add("Dọn vệ sinh tổng quát và kiểm tra lại phòng");
        StringBuilder result = new StringBuilder(TASKS_START).append('\n');
        for (String label : labels) result.append("- ").append(label).append('\n');
        result.append(TASKS_END);
        if (message != null) result.append('\n').append(NOTE_START).append('\n').append(message);
        return result.toString();
    }

    private String waitingSort(String sort) {
        return switch (sort) {
            case "roomType" -> "room_type_name";
            case "floor" -> "floor_number";
            case "taskType" -> "task_type";
            case "status" -> "status";
            case "created" -> "created_at";
            default -> "room_number";
        };
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
