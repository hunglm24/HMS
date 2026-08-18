package service;

import dao.RoomDao;
import dao.RoomTypeDao;
import model.Room;
import model.RoomType;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RoomService {
    private static final Set<String> STATUSES = Set.of(
            "AVAILABLE", "OCCUPIED", "CLEANING", "MAINTENANCE", "NOT_READY", "INSPECTION");

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
        // Validate and resolve dependencies before touching the database.
        validateRoom(room);
        ensureRoomTypeExists(room.getRoomTypeId());
        Long roomId = room.getId();
        ensureRoomNumberUnique(room.getRoomNumber(), roomId == null || roomId <= 0 ? null : roomId);
        if (roomId != null && roomId > 0) {
            return roomDao.update(room);
        }
        return roomDao.insert(room);
    }

    // Soft-disable a room by switching its status.
    public boolean deactivateRoom(long id) throws SQLException {
        // Soft-delete by changing the room status rather than removing it.
        if (id <= 0) {
            return false;
        }
        Optional<Room> room = roomDao.findById(id);
        if (room.isEmpty()) {
            return false;
        }
        Room value = room.get();
        value.setStatus("NOT_READY");
        return roomDao.update(value);
    }

    // Restore a previously inactive room.
    public boolean reactivateRoom(long id) throws SQLException {
        if (id <= 0) {
            return false;
        }
        Optional<Room> room = roomDao.findById(id);
        if (room.isEmpty()) {
            return false;
        }
        Room value = room.get();
        value.setStatus("AVAILABLE");
        return roomDao.update(value);
    }

    // Load room types for the room form dropdown.
    public List<RoomType> getRoomTypeOptions() {
        return roomTypeDao.findAll();
    }

    // Validate fields shared by create and update flows.
    public void validateRoom(Room room) {
        // Enforce the shared rules for both create and update flows.
        ValidationUtil.requireTrue(room != null, "Thong tin phong khong hop le.");

        String roomNumber = ValidationUtil.requireText(room.getRoomNumber(), "So phong", 1, 20);
        ValidationUtil.requireTrue(room.getRoomTypeId() > 0, "Vui long chon loai phong.");

        Integer floorNumber = room.getFloorNumber();
        ValidationUtil.requireTrue(floorNumber == null || floorNumber >= 0, "Tang khong duoc nho hon 0.");

        String description = ValidationUtil.optionalText(room.getDescription(), 500);
        String status = ValidationUtil.optionalStatus(room.getStatus(), STATUSES);

        room.setRoomNumber(roomNumber);
        room.setDescription(description.isEmpty() ? null : description);
        room.setStatus(status == null ? "AVAILABLE" : status);
    }

    // Make sure the selected room type really exists before saving.
    public void ensureRoomTypeExists(long roomTypeId) {
        // Make sure the selected room type is still present in the catalog.
        ValidationUtil.requireTrue(roomTypeId > 0 && roomTypeDao.findById(roomTypeId).isPresent(),
                "Loai phong khong ton tai.");
    }

    // Prevent duplicate room numbers within the current dataset.
    public void ensureRoomNumberUnique(String roomNumber, Long excludeId) {
        // Compare room numbers case-insensitively and skip the current row on edit.
        String normalizedRoomNumber = ValidationUtil.requireText(roomNumber, "So phong", 1, 20);

        // Stop as soon as another room already uses the same number.
        boolean duplicated = roomDao.findAll().stream()
                .anyMatch(room -> {
                    // Ignore rows without a number or the row currently being edited.
                    if (room.getRoomNumber() == null) return false;
                    boolean sameNumber = room.getRoomNumber().equalsIgnoreCase(normalizedRoomNumber);
                    boolean sameId = excludeId != null && excludeId > 0 && room.getId() == excludeId;
                    return sameNumber && !sameId;
                });
        if (duplicated) {
            throw new IllegalArgumentException("So phong nay da ton tai.");
        }
    }

    // Guard the deactivate action until booking-related rules are implemented.
    public void ensureRoomCanBeDeactivated(long id) {
        ValidationUtil.requireTrue(id > 0, "Khong tim thay phong.");
        ValidationUtil.requireTrue(roomDao.findById(id).isPresent(), "Khong tim thay phong.");
        // TODO: check active bookings before allowing deactivate.
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
}
