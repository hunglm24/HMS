package service;

import dao.RoomDao;
import dao.RoomTypeDao;
import model.Room;
import model.RoomType;
import util.DBConnectionUtil;
import util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class RoomService {
    private static final Set<String> STATUSES = Set.of(
            "AVAILABLE", "OCCUPIED", "CLEANING", "MAINTENANCE", "NOT_READY", "INSPECTION", "INACTIVE");
    private static final Set<String> LOCKED_EDIT_STATUSES = Set.of("OCCUPIED", "NOT_READY", "INACTIVE");
    private static final Pattern ROOM_DESCRIPTION_PATTERN = Pattern.compile("(?s)^.{1,100}$");

    private final RoomDao roomDao;
    private final RoomTypeDao roomTypeDao;

    public RoomService() {
        this(new RoomDao(), new RoomTypeDao());
    }

    public RoomService(RoomDao roomDao, RoomTypeDao roomTypeDao) {
        this.roomDao = roomDao;
        this.roomTypeDao = roomTypeDao;
    }

    // Return every room with its room type name for display.
    public List<Room> getAllRooms() {
        return roomDao.findAllWithRoomTypeName();
    }

    // Return only rooms that are still selectable in normal workflows.
    public List<Room> getActiveRooms() {
        return roomDao.findActiveWithRoomTypeName();
    }

    // Filter rooms in memory for the current UI state.
    public List<Room> findRooms(String keyword, Long roomTypeId, Integer floor, String status) {
        // Normalize the incoming filters before applying them.
        String normalizedKeyword = ValidationUtil.normalizeLower(keyword);
        final String filterKeyword = normalizedKeyword.length() > 100
                ? normalizedKeyword.substring(0, 100) : normalizedKeyword;
        Long normalizedRoomTypeId = roomTypeId != null && roomTypeId > 0 ? roomTypeId : null;
        Integer normalizedFloor = floor != null && floor > 0 ? floor : null;
        String normalizedStatus = ValidationUtil.optionalStatus(status, STATUSES);

        // Filter the room list in memory for the current page state.
        return roomDao.findAllWithRoomTypeName().stream()
                .filter(room -> normalizedStatus != null
                        || !"INACTIVE".equalsIgnoreCase(room.getStatus()))
                .filter(room -> matchesKeyword(room, filterKeyword))
                .filter(room -> normalizedRoomTypeId == null || normalizedRoomTypeId.equals(room.getRoomTypeId()))
                .filter(room -> normalizedFloor == null || normalizedFloor.equals(room.getFloorNumber()))
                .filter(room -> normalizedStatus == null
                        || normalizedStatus.equalsIgnoreCase(room.getStatus()))
                .toList();
    }

    // Load one room by its identifier.
    public Optional<Room> getRoomById(long id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return roomDao.findById(id);
    }

    // Validate and persist a room.
    public boolean saveRoom(Room room) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            return saveRoom(conn, room);
        }
    }

    public boolean saveRoom(Connection conn, Room room) throws SQLException {
        // Validate and resolve dependencies before touching the database.
        validateRoom(room);
        ensureRoomTypeAssignable(room);
        Long roomId = room.getId();
        if (roomId != null && roomId > 0) {
            Room existingRoom = roomDao.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong."));
            applyEditableStatusRule(room, existingRoom);
        }
        ensureRoomNumberUnique(room.getRoomNumber(), roomId == null || roomId <= 0 ? null : roomId);
        if (roomId != null && roomId > 0) {
            return roomDao.update(conn, room);
        }
        return roomDao.insert(conn, room);
    }

    // Soft-disable a room by switching its status.
    public boolean deactivateRoom(long id) throws SQLException {
        // Soft-delete by changing the room status rather than removing it.
        ensureRoomCanBeDeactivated(id);
        Room value = roomDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong."));
        value.setStatus("INACTIVE");
        return roomDao.update(value);
    }

    // Load room types for the room form dropdown.
    public List<RoomType> getRoomTypeOptions() {
        return roomTypeDao.findActive();
    }

    // Validate fields shared by create and update flows.
    public void validateRoom(Room room) {
        // Enforce the shared rules for both create and update flows.
        ValidationUtil.requireTrue(room != null, "Thong tin phong khong hop le.");

        String roomNumber = ValidationUtil.requireDigitsText(room.getRoomNumber(), "So phong", 3, 3);
        ValidationUtil.requireTrue(room.getRoomTypeId() > 0, "Vui long chon loai phong.");

        Integer floorNumber = room.getFloorNumber();
        ValidationUtil.requireTrue(floorNumber == null || (floorNumber >= 1 && floorNumber <= 4),
                "Tang chi duoc trong khoang 1 den 4.");
        ensureRoomNumberMatchesFloor(roomNumber, floorNumber);

        String description = ValidationUtil.normalizeText(room.getDescription());
        if (!description.isEmpty()) {
            description = ValidationUtil.requirePatternText(
                    description,
                    "Mo ta phong",
                    1,
                    100,
                    ROOM_DESCRIPTION_PATTERN,
                    "Mo ta phong khong duoc qua 100 ky tu."
            );
        }
        String status = ValidationUtil.optionalStatus(room.getStatus(), STATUSES);

        room.setRoomNumber(roomNumber);
        room.setDescription(description.isEmpty() ? null : description);
        room.setStatus(status == null ? "AVAILABLE" : status);
    }

    // Keep protected statuses stable on edit, and reject manual switching to them.
    private void applyEditableStatusRule(Room incomingRoom, Room existingRoom) {
        String currentStatus = ValidationUtil.optionalStatus(existingRoom.getStatus(), STATUSES);
        String requestedStatus = ValidationUtil.optionalStatus(incomingRoom.getStatus(), STATUSES);
        if (currentStatus != null && LOCKED_EDIT_STATUSES.contains(currentStatus.toUpperCase())) {
            boolean sameStatus = requestedStatus != null && currentStatus.equalsIgnoreCase(requestedStatus);
            ValidationUtil.requireTrue(sameStatus, "Trang thai nay khong duoc phep chinh tay.");
            incomingRoom.setStatus(currentStatus);
            return;
        }

        if ("INACTIVE".equalsIgnoreCase(requestedStatus)) {
            throw new IllegalArgumentException("Trang thai nay khong duoc phep chinh tay.");
        }

        if (requestedStatus != null && LOCKED_EDIT_STATUSES.contains(requestedStatus.toUpperCase())) {
            throw new IllegalArgumentException("Trang thai nay khong duoc phep chinh tay.");
        }

        incomingRoom.setStatus(requestedStatus == null ? "AVAILABLE" : requestedStatus);
    }

    // Make sure the selected room type really exists before saving.
    public void ensureRoomTypeExists(long roomTypeId) {
        // Make sure the selected room type is still present in the catalog.
        ValidationUtil.requireTrue(roomTypeId > 0 && roomTypeDao.findById(roomTypeId).isPresent(),
                "Loai phong khong ton tai.");
    }

    // Allow new rooms to use active types only, while edits can keep the current inactive type.
    private void ensureRoomTypeAssignable(Room room) {
        ValidationUtil.requireTrue(room != null, "Thong tin phong khong hop le.");
        long roomTypeId = room.getRoomTypeId();
        ValidationUtil.requireTrue(roomTypeId > 0, "Vui long chon loai phong.");

        RoomType selectedRoomType = roomTypeDao.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loai phong khong ton tai."));
        if (room.getId() == null || room.getId() <= 0) {
            ValidationUtil.requireTrue("ACTIVE".equalsIgnoreCase(selectedRoomType.getStatus()),
                    "Vui long chon loai phong dang hoat dong.");
            return;
        }

        boolean isCurrentRoomType = roomDao.findById(room.getId())
                .map(existingRoom -> existingRoom.getRoomTypeId() == roomTypeId)
                .orElse(false);
        ValidationUtil.requireTrue(isCurrentRoomType || "ACTIVE".equalsIgnoreCase(selectedRoomType.getStatus()),
                "Vui long chon loai phong dang hoat dong.");
    }

    // Prevent duplicate room numbers within the current dataset.
    public void ensureRoomNumberUnique(String roomNumber, Long excludeId) {
        // Compare room numbers case-insensitively and skip the current row on edit.
        String normalizedRoomNumber = ValidationUtil.requireDigitsText(roomNumber, "So phong", 3, 3);

        // Stop as soon as another active room already uses the same number.
        boolean duplicated = roomDao.findAll().stream()
                .anyMatch(room -> {
                    // Ignore rows without a number or the row currently being edited.
                    if (room.getRoomNumber() == null) return false;
                    if ("INACTIVE".equalsIgnoreCase(room.getStatus())) return false;
                    boolean sameNumber = room.getRoomNumber().equalsIgnoreCase(normalizedRoomNumber);
                    boolean sameId = excludeId != null && excludeId > 0 && room.getId() == excludeId;
                    return sameNumber && !sameId;
                });
        if (duplicated) {
            throw new IllegalArgumentException("So phong nay da ton tai.");
        }
    }

    // Keep room numbering aligned with the selected floor, e.g. floor 1 => 1xx.
    private void ensureRoomNumberMatchesFloor(String roomNumber, Integer floorNumber) {
        if (floorNumber == null) {
            return;
        }

        String expectedPrefix = String.valueOf(floorNumber);
        ValidationUtil.requireTrue(roomNumber != null
                        && roomNumber.length() == 3
                        && roomNumber.startsWith(expectedPrefix),
                "So phong phai dung dinh dang theo tang. Vi du tang 1 thi so phong phai la 1xx.");
    }

    // Guard the deactivate action until booking-related rules are implemented.
    public void ensureRoomCanBeDeactivated(long id) {
        ValidationUtil.requireTrue(id > 0, "Khong tim thay phong.");
        Room room = roomDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong."));
        ValidationUtil.requireTrue(!"OCCUPIED".equalsIgnoreCase(room.getStatus()),
                "Khong the ngung hoat dong phong dang co khach.");
    }

    private boolean matchesKeyword(Room room, String keyword) {
        if (keyword == null) {
            return true;
        }
        String roomNumber = ValidationUtil.normalizeLower(room.getRoomNumber());
        String roomTypeName = ValidationUtil.normalizeLower(room.getRoomTypeName());
        String description = ValidationUtil.normalizeLower(room.getDescription());
        return roomNumber.contains(keyword)
                || roomTypeName.contains(keyword)
                || description.contains(keyword);
    }

    public int getMaxFloor() {
        return roomDao.getMaxFloor();
    }

    public List<Integer> getDistinctFloors() {
        return roomDao.getDistinctFloors();
    }
}
