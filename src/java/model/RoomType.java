package model;
import java.math.BigDecimal; import java.sql.Timestamp;
public class RoomType extends BaseEntity {
 private String name,description,status; private int capacity; private BigDecimal basePrice; private Timestamp updatedAt;
 public String getName(){return name;} public void setName(String v){name=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;} public int getCapacity(){return capacity;} public void setCapacity(int v){capacity=v;}
 public BigDecimal getBasePrice(){return basePrice;} public void setBasePrice(BigDecimal v){basePrice=v;} public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
