package model;

public class Payment {
    private int paymentId;
    private int bookingId;
    private String paymentType;
    private String method;
    private double amount;
    private String status;
    private String transactionCode;
    private java.util.Date paidAt;

    public Payment() {
    }

    public Payment(int paymentId, int bookingId, String paymentType, String method, double amount, String status, String transactionCode, java.util.Date paidAt) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.paymentType = paymentType;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.transactionCode = transactionCode;
        this.paidAt = paidAt;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public java.util.Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(java.util.Date paidAt) {
        this.paidAt = paidAt;
    }

}
