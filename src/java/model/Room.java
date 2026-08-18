package model;

import java.sql.Timestamp;

public class Room extends BaseEntity {

    private long roomTypeId;
    private String roomNumber, viewType, status, description;
    private Integer floorNumber;
    private Timestamp updatedAt;
    private String roomTypeName;
    private Long currentBookingId;
    private String currentBookingCode;
    private String currentGuestName;
    private String currentBookingStatus;
    private java.math.BigDecimal roomTypeBasePrice;

    public java.math.BigDecimal getRoomTypeBasePrice() {
        return roomTypeBasePrice;
    }

    public void setRoomTypeBasePrice(java.math.BigDecimal roomTypeBasePrice) {
        this.roomTypeBasePrice = roomTypeBasePrice;
    }

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

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String v) {
        viewType = v;
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

    public Long getCurrentBookingId() {
        return currentBookingId;
    }

    public void setCurrentBookingId(Long currentBookingId) {
        this.currentBookingId = currentBookingId;
    }

    public String getCurrentBookingCode() {
        return currentBookingCode;
    }

    public void setCurrentBookingCode(String currentBookingCode) {
        this.currentBookingCode = currentBookingCode;
    }

    public String getCurrentGuestName() {
        return currentGuestName;
    }

    public void setCurrentGuestName(String currentGuestName) {
        this.currentGuestName = currentGuestName;
    }

    public String getCurrentBookingStatus() {
        return currentBookingStatus;
    }

    public void setCurrentBookingStatus(String currentBookingStatus) {
        this.currentBookingStatus = currentBookingStatus;
    }
}
