package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class RoomType extends BaseEntity {
    private String name, description, imageUrl, bedType, status;
    private int capacity, totalQuantity;
    private java.math.BigDecimal sizeM2;
    private BigDecimal basePrice;
    private Timestamp updatedAt;
    private int availableQuantity;

    public String getName() { return name; }
    public void setName(String v) { name = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { imageUrl = v; }

    public java.math.BigDecimal getSizeM2() { return sizeM2; }
    public void setSizeM2(java.math.BigDecimal v) { sizeM2 = v; }

    public String getBedType() { return bedType; }
    public void setBedType(String v) { bedType = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int v) { capacity = v; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int v) { totalQuantity = v; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal v) { basePrice = v; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp v) { updatedAt = v; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int v) { availableQuantity = v; }
}
