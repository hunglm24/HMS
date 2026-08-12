package model;

public class RoomTypeAmenity {
    private int roomTypeId;
    private int amenityId;

    public RoomTypeAmenity() {
    }

    public RoomTypeAmenity(int roomTypeId, int amenityId) {
        this.roomTypeId = roomTypeId;
        this.amenityId = amenityId;
    }

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public int getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(int amenityId) {
        this.amenityId = amenityId;
    }

}
