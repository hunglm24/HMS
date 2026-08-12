package model;

public class RoomType {
    private int roomTypeId;
    private String typeName;
    private String description;
    private double basePrice;
    private int maxOccupancy;
    private String imageUrl;

    public RoomType() {
    }

    public RoomType(int roomTypeId, String typeName, String description, double basePrice, int maxOccupancy, String imageUrl) {
        this.roomTypeId = roomTypeId;
        this.typeName = typeName;
        this.description = description;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
        this.imageUrl = imageUrl;
    }

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
