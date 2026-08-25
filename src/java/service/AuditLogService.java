package service;

import dao.AuditLogDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.Account;

public class AuditLogService {
    private final AuditLogDao auditLogDao = new AuditLogDao();

    public void log(HttpServletRequest request, String action, String targetType,
                    Long targetId, String detail) {
        Long actorId = null;
        HttpSession session = request.getSession(false);
        Object currentUser = session == null ? null : session.getAttribute("currentUser");
        if (currentUser instanceof Account) {
            actorId = ((Account) currentUser).getId();
        }
        auditLogDao.log(actorId, action, targetType, targetId, detail, request.getRemoteAddr());
    }
}
