package dao;

import model.HousekeepingTask;
import model.User;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HousekeepingDao {
    private static final String BASE_SELECT = """
            SELECT ht.task_id, ht.room_id,
                   CASE WHEN ht.status = 'Pending' THEN NULL ELSE ht.assigned_to END AS assigned_to,
                   ht.status,
                   ht.created_at, ht.started_at, ht.completed_at,
                   ht.completed_by, ht.completion_note, ht.updated_at,
                   r.room_number, r.floor, r.housekeeping_status,
                   rt.type_name AS room_type_name,
                   CASE WHEN ht.status = 'Pending' THEN NULL ELSE assigned.full_name END AS assigned_staff_name,
                   completed.full_name AS completed_staff_name
            FROM housekeeping_task ht
            JOIN room r ON r.room_id = ht.room_id
            JOIN room_type rt ON rt.room_type_id = r.room_type_id
            LEFT JOIN `user` assigned ON assigned.user_id = ht.assigned_to
            LEFT JOIN `user` completed ON completed.user_id = ht.completed_by
            """;

    public List<HousekeepingTask> findTasks(String keyword, String taskStatus,
                                             String roomStatus, Integer assignedTo,
                                             int viewerId, boolean manager,
                                             String sortColumn, String sortDirection,
                                             int offset, int limit) throws SQLException {
        QueryParts query = filters(keyword, taskStatus, roomStatus, assignedTo, viewerId, manager);
        String sql = BASE_SELECT + query.whereClause()
                + " ORDER BY " + sortColumn + " " + sortDirection + ", ht.task_id DESC LIMIT ? OFFSET ?";
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = bind(statement, query.parameters());
            statement.setInt(index++, limit);
            statement.setInt(index, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<HousekeepingTask> tasks = new ArrayList<>();
                while (resultSet.next()) tasks.add(mapTask(resultSet));
                return tasks;
            }
        }
    }

    public int countTasks(String keyword, String taskStatus, String roomStatus,
                          Integer assignedTo, int viewerId, boolean manager) throws SQLException {
        QueryParts query = filters(keyword, taskStatus, roomStatus, assignedTo, viewerId, manager);
        String sql = "SELECT COUNT(*) FROM housekeeping_task ht "
                + "JOIN room r ON r.room_id = ht.room_id "
                + "JOIN room_type rt ON rt.room_type_id = r.room_type_id "
                + query.whereClause();
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, query.parameters());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    public Optional<HousekeepingTask> findById(int taskId, int viewerId, boolean manager)
            throws SQLException {
        String scope = manager ? "" : " AND (ht.status = 'Pending' OR ht.assigned_to = ?)";
        String sql = BASE_SELECT + " WHERE ht.task_id = ?"
                + " AND ht.status IN ('Pending', 'InProgress', 'Blocked')" + scope;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            if (!manager) statement.setInt(2, viewerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapTask(resultSet)) : Optional.empty();
            }
        }
    }

    public List<User> findHousekeepingStaff() throws SQLException {
        String sql = """
                SELECT u.user_id, u.full_name
                FROM `user` u
                JOIN role r ON r.role_id = u.role_id
                WHERE r.role_name = 'Housekeeping' AND u.status = 'Active'
                ORDER BY u.full_name
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setFullName(resultSet.getString("full_name"));
                users.add(user);
            }
            return users;
        }
    }

    private QueryParts filters(String keyword, String taskStatus, String roomStatus,
                               Integer assignedTo, int viewerId, boolean manager) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        conditions.add("ht.status IN ('Pending', 'InProgress', 'Blocked')");
        if (keyword != null) {
            conditions.add("(LOWER(r.room_number) LIKE ? OR LOWER(rt.type_name) LIKE ?)");
            String pattern = "%" + keyword.toLowerCase() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (taskStatus != null) {
            conditions.add("ht.status = ?");
            parameters.add(taskStatus);
        }
        if (roomStatus != null) {
            conditions.add("r.housekeeping_status = ?");
            parameters.add(roomStatus);
        }
        if (assignedTo != null && manager) {
            conditions.add("ht.assigned_to = ?");
            parameters.add(assignedTo);
        }
        if (!manager) {
            conditions.add("(ht.status = 'Pending' OR ht.assigned_to = ?)");
            parameters.add(viewerId);
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return new QueryParts(where, parameters);
    }

    private int bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        int index = 1;
        for (Object parameter : parameters) {
            if (parameter instanceof Integer value) statement.setInt(index++, value);
            else statement.setString(index++, String.valueOf(parameter));
        }
        return index;
    }

    private HousekeepingTask mapTask(ResultSet resultSet) throws SQLException {
        HousekeepingTask task = new HousekeepingTask();
        task.setTaskId(resultSet.getInt("task_id"));
        task.setRoomId(resultSet.getInt("room_id"));
        task.setAssignedTo(nullableInteger(resultSet, "assigned_to"));
        task.setStatus(resultSet.getString("status"));
        task.setCreatedAt(resultSet.getTimestamp("created_at"));
        task.setStartedAt(resultSet.getTimestamp("started_at"));
        task.setCompletedAt(resultSet.getTimestamp("completed_at"));
        task.setCompletedBy(nullableInteger(resultSet, "completed_by"));
        task.setCompletionNote(resultSet.getString("completion_note"));
        task.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        task.setRoomNumber(resultSet.getString("room_number"));
        task.setFloor(resultSet.getInt("floor"));
        task.setRoomTypeName(resultSet.getString("room_type_name"));
        task.setRoomHousekeepingStatus(resultSet.getString("housekeeping_status"));
        task.setAssignedStaffName(resultSet.getString("assigned_staff_name"));
        task.setCompletedStaffName(resultSet.getString("completed_staff_name"));
        return task;
    }

    private Integer nullableInteger(ResultSet resultSet, String column) throws SQLException {
        Object value = resultSet.getObject(column);
        return value == null ? null : ((Number) value).intValue();
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        return connection;
    }

    private record QueryParts(String whereClause, List<Object> parameters) { }
}
