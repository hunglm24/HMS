package dto;

import model.RoomType;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class CartItem {
    private RoomType roomType;
    private int quantity;
    private Date checkInDate;
    private Date checkOutDate;
    private BigDecimal pricePerNight;
    private BigDecimal subtotal;

    public CartItem() {
    }

    public CartItem(RoomType roomType, int quantity, Date checkInDate, Date checkOutDate, BigDecimal pricePerNight) {
        this.roomType = roomType;
        this.quantity = quantity;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.pricePerNight = pricePerNight;
        calculateSubtotal();
    }

    public void calculateSubtotal() {
        if (checkInDate != null && checkOutDate != null && pricePerNight != null) {
            long days = ChronoUnit.DAYS.between(checkInDate.toLocalDate(), checkOutDate.toLocalDate());
            if (days <= 0) days = 1; // at least 1 night
            this.subtotal = pricePerNight.multiply(new BigDecimal(days)).multiply(new BigDecimal(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
        calculateSubtotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
        calculateSubtotal();
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
        calculateSubtotal();
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
        calculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public int getNumberOfNights() {
        if (checkInDate != null && checkOutDate != null) {
            long days = ChronoUnit.DAYS.between(checkInDate.toLocalDate(), checkOutDate.toLocalDate());
            return days > 0 ? (int) days : 1;
        }
        return 1;
    }
}
