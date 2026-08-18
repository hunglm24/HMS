package service;

import dao.BookingDao;
import model.CheckInBookingSummary;
import model.RoomType;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CheckInService {
    public static final int PAGE_SIZE = 8;
    private static final Set<String> BOOKING_STATUSES = Set.of(
            "Pending", "Confirmed", "CheckedIn", "Cancelled");
    private static final Set<String> SCOPES = Set.of("today", "upcoming", "overdue", "all", "checkout_today", "checkout_upcoming", "checkout_overdue");
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "created", "b.created_at",
            "checkIn", "b.check_in_date",
            "checkOut", "b.check_out_date",
            "guest", "g.full_name",
            "status", "b.status",
            "roomType", "room_types"
    );

    private final BookingDao bookingDao;

    public CheckInService() {
        this(new BookingDao());
    }

    public CheckInService(BookingDao bookingDao) {
        this.bookingDao = bookingDao;
    }

    public CheckInPage getCheckInPage(String keyword, String bookingStatus, Integer roomTypeId,
                                      String scope, String sort, String direction,
                                      int requestedPage) throws SQLException {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedStatus = bookingStatus != null && BOOKING_STATUSES.contains(bookingStatus)
                ? bookingStatus : null;
        Integer normalizedRoomTypeId = roomTypeId != null && roomTypeId > 0 ? roomTypeId : null;
        String normalizedScope = scope != null && SCOPES.contains(scope) ? scope : "today";
        String normalizedSort = sort != null && SORT_COLUMNS.containsKey(sort) ? sort : "created";
        String normalizedDirection = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";

        int totalItems = bookingDao.countCheckInBookings(normalizedKeyword, normalizedStatus,
                normalizedRoomTypeId, normalizedScope, null, null, null);
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        int offset = (page - 1) * PAGE_SIZE;

        List<CheckInBookingSummary> bookings = bookingDao.findCheckInBookings(
                normalizedKeyword, normalizedStatus, normalizedRoomTypeId, normalizedScope,
                SORT_COLUMNS.get(normalizedSort), normalizedDirection, offset, PAGE_SIZE, null, null, null);
        return new CheckInPage(bookings, page, totalPages, totalItems, normalizedKeyword,
                normalizedStatus, normalizedRoomTypeId, normalizedScope, normalizedSort,
                normalizedDirection.toLowerCase());
    }

    public Optional<CheckInBookingSummary> findBookingById(int bookingId) throws SQLException {
        if (bookingId <= 0) {
            return Optional.empty();
        }
        return bookingDao.findCheckInBookingById(bookingId);
    }

    public List<RoomType> getRoomTypes() throws SQLException {
        return bookingDao.findRoomTypes();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String value = keyword.trim();
        return value.length() > 50 ? value.substring(0, 50) : value;
    }

    public static class CheckInPage {
        private final List<CheckInBookingSummary> bookings;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final String keyword;
        private final String bookingStatus;
        private final Integer roomTypeId;
        private final String scope;
        private final String sort;
        private final String direction;

        public CheckInPage(List<CheckInBookingSummary> bookings, int page, int totalPages,
                           int totalItems, String keyword, String bookingStatus,
                           Integer roomTypeId, String scope, String sort,
                           String direction) {
            this.bookings = bookings;
            this.page = page;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.keyword = keyword;
            this.bookingStatus = bookingStatus;
            this.roomTypeId = roomTypeId;
            this.scope = scope;
            this.sort = sort;
            this.direction = direction;
        }

        public List<CheckInBookingSummary> getBookings() {
            return bookings;
        }

        public int getPage() {
            return page;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getBookingStatus() {
            return bookingStatus;
        }

        public Integer getRoomTypeId() {
            return roomTypeId;
        }

        public String getScope() {
            return scope;
        }

        public String getSort() {
            return sort;
        }

        public String getDirection() {
            return direction;
        }
    }
}
