package dao;

import model.MaintenanceLog;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceLogDao {

    public void insertMaintenanceLogs(long taskId, long confirmedBy, List<Long> equipmentIds, String note) throws SQLException {
        String logSql = """
                INSERT INTO equipment_maintenance_logs
                    (housekeeping_task_id, room_equipment_id, action_type,
                     previous_status, new_status, note, confirmed_by, confirmed_at)
                SELECT ?, id, 'RESTORE', status, 'NORMAL', ?, ?, CURRENT_TIMESTAMP
                FROM room_equipment WHERE id = ?
                """;

        String updateEqSql = "UPDATE room_equipment SET status = 'NORMAL' WHERE id = ?";

        String checkTaskCompleteSql = """
                SELECT COUNT(*) FROM room_equipment re
                JOIN housekeeping_tasks ht ON ht.room_id = re.room_id
                WHERE ht.id = ? AND re.status IN ('DAMAGED', 'MISSING', 'WAITING_REPAIR', 'WAITING_REPLACEMENT', 'MAINTENANCE')
                """;

        String updateTaskSql = "UPDATE housekeeping_tasks SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        String completeSpecificTaskSql = """
                UPDATE housekeeping_tasks SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP
                WHERE room_equipment_id = ? AND status IN ('PENDING', 'IN_PROGRESS')
                """;

        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement insertLog = connection.prepareStatement(logSql);
                     PreparedStatement updateEq = connection.prepareStatement(updateEqSql);
                     PreparedStatement completeSpecificTask = connection.prepareStatement(completeSpecificTaskSql)) {
                    for (Long eqId : equipmentIds) {
                        insertLog.setLong(1, taskId);
                        insertLog.setString(2, note);
                        insertLog.setLong(3, confirmedBy);
                        insertLog.setLong(4, eqId);
                        insertLog.addBatch();

                        updateEq.setLong(1, eqId);
                        updateEq.addBatch();
                        
                        completeSpecificTask.setLong(1, eqId);
                        completeSpecificTask.addBatch();
                    }
                    insertLog.executeBatch();
                    updateEq.executeBatch();
                    completeSpecificTask.executeBatch();
                }

                // Auto complete task if no damaged equipments are left for this room
                boolean allFixed = true;
                try (PreparedStatement check = connection.prepareStatement(checkTaskCompleteSql)) {
                    check.setLong(1, taskId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            allFixed = (rs.getInt(1) == 0);
                        }
                    }
                }

                if (allFixed) {
                    try (PreparedStatement complete = connection.prepareStatement(updateTaskSql)) {
                        complete.setLong(1, taskId);
                        complete.executeUpdate();
                    }
                    // Khôi phục trạng thái phòng về AVAILABLE nếu phòng đang ở NOT_READY hoặc MAINTENANCE
                    String restoreRoomSql = """
                            UPDATE rooms rm
                            JOIN housekeeping_tasks ht ON ht.room_id = rm.id
                            SET rm.status = 'AVAILABLE'
                            WHERE ht.id = ? AND rm.status IN ('NOT_READY', 'MAINTENANCE')
                            """;
                    try (PreparedStatement restoreRoom = connection.prepareStatement(restoreRoomSql)) {
                        restoreRoom.setLong(1, taskId);
                        restoreRoom.executeUpdate();
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

    public List<MaintenanceLog> findLogsByTaskId(long taskId) throws SQLException {
        String sql = """
                SELECT eml.id, eml.housekeeping_task_id, eml.room_equipment_id, e.name AS equipment_name,
                       eml.action_type, eml.previous_status, eml.new_status, eml.note,
                       eml.confirmed_by, a.full_name AS confirmed_by_name, eml.confirmed_at
                FROM equipment_maintenance_logs eml
                JOIN room_equipment re ON re.id = eml.room_equipment_id
                JOIN equipment e ON e.id = re.equipment_id
                LEFT JOIN accounts a ON a.id = eml.confirmed_by
                WHERE eml.housekeeping_task_id = ?
                ORDER BY eml.confirmed_at DESC
                """;
        try (Connection connection = requireConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            try (ResultSet rs = statement.executeQuery()) {
                List<MaintenanceLog> logs = new ArrayList<>();
                while (rs.next()) {
                    MaintenanceLog log = new MaintenanceLog();
                    log.setId(rs.getLong("id"));
                    log.setHousekeepingTaskId(rs.getLong("housekeeping_task_id"));
                    log.setRoomEquipmentId(rs.getLong("room_equipment_id"));
                    log.setEquipmentName(rs.getString("equipment_name"));
                    log.setActionType(rs.getString("action_type"));
                    log.setPreviousStatus(rs.getString("previous_status"));
                    log.setNewStatus(rs.getString("new_status"));
                    log.setNote(rs.getString("note"));
                    log.setConfirmedBy(rs.getLong("confirmed_by"));
                    log.setConfirmedByName(rs.getString("confirmed_by_name"));
                    log.setConfirmedAt(rs.getTimestamp("confirmed_at"));
                    logs.add(log);
                }
                return logs;
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        return connection;
    }
}