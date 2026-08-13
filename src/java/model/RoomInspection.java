package model;
import java.sql.Timestamp;
public class RoomInspection extends BaseEntity {
 private long housekeepingTaskId,bookingRoomId,inspectedBy; private String status,note; private Timestamp inspectedAt;
 public long getHousekeepingTaskId(){return housekeepingTaskId;} public void setHousekeepingTaskId(long v){housekeepingTaskId=v;} public long getBookingRoomId(){return bookingRoomId;} public void setBookingRoomId(long v){bookingRoomId=v;} public long getInspectedBy(){return inspectedBy;} public void setInspectedBy(long v){inspectedBy=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getNote(){return note;} public void setNote(String v){note=v;} public Timestamp getInspectedAt(){return inspectedAt;} public void setInspectedAt(Timestamp v){inspectedAt=v;}
}
