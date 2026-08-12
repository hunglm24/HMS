package model;

public class Booking {
    private int bookingId;
    private int guestId;
    private String bookingType;
    private java.util.Date checkInDate;
    private java.util.Date checkOutDate;
    private String status;
    private double totalAmount;
    private double depositAmount;
    private int cancellationPolicyId;
    private java.util.Date createdAt;

    public Booking() {
    }

    public Booking(int bookingId, int guestId, String bookingType, java.util.Date checkInDate, java.util.Date checkOutDate, String status, double totalAmount, double depositAmount, int cancellationPolicyId, java.util.Date createdAt) {
        this.bookingId = bookingId;
        this.guestId = guestId;
        this.bookingType = bookingType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.depositAmount = depositAmount;
        this.cancellationPolicyId = cancellationPolicyId;
        this.createdAt = createdAt;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public java.util.Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(java.util.Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public java.util.Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(java.util.Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public int getCancellationPolicyId() {
        return cancellationPolicyId;
    }

    public void setCancellationPolicyId(int cancellationPolicyId) {
        this.cancellationPolicyId = cancellationPolicyId;
    }

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

}
