package model;

public class GroupBookingMember {
    private int memberId;
    private int bookingRoomId;
    private String fullName;
    private String phone;

    public GroupBookingMember() {
    }

    public GroupBookingMember(int memberId, int bookingRoomId, String fullName, String phone) {
        this.memberId = memberId;
        this.bookingRoomId = bookingRoomId;
        this.fullName = fullName;
        this.phone = phone;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getBookingRoomId() {
        return bookingRoomId;
    }

    public void setBookingRoomId(int bookingRoomId) {
        this.bookingRoomId = bookingRoomId;
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

}
