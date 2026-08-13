package model;
import java.math.BigDecimal; import java.sql.Timestamp;
public class DamageReport extends BaseEntity {
 private long inspectionItemId,bookingId,roomEquipmentId; private String damageType,chargeStatus,note; private BigDecimal compensationAmount; private Timestamp updatedAt;
 public long getInspectionItemId(){return inspectionItemId;} public void setInspectionItemId(long v){inspectionItemId=v;} public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;} public long getRoomEquipmentId(){return roomEquipmentId;} public void setRoomEquipmentId(long v){roomEquipmentId=v;}
 public String getDamageType(){return damageType;} public void setDamageType(String v){damageType=v;} public BigDecimal getCompensationAmount(){return compensationAmount;} public void setCompensationAmount(BigDecimal v){compensationAmount=v;} public String getChargeStatus(){return chargeStatus;} public void setChargeStatus(String v){chargeStatus=v;} public String getNote(){return note;} public void setNote(String v){note=v;} public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
