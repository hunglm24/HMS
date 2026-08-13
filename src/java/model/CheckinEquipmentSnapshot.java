package model;
public class CheckinEquipmentSnapshot extends BaseEntity {
 private long checkInId,bookingRoomId,roomEquipmentId; private String initialStatus,note; private int initialQuantity;
 public long getCheckInId(){return checkInId;} public void setCheckInId(long v){checkInId=v;} public long getBookingRoomId(){return bookingRoomId;} public void setBookingRoomId(long v){bookingRoomId=v;} public long getRoomEquipmentId(){return roomEquipmentId;} public void setRoomEquipmentId(long v){roomEquipmentId=v;}
 public String getInitialStatus(){return initialStatus;} public void setInitialStatus(String v){initialStatus=v;} public int getInitialQuantity(){return initialQuantity;} public void setInitialQuantity(int v){initialQuantity=v;} public String getNote(){return note;} public void setNote(String v){note=v;}
}
