package model;

public class AuditLog {
    private int auditId;
    private int userId;
    private String action;
    private String objectReference;
    private String result;
    private java.util.Date timestamp;

    public AuditLog() {
    }

    public AuditLog(int auditId, int userId, String action, String objectReference, String result, java.util.Date timestamp) {
        this.auditId = auditId;
        this.userId = userId;
        this.action = action;
        this.objectReference = objectReference;
        this.result = result;
        this.timestamp = timestamp;
    }

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObjectReference() {
        return objectReference;
    }

    public void setObjectReference(String objectReference) {
        this.objectReference = objectReference;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public java.util.Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(java.util.Date timestamp) {
        this.timestamp = timestamp;
    }

}
