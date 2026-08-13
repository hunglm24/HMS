package model;
import java.sql.Date;
public class BookingGuest extends BaseEntity {
 private long bookingId; private String fullName,phone,identityNumber; private Date dateOfBirth; private boolean primaryGuest;
 public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public String getIdentityNumber(){return identityNumber;} public void setIdentityNumber(String v){identityNumber=v;} public Date getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(Date v){dateOfBirth=v;} public boolean isPrimaryGuest(){return primaryGuest;} public void setPrimaryGuest(boolean v){primaryGuest=v;}
}
