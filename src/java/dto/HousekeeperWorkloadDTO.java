package dto;

import java.util.Date;

public class HousekeeperWorkloadDTO {
    private long userId;
    private String fullName;
    private String phone;
    private int inProgressCount;
    private int pendingCount;
    private int completedToday;
    private String currentRoomNumber;
    private Integer currentFloor;
    private Date currentStartedAt;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(int inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(int completedToday) {
        this.completedToday = completedToday;
    }

    public String getCurrentRoomNumber() {
        return currentRoomNumber;
    }

    public void setCurrentRoomNumber(String currentRoomNumber) {
        this.currentRoomNumber = currentRoomNumber;
    }

    public Integer getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(Integer currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Date getCurrentStartedAt() {
        return currentStartedAt;
    }

    public void setCurrentStartedAt(Date currentStartedAt) {
        this.currentStartedAt = currentStartedAt;
    }

    public boolean isBusy() {
        return inProgressCount > 0;
    }

    public int getActiveCount() {
        return inProgressCount + pendingCount;
    }

    public String getStatusBadgeText() {
        if (inProgressCount > 0) {
            StringBuilder sb = new StringBuilder("Đang làm P").append(currentRoomNumber != null ? currentRoomNumber : "??");
            if (pendingCount > 0) {
                sb.append(" (+").append(pendingCount).append(" chờ)");
            }
            return sb.toString();
        } else if (pendingCount > 0) {
            return pendingCount + " việc đang chờ";
        } else {
            return "Đang rảnh (0 việc)";
        }
    }

    public String getStatusBadgeClass() {
        if (inProgressCount > 0) {
            return "badge-busy";
        } else if (pendingCount > 0) {
            return "badge-pending";
        } else {
            return "badge-available";
        }
    }
}
