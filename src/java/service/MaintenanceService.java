package service;

import dao.HousekeepingDao;
import dao.MaintenanceLogDao;
import model.HousekeepingTask;

import java.sql.SQLException;
import java.util.List;

public class MaintenanceService {
    private final HousekeepingDao housekeepingDao = new HousekeepingDao();
    private final MaintenanceLogDao maintenanceLogDao = new MaintenanceLogDao();

    public void reportIssue(long roomId, Long roomEquipmentId, String note) throws SQLException {
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mô tả sự cố");
        }
        housekeepingDao.reportIssue(roomId, roomEquipmentId, note);
    }

    public List<HousekeepingTask> findIssueTasks(String keyword, Integer floor, String sortColumn, String direction, int page, int pageSize) throws SQLException {
        int limit = Math.max(1, Math.min(pageSize, 100));
        int offset = Math.max(0, (page - 1) * limit);
        return housekeepingDao.findIssueTasks(keyword, floor, sortColumn, direction, offset, limit);
    }

    public int countIssueTasks(String keyword, Integer floor) throws SQLException {
        return housekeepingDao.countIssueTasks(keyword, floor);
    }

    public List<HousekeepingTask.EquipmentCheck> findDamagedEquipments(long roomId) throws SQLException {
        return housekeepingDao.findDamagedEquipments(roomId);
    }

    public void verifyMaintenance(long taskId, long staffId, List<Long> equipmentIds, String note) throws SQLException {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn thiết bị nào đã được sửa.");
        }
        maintenanceLogDao.insertMaintenanceLogs(taskId, staffId, equipmentIds, note);
    }
}
