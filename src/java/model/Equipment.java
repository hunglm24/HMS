package model;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Equipment extends BaseEntity {
    private String name, description, imageUrl, status;
    private BigDecimal defaultCompensationPrice;
    private Timestamp updatedAt;
    private boolean isMaintainable = true;

    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { imageUrl = v; }
    public BigDecimal getDefaultCompensationPrice() { return defaultCompensationPrice; }
    public void setDefaultCompensationPrice(BigDecimal v) { defaultCompensationPrice = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp v) { updatedAt = v; }
    public boolean isMaintainable() { return isMaintainable; }
    public boolean getIsMaintainable() { return isMaintainable; }
    public boolean getMaintainable() { return isMaintainable; }
    public void setMaintainable(boolean v) { isMaintainable = v; }
    public void setIsMaintainable(boolean v) { isMaintainable = v; }
}