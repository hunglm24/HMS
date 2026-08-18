package model;

import java.sql.Timestamp;

public class RoomTypeAmenity {
    private long roomTypeId;
    private long amenityId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(long v) { roomTypeId = v; }

    public long getAmenityId() { return amenityId; }
    public void setAmenityId(long v) { amenityId = v; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { createdAt = v; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp v) { updatedAt = v; }
}
