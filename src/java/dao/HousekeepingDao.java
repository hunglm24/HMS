package dao;

import model.HousekeepingTask;
import util.DBConnectionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HousekeepingDao {
    private static final String TASK_SELECT = """
            SELECT ht.id AS task_id, ht.room_id, ht.booking_room_id,
                   ht.room_equipment_id, ht.assigned_to, ht.task_type,
                   ht.priority, ht.status, ht.note, ht.created_at,
                   ht.started_at, ht.completed_at, rm.room_number,
                   rm.floor_number, rm.status AS room_status,
                   rt.name AS room_type_name, a.full_name AS assigned_staff_name,
                   CASE WHEN ht.task_type <> 'CLEANING' THEN TRUE ELSE EXISTS (
                       SELECT 1 FROM booking_rooms ready_br
                       JOIN bookings ready_b ON ready_b.id = ready_br.booking_id
                       JOIN check_outs ready_co ON ready_co.booking_id = ready_b.id
                       WHERE ready_br.id = ht.booking_room_id
                         AND ready_b.status = 'CHECKED_OUT'
                   ) END AS action_ready
            FROM housekeeping_tasks ht
            JOIN rooms rm ON rm.id = ht.room_id
            JOIN room_types rt ON rt.id = rm.room_type_id
            LEFT JOIN accounts a ON a.id = ht.assigned_to
            """;

    public List<HousekeepingTask> findPendingInspectionRooms(String keyword, Integer floor,
                                                              String sortColumn, String direction,
                                                              int offset, int limit)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT 0 AS task_id, rm.id AS room_id, br.id AS booking_room_id,
                       NULL AS room_equipment_id, NULL AS assigned_to,
                       'CHECKOUT_INSPECTION' AS task_type, 'NORMAL' AS priority,
                       'WAITING' AS status, NULL AS note, b.updated_at AS created_at,
                       NULL AS started_at, NULL AS completed_at, rm.room_number,
                       rm.floor_number, rm.status AS room_status,
                       rt.name AS room_type_name, NULL AS assigned_staff_name,
                       TRUE AS action_ready
                FROM bookings b
                JOIN booking_rooms br ON br.booking_id = b.id
                JOIN rooms rm ON rm.id = br.room_id
                JOIN room_types rt ON rt.id = rm.room_type_id
                WHERE b.status = 'CHECKOUT_PENDING'
                  AND NOT EXISTS (SELECT 1 FROM room_inspections ri WHERE ri.booking_room_id = br.id)
                  AND NOT EXISTS (
                      SELECT 1 FROM housekeeping_tasks ht
                      WHERE ht.booking_room_id = br.id
                        AND ht.task_type = 'CHECKOUT_INSPECTION'
                        AND ht.status <> 'CANCELLED'
                  )
                """);
        List<Object> params = new ArrayList<>();
        appendRoomFilters(sql, params, keyword, floor);
        sql.append(" ORDER BY ").append(sortColumn).append(' ').append(direction)
                .append(", rm.room_number ASC LIMIT ? OFFSET ?");
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = bind(statement, params);
            statement.setInt(index++, limit);
            statement.setInt(index, offset);
            try (ResultSet rs = statement.executeQuery()) {
                List<HousekeepingTask> result = new ArrayList<>();
                while (rs.next()) result.add(mapTask(rs));
                return result;
            }
        }
    }

    public int countPendingInspectionRooms(String keyword, Integer floor) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM bookings b
                JOIN booking_rooms br ON br.booking_id = b.id
                JOIN rooms rm ON rm.id = br.room_id
                JOIN room_types rt ON rt.id = rm.room_type_id
                WHERE b.status = 'CHECKOUT_PENDING'
                  AND NOT EXISTS (SELECT 1 FROM room_inspections ri WHERE ri.booking_room_id = br.id)
                  AND NOT EXISTS (
                      SELECT 1 FROM housekeeping_tasks ht
                      WHERE ht.booking_room_id = br.id
                        AND ht.task_type = 'CHECKOUT_INSPECTION'
                        AND ht.status <> 'CANCELLED'
                  )
                """);
        List<Object> params = new ArrayList<>();
        appendRoomFilters(sql, params, keyword, floor);
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    public List<HousekeepingTask> findMyTasks(long viewerId, String keyword, Integer floor,
                                               String taskType, String status,
                                               String sortColumn, String direction,
                                               int offset, int limit)
            throws SQLException {
        StringBuilder sql = new StringBuilder(TASK_SELECT)
                .append(" WHERE ht.assigned_to = ? AND ht.status IN ('PENDING', 'IN_PROGRESS')");
        List<Object> params = new ArrayList<>(); params.add(viewerId);
        appendRoomFilters(sql, params, keyword, floor);
        if (taskType != null) { sql.append(" AND ht.task_type = ?"); params.add(taskType); }
        if (status != null) { sql.append(" AND ht.status = ?"); params.add(status); }
        sql.append(" ORDER BY ").append(sortColumn).append(' ').append(direction)
                .append(", ht.id DESC LIMIT ? OFFSET ?");
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = bind(statement, params);
            statement.setInt(index++, limit);
            statement.setInt(index, offset);
            try (ResultSet rs = statement.executeQuery()) {
                List<HousekeepingTask> result = new ArrayList<>();
                while (rs.next()) result.add(mapTask(rs));
                return result;
            }
        }
    }

    public int countMyTasks(long viewerId, String keyword, Integer floor,
                            String taskType, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM housekeeping_tasks ht
                JOIN rooms rm ON rm.id = ht.room_id
                JOIN room_types rt ON rt.id = rm.room_type_id
                WHERE ht.assigned_to = ? AND ht.status IN ('PENDING', 'IN_PROGRESS')
                """);
        List<Object> params = new ArrayList<>(); params.add(viewerId);
        appendRoomFilters(sql, params, keyword, floor);
        if (taskType != null) { sql.append(" AND ht.task_type = ?"); params.add(taskType); }
        if (status != null) { sql.append(" AND ht.status = ?"); params.add(status); }
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void appendRoomFilters(StringBuilder sql, List<Object> params,
                                   String keyword, Integer floor) {
        if (keyword != null) {
            sql.append(" AND (LOWER(rm.room_number) LIKE ? OR LOWER(rt.name) LIKE ?)");
            String pattern = "%" + keyword.toLowerCase() + "%";
            params.add(pattern); params.add(pattern);
        }
        if (floor != null) { sql.append(" AND rm.floor_number = ?"); params.add(floor); }
    }

    private int bind(PreparedStatement statement, List<Object> params) throws SQLException {
        int index = 1;
        for (Object value : params) {
            if (value instanceof Number number) statement.setLong(index++, number.longValue());
            else statement.setString(index++, String.valueOf(value));
        }
        return index;
    }

    public Optional<HousekeepingTask> findById(long taskId, long viewerId) throws SQLException {
        String sql = TASK_SELECT + " WHERE ht.id = ? AND ht.assigned_to = ?";
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            statement.setLong(2, viewerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapTask(rs)) : Optional.empty();
            }
        }
    }

    public List<HousekeepingTask.EquipmentCheck> findEquipment(long roomId, Long bookingRoomId) throws SQLException {
        String sql = """
                SELECT re.id, e.name, re.quantity, re.status,
                       cis.initial_status, cis.initial_quantity
                FROM room_equipment re
                JOIN equipment e ON e.id = re.equipment_id
                LEFT JOIN checkin_equipment_snapshots cis
                  ON cis.room_equipment_id = re.id AND cis.booking_room_id = ?
                WHERE re.room_id = ?
                ORDER BY e.name
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (bookingRoomId == null) statement.setNull(1, java.sql.Types.BIGINT);
            else statement.setLong(1, bookingRoomId);
            statement.setLong(2, roomId);
            try (ResultSet rs = statement.executeQuery()) {
                List<HousekeepingTask.EquipmentCheck> result = new ArrayList<>();
                while (rs.next()) {
                    HousekeepingTask.EquipmentCheck item = new HousekeepingTask.EquipmentCheck();
                    item.setRoomEquipmentId(rs.getLong("id"));
                    item.setEquipmentName(rs.getString("name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCurrentStatus(rs.getString("status"));
                    item.setInitialStatus(rs.getString("initial_status"));
                    Object initialQuantity = rs.getObject("initial_quantity");
                    item.setInitialQuantity(initialQuantity == null ? null : ((Number) initialQuantity).intValue());
                    result.add(item);
                }
                return result;
            }
        }
    }

    public long claimInspection(long bookingRoomId, long staffId) throws SQLException {
        String lockSql = """
                SELECT br.room_id
                FROM booking_rooms br
                JOIN bookings b ON b.id = br.booking_id
                WHERE br.id = ? AND b.status = 'CHECKOUT_PENDING'
                  AND NOT EXISTS (SELECT 1 FROM room_inspections ri WHERE ri.booking_room_id = br.id)
                  AND NOT EXISTS (
                      SELECT 1 FROM housekeeping_tasks ht
                      WHERE ht.booking_room_id = br.id
                        AND ht.task_type = 'CHECKOUT_INSPECTION'
                        AND ht.status <> 'CANCELLED'
                  )
                FOR UPDATE
                """;
        String taskSql = """
                INSERT INTO housekeeping_tasks
                    (room_id, booking_room_id, assigned_to, task_type, priority,
                     status, note, started_at)
                VALUES (?, ?, ?, 'CHECKOUT_INSPECTION', 'NORMAL', 'IN_PROGRESS',
                        'Kiểm tra phòng sau checkout', CURRENT_TIMESTAMP)
                """;
        String inspectionSql = """
                INSERT INTO room_inspections
                    (housekeeping_task_id, booking_room_id, inspected_by, status)
                VALUES (?, ?, ?, 'PENDING')
                """;
        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long roomId;
                try (PreparedStatement lock = connection.prepareStatement(lockSql)) {
                    lock.setLong(1, bookingRoomId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Phòng không còn trong hàng đợi kiểm tra");
                        roomId = rs.getLong(1);
                    }
                }
                long taskId;
                try (PreparedStatement insert = connection.prepareStatement(taskSql, Statement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, roomId);
                    insert.setLong(2, bookingRoomId);
                    insert.setLong(3, staffId);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được ID công việc kiểm tra");
                        taskId = keys.getLong(1);
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(inspectionSql)) {
                    insert.setLong(1, taskId);
                    insert.setLong(2, bookingRoomId);
                    insert.setLong(3, staffId);
                    insert.executeUpdate();
                }
                connection.commit();
                return taskId;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public void completeInspection(long taskId, long staffId,
                                   List<HousekeepingTask.EquipmentCheck> checks,
                                   String inspectionNote) throws SQLException {
        String lockSql = """
                SELECT ht.room_id, ht.booking_room_id, br.booking_id, ri.id
                FROM housekeeping_tasks ht
                JOIN booking_rooms br ON br.id = ht.booking_room_id
                JOIN room_inspections ri ON ri.housekeeping_task_id = ht.id
                WHERE ht.id = ? AND ht.assigned_to = ?
                  AND ht.task_type = 'CHECKOUT_INSPECTION'
                  AND ht.status = 'IN_PROGRESS' AND ri.status = 'PENDING'
                FOR UPDATE
                """;
        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long roomId;
                long bookingRoomId;
                long bookingId;
                long inspectionId;
                try (PreparedStatement lock = connection.prepareStatement(lockSql)) {
                    lock.setLong(1, taskId);
                    lock.setLong(2, staffId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Công việc kiểm tra không hợp lệ hoặc đã hoàn thành");
                        roomId = rs.getLong("room_id");
                        bookingRoomId = rs.getLong("booking_room_id");
                        bookingId = rs.getLong("booking_id");
                        inspectionId = rs.getLong("id");
                    }
                }
                boolean damaged = false;
                for (HousekeepingTask.EquipmentCheck check : checks) {
                    validateEquipment(connection, roomId, check.getRoomEquipmentId());
                    long itemId = insertInspectionItem(connection, inspectionId, check);
                    if (!"NORMAL".equals(check.getConditionStatus())) {
                        damaged = true;
                        long damageReportId = insertDamageReport(connection, itemId, bookingId, check);
                        markEquipmentAndCreateTask(connection, roomId, damageReportId, check);
                    }
                }
                updateInspection(connection, inspectionId, damaged, inspectionNote);
                completeTask(connection, taskId);
                createCleaningTask(connection, roomId, bookingRoomId, staffId);
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public void startCleaning(long taskId, long staffId) throws SQLException {
        String sql = """
                UPDATE housekeeping_tasks ht
                JOIN booking_rooms br ON br.id = ht.booking_room_id
                JOIN bookings b ON b.id = br.booking_id
                JOIN check_outs co ON co.booking_id = b.id
                SET ht.status = 'IN_PROGRESS', ht.started_at = CURRENT_TIMESTAMP
                WHERE ht.id = ? AND ht.assigned_to = ?
                  AND ht.task_type = 'CLEANING' AND ht.status = 'PENDING'
                  AND b.status = 'CHECKED_OUT'
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            statement.setLong(2, staffId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Chỉ có thể bắt đầu dọn phòng sau khi checkout hoàn tất");
            }
        }
    }

    public void completeCleaning(long taskId, long staffId) throws SQLException {
        String lockSql = """
                SELECT room_id FROM housekeeping_tasks
                WHERE id = ? AND assigned_to = ? AND task_type = 'CLEANING'
                  AND status = 'IN_PROGRESS' FOR UPDATE
                """;
        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long roomId;
                try (PreparedStatement lock = connection.prepareStatement(lockSql)) {
                    lock.setLong(1, taskId); lock.setLong(2, staffId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không thể hoàn tất công việc dọn phòng");
                        roomId = rs.getLong(1);
                    }
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE housekeeping_tasks SET status='COMPLETED', completed_at=CURRENT_TIMESTAMP WHERE id=?")) {
                    update.setLong(1, taskId); update.executeUpdate();
                }
                try (PreparedStatement updateRoom = connection.prepareStatement("""
                        UPDATE rooms rm SET rm.status = CASE WHEN EXISTS (
                            SELECT 1 FROM room_equipment re
                            WHERE re.room_id = rm.id AND re.status <> 'NORMAL'
                        ) THEN 'NOT_READY' ELSE 'AVAILABLE' END
                        WHERE rm.id = ?
                        """)) {
                    updateRoom.setLong(1, roomId); updateRoom.executeUpdate();
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback(); throw ex;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    private void validateEquipment(Connection connection, long roomId, long equipmentId)
            throws SQLException {
        String sql = "SELECT 1 FROM room_equipment WHERE id = ? AND room_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, equipmentId);
            statement.setLong(2, roomId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new SQLException("Thiết bị không thuộc phòng đang kiểm tra");
            }
        }
    }

    private long insertInspectionItem(Connection connection, long inspectionId,
                                      HousekeepingTask.EquipmentCheck check) throws SQLException {
        String sql = """
                INSERT INTO inspection_items
                    (inspection_id, room_equipment_id, condition_status, quantity,
                     damage_fee, note)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, inspectionId);
            statement.setLong(2, check.getRoomEquipmentId());
            statement.setString(3, check.getConditionStatus());
            statement.setInt(4, check.getQuantity());
            statement.setBigDecimal(5, check.getDamageFee());
            statement.setString(6, check.getNote());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không lấy được ID kết quả kiểm tra");
                return keys.getLong(1);
            }
        }
    }

    private long insertDamageReport(Connection connection, long itemId, long bookingId,
                                    HousekeepingTask.EquipmentCheck check) throws SQLException {
        String sql = """
                INSERT INTO damage_reports
                    (inspection_item_id, booking_id, room_equipment_id, damage_type,
                     compensation_amount, charge_status, note)
                VALUES (?, ?, ?, ?, ?, 'PENDING', ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, itemId);
            statement.setLong(2, bookingId);
            statement.setLong(3, check.getRoomEquipmentId());
            statement.setString(4, check.getConditionStatus());
            statement.setBigDecimal(5, check.getDamageFee());
            statement.setString(6, check.getNote());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không lấy được ID báo cáo sự cố");
                return keys.getLong(1);
            }
        }
    }

    private void markEquipmentAndCreateTask(Connection connection, long roomId, long damageReportId,
                                            HousekeepingTask.EquipmentCheck check) throws SQLException {
        boolean missing = "MISSING".equals(check.getConditionStatus());
        String equipmentStatus = missing ? "WAITING_REPLACEMENT" : "WAITING_REPAIR";
        String taskType = missing ? "EQUIPMENT_REPLACEMENT" : "EQUIPMENT_REPAIR";
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE room_equipment SET status=?, note=? WHERE id=? AND room_id=?")) {
            String equipmentNote = check.getNote();
            if (equipmentNote != null && equipmentNote.length() > 500) {
                equipmentNote = equipmentNote.substring(0, 500);
            }
            update.setString(1, equipmentStatus); update.setString(2, equipmentNote);
            update.setLong(3, check.getRoomEquipmentId()); update.setLong(4, roomId);
            if (update.executeUpdate() != 1) throw new SQLException("Không thể cập nhật trạng thái thiết bị");
        }
        String sql = """
                INSERT INTO housekeeping_tasks
                    (room_id, room_equipment_id, assigned_to, task_type, priority, status, note)
                SELECT ?, ?, NULL, ?, 'HIGH', 'PENDING', ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM housekeeping_tasks
                    WHERE room_equipment_id = ? AND task_type = ?
                      AND status IN ('PENDING','IN_PROGRESS')
                )
                """;
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            insert.setLong(1, roomId); insert.setLong(2, check.getRoomEquipmentId());
            insert.setString(3, taskType);
            insert.setString(4, "Xử lý sự cố #" + damageReportId + ": "
                    + (check.getNote() == null ? check.getConditionStatus() : check.getNote()));
            insert.setLong(5, check.getRoomEquipmentId()); insert.setString(6, taskType);
            insert.executeUpdate();
        }
    }

    private void updateInspection(Connection connection, long inspectionId, boolean damaged,
                                  String note) throws SQLException {
        String sql = "UPDATE room_inspections SET status = ?, note = ?, inspected_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, damaged ? "DAMAGE_FOUND" : "PASSED");
            statement.setString(2, note);
            statement.setLong(3, inspectionId);
            statement.executeUpdate();
        }
    }

    private void completeTask(Connection connection, long taskId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE housekeeping_tasks SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            statement.setLong(1, taskId);
            statement.executeUpdate();
        }
    }

    private void createCleaningTask(Connection connection, long roomId, long bookingRoomId,
                                    long staffId) throws SQLException {
        String sql = """
                INSERT INTO housekeeping_tasks
                    (room_id, booking_room_id, assigned_to, task_type, priority, status, note)
                SELECT ?, ?, ?, 'CLEANING', 'NORMAL', 'PENDING', 'Dọn phòng sau kiểm tra checkout'
                WHERE NOT EXISTS (
                    SELECT 1 FROM housekeeping_tasks
                    WHERE booking_room_id = ? AND task_type = 'CLEANING'
                      AND status <> 'CANCELLED'
                )
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            statement.setLong(2, bookingRoomId);
            statement.setLong(3, staffId);
            statement.setLong(4, bookingRoomId);
            if (statement.executeUpdate() != 1) throw new SQLException("Không thể tạo công việc dọn phòng");
        }
    }

    private HousekeepingTask mapTask(ResultSet rs) throws SQLException {
        HousekeepingTask task = new HousekeepingTask();
        task.setTaskId(rs.getLong("task_id"));
        task.setRoomId(rs.getLong("room_id"));
        task.setBookingRoomId(nullableLong(rs, "booking_room_id"));
        task.setRoomEquipmentId(nullableLong(rs, "room_equipment_id"));
        task.setAssignedTo(nullableLong(rs, "assigned_to"));
        task.setTaskType(rs.getString("task_type"));
        task.setPriority(rs.getString("priority"));
        task.setStatus(rs.getString("status"));
        task.setNote(rs.getString("note"));
        task.setCreatedAt(rs.getTimestamp("created_at"));
        task.setStartedAt(rs.getTimestamp("started_at"));
        task.setCompletedAt(rs.getTimestamp("completed_at"));
        task.setRoomNumber(rs.getString("room_number"));
        task.setFloorNumber((Integer) rs.getObject("floor_number"));
        task.setRoomStatus(rs.getString("room_status"));
        task.setRoomTypeName(rs.getString("room_type_name"));
        task.setAssignedStaffName(rs.getString("assigned_staff_name"));
        task.setActionReady(rs.getBoolean("action_ready"));
        return task;
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).longValue();
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        return connection;
    }
}
