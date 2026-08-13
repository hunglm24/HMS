package model;
import java.math.BigDecimal; import java.sql.Timestamp;
public class Payment extends BaseEntity {
 private long bookingId; private BigDecimal amount; private String paymentMethod,paymentType,transactionCode,status; private Long processedBy; private Timestamp paidAt;
 public long getBookingId(){return bookingId;} public void setBookingId(long v){bookingId=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
 public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;} public String getPaymentType(){return paymentType;} public void setPaymentType(String v){paymentType=v;} public String getTransactionCode(){return transactionCode;} public void setTransactionCode(String v){transactionCode=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public Long getProcessedBy(){return processedBy;} public void setProcessedBy(Long v){processedBy=v;} public Timestamp getPaidAt(){return paidAt;} public void setPaidAt(Timestamp v){paidAt=v;}
}
