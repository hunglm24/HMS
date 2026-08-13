package model;
import java.sql.Timestamp;
public class HousekeepingTask extends BaseEntity {
 private long roomId; private Long bookingRoomId,roomEquipmentId,assignedTo; private String taskType,priority,status,note; private Timestamp startedAt,completedAt;
 public long getRoomId(){return roomId;} public void setRoomId(long v){roomId=v;} public Long getBookingRoomId(){return bookingRoomId;} public void setBookingRoomId(Long v){bookingRoomId=v;} public Long getRoomEquipmentId(){return roomEquipmentId;} public void setRoomEquipmentId(Long v){roomEquipmentId=v;} public Long getAssignedTo(){return assignedTo;} public void setAssignedTo(Long v){assignedTo=v;}
 public String getTaskType(){return taskType;} public void setTaskType(String v){taskType=v;} public String getPriority(){return priority;} public void setPriority(String v){priority=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getNote(){return note;} public void setNote(String v){note=v;} public Timestamp getStartedAt(){return startedAt;} public void setStartedAt(Timestamp v){startedAt=v;} public Timestamp getCompletedAt(){return completedAt;} public void setCompletedAt(Timestamp v){completedAt=v;}
}
