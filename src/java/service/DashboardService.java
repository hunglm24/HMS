//file:noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection,SpellCheckingInspection,StringConcatenationMissingWhitespace,TextBlockMigration
package service;

import model.DashboardStats;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardService {
    public DashboardStats loadStats(long userId, String roleName) throws SQLException {
        DashboardStats stats = new DashboardStats();
        boolean housekeeping = "HOUSEKEEPING".equalsIgnoreCase(roleName);
        try (Connection connection = DBConnectionUtil.getConnection()) {
            loadRoomStats(connection, stats);
            loadBookingStats(connection, stats);
            loadRevenueStats(connection, stats);
            loadTaskStats(connection, stats, userId);
            loadAccountStats(connection, stats);
            stats.setRecentBookings(loadRecentBookings(connection));
            stats.setUrgentTasks(loadUrgentTasks(connection, userId, housekeeping));
        }
        return stats;
    }

    private void loadRoomStats(Connection connection, DashboardStats stats) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT "
                + "COUNT(*) AS total_rooms, "
                + "COALESCE(SUM(IF(status = 'AVAILABLE', 1, 0)), 0) AS available_rooms, "
                + "COALESCE(SUM(IF(status = 'OCCUPIED', 1, 0)), 0) AS occupied_rooms, "
                + "COALESCE(SUM(IF(status = 'CLEANING', 1, 0)), 0) AS cleaning_rooms, "
                + "COALESCE(SUM(IF(status = 'INSPECTION', 1, 0)), 0) AS inspection_rooms, "
                + "COALESCE(SUM(IF(status = 'MAINTENANCE', 1, 0)), 0) AS maintenance_rooms, "
                + "COALESCE(SUM(IF(status = 'NOT_READY', 1, 0)), 0) AS not_ready_rooms "
                + "FROM rooms "
                + "WHERE status <> 'INACTIVE'";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                stats.setTotalRooms(rs.getInt("total_rooms"));
                stats.setAvailableRooms(rs.getInt("available_rooms"));
                stats.setOccupiedRooms(rs.getInt("occupied_rooms"));
                stats.setCleaningRooms(rs.getInt("cleaning_rooms"));
                stats.setInspectionRooms(rs.getInt("inspection_rooms"));
                stats.setMaintenanceRooms(rs.getInt("maintenance_rooms"));
                stats.setNotReadyRooms(rs.getInt("not_ready_rooms"));
            }
        }
        loadTodayOccupancy(connection, stats);
    }

    private void loadTodayOccupancy(Connection connection, DashboardStats stats) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT COUNT(DISTINCT br.room_id) AS occupied_today "
                + "FROM booking_rooms br "
                + "JOIN bookings b ON b.id = br.booking_id "
                + "JOIN rooms r ON r.id = br.room_id "
                + "WHERE r.status <> 'INACTIVE' "
                + "AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKOUT_PENDING') "
                + "AND b.check_in_date <= CURDATE() "
                + "AND b.check_out_date > CURDATE()";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                stats.setTodayOccupiedRooms(rs.getInt("occupied_today"));
            }
        }
    }

    private void loadBookingStats(Connection connection, DashboardStats stats) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT "
                + "COALESCE(SUM(IF(check_in_date = CURDATE() AND status = 'CONFIRMED', 1, 0)), 0) AS arrivals_today, "
                + "COALESCE(SUM(IF(check_out_date = CURDATE() AND status IN ('CHECKED_IN', 'CHECKOUT_PENDING'), 1, 0)), 0) AS departures_today, "
                + "COALESCE(SUM(IF(status = 'PENDING_PAYMENT', 1, 0)), 0) AS pending_payments, "
                + "COALESCE(SUM(IF(status = 'CHECKOUT_PENDING', 1, 0)), 0) AS checkout_pending "
                + "FROM bookings";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                stats.setArrivalsToday(rs.getInt("arrivals_today"));
                stats.setDeparturesToday(rs.getInt("departures_today"));
                stats.setPendingPayments(rs.getInt("pending_payments"));
                stats.setCheckoutPending(rs.getInt("checkout_pending"));
            }
        }
    }

    private void loadRevenueStats(Connection connection, DashboardStats stats) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT "
                + "COALESCE(SUM(IF(DATE(paid_at) = CURDATE(), amount, 0)), 0) AS revenue_today, "
                + "COALESCE(SUM(IF(YEAR(paid_at) = YEAR(CURDATE()) "
                + "AND MONTH(paid_at) = MONTH(CURDATE()), amount, 0)), 0) AS revenue_month "
                + "FROM payments "
                + "WHERE status IN ('SUCCESS', 'COMPLETED')";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                stats.setRevenueToday(rs.getBigDecimal("revenue_today"));
                stats.setRevenueThisMonth(rs.getBigDecimal("revenue_month"));
            }
        }
    }

    private void loadTaskStats(Connection connection, DashboardStats stats, long userId) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT "
                + "COALESCE(SUM(IF(status IN ('PENDING', 'IN_PROGRESS') "
                + "AND task_type IN ('CHECKOUT_INSPECTION', 'CLEANING'), 1, 0)), 0) AS open_housekeeping, "
                + "COALESCE(SUM(IF(status IN ('PENDING', 'IN_PROGRESS') "
                + "AND task_type IN ('CHECKOUT_INSPECTION', 'CLEANING') "
                + "AND (assigned_to = ? OR assigned_to IS NULL), 1, 0)), 0) AS my_open_tasks, "
                + "COALESCE(SUM(IF(status IN ('PENDING', 'IN_PROGRESS') "
                + "AND task_type IN ('EQUIPMENT_REPAIR', 'MAINTENANCE_CHECK', 'EQUIPMENT_REPLACEMENT'), 1, 0)), 0) AS open_issues "
                + "FROM housekeeping_tasks";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    stats.setOpenHousekeepingTasks(rs.getInt("open_housekeeping"));
                    stats.setMyOpenTasks(rs.getInt("my_open_tasks"));
                    stats.setOpenIssueTasks(rs.getInt("open_issues"));
                }
            }
        }
    }

    private void loadAccountStats(Connection connection, DashboardStats stats) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT "
                + "COALESCE(SUM(IF(r.name <> 'CUSTOMER' AND a.status = 'ACTIVE', 1, 0)), 0) AS active_staff, "
                + "COALESCE(SUM(IF(r.name = 'CUSTOMER' AND a.status = 'ACTIVE', 1, 0)), 0) AS active_customers "
                + "FROM accounts a "
                + "JOIN roles r ON r.id = a.role_id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                stats.setActiveStaff(rs.getInt("active_staff"));
                stats.setActiveCustomers(rs.getInt("active_customers"));
            }
        }
    }

    private List<DashboardStats.RecentBooking> loadRecentBookings(Connection connection) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT b.booking_code, "
                + "COALESCE(bg.full_name, a.full_name, 'Guest') AS guest_name, "
                + "b.status, b.check_in_date, b.total_amount "
                + "FROM bookings b "
                + "LEFT JOIN booking_guests bg ON bg.booking_id = b.id AND bg.is_primary_guest = TRUE "
                + "LEFT JOIN accounts a ON a.id = b.customer_id "
                + "ORDER BY b.created_at DESC, b.id DESC "
                + "LIMIT 6";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<DashboardStats.RecentBooking> bookings = new ArrayList<>();
            while (rs.next()) {
                DashboardStats.RecentBooking booking = new DashboardStats.RecentBooking();
                booking.setBookingCode(rs.getString("booking_code"));
                booking.setGuestName(rs.getString("guest_name"));
                booking.setStatus(rs.getString("status"));
                booking.setCheckInDate(rs.getDate("check_in_date"));
                booking.setTotalAmount(rs.getBigDecimal("total_amount"));
                bookings.add(booking);
            }
            return bookings;
        }
    }

    private List<DashboardStats.UrgentTask> loadUrgentTasks(Connection connection, long userId, boolean housekeeping) throws SQLException {
        //noinspection SqlNoDataSourceInspection,SqlResolve,SqlDialectInspection
        String sql = "SELECT ht.id, r.room_number, ht.task_type, ht.priority, ht.status, "
                + "COALESCE(a.full_name, 'Unassigned') AS staff_name "
                + "FROM housekeeping_tasks ht "
                + "JOIN rooms r ON r.id = ht.room_id "
                + "LEFT JOIN accounts a ON a.id = ht.assigned_to "
                + "WHERE ht.status IN ('PENDING', 'IN_PROGRESS') "
                + (housekeeping ? "AND (ht.assigned_to = ? OR ht.assigned_to IS NULL) " : "")
                + "ORDER BY IF(ht.priority IN ('URGENT', 'HIGH'), 1, IF(ht.priority = 'NORMAL', 2, 3)), "
                + "ht.created_at, ht.id "
                + "LIMIT 6";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (housekeeping) {
                statement.setLong(1, userId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<DashboardStats.UrgentTask> tasks = new ArrayList<>();
                while (rs.next()) {
                    DashboardStats.UrgentTask task = new DashboardStats.UrgentTask();
                    task.setId(rs.getLong("id"));
                    task.setRoomNumber(rs.getString("room_number"));
                    task.setTaskType(rs.getString("task_type"));
                    task.setPriority(rs.getString("priority"));
                    task.setStatus(rs.getString("status"));
                    task.setStaffName(rs.getString("staff_name"));
                    tasks.add(task);
                }
                return tasks;
            }
        }
    }
}
