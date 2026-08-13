package model;
import java.math.BigDecimal;
public class InspectionItem extends BaseEntity {
 private long inspectionId,roomEquipmentId; private String conditionStatus,note,imageUrl; private int quantity; private BigDecimal damageFee;
 public long getInspectionId(){return inspectionId;} public void setInspectionId(long v){inspectionId=v;} public long getRoomEquipmentId(){return roomEquipmentId;} public void setRoomEquipmentId(long v){roomEquipmentId=v;} public String getConditionStatus(){return conditionStatus;} public void setConditionStatus(String v){conditionStatus=v;}
 public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;} public BigDecimal getDamageFee(){return damageFee;} public void setDamageFee(BigDecimal v){damageFee=v;} public String getNote(){return note;} public void setNote(String v){note=v;} public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
}
