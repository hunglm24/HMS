package model;

import java.sql.Timestamp;

public class MaintenanceLog {
    private long id;
    private long housekeepingTaskId;
    private long roomEquipmentId;
    private String equipmentName;
    private String actionType;
    private String previousStatus;
    private String newStatus;
    private String note;
    private long confirmedBy;
    private String confirmedByName;
    private Timestamp confirmedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getHousekeepingTaskId() {
        return housekeepingTaskId;
    }

    public void setHousekeepingTaskId(long housekeepingTaskId) {
        this.housekeepingTaskId = housekeepingTaskId;
    }

    public long getRoomEquipmentId() {
        return roomEquipmentId;
    }

    public void setRoomEquipmentId(long roomEquipmentId) {
        this.roomEquipmentId = roomEquipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(long confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public String getConfirmedByName() {
        return confirmedByName;
    }

    public void setConfirmedByName(String confirmedByName) {
        this.confirmedByName = confirmedByName;
    }

    public Timestamp getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Timestamp confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}