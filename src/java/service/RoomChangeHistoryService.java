package service;

import dao.AuditLogDao;
import dao.UserDao;
import model.AuditLog;
import model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RoomChangeHistoryService {
    private final AuditLogDao auditLogDao;
    private final UserDao userDao;

    public RoomChangeHistoryService() {
        this(new AuditLogDao(), new UserDao());
    }

    public RoomChangeHistoryService(AuditLogDao auditLogDao, UserDao userDao) {
        this.auditLogDao = auditLogDao;
        this.userDao = userDao;
    }

    // Load room change history from the shared system log table.
    public List<AuditLog> getRoomChangeHistory(String bookingCode, LocalDate fromDate,
                                               LocalDate toDate, Long receptionistId,
                                               int limit) throws SQLException {
        return auditLogDao.findRoomChangeHistory(bookingCode, fromDate, toDate, receptionistId, limit);
    }

    public List<User> getReceptionists() throws SQLException {
        return userDao.findByRoleName("RECEPTIONIST");
    }
}
