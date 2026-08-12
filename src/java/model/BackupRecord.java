package model;

public class BackupRecord {
    private int backupId;
    private String type;
    private int performedBy;
    private String status;
    private String filePath;
    private java.util.Date performedAt;

    public BackupRecord() {
    }

    public BackupRecord(int backupId, String type, int performedBy, String status, String filePath, java.util.Date performedAt) {
        this.backupId = backupId;
        this.type = type;
        this.performedBy = performedBy;
        this.status = status;
        this.filePath = filePath;
        this.performedAt = performedAt;
    }

    public int getBackupId() {
        return backupId;
    }

    public void setBackupId(int backupId) {
        this.backupId = backupId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(int performedBy) {
        this.performedBy = performedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public java.util.Date getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(java.util.Date performedAt) {
        this.performedAt = performedAt;
    }

}
