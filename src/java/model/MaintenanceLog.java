package model;

public class MaintenanceLog {
    private int logId;
    private int equipmentId;
    private int performedBy;
    private java.util.Date maintenanceDate;
    private String content;
    private double cost;
    private String statusBefore;
    private String statusAfter;
    private String note;

    public MaintenanceLog() {
    }

    public MaintenanceLog(int logId, int equipmentId, int performedBy, java.util.Date maintenanceDate, String content, double cost, String statusBefore, String statusAfter, String note) {
        this.logId = logId;
        this.equipmentId = equipmentId;
        this.performedBy = performedBy;
        this.maintenanceDate = maintenanceDate;
        this.content = content;
        this.cost = cost;
        this.statusBefore = statusBefore;
        this.statusAfter = statusAfter;
        this.note = note;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(int performedBy) {
        this.performedBy = performedBy;
    }

    public java.util.Date getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(java.util.Date maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getStatusBefore() {
        return statusBefore;
    }

    public void setStatusBefore(String statusBefore) {
        this.statusBefore = statusBefore;
    }

    public String getStatusAfter() {
        return statusAfter;
    }

    public void setStatusAfter(String statusAfter) {
        this.statusAfter = statusAfter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
