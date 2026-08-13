package model;
import java.sql.Timestamp;
public class Feedback extends BaseEntity {
 private long bookingId,customerId; private int rating; private String comment,status; private Timestamp updatedAt;
 public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;} public long getCustomerId(){return customerId;} public void setCustomerId(long v){customerId=v;} public int getRating(){return rating;} public void setRating(int v){rating=v;} public String getComment(){return comment;} public void setComment(String v){comment=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
