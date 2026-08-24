package service;

import dao.AmenityDao;
import dao.RoomTypeDao;
import dao.RoomTypeAmenityDao;
import model.Amenity;
import model.RoomType;
import util.DBConnectionUtil;
import util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomTypeService {
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");
    private final RoomTypeDao roomTypeDao;
    private final AmenityDao amenityDao;
    private final RoomTypeAmenityDao roomTypeAmenityDao;

    public RoomTypeService() {
        this(new RoomTypeDao(), new AmenityDao(), new RoomTypeAmenityDao());
    }

    public RoomTypeService(RoomTypeDao roomTypeDao, AmenityDao amenityDao, RoomTypeAmenityDao roomTypeAmenityDao) {
        this.roomTypeDao = roomTypeDao;
        this.amenityDao = amenityDao;
        this.roomTypeAmenityDao = roomTypeAmenityDao;
    }

    // Return room types for the management list.
    public List<RoomType> findRoomTypes(String keyword, String status) {
        return findRoomTypes(keyword, status, "popular", "DESC");
    }

    // Return room types for the management list with popularity sorting.
    public List<RoomType> findRoomTypes(String keyword, String status, String sort, String direction) {
        String normalizedKeyword = ValidationUtil.normalizeLower(keyword);
        final String filterKeyword = normalizedKeyword.length() > 100
                ? normalizedKeyword.substring(0, 100) : normalizedKeyword;
        String normalizedStatus = ValidationUtil.optionalStatus(status, STATUSES);
        String normalizedSort = ValidationUtil.normalizeLower(sort);
        String normalizedDirection = "ASC".equalsIgnoreCase(direction) ? "ASC" : "DESC";

        List<RoomType> roomTypes = roomTypeDao.findAllWithRoomCounts();
        if (roomTypes == null) {
            return Collections.emptyList();
        }

        List<RoomType> filteredRoomTypes = roomTypes.stream()
                .filter(roomType -> matchesKeyword(roomType, filterKeyword))
                .filter(roomType -> normalizedStatus == null
                        || normalizedStatus.equalsIgnoreCase(roomType.getStatus()))
                .toList();

        List<RoomType> sortedRoomTypes = new ArrayList<>(filteredRoomTypes);
        if ("popular".equals(normalizedSort) || normalizedSort.isBlank()) {
            Comparator<RoomType> popularityComparator = Comparator.comparingInt(RoomType::getTotalQuantity)
                    .thenComparing(roomType -> roomType.getName() == null ? "" : roomType.getName(),
                            String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(roomType -> roomType.getId() == null ? Long.MAX_VALUE : roomType.getId());

            if ("DESC".equals(normalizedDirection)) {
                popularityComparator = Comparator.comparingInt(RoomType::getTotalQuantity).reversed()
                        .thenComparing(roomType -> roomType.getName() == null ? "" : roomType.getName(),
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(roomType -> roomType.getId() == null ? Long.MAX_VALUE : roomType.getId());
            }

            sortedRoomTypes.sort(popularityComparator);
        }

        return sortedRoomTypes;
    }

    // Validate and create a new room type.
    public boolean createRoomType(RoomType roomType) throws SQLException {
        return createRoomType(roomType, Collections.emptyList());
    }

    // Find a room type by id.
    public RoomType findRoomTypeById(long roomTypeId) {
        return roomTypeDao.findById(roomTypeId).orElse(null);
    }

    // Validate and create a new room type with optional amenity relations.
    public boolean createRoomType(RoomType roomType, List<Long> amenityIds) throws SQLException {
        return persistRoomType(roomType, amenityIds, false);
    }

    // Validate and update an existing room type with optional amenity relations.
    public boolean updateRoomType(RoomType roomType, List<Long> amenityIds) throws SQLException {
        return persistRoomType(roomType, amenityIds, true);
    }

    // Toggle a room type status between ACTIVE and INACTIVE without changing other fields.
    public boolean toggleRoomTypeStatus(long roomTypeId) throws SQLException {
        RoomType roomType = findRoomTypeById(roomTypeId);
        ValidationUtil.requireTrue(roomType != null, "Room type not found.");

        String currentStatus = ValidationUtil.normalizeUpper(roomType.getStatus());
        String nextStatus = "ACTIVE".equals(currentStatus) ? "INACTIVE" : "ACTIVE";
        roomType.setStatus(nextStatus);

        List<Long> selectedAmenityIds = findAmenitiesByRoomTypeId(roomTypeId).stream()
                .map(Amenity::getId)
                .collect(Collectors.toList());
        return updateRoomType(roomType, selectedAmenityIds);
    }

    // Return distinct statuses found in the room_types table, with a safe fallback.
    public List<String> findCreateStatuses() {
        List<String> statuses = roomTypeDao.findDistinctStatuses();
        if (statuses.isEmpty()) {
            return List.of("ACTIVE", "INACTIVE");
        }

        java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>(statuses);
        merged.add("ACTIVE");
        merged.add("INACTIVE");
        return List.copyOf(merged);
    }

    // Return distinct bed types found in the room_types table.
    public List<String> findBedTypes() {
        return roomTypeDao.findDistinctBedTypes();
    }

    // Return active amenities for the room type create form.
    public List<Amenity> findActiveAmenities() {
        return amenityDao.findActiveAmenities();
    }

    // Load amenities attached to a given room type.
    public List<Amenity> findAmenitiesByRoomTypeId(long roomTypeId) {
        if (roomTypeId <= 0L) {
            return Collections.emptyList();
        }
        return roomTypeAmenityDao.findAmenitiesByRoomTypeId(roomTypeId);
    }

    // Prevent duplicate room type names across the catalog.
    private void ensureRoomTypeNameUnique(String name) {
        ensureRoomTypeNameUnique(name, null);
    }

    // Prevent duplicate room type names across the catalog while excluding one record.
    private void ensureRoomTypeNameUnique(String name, Long excludedRoomTypeId) {
        String normalizedName = ValidationUtil.requireText(name, "Ten loai phong", 2, 100);
        List<RoomType> roomTypes = roomTypeDao.findAll();
        if (roomTypes == null) {
            roomTypes = Collections.emptyList();
        }

        boolean duplicated = roomTypes.stream()
                .filter(roomType -> excludedRoomTypeId == null
                        || roomType.getId() == null
                        || !excludedRoomTypeId.equals(roomType.getId()))
                .anyMatch(roomType -> roomType.getName() != null
                        && roomType.getName().equalsIgnoreCase(normalizedName));

        if (duplicated) {
            throw new IllegalArgumentException("Ten loai phong nay da ton tai.");
        }
    }

    // Validate common room type fields for create and update flows.
    private void validateRoomTypeCoreFields(RoomType roomType) {
        ValidationUtil.requireTrue(roomType != null, "Thong tin loai phong khong hop le.");

        String name = ValidationUtil.requireText(roomType.getName(), "Ten loai phong", 2, 100);
        String description = ValidationUtil.optionalText(roomType.getDescription(), 500);
        String bedType = ValidationUtil.optionalText(roomType.getBedType(), 100);
        java.math.BigDecimal sizeM2 = roomType.getSizeM2();
        int capacity = ValidationUtil.requirePositiveInt(String.valueOf(roomType.getCapacity()), "Capacity");
        Set<String> allowedStatuses = Set.copyOf(findCreateStatuses());
        String status = ValidationUtil.optionalStatus(roomType.getStatus(), allowedStatuses);
        List<String> allowedBedTypes = roomTypeDao.findDistinctBedTypes();

        roomType.setName(name);
        roomType.setDescription(description.isEmpty() ? null : description);
        roomType.setBedType(bedType.isEmpty() ? null : validateBedType(bedType, allowedBedTypes));
        if (sizeM2 != null) {
            ValidationUtil.requireTrue(sizeM2.signum() > 0, "Room size phai lon hon 0.");
        }
        roomType.setSizeM2(sizeM2);
        roomType.setCapacity(capacity);
        roomType.setBasePrice(ValidationUtil.requirePositiveBigDecimal(
                roomType.getBasePrice() == null ? null : roomType.getBasePrice().toPlainString(),
                "Base price"));
        roomType.setStatus(status == null ? "ACTIVE" : status);
    }

    // Persist a room type by create or update mode inside one transaction.
    private boolean persistRoomType(RoomType roomType, List<Long> amenityIds, boolean updating) throws SQLException {
        validateRoomTypeCoreFields(roomType);

        List<Amenity> activeAmenities = amenityDao.findActiveAmenities();
        Set<Long> allowedAmenityIds = activeAmenities.stream()
                .map(Amenity::getId)
                .collect(Collectors.toSet());
        List<Long> sanitizedAmenityIds = normalizeAmenityIds(amenityIds, allowedAmenityIds);

        if (updating) {
            ensureRoomTypeNameUnique(roomType.getName(), roomType.getId());
        } else {
            ensureRoomTypeNameUnique(roomType.getName());
        }

        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (updating) {
                    roomTypeDao.update(conn, roomType);
                    if (roomType.getId() != null) {
                        roomTypeAmenityDao.replaceRoomTypeAmenities(conn, roomType.getId(), sanitizedAmenityIds);
                    }
                } else {
                    long roomTypeId = roomTypeDao.insert(conn, roomType);
                    roomTypeAmenityDao.replaceRoomTypeAmenities(conn, roomTypeId, sanitizedAmenityIds);
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Check whether a room type matches the keyword filter.
    private boolean matchesKeyword(RoomType roomType, String keyword) {
        if (keyword == null) {
            return true;
        }
        String name = ValidationUtil.normalizeLower(roomType.getName());
        String description = ValidationUtil.normalizeLower(roomType.getDescription());
        return name.contains(keyword) || description.contains(keyword);
    }

    // Validate bed type against the allowed database values when available.
    private String validateBedType(String bedType, List<String> allowedBedTypes) {
        if (allowedBedTypes == null || allowedBedTypes.isEmpty()) {
            return bedType;
        }

        boolean valid = allowedBedTypes.stream()
                .filter(value -> value != null && !value.isBlank())
                .anyMatch(value -> value.equalsIgnoreCase(bedType));

        if (!valid) {
            throw new IllegalArgumentException("Bed type khong hop le.");
        }
        return bedType;
    }

    // Normalize amenity ids and reject unknown ids.
    private List<Long> normalizeAmenityIds(List<Long> amenityIds, Set<Long> allowedAmenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return Collections.emptyList();
        }

        return amenityIds.stream()
                .filter(id -> id != null && id > 0L)
                .distinct()
                .peek(id -> ValidationUtil.requireTrue(allowedAmenityIds.contains(id), "Amenity khong hop le."))
                .toList();
    }
}
