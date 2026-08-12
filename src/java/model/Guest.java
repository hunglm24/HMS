package model;

public class Guest {
    private int guestId;
    private String fullName;
    private String phone;
    private String email;
    private boolean hasAccount;
    private String passwordHash;
    private java.util.Date createdAt;

    public Guest() {
    }

    public Guest(int guestId, String fullName, String phone, String email, boolean hasAccount, String passwordHash, java.util.Date createdAt) {
        this.guestId = guestId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.hasAccount = hasAccount;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public boolean isHasAccount() {
        return hasAccount;
    }

    public void setHasAccount(boolean hasAccount) {
        this.hasAccount = hasAccount;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

}
