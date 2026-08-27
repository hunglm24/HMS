package service;

import dao.HousekeepingDao;
import dto.HousekeeperWorkloadDTO;
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
            "taskType", "ht.task_type", "status", "ht.status", "created", "ht.created_at", "time", "ht.created_at", "assigned_to", "COALESCE(a.full_name, '')");
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
        String selectedView = "history".equals(view) ? "history" : "mine";
        String normalizedKeyword = normalizeKeyword(keyword);
        Integer normalizedFloor = floor != null && floor >= 0 && floor <= 999 ? floor : null;
        String normalizedType = taskType != null && TASK_TYPES.contains(taskType) ? taskType : null;
        String normalizedStatus = status != null && TASK_STATUSES.contains(status) ? status : null;
        String normalizedSort = sort != null && SORTS.containsKey(sort) ? sort : "room";
        String normalizedDirection = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        String sortColumn = SORTS.get(normalizedSort);
        int totalItems = "history".equals(selectedView)
                ? dao.countHistory(viewerId, manager, normalizedKeyword, normalizedFloor, normalizedType, normalizedStatus)
                : dao.countMyTasks(viewerId, normalizedKeyword, normalizedFloor, normalizedType,
                        normalizedStatus != null && !Set.of("COMPLETED","CANCELLED").contains(normalizedStatus) ? normalizedStatus : null);
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        int offset = (page - 1) * PAGE_SIZE;
        List<HousekeepingTask> tasks = "history".equals(selectedView)
                ? dao.findHistory(viewerId, manager, normalizedKeyword, normalizedFloor, normalizedType,
                    normalizedStatus, sortColumn, normalizedDirection, offset, PAGE_SIZE)
                : dao.findMyTasks(viewerId, normalizedKeyword, normalizedFloor, normalizedType,
                    normalizedStatus != null && Set.of("COMPLETED","CANCELLED").contains(normalizedStatus) ? null : normalizedStatus,
                    sortColumn, normalizedDirection, offset, PAGE_SIZE);
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
                                   List<String> selectedCleaningItems,
                                   String customCleaningTasks,
                                   String note) throws SQLException {
        if (taskId <= 0 || staffId <= 0) throw new IllegalArgumentException("Thông tin kiểm tra không hợp lệ");
        if (checks == null) throw new IllegalArgumentException("Thiếu kết quả kiểm tra thiết bị");
        for (HousekeepingTask.EquipmentCheck check : checks) {
            if (check.getConditionStatus() == null || !CONDITIONS.contains(check.getConditionStatus())) {
                throw new IllegalArgumentException("Trạng thái thiết bị không hợp lệ");
            }
            if (check.getQuantity() <= 0) { check.setQuantity(1); }
            BigDecimal fee = check.getDamageFee();
            if (fee == null || fee.signum() < 0 || fee.compareTo(MAX_DAMAGE_FEE) > 0)
                throw new IllegalArgumentException("Phí bồi thường phải từ 0 đến 15.000.000 VND");
            if ("NORMAL".equals(check.getConditionStatus())) check.setDamageFee(BigDecimal.ZERO);
            check.setNote(trim(check.getNote(), 1000));
        }
        dao.completeInspection(taskId, staffId, checks,
                buildCleaningNote(selectedCleaningItems, customCleaningTasks, trim(note, 2000)));
    }

    public void startCleaning(long taskId, long staffId) throws SQLException {
        dao.startCleaning(taskId, staffId);
    }

    public List<HousekeepingTask.EquipmentCheck> getCleaningEquipment(long taskId) throws SQLException {
        return dao.findCleaningEquipment(taskId);
    }

    public Map<String, String> getCleaningChecklist() { return CLEANING_CHECKLIST; }

    public List<HousekeepingTask.WorkItem> getWorkItems(String note) {
        if (note == null || note.isBlank()) {
            return List.of(new HousekeepingTask.WorkItem("Dọn vệ sinh tổng quát và kiểm tra lại phòng", false));
        }

        String block = note;
        int start = note.indexOf(TASKS_START);
        int end = note.indexOf(TASKS_END);
        if (start < 0) {
            start = note.indexOf("[===TASKS===]");
            if (start >= 0) start += "[===TASKS===]".length();
            end = note.indexOf("[===END_TASKS===]");
        } else {
            start += TASKS_START.length();
        }

        if (start >= 0 && end > start) {
            block = note.substring(start, end).trim();
        } else {
            int noteMarker = note.indexOf(NOTE_START);
            if (noteMarker < 0) noteMarker = note.indexOf("[===NOTE===]");
            if (noteMarker >= 0) {
                block = note.substring(0, noteMarker).trim();
            }
        }

        List<HousekeepingTask.WorkItem> result = new ArrayList<>();
        for (String rawLine : block.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || (line.startsWith("[") && line.endsWith("]") && !line.startsWith("[x]") && !line.startsWith("[X]") && !line.startsWith("[ ]"))) {
                continue;
            }

            boolean completed = false;
            String itemName = line;

            if (itemName.startsWith("- [x]") || itemName.startsWith("- [X]")) {
                completed = true;
                itemName = itemName.substring(5).trim();
            } else if (itemName.startsWith("- [ ]")) {
                completed = false;
                itemName = itemName.substring(5).trim();
            } else if (itemName.startsWith("[x]") || itemName.startsWith("[X]")) {
                completed = true;
                itemName = itemName.substring(3).trim();
            } else if (itemName.startsWith("[ ]")) {
                completed = false;
                itemName = itemName.substring(3).trim();
            } else if (itemName.startsWith("- ") || itemName.startsWith("* ") || itemName.startsWith("+ ") || itemName.startsWith("• ")) {
                itemName = itemName.substring(2).trim();
            } else if (itemName.startsWith("-") || itemName.startsWith("*") || itemName.startsWith("+") || itemName.startsWith("•")) {
                itemName = itemName.substring(1).trim();
            }

            // Exclude fallback labels or dummy headers from becoming work items
            if ("Dọn phòng sau checkout".equalsIgnoreCase(itemName)) {
                continue;
            }

            if (!itemName.isEmpty()) {
                result.add(new HousekeepingTask.WorkItem(itemName, completed));
            }
        }

        if (result.isEmpty()) {
            result.add(new HousekeepingTask.WorkItem("Dọn vệ sinh tổng quát và kiểm tra lại phòng", false));
        }
        return result;
    }

    public void saveCleaningProgress(long taskId, long staffId, List<String> completedItemNames) throws SQLException {
        Optional<HousekeepingTask> taskOpt = dao.findById(taskId, staffId, true);
        if (taskOpt.isEmpty()) throw new IllegalArgumentException("Không tìm thấy công việc");
        HousekeepingTask task = taskOpt.get();

        List<HousekeepingTask.WorkItem> currentItems = getWorkItems(task.getNote());
        java.util.Set<String> completedSet = completedItemNames != null ? new java.util.HashSet<>(completedItemNames) : java.util.Set.of();

        StringBuilder sb = new StringBuilder(TASKS_START).append('\n');
        for (HousekeepingTask.WorkItem item : currentItems) {
            boolean done = completedSet.contains(item.getName());
            sb.append(done ? "[x] " : "[ ] ").append(item.getName()).append('\n');
        }
        sb.append(TASKS_END);

        String message = getInspectionMessage(task.getNote());
        if (message != null && !message.isBlank()) {
            sb.append('\n').append(NOTE_START).append('\n').append(message);
        }

        dao.updateTaskNote(taskId, staffId, sb.toString());
    }

    public String getInspectionMessage(String note) {
        if (note == null || note.isBlank()) return null;
        int marker = note.indexOf(NOTE_START);
        if (marker >= 0) {
            String message = note.substring(marker + NOTE_START.length()).trim();
            return isMeaningfulNote(message) ? message : null;
        }
        marker = note.indexOf("[===NOTE===]");
        if (marker >= 0) {
            String message = note.substring(marker + "[===NOTE===]".length()).trim();
            return isMeaningfulNote(message) ? message : null;
        }
        int start = note.indexOf(TASKS_START);
        int end = note.indexOf(TASKS_END);
        if (start < 0) {
            start = note.indexOf("[===TASKS===]");
            end = note.indexOf("[===END_TASKS===]");
            if (start >= 0 && end > start) {
                String outside = note.substring(end + "[===END_TASKS===]".length()).trim();
                return isMeaningfulNote(outside) ? outside : null;
            }
        } else if (end > start) {
            String outside = note.substring(end + TASKS_END.length()).trim();
            return isMeaningfulNote(outside) ? outside : null;
        }
        boolean isChecklist = false;
        for (String line : note.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("+") || trimmed.startsWith("•") || trimmed.startsWith("[x]") || trimmed.startsWith("[ ]")) {
                isChecklist = true;
                break;
            }
        }
        if (isChecklist) {
            return null;
        }
        return isMeaningfulNote(note.trim()) ? note.trim() : null;
    }

    private boolean isMeaningfulNote(String message) {
        if (message == null || message.isBlank()) return false;
        String clean = message.trim();
        return !"Dọn phòng sau checkout".equalsIgnoreCase(clean);
    }

        private String buildCleaningNote(List<String> selectedItems, String customTasks, String message) {
        List<String> labels = new ArrayList<>();
        if (selectedItems != null) {
            for (String key : selectedItems) {
                String label = CLEANING_CHECKLIST.get(key);
                if (label != null && !labels.contains(label)) labels.add(label);
            }
        }
        if (customTasks != null && !customTasks.isBlank()) {
            for (String rawLine : customTasks.split("\\R")) {
                String line = rawLine.trim();
                if (line.startsWith("-") || line.startsWith("*") || line.startsWith("+") || line.startsWith("•")) {
                    line = line.replaceFirst("^[-*+•]\\s*", "");
                }
                if (!line.isBlank() && !labels.contains(line)) {
                    labels.add(line);
                }
            }
        }
        if (labels.isEmpty() && (message == null || message.isBlank())) {
            return null; // Không có yêu cầu dọn phòng nào được chọn
        }
        if (labels.isEmpty()) {
            return message;
        }
        StringBuilder result = new StringBuilder(TASKS_START).append('\n');
        for (String label : labels) result.append("[ ] ").append(label).append('\n');
        result.append(TASKS_END);
        if (message != null && !message.isBlank()) {
            result.append('\n').append(NOTE_START).append('\n').append(message);
        }
        return result.toString();
    }
