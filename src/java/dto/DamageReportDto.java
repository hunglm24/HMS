package dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DamageReportDto {
    private long id;
    private long bookingId;
    private String bookingCode;
    private String customerName;
    private long roomId;
    private String roomNumber;
    private int floorNumber;
    private long roomEquipmentId;
    private String equipmentName;
    private BigDecimal defaultPrice = BigDecimal.ZERO;
    private boolean maintainable = true;
    private String damageType; // DAMAGED, MISSING
    private BigDecimal suggestedAmount = BigDecimal.ZERO;
    private BigDecimal compensationAmount = BigDecimal.ZERO;
    private String chargeStatus; // PENDING, CHARGED, WAIVED, PAID
    private String note;
    private String housekeeperNote;
    private String inspectedByName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public long getRoomEquipmentId() { return roomEquipmentId; }
    public void setRoomEquipmentId(long roomEquipmentId) { this.roomEquipmentId = roomEquipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public BigDecimal getDefaultPrice() { return defaultPrice; }
    public void setDefaultPrice(BigDecimal defaultPrice) { this.defaultPrice = defaultPrice; }

    public boolean isMaintainable() { return maintainable; }
    public void setMaintainable(boolean maintainable) { this.maintainable = maintainable; }

    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }

    public String getDamageTypeLabel() {
        if ("MISSING".equalsIgnoreCase(damageType)) return "Mất / Thất lạc";
        if ("DAMAGED".equalsIgnoreCase(damageType)) return "Hư hỏng";
        return damageType != null ? damageType : "";
    }

    public BigDecimal getSuggestedAmount() { return suggestedAmount; }
    public void setSuggestedAmount(BigDecimal suggestedAmount) { this.suggestedAmount = suggestedAmount; }

    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal compensationAmount) { this.compensationAmount = compensationAmount; }

    public String getChargeStatus() { return chargeStatus; }
    public void setChargeStatus(String chargeStatus) { this.chargeStatus = chargeStatus; }

    public String getChargeStatusLabel() {
        if ("PENDING".equalsIgnoreCase(chargeStatus)) return "Chờ duyệt phạt";
        if ("CHARGED".equalsIgnoreCase(chargeStatus)) return "Đã duyệt phạt";
        if ("WAIVED".equalsIgnoreCase(chargeStatus)) return "Miễn phạt";
        if ("PAID".equalsIgnoreCase(chargeStatus)) return "Đã thanh toán";
        return chargeStatus != null ? chargeStatus : "";
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getHousekeeperNote() { return housekeeperNote; }
    public void setHousekeeperNote(String housekeeperNote) { this.housekeeperNote = housekeeperNote; }

    public String getInspectedByName() { return inspectedByName; }
    public void setInspectedByName(String inspectedByName) { this.inspectedByName = inspectedByName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
