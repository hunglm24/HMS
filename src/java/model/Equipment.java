package model;

public class Equipment {
    private int equipmentId;
    private String equipmentName;
    private String category;
    private int roomId;
    private boolean isCritical;
    private int status;
    private java.util.Date importedAt;

    public Equipment() {
    }

    public Equipment(int equipmentId, String equipmentName, String category, int roomId, boolean isCritical, int status, java.util.Date importedAt) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.category = category;
        this.roomId = roomId;
        this.isCritical = isCritical;
        this.status = status;
        this.importedAt = importedAt;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public boolean isIsCritical() {
        return isCritical;
    }

    public void setIsCritical(boolean isCritical) {
        this.isCritical = isCritical;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public java.util.Date getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(java.util.Date importedAt) {
        this.importedAt = importedAt;
    }

}
