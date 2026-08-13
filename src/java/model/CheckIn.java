package model;
import java.sql.Timestamp;
public class CheckIn extends BaseEntity {
 private long bookingId,checkedInBy; private Timestamp actualCheckInTime; private String note;
 public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;} public long getCheckedInBy(){return checkedInBy;} public void setCheckedInBy(long v){checkedInBy=v;} public Timestamp getActualCheckInTime(){return actualCheckInTime;} public void setActualCheckInTime(Timestamp v){actualCheckInTime=v;} public String getNote(){return note;} public void setNote(String v){note=v;}
}
