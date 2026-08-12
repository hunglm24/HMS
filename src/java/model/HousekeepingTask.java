package model;

public class HousekeepingTask {
    private int taskId;
    private int roomId;
    private int assignedTo;
    private String status;
    private java.util.Date updatedAt;

    public HousekeepingTask() {
    }

    public HousekeepingTask(int taskId, int roomId, int assignedTo, String status, java.util.Date updatedAt) {
        this.taskId = taskId;
        this.roomId = roomId;
        this.assignedTo = assignedTo;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(int assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.util.Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.util.Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}
