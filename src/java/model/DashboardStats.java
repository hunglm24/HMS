//file:noinspection unused,SpellCheckingInspection
package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DashboardStats {
    private int totalRooms;
    private int availableRooms;
    private int occupiedRooms;
    private int todayOccupiedRooms;
    private int cleaningRooms;
    private int inspectionRooms;
    private int maintenanceRooms;
    private int notReadyRooms;
    private int arrivalsToday;
    private int departuresToday;
    private int pendingPayments;
    private int checkoutPending;
    private int openHousekeepingTasks;
    private int myOpenTasks;
    private int openIssueTasks;
    private int activeStaff;
    private int activeCustomers;
    private BigDecimal revenueToday = BigDecimal.ZERO;
    private BigDecimal revenueThisMonth = BigDecimal.ZERO;
    private List<RecentBooking> recentBookings = new ArrayList<>();
    private List<UrgentTask> urgentTasks = new ArrayList<>();

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    public int getOccupiedRooms() { return occupiedRooms; }
    public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }
    public int getTodayOccupiedRooms() { return todayOccupiedRooms; }
    public void setTodayOccupiedRooms(int todayOccupiedRooms) { this.todayOccupiedRooms = todayOccupiedRooms; }
    public int getOccupancyRatePercent() {
        return totalRooms == 0 ? 0 : (int) Math.round(todayOccupiedRooms * 100.0 / totalRooms);
    }
    public int getCleaningRooms() { return cleaningRooms; }
    public void setCleaningRooms(int cleaningRooms) { this.cleaningRooms = cleaningRooms; }
    public int getInspectionRooms() { return inspectionRooms; }
    public void setInspectionRooms(int inspectionRooms) { this.inspectionRooms = inspectionRooms; }
    public int getMaintenanceRooms() { return maintenanceRooms; }
    public void setMaintenanceRooms(int maintenanceRooms) { this.maintenanceRooms = maintenanceRooms; }
    public int getNotReadyRooms() { return notReadyRooms; }
    public void setNotReadyRooms(int notReadyRooms) { this.notReadyRooms = notReadyRooms; }
    public int getArrivalsToday() { return arrivalsToday; }
    public void setArrivalsToday(int arrivalsToday) { this.arrivalsToday = arrivalsToday; }
    public int getDeparturesToday() { return departuresToday; }
    public void setDeparturesToday(int departuresToday) { this.departuresToday = departuresToday; }
    public int getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(int pendingPayments) { this.pendingPayments = pendingPayments; }
    public int getCheckoutPending() { return checkoutPending; }
    public void setCheckoutPending(int checkoutPending) { this.checkoutPending = checkoutPending; }
    public int getOpenHousekeepingTasks() { return openHousekeepingTasks; }
    public void setOpenHousekeepingTasks(int openHousekeepingTasks) { this.openHousekeepingTasks = openHousekeepingTasks; }
    public int getMyOpenTasks() { return myOpenTasks; }
    public void setMyOpenTasks(int myOpenTasks) { this.myOpenTasks = myOpenTasks; }
    public int getOpenIssueTasks() { return openIssueTasks; }
    public void setOpenIssueTasks(int openIssueTasks) { this.openIssueTasks = openIssueTasks; }
    public int getActiveStaff() { return activeStaff; }
    public void setActiveStaff(int activeStaff) { this.activeStaff = activeStaff; }
    public int getActiveCustomers() { return activeCustomers; }
    public void setActiveCustomers(int activeCustomers) { this.activeCustomers = activeCustomers; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public void setRevenueToday(BigDecimal revenueToday) { this.revenueToday = revenueToday == null ? BigDecimal.ZERO : revenueToday; }
    public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
    public void setRevenueThisMonth(BigDecimal revenueThisMonth) { this.revenueThisMonth = revenueThisMonth == null ? BigDecimal.ZERO : revenueThisMonth; }
    public List<RecentBooking> getRecentBookings() { return recentBookings; }
    public void setRecentBookings(List<RecentBooking> recentBookings) { this.recentBookings = recentBookings == null ? new ArrayList<>() : recentBookings; }
    public List<UrgentTask> getUrgentTasks() { return urgentTasks; }
    public void setUrgentTasks(List<UrgentTask> urgentTasks) { this.urgentTasks = urgentTasks == null ? new ArrayList<>() : urgentTasks; }

    public static class RecentBooking {
        private String bookingCode;
        private String guestName;
        private String status;
        private Date checkInDate;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        public String getBookingCode() { return bookingCode; }
        public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getCheckInDate() { return checkInDate; }
        public void setCheckInDate(Date checkInDate) { this.checkInDate = checkInDate; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount; }
    }

    public static class UrgentTask {
        private long id;
        private String roomNumber;
        private String taskType;
        private String priority;
        private String status;
        private String staffName;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }
    }
}
