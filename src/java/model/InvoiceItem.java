package model;
import java.math.BigDecimal;
public class InvoiceItem extends BaseEntity {
 private long invoiceId; private Long damageReportId; private String itemType,description; private int quantity; private BigDecimal unitPrice,totalPrice;
 public long getInvoiceId(){return invoiceId;} public void setInvoiceId(long v){invoiceId=v;} public Long getDamageReportId(){return damageReportId;} public void setDamageReportId(Long v){damageReportId=v;} public String getItemType(){return itemType;} public void setItemType(String v){itemType=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;} public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;} public BigDecimal getTotalPrice(){return totalPrice;} public void setTotalPrice(BigDecimal v){totalPrice=v;}
}
