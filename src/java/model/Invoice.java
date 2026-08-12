package model;

public class Invoice {
    private int invoiceId;
    private int bookingId;
    private double totalAmount;
    private java.util.Date issuedAt;
    private int issuedBy;

    public Invoice() {
    }

    public Invoice(int invoiceId, int bookingId, double totalAmount, java.util.Date issuedAt, int issuedBy) {
        this.invoiceId = invoiceId;
        this.bookingId = bookingId;
        this.totalAmount = totalAmount;
        this.issuedAt = issuedAt;
        this.issuedBy = issuedBy;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public java.util.Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(java.util.Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public int getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(int issuedBy) {
        this.issuedBy = issuedBy;
    }

}
