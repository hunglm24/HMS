package model;

public class BookingSurcharge {
    private int bookingId;
    private int surchargeId;
    private double appliedAmount;

    public BookingSurcharge() {
    }

    public BookingSurcharge(int bookingId, int surchargeId, double appliedAmount) {
        this.bookingId = bookingId;
        this.surchargeId = surchargeId;
        this.appliedAmount = appliedAmount;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getSurchargeId() {
        return surchargeId;
    }

    public void setSurchargeId(int surchargeId) {
        this.surchargeId = surchargeId;
    }

    public double getAppliedAmount() {
        return appliedAmount;
    }

    public void setAppliedAmount(double appliedAmount) {
        this.appliedAmount = appliedAmount;
    }

}
