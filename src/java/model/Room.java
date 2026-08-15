package model;

import java.sql.Timestamp;

public class Room extends BaseEntity {

    private long roomTypeId;
    private String roomNumber, status, description;
    private Integer floorNumber;
    private Timestamp updatedAt;
    private String roomTypeName;

    public long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(long v) {
        roomTypeId = v;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String v) {
        roomNumber = v;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer v) {
        floorNumber = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp v) {
        updatedAt = v;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }
}
