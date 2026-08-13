package model;
import java.sql.Timestamp;
public class EquipmentMaintenanceLog {
 private Long id,housekeepingTaskId,damageReportId; private long roomEquipmentId,confirmedBy; private String actionType,previousStatus,newStatus,note,proofImageUrl; private Timestamp confirmedAt;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public Long getHousekeepingTaskId(){return housekeepingTaskId;} public void setHousekeepingTaskId(Long v){housekeepingTaskId=v;} public long getRoomEquipmentId(){return roomEquipmentId;} public void setRoomEquipmentId(long v){roomEquipmentId=v;} public Long getDamageReportId(){return damageReportId;} public void setDamageReportId(Long v){damageReportId=v;}
 public String getActionType(){return actionType;} public void setActionType(String v){actionType=v;} public String getPreviousStatus(){return previousStatus;} public void setPreviousStatus(String v){previousStatus=v;} public String getNewStatus(){return newStatus;} public void setNewStatus(String v){newStatus=v;} public String getNote(){return note;} public void setNote(String v){note=v;} public String getProofImageUrl(){return proofImageUrl;} public void setProofImageUrl(String v){proofImageUrl=v;}
 public long getConfirmedBy(){return confirmedBy;} public void setConfirmedBy(long v){confirmedBy=v;} public Timestamp getConfirmedAt(){return confirmedAt;} public void setConfirmedAt(Timestamp v){confirmedAt=v;}
}
