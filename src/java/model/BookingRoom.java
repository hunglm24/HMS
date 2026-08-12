package model;

public class BookingRoom {
    private int bookingRoomId;
    private int bookingId;
    private int roomId;
    private double priceApplied;
    private boolean earlyCheckinRequested;
    private boolean lateCheckoutRequested;
    private String status;

    public BookingRoom() {
    }

    public BookingRoom(int bookingRoomId, int bookingId, int roomId, double priceApplied, boolean earlyCheckinRequested, boolean lateCheckoutRequested, String status) {
        this.bookingRoomId = bookingRoomId;
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.priceApplied = priceApplied;
        this.earlyCheckinRequested = earlyCheckinRequested;
        this.lateCheckoutRequested = lateCheckoutRequested;
        this.status = status;
    }

    public int getBookingRoomId() {
        return bookingRoomId;
    }

    public void setBookingRoomId(int bookingRoomId) {
        this.bookingRoomId = bookingRoomId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public double getPriceApplied() {
        return priceApplied;
    }

    public void setPriceApplied(double priceApplied) {
        this.priceApplied = priceApplied;
    }

    public boolean isEarlyCheckinRequested() {
        return earlyCheckinRequested;
    }

    public void setEarlyCheckinRequested(boolean earlyCheckinRequested) {
        this.earlyCheckinRequested = earlyCheckinRequested;
    }

    public boolean isLateCheckoutRequested() {
        return lateCheckoutRequested;
    }

    public void setLateCheckoutRequested(boolean lateCheckoutRequested) {
        this.lateCheckoutRequested = lateCheckoutRequested;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
