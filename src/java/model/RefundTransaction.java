package model;
import java.math.BigDecimal; import java.sql.Timestamp;
public class RefundTransaction {
 private Long id; private long refundRequestId,processedBy; private BigDecimal amount; private String transactionCode,proofImageUrl,status,note; private Timestamp processedAt;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public long getRefundRequestId(){return refundRequestId;} public void setRefundRequestId(long v){refundRequestId=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public String getTransactionCode(){return transactionCode;} public void setTransactionCode(String v){transactionCode=v;}
 public String getProofImageUrl(){return proofImageUrl;} public void setProofImageUrl(String v){proofImageUrl=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getNote(){return note;} public void setNote(String v){note=v;} public long getProcessedBy(){return processedBy;} public void setProcessedBy(long v){processedBy=v;} public Timestamp getProcessedAt(){return processedAt;} public void setProcessedAt(Timestamp v){processedAt=v;}
}
