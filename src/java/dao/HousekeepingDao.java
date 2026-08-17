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
                   TRUE AS action_ready
            FROM housekeeping_tasks ht
            JOIN rooms rm ON rm.id = ht.room_id
            JOIN room_types rt ON rt.id = rm.room_type_id
            LEFT JOIN accounts a ON a.id = ht.assigned_to
            """;

    public List<HousekeepingTask> findAvailableWork(String keyword, Integer floor,
                                                     String sortColumn, String direction,
                                                     int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM (").append(availableWorkSelect())
                .append(") available WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendAvailableFilters(sql, params, keyword, floor);
        sql.append(" ORDER BY ").append(sortColumn).append(' ').append(direction)
                .append(", room_number ASC LIMIT ? OFFSET ?");
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

    public int countAvailableWork(String keyword, Integer floor) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM (").append(availableWorkSelect())
                .append(") available WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendAvailableFilters(sql, params, keyword, floor);
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    private String availableWorkSelect() {
        return """
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
                UNION ALL
                SELECT ht.id, ht.room_id, ht.booking_room_id, ht.room_equipment_id,
                       ht.assigned_to, ht.task_type, ht.priority, ht.status, ht.note,
                       ht.created_at, ht.started_at, ht.completed_at, rm.room_number,
                       rm.floor_number, rm.status, rt.name, NULL,
                       TRUE
                FROM housekeeping_tasks ht
                JOIN rooms rm ON rm.id = ht.room_id
                JOIN room_types rt ON rt.id = rm.room_type_id
                WHERE ht.task_type = 'CLEANING' AND ht.status = 'PENDING'
                  AND ht.assigned_to IS NULL
                """;
    }

    private void appendAvailableFilters(StringBuilder sql, List<Object> params,
                                        String keyword, Integer floor) {
        if (keyword != null) {
            sql.append(" AND (LOWER(available.room_number) LIKE ? OR LOWER(available.room_type_name) LIKE ?)");
            String pattern = "%" + keyword.toLowerCase() + "%";
            params.add(pattern); params.add(pattern);
        }
        if (floor != null) { sql.append(" AND available.floor_number = ?"); params.add(floor); }
    }

    public List<HousekeepingTask> findMyTasks(long viewerId, String keyword, Integer floor,
                                               String taskType, String status,
                                               String sortColumn, String direction,
                                               int offset, int limit)
            throws SQLException {
        StringBuilder sql = new StringBuilder(TASK_SELECT)
                .append(" WHERE ht.assigned_to = ? AND ht.status IN ('PENDING', 'IN_PROGRESS')")
                .append(" AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')")
                .append(" AND (ht.task_type <> 'CHECKOUT_INSPECTION' OR ht.booking_room_id IS NOT NULL)");
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
                  AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')
                  AND (ht.task_type <> 'CHECKOUT_INSPECTION' OR ht.booking_room_id IS NOT NULL)
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
        StringBuilder sql = new StringBuilder(TASK_SELECT)
                .append(" WHERE ht.status IN ('COMPLETED','CANCELLED')")
                .append(" AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')");
        List<Object> params = new ArrayList<>();
        if (!manager) { sql.append(" AND ht.assigned_to=?"); params.add(viewerId); }
        appendRoomFilters(sql, params, keyword, floor);
        if (taskType != null) { sql.append(" AND ht.task_type=?"); params.add(taskType); }
        if (status != null) { sql.append(" AND ht.status=?"); params.add(status); }
        sql.append(" ORDER BY ").append(sortColumn).append(' ').append(direction)
                .append(", ht.id DESC LIMIT ? OFFSET ?");
        try(Connection c=requireConnection();PreparedStatement s=c.prepareStatement(sql.toString())){
            int i=bind(s,params);s.setInt(i++,limit);s.setInt(i,offset);
            try(ResultSet rs=s.executeQuery()){List<HousekeepingTask> out=new ArrayList<>();while(rs.next())out.add(mapTask(rs));return out;}
        }
    }

    public int countHistory(long viewerId, boolean manager, String keyword, Integer floor,
                            String taskType, String status) throws SQLException {
        StringBuilder sql=new StringBuilder("SELECT COUNT(*) FROM housekeeping_tasks ht JOIN rooms rm ON rm.id=ht.room_id JOIN room_types rt ON rt.id=rm.room_type_id WHERE ht.status IN ('COMPLETED','CANCELLED') AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')");
        List<Object> params=new ArrayList<>();
        if(!manager){sql.append(" AND ht.assigned_to=?");params.add(viewerId);}
        appendRoomFilters(sql,params,keyword,floor);
        if(taskType!=null){sql.append(" AND ht.task_type=?");params.add(taskType);}
        if(status!=null){sql.append(" AND ht.status=?");params.add(status);}
        try(Connection c=requireConnection();PreparedStatement s=c.prepareStatement(sql.toString())){bind(s,params);try(ResultSet rs=s.executeQuery()){rs.next();return rs.getInt(1);}}
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

    public Optional<HousekeepingTask> findById(long taskId, long viewerId, boolean manager) throws SQLException {
        String sql = TASK_SELECT + " WHERE ht.id = ? AND ht.task_type IN ('CHECKOUT_INSPECTION','CLEANING')"
                + " AND (ht.task_type <> 'CHECKOUT_INSPECTION' OR ht.booking_room_id IS NOT NULL)"
                + (manager ? "" : " AND ht.assigned_to = ?");
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
            if (statement.executeUpdate() != 1) {
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
                WHERE ht.id = ? AND ht.assigned_to = ?
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
                long bookingId;
                long inspectionId;
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
                createCleaningTask(connection, roomId, bookingRoomId, inspectionNote);
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
                    rm.status = CASE WHEN rm.status IN ('NOT_READY','MAINTENANCE')
                        THEN rm.status ELSE 'CLEANING' END
                WHERE ht.id = ? AND ht.assigned_to = ?
                  AND ht.task_type = 'CLEANING' AND ht.status = 'PENDING'
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            statement.setLong(2, staffId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Công việc dọn phòng không hợp lệ hoặc đã được xử lý");
            }
        }
    }

    private long[] resolveInspectionBooking(Connection connection, long roomId, long bookingRoomId)
            throws SQLException {
        String sql = bookingRoomId > 0 ? """
                SELECT br.id, br.booking_id
                FROM booking_rooms br
                JOIN bookings b ON b.id=br.booking_id
                WHERE br.id=? AND br.room_id=?
                  AND b.status IN ('CHECKOUT_PENDING','CHECKED_OUT')
                FOR UPDATE
                """ : """
                SELECT br.id, br.booking_id
                FROM booking_rooms br
                JOIN bookings b ON b.id=br.booking_id
                WHERE br.room_id=? AND b.status IN ('CHECKOUT_PENDING','CHECKED_OUT')
                ORDER BY b.updated_at DESC, br.id DESC
                LIMIT 1 FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            if (bookingRoomId > 0) statement.setLong(index++, bookingRoomId);
            statement.setLong(index, roomId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy lượt lưu trú đang chờ kiểm tra của phòng");
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
                       ii.condition_status, ii.damage_fee, ii.note
                FROM housekeeping_tasks cleaning
                JOIN room_inspections ri ON ri.booking_room_id = cleaning.booking_room_id
                JOIN inspection_items ii ON ii.inspection_id = ri.id
                JOIN room_equipment re ON re.id = ii.room_equipment_id
                JOIN equipment e ON e.id = re.equipment_id
                WHERE cleaning.id = ? AND cleaning.task_type = 'CLEANING'
                  AND ii.condition_status IN ('DAMAGED','MISSING')
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
                        UPDATE rooms rm SET rm.status = CASE
                        WHEN rm.status = 'MAINTENANCE' THEN 'MAINTENANCE'
                        WHEN EXISTS (
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
                                    String cleaningNote) throws SQLException {
        String sql = """
                INSERT INTO housekeeping_tasks
                    (room_id, booking_room_id, assigned_to, task_type, priority, status, note)
                SELECT ?, ?, NULL, 'CLEANING', 'NORMAL', 'PENDING', ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM housekeeping_tasks
                    WHERE booking_room_id = ? AND task_type = 'CLEANING'
                      AND status <> 'CANCELLED'
                )
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            statement.setLong(2, bookingRoomId);
            statement.setString(3, cleaningNote);
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

    public void reportIssue(long roomId, Long roomEquipmentId, String note) throws SQLException {
        String sql = """
                INSERT INTO housekeeping_tasks
                    (room_id, room_equipment_id, task_type, priority, status, note)
                VALUES (?, ?, 'EQUIPMENT_REPAIR', 'HIGH', 'PENDING', ?)
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            if (roomEquipmentId != null && roomEquipmentId > 0) {
                statement.setLong(2, roomEquipmentId);
            } else {
                statement.setNull(2, java.sql.Types.BIGINT);
            }
            statement.setString(3, note);
            statement.executeUpdate();
        }
    }

    public List<HousekeepingTask> findIssueTasks(String keyword, Integer floor, String sortColumn, String direction, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(TASK_SELECT)
                .append(" WHERE ht.task_type IN ('EQUIPMENT_REPAIR', 'MAINTENANCE_CHECK', 'EQUIPMENT_REPLACEMENT')");
        List<Object> params = new ArrayList<>();
        appendRoomFilters(sql, params, keyword, floor);
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

    public int countIssueTasks(String keyword, Integer floor) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM housekeeping_tasks ht
                JOIN rooms rm ON rm.id = ht.room_id
                JOIN room_types rt ON rt.id = rm.room_type_id
                WHERE ht.task_type IN ('EQUIPMENT_REPAIR', 'MAINTENANCE_CHECK', 'EQUIPMENT_REPLACEMENT')
                """);
        List<Object> params = new ArrayList<>();
        appendRoomFilters(sql, params, keyword, floor);
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<HousekeepingTask.EquipmentCheck> findDamagedEquipments(long roomId) throws SQLException {
        String sql = """
                SELECT re.id, e.name, re.quantity, re.status,
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
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCurrentStatus(rs.getString("status"));
                    result.add(item);
                }
                return result;
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        return connection;
    }
}
