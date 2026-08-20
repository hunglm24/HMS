package model;

import java.math.BigDecimal;
import java.util.Date;

public class HousekeepingTask {
    private long taskId;
    private long roomId;
    private Long bookingRoomId;
    private Long roomEquipmentId;
    private Long assignedTo;
    private String taskType;
    private String priority;
    private String status;
    private String note;
    private Date createdAt;
    private Date startedAt;
    private Date completedAt;
    private String roomNumber;
    private Integer floorNumber;
    private String roomTypeName;
    private String roomStatus;
    private String assignedStaffName;
    private boolean actionReady;

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }
    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public Long getBookingRoomId() { return bookingRoomId; }
    public void setBookingRoomId(Long bookingRoomId) { this.bookingRoomId = bookingRoomId; }
    public Long getRoomEquipmentId() { return roomEquipmentId; }
    public void setRoomEquipmentId(Long roomEquipmentId) { this.roomEquipmentId = roomEquipmentId; }
    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }
    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }
    public String getRoomStatus() { return roomStatus; }
    public void setRoomStatus(String roomStatus) { this.roomStatus = roomStatus; }
    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }
    public boolean isActionReady() { return actionReady; }
    public void setActionReady(boolean actionReady) { this.actionReady = actionReady; }

    public String getStatusLabel() {
        if (status == null) return "--";
        switch (status) {
            case "WAITING": return "Chờ kiểm tra";
            case "PENDING": return "Chờ thực hiện";
            case "IN_PROGRESS": return "Đang thực hiện";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    public String getTaskTypeLabel() {
        if (taskType == null) return "--";
        switch (taskType) {
            case "CHECKOUT_INSPECTION": return "Kiểm tra sau checkout";
            case "CLEANING": return "Dọn phòng";
            default: return taskType;
        }
    }

    public static String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    public static class EquipmentCheck {
        private long roomEquipmentId;
        private String equipmentName;
        private int quantity;
        private String currentStatus;
        private String initialStatus;
        private Integer initialQuantity;
        private String conditionStatus = "NORMAL";
        private BigDecimal damageFee = BigDecimal.ZERO;
        private String note;

        public long getRoomEquipmentId() { return roomEquipmentId; }
        public void setRoomEquipmentId(long roomEquipmentId) { this.roomEquipmentId = roomEquipmentId; }
        public String getEquipmentName() { return equipmentName; }
        public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
        
        public String getCurrentStatusLabel() {
            if (currentStatus == null) return "";
            switch (currentStatus) {
                case "NORMAL": return "Bình thường";
                case "DAMAGED": return "Hư hỏng";
                case "MISSING": return "Thất lạc";
                case "WAITING_REPAIR": return "Chờ sửa chữa";
                case "WAITING_REPLACEMENT": return "Chờ thay thế";
                case "MAINTENANCE": return "Bảo trì định kỳ";
                default: return currentStatus;
            }
        }
        public String getInitialStatus() { return initialStatus; }
        public void setInitialStatus(String initialStatus) { this.initialStatus = initialStatus; }
        public Integer getInitialQuantity() { return initialQuantity; }
        public void setInitialQuantity(Integer initialQuantity) { this.initialQuantity = initialQuantity; }
        public String getConditionStatus() { return conditionStatus; }
        public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }
        public BigDecimal getDamageFee() { return damageFee; }
        public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class WorkItem {
        private String name;
        private boolean completed;

        public WorkItem() {}
        public WorkItem(String name, boolean completed) {
            this.name = name;
            this.completed = completed;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
