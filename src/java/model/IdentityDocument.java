package model;

public class IdentityDocument {
    private int documentId;
    private int bookingId;
    private String guestName;
    private String documentType;
    private String documentNumber;
    private int verifiedBy;
    private java.util.Date verifiedAt;

    public IdentityDocument() {
    }

    public IdentityDocument(int documentId, int bookingId, String guestName, String documentType, String documentNumber, int verifiedBy, java.util.Date verifiedAt) {
        this.documentId = documentId;
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = verifiedAt;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public int getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(int verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public java.util.Date getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(java.util.Date verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

}
