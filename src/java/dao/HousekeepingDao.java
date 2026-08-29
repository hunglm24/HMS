package dao;

import dto.HousekeeperWorkloadDTO;
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
                   rt.name AS room_type_name,
                   COALESCE(
                       a.full_name,
                       (SELECT acc.full_name FROM room_inspections ri JOIN accounts acc ON acc.id = ri.inspected_by WHERE ri.housekeeping_task_id = ht.id OR ri.booking_room_id = ht.booking_room_id ORDER BY ri.id DESC LIMIT 1),
                       (SELECT acc.full_name FROM equipment_maintenance_logs eml JOIN accounts acc ON acc.id = eml.confirmed_by WHERE eml.housekeeping_task_id = ht.id ORDER BY eml.id DESC LIMIT 1),
                       (SELECT acc.full_name FROM accounts acc JOIN roles r ON r.id = acc.role_id WHERE r.name = 'HOUSEKEEPING' AND acc.status = 'ACTIVE' ORDER BY (CASE WHEN rm.floor_number <= 2 AND acc.email LIKE '%housekeeping1%' THEN 1 WHEN rm.floor_number >= 3 AND acc.email LIKE '%housekeeping2%' THEN 1 ELSE 2 END), acc.id ASC LIMIT 1)
                   ) AS assigned_staff_name,
                   TRUE AS action_ready
            FROM housekeeping_tasks ht
            JOIN rooms rm ON rm.id = ht.room_id
            JOIN room_types rt ON rt.id = rm.room_type_id
            LEFT JOIN accounts a ON a.id = ht.assigned_to
            """;

    public List<HousekeepingTask> findMyTasks(long viewerId, String keyword, Integer floor,
                                               String taskType, String status,
                                               String sortColumn, String direction,
                                               int offset, int limit)
            throws SQLException {
        StringBuilder sql = new StringBuilder(TASK_SELECT)
                .append(" WHERE (ht.assigned_to = ? OR ht.assigned_to IS NULL) AND ht.status IN ('PENDING', 'IN_PROGRESS')")
                .append(" AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')");
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
                WHERE (ht.assigned_to = ? OR ht.assigned_to IS NULL) AND ht.status IN ('PENDING', 'IN_PROGRESS')
                  AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')
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

        public List<HousekeepingTask> findHistory(long viewerId, boolean manager, String keyword,
                                               Integer floor, String taskType, String status,
                                               String sortColumn, String direction,
                                               int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(TASK_SELECT);
        List<Object> params = new ArrayList<>();
        if (manager) {
            sql.append(" WHERE 1=1");
            if (taskType != null && !taskType.isBlank()) {
                sql.append(" AND ht.task_type = ?");
                params.add(taskType);
            } else {
                sql.append(" AND ht.task_type IN ('CHECKOUT_INSPECTION', 'CLEANING')");
            }
            if (status != null && !status.isBlank()) {
                sql.append(" AND ht.status = ?");
                params.add(status);
            }
        } else {
            sql.append(" WHERE ht.assigned_to = ?")
               .append(" AND ht.status IN ('COMPLETED','CANCELLED')")
               .append(" AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')");
            params.add(viewerId);
            if (taskType != null && !taskType.isBlank()) {
                sql.append(" AND ht.task_type = ?");
                params.add(taskType);
            }
            if (status != null && !status.isBlank()) {
                sql.append(" AND ht.status = ?");
                params.add(status);
            }
        }
        appendRoomFilters(sql, params, keyword, floor);
        sql.append(" ORDER BY ").append(sortColumn).append(' ').append(direction)
                .append(", ht.id DESC LIMIT ? OFFSET ?");
        try (Connection c = requireConnection(); PreparedStatement s = c.prepareStatement(sql.toString())) {
            int i = bind(s, params);
            s.setInt(i++, limit);
            s.setInt(i, offset);
            try (ResultSet rs = s.executeQuery()) {
                List<HousekeepingTask> out = new ArrayList<>();
                while (rs.next()) out.add(mapTask(rs));
                return out;
            }
        }
    }

    public int countHistory(long viewerId, boolean manager, String keyword, Integer floor,
                            String taskType, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM housekeeping_tasks ht
                JOIN rooms rm ON rm.id = ht.room_id
                JOIN room_types rt ON rt.id = rm.room_type_id
                """);
        List<Object> params = new ArrayList<>();
        if (manager) {
            sql.append(" WHERE 1=1");
            if (taskType != null && !taskType.isBlank()) {
                sql.append(" AND ht.task_type = ?");
                params.add(taskType);
            } else {
                sql.append(" AND ht.task_type IN ('CHECKOUT_INSPECTION', 'CLEANING')");
            }
            if (status != null && !status.isBlank()) {
                sql.append(" AND ht.status = ?");
                params.add(status);
            }
        } else {
            sql.append(" WHERE ht.assigned_to = ?")
               .append(" AND ht.status IN ('COMPLETED','CANCELLED')")
               .append(" AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')");
            params.add(viewerId);
            if (taskType != null && !taskType.isBlank()) {
                sql.append(" AND ht.task_type = ?");
                params.add(taskType);
            }
            if (status != null && !status.isBlank()) {
                sql.append(" AND ht.status = ?");
                params.add(status);
            }
        }
        appendRoomFilters(sql, params, keyword, floor);
        try (Connection c = requireConnection(); PreparedStatement s = c.prepareStatement(sql.toString())) {
            bind(s, params);
            try (ResultSet rs = s.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
    private void appendRoomFilters(StringBuilder sql, List<Object> params,
                                   String keyword, Integer floor) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                 AND (
                    LOWER(rm.room_number) LIKE ? 
                    OR LOWER(rt.name) LIKE ? 
                    OR LOWER(COALESCE(ht.note, '')) LIKE ?
                    OR EXISTS (
                        SELECT 1 FROM room_equipment sub_re 
                        JOIN equipment sub_e ON sub_e.id = sub_re.equipment_id 
                        WHERE sub_re.id = ht.room_equipment_id
                          AND LOWER(sub_e.name) LIKE ?
                    )
                    OR EXISTS (
                        SELECT 1 FROM equipment_maintenance_logs sub_eml
                        JOIN room_equipment sub_re2 ON sub_re2.id = sub_eml.room_equipment_id
                        JOIN equipment sub_e2 ON sub_e2.id = sub_re2.equipment_id
                        WHERE sub_eml.housekeeping_task_id = ht.id
                          AND LOWER(sub_e2.name) LIKE ?
                    )
                 )
                """);
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (floor != null) {
            sql.append(" AND rm.floor_number = ?");
            params.add(floor);
        }
    }

    private int bind(PreparedStatement statement, List<Object> params) throws SQLException {
        int index = 1;
        for (Object value : params) {
            if (value instanceof Number number) statement.setLong(index++, number.longValue());
            else statement.setString(index++, String.valueOf(value));
        }
        return index;
    }

    public Optional<HousekeepingTask> findById(long taskId, long viewerId, boolean manager) throws SQLException {
        String sql = TASK_SELECT + " WHERE ht.id = ?"
                + (manager ? "" : " AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING') AND (ht.assigned_to = ? OR ht.assigned_to IS NULL)");
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            if (!manager) statement.setLong(2, viewerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapTask(rs)) : Optional.empty();
            }
        }
    }
    public List<HousekeepingTask.EquipmentCheck> findEquipment(long roomId, Long bookingRoomId) throws SQLException {
        String sql = """
                SELECT re.id, e.name, e.is_maintainable, e.default_compensation_price, re.quantity, re.status,
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
                    item.setMaintainable(rs.getBoolean("is_maintainable"));
                    item.setDefaultCompensationPrice(rs.getBigDecimal("default_compensation_price"));
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

    public long claimCleaning(long taskId, long staffId) throws SQLException {
        String sql = """
                UPDATE housekeeping_tasks
                SET assigned_to = ?
                WHERE id = ? AND task_type = 'CLEANING'
                  AND status = 'PENDING' AND assigned_to IS NULL
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, staffId);
            statement.setLong(2, taskId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Công việc vừa được nhân viên khác nhận hoặc không còn khả dụng");
            }
            return taskId;
        }
    }

    public void completeInspection(long taskId, long staffId,
                                   List<HousekeepingTask.EquipmentCheck> checks,
                                   String inspectionNote) throws SQLException {
        String lockSql = """
                SELECT ht.room_id, ht.booking_room_id
                FROM housekeeping_tasks ht
                WHERE ht.id = ? AND (ht.assigned_to = ? OR ht.assigned_to IS NULL)
                  AND ht.task_type = 'CHECKOUT_INSPECTION'
                  AND ht.status IN ('PENDING','IN_PROGRESS')
                FOR UPDATE
                """;
        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long roomId;
                long bookingRoomId;
                long bookingId = 0;
                long inspectionId = 0;
                try (PreparedStatement lock = connection.prepareStatement(lockSql)) {
                    lock.setLong(1, taskId);
                    lock.setLong(2, staffId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Công việc kiểm tra không hợp lệ hoặc đã hoàn thành");
                        roomId = rs.getLong("room_id");
                        Object linkedBookingRoom = rs.getObject("booking_room_id");
                        bookingRoomId = linkedBookingRoom == null ? 0L : ((Number) linkedBookingRoom).longValue();
                    }
                }
                long[] booking = resolveInspectionBooking(connection, roomId, bookingRoomId);
                boolean isCheckout = (booking != null);
                if (isCheckout) {
                    bookingRoomId = booking[0];
                    bookingId = booking[1];
                    inspectionId = ensurePendingInspection(connection, taskId, bookingRoomId, staffId);
                    try (PreparedStatement updateTask = connection.prepareStatement("""
                            UPDATE housekeeping_tasks
                            SET booking_room_id=?, status='IN_PROGRESS',
                                started_at=COALESCE(started_at,CURRENT_TIMESTAMP)
                            WHERE id=?
                            """)) {
                        updateTask.setLong(1, bookingRoomId);
                        updateTask.setLong(2, taskId);
                        updateTask.executeUpdate();
                    }
                }
                boolean damaged = false;
                for (HousekeepingTask.EquipmentCheck check : checks) {
                    validateEquipment(connection, roomId, check.getRoomEquipmentId());
                    long itemId = isCheckout ? insertInspectionItem(connection, inspectionId, check) : 0;
                    if (!"NORMAL".equals(check.getConditionStatus())) {
                        damaged = true;
                        long damageReportId = isCheckout ? insertDamageReport(connection, itemId, bookingId, check) : 0;
                        markEquipmentAndCreateTask(connection, roomId, damageReportId, check, staffId);
                    }
                }
                if (isCheckout) {
                    updateInspection(connection, inspectionId, damaged, inspectionNote);
                }
                completeTask(connection, taskId);

                boolean hasCleaningRequest = (inspectionNote != null && !inspectionNote.isBlank());
                if (isCheckout) {
                    try (PreparedStatement updateRoom = connection.prepareStatement("""
                            UPDATE rooms SET status = CASE 
                                WHEN status = 'INACTIVE' THEN 'INACTIVE'
                                WHEN ? THEN 'NOT_READY'
                                ELSE 'INSPECTION' 
                            END WHERE id = ?
                            """)) {
                        updateRoom.setBoolean(1, damaged);
                        updateRoom.setLong(2, roomId);
                        updateRoom.executeUpdate();
                    }
                } else if (hasCleaningRequest) {
                    createCleaningTask(connection, roomId, null, staffId, inspectionNote);
                    try (PreparedStatement updateRoom = connection.prepareStatement(
                            "UPDATE rooms SET status = CASE WHEN status = 'INACTIVE' THEN 'INACTIVE' WHEN ? THEN 'NOT_READY' ELSE 'CLEANING' END WHERE id = ?")) {
                        updateRoom.setBoolean(1, damaged);
                        updateRoom.setLong(2, roomId);
                        updateRoom.executeUpdate();
                    }
                } else {
                    try (PreparedStatement updateRoom = connection.prepareStatement("""
                            UPDATE rooms rm SET rm.status = CASE 
                                WHEN rm.status = 'INACTIVE' THEN 'INACTIVE'
                                WHEN ? THEN 'NOT_READY'
                                WHEN EXISTS (
                                    SELECT 1 FROM booking_rooms br
                                    JOIN bookings b ON b.id = br.booking_id
                                    WHERE br.room_id = rm.id AND b.status IN ('CHECKED_IN', 'IN_HOUSE')
                                ) THEN 'OCCUPIED'
                                ELSE 'AVAILABLE' 
                            END WHERE rm.id = ?
                            """)) {
                        updateRoom.setBoolean(1, damaged);
                        updateRoom.setLong(2, roomId);
                        updateRoom.executeUpdate();
                    }
                }
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
                JOIN rooms rm ON rm.id = ht.room_id
                SET ht.status = 'IN_PROGRESS', ht.started_at = CURRENT_TIMESTAMP,
                    ht.assigned_to = ?,
                    rm.status = CASE WHEN rm.status IN ('NOT_READY','MAINTENANCE')
                        THEN rm.status ELSE 'CLEANING' END
                WHERE ht.id = ? AND (ht.assigned_to = ? OR ht.assigned_to IS NULL)
                  AND ht.task_type = 'CLEANING' AND ht.status IN ('PENDING', 'IN_PROGRESS')
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, staffId);
            statement.setLong(2, taskId);
            statement.setLong(3, staffId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Công việc dọn phòng không hợp lệ hoặc đã được xử lý");
            }
        }
    }
    private long[] resolveInspectionBooking(Connection connection, long roomId, long bookingRoomId)
            throws SQLException {
        if (bookingRoomId <= 0) {
            return null;
        }
        String sql = """
                SELECT br.id, br.booking_id
                FROM booking_rooms br
                JOIN bookings b ON b.id = br.booking_id
                WHERE br.id = ? AND br.room_id = ?
                  AND b.status IN ('CHECKOUT_PENDING', 'CHECKED_OUT')
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookingRoomId);
            statement.setLong(2, roomId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new long[]{rs.getLong("id"), rs.getLong("booking_id")};
            }
        }
    }
    private long ensurePendingInspection(Connection connection, long taskId,
                                         long bookingRoomId, long staffId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id,status FROM room_inspections WHERE housekeeping_task_id=? FOR UPDATE")) {
            statement.setLong(1, taskId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    if (!"PENDING".equals(rs.getString("status"))) {
                        throw new SQLException("Công việc kiểm tra đã hoàn thành");
                    }
                    return rs.getLong("id");
                }
            }
        }
        String sql = """
                INSERT INTO room_inspections
                    (housekeeping_task_id,booking_room_id,inspected_by,status)
                VALUES(?,?,?,'PENDING')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, taskId);
            statement.setLong(2, bookingRoomId);
            statement.setLong(3, staffId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không thể tạo kết quả kiểm tra phòng");
                return keys.getLong(1);
            }
        }
    }

    public List<HousekeepingTask.EquipmentCheck> findInspectionResults(long taskId) throws SQLException {
        String sql="SELECT re.id,e.name,re.quantity,re.status,ii.condition_status,ii.damage_fee,ii.note FROM room_inspections ri JOIN inspection_items ii ON ii.inspection_id=ri.id JOIN room_equipment re ON re.id=ii.room_equipment_id JOIN equipment e ON e.id=re.equipment_id WHERE ri.housekeeping_task_id=? ORDER BY e.name";
        try(Connection c=requireConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setLong(1,taskId);try(ResultSet rs=s.executeQuery()){
                List<HousekeepingTask.EquipmentCheck> out=new ArrayList<>();
                while(rs.next()){HousekeepingTask.EquipmentCheck i=new HousekeepingTask.EquipmentCheck();i.setRoomEquipmentId(rs.getLong("id"));i.setEquipmentName(rs.getString("name"));i.setQuantity(rs.getInt("quantity"));i.setCurrentStatus(rs.getString("status"));i.setConditionStatus(rs.getString("condition_status"));i.setDamageFee(rs.getBigDecimal("damage_fee"));i.setNote(rs.getString("note"));out.add(i);}return out;
            }
        }
    }

    public List<HousekeepingTask.EquipmentCheck> findCleaningEquipment(long taskId) throws SQLException {
        String sql = """
                SELECT re.id, e.name, re.quantity, re.status,
                       COALESCE(ii.condition_status, re.status) AS condition_status,
                       COALESCE(ii.damage_fee, 0) AS damage_fee,
                       COALESCE(re.note, ii.note) AS note
                FROM room_equipment re
                JOIN equipment e ON e.id = re.equipment_id
                JOIN housekeeping_tasks cleaning ON cleaning.room_id = re.room_id
                LEFT JOIN room_inspections ri ON ri.booking_room_id = cleaning.booking_room_id
                LEFT JOIN inspection_items ii ON ii.inspection_id = ri.id AND ii.room_equipment_id = re.id
                WHERE cleaning.id = ?
                  AND (
                      re.status IN ('DAMAGED', 'MISSING', 'WAITING_REPAIR', 'WAITING_REPLACEMENT', 'MAINTENANCE')
                      OR (ii.condition_status IS NOT NULL AND ii.condition_status != 'NORMAL')
                      OR EXISTS (
                          SELECT 1 FROM housekeeping_tasks ht_issue
                          WHERE ht_issue.room_id = cleaning.room_id
                            AND ht_issue.room_equipment_id = re.id
                            AND ht_issue.task_type IN ('EQUIPMENT_REPAIR', 'EQUIPMENT_REPLACEMENT', 'MAINTENANCE_CHECK')
                            AND ht_issue.status IN ('PENDING', 'IN_PROGRESS')
                      )
                  )
                ORDER BY e.name
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            try (ResultSet rs = statement.executeQuery()) {
                List<HousekeepingTask.EquipmentCheck> result = new ArrayList<>();
                while (rs.next()) {
                    HousekeepingTask.EquipmentCheck item = new HousekeepingTask.EquipmentCheck();
                    item.setRoomEquipmentId(rs.getLong("id"));
                    item.setEquipmentName(rs.getString("name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCurrentStatus(rs.getString("status"));
                    item.setConditionStatus(rs.getString("condition_status"));
                    item.setDamageFee(rs.getBigDecimal("damage_fee"));
                    item.setNote(rs.getString("note"));
                    result.add(item);
                }
                return result;
            }
        }
    }

    public void completeCleaning(long taskId, long staffId) throws SQLException {
        String lockSql = """
                SELECT room_id FROM housekeeping_tasks
                WHERE id = ? AND (assigned_to = ? OR assigned_to IS NULL) AND task_type = 'CLEANING'
                  AND status IN ('PENDING', 'IN_PROGRESS') FOR UPDATE
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
                        "UPDATE housekeeping_tasks SET status='COMPLETED', assigned_to=?, completed_at=CURRENT_TIMESTAMP WHERE id=?")) {
                    update.setLong(1, staffId); update.setLong(2, taskId); update.executeUpdate();
                }
                try (PreparedStatement updateRoom = connection.prepareStatement("""
                        UPDATE rooms rm SET rm.status = CASE
                        WHEN rm.status = 'INACTIVE' THEN 'INACTIVE'
                        WHEN rm.status = 'MAINTENANCE' THEN 'MAINTENANCE'
                        WHEN EXISTS (
                            SELECT 1 FROM room_equipment re
                            WHERE re.room_id = rm.id AND re.status <> 'NORMAL'
                        ) THEN 'NOT_READY'
                        WHEN EXISTS (
                            SELECT 1 FROM booking_rooms br
                            JOIN bookings b ON b.id = br.booking_id
                            WHERE br.room_id = rm.id AND b.status IN ('CHECKED_IN', 'IN_HOUSE')
                        ) THEN 'OCCUPIED'
                        ELSE 'AVAILABLE' END
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
        BigDecimal defaultCompPrice = check.getDefaultCompensationPrice();
        boolean isMaintainable = check.isMaintainable();
        if (defaultCompPrice == null || defaultCompPrice.compareTo(BigDecimal.ZERO) <= 0) {
            String lookupSql = "SELECT e.default_compensation_price, e.is_maintainable FROM room_equipment re JOIN equipment e ON e.id = re.equipment_id WHERE re.id = ?";
            try (PreparedStatement ps = connection.prepareStatement(lookupSql)) {
                ps.setLong(1, check.getRoomEquipmentId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        defaultCompPrice = rs.getBigDecimal("default_compensation_price");
                        isMaintainable = rs.getBoolean("is_maintainable");
                    }
                }
            }
        }
        if (defaultCompPrice == null) defaultCompPrice = BigDecimal.ZERO;

        BigDecimal compensationAmount = check.getDamageFee();
        if (compensationAmount == null || compensationAmount.compareTo(BigDecimal.ZERO) <= 0) {
            if ("MISSING".equalsIgnoreCase(check.getConditionStatus())) {
                compensationAmount = defaultCompPrice;
            } else if ("DAMAGED".equalsIgnoreCase(check.getConditionStatus())) {
                if (!isMaintainable) {
                    compensationAmount = BigDecimal.ZERO;
                } else {
                    compensationAmount = defaultCompPrice.multiply(new BigDecimal("0.30")).setScale(-3, java.math.RoundingMode.HALF_UP);
                }
            } else {
                compensationAmount = BigDecimal.ZERO;
            }
        }

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
            statement.setBigDecimal(5, compensationAmount);
            statement.setString(6, check.getNote());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không lấy được ID báo cáo sự cố");
                return keys.getLong(1);
            }
        }
    }

    private void markEquipmentAndCreateTask(Connection connection, long roomId, long damageReportId,
                                            HousekeepingTask.EquipmentCheck check, Long staffId) throws SQLException {
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
            if (update.executeUpdate() == 0) throw new SQLException("Không thể cập nhật trạng thái thiết bị");
        }
        Long assignedTo = (staffId != null && staffId > 0) ? staffId : getHousekeeperForRoom(connection, roomId);
        String sql = """
                INSERT INTO housekeeping_tasks
                    (room_id, room_equipment_id, assigned_to, task_type, priority, status, note)
                SELECT ?, ?, ?, ?, 'HIGH', 'PENDING', ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM housekeeping_tasks
                    WHERE room_equipment_id = ? AND task_type = ?
                      AND status IN ('PENDING','IN_PROGRESS')
                )
                """;
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            insert.setLong(1, roomId); insert.setLong(2, check.getRoomEquipmentId());
            if (assignedTo != null && assignedTo > 0) {
                insert.setLong(3, assignedTo);
            } else {
                insert.setNull(3, java.sql.Types.BIGINT);
            }
            insert.setString(4, taskType);
            String taskNote = (check.getNote() != null && !check.getNote().isBlank()) 
                    ? check.getNote().trim() 
                    : (check.getEquipmentName() != null ? check.getEquipmentName() + " - " + check.getCurrentStatusLabel() : "Sự cố thiết bị");
            insert.setString(5, taskNote);
            insert.setLong(6, check.getRoomEquipmentId()); insert.setString(7, taskType);
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

    private void createCleaningTask(Connection connection, long roomId, Long bookingRoomId, Long staffId,
                                    String cleaningNote) throws SQLException {
        if (staffId == null || staffId <= 0) {
            staffId = getHousekeeperForRoom(connection, roomId);
        }
        if ((cleaningNote == null || cleaningNote.isBlank()) && bookingRoomId != null && bookingRoomId > 0) {
            try (PreparedStatement notePs = connection.prepareStatement(
                    "SELECT note FROM room_inspections WHERE booking_room_id = ? ORDER BY id DESC LIMIT 1")) {
                notePs.setLong(1, bookingRoomId);
                try (ResultSet noteRs = notePs.executeQuery()) {
                    if (noteRs.next()) {
                        cleaningNote = noteRs.getString("note");
                    }
                }
            }
        }
        if (cleaningNote == null || cleaningNote.isBlank()) {
            cleaningNote = "[CLEANING_TASKS]\n[ ] Dọn vệ sinh tổng quát và kiểm tra lại phòng\n[/CLEANING_TASKS]";
        } else if (!cleaningNote.contains("[CLEANING_TASKS]") && !cleaningNote.contains("[===TASKS===]")) {
            cleaningNote = "[CLEANING_TASKS]\n[ ] Dọn vệ sinh tổng quát và kiểm tra lại phòng\n[/CLEANING_TASKS]\n[INSPECTION_NOTE]\n" + cleaningNote.trim();
        }
        if (bookingRoomId != null && bookingRoomId > 0) {
            String sql = """
                    INSERT INTO housekeeping_tasks
                        (room_id, booking_room_id, assigned_to, task_type, priority, status, note)
                    SELECT ?, ?, ?, 'CLEANING', 'NORMAL', 'PENDING', ?
                    WHERE NOT EXISTS (
                        SELECT 1 FROM housekeeping_tasks
                        WHERE booking_room_id = ? AND task_type = 'CLEANING'
                          AND status <> 'CANCELLED'
                    )
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, roomId);
                statement.setLong(2, bookingRoomId);
                if (staffId != null && staffId > 0) statement.setLong(3, staffId);
                else statement.setNull(3, java.sql.Types.BIGINT);
                statement.setString(4, cleaningNote);
                statement.setLong(5, bookingRoomId);
                statement.executeUpdate();
            }
        } else {
            String sql = """
                    INSERT INTO housekeeping_tasks
                        (room_id, booking_room_id, assigned_to, task_type, priority, status, note)
                    VALUES (?, NULL, ?, 'CLEANING', 'NORMAL', 'PENDING', ?)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, roomId);
                if (staffId != null && staffId > 0) statement.setLong(2, staffId);
                else statement.setNull(2, java.sql.Types.BIGINT);
                statement.setString(3, cleaningNote);
                statement.executeUpdate();
            }
        }
    }

    private Long getHousekeeperForRoom(Connection connection, long roomId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT floor_number FROM rooms WHERE id = ?")) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int floor = rs.getInt("floor_number");
                    String targetEmail = (floor >= 3) ? "housekeeping2@hms.com" : "housekeeping1@hms.com";
                    try (PreparedStatement staffPs = connection.prepareStatement("SELECT id FROM accounts WHERE email = ? AND status = 'ACTIVE' LIMIT 1")) {
                        staffPs.setString(1, targetEmail);
                        try (ResultSet staffRs = staffPs.executeQuery()) {
                            if (staffRs.next()) return staffRs.getLong("id");
                        }
                    }
                    try (PreparedStatement staffPs = connection.prepareStatement("SELECT a.id FROM accounts a JOIN roles r ON a.role_id = r.id WHERE r.name = 'HOUSEKEEPING' AND a.status = 'ACTIVE' ORDER BY a.id ASC")) {
                        try (ResultSet staffRs = staffPs.executeQuery()) {
                            List<Long> ids = new ArrayList<>();
                            while (staffRs.next()) ids.add(staffRs.getLong("id"));
                            if (!ids.isEmpty()) {
                                if (floor >= 3 && ids.size() > 1) return ids.get(1);
                                return ids.get(0);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
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

    public void reportIssue(long roomId, Long roomEquipmentId, String newStatus, String note, Long reportedBy) throws SQLException {
        String taskType = "EQUIPMENT_REPAIR";
        if ("MISSING".equals(newStatus) || "WAITING_REPLACEMENT".equals(newStatus)) {
            taskType = "EQUIPMENT_REPLACEMENT";
        } else if ("MAINTENANCE".equals(newStatus)) {
            taskType = "MAINTENANCE_CHECK";
        }
        
        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (roomEquipmentId != null && roomEquipmentId > 0 && newStatus != null && !"NORMAL".equals(newStatus)) {
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE room_equipment SET status = ?, note = ? WHERE id = ? AND room_id = ?")) {
                        String eqNote = note;
                        if (eqNote != null && eqNote.length() > 500) eqNote = eqNote.substring(0, 500);
                        update.setString(1, newStatus);
                        update.setString(2, eqNote);
                        update.setLong(3, roomEquipmentId);
                        update.setLong(4, roomId);
                        update.executeUpdate();
                    }
                }
                
                Long staffId = (reportedBy != null && reportedBy > 0) ? reportedBy : getHousekeeperForRoom(connection, roomId);
                String sql = """
                        INSERT INTO housekeeping_tasks
                            (room_id, room_equipment_id, assigned_to, task_type, priority, status, note)
                        VALUES (?, ?, ?, ?, 'HIGH', 'PENDING', ?)
                        """;
                try (PreparedStatement insert = connection.prepareStatement(sql)) {
                    insert.setLong(1, roomId);
                    if (roomEquipmentId != null && roomEquipmentId > 0) {
                        insert.setLong(2, roomEquipmentId);
                    } else {
                        insert.setNull(2, java.sql.Types.BIGINT);
                        taskType = "MAINTENANCE_CHECK"; 
                    }
                    if (staffId != null && staffId > 0) {
                        insert.setLong(3, staffId);
                    } else {
                        insert.setNull(3, java.sql.Types.BIGINT);
                    }
                    insert.setString(4, taskType);
                    insert.setString(5, note);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public void reportIssue(long roomId, Long roomEquipmentId, String newStatus, String note) throws SQLException {
        reportIssue(roomId, roomEquipmentId, newStatus, note, null);
    }

    public List<HousekeepingTask> findIssueTasks(String keyword, Integer floor, String taskType, String status,
                                                  String sortColumn, String direction, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(TASK_SELECT)
                .append(" WHERE ht.task_type IN ('EQUIPMENT_REPAIR', 'MAINTENANCE_CHECK', 'EQUIPMENT_REPLACEMENT')");
        List<Object> params = new ArrayList<>();
        appendRoomFilters(sql, params, keyword, floor);
        if (taskType != null && !taskType.isBlank()) {
            sql.append(" AND ht.task_type = ?");
            params.add(taskType);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND ht.status = ?");
            params.add(status);
        }
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

    public int countIssueTasks(String keyword, Integer floor, String taskType, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM housekeeping_tasks ht
                JOIN rooms rm ON rm.id = ht.room_id
                JOIN room_types rt ON rt.id = rm.room_type_id
                WHERE ht.task_type IN ('EQUIPMENT_REPAIR', 'MAINTENANCE_CHECK', 'EQUIPMENT_REPLACEMENT')
                """);
        List<Object> params = new ArrayList<>();
        appendRoomFilters(sql, params, keyword, floor);
        if (taskType != null && !taskType.isBlank()) {
            sql.append(" AND ht.task_type = ?");
            params.add(taskType);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND ht.status = ?");
            params.add(status);
        }
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
    public List<HousekeepingTask.EquipmentCheck> findDamagedEquipmentById(long roomEquipmentId) throws SQLException {
        String sql = """
                SELECT re.id, e.name, e.is_maintainable, re.quantity, re.status,
                       NULL AS initial_status, NULL AS initial_quantity
                FROM room_equipment re
                JOIN equipment e ON e.id = re.equipment_id
                WHERE re.id = ? AND re.status IN ('DAMAGED', 'MISSING', 'WAITING_REPAIR', 'WAITING_REPLACEMENT', 'MAINTENANCE')
                ORDER BY e.name
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomEquipmentId);
            try (ResultSet rs = statement.executeQuery()) {
                List<HousekeepingTask.EquipmentCheck> result = new ArrayList<>();
                while (rs.next()) {
                    HousekeepingTask.EquipmentCheck item = new HousekeepingTask.EquipmentCheck();
                    item.setRoomEquipmentId(rs.getLong("id"));
                    item.setEquipmentName(rs.getString("name"));
                    item.setMaintainable(rs.getBoolean("is_maintainable"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCurrentStatus(rs.getString("status"));
                    result.add(item);
                }
                return result;
            }
        }
    }

    public List<HousekeepingTask.EquipmentCheck> findDamagedEquipments(long roomId) throws SQLException {
        String sql = """
                SELECT re.id, e.name, e.is_maintainable, re.quantity, re.status,
                       NULL AS initial_status, NULL AS initial_quantity
                FROM room_equipment re
                JOIN equipment e ON e.id = re.equipment_id
                WHERE re.room_id = ? AND re.status IN ('DAMAGED', 'MISSING', 'WAITING_REPAIR', 'WAITING_REPLACEMENT', 'MAINTENANCE')
                ORDER BY e.name
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            try (ResultSet rs = statement.executeQuery()) {
                List<HousekeepingTask.EquipmentCheck> result = new ArrayList<>();
                while (rs.next()) {
                    HousekeepingTask.EquipmentCheck item = new HousekeepingTask.EquipmentCheck();
                    item.setRoomEquipmentId(rs.getLong("id"));
                    item.setEquipmentName(rs.getString("name"));
                    item.setMaintainable(rs.getBoolean("is_maintainable"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCurrentStatus(rs.getString("status"));
                    result.add(item);
                }
                return result;
            }
        }
    }

    public static class HousekeepingStats {
        private int pendingCount;
        private int inProgressCount;
        private int completedTodayCount;
        private int totalCompletedCount;

        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        public int getInProgressCount() { return inProgressCount; }
        public void setInProgressCount(int inProgressCount) { this.inProgressCount = inProgressCount; }
        public int getCompletedTodayCount() { return completedTodayCount; }
        public void setCompletedTodayCount(int completedTodayCount) { this.completedTodayCount = completedTodayCount; }
        public int getTotalCompletedCount() { return totalCompletedCount; }
        public void setTotalCompletedCount(int totalCompletedCount) { this.totalCompletedCount = totalCompletedCount; }
    }

    public HousekeepingStats getHousekeepingStats() throws SQLException {
        String sql = """
                SELECT 
                    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) AS pending_count,
                    COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) AS in_progress_count,
                    COUNT(CASE WHEN status = 'COMPLETED' AND CAST(completed_at AS DATE) = CURRENT_DATE THEN 1 END) AS completed_today,
                    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS total_completed
                FROM housekeeping_tasks
                WHERE task_type IN ('CHECKOUT_INSPECTION', 'CLEANING')
                """;
        try (Connection connection = requireConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            HousekeepingStats stats = new HousekeepingStats();
            if (rs.next()) {
                stats.setPendingCount(rs.getInt("pending_count"));
                stats.setInProgressCount(rs.getInt("in_progress_count"));
                stats.setCompletedTodayCount(rs.getInt("completed_today"));
                stats.setTotalCompletedCount(rs.getInt("total_completed"));
            }
            return stats;
        }
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        return connection;
    }

    public List<HousekeeperWorkloadDTO> getHousekeeperWorkloads() throws SQLException {
        String sql = """
                SELECT 
                    a.id AS user_id,
                    a.full_name,
                    a.phone,
                    COUNT(CASE WHEN ht.status = 'IN_PROGRESS' THEN 1 END) AS in_progress_count,
                    COUNT(CASE WHEN ht.status = 'PENDING' THEN 1 END) AS pending_count,
                    COUNT(CASE WHEN ht.status = 'COMPLETED' AND CAST(ht.completed_at AS DATE) = CURRENT_DATE THEN 1 END) AS completed_today,
                    MAX(CASE WHEN ht.status = 'IN_PROGRESS' THEN rm.room_number END) AS current_room_number,
                    MAX(CASE WHEN ht.status = 'IN_PROGRESS' THEN rm.floor_number END) AS current_floor,
                    MAX(CASE WHEN ht.status = 'IN_PROGRESS' THEN ht.started_at END) AS current_started_at
                FROM accounts a
                JOIN roles r ON r.id = a.role_id AND r.name = 'HOUSEKEEPING'
                LEFT JOIN housekeeping_tasks ht ON ht.assigned_to = a.id 
                    AND (
                        ht.status IN ('PENDING', 'IN_PROGRESS') 
                        OR (ht.status = 'COMPLETED' AND CAST(ht.completed_at AS DATE) = CURRENT_DATE)
                    )
                LEFT JOIN rooms rm ON rm.id = ht.room_id
                WHERE a.status = 'ACTIVE'
                GROUP BY a.id, a.full_name, a.phone
                ORDER BY a.id ASC
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<HousekeeperWorkloadDTO> list = new ArrayList<>();
            while (rs.next()) {
                HousekeeperWorkloadDTO dto = new HousekeeperWorkloadDTO();
                dto.setUserId(rs.getLong("user_id"));
                dto.setFullName(rs.getString("full_name"));
                dto.setPhone(rs.getString("phone"));
                dto.setInProgressCount(rs.getInt("in_progress_count"));
                dto.setPendingCount(rs.getInt("pending_count"));
                dto.setCompletedToday(rs.getInt("completed_today"));
                dto.setCurrentRoomNumber(rs.getString("current_room_number"));
                int fl = rs.getInt("current_floor");
                if (!rs.wasNull()) dto.setCurrentFloor(fl);
                dto.setCurrentStartedAt(rs.getTimestamp("current_started_at"));
                list.add(dto);
            }
            return list;
        }
    }

    public void createManualTask(long roomId, String taskType, Long assignedTo, String priority, String note) throws SQLException {
        try (Connection connection = requireConnection()) {
            connection.setAutoCommit(false);
            try {
                int floor = 0;
                try (PreparedStatement psF = connection.prepareStatement("SELECT floor_number FROM rooms WHERE id = ?")) {
                    psF.setLong(1, roomId);
                    try (ResultSet rsF = psF.executeQuery()) {
                        if (rsF.next()) floor = rsF.getInt(1);
                    }
                }
                
                if (assignedTo == null || assignedTo <= 0) {
                    List<Long> hks = new ArrayList<>();
                    try (PreparedStatement psHk = connection.prepareStatement(
                            "SELECT a.id FROM accounts a JOIN roles r ON r.id = a.role_id WHERE r.name='HOUSEKEEPING' AND a.status = 'ACTIVE' ORDER BY a.id ASC")) {
                        try (ResultSet rsHk = psHk.executeQuery()) {
                            while (rsHk.next()) hks.add(rsHk.getLong(1));
                        }
                    }
                    if (hks.size() >= 2) {
                        if (floor <= 2) assignedTo = hks.get(0);
                        else assignedTo = hks.get(1);
                    } else if (!hks.isEmpty()) {
                        assignedTo = hks.get(0);
                    }
                }

                // Kiểm tra xem nhân viên được gán có đang bận xử lý task IN_PROGRESS nào không
                boolean isAssigneeBusy = false;
                if (assignedTo != null && assignedTo > 0) {
                    String checkBusySql = "SELECT COUNT(*) FROM housekeeping_tasks WHERE assigned_to = ? AND status = 'IN_PROGRESS'";
                    try (PreparedStatement psBusy = connection.prepareStatement(checkBusySql)) {
                        psBusy.setLong(1, assignedTo);
                        try (ResultSet rsBusy = psBusy.executeQuery()) {
                            if (rsBusy.next() && rsBusy.getInt(1) > 0) {
                                isAssigneeBusy = true;
                            }
                        }
                    }
                }

                String sql = """
                        INSERT INTO housekeeping_tasks
                            (room_id, assigned_to, task_type, priority, status, note, started_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, roomId);
                    if (assignedTo != null && assignedTo > 0) {
                        statement.setLong(2, assignedTo);
                        if (isAssigneeBusy) {
                            statement.setString(5, "PENDING");
                            statement.setNull(7, java.sql.Types.TIMESTAMP);
                        } else {
                            statement.setString(5, "IN_PROGRESS");
                            statement.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));
                        }
                    } else {
                        statement.setNull(2, java.sql.Types.BIGINT);
                        statement.setString(5, "PENDING");
                        statement.setNull(7, java.sql.Types.TIMESTAMP);
                    }
                    statement.setString(3, taskType);
                    statement.setString(4, priority);
                    statement.setString(6, note);
                    statement.executeUpdate();
                }
                
                String roomStatus = "CLEANING".equals(taskType) ? "CLEANING" : "INSPECTION";
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE rooms SET status = CASE WHEN status = 'INACTIVE' THEN status ELSE ? END WHERE id = ?")) {
                    statement.setString(1, roomStatus);
                    statement.setLong(2, roomId);
                    statement.executeUpdate();
                }
                
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public void updateTaskNote(long taskId, long staffId, String newNote) throws SQLException {
        String sql = "UPDATE housekeeping_tasks SET note = ? WHERE id = ?"
                + (staffId > 0 ? " AND (assigned_to = ? OR assigned_to IS NULL)" : "");
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newNote);
            statement.setLong(2, taskId);
            if (staffId > 0) {
                statement.setLong(3, staffId);
            }
            statement.executeUpdate();
        }
    }
    public void syncDatabaseState() throws SQLException {
        String fixCleaningRoomsSql = """
            UPDATE rooms rm SET rm.status = CASE
                WHEN rm.status = 'INACTIVE' THEN 'INACTIVE'
                WHEN EXISTS (
                    SELECT 1 FROM room_equipment re
                    WHERE re.room_id = rm.id AND re.status IN ('DAMAGED', 'MISSING', 'WAITING_REPAIR', 'WAITING_REPLACEMENT', 'MAINTENANCE')
                ) THEN 'NOT_READY'
                WHEN EXISTS (
                    SELECT 1 FROM booking_rooms br
                    JOIN bookings b ON b.id = br.booking_id
                    WHERE br.room_id = rm.id AND b.status IN ('CHECKED_IN', 'IN_HOUSE')
                ) THEN 'OCCUPIED'
                ELSE 'AVAILABLE' END
            WHERE rm.status = 'CLEANING' AND NOT EXISTS (
                SELECT 1 FROM housekeeping_tasks ht 
                WHERE ht.room_id = rm.id AND ht.task_type = 'CLEANING' AND ht.status IN ('PENDING', 'IN_PROGRESS')
            )
            """;

        String fixInspectionRoomsSql = """
            UPDATE rooms rm SET rm.status = CASE
                WHEN rm.status = 'INACTIVE' THEN 'INACTIVE'
                WHEN EXISTS (
                    SELECT 1 FROM room_equipment re
                    WHERE re.room_id = rm.id AND re.status IN ('DAMAGED', 'MISSING', 'WAITING_REPAIR', 'WAITING_REPLACEMENT', 'MAINTENANCE')
                ) THEN 'NOT_READY'
                WHEN EXISTS (
                    SELECT 1 FROM booking_rooms br
                    JOIN bookings b ON b.id = br.booking_id
                    WHERE br.room_id = rm.id AND b.status IN ('CHECKED_IN', 'IN_HOUSE')
                ) THEN 'OCCUPIED'
                ELSE 'AVAILABLE' END
            WHERE rm.status = 'INSPECTION' AND NOT EXISTS (
                SELECT 1 FROM housekeeping_tasks ht 
                WHERE ht.room_id = rm.id AND ht.task_type = 'CHECKOUT_INSPECTION' AND ht.status IN ('PENDING', 'IN_PROGRESS')
            ) AND NOT EXISTS (
                SELECT 1 FROM booking_rooms br
                JOIN bookings b ON b.id = br.booking_id
                WHERE br.room_id = rm.id AND b.status = 'CHECKOUT_PENDING'
            )
            """;

        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = connection.prepareStatement(fixCleaningRoomsSql)) {
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = connection.prepareStatement(fixInspectionRoomsSql)) {
                    ps2.executeUpdate();
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }
}
