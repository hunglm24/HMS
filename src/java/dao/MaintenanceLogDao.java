package dao;

import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

        try (Connection connection = requireConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement insertLog = connection.prepareStatement(logSql);
                     PreparedStatement updateEq = connection.prepareStatement(updateEqSql)) {
                    for (Long eqId : equipmentIds) {
                        insertLog.setLong(1, taskId);
                        insertLog.setString(2, note);
                        insertLog.setLong(3, confirmedBy);
                        insertLog.setLong(4, eqId);
                        insertLog.addBatch();

                        updateEq.setLong(1, eqId);
                        updateEq.addBatch();
                    }
                    insertLog.executeBatch();
                    updateEq.executeBatch();
                }

                // Auto complete task if no damaged equipments are left for this room
                boolean allFixed = true;
                try (PreparedStatement check = connection.prepareStatement(checkTaskCompleteSql)) {
                    check.setLong(1, taskId);
                    try (java.sql.ResultSet rs = check.executeQuery()) {
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

    private Connection requireConnection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) throw new SQLException("Không thể kết nối cơ sở dữ liệu");
        return connection;
    }
}
