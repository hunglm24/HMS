package model;
import java.math.BigDecimal; import java.sql.Timestamp;
public class CheckOut extends BaseEntity {
 private long bookingId,checkedOutBy; private Timestamp actualCheckOutTime; private BigDecimal finalAmount; private String note;
 public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;} public long getCheckedOutBy(){return checkedOutBy;} public void setCheckedOutBy(long v){checkedOutBy=v;} public Timestamp getActualCheckOutTime(){return actualCheckOutTime;} public void setActualCheckOutTime(Timestamp v){actualCheckOutTime=v;} public BigDecimal getFinalAmount(){return finalAmount;} public void setFinalAmount(BigDecimal v){finalAmount=v;} public String getNote(){return note;} public void setNote(String v){note=v;}
}
