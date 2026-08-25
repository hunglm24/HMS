package model;

public class CheckInBookingSummary {
    private int bookingId;
    private String bookingCode;
    private int guestId;
    private String guestName;
    private String phone;
    private String email;
    private String bookingType;
    private java.util.Date checkInDate;
    private java.util.Date checkOutDate;
    private String status;
    private double totalRoomAmount;
    private double totalDamageAmount;
    private double totalAmount;
    private double depositAmount;
    private java.util.Date createdAt;
    private int roomCount;
    private String roomTypes;
    private String roomNumbers;
    private String note;

    public CheckInBookingSummary() {
    }

    public double getTotalRoomAmount() {
        return totalRoomAmount;
    }

    public void setTotalRoomAmount(double totalRoomAmount) {
        this.totalRoomAmount = totalRoomAmount;
    }

    public double getTotalDamageAmount() {
        return totalDamageAmount;
    }

    public void setTotalDamageAmount(double totalDamageAmount) {
        this.totalDamageAmount = totalDamageAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int roomCount) {
        this.roomCount = roomCount;
    }

    public String getRoomTypes() {
        return roomTypes;
    }

    public void setRoomTypes(String roomTypes) {
        this.roomTypes = roomTypes;
    }

    public String getRoomNumbers() {
        return roomNumbers;
    }

    public void setRoomNumbers(String roomNumbers) {
        this.roomNumbers = roomNumbers;
    }
}
