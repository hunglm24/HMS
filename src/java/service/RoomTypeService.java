package service;

import dao.RoomTypeDao;
import model.RoomType;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RoomTypeService {
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");
    private final RoomTypeDao roomTypeDao;

    public RoomTypeService() {
        this(new RoomTypeDao());
    }

    public RoomTypeService(RoomTypeDao roomTypeDao) {
        this.roomTypeDao = roomTypeDao;
    }

    // Return every room type for the management screen.
    public List<RoomType> getAllRoomTypes() {
        return roomTypeDao.findAll();
    }

    // Filter room types in memory for the current UI state.
    public List<RoomType> findRoomTypes(String keyword, String status) {
        String normalizedKeyword = ValidationUtil.normalizeLower(keyword);
        final String filterKeyword = normalizedKeyword.length() > 100
                ? normalizedKeyword.substring(0, 100) : normalizedKeyword;
        String normalizedStatus = ValidationUtil.optionalStatus(status, STATUSES);
        List<RoomType> roomTypes = roomTypeDao.findAll();
        return roomTypes.stream()
                .filter(roomType -> matchesKeyword(roomType, filterKeyword))
                .filter(roomType -> normalizedStatus == null
                        || normalizedStatus.equalsIgnoreCase(roomType.getStatus()))
                .toList();
    }

    // Load one room type by its identifier.
    public Optional<RoomType> getRoomTypeById(long id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return roomTypeDao.findById(id);
    }

    // Validate and persist a room type.
    public boolean saveRoomType(RoomType roomType) throws SQLException {
        validateRoomType(roomType);
        Long roomTypeId = roomType.getId();
        ensureRoomTypeNameUnique(roomType.getName(), roomTypeId == null || roomTypeId <= 0 ? null : roomTypeId);
        if (roomTypeId != null && roomTypeId > 0) {
            return roomTypeDao.update(roomType);
        }
        return roomTypeDao.insert(roomType);
    }

    // Soft-disable a room type by switching its status.
    public boolean deactivateRoomType(long id) throws SQLException {
        if (id <= 0) {
            return false;
        }
        Optional<RoomType> roomType = roomTypeDao.findById(id);
        if (roomType.isEmpty()) {
            return false;
        }
        RoomType value = roomType.get();
        value.setStatus("INACTIVE");
        return roomTypeDao.update(value);
    }

    // Restore a previously inactive room type.
    public boolean reactivateRoomType(long id) throws SQLException {
        if (id <= 0) {
            return false;
        }
        Optional<RoomType> roomType = roomTypeDao.findById(id);
        if (roomType.isEmpty()) {
            return false;
        }
        RoomType value = roomType.get();
        value.setStatus("ACTIVE");
        return roomTypeDao.update(value);
    }

    // Validate fields that are shared by create and update flows.
    public void validateRoomType(RoomType roomType) {
        ValidationUtil.requireTrue(roomType != null, "Thong tin loai phong khong hop le.");

        String name = ValidationUtil.requireText(roomType.getName(), "Ten loai phong", 2, 100);
        String description = ValidationUtil.optionalText(roomType.getDescription(), 500);
        int capacity = ValidationUtil.requirePositiveInt(String.valueOf(roomType.getCapacity()), "Suc chua");
        String status = ValidationUtil.optionalStatus(roomType.getStatus(), STATUSES);

        roomType.setName(name);
        roomType.setDescription(description.isEmpty() ? null : description);
        roomType.setCapacity(capacity);
        roomType.setBasePrice(ValidationUtil.requirePositiveBigDecimal(
                roomType.getBasePrice() == null ? null : roomType.getBasePrice().toPlainString(),
                "Gia co ban"));
        roomType.setStatus(status == null ? "ACTIVE" : status);
    }

    // Guard the deactivate action until booking-related rules are implemented.
    public void ensureRoomTypeCanBeDeactivated(long id) {
        ValidationUtil.requireTrue(id > 0, "Khong tim thay loai phong.");
        ValidationUtil.requireTrue(roomTypeDao.findById(id).isPresent(), "Khong tim thay loai phong.");
        // TODO: check active bookings before allowing deactivate.
    }

    // Prevent duplicate room type names across the catalog.
    public void ensureRoomTypeNameUnique(String name, Long excludeId) {
        String normalizedName = ValidationUtil.requireText(name, "Ten loai phong", 2, 100);
        boolean duplicated = roomTypeDao.findAll().stream()
                .anyMatch(roomType -> {
                    if (roomType.getName() == null) {
                        return false;
                    }
                    boolean sameName = roomType.getName().equalsIgnoreCase(normalizedName);
                    boolean sameId = excludeId != null && excludeId > 0 && roomType.getId() == excludeId;
                    return sameName && !sameId;
                });
        if (duplicated) {
            throw new IllegalArgumentException("Ten loai phong nay da ton tai.");
        }
    }

    private boolean matchesKeyword(RoomType roomType, String keyword) {
        if (keyword == null) {
            return true;
        }
        String name = ValidationUtil.normalizeLower(roomType.getName());
        String description = ValidationUtil.normalizeLower(roomType.getDescription());
        return name.contains(keyword) || description.contains(keyword);
    }
}
