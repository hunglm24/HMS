package model;

public class HousekeepingTask {
    private int taskId;
    private int roomId;
    private Integer assignedTo;
    private String status;
    private java.util.Date createdAt;
    private java.util.Date startedAt;
    private java.util.Date completedAt;
    private Integer completedBy;
    private String completionNote;
    private java.util.Date updatedAt;
    private String roomNumber;
    private int floor;
    private String roomTypeName;
    private String roomHousekeepingStatus;
    private String assignedStaffName;
    private String completedStaffName;

    public HousekeepingTask() {
    }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public Integer getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Integer assignedTo) { this.assignedTo = assignedTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.util.Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }
    public java.util.Date getStartedAt() { return startedAt; }
    public void setStartedAt(java.util.Date startedAt) { this.startedAt = startedAt; }
    public java.util.Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(java.util.Date completedAt) { this.completedAt = completedAt; }
    public Integer getCompletedBy() { return completedBy; }
    public void setCompletedBy(Integer completedBy) { this.completedBy = completedBy; }
    public String getCompletionNote() { return completionNote; }
    public void setCompletionNote(String completionNote) { this.completionNote = completionNote; }
    public java.util.Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.util.Date updatedAt) { this.updatedAt = updatedAt; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }
    public String getRoomHousekeepingStatus() { return roomHousekeepingStatus; }
    public void setRoomHousekeepingStatus(String roomHousekeepingStatus) { this.roomHousekeepingStatus = roomHousekeepingStatus; }
    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }
    public String getCompletedStaffName() { return completedStaffName; }
    public void setCompletedStaffName(String completedStaffName) { this.completedStaffName = completedStaffName; }
}
