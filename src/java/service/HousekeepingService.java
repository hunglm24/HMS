package service;

import dao.HousekeepingDao;
import model.HousekeepingTask;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HousekeepingService {
    public static final int PAGE_SIZE = 10;
    private static final Set<String> TASK_STATUSES = Set.of(
            "Pending", "InProgress", "Blocked");
    private static final Set<String> ROOM_STATUSES = Set.of(
            "Clean", "Dirty", "Cleaning", "Maintenance");
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "room", "r.room_number",
            "floor", "r.floor",
            "roomType", "rt.type_name",
            "staff", "assigned.full_name",
            "taskStatus", "ht.status",
            "roomStatus", "r.housekeeping_status",
            "created", "ht.created_at",
            "updated", "ht.updated_at"
    );

    private final HousekeepingDao housekeepingDao;

    public HousekeepingService() {
        this(new HousekeepingDao());
    }

    public HousekeepingService(HousekeepingDao housekeepingDao) {
        this.housekeepingDao = housekeepingDao;
    }

    public TaskPage getTaskPage(String keyword, String taskStatus, String roomStatus,
                                Integer assignedTo, int viewerId, boolean manager,
                                String sort, String direction, int requestedPage) throws SQLException {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedTaskStatus = taskStatus != null && TASK_STATUSES.contains(taskStatus)
                ? taskStatus : null;
        String normalizedRoomStatus = roomStatus != null && ROOM_STATUSES.contains(roomStatus)
                ? roomStatus : null;
        Integer normalizedAssignee = assignedTo != null && assignedTo > 0 ? assignedTo : null;
        String normalizedSort = sort != null && SORT_COLUMNS.containsKey(sort) ? sort : "created";
        String normalizedDirection = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";

        int totalItems = housekeepingDao.countTasks(normalizedKeyword, normalizedTaskStatus,
                normalizedRoomStatus, normalizedAssignee, viewerId, manager);
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        int offset = (page - 1) * PAGE_SIZE;

        List<HousekeepingTask> tasks = housekeepingDao.findTasks(normalizedKeyword,
                normalizedTaskStatus, normalizedRoomStatus, normalizedAssignee,
                viewerId, manager, SORT_COLUMNS.get(normalizedSort), normalizedDirection,
                offset, PAGE_SIZE);
        return new TaskPage(tasks, page, totalPages, totalItems, normalizedKeyword,
                normalizedTaskStatus, normalizedRoomStatus, normalizedAssignee,
                normalizedSort, normalizedDirection.toLowerCase());
    }

    public Optional<HousekeepingTask> getTaskDetail(int taskId, int viewerId, boolean manager)
            throws SQLException {
        if (taskId <= 0) return Optional.empty();
        return housekeepingDao.findById(taskId, viewerId, manager);
    }

    public List<User> getHousekeepingStaff() throws SQLException {
        return housekeepingDao.findHousekeepingStaff();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String value = keyword.trim();
        return value.length() > 50 ? value.substring(0, 50) : value;
    }

    public record TaskPage(List<HousekeepingTask> tasks, int page, int totalPages,
                           int totalItems, String keyword, String taskStatus,
                           String roomStatus, Integer assignedTo, String sort,
                           String direction) { }
}
