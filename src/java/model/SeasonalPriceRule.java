package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class SeasonalPriceRule extends BaseEntity {
    private long roomTypeId;
    private String roomTypeName;
    private String ruleName;
    private String ruleType;
    private Date startDate;
    private Date endDate;
    private BigDecimal pricePerNight;
    private BigDecimal surchargePercent;
    private String status;
    private Timestamp updatedAt;

    public long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(long roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public BigDecimal getSurchargePercent() { return surchargePercent; }
    public void setSurchargePercent(BigDecimal surchargePercent) { this.surchargePercent = surchargePercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
