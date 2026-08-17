package model;
import java.math.BigDecimal; import java.sql.Date; import java.sql.Timestamp;
public class Booking extends BaseEntity {
 private String bookingCode,bookingSource,status,cancellationReason; private Long customerId,createdBy; private Date checkInDate,checkOutDate; private Timestamp checkInDatetime,checkOutDatetime,cancelledAt,updatedAt;
 private BigDecimal totalRoomAmount,totalServiceAmount,totalDamageAmount,discountAmount,totalAmount;
 
 // Transient fields for UI display
 private String customerName, customerPhone, customerEmail, roomNumbers;
 public String getCustomerName() { return customerName; } public void setCustomerName(String v) { customerName = v; }
 public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String v) { customerPhone = v; }
 public String getCustomerEmail() { return customerEmail; } public void setCustomerEmail(String v) { customerEmail = v; }
 public String getRoomNumbers() { return roomNumbers; } public void setRoomNumbers(String v) { roomNumbers = v; }
 
 public String getBookingCode(){return bookingCode;} public void setBookingCode(String v){bookingCode=v;} public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;}
 public String getBookingSource(){return bookingSource;} public void setBookingSource(String v){bookingSource=v;} public Date getCheckInDate(){return checkInDate;} public void setCheckInDate(Date v){checkInDate=v;} public Date getCheckOutDate(){return checkOutDate;} public void setCheckOutDate(Date v){checkOutDate=v;}
 public Timestamp getCheckInDatetime(){return checkInDatetime;} public void setCheckInDatetime(Timestamp v){checkInDatetime=v;} public Timestamp getCheckOutDatetime(){return checkOutDatetime;} public void setCheckOutDatetime(Timestamp v){checkOutDatetime=v;}
 public BigDecimal getTotalRoomAmount(){return totalRoomAmount;} public void setTotalRoomAmount(BigDecimal v){totalRoomAmount=v;} public BigDecimal getTotalServiceAmount(){return totalServiceAmount;} public void setTotalServiceAmount(BigDecimal v){totalServiceAmount=v;}
 public BigDecimal getTotalDamageAmount(){return totalDamageAmount;} public void setTotalDamageAmount(BigDecimal v){totalDamageAmount=v;} public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal v){discountAmount=v;} public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getCancellationReason(){return cancellationReason;} public void setCancellationReason(String v){cancellationReason=v;} public Timestamp getCancelledAt(){return cancelledAt;} public void setCancelledAt(Timestamp v){cancelledAt=v;}
 public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long v){createdBy=v;} public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