public void completeCleaning(long taskId, long staffId) throws SQLException {
        Optional<HousekeepingTask> taskOpt = dao.findById(taskId, staffId, true);
        if (taskOpt.isPresent()) {
            HousekeepingTask task = taskOpt.get();
            List<HousekeepingTask.WorkItem> currentItems = getWorkItems(task.getNote());
            StringBuilder sb = new StringBuilder(TASKS_START).append('\n');
            for (HousekeepingTask.WorkItem item : currentItems) {
                sb.append("[x] ").append(item.getName()).append('\n');
            }
            sb.append(TASKS_END);
            String message = getInspectionMessage(task.getNote());
            if (message != null && !message.isBlank()) {
                sb.append('\n').append(NOTE_START).append('\n').append(message);
            }
            dao.updateTaskNote(taskId, staffId, sb.toString());
        }
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

    public void createManualTask(long roomId, String taskType, Long assignedTo, String priority, String cleaningTasks, String note) throws SQLException {
        if (roomId <= 0) throw new IllegalArgumentException("Phòng không hợp lệ");
        if (taskType == null || !TASK_TYPES.contains(taskType)) throw new IllegalArgumentException("Loại công việc không hợp lệ");
        if (priority == null || (!priority.equals("NORMAL") && !priority.equals("HIGH"))) throw new IllegalArgumentException("Độ ưu tiên không hợp lệ");
        
        String combinedNote;
        if ("CLEANING".equals(taskType)) {
            List<String> tasks = new ArrayList<>();
            if (cleaningTasks != null && !cleaningTasks.isBlank()) {
                for (String rawLine : cleaningTasks.split("\\R")) {
                    String line = rawLine.trim();
                    if (line.startsWith("-") || line.startsWith("*") || line.startsWith("+") || line.startsWith("•")) {
                        line = line.replaceFirst("^[-*+•]\\s*", "");
                    }
                    if (!line.isBlank() && !tasks.contains(line)) {
                        tasks.add(line);
                    }
                }
            }
            if (tasks.isEmpty()) tasks.add("Dọn vệ sinh tổng quát và kiểm tra lại phòng");
            StringBuilder sb = new StringBuilder(TASKS_START).append('\n');
            for (String t : tasks) sb.append("[ ] ").append(t).append('\n');
            sb.append(TASKS_END);
            if (note != null && !note.isBlank()) {
                sb.append('\n').append(NOTE_START).append('\n').append(note.trim());
            }
            combinedNote = sb.toString();
        } else {
            combinedNote = (note != null && !note.isBlank()) ? note.trim() : null;
        }
        
        dao.createManualTask(roomId, taskType, assignedTo, priority, combinedNote != null ? trim(combinedNote, 2000) : null);
    }

    public void createManualTask(long roomId, String taskType, Long assignedTo, String priority, String note) throws SQLException {
        createManualTask(roomId, taskType, assignedTo, priority, null, note);
    }

    public List<model.User> getHousekeepers() throws SQLException {
        return new dao.UserDao().findByRoleName("HOUSEKEEPING");
    }

    public List<HousekeeperWorkloadDTO> getHousekeeperWorkloads() throws SQLException {
        return dao.getHousekeeperWorkloads();
    }

    public void syncDatabaseState() throws SQLException {
        dao.syncDatabaseState();
    }

    public record TaskPage(List<HousekeepingTask> tasks, int page, int totalPages,
                           int totalItems, String view, String keyword, Integer floor,
                           String taskType, String status, String sort, String direction) { }
}
