package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import model.RoomType;

public class CartItem {
    private RoomType roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int quantity;
    private int guests;
    private BigDecimal subtotalOverride;

    public CartItem(RoomType roomType, LocalDate checkIn, LocalDate checkOut, int quantity, int guests) {
        this.roomType = roomType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.quantity = quantity;
        this.guests = guests;
    }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
    
    public long getNumberOfNights() {
        return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
    }
    
    public BigDecimal getSubtotal() {
        if (subtotalOverride != null) {
            return subtotalOverride;
        }
        return roomType.getBasePrice().multiply(BigDecimal.valueOf(quantity)).multiply(BigDecimal.valueOf(getNumberOfNights()));
    }

    public BigDecimal getAveragePricePerNight() {
        long nights = getNumberOfNights();
        if (nights <= 0 || quantity <= 0) {
            return roomType.getBasePrice();
        }
        return getSubtotal().divide(
                BigDecimal.valueOf(nights).multiply(BigDecimal.valueOf(quantity)),
                0,
                java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getSubtotalOverride() { return subtotalOverride; }
    public void setSubtotalOverride(BigDecimal subtotalOverride) { this.subtotalOverride = subtotalOverride; }
}
