package service;

import dao.AmenityDao;
import model.Amenity;
import util.DBConnectionUtil;
import util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AmenityService {
    private static final Set<String> AMENITY_STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> FILTER_STATUSES = Set.of("ACTIVE", "INACTIVE", "ALL");
    private final AmenityDao amenityDao;

    public AmenityService() {
        this(new AmenityDao());
    }

    public AmenityService(AmenityDao amenityDao) {
        this.amenityDao = amenityDao;
    }

    // Return all amenity items for the management list.
    public List<Amenity> findAmenities() {
        List<Amenity> amenities = amenityDao.findAll();
        return amenities == null ? Collections.emptyList() : amenities;
    }

    // Return filtered amenity items for the management list.
    public List<Amenity> findAmenities(String keyword, String status) {
        String normalizedKeyword = ValidationUtil.normalizeLower(keyword);
        String filterKeyword = normalizedKeyword.length() > 100
                ? normalizedKeyword.substring(0, 100)
                : normalizedKeyword;
        String normalizedStatus = ValidationUtil.optionalStatus(status, FILTER_STATUSES);

        List<Amenity> amenities = findAmenities();
        if (amenities.isEmpty()) {
            return Collections.emptyList();
        }

        return amenities.stream()
                .filter(amenity -> matchesKeyword(amenity, filterKeyword))
                .filter(amenity -> normalizedStatus == null
                        || "ALL".equals(normalizedStatus)
                        || normalizedStatus.equalsIgnoreCase(amenity.getStatus()))
                .toList();
    }

    // Find one amenity item by id.
    public Amenity findAmenityById(long id) {
        return amenityDao.findById(id).orElse(null);
    }

    // Create a new amenity row after validation and uniqueness checks.
    public boolean createAmenity(Amenity amenity) throws SQLException {
        return persistAmenity(amenity, false);
    }

    // Update an existing amenity row after validation and uniqueness checks.
    public boolean updateAmenity(Amenity amenity) throws SQLException {
        return persistAmenity(amenity, true);
    }

    // Return the allowed status list used by the form radios.
    public List<String> findStatuses() {
        return List.of("ACTIVE", "INACTIVE");
    }

    // Check whether the amenity matches the typed keyword.
    private boolean matchesKeyword(Amenity amenity, String keyword) {
        if (ValidationUtil.isBlank(keyword)) {
            return true;
        }
        String name = ValidationUtil.normalizeLower(amenity == null ? null : amenity.getName());
        String description = ValidationUtil.normalizeLower(amenity == null ? null : amenity.getDescription());
        return name.contains(keyword) || description.contains(keyword);
    }

    // Validate the core amenity fields before persistence.
    private void validateAmenityCoreFields(Amenity amenity) {
        ValidationUtil.requireTrue(amenity != null, "Amenity is required.");
        amenity.setName(ValidationUtil.requireText(amenity.getName(), "Amenity name", 2, 100));
        amenity.setDescription(ValidationUtil.optionalText(amenity.getDescription(), 500));
        amenity.setIcon(ValidationUtil.requireText(amenity.getIcon(), "Icon class", 2, 50));
        amenity.setStatus(ValidationUtil.requireStatus(amenity.getStatus(), "Status", AMENITY_STATUSES));
    }

    // Ensure the amenity name is unique.
    private void ensureAmenityNameUnique(String name, Long excludedAmenityId) {
        String normalizedName = ValidationUtil.normalizeLower(name);
        boolean duplicate = findAmenities().stream()
                .filter(amenity -> amenity != null)
                .filter(amenity -> excludedAmenityId == null
                        || !excludedAmenityId.equals(amenity.getId()))
                .anyMatch(amenity -> normalizedName.equals(ValidationUtil.normalizeLower(amenity.getName())));
        ValidationUtil.requireTrue(!duplicate, "Amenity name already exists.");
    }

    // Shared create/update persistence flow.
    private boolean persistAmenity(Amenity amenity, boolean updating) throws SQLException {
        validateAmenityCoreFields(amenity);
        if (updating) {
            ValidationUtil.requireTrue(amenity.getId() != null && amenity.getId() > 0L, "Amenity id is required.");
            ensureAmenityNameUnique(amenity.getName(), amenity.getId());
        } else {
            ensureAmenityNameUnique(amenity.getName(), null);
        }

        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (updating) {
                    ValidationUtil.requireTrue(amenityDao.update(conn, amenity) > 0, "Amenity not found.");
                } else {
                    amenityDao.insert(conn, amenity);
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException sqlException) {
                    throw sqlException;
                }
                if (ex instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new SQLException("Failed to save amenity.", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
