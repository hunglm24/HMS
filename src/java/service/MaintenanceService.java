package service;

import dao.HousekeepingDao;
import dao.MaintenanceLogDao;
import model.HousekeepingTask;
import model.MaintenanceLog;

import java.sql.SQLException;
import java.util.List;

public class MaintenanceService {
    private final HousekeepingDao housekeepingDao = new HousekeepingDao();
    private final MaintenanceLogDao maintenanceLogDao = new MaintenanceLogDao();

    public void reportIssue(long roomId, Long roomEquipmentId, String newStatus, String note) throws SQLException {
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mô tả sự cố.");
        }
        housekeepingDao.reportIssue(roomId, roomEquipmentId, newStatus, note);
    }

    public List<HousekeepingTask> findIssueTasks(String keyword, Integer floor, String taskType, String status,
                                                  String sortColumn, String direction, int page, int pageSize) throws SQLException {
        int limit = Math.max(1, Math.min(pageSize, 1000));
        int offset = Math.max(0, (page - 1) * limit);
        return housekeepingDao.findIssueTasks(keyword, floor, taskType, status, sortColumn, direction, offset, limit);
    }

    public List<HousekeepingTask> findIssueTasks(String keyword, Integer floor, String sortColumn, String direction, int page, int pageSize) throws SQLException {
        return findIssueTasks(keyword, floor, null, null, sortColumn, direction, page, pageSize);
    }

    public int countIssueTasks(String keyword, Integer floor, String taskType, String status) throws SQLException {
        return housekeepingDao.countIssueTasks(keyword, floor, taskType, status);
    }

    public int countIssueTasks(String keyword, Integer floor) throws SQLException {
        return countIssueTasks(keyword, floor, null, null);
    }

    public List<HousekeepingTask.EquipmentCheck> findDamagedEquipmentById(long roomEquipmentId) throws SQLException {
        return housekeepingDao.findDamagedEquipmentById(roomEquipmentId);
    }

    public List<HousekeepingTask.EquipmentCheck> findDamagedEquipments(long roomId) throws SQLException {
        return housekeepingDao.findDamagedEquipments(roomId);
    }

    public List<HousekeepingTask.EquipmentCheck> findAllEquipmentsInRoom(long roomId) throws SQLException {
        return housekeepingDao.findEquipment(roomId, null);
    }

    public void verifyMaintenance(long taskId, long staffId, List<Long> equipmentIds, String note) throws SQLException {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn thiết bị nào đã được sửa.");
        }
        maintenanceLogDao.insertMaintenanceLogs(taskId, staffId, equipmentIds, note);
    }

    public List<MaintenanceLog> findLogsByTaskId(long taskId) throws SQLException {
        return maintenanceLogDao.findLogsByTaskId(taskId);
    }
}