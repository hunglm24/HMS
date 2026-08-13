package model;
import java.math.BigDecimal; import java.sql.Timestamp;
public class Invoice extends BaseEntity {
 private String invoiceCode,status; private long bookingId; private BigDecimal roomAmount,serviceAmount,damageAmount,discountAmount,taxAmount,totalAmount; private Timestamp updatedAt;
 public String getInvoiceCode(){return invoiceCode;} public void setInvoiceCode(String v){invoiceCode=v;} public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;}
 public BigDecimal getRoomAmount(){return roomAmount;} public void setRoomAmount(BigDecimal v){roomAmount=v;} public BigDecimal getServiceAmount(){return serviceAmount;} public void setServiceAmount(BigDecimal v){serviceAmount=v;} public BigDecimal getDamageAmount(){return damageAmount;} public void setDamageAmount(BigDecimal v){damageAmount=v;}
 public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal v){discountAmount=v;} public BigDecimal getTaxAmount(){return taxAmount;} public void setTaxAmount(BigDecimal v){taxAmount=v;} public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;} public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
