package model;
import java.math.BigDecimal;
public class BookingRoom extends BaseEntity {
 private long bookingId,roomId; private BigDecimal pricePerNight,subtotal; private int numberOfNights;
 public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;} public long getRoomId(){return roomId;} public void setRoomId(long v){roomId=v;}
 public BigDecimal getPricePerNight(){return pricePerNight;} public void setPricePerNight(BigDecimal v){pricePerNight=v;} public int getNumberOfNights(){return numberOfNights;} public void setNumberOfNights(int v){numberOfNights=v;} public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;}
}
