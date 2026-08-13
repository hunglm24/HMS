package model;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

public class HotelConfig extends BaseEntity {
    private String hotelName, address, phone, email;
    private Time checkInTime, checkOutTime;
    private BigDecimal sameDayRefundRate, beforeDayRefundRate, taxRate, serviceFeeRate;
    private Timestamp updatedAt;
    public String getHotelName(){return hotelName;} public void setHotelName(String v){hotelName=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public Time getCheckInTime(){return checkInTime;} public void setCheckInTime(Time v){checkInTime=v;}
    public Time getCheckOutTime(){return checkOutTime;} public void setCheckOutTime(Time v){checkOutTime=v;}
    public BigDecimal getSameDayRefundRate(){return sameDayRefundRate;} public void setSameDayRefundRate(BigDecimal v){sameDayRefundRate=v;}
    public BigDecimal getBeforeDayRefundRate(){return beforeDayRefundRate;} public void setBeforeDayRefundRate(BigDecimal v){beforeDayRefundRate=v;}
    public BigDecimal getTaxRate(){return taxRate;} public void setTaxRate(BigDecimal v){taxRate=v;}
    public BigDecimal getServiceFeeRate(){return serviceFeeRate;} public void setServiceFeeRate(BigDecimal v){serviceFeeRate=v;}
    public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
