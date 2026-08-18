package service;

import dao.AuditLogDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.User;

public class AuditLogService {
    private final AuditLogDao auditLogDao = new AuditLogDao();

    public void log(HttpServletRequest request, String action, String targetType,
                    Long targetId, String detail) {
        Long actorId = null;
        HttpSession session = request.getSession(false);
        Object currentUser = session == null ? null : session.getAttribute("currentUser");
        // Use the legacy instanceof style for compatibility with the project source level.
        if (currentUser instanceof User) {
            actorId = ((User) currentUser).getId();
        }
        auditLogDao.log(actorId, action, targetType, targetId, detail, request.getRemoteAddr());
    }
}
