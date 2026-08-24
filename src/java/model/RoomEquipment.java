package model;
import java.sql.Timestamp;
public class RoomEquipment extends BaseEntity {
 private long roomId,equipmentId; private int quantity; private String status,note; private Long updatedBy; private Timestamp updatedAt; private String equipmentName;
 public long getRoomId(){return roomId;} public void setRoomId(long v){roomId=v;} public long getEquipmentId(){return equipmentId;} public void setEquipmentId(long v){equipmentId=v;}
 public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getNote(){return note;} public void setNote(String v){note=v;}
 public Long getUpdatedBy(){return updatedBy;} public void setUpdatedBy(Long v){updatedBy=v;} public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
 public String getEquipmentName(){return equipmentName;} public void setEquipmentName(String v){equipmentName=v;}
}
